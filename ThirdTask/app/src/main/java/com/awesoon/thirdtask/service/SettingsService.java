package com.awesoon.thirdtask.service;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.awesoon.thirdtask.NotesApplication;

import javax.inject.Inject;

public class SettingsService {
  public static final String IS_SYNCING_IDENT = "IS_SYNCING";
  public static final Object syncLock = new Object();

  @Inject
  NotesApplication notesApplication;

  public SettingsService(NotesApplication notesApplication) {
    this.notesApplication = notesApplication;
  }

  public boolean lockSyncWithRemote() {
    return compareSyncStatusAndSet(false, true);
  }

  public void unlockSyncWithRemote() {
    compareSyncStatusAndSet(true, false);
  }

  public synchronized boolean compareSyncStatusAndSet(boolean expected, boolean newSyncStatus) {
    synchronized (syncLock) {
      SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(notesApplication);
      if (sharedPreferences.getBoolean(IS_SYNCING_IDENT, false) != expected) {
        return false;
      }

      sharedPreferences.edit().putBoolean(IS_SYNCING_IDENT, newSyncStatus).apply();
      return true;
    }
  }
}
