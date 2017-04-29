package com.awesoon.thirdtask.util;

import android.content.ContentValues;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class ContentValuesBuilder {
  private ContentValues values;

  public ContentValuesBuilder() {
    values = new ContentValues();
  }

  public ContentValuesBuilder put(String key, String value) {
    values.put(key, value);
    return this;
  }

  public ContentValuesBuilder put(String key, Integer value) {
    values.put(key, value);
    return this;
  }

  public ContentValuesBuilder put(String key, DateTime value) {
    String str = value.withZone(DateTimeZone.UTC).toString();
    return put(key, str);
  }

  public ContentValues build() {
    return values;
  }
}
