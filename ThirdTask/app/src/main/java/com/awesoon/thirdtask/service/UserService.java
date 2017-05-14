package com.awesoon.thirdtask.service;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.awesoon.thirdtask.NotesApplication;

import javax.inject.Inject;

public class UserService {
  public static final String CURRENT_USER_ID_IDENT = "CURRENT_USER_ID";
  public static final long DEFAULT_USER_ID = 0;

  @Inject
  NotesApplication notesApplication;

  public UserService(NotesApplication notesApplication) {
    this.notesApplication = notesApplication;
  }

  public long getCurrentUserId() {
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(notesApplication);
    return sharedPreferences.getLong(CURRENT_USER_ID_IDENT, DEFAULT_USER_ID);
  }

  public void setCurrentUserId(long userId) {
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(notesApplication);
    sharedPreferences.edit().putLong(CURRENT_USER_ID_IDENT, userId).apply();
  }
}
