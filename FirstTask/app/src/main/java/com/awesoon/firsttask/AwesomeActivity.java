package com.awesoon.firsttask;

import android.app.Activity;
import android.os.Bundle;

import com.awesoon.firsttask.util.Log;

public class AwesomeActivity extends Activity {
  public static final String TAG = AwesomeActivity.class.getSimpleName();
  private Log log = new Log();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    log.i(TAG, "On create");
  }

  @Override
  protected void onStart() {
    super.onStart();
    log.i(TAG, "On start");
  }

  @Override
  protected void onResume() {
    super.onResume();
    log.i(TAG, "On resume");
  }

  @Override
  protected void onPause() {
    super.onPause();
    log.i(TAG, "On pause");
  }

  @Override
  protected void onStop() {
    super.onStop();
    log.i(TAG, "On stop");
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    log.i(TAG, "On destroy");
  }

  public Log getLog() {
    return log;
  }
}
