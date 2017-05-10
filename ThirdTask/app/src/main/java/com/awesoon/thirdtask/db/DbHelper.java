package com.awesoon.thirdtask.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.awesoon.core.async.AsyncTaskBuilder;
import com.awesoon.core.async.AsyncTaskProducer;
import com.awesoon.core.sql.FilteredPage;
import com.awesoon.core.sql.Page;
import com.awesoon.core.sql.Pageable;
import com.awesoon.thirdtask.domain.FavoriteColor;
import com.awesoon.thirdtask.domain.SysItem;
import com.awesoon.thirdtask.repository.filter.DatePeriodFilter;
import com.awesoon.thirdtask.repository.filter.SortFilter;
import com.awesoon.thirdtask.repository.filter.SysItemFilter;
import com.awesoon.thirdtask.util.Action;
import com.awesoon.thirdtask.util.Assert;
import com.awesoon.thirdtask.util.CollectionUtils;
import com.awesoon.thirdtask.util.Consumer;
import com.awesoon.thirdtask.util.ContentValuesBuilder;
import com.awesoon.thirdtask.util.RowMapperAdapter;
import com.awesoon.thirdtask.util.SqlUtils;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import static com.awesoon.thirdtask.util.SqlUtils.dateTimeField;
import static com.awesoon.thirdtask.util.SqlUtils.intField;
import static com.awesoon.thirdtask.util.SqlUtils.pkIntAutoincrement;
import static com.awesoon.thirdtask.util.SqlUtils.textField;

public class DbHelper extends SQLiteOpenHelper {
  private static final String TAG = "DbHelper";

  public static final int DATABASE_INITIAL_VERSION = 0;
  public static final int DATABASE_VERSION_1 = 1;
  public static final int DATABASE_VERSION_2 = 2;
  public static final int DATABASE_VERSION_3 = 3;

  public static final int DATABASE_VERSION = DATABASE_VERSION_3;
  public static final String DATABASE_NAME = "ThirdTask.db";

