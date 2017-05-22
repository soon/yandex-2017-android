package com.awesoon.thirdtask.config;

import com.awesoon.thirdtask.NotesApplication;
import com.awesoon.thirdtask.db.DbHelper;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class DatabaseModule {
  @Provides
  @Singleton
  public DbHelper dbHelper(NotesApplication application) {
    return new DbHelper(application);
  }
}
