package com.awesoon.thirdtask.util;

import android.database.Cursor;

public abstract class RowMapperAdapter<T> implements RowMapper<T> {
  public int getInt(Cursor cursor, String columnName) {
    return cursor.getInt(cursor.getColumnIndex(columnName));
  }

  public long getLong(Cursor cursor, String columnName) {
    return cursor.getLong(cursor.getColumnIndex(columnName));
  }

  public String getString(Cursor cursor, String columnName) {
    return cursor.getString(cursor.getColumnIndex(columnName));
  }
}
