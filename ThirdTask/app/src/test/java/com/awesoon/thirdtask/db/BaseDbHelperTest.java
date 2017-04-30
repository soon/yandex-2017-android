package com.awesoon.thirdtask.db;

import com.awesoon.thirdtask.BuildConfig;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static android.os.Build.VERSION_CODES.LOLLIPOP;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = LOLLIPOP, packageName = "com.awesoon.thirdtask")
public abstract class BaseDbHelperTest {
  protected DbHelper dbHelper;

  @Before
  public void setUp() throws Exception {
    dbHelper = new DbHelper(RuntimeEnvironment.application);
    dbHelper.clearDbAndRecreate();
  }

  @After
  public void tearDown() throws Exception {
    dbHelper.clearDbAndRecreate();
  }
}
