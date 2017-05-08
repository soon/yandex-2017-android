package com.awesoon.thirdtask.db;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.awesoon.thirdtask.BuildConfig;
import com.awesoon.thirdtask.domain.SysItem;
import com.awesoon.thirdtask.util.ContentValuesBuilder;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.List;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

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

  @Test
  public void testUpgrade1to3() throws Exception {
    // this is a placeholder test for the future db upgrades

    // given
    dbHelper.installSpecificDbVersionInternal(DbHelper.DATABASE_VERSION_1);

    // when
    dbHelper.performUpgradeInternal(DbHelper.DATABASE_VERSION_1, DbHelper.DATABASE_VERSION_2 + 1);

    // then
    // no exceptions expected
  }

  @Test
  public void testUpgrade1to2__setDateTime() throws Exception {
    // given
    dbHelper.installSpecificDbVersionInternal(DbHelper.DATABASE_VERSION_1);
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    ContentValues values = new ContentValuesBuilder()
        .put(SysItem.SysItemEntry.COLUMN_NAME_TITLE, "title")
        .put(SysItem.SysItemEntry.COLUMN_NAME_BODY, "body")
        .put(SysItem.SysItemEntry.COLUMN_NAME_COLOR, 1234)
        .build();
    db.insert(SysItem.SysItemEntry.TABLE_NAME, null, values);

    // when
    dbHelper.performUpgradeInternal(DbHelper.DATABASE_VERSION_1, DbHelper.DATABASE_VERSION_2);

    // then
    List<SysItem> sysItems = dbHelper.findAllSysItems();
    assertThat(sysItems.size(), is(1));
    assertThat(sysItems.get(0).getCreatedTime(), is(notNullValue()));
    assertThat(sysItems.get(0).getLastViewedTime(), is(notNullValue()));
    assertThat(sysItems.get(0).getLastEditedTime(), is(notNullValue()));
  }
}
