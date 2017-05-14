package com.awesoon.thirdtask.config;

import com.awesoon.thirdtask.web.rest.NotesBackendService;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

@Module
public class WebConfig {
  @Provides
  @Singleton
  public NotesBackendService notesBackendService(@Named("notesBackendServerUrl") String serverUrl) {
    Retrofit retrofit = new Retrofit.Builder()
        .baseUrl(serverUrl)
        .addConverterFactory(MoshiConverterFactory.create())
        .build();
    return retrofit.create(NotesBackendService.class);
  }

  @Provides
  @Named("notesBackendServerUrl")
  public String notesBackendServerUrl() {
    return "https://notesbackend-yufimtsev.rhcloud.com";
  }
}
