package com.awesoon.thirdtask.db;

import com.awesoon.thirdtask.BuildConfig;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static android.os.Build.VERSION_CODES.LOLLIPOP;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = LOLLIPOP, packageName = "com.awesoon.thirdtask")
public class DbHelperUpgradeTest {
  private DbHelper dbHelper;

  @Before
  public void setUp() throws Exception {
    dbHelper = new DbHelper(RuntimeEnvironment.application);
    dbHelper.dropAllTables();
  }

  @After
  public void tearDown() throws Exception {
    dbHelper.dropAllTables();
  }

  @Test
  public void testUpgrade1to2() throws Exception {
    // given
    dbHelper.installSpecificDbVersionInternal(DbHelper.DATABASE_VERSION_1);

    // when
    dbHelper.performUpgradeInternal(DbHelper.DATABASE_VERSION_1, DbHelper.DATABASE_VERSION_2);

    // then
    // no exceptions expected
  }
}
