package com.awesoon.thirdtask.domain;

import android.provider.BaseColumns;

import com.awesoon.thirdtask.util.SqlUtils;

import static com.awesoon.thirdtask.util.SqlUtils.intField;
import static com.awesoon.thirdtask.util.SqlUtils.pkIntAutoincrement;

public class FavoriteColor {
  public static final String SQL_CREATE_TABLE = SqlUtils.makeCreateTableSql(FavoriteColorEntry.TABLE_NAME,
      pkIntAutoincrement(FavoriteColorEntry.COLUMN_NAME_ID),
      intField(FavoriteColorEntry.COLUMN_NAME_COLOR).setNull(false).setUnique(true)
  );

  public static final String SQL_DROP_TABLE = SqlUtils.makeDropTableIfExistsSql(FavoriteColorEntry.TABLE_NAME);

  private int color;

  public int getColor() {
    return color;
  }

  public FavoriteColor setColor(int color) {
    this.color = color;
    return this;
  }

  public static class FavoriteColorEntry implements BaseColumns {
    public static final String TABLE_NAME = "favorite_color";
    public static final String COLUMN_NAME_ID = _ID;
    public static final String COLUMN_NAME_COLOR = "color";
  }
}
