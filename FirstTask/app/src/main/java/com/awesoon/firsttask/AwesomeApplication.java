package com.awesoon.firsttask;

import android.app.Application;
import android.content.res.Configuration;

import com.awesoon.firsttask.util.Log;

public class AwesomeApplication extends Application {
  public static final String TAG = "AwesomeApplication";
  public static final int LOG_LEVEL = 0;

  private Log log = new Log();

  public AwesomeApplication() {
    addLogEntry("constructor");
  }

  @Override
  public void onCreate() {
    super.onCreate();
    addLogEntry("On create");
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
    addLogEntry("On terminate (tests only)");
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    addLogEntry("On configuration changed");
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    addLogEntry("On low memory");
  }

  @Override
  public void onTrimMemory(int level) {
    super.onTrimMemory(level);
    addLogEntry("On trim memory");
  }

  public Log getLog() {
    return log;
  }

  private void addLogEntry(String message) {
    log.addEntry(LOG_LEVEL, TAG, message);
  }
}
