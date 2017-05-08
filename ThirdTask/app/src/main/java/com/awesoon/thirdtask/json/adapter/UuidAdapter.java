package com.awesoon.thirdtask.json.adapter;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

import java.util.UUID;

public class UuidAdapter {
  @ToJson
  public String toJson(UUID uuid) {
    return uuid.toString();
  }

  @FromJson
  public UUID fromJson(String uuid) {
    return UUID.fromString(uuid);
  }
}
