package com.awesoon.thirdtask.json.adapter;

import com.awesoon.thirdtask.util.DateTimeUtils;
import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

import org.joda.time.DateTime;

public class DateTimeAdapter {
  @ToJson
  public  String toJson(DateTime dateTime) {
    return dateTime.toString();
  }

  @FromJson
  public DateTime fromJson(String dateTime) {
    return DateTimeUtils.parseDateTime(dateTime);
  }
}