  public DbHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    doCreateDb(db);
  }

  private void doCreateDb(SQLiteDatabase db) {
    db.execSQL(SysItem.SQL_CREATE_TABLE);
    for (String index : SysItem.INDICES) {
      db.execSQL(index);
    }
    db.execSQL(FavoriteColor.SQL_CREATE_TABLE);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    doOnUpgrade(db, oldVersion, newVersion);
  }

  void installSpecificDbVersionInternal(int newVersion) {
    performUpgradeInternal(DATABASE_INITIAL_VERSION, newVersion);
  }

  void performUpgradeInternal(int oldVersion, int newVersion) {
    SQLiteDatabase db = getWritableDatabase();
    doOnUpgrade(db, oldVersion, newVersion);
  }

  private void doOnUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    switch (oldVersion) {
      case DATABASE_INITIAL_VERSION:
        doUpgrade0To1(db);
        if (newVersion == DATABASE_VERSION_1) {
          break;
        }
      case DATABASE_VERSION_1:
        doUpgrade1To2(db);
        if (newVersion == DATABASE_VERSION_2) {
          break;
        }
      case DATABASE_VERSION_2:
        doUpgrade2To3(db);
        if (newVersion == DATABASE_VERSION_3) {
          break;
        }
      default:
        throw new IllegalArgumentException(
            "Unknown old DB version " + oldVersion + ", max DB version is " + DATABASE_VERSION);
    }
  }

  private void doUpgrade0To1(SQLiteDatabase db) {
    String sysItemSql = SqlUtils.makeCreateTableSql(SysItem.SysItemEntry.TABLE_NAME,
        pkIntAutoincrement(SysItem.SysItemEntry.COLUMN_NAME_ID),
        textField(SysItem.SysItemEntry.COLUMN_NAME_TITLE).setNull(false),
        textField(SysItem.SysItemEntry.COLUMN_NAME_BODY).setNull(false),
        intField(SysItem.SysItemEntry.COLUMN_NAME_COLOR).setNull(false)
    );

    String favoriteColorSql = SqlUtils.makeCreateTableSql(FavoriteColor.FavoriteColorEntry.TABLE_NAME,
        pkIntAutoincrement(FavoriteColor.FavoriteColorEntry.COLUMN_NAME_ID),
        intField(FavoriteColor.FavoriteColorEntry.COLUMN_NAME_COLOR).setNull(false).setUnique(true)
    );

    db.execSQL(sysItemSql);
    db.execSQL(favoriteColorSql);
  }

  private void doUpgrade1To2(SQLiteDatabase db) {
    List<String> sqlQueries = SqlUtils.makeAlterTableBuilder(SysItem.SysItemEntry.TABLE_NAME)
        .addColumn(dateTimeField(SysItem.SysItemEntry.COLUMN_CREATED_TIME))
        .addColumn(dateTimeField(SysItem.SysItemEntry.COLUMN_LAST_EDITED_TIME))
        .addColumn(dateTimeField(SysItem.SysItemEntry.COLUMN_LAST_VIEWED_TIME))
        .build();
    for (String sql : sqlQueries) {
      db.execSQL(sql);
    }

    DateTime now = DateTime.now();
    ContentValues values = new ContentValuesBuilder()
        .put(SysItem.SysItemEntry.COLUMN_CREATED_TIME, now)
        .put(SysItem.SysItemEntry.COLUMN_LAST_EDITED_TIME, now)
        .put(SysItem.SysItemEntry.COLUMN_LAST_VIEWED_TIME, now)
        .build();
    db.update(SysItem.SysItemEntry.TABLE_NAME, values, null, null);
  }

  private void doUpgrade2To3(SQLiteDatabase db) {
    List<String> newFields = SqlUtils.makeAlterTableBuilder(SysItem.SysItemEntry.TABLE_NAME)
        .addColumn(intField(SysItem.SysItemEntry.COLUMN_CREATED_TIME_TS))
        .addColumn(intField(SysItem.SysItemEntry.COLUMN_LAST_EDITED_TIME_TS))
        .addColumn(intField(SysItem.SysItemEntry.COLUMN_LAST_VIEWED_TIME_TS))
        .build();
    for (String sql : newFields) {
      db.execSQL(sql);
    }

    List<SysItem> sysItems = findAllSysItems();
    for (SysItem item : sysItems) {
      ContentValues values = new ContentValuesBuilder()
          .put(SysItem.SysItemEntry.COLUMN_CREATED_TIME_TS, item.getCreatedTime().getMillis())
          .put(SysItem.SysItemEntry.COLUMN_LAST_EDITED_TIME_TS, item.getLastEditedTime().getMillis())
          .put(SysItem.SysItemEntry.COLUMN_LAST_VIEWED_TIME_TS, item.getLastViewedTime().getMillis())
          .build();
      int updatedRows = db.update(SysItem.SysItemEntry.TABLE_NAME, values,
          SysItem.SysItemEntry.COLUMN_NAME_ID + " = ?", new String[]{String.valueOf(item.getId())});
      validateUpdatedObjectThrowing(updatedRows, item);
    }

    List<String> indices = SqlUtils.makeCreateIndicesSql(SysItem.SysItemEntry.TABLE_NAME,
        SysItem.SysItemEntry.COLUMN_NAME_TITLE,
        SysItem.SysItemEntry.COLUMN_NAME_BODY,
        SysItem.SysItemEntry.COLUMN_NAME_COLOR,
        SysItem.SysItemEntry.COLUMN_CREATED_TIME_TS,
        SysItem.SysItemEntry.COLUMN_LAST_EDITED_TIME_TS,
        SysItem.SysItemEntry.COLUMN_LAST_VIEWED_TIME_TS
    );

    for (String index : indices) {
      db.execSQL(index);
    }
  }

  /**
   * Drops all data from the database and recreates tables.
   */
  void clearDbAndRecreate() {
    SQLiteDatabase db = getWritableDatabase();
    dropAllTables(db);
    doCreateDb(db);
  }

  /**
   * Drops all tables from the database.
   */
  void dropAllTables() {
    SQLiteDatabase db = getWritableDatabase();
    dropAllTables(db);
  }

  /**
   * Drops all tables from the database.
   */
  private void dropAllTables(SQLiteDatabase db) {
    db.execSQL(SysItem.SQL_DROP_TABLE);
    db.execSQL(FavoriteColor.SQL_DROP_TABLE);
  }

  /**
   * Stores the given favorite color into the database. Does noting if the database already contains the color.
   *
   * @param color A favorite color value.
   */
  public void addFavoriteColor(int color) {
    SQLiteDatabase db = getWritableDatabase();

    ContentValues values = new ContentValues();
    values.put(FavoriteColor.FavoriteColorEntry.COLUMN_NAME_COLOR, color);

    db.insertWithOnConflict(FavoriteColor.FavoriteColorEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
  }

  /**
   * Removes the given color value from the database. Does nothing if the database does not contain given color.
   *
   * @param color A favorite color value to remove.
   */
  public void removeFavoriteColor(int color) {
    SQLiteDatabase db = getWritableDatabase();

    db.delete(FavoriteColor.FavoriteColorEntry.TABLE_NAME,
        FavoriteColor.FavoriteColorEntry.COLUMN_NAME_COLOR + " = ?",
        new String[]{String.valueOf(color)});
  }

  /**
   * Retrieves all favorite colors.
   *
   * @return A list of all favorite colors.
   */
  public List<FavoriteColor> findAllFavoriteColors() {
    SQLiteDatabase db = getReadableDatabase();
    String sql = String.format("SELECT a.%s FROM %s a ORDER BY a.%s",
        FavoriteColor.FavoriteColorEntry.COLUMN_NAME_COLOR,
        FavoriteColor.FavoriteColorEntry.TABLE_NAME,
        FavoriteColor.FavoriteColorEntry.COLUMN_NAME_ID);

    return SqlUtils.queryForList(db, sql, FavoriteColorMapper.INSTANCE);
  }

  /**
   * Adds sys items to the db async.
   *
   * @param items             Items to add.
   * @param successConsumer   A success callback. Will be called when all items are added to the db.
   * @param exceptionConsumer An error callback. Will be called if there was an exception during task execution.
   */
  public void addSysItemsAsync(final List<SysItem> items, final Consumer<List<SysItem>> successConsumer,
                               @Nullable final Consumer<Exception> exceptionConsumer) {
    Assert.notNull(items, "items must not be null");
    Assert.notNull(successConsumer, "successConsumer must not be null");

    new AsyncTask<Void, Void, List<SysItem>>() {

      private Exception exception;

      @Override
      protected List<SysItem> doInBackground(Void... params) {
        try {
          return addSysItems(items);
        } catch (Exception e) {
          exception = e;
          Log.e(TAG, "Unable to add sys items async", e);
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
   * Adds sys items to the db.
   *
   * @param items An items to add.
   * @return Added items.
   */
  public List<SysItem> addSysItems(List<SysItem> items) {
    return addSysItems(items, (Consumer<Integer>) null);
  }

  /**
   * Adds sys items to the db.
   *
   * @param items An items to add.
   * @return Added items.
   */
  public List<SysItem> addSysItems(List<SysItem> items, @Nullable Consumer<Integer> onNoteSaved) {
    Assert.notNull(items, "items must not be null");

    SavingOptions savingOptions = SavingOptions.getDefault()
        .setOverwriteCreatedTime(false)
        .setOverwriteLastEditedTime(false)
        .setOverwriteLastViewedTime(false)
        .setOnNoteSaved(onNoteSaved);
    return addSysItems(items, savingOptions);
  }

  /**
   * Adds sys items to the db.
   *
   * @param items An items to add.
   * @return Added items.
   */
  public List<SysItem> addSysItems(List<SysItem> items, SavingOptions savingOptions) {
    Assert.notNull(items, "items must not be null");
    SQLiteDatabase db = getWritableDatabase();

    savingOptions
        .setDb(db)
        .setNotifyItemInserted(false)
        .setNotifyItemUpdated(false);

    try {
      db.beginTransaction();
      for (int i = 0; i < items.size(); i++) {
        SysItem item = items.get(i);
        item.setId(null);
        saveSysItemInternal(item, savingOptions);
        if (savingOptions.getOnNotesSaved() != null) {
          savingOptions.getOnNotesSaved().apply(i);
        }
      }
      db.setTransactionSuccessful();
      GlobalDbState.notifySysItemsAdded(items);
    } finally {
      db.endTransaction();
    }

    return items;
  }

  /**
   * Adds new sys item.
   *
   * @param item An item to be added.
   * @return Added item.
   */
  public SysItem addSysItem(SysItem item) {
    item.setId(null);
    return saveSysItem(item);
  }

  /**
   * Adds new sys item.
   *
   * @param item An item to be added.
   * @return Added item.
   */
  public SysItem saveSysItem(SysItem item) {
    return saveSysItemInternal(item, SavingOptions.getDefault());
  }

  /**
   * Performs item saving.
   *
   * @param item    An item to save.
   * @param options Saving options.
   * @return Saved item.
   */
  private SysItem saveSysItemInternal(SysItem item, SavingOptions options) {
    SQLiteDatabase db = options.getDb();
    if (db == null) {
      db = getWritableDatabase();
    }

    if (item.getCreatedTime() == null || (item.getId() == null && options.isOverwriteCreatedTime())) {
      item.setCreatedTime(DateTime.now());
    }
    if (item.getLastEditedTime() == null || options.isOverwriteLastEditedTime()) {
      item.setLastEditedTime(DateTime.now());
    }
    if (item.getLastViewedTime() == null || options.isOverwriteLastViewedTime()) {
      item.setLastViewedTime(DateTime.now());
    }

    ContentValues values = new ContentValuesBuilder()
        .put(SysItem.SysItemEntry.COLUMN_NAME_TITLE, item.getTitle())
        .put(SysItem.SysItemEntry.COLUMN_NAME_BODY, item.getBody())
        .put(SysItem.SysItemEntry.COLUMN_NAME_COLOR, item.getColor())
        .put(SysItem.SysItemEntry.COLUMN_CREATED_TIME, item.getCreatedTime())
        .put(SysItem.SysItemEntry.COLUMN_CREATED_TIME_TS, item.getCreatedTime().getMillis())
        .put(SysItem.SysItemEntry.COLUMN_LAST_EDITED_TIME, item.getLastEditedTime())
        .put(SysItem.SysItemEntry.COLUMN_LAST_EDITED_TIME_TS, item.getLastEditedTime().getMillis())
        .put(SysItem.SysItemEntry.COLUMN_LAST_VIEWED_TIME, item.getLastViewedTime())
        .put(SysItem.SysItemEntry.COLUMN_LAST_VIEWED_TIME_TS, item.getLastViewedTime().getMillis())
        .build();

    if (item.getId() == null) {
      long id = db.insertOrThrow(SysItem.SysItemEntry.TABLE_NAME, null, values);
      validateInsertedObjectThrowing(id, item);
      item.setId(id);
      if (options.isNotifyItemInserted()) {
        GlobalDbState.notifySysItemAdded(item);
      }
    } else {
      int updatedRows = db.update(SysItem.SysItemEntry.TABLE_NAME, values, SysItem.SysItemEntry.COLUMN_NAME_ID + " = ?",
          new String[]{String.valueOf(item.getId())});
      validateUpdatedObjectThrowing(updatedRows, item);
      if (options.isNotifyItemUpdated()) {
        GlobalDbState.notifySysItemUpdated(item);
      }
    }

    return item;
  }

  private void validateInsertedObjectThrowing(long id, Object o) {
    if (id == -1) {
      throw new RuntimeException("Unable to perform insert operation with object " + o);
    }
  }

  private void validateUpdatedObjectThrowing(int updatedRows, Object o) {
    if (updatedRows != 1) {
      throw new RuntimeException("Unable to update object " + o + ", " + updatedRows + " rows updated");
    }
  }

  /**
   * Finds all sys item async.
   *
   * @param successConsumer A success consumer. Called when all items are retrieved from the db.
   */
  public void findAllSysItemsAsync(final Consumer<List<SysItem>> successConsumer) {
    new AsyncTask<Void, Void, List<SysItem>>() {

      @Override
      protected List<SysItem> doInBackground(Void... params) {
        return findAllSysItems();
      }

      @Override
      protected void onPostExecute(List<SysItem> sysItems) {
        successConsumer.apply(sysItems);
      }
    }.execute();
  }

  /**
   * Retrieves all items.
   *
   * @return A list of all items.
   */
  public List<SysItem> findAllSysItems() {
    SQLiteDatabase db = getReadableDatabase();
    String sql = String.format("SELECT * FROM %s a ORDER BY a.%s",
        SysItem.SysItemEntry.TABLE_NAME,
        SysItem.SysItemEntry.COLUMN_NAME_TITLE);

    return SqlUtils.queryForList(db, sql, SysItemMapper.INSTANCE);
  }

  public void findSysItemsAsync(final Pageable pageable, @Nullable final SysItemFilter filter,
                                Consumer<Page<SysItem>> successConsumer) {
    AsyncTaskBuilder
        .firstly(new AsyncTaskProducer<Page<SysItem>>() {
          @Override
          public Page<SysItem> doApply() {
            return findSysItems(pageable, filter);
          }
        }, successConsumer)
        .build().execute();
  }

  /**
   * Retrieves page of sys items.
   *
   * @return A page of sys items.
   */
  public FilteredPage<SysItem> findSysItems(Pageable pageable, @Nullable SysItemFilter filter) {
    Assert.notNull(pageable, "pageable must not be null");

    String originalSql = "SELECT * FROM " + SysItem.SysItemEntry.TABLE_NAME + " a";

    List<Object> args = new ArrayList<>();

    String filteredSql = createFilteredSql(filter, originalSql, args);

    SQLiteDatabase db = getReadableDatabase();
    Page<SysItem> queriedData = SqlUtils.queryForPage(db, filteredSql, pageable, SysItemMapper.INSTANCE, args);
    int totalSourceElements = SqlUtils.queryCountAll(db, originalSql);

    return new FilteredPage<SysItem>()
        .setData(queriedData.getData())
        .setTotalPages(queriedData.getTotalPages())
        .setSize(queriedData.getSize())
        .setNumber(queriedData.getNumber())
        .setTotalElements(queriedData.getTotalElements())
        .setTotalSourceElements(totalSourceElements);
  }

  @NonNull
  private String createFilteredSql(@Nullable SysItemFilter filter, String originalSql, List<Object> args) {
    if (filter == null) {
      return originalSql;
    }

    StringBuilder whereSb = new StringBuilder();
    if (!CollectionUtils.isEmpty(filter.getColors())) {
      whereSb.append(" a.").append(SysItem.SysItemEntry.COLUMN_NAME_COLOR)
          .append(" IN (").append(SqlUtils.createInPlaceholders(filter.getColors().size())).append(")");
      args.addAll(filter.getColors());
    }

    appendConditionToWherePart(whereSb,
        SysItem.SysItemEntry.COLUMN_CREATED_TIME_TS, filter.getCreatedTimeFilter(), args);
    appendConditionToWherePart(whereSb,
        SysItem.SysItemEntry.COLUMN_LAST_EDITED_TIME_TS, filter.getLastEditedTimeFilter(), args);
    appendConditionToWherePart(whereSb,
        SysItem.SysItemEntry.COLUMN_LAST_VIEWED_TIME_TS, filter.getLastViewedTimeFilter(), args);

    StringBuilder sqlSb = new StringBuilder(originalSql);
    if (whereSb.length() > 0) {
      sqlSb.append(" WHERE").append(whereSb);
    }

    StringBuilder orderBySb = new StringBuilder();
    List<SortFilter> sorts = filter.getSorts();
    if (sorts != null) {
      for (SortFilter sort : sorts) {
        appendSortFilterToOrderByPart(orderBySb, sort);
      }
    }

    sqlSb.append(" ORDER BY");
    if (orderBySb.length() > 0) {
      sqlSb.append(orderBySb);
    } else {
      sqlSb.append(" a.").append(SysItem.SysItemEntry.COLUMN_NAME_TITLE);
    }

    return sqlSb.toString();
  }

  private void appendSortFilterToOrderByPart(StringBuilder orderBySb, SortFilter sort) {
    if (sort == null || sort.getFilteredColumn() == null) {
      return;
    }

    if (orderBySb.length() > 0) {
      orderBySb.append(",");
    }

    orderBySb.append(" a.");
    switch (sort.getFilteredColumn()) {
      case TITLE:
        orderBySb.append(SysItem.SysItemEntry.COLUMN_NAME_TITLE);
        break;
      case BODY:
        orderBySb.append(SysItem.SysItemEntry.COLUMN_NAME_BODY);
        break;
      case CREATED:
        orderBySb.append(SysItem.SysItemEntry.COLUMN_CREATED_TIME_TS);
        break;
      case EDITED:
        orderBySb.append(SysItem.SysItemEntry.COLUMN_LAST_EDITED_TIME_TS);
        break;
      case VIEWED:
        orderBySb.append(SysItem.SysItemEntry.COLUMN_LAST_VIEWED_TIME_TS);
        break;
    }

    orderBySb.append(sort.isAsc() ? " ASC" : " DESC");
  }

  private void appendConditionToWherePart(StringBuilder whereSb, String columnName, DatePeriodFilter filter,
                                          List<Object> args) {
    if (filter.isEmpty()) {
      return;
    }

    appendTimeConditionToWherePart(whereSb, columnName, filter.getFrom(), args);
    appendTimeConditionToWherePart(whereSb, columnName, filter.getTo(), args);
  }

  private void appendTimeConditionToWherePart(StringBuilder whereSb, String columnName, DateTime from,
                                              List<Object> args) {
    if (from == null) {
      return;
    }

    if (whereSb.length() > 0) {
      whereSb.append(" AND");
    }
    whereSb.append(" a.").append(columnName)
        .append(" >= ?");
    args.add(from.getMillis());
  }

  /**
   * Finds a sys item by the id.
   *
   * @param id SysItem id.
   * @return SysItem with the given id. If not found, returns null.
   */
  public SysItem findSysItemById(Long id) {
    SQLiteDatabase db = getReadableDatabase();
    String sql = String.format("SELECT * FROM %s a WHERE a.%s = ?",
        SysItem.SysItemEntry.TABLE_NAME,
        SysItem.SysItemEntry.COLUMN_NAME_ID);

    SysItem sysItem = SqlUtils.queryForObject(db, sql, SysItemMapper.INSTANCE, id);
    if (sysItem != null) {
      saveSysItemInternal(sysItem, new SavingOptions().setOverwriteLastViewedTime(true));
    }
    return sysItem;
  }

  /**
   * Removes all sys items async.
   *
   * @param successAction A success action. Called when the all sys items are removed from the db.
   */
  public void removeAllSysItemsAsync(final Action successAction) {
    Assert.notNull(successAction, "action must not be null");

    new AsyncTask<Void, Void, Void>() {
      @Override
      protected Void doInBackground(Void... params) {
        removeAllSysItems();
        return null;
      }

      @Override
      protected void onPostExecute(Void aVoid) {
        successAction.call();
      }
    }.execute();
  }

  /**
   * Replaces all existing notes with the given list of notes.
   *
   * @param newItems          A list of new notes.
   * @param successConsumer   A success callback. Will be called when all items are added to the db.
   * @param exceptionConsumer An error callback. Will be called if there was an exception during task execution.
   */
  public void replaceAllSysItemsAsync(final List<SysItem> newItems, final Consumer<List<SysItem>> successConsumer,
                                      @Nullable final Consumer<Exception> exceptionConsumer) {
    Assert.notNull(newItems, "newItems must not be null");
    Assert.notNull(successConsumer, "successConsumer must not be null");

    new AsyncTask<Void, Void, List<SysItem>>() {
      private Exception exception;

      @Override
      protected List<SysItem> doInBackground(Void... params) {
        try {
          removeAllSysItems();
          return addSysItems(newItems);
        } catch (Exception e) {
          exception = e;
          return null;
        }
      }

      @Override
      protected void onPostExecute(List<SysItem> sysItems) {
        if (exception == null) {
          successConsumer.apply(sysItems);
        } else if (exceptionConsumer != null) {
          exceptionConsumer.apply(exception);
        }
      }
    }.execute();
  }

  /**
   * Removes all sys items from the db.
   */
  public int removeAllSysItems() {
    SQLiteDatabase db = getWritableDatabase();
    return db.delete(SysItem.SysItemEntry.TABLE_NAME, null, null);
  }

  /**
   * Removes sys item by the id.
   *
   * @param id Sys item id.
   */
  public void removeSysItemById(Long id) {
    SQLiteDatabase db = getWritableDatabase();

    int deletedEntries = db.delete(SysItem.SysItemEntry.TABLE_NAME,
        SysItem.SysItemEntry.COLUMN_NAME_ID + " = ?",
        new String[]{String.valueOf(id)});

    if (deletedEntries > 0) {
      GlobalDbState.notifySysItemDeleted(id);
    }
  }

  private static class FavoriteColorMapper extends RowMapperAdapter<FavoriteColor> {
    public static final FavoriteColorMapper INSTANCE = new FavoriteColorMapper();

    @Override
    public FavoriteColor mapRow(Cursor cursor, int rowNumber) {
      FavoriteColor favoriteColor = new FavoriteColor();
      favoriteColor.setColor(getInt(cursor, FavoriteColor.FavoriteColorEntry.COLUMN_NAME_COLOR));
      return favoriteColor;
    }
  }

  private static class SysItemMapper extends RowMapperAdapter<SysItem> {
    public static final SysItemMapper INSTANCE = new SysItemMapper();

    @Override
    public SysItem mapRow(Cursor cursor, int rowNumber) {
      SysItem sysItem = new SysItem();
      sysItem.setId(getLong(cursor, SysItem.SysItemEntry.COLUMN_NAME_ID));
      sysItem.setTitle(getString(cursor, SysItem.SysItemEntry.COLUMN_NAME_TITLE));
      sysItem.setBody(getString(cursor, SysItem.SysItemEntry.COLUMN_NAME_BODY));
      sysItem.setColor(getInt(cursor, SysItem.SysItemEntry.COLUMN_NAME_COLOR));
      sysItem.setCreatedTime(getDateTime(cursor, SysItem.SysItemEntry.COLUMN_CREATED_TIME));
      sysItem.setLastEditedTime(getDateTime(cursor, SysItem.SysItemEntry.COLUMN_LAST_EDITED_TIME));
      sysItem.setLastViewedTime(getDateTime(cursor, SysItem.SysItemEntry.COLUMN_LAST_VIEWED_TIME));
      return sysItem;
    }
  }

  public static class SavingOptions {
    private SQLiteDatabase db;
    private boolean notifyItemInserted;
    private boolean notifyItemUpdated;
    private boolean overwriteCreatedTime;
    private boolean overwriteLastEditedTime;
    private boolean overwriteLastViewedTime;
    private Consumer<Integer> onNotesSaved;

    public static SavingOptions getDefault() {
      return new SavingOptions()
          .setNotifyItemInserted(true)
          .setNotifyItemUpdated(true)
          .setOverwriteCreatedTime(true)
          .setOverwriteLastEditedTime(true)
          .setOverwriteLastViewedTime(true);
    }

    public SQLiteDatabase getDb() {
      return db;
    }

    public SavingOptions setDb(SQLiteDatabase db) {
      this.db = db;
      return this;
    }

    public boolean isNotifyItemInserted() {
      return notifyItemInserted;
    }

    public SavingOptions setNotifyItemInserted(boolean notifyItemInserted) {
      this.notifyItemInserted = notifyItemInserted;
      return this;
    }

    public boolean isNotifyItemUpdated() {
      return notifyItemUpdated;
    }

    public SavingOptions setNotifyItemUpdated(boolean notifyItemUpdated) {
      this.notifyItemUpdated = notifyItemUpdated;
      return this;
    }

    public boolean isOverwriteCreatedTime() {
      return overwriteCreatedTime;
    }

    public SavingOptions setOverwriteCreatedTime(boolean overwriteCreatedTime) {
      this.overwriteCreatedTime = overwriteCreatedTime;
      return this;
    }

    public boolean isOverwriteLastEditedTime() {
      return overwriteLastEditedTime;
    }

    public SavingOptions setOverwriteLastEditedTime(boolean overwriteLastEditedTime) {
      this.overwriteLastEditedTime = overwriteLastEditedTime;
      return this;
    }

    public boolean isOverwriteLastViewedTime() {
      return overwriteLastViewedTime;
    }

    public SavingOptions setOverwriteLastViewedTime(boolean overwriteLastViewedTime) {
      this.overwriteLastViewedTime = overwriteLastViewedTime;
      return this;
    }

    public Consumer<Integer> getOnNotesSaved() {
      return onNotesSaved;
    }

    public SavingOptions setOnNoteSaved(Consumer<Integer> onNotesSaved) {
      this.onNotesSaved = onNotesSaved;
      return this;
    }
  }
}
