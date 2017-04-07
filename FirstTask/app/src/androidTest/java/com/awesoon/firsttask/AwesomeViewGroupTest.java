package com.awesoon.firsttask;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.awesoon.firsttask.testUtils.LogMatcher;
import com.awesoon.firsttask.util.Log;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AwesomeViewGroupTest {
  @Rule
  public ActivityTestRule<AwesomeActivity> activityRule =
      new ActivityTestRule<>(AwesomeActivity.class);

  @Test
  public void testDefaultLifecycle() throws Exception {
    // given
    AwesomeActivity activity = activityRule.getActivity();
    AwesomeViewGroup viewGroup = activity.getViewGroup();

    // when
    // then
    Log log = viewGroup.getLog();
    LogMatcher logMatcher = new LogMatcher(log);
    logMatcher
        .assertMatches(AwesomeViewGroup.LOG_LEVEL, AwesomeViewGroup.TAG, "On finish inflate")
        .assertMatches(AwesomeViewGroup.LOG_LEVEL, AwesomeViewGroup.TAG, "On attached to window")
        .startRepeatingAction()
        .assertMatches(AwesomeViewGroup.LOG_LEVEL, AwesomeViewGroup.TAG, "On measure")
        .assertMatches(AwesomeViewGroup.LOG_LEVEL, AwesomeViewGroup.TAG, "On layout")
        .checkRepeatingAction()
        .assertMatches(AwesomeViewGroup.LOG_LEVEL, AwesomeViewGroup.TAG, "On draw")
        .assertAllMessagesChecked();
  }
}
