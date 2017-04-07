package com.awesoon.firsttask;


import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.awesoon.firsttask.testUtils.LogMatcher;
import com.awesoon.firsttask.util.Log;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AwesomeApplicationTest {
  @Rule
  public ActivityTestRule<AwesomeActivity> activityRule =
      new ActivityTestRule<>(AwesomeActivity.class);

  @Test
  public void testDefaultLifecycle() throws Exception {
    // given
    AwesomeActivity activity = activityRule.getActivity();
    AwesomeApplication application = (AwesomeApplication) activity.getApplication();

    // when
    // then
    Log log = application.getLog();
    LogMatcher logMatcher = new LogMatcher(log);

    logMatcher
        .assertMatches(AwesomeApplication.LOG_LEVEL, AwesomeApplication.TAG, "On create")
        .assertAllMessagesChecked();
  }
}
