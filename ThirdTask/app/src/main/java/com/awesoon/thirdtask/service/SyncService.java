package com.awesoon.thirdtask.service;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.internal.util.Predicate;
import com.awesoon.thirdtask.db.DbHelper;
import com.awesoon.thirdtask.domain.SysItem;
import com.awesoon.thirdtask.service.container.SyncOptions;
import com.awesoon.thirdtask.util.CollectionUtils;
import com.awesoon.thirdtask.util.ColorUtils;
import com.awesoon.thirdtask.util.Function;
import com.awesoon.thirdtask.util.StringUtils;
import com.awesoon.thirdtask.web.rest.NotesBackendService;
import com.awesoon.thirdtask.web.rest.dto.CreateUserNoteResponseDto;
import com.awesoon.thirdtask.web.rest.dto.EditUserNoteResponseDto;
import com.awesoon.thirdtask.web.rest.dto.NotesBackendUserNote;
import com.awesoon.thirdtask.web.rest.dto.NotesBackendUserNoteDto;
import com.awesoon.thirdtask.web.rest.dto.NotesBackendUserNotesDto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import retrofit2.Response;

public class SyncService {
  private static final String TAG = "SyncService";

  private final Object syncLock = new Object();

  @Inject
  UserService userService;

  @Inject
  NotesBackendService notesBackendService;

  @Inject
  DbHelper dbHelper;

  public SyncService(UserService userService, NotesBackendService notesBackendService, DbHelper dbHelper) {
    this.userService = userService;
    this.notesBackendService = notesBackendService;
    this.dbHelper = dbHelper;
  }

  private NotesBackendUserNote createNoteDto(SysItem sysItem) {
    return new NotesBackendUserNote()
        .setId(sysItem.getRemoteId())
        .setTitle(sysItem.getTitle())
        .setDescription(sysItem.getBody())
        .setColor(ColorUtils.toHexString(sysItem.getColor()))
        .setImageUrl(sysItem.getImageUrl())
        .setCreated(sysItem.getCreatedTime().toString())
        .setEdited(sysItem.getLastEditedTime().toString())
        .setViewed(sysItem.getLastViewedTime().toString())
        .setExtra("Здесь могла быть ваша реклама");
  }


  public SyncResult syncAllNotes(final SyncOptions syncOptions) {
    synchronized (syncLock) {
      return doSyncAllNotes(syncOptions);
    }
  }

