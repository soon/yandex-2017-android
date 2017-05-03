package com.awesoon.thirdtask.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.awesoon.thirdtask.repository.filter.SysItemFilter;
import com.awesoon.thirdtask.util.StringUtils;

import java.io.IOException;

public final class SysItemFilterRepository {
  private static final String TAG = "SortFilterRepository";
  public static final String CURRENT_FILTER_IDENT = "CURRENT_FILTER_IDENT";

  public static SysItemFilter getCurrentFilter(Context context) {
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    String json = sharedPreferences.getString(CURRENT_FILTER_IDENT, null);
    if (StringUtils.isBlank(json)) {
      return SysItemFilter.empty();
    }

    try {
      return SysItemFilter.parseJson(json);
    } catch (IOException e) {
      Log.e(TAG, "Unable to retrieve current filter", e);
      return SysItemFilter.empty();
    }
  }

  public static void updateCurrentFilter(Context context, SysItemFilter newFilter) {
    if (newFilter == null) {
      newFilter = SysItemFilter.empty();
    }

    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    sharedPreferences.edit()
        .putString(CURRENT_FILTER_IDENT, newFilter.toJson())
        .apply();
  }
}
