package com.awesoon.thirdtask;

import android.app.Application;

import net.danlew.android.joda.JodaTimeAndroid;

public class NotesApplication extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
    JodaTimeAndroid.init(this);
  }
}
