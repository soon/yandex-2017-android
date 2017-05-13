package com.awesoon.thirdtask.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.awesoon.thirdtask.BuildConfig;
import com.awesoon.thirdtask.domain.SysItem;
import com.awesoon.thirdtask.util.ContentValuesBuilder;
import com.awesoon.thirdtask.util.RowMapperAdapter;
import com.awesoon.thirdtask.util.SqlUtils;
import com.google.common.collect.ImmutableList;

import org.joda.time.DateTime;
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
  public void testUpgradeFrom1_setDateTime() throws Exception {
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
    dbHelper.performUpgradeInternal(DbHelper.DATABASE_VERSION_1, DbHelper.DATABASE_VERSION);

    // then
    List<SysItem> sysItems = dbHelper.findAllSysItems();
    assertThat(sysItems.size(), is(1));
    assertThat(sysItems.get(0).getCreatedTime(), is(notNullValue()));
    assertThat(sysItems.get(0).getLastViewedTime(), is(notNullValue()));
    assertThat(sysItems.get(0).getLastEditedTime(), is(notNullValue()));
  }

  @Test
  public void testUpgrade1to3() throws Exception {
    // this is a placeholder test for the future db upgrades

    // given
    dbHelper.installSpecificDbVersionInternal(DbHelper.DATABASE_VERSION_1);

    // when
    dbHelper.performUpgradeInternal(DbHelper.DATABASE_VERSION_1, DbHelper.DATABASE_VERSION_3);

    // then
    // no exceptions expected
  }

  @Test
  public void testUpgradeFrom2__checkDateTimeTs() throws Exception {
    // given
    dbHelper.installSpecificDbVersionInternal(DbHelper.DATABASE_VERSION_2);
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    DateTime created = new DateTime(2015, 1, 1, 1, 1, 1);
    DateTime edited = new DateTime(2016, 1, 1, 1, 1, 1);
    DateTime viewed = new DateTime(2017, 1, 1, 1, 1, 1);
    ContentValues values = new ContentValuesBuilder()
        .put(SysItem.SysItemEntry.COLUMN_NAME_TITLE, "title")
        .put(SysItem.SysItemEntry.COLUMN_NAME_BODY, "body")
        .put(SysItem.SysItemEntry.COLUMN_NAME_COLOR, 1234)
        .put(SysItem.SysItemEntry.COLUMN_CREATED_TIME, created)
        .put(SysItem.SysItemEntry.COLUMN_LAST_EDITED_TIME, edited)
        .put(SysItem.SysItemEntry.COLUMN_LAST_VIEWED_TIME, viewed)
        .build();
    db.insert(SysItem.SysItemEntry.TABLE_NAME, null, values);

    // when
    dbHelper.performUpgradeInternal(DbHelper.DATABASE_VERSION_2, DbHelper.DATABASE_VERSION);

    // then
    String sql = String.format("SELECT a.%s, a.%s, a.%s FROM %s a",
        SysItem.SysItemEntry.COLUMN_CREATED_TIME_TS,
        SysItem.SysItemEntry.COLUMN_LAST_EDITED_TIME_TS,
        SysItem.SysItemEntry.COLUMN_LAST_VIEWED_TIME_TS,
        SysItem.SysItemEntry.TABLE_NAME);
    List<Long> ts = SqlUtils.queryForObject(db, sql, new RowMapperAdapter<List<Long>>() {
      @Override
      public List<Long> mapRow(Cursor cursor, int rowNumber) {
        long created = cursor.getLong(0);
        long edited = cursor.getLong(1);
        long viewed = cursor.getLong(2);
        return ImmutableList.of(created, edited, viewed);
      }
    });
    assertThat(ts.size(), is(3));
    assertThat(ts.get(0), is(created.getMillis()));
    assertThat(ts.get(1), is(edited.getMillis()));
    assertThat(ts.get(2), is(viewed.getMillis()));
  }


  @Test
  public void testUpgrade3to4() throws Exception {
    // given
    dbHelper.installSpecificDbVersionInternal(DbHelper.DATABASE_VERSION_3);

    // when
    dbHelper.performUpgradeInternal(DbHelper.DATABASE_VERSION_3, DbHelper.DATABASE_VERSION_4);

    // then
    // no exceptions expected
  }
}
