package com.awesoon.thirdtask.config;

import com.awesoon.thirdtask.NotesApplication;
import com.awesoon.thirdtask.db.DbHelper;
import com.awesoon.thirdtask.service.SyncService;
import com.awesoon.thirdtask.service.UserService;
import com.awesoon.thirdtask.web.rest.NotesBackendService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ServiceModule {
  @Provides
  @Singleton
  public SyncService syncService(UserService userService, NotesBackendService notesBackendService, DbHelper dbHelper) {
    return new SyncService(userService, notesBackendService, dbHelper);
  }

  @Provides
  @Singleton
  public UserService userService(NotesApplication notesApplication) {
    return new UserService(notesApplication);
  }
}