  @NonNull
  private SyncResult doSyncAllNotes(final SyncOptions syncOptions) {
    if (syncOptions.getUserId() == null) {
      syncOptions.setUserId(userService.getCurrentUserId());
    }
    Long userId = syncOptions.getUserId();

    SyncResult syncResult = new SyncResult();

    List<SysItem> unsyncedSysItems = dbHelper.findUnsyncedSysItems();

    final List<SysItem> newItems = CollectionUtils.filter(unsyncedSysItems, new Predicate<SysItem>() {
      @Override
      public boolean apply(SysItem sysItem) {
        return sysItem.isActive() && sysItem.getRemoteId() == null;
      }
    });
    final List<SysItem> removedItems = CollectionUtils.filter(unsyncedSysItems, new Predicate<SysItem>() {
      @Override
      public boolean apply(SysItem sysItem) {
        return sysItem.isRemoved();
      }
    });
    final List<SysItem> updatedItems = CollectionUtils.filter(unsyncedSysItems, new Predicate<SysItem>() {
      @Override
      public boolean apply(SysItem sysItem) {
        return sysItem.isActive() && sysItem.getRemoteId() != null;
      }
    });

    forceCreateRemoteItems(newItems, syncOptions, syncResult);
    if (!syncResult.isOk()) {
      return syncResult;
    }

    forceRemoveRemoteItems(removedItems, syncOptions, syncResult);
    if (!syncResult.isOk()) {
      return syncResult;
    }

    forceUpdateRemoteItems(updatedItems, syncOptions, syncResult);
    if (!syncResult.isOk()) {
      return syncResult;
    }

    if (syncResult.hasConflicts()) {
      // Если при синхронизации произошли конфликты - пусть пользователь их решит
      // а потом подтянем все изменения с сервера
      return syncResult;
    }

    // Несмотря на то, что обновление локальных изменений происходит в разрезе всех пользователей,
    // изменения с сервера подтягиваются только для текущего пользователя
    // К данному моменту все несохраненные изменения должны быть отправлены на сервер
    // Так что смело принимаем все изменения с сервера
    List<NotesBackendUserNote> allRemoteNotes = findAllUserNotes(userId);
    if (allRemoteNotes == null) {
      return syncResult.setFailure();
    }

    List<SysItem> userNotes = dbHelper.findSysItemsByUserId(userId);
    Map<Long, SysItem> remoteIdToLocalNote = CollectionUtils.collectToMap(userNotes,
        new Function<SysItem, Long>() {
          @Override
          public Long apply(SysItem sysItem) {
            return sysItem.getRemoteId();
          }
        });

    for (NotesBackendUserNote remoteNote : allRemoteNotes) {
      SysItem localLinkedNote = remoteIdToLocalNote.get(remoteNote.getId());
      if (localLinkedNote == null || !areNotesSame(localLinkedNote, remoteNote)) {
        // Нет заметки или они отличаются - обновляем локальную
        updateLocalNoteValuesAndSaveToDb(localLinkedNote, remoteNote, userId);
        if (localLinkedNote == null) {
          syncResult.increaseLocalAddedCount();
        } else {
          syncResult.increaseLocalChangedCount();
        }
      }

      remoteIdToLocalNote.remove(remoteNote.getId());
    }

    // Удаляем все заметки, которые не были обновлены
    for (Long remoteId : remoteIdToLocalNote.keySet()) {
      SysItem sysItem = remoteIdToLocalNote.get(remoteId);
      dbHelper.forceRemoveSysItem(sysItem);
      syncResult.increaseLocalRemovedCount();
    }

    return syncResult;
  }

  private void updateLocalNoteValues(SysItem localNote, NotesBackendUserNote remoteNote, long userId) {
    localNote
        .setUserId(userId)
        .setTitle(StringUtils.trim(remoteNote.getTitle()))
        .setBody(StringUtils.trim(remoteNote.getDescription()))
        .setColor(ColorUtils.colorStringToInt(remoteNote.getColor()))
        .setImageUrl(StringUtils.trim(remoteNote.getImageUrl()))
        .setRemoteId(remoteNote.getId())
        .setSynced(true);
  }

  private void updateLocalNoteValuesAndSaveToDb(SysItem localNote, NotesBackendUserNote remoteNote, long userId) {
    if (localNote == null) {
      localNote = new SysItem();
    }
    updateLocalNoteValues(localNote, remoteNote, userId);
    dbHelper.saveSysItemWithoutNotifications(localNote);
  }

  @Nullable
  private List<NotesBackendUserNote> findAllUserNotes(Long userId) {
    try {
      Response<NotesBackendUserNotesDto> response = notesBackendService.getAllUserNotesByUserId(userId).execute();
      if (!response.isSuccessful()) {
        Log.e(TAG, "Unable to retrieve all user notes: " + getResponseErrorBodyAsString(response));
        return null;
      }
      NotesBackendUserNotesDto body = response.body();
      if (body == null || !body.isOk() || body.getData() == null) {
        Log.e(TAG, "Unable to retrieve all user notes");
        return null;
      }

      return body.getData();
    } catch (Exception e) {
      Log.e(TAG, "Unable to retrieve all user notes", e);
      return null;
    }
  }

