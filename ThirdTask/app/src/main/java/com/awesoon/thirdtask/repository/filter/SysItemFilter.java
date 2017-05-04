package com.awesoon.thirdtask.repository.filter;

import android.support.annotation.Nullable;
import android.util.Log;

import com.awesoon.thirdtask.json.adapter.DateTimeAdapter;
import com.awesoon.thirdtask.json.adapter.UuidAdapter;
import com.awesoon.thirdtask.util.JsonUtils;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class SysItemFilter {
  private static final String TAG = "SysItemFilter";

  private UUID uuid;
  private String name;
  private DatePeriodFilter createdDateFilter;
  private DatePeriodFilter lastEditedDateFilter;
  private DatePeriodFilter lastViewedDateFilter;
  private Set<Integer> colors;
  private List<SortFilter> sorts;

  public static SysItemFilter empty() {
    return new SysItemFilter();
  }

  public static SysItemFilter parseJson(String json) throws IOException {
    Moshi moshi = new Moshi.Builder()
        .add(new DateTimeAdapter())
        .add(new UuidAdapter())
        .build();

    return JsonUtils.parseSingleObject(moshi, json, SysItemFilter.class);
  }

  @Nullable
  public static SysItemFilter tryParseJson(String json) {
    try {
      return parseJson(json);
    } catch (Exception e) {
      Log.e(TAG, "Unable to parse a json " + json, e);
      return null;
    }
  }

  public String toJson() {
    Moshi moshi = new Moshi.Builder()
        .add(new DateTimeAdapter())
        .add(new UuidAdapter())
        .build();

    return JsonUtils.writeSingleObject(moshi, this, SysItemFilter.class);
  }

  public UUID getUuid() {
    return uuid;
  }

  public SysItemFilter setUuid(UUID uuid) {
    this.uuid = uuid;
    return this;
  }

  public String getName() {
    return name;
  }

  public SysItemFilter setName(String name) {
    this.name = name;
    return this;
  }

  public DatePeriodFilter getCreatedTimeFilter() {
    return createdDateFilter;
  }

  public SysItemFilter setCreatedDateFilter(DatePeriodFilter createdDateFilter) {
    this.createdDateFilter = createdDateFilter;
    return this;
  }

  public DatePeriodFilter getLastEditedTimeFilter() {
    return lastEditedDateFilter;
  }

  public SysItemFilter setLastEditedDateFilter(DatePeriodFilter lastEditedDateFilter) {
    this.lastEditedDateFilter = lastEditedDateFilter;
    return this;
  }

  public DatePeriodFilter getLastViewedTimeFilter() {
    return lastViewedDateFilter;
  }

  public SysItemFilter setLastViewedDateFilter(DatePeriodFilter lastViewedDateFilter) {
    this.lastViewedDateFilter = lastViewedDateFilter;
    return this;
  }

  public Set<Integer> getColors() {
    return colors;
  }

  public SysItemFilter setColors(Set<Integer> colors) {
    this.colors = colors;
    return this;
  }

  public List<SortFilter> getSorts() {
    return sorts;
  }

  public SysItemFilter setSorts(List<SortFilter> sorts) {
    this.sorts = sorts;
    return this;
  }
}
