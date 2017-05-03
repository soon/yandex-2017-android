package com.awesoon.thirdtask.repository;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.awesoon.thirdtask.db.DbHelper;
import com.awesoon.thirdtask.domain.SysItem;
import com.awesoon.thirdtask.repository.filter.DatePeriodFilter;
import com.awesoon.thirdtask.repository.filter.SortFilter;
import com.awesoon.thirdtask.repository.filter.SysItemFilter;
import com.awesoon.thirdtask.util.Action;
import com.awesoon.thirdtask.util.Assert;
import com.awesoon.thirdtask.util.Consumer;
import com.awesoon.thirdtask.util.DateTimeUtils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public final class SysItemRepository {
  private static final String TAG = "SysItemRepository";

  public static FilteredItemsContainer getAllItemsFiltered(@NonNull DbHelper dbHelper,
                                                           @Nullable SysItemFilter sysItemFilter) {
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

    return new FilteredItemsContainer(allItems, filteredItems);
  }

  /**
   * Loads all items from thr given file.
   *
   * @param filePath          A path to file.
   * @param successConsumer   A success consumer. Called when all items are extracted from the file.
   * @param exceptionConsumer An exception consumer. Called when the task fails with an exception.
   */
  public static void loadAllItemsFromFileAsync(final String filePath, final Consumer<List<SysItem>> successConsumer,
                                               @Nullable final Consumer<Exception> exceptionConsumer) {
    Assert.notNull(filePath, "filePath must not be null");
    Assert.notNull(successConsumer, "successConsumer must not be null");

    new AsyncTask<Void, Void, List<SysItem>>() {

      private Exception exception;

      @Override
      protected List<SysItem> doInBackground(Void... params) {
        try {
          return loadAllItemsFromFile(filePath);
        } catch (Exception e) {
          Log.e(TAG, "Unable load items from a file", e);
          exception = e;
        }

        return null;
      }

      @Override
      protected void onPostExecute(List<SysItem> sysItems) {
        if (exception != null) {
          if (exceptionConsumer != null) {
            exceptionConsumer.apply(exception);
          }
        } else {
          successConsumer.apply(sysItems);
        }
      }
    }.execute();
  }

  /**
   * Loads all items from the file.
   *
   * @param filePath A path to file.
   * @return A list of parsed items.
   * @throws IOException When the parser is unable to process the file or there is problem with opening the file.
   */
  public static List<SysItem> loadAllItemsFromFile(String filePath) throws IOException {
    Assert.notNull(filePath, "filePath must not be null");

    try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
      StringBuilder sb = new StringBuilder();
      String s = reader.readLine();
      while (s != null) {
        sb.append(s);
        s = reader.readLine();
      }

      String json = sb.toString();
      SysItemsContainer container = SysItemsContainer.parseJson(json);
      return container.getNotes();
    }
  }

  /**
   * Stores all items to a file async.
   *
   * @param items             A list of items to store.
   * @param filePath          A file path
   * @param successAction     A success action. Called when the all items are saved to the file.
   * @param exceptionConsumer An exception consumer. Called when a task fails with an exception.
   */
  public static void storeAllItemsToFileAsync(final List<SysItem> items, final String filePath,
                                              final Action successAction,
                                              @Nullable final Consumer<Exception> exceptionConsumer) {
    Assert.notNull(items, "items must not be null");
    Assert.notNull(filePath, "filePath must not be null");
    Assert.notNull(successAction, "successAction must not be null");

    new AsyncTask<Void, Void, Void>() {
      private Exception exception;

      @Override
      protected Void doInBackground(Void... params) {
        try {
          storeAllItemsToFile(items, filePath);
        } catch (Exception e) {
          Log.e(TAG, "Unable store items to a file", e);
          exception = e;
        }

        return null;
      }

      @Override
      protected void onPostExecute(Void aVoid) {
        if (exception != null) {
          if (exceptionConsumer != null) {
            exceptionConsumer.apply(exception);
          }
        } else {
          successAction.call();
        }
      }
    }.execute();
  }

  /**
   * Stores all items to the file.
   *
   * @param items    A items to store.
   * @param filePath A file path.
   * @throws IOException When there is a problem with opening the file.
   */
  public static void storeAllItemsToFile(List<SysItem> items, String filePath) throws IOException {
    Assert.notNull(items, "items must not be null");
    Assert.notNull(filePath, "filePath must not be null");

    SysItemsContainer container = new SysItemsContainer(items);
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
      writer.write(container.toJson());
    }
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
          case CREATED:
            return doCompare(lhs.getCreatedTime(), rhs.getCreatedTime(), sortFilter.isAsc());
          case EDITED:
            return doCompare(lhs.getLastEditedTime(), rhs.getLastEditedTime(), sortFilter.isAsc());
          case VIEWED:
            return doCompare(lhs.getLastViewedTime(), rhs.getLastViewedTime(), sortFilter.isAsc());
        }

        return 0;
      }

      private int doCompare(DateTime t1, DateTime t2, boolean isAsc) {
        Integer x = doCompareNullables(t1, t2, isAsc);
        if (x != null) {
          return x;
        }

        DateTimeComparator comparator = DateTimeComparator.getInstance();
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
    if (colors != null && !colors.isEmpty() && !colors.contains(item.getColor())) {
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