  private void forceUpdateRemoteItems(List<SysItem> updatedItems, final SyncOptions syncOptions,
                                      SyncResult syncResult) {
    for (SysItem updatedItem : updatedItems) {
      Long remoteId = updatedItem.getRemoteId();
      Long userId = updatedItem.getUserId();
      if (remoteId == null || userId == null) {
        continue;
      }

      List<SysItem> originalNotes = CollectionUtils.filter(dbHelper.findSysItemsByRemoteId(remoteId),
          new Predicate<SysItem>() {
            @Override
            public boolean apply(SysItem sysItem) {
              return sysItem.isHidden();
            }
          });

      if (originalNotes.isEmpty()) {
        // Нет синхронизированных исходных заметок
        // Создаем заметку на сервере
        if (!forceCreateRemoteItem(updatedItem)) {
          syncResult.setFailure();
          return;
        }

        continue;
      }

      NotesBackendUserNoteDto remoteNoteDto = getUserNoteByUserIdAndNoteId(userId, remoteId);
      if (remoteNoteDto == null) {
        syncResult.setFailure();
        return;
      }

      if (remoteNoteDto.isNotFound()) {
        // Если измененный элемент не найден на сервере - его надо либо создать, либо удалить
        if (syncOptions.getCreateLocallyEditedButRemotelyRemovedIds().contains(updatedItem.getId())) {
          // Пользователь выбрал создание элемента на сервере
          if (!forceCreateRemoteItem(updatedItem)) {
            syncResult.setFailure();
            return;
          }
          forceRemoveNotesFromDb(originalNotes);

        } else if (syncOptions.getRemoveLocallyEditedButRemotelyRemovedIds().contains(updatedItem.getId())) {
          // Пользователь выбрал удаление локального элемента
          dbHelper.forceRemoveSysItemById(updatedItem.getId());
          forceRemoveNotesFromDb(originalNotes);
          syncResult.increaseLocalRemovedCount();
        } else {
          // Пользователь не определился с выбором
          syncResult.addEditConflict(updatedItem, null);
        }
      } else {
        // Заметка найдена на сервере, необходимо сравнить заметку сервера и исходную заметку
        SysItem originalNote = originalNotes.get(0);
        boolean doUpdate = true;
        NotesBackendUserNote remoteNote = remoteNoteDto.getData();
        if (!areNotesSame(originalNote, remoteNote)) {
          // Исходная заметка и заметка на сервере не совпадают
          if (syncOptions.getAcceptLocalChangesIds().contains(updatedItem.getId())) {
            // Пользователь выбрал локальные изменения, перезаписываем заметку на сервере
            doUpdate = true;
          } else if (syncOptions.getAcceptRemoteChangesIds().contains(updatedItem.getId())) {
            // Пользователь выбрал изменения с сервера, перезаписываем локальную заметку
            updateLocalNoteValuesAndSaveToDb(updatedItem, remoteNote, userId);
            syncResult.increaseLocalChangedCount();
            forceRemoveNotesFromDb(originalNotes);
            doUpdate = false;
          } else {
            // Пользователь не определился с выбором
            syncResult.addEditConflict(updatedItem, remoteNote);
          }
        }

        if (doUpdate) {
          // Обновляем заметку на сервере
          if (!forceEditRemoteItem(updatedItem)) {
            syncResult.setFailure();
            return;
          }

          forceRemoveNotesFromDb(originalNotes);
        }
      }
    }
  }

  private void forceRemoveNotesFromDb(List<SysItem> originalNotes) {
    for (SysItem note : originalNotes) {
      dbHelper.forceRemoveSysItemById(note.getId());
    }
  }

  private void forceRemoveRemoteItems(List<SysItem> removedUnsyncedSysItems, SyncOptions syncOptions,
                                      SyncResult syncResult) {
    for (SysItem removedSysItem : removedUnsyncedSysItems) {
      Long userId = removedSysItem.getUserId();
      Long remoteId = removedSysItem.getRemoteId();
      if (remoteId == null || userId == null) {
        dbHelper.forceRemoveSysItemById(removedSysItem.getId());
        syncResult.increaseLocalRemovedCount();
        continue;
      }

      NotesBackendUserNoteDto remoteUserNoteDto = getUserNoteByUserIdAndNoteId(userId, remoteId);
      if (remoteUserNoteDto == null) {
        syncResult.setFailure();
        return;
      }

      NotesBackendUserNote remoteUserNote = remoteUserNoteDto.getData();
      if (remoteUserNoteDto.isNotFound() || remoteUserNote == null) {
        dbHelper.forceRemoveSysItemById(removedSysItem.getId());
        syncResult.increaseLocalRemovedCount();
        continue;
      }

      if (areNotesSame(removedSysItem, remoteUserNote) ||
          syncOptions.getRemoveRemoteIds().contains(removedSysItem.getId())) {

        if (!deleteUserNote(userId, remoteId)) {
          Log.e(TAG, "Unable to remove remote note with userId=" + userId + ", and id=" + remoteId);
          syncResult.setFailure();
          return;
        }

        dbHelper.forceRemoveSysItemById(removedSysItem.getId());
        syncResult.increaseLocalRemovedCount();

      } else if (syncOptions.getRevertRemovedIds().contains(removedSysItem.getId())) {
        updateLocalNoteValuesAndSaveToDb(removedSysItem.setStatus(SysItem.STATUS_ACTIVE), remoteUserNote, userId);
        syncResult.increaseLocalAddedCount();
      } else {
        syncResult.addRemoveConflict(removedSysItem, remoteUserNote);
      }
    }
  }

