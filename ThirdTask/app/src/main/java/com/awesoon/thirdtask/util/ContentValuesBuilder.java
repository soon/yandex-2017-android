package com.awesoon.thirdtask.util;

import android.content.ContentValues;

import org.joda.time.DateTime;

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
    if (value == null) {
      return put(key, (String) null);
    } else {
      return put(key, value.toString());
    }
  }

  public ContentValues build() {
    return values;
  }
}
