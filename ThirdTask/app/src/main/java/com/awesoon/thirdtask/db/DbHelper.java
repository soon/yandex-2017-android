package com.awesoon.thirdtask.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.awesoon.thirdtask.domain.FavoriteColor;
import com.awesoon.thirdtask.domain.SysItem;
import com.awesoon.thirdtask.util.RowMapperAdapter;
import com.awesoon.thirdtask.util.SqlUtils;

import java.util.List;

public class DbHelper extends SQLiteOpenHelper {
  public static final int DATABASE_VERSION = 1;
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
   * Dtops all tables from the database.
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

    ContentValues values = new ContentValues();
    values.put(SysItem.SysItemEntry.COLUMN_NAME_TITLE, item.getTitle());
    values.put(SysItem.SysItemEntry.COLUMN_NAME_BODY, item.getBody());
    values.put(SysItem.SysItemEntry.COLUMN_NAME_COLOR, item.getColor());

    if (item.getId() == null) {
      long id = db.insertOrThrow(SysItem.SysItemEntry.TABLE_NAME, null, values);
      validateIdThrowing(id, item);
      item.setId(id);
    } else {
      db.update(SysItem.SysItemEntry.TABLE_NAME, values, SysItem.SysItemEntry.COLUMN_NAME_ID + " = ?",
          new String[]{String.valueOf(item.getId())});
    }

    return item;
  }

  private void validateIdThrowing(long id, Object o) {
    if (id == -1) {
      throw new RuntimeException("Unable to perform insert / update operation with object " + o);
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
      return sysItem;
    }
  }
}