  private boolean deleteUserNote(Long userId, Long remoteId) {
    try {
      Response<?> response =
          notesBackendService.deleteUserNote(userId, remoteId).execute();
      if (!response.isSuccessful()) {
        Log.e(TAG, "Unable to remove user note: " + getResponseErrorBodyAsString(response));
        return false;
      }
      return true;

    } catch (Exception e) {
      Log.e(TAG, "Unable to remove user note", e);
      return false;
    }
  }

  private NotesBackendUserNoteDto getUserNoteByUserIdAndNoteId(Long userId, Long remoteId) {
    try {
      Response<NotesBackendUserNoteDto> response =
          notesBackendService.getUserNoteByUserIdAndNoteId(userId, remoteId).execute();
      if (!response.isSuccessful()) {
        Log.e(TAG, "Unable to retrieve user note: " + getResponseErrorBodyAsString(response));
        return null;
      }
      return response.body();

    } catch (Exception e) {
      Log.e(TAG, "Unable to retrieve user note", e);
      return null;
    }
  }

  private boolean areNotesSame(SysItem localNote, NotesBackendUserNote remoteNote) {
    if (localNote == null && remoteNote == null) {
      return true;
    }
    if (localNote == null || remoteNote == null) {
      return false;
    }

    return StringUtils.areSameTrimmed(localNote.getTitle(), remoteNote.getTitle()) &&
        StringUtils.areSameTrimmed(localNote.getBody(), remoteNote.getDescription()) &&
        StringUtils.areSameTrimmed(localNote.getImageUrl(), remoteNote.getImageUrl()) &&
        localNote.getColor() == ColorUtils.colorStringToInt(remoteNote.getColor());
  }

  private void forceCreateRemoteItems(@NonNull List<SysItem> newItems, SyncOptions syncOptions,
                                      SyncResult syncResult) {
    for (SysItem newItem : newItems) {
      if (newItem.getUserId() == null) {
        // Произошла какая-то ерунда
        // Возможно, удалять заметку не лучшая идея, но пользователь ее все равно не увидит
        dbHelper.forceRemoveSysItemById(newItem.getId());
        // Увеличивать счетчик удаленных не стоит, потому что заметка не видна пользователю
        continue;
      }

      if (!forceCreateRemoteItem(newItem)) {
        syncResult.setFailure();
        return;
      }
      syncResult.increaseLocalAddedCount();
    }
  }

  private boolean forceCreateRemoteItem(SysItem newItem) {
    NotesBackendUserNote noteDto = createNoteDto(newItem);
    return forceCreateRemoteItem(newItem, noteDto);
  }

  private boolean forceEditRemoteItem(SysItem newItem) {
    NotesBackendUserNote noteDto = createNoteDto(newItem);
    return forceEditRemoteItem(newItem, noteDto);
  }

  private boolean forceEditRemoteItem(SysItem newItem, NotesBackendUserNote noteDto) {
    try {
      Response<EditUserNoteResponseDto> response = notesBackendService.editUserNote(
          newItem.getUserId(), newItem.getRemoteId(), noteDto)
          .execute();
      if (!response.isSuccessful()) {
        Log.e(TAG, "Unable to update user note: " + getResponseErrorBodyAsString(response));
        return false;
      }
      EditUserNoteResponseDto body = response.body();
      if (body == null || !body.isOk() || body.getData() == null) {
        Log.e(TAG, "Unable to update user note: " + body);
        return false;
      }

      newItem.setSynced(true);
      dbHelper.saveSysItemWithoutNotifications(newItem);

    } catch (Exception e) {
      Log.e(TAG, "Unable to update user note", e);
      return false;
    }

    return true;
  }

