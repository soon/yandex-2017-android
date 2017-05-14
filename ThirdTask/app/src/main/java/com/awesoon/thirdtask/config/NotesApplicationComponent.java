package com.awesoon.thirdtask.config;

import com.awesoon.thirdtask.NotesApplication;

import javax.inject.Singleton;

import dagger.Component;
import dagger.android.AndroidInjectionModule;

@Singleton
@Component(modules = {
    AndroidInjectionModule.class,
    AppModule.class,
    DatabaseModule.class,
    ServiceModule.class,
    WebModule.class,
    ElementEditorActivityModule.class
})
public interface NotesApplicationComponent {
  void inject(NotesApplication application);
}
