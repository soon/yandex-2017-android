package com.awesoon.secondtask.domain;

import android.provider.BaseColumns;

public class FavoriteColor {
  public static final String SQL_CREATE_ENTRIES =
      "CREATE TABLE " + FavoriteColorEntry.TABLE_NAME + " (" +
          FavoriteColorEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
          FavoriteColorEntry.COLUMN_NAME_COLOR + " INTEGER NOT NULL UNIQUE)";
  public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + FavoriteColorEntry.TABLE_NAME;

  private int color;

  public int getColor() {
    return color;
  }

  public void setColor(int color) {
    this.color = color;
  }

  public static class FavoriteColorEntry implements BaseColumns {
    public static final String TABLE_NAME = "favorite_color";
    public static final String COLUMN_NAME_COLOR = "color";
  }
}
