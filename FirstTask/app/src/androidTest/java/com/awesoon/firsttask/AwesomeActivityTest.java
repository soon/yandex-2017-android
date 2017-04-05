package com.awesoon.firsttask;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.awesoon.firsttask.testUtils.LogMatcher;
import com.awesoon.firsttask.util.Log;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;

@RunWith(AndroidJUnit4.class)
public class AwesomeActivityTest {
  @Rule
  public ActivityTestRule<AwesomeActivity> activityRule =
      new ActivityTestRule<>(AwesomeActivity.class);

  @Test
  public void testLifecycleResumePauseMultipleTimes() throws Exception {
    // given
    AwesomeActivity activity = activityRule.getActivity();

    // when
    getInstrumentation().callActivityOnPause(activity);
    getInstrumentation().callActivityOnResume(activity);
    getInstrumentation().callActivityOnPause(activity);
    getInstrumentation().callActivityOnResume(activity);

    // then
    Log log = activity.getLog();
    LogMatcher logMatcher = new LogMatcher(log);
    logMatcher
        .assertMatches(Log.Severity.INFO, AwesomeActivity.TAG, "On create")
        .assertMatches(Log.Severity.INFO, AwesomeActivity.TAG, "On start")
        .assertMatches(Log.Severity.INFO, AwesomeActivity.TAG, "On resume")
        .assertMatches(Log.Severity.INFO, AwesomeActivity.TAG, "On pause")
        .assertMatches(Log.Severity.INFO, AwesomeActivity.TAG, "On resume")
        .assertMatches(Log.Severity.INFO, AwesomeActivity.TAG, "On pause")
        .assertMatches(Log.Severity.INFO, AwesomeActivity.TAG, "On resume")
        .assertMatches(Log.Severity.INFO, AwesomeActivity.TAG, "On pause")
        .assertAllMessagesChecked();
  }

  @Test
  public void testLifecyclePause() throws Exception {
    // given
    AwesomeActivity activity = activityRule.getActivity();

    // when
    getInstrumentation().callActivityOnPause(activity);

    // then
    Log log = activity.getLog();
    LogMatcher logMatcher = new LogMatcher(log);
    logMatcher
        .assertMatches(Log.Severity.INFO, AwesomeActivity.TAG, "On create")
        .assertMatches(Log.Severity.INFO, AwesomeActivity.TAG, "On start")
        .assertMatches(Log.Severity.INFO, AwesomeActivity.TAG, "On resume")
        .assertMatches(Log.Severity.INFO, AwesomeActivity.TAG, "On pause")
        .assertAllMessagesChecked();
  }

  @Test
  public void testDefaultLifecycle() throws Exception {
    // given
    AwesomeActivity activity = activityRule.getActivity();

    // when

    // then
    Log log = activity.getLog();
    LogMatcher logMatcher = new LogMatcher(log);
    logMatcher
        .assertMatches(Log.Severity.INFO, AwesomeActivity.TAG, "On create")
        .assertMatches(Log.Severity.INFO, AwesomeActivity.TAG, "On start")
        .assertMatches(Log.Severity.INFO, AwesomeActivity.TAG, "On resume")
        .assertAllMessagesChecked();
  }
}
