package com.awesoon.firsttask;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.awesoon.firsttask.testUtils.LogMatcher;
import com.awesoon.firsttask.util.Log;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AwesomeViewTest {
  @Rule
  public ActivityTestRule<AwesomeActivity> activityRule =
      new ActivityTestRule<>(AwesomeActivity.class);

  @Test
  public void testDefaultLifecycle() throws Exception {
    // given
    AwesomeActivity activity = activityRule.getActivity();
    AwesomeViewGroup viewGroup = activity.getViewGroup();
    AwesomeView awesomeView = viewGroup.getInnerView();

    // when
    // then
    Log log = awesomeView.getLog();
    LogMatcher logMatcher = new LogMatcher(log);
    logMatcher
        .assertMatches(AwesomeView.LOG_LEVEL, AwesomeView.TAG, "constructor")
        .assertMatches(AwesomeView.LOG_LEVEL, AwesomeView.TAG, "On attached to window")
        .startRepeatingAction()
        .assertMatches(AwesomeView.LOG_LEVEL, AwesomeView.TAG, "On measure")
        .assertMatches(AwesomeView.LOG_LEVEL, AwesomeView.TAG, "On layout")
        .checkRepeatingAction()
        .assertMatches(AwesomeView.LOG_LEVEL, AwesomeView.TAG, "On draw")
        .assertAllMessagesChecked();
  }
}
