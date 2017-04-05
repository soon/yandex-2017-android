package com.awesoon.firsttask;

import android.app.Application;
import android.content.res.Configuration;
import android.util.Log;

public class AwesomeApplication extends Application {
  private static final String TAG = AwesomeApplication.class.getSimpleName();

  @Override
  public void onCreate() {
    Log.w(TAG, "Before on create");
    super.onCreate();
    Log.w(TAG, "After on create");
  }

  @Override
  public void onTerminate() {
    Log.i(TAG, "Before on terminate");
    super.onTerminate();
    Log.i(TAG, "Before on terminate");
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    Log.i(TAG, "Before on configuration changed");
    super.onConfigurationChanged(newConfig);
    Log.i(TAG, "After on configuration changed");
  }

  @Override
  public void onLowMemory() {
    Log.i(TAG, "Before on low memory");
    super.onLowMemory();
    Log.i(TAG, "After on low memory");
  }

  @Override
  public void onTrimMemory(int level) {
    Log.d(TAG, "Before on trim memory, level = " + level);
    super.onTrimMemory(level);
    Log.d(TAG, "After on trim memory, level = " + level);
  }
}
