package com.awesoon.thirdtask.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.awesoon.thirdtask.repository.filter.SysItemFilter;
import com.awesoon.thirdtask.util.Assert;
import com.awesoon.thirdtask.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class SysItemFilterRepository {
  public static final String CURRENT_FILTER_UUID_IDENT = "CURRENT_FILTER_UUID";
  public static final String ALL_FILTER_UUIDS_IDENT = "ALL_FILTERS";

  public static Set<String> getAllFilterUuids(Context context) {
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    Set<String> allFilterIdents = sharedPreferences.getStringSet(ALL_FILTER_UUIDS_IDENT, null);
    return allFilterIdents == null ? new HashSet<String>() : new HashSet<>(allFilterIdents);
  }

  @Nullable
  public static SysItemFilter getFilterByUuid(@NonNull Context context, UUID uuid) {
    Assert.notNull(context, "context must not be null");

    if (uuid == null) {
      return null;
    }

    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    String json = sharedPreferences.getString(uuid.toString(), null);
    if (json == null) {
      return null;
    }

    return SysItemFilter.tryParseJson(json);
  }

  @NonNull
  public static SysItemFilter saveFilter(@NonNull Context context, @NonNull SysItemFilter filter) {
    Assert.notNull(context, "context must not be null");
    Assert.notNull(filter, "filter must not be null");

    if (filter.getUuid() == null) {
      filter.setUuid(getAvailableUuid(context));
    }

    Set<String> allFilterUuids = getAllFilterUuids(context);
    allFilterUuids.add(filter.getUuid().toString());
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    sharedPreferences.edit()
        .putStringSet(ALL_FILTER_UUIDS_IDENT, allFilterUuids)
        .putString(filter.getUuid().toString(), filter.toJson())
        .apply();

    return filter;
  }

  public static void removeFiltersByUuids(@NonNull Context context, @NonNull Set<UUID> uuidsToRemove) {
    Assert.notNull(context, "context must not be null");
    Assert.notNull(uuidsToRemove, "uuidsToRemove must not be null");

    Set<String> allFilterUuids = getAllFilterUuids(context);
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    SharedPreferences.Editor editor = sharedPreferences.edit();

    for (UUID uuid : uuidsToRemove) {
      allFilterUuids.remove(uuid.toString());
      editor.remove(uuid.toString());
    }

    editor.putStringSet(ALL_FILTER_UUIDS_IDENT, allFilterUuids)
        .apply();
  }

  @NonNull
  public static List<SysItemFilter> getAllFilters(@NonNull Context context) {
    Assert.notNull(context, "context must not be null");

    Set<String> allFilterUuids = getAllFilterUuids(context);
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    List<SysItemFilter> allFilters = new ArrayList<>();

    for (String uuid : allFilterUuids) {
      String json = sharedPreferences.getString(uuid, null);
      SysItemFilter filter = SysItemFilter.tryParseJson(json);
      if (filter != null) {
        allFilters.add(filter);
      }
    }

    Collections.sort(allFilters, new Comparator<SysItemFilter>() {
      @Override
      public int compare(SysItemFilter lhs, SysItemFilter rhs) {
        if (lhs.getName() == null) {
          return -1;
        }
        if (rhs.getName() == null) {
          return 1;
        }
        return lhs.getName().compareTo(rhs.getName());
      }
    });

    return allFilters;
  }

  public static void setCurrentFilter(@NonNull Context context, @Nullable UUID uuid) {
    Assert.notNull(context, "context must not be null");

    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    if (uuid == null) {
      sharedPreferences.edit()
          .putString(CURRENT_FILTER_UUID_IDENT, null)
          .apply();
    } else {
      sharedPreferences.edit()
          .putString(CURRENT_FILTER_UUID_IDENT, uuid.toString())
          .apply();
    }
  }

  @NonNull
  private static UUID getAvailableUuid(@NonNull Context context) {
    Assert.notNull(context, "context must not be null");

    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    UUID uuid = UUID.randomUUID();
    while (sharedPreferences.contains(uuid.toString())) {
      uuid = UUID.randomUUID();
    }

    return uuid;
  }

  @Nullable
  public static UUID getCurrentFilterUuid(@NonNull Context context) {
    Assert.notNull(context, "context must not be null");

    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    String uuid = sharedPreferences.getString(CURRENT_FILTER_UUID_IDENT, null);
    if (StringUtils.isBlank(uuid)) {
      return null;
    } else {
      return UUID.fromString(uuid);
    }
  }

  @NonNull
  public static SysItemFilter getCurrentFilter(@NonNull Context context) {
    Assert.notNull(context, "context must not be null");

    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    UUID uuid = getCurrentFilterUuid(context);
    if (uuid == null) {
      return SysItemFilter.empty();
    }

    String json = sharedPreferences.getString(uuid.toString(), null);
    if (StringUtils.isBlank(json)) {
      return SysItemFilter.empty();
    }

    SysItemFilter filter = SysItemFilter.tryParseJson(json);
    return filter == null ? SysItemFilter.empty() : filter;
  }

  @NonNull
  public static SysItemFilter updateCurrentFilter(@NonNull Context context, @Nullable SysItemFilter newFilter) {
    Assert.notNull(context, "context must not be null");

    if (newFilter == null) {
      newFilter = SysItemFilter.empty();
    }

    UUID uuid = getCurrentFilterUuid(context);
    if (uuid == null) {
      uuid = getAvailableUuid(context);
    }

    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    SharedPreferences.Editor editor = sharedPreferences.edit();

    newFilter.setUuid(uuid);
    Set<String> allUuids = getAllFilterUuids(context);
    if (!allUuids.contains(uuid.toString())) {
      allUuids.add(uuid.toString());
      editor.putStringSet(ALL_FILTER_UUIDS_IDENT, allUuids);
    }

    editor
        .putString(uuid.toString(), newFilter.toJson())
        .putString(CURRENT_FILTER_UUID_IDENT, uuid.toString())
        .apply();

    return newFilter;
  }
}
