package com.awesoon.thirdtask;

import android.app.Activity;
import android.app.Application;

import com.awesoon.thirdtask.config.AppModule;
import com.awesoon.thirdtask.config.DaggerNotesApplicationComponent;
import com.awesoon.thirdtask.db.DbHelper;

import net.danlew.android.joda.JodaTimeAndroid;

import javax.inject.Inject;

import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasDispatchingActivityInjector;

public class NotesApplication extends Application implements HasDispatchingActivityInjector {
  public static final int NOTES_GENERATOR_NOTIFICATION_ID = 1;
  public static final int NOTES_IMPORT_NOTIFICATION_ID = 2;
  public static final int NOTES_EXPORT_NOTIFICATION_ID = 3;

  @Inject
  DbHelper dbHelper;

  @Inject
  DispatchingAndroidInjector<Activity> dispatchingAndroidInjector;

  @Override
  public void onCreate() {
    super.onCreate();
    JodaTimeAndroid.init(this);
    DaggerNotesApplicationComponent.builder()
        .appModule(new AppModule(this))
        .build()
        .inject(this);
  }

  @Override
  public DispatchingAndroidInjector<Activity> activityInjector() {
    return dispatchingAndroidInjector;
  }

  public DbHelper getDbHelper() {
    return dbHelper;
  }
}
