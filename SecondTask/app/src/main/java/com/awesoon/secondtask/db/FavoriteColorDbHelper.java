package com.awesoon.secondtask.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.awesoon.secondtask.domain.FavoriteColor;
import com.awesoon.secondtask.domain.FavoriteColor.FavoriteColorEntry;

import java.util.ArrayList;
import java.util.List;

public class FavoriteColorDbHelper extends SQLiteOpenHelper {
  public static final int DATABASE_VERSION = 1;
  public static final String DATABASE_NAME = "ColorPicker.db";

  public FavoriteColorDbHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL(FavoriteColor.SQL_CREATE_ENTRIES);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

  }

  /**
   * Stores the given favorite color into the database. Does noting if the database already contains the color.
   *
   * @param color A favorite color value.
   */
  public void addFavoriteColor(int color) {
    SQLiteDatabase db = getWritableDatabase();

    ContentValues values = new ContentValues();
    values.put(FavoriteColorEntry.COLUMN_NAME_COLOR, color);

    db.insertWithOnConflict(FavoriteColorEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
  }

  /**
   * Removes the given color value from the database. Does nothing if the database does not contain given color.
   *
   * @param color A favorite color value to remove.
   */
  public void removeFavoriteColor(int color) {
    SQLiteDatabase db = getWritableDatabase();

    db.delete(FavoriteColorEntry.TABLE_NAME,
        FavoriteColorEntry.COLUMN_NAME_COLOR + " = ?",
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
        FavoriteColorEntry.COLUMN_NAME_COLOR,
        FavoriteColorEntry.TABLE_NAME,
        FavoriteColorEntry._ID);

    List<FavoriteColor> colors = new ArrayList<>();
    try (Cursor cursor = db.rawQuery(sql, null)) {
      while (cursor.moveToNext()) {
        FavoriteColor favoriteColor = new FavoriteColor();

        int color = cursor.getInt(cursor.getColumnIndex(FavoriteColorEntry.COLUMN_NAME_COLOR));
        favoriteColor.setColor(color);

        colors.add(favoriteColor);
      }
    }

    return colors;
  }
}
