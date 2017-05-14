package com.awesoon.thirdtask.service;

import android.support.annotation.NonNull;
import android.util.Log;

import com.awesoon.thirdtask.db.DbHelper;
import com.awesoon.thirdtask.domain.SysItem;
import com.awesoon.thirdtask.service.container.SyncOptions;
import com.awesoon.thirdtask.util.Assert;
import com.awesoon.thirdtask.util.ColorUtils;
import com.awesoon.thirdtask.web.rest.NotesBackendService;
import com.awesoon.thirdtask.web.rest.dto.CreateUserNoteResponseDto;
import com.awesoon.thirdtask.web.rest.dto.EditUserNoteResponseDto;
import com.awesoon.thirdtask.web.rest.dto.NotesBackendUserNote;
import com.awesoon.thirdtask.web.rest.dto.NotesBackendUserNoteDto;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SyncService {
  private static final String TAG = "SyncService";

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

  public void syncWithRemote(Long noteId, SyncOptions syncOptions) {
    Assert.notNull(syncOptions, "syncOptions must not be null");

    SysItem sysItem = dbHelper.findSysItemById(noteId);
    if (sysItem == null) {
      return;
    }

    if (sysItem.getUserId() == null) {
      long currentUserId = userService.getCurrentUserId();
      userService.setCurrentUserId(currentUserId);
      dbHelper.saveSysItemDoesNotNotify(sysItem);
    }

    if (syncOptions.getOverwriteOptions() == null) {

    } else {
      switch (syncOptions.getOverwriteOptions()) {
        case OVERWRITE_LOCAL:
          forceUpdateLocal(sysItem);
          break;
        case OVERWRITE_REMOTE:
          forceSaveToRemote(sysItem);
          break;
      }
    }
  }

  public void forceUpdateLocal(final SysItem sysItem) {
    notesBackendService.getUserNoteByUserIdAndNoteId(sysItem.getUserId(), sysItem.getRemoteId())
        .enqueue(new Callback<NotesBackendUserNoteDto>() {
          @Override
          public void onResponse(@NonNull Call<NotesBackendUserNoteDto> call,
                                 @NonNull Response<NotesBackendUserNoteDto> response) {
            NotesBackendUserNoteDto body = response.body();
            if (!response.isSuccessful() || body == null || !body.isOk()) {
              Log.i(TAG, "Unable to retrieve user note with id=" + sysItem.getRemoteId() +
                  ", userId=" + sysItem.getUserId());
              return;
            }

            NotesBackendUserNote data = body.getData();
            sysItem
                .setTitle(data.getTitle())
                .setBody(data.getDescription())
                .setImageUrl(data.getImageUrl())
                .setSynced(true)
                .setRemoteId(data.getId());
            dbHelper.saveSysItemNotifySyncedOnly(sysItem);
          }

          @Override
          public void onFailure(@NonNull Call<NotesBackendUserNoteDto> call,
                                @NonNull Throwable t) {
            Log.e(TAG, "Error", t);
            sysItem.setSynced(false);
            dbHelper.saveSysItemNotifySyncedOnly(sysItem);
          }
        });
  }

  public void forceSaveToRemote(final SysItem sysItem) {
    Assert.notNull(sysItem, "sysItem must not be null");

    if (sysItem.getRemoteId() != null) {
      forceEditRemoteNote(sysItem);
    } else {
      forceCreateRemoteNote(sysItem);
    }
  }

  private void forceCreateRemoteNote(final SysItem sysItem) {
    NotesBackendUserNote dto = createNoteDto(sysItem);
    notesBackendService.createUserNote(sysItem.getUserId(), dto)
        .enqueue(new Callback<CreateUserNoteResponseDto>() {
          @Override
          public void onResponse(@NonNull Call<CreateUserNoteResponseDto> call,
                                 @NonNull Response<CreateUserNoteResponseDto> response) {
            CreateUserNoteResponseDto body = response.body();
            if (response.isSuccessful() && body != null && body.isOk()) {
              sysItem.setRemoteId(body.getData())
                  .setSynced(true);
              dbHelper.saveSysItemNotifySyncedOnly(sysItem);
            }
          }

          @Override
          public void onFailure(@NonNull Call<CreateUserNoteResponseDto> call,
                                @NonNull Throwable t) {
            Log.e(TAG, "Unable to create user note", t);
            sysItem.setSynced(false);
            dbHelper.saveSysItemNotifySyncedOnly(sysItem);
          }
        });
  }

  private void forceEditRemoteNote(final SysItem sysItem) {
    NotesBackendUserNote dto = createNoteDto(sysItem);
    notesBackendService.editUserNote(sysItem.getUserId(), dto.getId(), dto)
        .enqueue(new Callback<EditUserNoteResponseDto>() {
          @Override
          public void onResponse(@NonNull Call<EditUserNoteResponseDto> call,
                                 @NonNull Response<EditUserNoteResponseDto> response) {
            EditUserNoteResponseDto body = response.body();
            if (response.isSuccessful() && body != null && body.isOk()) {
              sysItem.setSynced(true);
              dbHelper.saveSysItemNotifySyncedOnly(sysItem);
            }
          }

          @Override
          public void onFailure(@NonNull Call<EditUserNoteResponseDto> call, @NonNull Throwable t) {
            Log.e(TAG, "Unable to update user note", t);
            sysItem.setSynced(false);
            dbHelper.saveSysItemNotifySyncedOnly(sysItem);
          }
        });
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
}
