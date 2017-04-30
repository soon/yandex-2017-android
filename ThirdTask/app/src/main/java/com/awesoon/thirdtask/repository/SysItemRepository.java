package com.awesoon.thirdtask.repository;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.awesoon.thirdtask.db.DbHelper;
import com.awesoon.thirdtask.domain.SysItem;
import com.awesoon.thirdtask.repository.filter.DatePeriodFilter;
import com.awesoon.thirdtask.repository.filter.SortFilter;
import com.awesoon.thirdtask.repository.filter.SysItemFilter;
import com.awesoon.thirdtask.util.Assert;
import com.awesoon.thirdtask.util.DateTimeUtils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public final class SysItemRepository {
  public static List<SysItem> getAllItemsFiltered(@NonNull DbHelper dbHelper, @Nullable SysItemFilter sysItemFilter) {
    Assert.notNull(dbHelper, "dbHelper must not be null");

    List<SysItem> allItems = dbHelper.findAllSysItems();
    List<SysItem> filteredItems = new ArrayList<>();

    for (SysItem item : allItems) {
      if (isItemSelected(item, sysItemFilter)) {
        filteredItems.add(item);
      }
    }

    Comparator<? super SysItem> comparator = createItemComparator(sysItemFilter);
    if (comparator != null) {
      Collections.sort(filteredItems, comparator);
    }

    return filteredItems;
  }

  @Nullable
  private static Comparator<? super SysItem> createItemComparator(SysItemFilter filter) {
    if (filter == null || filter.getSorts() == null) {
      return null;
    }

    final List<SortFilter> sorts = filter.getSorts();

    return new Comparator<SysItem>() {
      @Override
      public int compare(SysItem lhs, SysItem rhs) {
        for (int i = 0; i < sorts.size(); i++) {
          SortFilter sortFilter = sorts.get(i);
          int cmp = doCompareField(lhs, rhs, sortFilter);
          if (cmp != 0) {
            return cmp;
          }
        }

        return 0;
      }

      private int doCompareField(SysItem lhs, SysItem rhs, SortFilter sortFilter) {
        if (sortFilter == null || sortFilter.getFilteredColumn() == null) {
          return 0;
        }

        switch (sortFilter.getFilteredColumn()) {
          case TITLE:
            return doCompare(lhs.getTitle(), rhs.getTitle(), sortFilter.isAsc());
          case BODY:
            return doCompare(lhs.getBody(), rhs.getBody(), sortFilter.isAsc());
          case CREATED_TIME:
            return doCompare(lhs.getCreatedTime(), rhs.getCreatedTime(), sortFilter.isAsc());
          case LAST_EDITED_TIME:
            return doCompare(lhs.getLastEditedTime(), rhs.getLastEditedTime(), sortFilter.isAsc());
          case LAST_VIEWED_TIME:
            return doCompare(lhs.getLastViewedTime(), rhs.getLastViewedTime(), sortFilter.isAsc());
        }

        return 0;
      }

      private int doCompare(DateTime t1, DateTime t2, boolean isAsc) {
        Integer x = doCompareNullables(t1, t2, isAsc);
        if (x != null) {
          return x;
        }

        DateTimeComparator comparator = DateTimeComparator.getDateOnlyInstance();
        return isAsc ? comparator.compare(t1, t2) : comparator.compare(t2, t1);
      }

      private <T extends Comparable<? super T>> int doCompare(T t1, T t2, boolean isAsc) {
        Integer x = doCompareNullables(t1, t2, isAsc);
        if (x != null) {
          return x;
        }

        return isAsc ? t1.compareTo(t2) : t2.compareTo(t1);
      }

      @Nullable
      private <T extends Comparable<? super T>> Integer doCompareNullables(T t1, T t2, boolean isAsc) {
        if (t1 == null && t2 == null) {
          return 0;
        }
        if (t1 == null) {
          return isAsc ? -1 : 1;
        }
        if (t2 == null) {
          return isAsc ? 1 : -1;
        }

        return null;
      }
    };
  }

  private static boolean isItemSelected(SysItem item, SysItemFilter sysItemFilter) {
    if (sysItemFilter == null) {
      return true;
    }

    Set<Integer> colors = sysItemFilter.getColors();
    if (colors != null && !colors.contains(item.getColor())) {
      return false;
    }

    if (!isItemSelected(item.getCreatedTime(), sysItemFilter.getCreatedTimeFilter())) {
      return false;
    }

    if (!isItemSelected(item.getLastEditedTime(), sysItemFilter.getLastEditedTimeFilter())) {
      return false;
    }

    if (!isItemSelected(item.getLastViewedTime(), sysItemFilter.getLastViewedTimeFilter())) {
      return false;
    }

    return true;
  }

  private static boolean isItemSelected(DateTime time, DatePeriodFilter timeFilter) {
    if (time == null || timeFilter == null) {
      return true;
    }

    DateTime from = timeFilter.getFrom();
    DateTime to = timeFilter.getTo();

    if (to == null && from == null) {
      return true;
    }

    if (from != null && DateTimeUtils.isBeforeDateOnly(time, from)) {
      return false;
    }

    if (to != null && DateTimeUtils.isAfterDateOnly(time, to)) {
      return false;
    }

    return true;
  }

}
