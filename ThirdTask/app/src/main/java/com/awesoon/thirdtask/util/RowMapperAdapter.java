package com.awesoon.thirdtask.util;

import android.database.Cursor;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public abstract class RowMapperAdapter<T> implements RowMapper<T> {
  public int getInt(Cursor cursor, String columnName) {
    return cursor.getInt(cursor.getColumnIndexOrThrow(columnName));
  }

  public long getLong(Cursor cursor, String columnName) {
    return cursor.getLong(cursor.getColumnIndexOrThrow(columnName));
  }

  public Long tryGetLong(Cursor cursor, int columnIndex) {
    if (columnIndex < 0) {
      return null;
    }
    if (cursor.isNull(columnIndex)) {
      return null;
    }
    return cursor.getLong(columnIndex);
  }

  public Long tryGetLong(Cursor cursor, String columnName) {
    return tryGetLong(cursor, cursor.getColumnIndex(columnName));
  }

  public String getString(Cursor cursor, String columnName) {
    return cursor.getString(cursor.getColumnIndexOrThrow(columnName));
  }

  public String tryGetString(Cursor cursor, String columnName) {
    int columnIndex = cursor.getColumnIndex(columnName);
    if (columnIndex < 0) {
      return null;
    }
    return cursor.getString(columnIndex);
  }

  public boolean tryGetBoolean(Cursor cursor, int columnIndex, boolean defaultValue) {
    if (columnIndex < 0) {
      return defaultValue;
    }
    if (cursor.isNull(columnIndex)) {
      return defaultValue;
    }
    return cursor.getLong(columnIndex) != 0;
  }

  public boolean tryGetBoolean(Cursor cursor, String columnName, boolean defaultValue) {
    return tryGetBoolean(cursor, cursor.getColumnIndex(columnName), defaultValue);
  }

  public DateTime getDateTime(Cursor cursor, String columnName) {
    return getDateTime(cursor, columnName, DateTimeZone.getDefault());
  }

  public DateTime getDateTime(Cursor cursor, String columnName, DateTimeZone timeZone) {
    String dateTime = getString(cursor, columnName);
    if (StringUtils.isBlank(dateTime)) {
      return null;
    }

    return DateTimeUtils.parseDateTime(dateTime, timeZone);
  }
}
