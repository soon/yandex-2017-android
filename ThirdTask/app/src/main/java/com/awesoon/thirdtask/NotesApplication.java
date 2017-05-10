package com.awesoon.thirdtask;

import android.app.Application;

import com.awesoon.thirdtask.db.DbHelper;

import net.danlew.android.joda.JodaTimeAndroid;

public class NotesApplication extends Application {
  public static final int NOTES_GENERATOR_NOTIFICATION_ID = 1;

  private DbHelper dbHelper;


  @Override
  public void onCreate() {
    super.onCreate();
    JodaTimeAndroid.init(this);
    dbHelper = new DbHelper(this);
  }

  public DbHelper getDbHelper() {
    return dbHelper;
  }
}