  private boolean forceCreateRemoteItem(SysItem newItem, NotesBackendUserNote noteDto) {
    try {
      Response<CreateUserNoteResponseDto> response = notesBackendService.createUserNote(newItem.getUserId(), noteDto)
          .execute();
      if (!response.isSuccessful()) {
        Log.e(TAG, "Unable to create user note: " + getResponseErrorBodyAsString(response));
        return false;
      }
      CreateUserNoteResponseDto body = response.body();
      if (body == null || !body.isOk() || body.getData() == null) {
        Log.e(TAG, "Unable to create user note: " + body);
        return false;
      }

      newItem.setRemoteId(body.getData())
          .setSynced(true);
      dbHelper.saveSysItemWithoutNotifications(newItem);

    } catch (Exception e) {
      Log.e(TAG, "Unable to create user note", e);
      return false;
    }

    return true;
  }

  private String getResponseErrorBodyAsString(Response<?> response) {
    try {
      if (response == null || response.errorBody() == null || response.errorBody().string() == null) {
        return "";
      }
      return response.errorBody().string();
    } catch (IOException e) {
      return "";
    }
  }

  public enum SyncResultStatus {
    OK,
    FAILURE,
  }

  public static class SyncResult {
    private SyncResultStatus status = SyncResultStatus.OK;
    private List<LocalRemotePair> removeConflicts = new ArrayList<>();
    private List<LocalRemotePair> editConflicts = new ArrayList<>();
    private int localRemovedCount;
    private int localAddedCount;
    private int localChangedCount;

    public SyncResult setOk() {
      return setStatus(SyncResultStatus.OK);
    }

    public SyncResult setFailure() {
      return setStatus(SyncResultStatus.FAILURE);
    }

    public SyncResultStatus getStatus() {
      return status;
    }

    public SyncResult setStatus(SyncResultStatus status) {
      this.status = status;
      return this;
    }

    public List<LocalRemotePair> getRemoveConflicts() {
      return removeConflicts;
    }

    public SyncResult addRemoveConflict(SysItem local, NotesBackendUserNote remote) {
      removeConflicts.add(new LocalRemotePair(local, remote));
      return this;
    }

    public List<LocalRemotePair> getEditConflicts() {
      return editConflicts;
    }

    public SyncResult addEditConflict(SysItem local, NotesBackendUserNote remote) {
      editConflicts.add(new LocalRemotePair(local, remote));
      return this;
    }

    public boolean hasConflicts() {
      return !removeConflicts.isEmpty() || !editConflicts.isEmpty();
    }

    public boolean isOk() {
      return status == SyncResultStatus.OK;
    }

    public boolean isFailure() {
      return status == SyncResultStatus.FAILURE;
    }

    public int getLocalRemovedCount() {
      return localRemovedCount;
    }

    public SyncResult increaseLocalRemovedCount() {
      localRemovedCount++;
      return this;
    }

    public int getLocalAddedCount() {
      return localAddedCount;
    }

    public SyncResult increaseLocalAddedCount() {
      localAddedCount++;
      return this;
    }

    public int getLocalChangedCount() {
      return localChangedCount;
    }

    public SyncResult increaseLocalChangedCount() {
      localChangedCount++;
      return this;
    }

    public boolean hasLocalChanges() {
      return localAddedCount > 0 || localChangedCount > 0 || localRemovedCount > 0;
    }
  }

  public static class LocalRemotePair {
    private SysItem local;
    @Nullable
    private NotesBackendUserNote remote;

    public LocalRemotePair(SysItem local, NotesBackendUserNote remote) {
      this.local = local;
      this.remote = remote;
    }

    public SysItem getLocal() {
      return local;
    }

    public LocalRemotePair setLocal(SysItem local) {
      this.local = local;
      return this;
    }

    public NotesBackendUserNote getRemote() {
      return remote;
    }

    public LocalRemotePair setRemote(NotesBackendUserNote remote) {
      this.remote = remote;
      return this;
    }
  }
}
