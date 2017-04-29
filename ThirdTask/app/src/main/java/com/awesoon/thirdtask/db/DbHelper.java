package com.awesoon.thirdtask.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.awesoon.thirdtask.domain.FavoriteColor;
import com.awesoon.thirdtask.domain.SysItem;
import com.awesoon.thirdtask.util.ContentValuesBuilder;
import com.awesoon.thirdtask.util.RowMapperAdapter;
import com.awesoon.thirdtask.util.SqlUtils;

import org.joda.time.DateTime;

import java.util.List;

import static com.awesoon.thirdtask.util.SqlUtils.dateTimeField;
import static com.awesoon.thirdtask.util.SqlUtils.intField;
import static com.awesoon.thirdtask.util.SqlUtils.pkIntAutoincrement;
import static com.awesoon.thirdtask.util.SqlUtils.textField;

public class DbHelper extends SQLiteOpenHelper {
  public static final int DATABASE_INITIAL_VERSION = 0;
  public static final int DATABASE_VERSION_1 = 1;
  public static final int DATABASE_VERSION_2 = 2;

  public static final int DATABASE_VERSION = DATABASE_VERSION_2;
  public static final String DATABASE_NAME = "ThirdTask.db";

  public DbHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    doCreateDb(db);
  }

  private void doCreateDb(SQLiteDatabase db) {
    db.execSQL(SysItem.SQL_CREATE_TABLE);
    db.execSQL(FavoriteColor.SQL_CREATE_TABLE);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    doOnUpgrade(db, oldVersion, newVersion);
  }

  void installSpecificDbVersionInternal(int newVersion) {
    performUpgradeInternal(DATABASE_INITIAL_VERSION, newVersion);
  }

  void performUpgradeInternal(int oldVersion, int newVersion) {
    SQLiteDatabase db = getWritableDatabase();
    doOnUpgrade(db, oldVersion, newVersion);
  }

  private void doOnUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    switch (oldVersion) {
      case DATABASE_INITIAL_VERSION:
        doUpgrade0To1(db);
        if (newVersion == DATABASE_VERSION_1) {
          break;
        }
      case DATABASE_VERSION_1:
        doUpgrade1To2(db);
        if (newVersion == DATABASE_VERSION_2) {
          break;
        }
      case DATABASE_VERSION_2:
        doUpgrade2To3(db);
        break;
      default:
        throw new IllegalArgumentException(
            "Unknown old DB version " + oldVersion + ", max DB version is " + DATABASE_VERSION);
    }
  }

  private void doUpgrade0To1(SQLiteDatabase db) {
    String sysItemSql = SqlUtils.makeCreateTableSql(SysItem.SysItemEntry.TABLE_NAME,
        pkIntAutoincrement(SysItem.SysItemEntry.COLUMN_NAME_ID),
        textField(SysItem.SysItemEntry.COLUMN_NAME_TITLE).setNull(false),
        textField(SysItem.SysItemEntry.COLUMN_NAME_BODY).setNull(false),
        intField(SysItem.SysItemEntry.COLUMN_NAME_COLOR).setNull(false)
    );

    String favoriteColorSql = SqlUtils.makeCreateTableSql(FavoriteColor.FavoriteColorEntry.TABLE_NAME,
        pkIntAutoincrement(FavoriteColor.FavoriteColorEntry.COLUMN_NAME_ID),
        intField(FavoriteColor.FavoriteColorEntry.COLUMN_NAME_COLOR).setNull(false).setUnique(true)
    );

    db.execSQL(sysItemSql);
    db.execSQL(favoriteColorSql);
  }

  private void doUpgrade1To2(SQLiteDatabase db) {
    List<String> sqlQueries = SqlUtils.makeAlterTableBuilder(SysItem.SysItemEntry.TABLE_NAME)
        .addColumn(dateTimeField(SysItem.SysItemEntry.COLUMN_CREATED_TIME))
        .addColumn(dateTimeField(SysItem.SysItemEntry.COLUMN_LAST_EDITED_TIME))
        .addColumn(dateTimeField(SysItem.SysItemEntry.COLUMN_LAST_VIEWED_TIME))
        .build();
    for (String sql : sqlQueries) {
      db.execSQL(sql);
    }
  }

  private void doUpgrade2To3(SQLiteDatabase db) {

  }

  /**
   * Drops all data from the database and recreates tables.
   */
  void clearDbAndRecreate() {
    SQLiteDatabase db = getWritableDatabase();
    dropAllTables(db);
    doCreateDb(db);
  }

  /**
   * Drops all tables from the database.
   */
  void dropAllTables() {
    SQLiteDatabase db = getWritableDatabase();
    dropAllTables(db);
  }

  /**
   * Drops all tables from the database.
   */
  private void dropAllTables(SQLiteDatabase db) {
    db.execSQL(SysItem.SQL_DROP_TABLE);
    db.execSQL(FavoriteColor.SQL_DROP_TABLE);
  }

  /**
   * Stores the given favorite color into the database. Does noting if the database already contains the color.
   *
   * @param color A favorite color value.
   */
  public void addFavoriteColor(int color) {
    SQLiteDatabase db = getWritableDatabase();

    ContentValues values = new ContentValues();
    values.put(FavoriteColor.FavoriteColorEntry.COLUMN_NAME_COLOR, color);

    db.insertWithOnConflict(FavoriteColor.FavoriteColorEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
  }

  /**
   * Removes the given color value from the database. Does nothing if the database does not contain given color.
   *
   * @param color A favorite color value to remove.
   */
  public void removeFavoriteColor(int color) {
    SQLiteDatabase db = getWritableDatabase();

    db.delete(FavoriteColor.FavoriteColorEntry.TABLE_NAME,
        FavoriteColor.FavoriteColorEntry.COLUMN_NAME_COLOR + " = ?",
        new String[]{String.valueOf(color)});
  }

  /**
   * Retrieves all favorite colors.
   *
   * @return A list of all favorite colors.
   */
  public List<FavoriteColor> findAllFavoriteColors() {
    SQLiteDatabase db = getReadableDatabase();
    String sql = String.format("SELECT a.%s FROM %s a ORDER BY a.%s",
        FavoriteColor.FavoriteColorEntry.COLUMN_NAME_COLOR,
        FavoriteColor.FavoriteColorEntry.TABLE_NAME,
        FavoriteColor.FavoriteColorEntry.COLUMN_NAME_ID);

    return SqlUtils.queryForList(db, sql, FavoriteColorMapper.INSTANCE);
  }

  /**
   * Adds new sys item.
   *
   * @param item An item to be added.
   * @return Added item.
   */
  public SysItem addSysItem(SysItem item) {
    item.setId(null);
    return saveSysItem(item);
  }

  /**
   * Adds new sys item.
   *
   * @param item An item to be added.
   * @return Added item.
   */
  public SysItem saveSysItem(SysItem item) {
    SQLiteDatabase db = getWritableDatabase();

    if (item.getCreatedTime() == null || item.getId() == null) {
      item.setCreatedTime(DateTime.now());
    }
    item.setLastEditedTime(DateTime.now());
    item.setLastViewedTime(DateTime.now());

    ContentValues values = new ContentValuesBuilder()
        .put(SysItem.SysItemEntry.COLUMN_NAME_TITLE, item.getTitle())
        .put(SysItem.SysItemEntry.COLUMN_NAME_BODY, item.getBody())
        .put(SysItem.SysItemEntry.COLUMN_NAME_COLOR, item.getColor())
        .put(SysItem.SysItemEntry.COLUMN_CREATED_TIME, item.getCreatedTime())
        .put(SysItem.SysItemEntry.COLUMN_LAST_EDITED_TIME, item.getLastEditedTime())
        .put(SysItem.SysItemEntry.COLUMN_LAST_VIEWED_TIME, item.getLastViewedTime())
        .build();

    if (item.getId() == null) {
      long id = db.insertOrThrow(SysItem.SysItemEntry.TABLE_NAME, null, values);
      validateInsertedObjectThrowing(id, item);
      item.setId(id);
      GlobalDbState.notifySysItemAdded(item);
    } else {
      int updatedRows = db.update(SysItem.SysItemEntry.TABLE_NAME, values, SysItem.SysItemEntry.COLUMN_NAME_ID + " = ?",
          new String[]{String.valueOf(item.getId())});
      validateUpdatedObjectThrowing(updatedRows, item);
      GlobalDbState.notifySysItemUpdated(item);
    }

    return item;
  }

  private void validateInsertedObjectThrowing(long id, Object o) {
    if (id == -1) {
      throw new RuntimeException("Unable to perform insert operation with object " + o);
    }
  }

  private void validateUpdatedObjectThrowing(int updatedRows, Object o) {
    if (updatedRows != 1) {
      throw new RuntimeException("Unable to update object " + o + ", " + updatedRows + " rows updated");
    }
  }

  /**
   * Retrieves all items.
   *
   * @return A list of all items.
   */
  public List<SysItem> findAllSysItems() {
    SQLiteDatabase db = getReadableDatabase();
    String sql = String.format("SELECT * FROM %s a ORDER BY a.%s",
        SysItem.SysItemEntry.TABLE_NAME,
        SysItem.SysItemEntry.COLUMN_NAME_TITLE);

    return SqlUtils.queryForList(db, sql, SysItemMapper.INSTANCE);
  }

  /**
   * Finds a sys item by the id.
   *
   * @param id SysItem id.
   * @return SysItem with the given id. If not found, returns null.
   */
  public SysItem findSysItemById(Long id) {
    SQLiteDatabase db = getReadableDatabase();
    String sql = String.format("SELECT * FROM %s a WHERE a.%s = ?",
        SysItem.SysItemEntry.TABLE_NAME,
        SysItem.SysItemEntry.COLUMN_NAME_ID);

    return SqlUtils.queryForObject(db, sql, SysItemMapper.INSTANCE, id);
  }

  /**
   * Removes sys item by the id.
   *
   * @param id Sys item id.
   */
  public void removeSysItemById(Long id) {
    SQLiteDatabase db = getWritableDatabase();

    int deletedEntries = db.delete(SysItem.SysItemEntry.TABLE_NAME,
        SysItem.SysItemEntry.COLUMN_NAME_ID + " = ?",
        new String[]{String.valueOf(id)});

    if (deletedEntries > 0) {
      GlobalDbState.notifySysItemDeleted(id);
    }
  }

  private static class FavoriteColorMapper extends RowMapperAdapter<FavoriteColor> {
    public static final FavoriteColorMapper INSTANCE = new FavoriteColorMapper();

    @Override
    public FavoriteColor mapRow(Cursor cursor, int rowNumber) {
      FavoriteColor favoriteColor = new FavoriteColor();
      favoriteColor.setColor(getInt(cursor, FavoriteColor.FavoriteColorEntry.COLUMN_NAME_COLOR));
      return favoriteColor;
    }
  }

  private static class SysItemMapper extends RowMapperAdapter<SysItem> {
    public static final SysItemMapper INSTANCE = new SysItemMapper();

    @Override
    public SysItem mapRow(Cursor cursor, int rowNumber) {
      SysItem sysItem = new SysItem();
      sysItem.setId(getLong(cursor, SysItem.SysItemEntry.COLUMN_NAME_ID));
      sysItem.setTitle(getString(cursor, SysItem.SysItemEntry.COLUMN_NAME_TITLE));
      sysItem.setBody(getString(cursor, SysItem.SysItemEntry.COLUMN_NAME_BODY));
      sysItem.setColor(getInt(cursor, SysItem.SysItemEntry.COLUMN_NAME_COLOR));
      sysItem.setCreatedTime(getDateTime(cursor, SysItem.SysItemEntry.COLUMN_CREATED_TIME));
      sysItem.setLastEditedTime(getDateTime(cursor, SysItem.SysItemEntry.COLUMN_LAST_EDITED_TIME));
      sysItem.setLastViewedTime(getDateTime(cursor, SysItem.SysItemEntry.COLUMN_LAST_VIEWED_TIME));
      return sysItem;
    }
  }
}
