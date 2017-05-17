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
