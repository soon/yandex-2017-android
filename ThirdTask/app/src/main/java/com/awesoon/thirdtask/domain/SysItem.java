package com.awesoon.thirdtask.domain;

import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;

import com.awesoon.thirdtask.json.HexColor;
import com.awesoon.thirdtask.json.adapter.ColorAdapter;
import com.awesoon.thirdtask.json.adapter.DateTimeAdapter;
import com.awesoon.thirdtask.util.JsonUtils;
import com.awesoon.thirdtask.util.SqlUtils;
import com.squareup.moshi.Json;
import com.squareup.moshi.Moshi;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.List;

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
      intField(SysItemEntry.COLUMN_CREATED_TIME_TS).setNull(false),
      dateTimeField(SysItemEntry.COLUMN_LAST_EDITED_TIME).setNull(false),
      intField(SysItemEntry.COLUMN_LAST_EDITED_TIME_TS).setNull(false),
      dateTimeField(SysItemEntry.COLUMN_LAST_VIEWED_TIME).setNull(false),
      intField(SysItemEntry.COLUMN_LAST_VIEWED_TIME_TS).setNull(false),
      textField(SysItemEntry.COLUMN_IMAGE_URL).setNull(true),
      intField(SysItemEntry.COLUMN_REMOTE_ID).setNull(true).setUnique(true),
      intField(SysItemEntry.COLUMN_SYNCED).setNull(true),
      intField(SysItemEntry.COLUMN_USER_ID).setNull(true)
  );

  public static final String[] INDICES = new String[] {
      SqlUtils.makeCreateIndexSql(SysItemEntry.TABLE_NAME, SysItemEntry.COLUMN_NAME_TITLE),
      SqlUtils.makeCreateIndexSql(SysItemEntry.TABLE_NAME, SysItemEntry.COLUMN_NAME_BODY),
      SqlUtils.makeCreateIndexSql(SysItemEntry.TABLE_NAME, SysItemEntry.COLUMN_NAME_COLOR),
      SqlUtils.makeCreateIndexSql(SysItemEntry.TABLE_NAME, SysItemEntry.COLUMN_CREATED_TIME_TS),
      SqlUtils.makeCreateIndexSql(SysItemEntry.TABLE_NAME, SysItemEntry.COLUMN_LAST_EDITED_TIME_TS),
      SqlUtils.makeCreateIndexSql(SysItemEntry.TABLE_NAME, SysItemEntry.COLUMN_LAST_VIEWED_TIME_TS),
      SqlUtils.makeCreateIndexSql(SysItemEntry.TABLE_NAME, SysItemEntry.COLUMN_REMOTE_ID),
      SqlUtils.makeCreateIndexSql(SysItemEntry.TABLE_NAME, SysItemEntry.COLUMN_SYNCED),
      SqlUtils.makeCreateIndexSql(SysItemEntry.TABLE_NAME, SysItemEntry.COLUMN_USER_ID),
  };

  public static final String SQL_DROP_TABLE = SqlUtils.makeDropTableIfExistsSql(SysItemEntry.TABLE_NAME);

  private Long id;
  private String title;
  @Json(name = "description")
  private String body;
  @HexColor
  private int color;
  @Json(name = "created")
  private DateTime createdTime;
  @Json(name = "edited")
  private DateTime lastEditedTime;
  @Json(name = "viewed")
  private DateTime lastViewedTime;
  @Json(name = "imageUrl")
  private String imageUrl;

  private Long remoteId;
  private boolean synced;
  private Long userId;

  public SysItem() {
  }

  public SysItem(String title, String body, int color, DateTime createdTime, DateTime lastEditedTime,
                 DateTime lastViewedTime) {
    this.title = title;
    this.body = body;
    this.color = color;
    this.createdTime = createdTime;
    this.lastEditedTime = lastEditedTime;
    this.lastViewedTime = lastViewedTime;
  }

  private SysItem(Parcel in) {
    title = in.readString();
    body = in.readString();
    color = in.readInt();
  }

  /**
   * Parses a list of items from the given json string.
   *
   * @param json A json string.
   * @return A list of parsed items.
   * @throws IOException When the parser is unable to process the json.
   */
  public static List<SysItem> parseJsonToList(String json) throws IOException {
    Moshi moshi = new Moshi.Builder()
        .add(new ColorAdapter())
        .add(new DateTimeAdapter())
        .build();

    return JsonUtils.parseList(moshi, json, SysItem.class);
  }

  /**
   * Parses an item from the given json string.
   *
   * @param json A json string.
   * @return A parsed item.
   * @throws IOException When the parser is unable to process the json.
   */
  public static SysItem parseJson(String json) throws IOException {
    Moshi moshi = new Moshi.Builder()
        .add(new ColorAdapter())
        .add(new DateTimeAdapter())
        .build();

    return JsonUtils.parseSingleObject(moshi, json, SysItem.class);
  }

  /**
   * Converts current object to a json representation.
   *
   * @return A json representation.
   */
  public String toJson() {
    Moshi moshi = new Moshi.Builder()
        .add(new ColorAdapter())
        .add(new DateTimeAdapter())
        .build();

    return JsonUtils.writeSingleObject(moshi, this, SysItem.class);
  }

  /**
   * Converts a list of items into a list of
   *
   * @param items A list of items to convert to json.
   * @return A json representation of the given items list.
   */
  public static String toJson(List<SysItem> items) {
    Moshi moshi = new Moshi.Builder()
        .add(new ColorAdapter())
        .add(new DateTimeAdapter())
        .build();

    return JsonUtils.writeList(moshi, items, SysItem.class);
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

  public String getImageUrl() {
    return imageUrl;
  }

  public SysItem setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
    return this;
  }

  public Long getRemoteId() {
    return remoteId;
  }

  public SysItem setRemoteId(Long remoteId) {
    this.remoteId = remoteId;
    return this;
  }

  public boolean isSynced() {
    return synced;
  }

  public SysItem setSynced(boolean synced) {
    this.synced = synced;
    return this;
  }

  public Long getUserId() {
    return userId;
  }

  public SysItem setUserId(Long userId) {
    this.userId = userId;
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
    public static final String COLUMN_CREATED_TIME_TS = "created_time_ts";
    public static final String COLUMN_LAST_EDITED_TIME = "last_edited_time";
    public static final String COLUMN_LAST_EDITED_TIME_TS = "last_edited_time_ts";
    public static final String COLUMN_LAST_VIEWED_TIME = "last_viewed_time";
    public static final String COLUMN_LAST_VIEWED_TIME_TS = "last_viewed_time_ts";
    public static final String COLUMN_IMAGE_URL = "image_url";
    public static final String COLUMN_REMOTE_ID = "remote_id";
    public static final String COLUMN_SYNCED = "synced";
    public static final String COLUMN_USER_ID = "user_id";
  }
}
