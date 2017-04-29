package com.awesoon.thirdtask.domain;

import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;

import com.awesoon.thirdtask.util.SqlUtils;

import org.joda.time.DateTime;

import static com.awesoon.thirdtask.util.SqlUtils.dateTimeField;
import static com.awesoon.thirdtask.util.SqlUtils.intField;
import static com.awesoon.thirdtask.util.SqlUtils.pkIntAutoincrement;
import static com.awesoon.thirdtask.util.SqlUtils.textField;

public class SysItem implements Parcelable {
  public static final String SQL_CREATE_TABLE = SqlUtils.makeCreateTableSql(SysItemEntry.TABLE_NAME,
      pkIntAutoincrement(SysItemEntry.COLUMN_NAME_ID),
      textField(SysItemEntry.COLUMN_NAME_TITLE).setNull(false),
      textField(SysItemEntry.COLUMN_NAME_BODY).setNull(false),
      intField(SysItemEntry.COLUMN_NAME_COLOR).setNull(false),
      dateTimeField(SysItemEntry.COLUMN_CREATED_TIME).setNull(false),
      dateTimeField(SysItemEntry.COLUMN_LAST_EDITED_TIME).setNull(false),
      dateTimeField(SysItemEntry.COLUMN_LAST_VIEWED_TIME).setNull(false)
  );

  public static final String SQL_DROP_TABLE = SqlUtils.makeDropTableIfExistsSql(SysItemEntry.TABLE_NAME);

  private Long id;
  private String title;
  private String body;
  private int color;
  private DateTime createdTime;
  private DateTime lastEditedTime;
  private DateTime lastViewedTime;

  public SysItem() {
  }

  private SysItem(Parcel in) {
    title = in.readString();
    body = in.readString();
    color = in.readInt();
  }

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

  public DateTime getCreatedTime() {
    return createdTime;
  }

  public SysItem setCreatedTime(DateTime createdTime) {
    this.createdTime = createdTime;
    return this;
  }

  public DateTime getLastEditedTime() {
    return lastEditedTime;
  }

  public SysItem setLastEditedTime(DateTime lastEditedTime) {
    this.lastEditedTime = lastEditedTime;
    return this;
  }

  public DateTime getLastViewedTime() {
    return lastViewedTime;
  }

  public SysItem setLastViewedTime(DateTime lastViewedTime) {
    this.lastViewedTime = lastViewedTime;
    return this;
  }

  @Override
  public String toString() {
    return "SysItem{" +
        "id=" + id +
        ", title='" + title + '\'' +
        '}';
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(title);
    dest.writeString(body);
    dest.writeInt(color);
  }

  public static final Creator<SysItem> CREATOR = new Creator<SysItem>() {
    @Override
    public SysItem createFromParcel(Parcel in) {
      return new SysItem(in);
    }

    @Override
    public SysItem[] newArray(int size) {
      return new SysItem[size];
    }
  };

  public static class SysItemEntry implements BaseColumns {
    public static final String TABLE_NAME = "sys_item";

    public static final String COLUMN_NAME_ID = _ID;
    public static final String COLUMN_NAME_TITLE = "title";
    public static final String COLUMN_NAME_BODY = "body";
    public static final String COLUMN_NAME_COLOR = "color";
    public static final String COLUMN_CREATED_TIME = "created_time";
    public static final String COLUMN_LAST_EDITED_TIME = "last_edited_time";
    public static final String COLUMN_LAST_VIEWED_TIME = "last_viewed_time";
  }
}
