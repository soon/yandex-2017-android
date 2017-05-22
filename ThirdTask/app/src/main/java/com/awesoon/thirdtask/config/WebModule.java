package com.awesoon.thirdtask.config;

import com.awesoon.thirdtask.web.rest.NotesBackendService;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

@Module
public class WebModule {
  public OkHttpClient okHttpClient() {
    HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
    interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
    return new OkHttpClient.Builder().addInterceptor(interceptor).build();
  }

  @Provides
  @Singleton
  public NotesBackendService notesBackendService(@Named("notesBackendServerUrl") String serverUrl) {
    Retrofit retrofit = new Retrofit.Builder()
        .baseUrl(serverUrl)
        .client(okHttpClient())
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
