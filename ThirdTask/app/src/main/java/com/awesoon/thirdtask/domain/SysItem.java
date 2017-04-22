package com.awesoon.thirdtask.domain;

import android.provider.BaseColumns;

import com.awesoon.thirdtask.util.SqlUtils;

import static com.awesoon.thirdtask.util.SqlUtils.intField;
import static com.awesoon.thirdtask.util.SqlUtils.pkIntAutoincrement;
import static com.awesoon.thirdtask.util.SqlUtils.textField;

public class SysItem {
  public static final String SQL_CREATE_TABLE = SqlUtils.makeCreateTableSql(SysItemEntry.TABLE_NAME,
      pkIntAutoincrement(SysItemEntry.COLUMN_NAME_ID),
      textField(SysItemEntry.COLUMN_NAME_TITLE).setNull(false),
      textField(SysItemEntry.COLUMN_NAME_BODY).setNull(false),
      intField(SysItemEntry.COLUMN_NAME_COLOR).setNull(false)
  );

  public static final String SQL_DROP_TABLE = SqlUtils.makeDropTableIfExistsSql(SysItemEntry.TABLE_NAME);

  private Long id;
  private String title;
  private String body;
  private int color;

  public Long getId() {
    return id;
  }

  public SysItem setId(Long id) {
    this.id = id;
    return this;
  }

  public String getTitle() {
    return title;
  }

  public SysItem setTitle(String title) {
    this.title = title;
    return this;
  }

  public String getBody() {
    return body;
  }

  public SysItem setBody(String body) {
    this.body = body;
    return this;
  }

  public int getColor() {
    return color;
  }

  public SysItem setColor(int color) {
    this.color = color;
    return this;
  }

  @Override
  public String toString() {
    return "SysItem{" +
        "id=" + id +
        '}';
  }

  public static class SysItemEntry implements BaseColumns {
    public static final String TABLE_NAME = "sys_item";

    public static final String COLUMN_NAME_ID = _ID;
    public static final String COLUMN_NAME_TITLE = "title";
    public static final String COLUMN_NAME_BODY = "body";
    public static final String COLUMN_NAME_COLOR = "color";
  }
}
