package com.awesoon.thirdtask.util;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class JsonUtils {
  public static <T> T parseSingleObject(Moshi moshi, String data, Class<T> clazz) throws IOException {
    JsonAdapter<T> adapter = moshi.adapter(clazz);
    return adapter.nullSafe().fromJson(data);
  }

  public static <T> List<T> parseList(Moshi moshi, String data, Class<T> clazz) throws IOException {
    Type type = Types.newParameterizedType(List.class, clazz);
    JsonAdapter<List<T>> adapter = moshi.adapter(type);
    return adapter.nullSafe().fromJson(data);
  }

  public static <T> String writeSingleObject(Moshi moshi, T data, Class<T> clazz) {
    JsonAdapter<T> adapter = moshi.adapter(clazz);
    return adapter.nullSafe().toJson(data);
  }

  public static <T> String writeList(Moshi moshi, List<T> data, Class<T> clazz) {
    Type type = Types.newParameterizedType(List.class, clazz);
    JsonAdapter<List<T>> adapter = moshi.adapter(type);
    return adapter.nullSafe().toJson(data);
  }
}
