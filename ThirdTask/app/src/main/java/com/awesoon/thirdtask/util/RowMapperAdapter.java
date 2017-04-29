package com.awesoon.thirdtask.util;

import android.database.Cursor;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

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

  public DateTime getDateTime(Cursor cursor, String columnName) {
    return getDateTime(cursor, columnName, DateTimeZone.getDefault());
  }

  public DateTime getDateTime(Cursor cursor, String columnName, DateTimeZone timeZone) {
    String dateTime = getString(cursor, columnName);
    if (StringUtils.isBlank(dateTime)) {
      return null;
    }

    DateTimeFormatter dateTimeFormatter = hasMillis(dateTime)
        ? ISODateTimeFormat.dateTime()
        : ISODateTimeFormat.dateTimeNoMillis();

    return dateTimeFormatter.parseDateTime(dateTime).toDateTime(timeZone);
  }

  private boolean hasMillis(String data) {
    // checks whether the data has yyyy-MM-dd'T'HH:mm:ss.SSSZZ format or yyyy-MM-dd'T'HH:mm:ssZZ (note the .SSS part)
    return data.contains(".");
  }
}
