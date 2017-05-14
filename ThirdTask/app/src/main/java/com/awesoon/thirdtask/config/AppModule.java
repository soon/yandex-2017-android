package com.awesoon.thirdtask.config;

import com.awesoon.thirdtask.NotesApplication;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {
  private NotesApplication application;

  public AppModule(NotesApplication application) {
    this.application = application;
  }

  @Provides
  @Singleton
  public NotesApplication providesApplication() {
    return application;
  }
}
