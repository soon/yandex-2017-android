package com.awesoon.firsttask;

import android.app.Activity;
import android.os.Bundle;
import android.widget.FrameLayout;

import com.awesoon.firsttask.util.Log;

public class AwesomeActivity extends Activity {
  public static final String TAG = "AwesomeActivity";
  public static final int LOG_LEVEL = 1;

  private boolean isActivityVisible = false;
  private Log log = new Log();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    log.addEntry(LOG_LEVEL, TAG, "On create");
    setContentView(R.layout.awesome_activity);
  }

  @Override
  protected void onStart() {
    super.onStart();
    log.addEntry(LOG_LEVEL, TAG, "On start");
  }

  @Override
  protected void onRestart() {
    super.onRestart();
    log.addEntry(LOG_LEVEL, TAG, "On restart");
  }

  @Override
  protected void onResume() {
    super.onResume();
    log.addEntry(LOG_LEVEL, TAG, "On resume");
    isActivityVisible = true;
  }

  @Override
  protected void onPause() {
    super.onPause();
    log.addEntry(LOG_LEVEL, TAG, "On pause");
    isActivityVisible = false;
  }

  @Override
  protected void onStop() {
    super.onStop();
    log.addEntry(LOG_LEVEL, TAG, "On stop");
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    log.addEntry(LOG_LEVEL, TAG, "On destroy");
  }

  public Log getLog() {
    return log;
  }

  public boolean isActivityVisible() {
    return isActivityVisible;
  }

  public AwesomeViewGroup getViewGroup() {
    FrameLayout layout = ((FrameLayout) findViewById(android.R.id.content));
    return (AwesomeViewGroup) layout.getChildAt(0);
  }
}
