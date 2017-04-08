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

  public AwesomeActivity() {
    addLogEntry("constructor");
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addLogEntry("On create");
    setContentView(R.layout.awesome_activity);
  }

  @Override
  protected void onStart() {
    super.onStart();
    addLogEntry("On start");
  }

  @Override
  protected void onRestart() {
    super.onRestart();
    addLogEntry("On restart");
  }

  @Override
  protected void onResume() {
    super.onResume();
    addLogEntry("On resume");
    isActivityVisible = true;
  }

  @Override
  protected void onPause() {
    super.onPause();
    addLogEntry("On pause");
    isActivityVisible = false;
  }

  @Override
  protected void onStop() {
    super.onStop();
    addLogEntry("On stop");
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    addLogEntry("On destroy");
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

  private void addLogEntry(String message) {
    log.addEntry(LOG_LEVEL, TAG, message);
  }
}
