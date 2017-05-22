package com.awesoon.thirdtask.service;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.android.internal.util.Predicate;
import com.awesoon.thirdtask.NotesApplication;
import com.awesoon.thirdtask.service.container.SysUser;
import com.awesoon.thirdtask.util.CollectionUtils;
import com.awesoon.thirdtask.util.Function;
import com.awesoon.thirdtask.util.NameUtils;
import com.awesoon.thirdtask.util.NumberUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import javax.inject.Inject;

public class UserService {
  public static final String CURRENT_USER_ID_IDENT = makeIdent("CURRENT_USER_ID");
  public static final String ALL_USER_IDS = makeIdent("ALL_USER_IDS");
  public static final String USER_NAME_IDENT_PATTERN = makeIdent("USER_NAME_%d");
  public static final long DEFAULT_USER_ID = 0;
  public static final String DEFAULT_USER_NAME = "No name";

  @Inject
  NotesApplication notesApplication;

  public UserService(NotesApplication notesApplication) {
    this.notesApplication = notesApplication;
  }

  public long getCurrentUserId() {
    SharedPreferences sharedPreferences = getSharedPreferences();
    return sharedPreferences.getLong(CURRENT_USER_ID_IDENT, DEFAULT_USER_ID);
  }

  public void setCurrentUserId(SharedPreferences sharedPreferences, long userId) {
    sharedPreferences.edit().putLong(CURRENT_USER_ID_IDENT, userId).apply();
  }

  public void setCurrentUserId(long userId) {
    setCurrentUserId(getSharedPreferences(), userId);
  }

  public List<SysUser> getAllUsers() {
    final SharedPreferences sharedPreferences = getSharedPreferences();
    final List<Long> allUserIds = getAllUserIds(sharedPreferences);
    List<SysUser> allUsers = CollectionUtils.mapNotNull(allUserIds, new Function<Long, SysUser>() {
      @Override
      public SysUser apply(Long id) {
        String userName = getUserName(sharedPreferences, id);
        return new SysUser(id, userName);
      }
    });
    Collections.sort(allUsers, new Comparator<SysUser>() {
      @Override
      public int compare(SysUser lhs, SysUser rhs) {
        return lhs.getName().compareTo(rhs.getName());
      }
    });

    return allUsers;
  }

  public void setUserName(SharedPreferences sharedPreferences, Long id, String name) {
    sharedPreferences.edit().putString(String.format(USER_NAME_IDENT_PATTERN, id), name).apply();
  }

  public String getUserName(SharedPreferences sharedPreferences, Long id) {
    return sharedPreferences.getString(String.format(USER_NAME_IDENT_PATTERN, id), DEFAULT_USER_NAME);
  }

  public List<Long> getAllUserIds(SharedPreferences sharedPreferences) {
    List<Long> userIds = CollectionUtils.mapNotNull(
        sharedPreferences.getStringSet(ALL_USER_IDS, Collections.<String>emptySet()),
        new Function<String, Long>() {
          @Override
          public Long apply(String s) {
            return NumberUtils.tryParseLong(s);
          }
        });

    return userIds;
  }

  public void addUserId(SharedPreferences sharedPreferences, Long id) {
    Set<String> userIds = sharedPreferences.getStringSet(ALL_USER_IDS, new HashSet<String>());
    userIds.add(String.valueOf(id));
    sharedPreferences.edit().putStringSet(ALL_USER_IDS, userIds).apply();
  }

  public SysUser createAndSelectUser(String name) {
    List<SysUser> allUsers = getAllUsers();
    List<String> allUserNames = CollectionUtils.mapNotNull(allUsers, new Function<SysUser, String>() {
      @Override
      public String apply(SysUser sysUser) {
        return sysUser.getName();
      }
    });

    Long id = NumberUtils.tryParseLong(name);
    if (id != null) {
      name = "id#" + name;
    } else {
      Random rnd = new Random();
      id = (long) Math.abs(rnd.nextInt());
    }

    String uniqueName = NameUtils.createUniqueName(name, allUserNames);

    SharedPreferences sharedPreferences = getSharedPreferences();
    addUserId(sharedPreferences, id);
    setUserName(sharedPreferences, id, uniqueName);
    setCurrentUserId(id);

    return new SysUser(id, name);
  }

  private SharedPreferences getSharedPreferences() {
    return PreferenceManager.getDefaultSharedPreferences(notesApplication);
  }

  private static String makeIdent(String name) {
    return UserService.class.getCanonicalName() + "." + name;
  }

  public SysUser removeCurrentUser() {
    List<SysUser> allUsers = getAllUsers();
    if (allUsers.size() < 2) {
      return null;
    }


    SharedPreferences sharedPreferences = getSharedPreferences();
    final Set<String> allUserIds = sharedPreferences.getStringSet(ALL_USER_IDS, new HashSet<String>());
    final long currentUserId = sharedPreferences.getLong(CURRENT_USER_ID_IDENT, DEFAULT_USER_ID);
    List<SysUser> usersToRemove = CollectionUtils.filter(allUsers, new Predicate<SysUser>() {
      @Override
      public boolean apply(SysUser sysUser) {
        return Objects.equals(sysUser.getId(), currentUserId);
      }
    });
    if (usersToRemove.isEmpty()) {
      return null;
    }

    allUserIds.remove(String.valueOf(currentUserId));

    List<SysUser> validUsers = CollectionUtils.filter(allUsers, new Predicate<SysUser>() {
      @Override
      public boolean apply(SysUser sysUser) {
        return !Objects.equals(sysUser.getId(), currentUserId);
      }
    });

    SysUser newUser = validUsers.get(0);

    sharedPreferences.edit()
        .putStringSet(ALL_USER_IDS, allUserIds)
        .remove(String.format(USER_NAME_IDENT_PATTERN, currentUserId))
        .putLong(CURRENT_USER_ID_IDENT, newUser.getId())
        .apply();

    return usersToRemove.get(0);
  }
}
