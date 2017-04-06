package com.awesoon.firsttask;

import android.app.Application;
import android.content.res.Configuration;

import com.awesoon.firsttask.util.Log;

public class AwesomeApplication extends Application {
  public static final String TAG = "AwesomeApplication";
  public static final int LOG_LEVEL = 0;

  private Log log = new Log();

  @Override
  public void onCreate() {
    super.onCreate();
    log.addEntry(LOG_LEVEL, TAG, "On create");
  }

  /**
   * This method is for use in emulated process environments.  It will
   * never be called on a production Android device, where processes are
   * removed by simply killing them; no user code (including this callback)
   * is executed when doing so.
   */
  @Override
  public void onTerminate() {
    super.onTerminate();
    log.addEntry(LOG_LEVEL, TAG, "On terminate (tests only)");
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    log.addEntry(LOG_LEVEL, TAG, "On configuration changed");
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    log.addEntry(LOG_LEVEL, TAG, "On low memory");
  }

  @Override
  public void onTrimMemory(int level) {
    super.onTrimMemory(level);
    log.addEntry(LOG_LEVEL, TAG, "On trim memory");
  }

  public Log getLog() {
    return log;
  }
}
