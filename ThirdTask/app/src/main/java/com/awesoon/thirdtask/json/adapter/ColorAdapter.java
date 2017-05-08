package com.awesoon.thirdtask.json.adapter;

import com.awesoon.thirdtask.json.HexColor;
import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

public class ColorAdapter {
  @ToJson
  String toJson(@HexColor int rgb) {
    return String.format("#%06x", rgb);
  }

  @FromJson
  @HexColor
  int fromJson(String rgb) {
    return ((int) Long.parseLong(rgb.substring(1), 16));
  }
}
