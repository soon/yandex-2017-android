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
import com.awesoon.thirdtask.util.StringUtils;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
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
  public static final int DATABASE_VERSION_4 = 4;
  public static final int DATABASE_VERSION_5 = 5;
  public static final int DATABASE_VERSION_6 = 6;

  public static final int DATABASE_VERSION = DATABASE_VERSION_6;
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
      case DATABASE_VERSION_3:
        doUpgrade3To4(db);
        if (newVersion == DATABASE_VERSION_4) {
          break;
        }
      case DATABASE_VERSION_4:
        doUpgrade4To5(db);
        if (newVersion == DATABASE_VERSION_5) {
          break;
        }
      case DATABASE_VERSION_5:
        doUpgrade5To6(db);
        if (newVersion == DATABASE_VERSION_6) {
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

  private void doUpgrade3To4(SQLiteDatabase db) {
    List<String> newFields = SqlUtils.makeAlterTableBuilder(SysItem.SysItemEntry.TABLE_NAME)
        .addColumn(textField(SysItem.SysItemEntry.COLUMN_IMAGE_URL))
        .build();
    for (String sql : newFields) {
      db.execSQL(sql);
    }
  }

  private void doUpgrade4To5(SQLiteDatabase db) {
    List<String> newFields = SqlUtils.makeAlterTableBuilder(SysItem.SysItemEntry.TABLE_NAME)
        .addColumn(intField(SysItem.SysItemEntry.COLUMN_REMOTE_ID))
        .addColumn(intField(SysItem.SysItemEntry.COLUMN_SYNCED))
        .addColumn(intField(SysItem.SysItemEntry.COLUMN_USER_ID))
        .build();
    for (String sql : newFields) {
      db.execSQL(sql);
    }

    List<String> indices = SqlUtils.makeCreateIndicesSql(SysItem.SysItemEntry.TABLE_NAME,
        SysItem.SysItemEntry.COLUMN_REMOTE_ID,
        SysItem.SysItemEntry.COLUMN_SYNCED,
        SysItem.SysItemEntry.COLUMN_USER_ID
    );
    for (String index : indices) {
      db.execSQL(index);
    }

    final long defaultUserId = 0;
    ContentValues values = new ContentValuesBuilder()
        .put(SysItem.SysItemEntry.COLUMN_USER_ID, defaultUserId)
        .build();
    db.update(SysItem.SysItemEntry.TABLE_NAME, values, null, null);
  }

  private void doUpgrade5To6(SQLiteDatabase db) {
    List<String> newFields = SqlUtils.makeAlterTableBuilder(SysItem.SysItemEntry.TABLE_NAME)
        .addColumn(intField(SysItem.SysItemEntry.COLUMN_STATUS))
        .build();
    for (String sql : newFields) {
      db.execSQL(sql);
    }

    List<String> indices = SqlUtils.makeCreateIndicesSql(SysItem.SysItemEntry.TABLE_NAME,
        SysItem.SysItemEntry.COLUMN_STATUS
    );
    for (String index : indices) {
      db.execSQL(index);
    }

    ContentValues values = new ContentValuesBuilder()
        .put(SysItem.SysItemEntry.COLUMN_STATUS, SysItem.STATUS_ACTIVE)
        .build();
    db.update(SysItem.SysItemEntry.TABLE_NAME, values, null, null);
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
        saveSysItem(item, savingOptions);
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
    return saveSysItem(item, SavingOptions.getDefault());
  }

  public SysItem saveSysItemWithoutNotifications(SysItem item) {
    return saveSysItem(item, SavingOptions.withoutNotifications());
  }

  public SysItem saveSysItemNotifySyncedOnly(SysItem item) {
    return saveSysItem(item, SavingOptions.withoutNotifications().setNotifySynced(true));
  }

  /**
   * Performs item saving.
   *
   * @param item    An item to save.
   * @param options Saving options.
   * @return Saved item.
   */
  public SysItem saveSysItem(SysItem item, SavingOptions options) {
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

    if (item.getStatus() == null) {
      item.setStatus(SysItem.STATUS_ACTIVE);
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
        .put(SysItem.SysItemEntry.COLUMN_IMAGE_URL, item.getImageUrl())
        .put(SysItem.SysItemEntry.COLUMN_REMOTE_ID, item.getRemoteId())
        .put(SysItem.SysItemEntry.COLUMN_SYNCED, item.isSynced())
        .put(SysItem.SysItemEntry.COLUMN_USER_ID, item.getUserId())
        .put(SysItem.SysItemEntry.COLUMN_STATUS, item.getStatus())
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

  @Nullable
  public SysItem createUnderlyingSysItemIfAbsent(@Nullable SysItem item) {
    if (item == null || item.getRemoteId() == null) {
      return null;
    }

    List<SysItem> itemsWithSameRemoteId = findSysItemsByRemoteId(item.getRemoteId());
    if (itemsWithSameRemoteId.size() != 1) {
      return null;
    }

    item.setId(null);
    item.setStatus(SysItem.STATUS_HIDDEN);
    return saveSysItem(item, SavingOptions.withoutNotifications());
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

  public int countAllItems() {
    SQLiteDatabase db = getReadableDatabase();
    String sql = String.format("SELECT * FROM %s a", SysItem.SysItemEntry.TABLE_NAME);
    return SqlUtils.queryCountAll(db, sql);
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

  public void findSysItemsAsync(final long userId, final Pageable pageable, @Nullable final String searchText,
                                @Nullable final SysItemFilter filter, final boolean countAllFilteredItems,
                                Consumer<Page<SysItem>> successConsumer) {
    AsyncTaskBuilder
        .firstly(new AsyncTaskProducer<Page<SysItem>>() {
          @Override
          public Page<SysItem> doApply() {
            return findSysItems(userId, pageable, searchText, filter, countAllFilteredItems);
          }
        }, successConsumer)
        .build().execute();
  }

  public List<SysItem> findSysItemsByUserId(long userId) {
    return findSysItems(userId, null, null, null, false).getData();
  }

  /**
   * Retrieves page of sys items.
   *
   * @return A page of sys items.
   */
  public FilteredPage<SysItem> findSysItems(long userId, @Nullable Pageable pageable, @Nullable String searchText,
                                            @Nullable SysItemFilter filter, boolean countAllFilteredItems) {
    String originalSql = "SELECT * FROM " + SysItem.SysItemEntry.TABLE_NAME + " a";

    List<Object> args = new ArrayList<>();

    String filteredSql = createFilteredSql(userId, searchText, filter, originalSql, args);

    SQLiteDatabase db = getReadableDatabase();
    Page<SysItem> queriedData = SqlUtils.queryForPage(
        db, filteredSql, pageable, SysItemMapper.INSTANCE, countAllFilteredItems, args);

    List<Object> countAllArgs = new ArrayList<>();
    String countAllSql = appendOnlyActiveUserElementsCondition(userId, originalSql, countAllArgs);
    int totalSourceElements = SqlUtils.queryCountAll(db, countAllSql, countAllArgs);

    return new FilteredPage<SysItem>()
        .setData(queriedData.getData())
        .setTotalPages(queriedData.getTotalPages())
        .setSize(queriedData.getSize())
        .setNumber(queriedData.getNumber())
        .setTotalElements(countAllFilteredItems ? queriedData.getTotalElements() : totalSourceElements)
        .setTotalSourceElements(totalSourceElements);
  }

  public String appendOnlyActiveUserElementsCondition(long userId, String sql, List<Object> args) {
    args.add(SysItem.STATUS_ACTIVE);
    args.add(userId);
    return sql + " WHERE a." + SysItem.SysItemEntry.COLUMN_STATUS + " = ? " +
        "AND a." + SysItem.SysItemEntry.COLUMN_USER_ID + " = ?";
  }

  @NonNull
  private String createFilteredSql(long userId, @Nullable String searchText, @Nullable SysItemFilter filter,
                                   String originalSql, List<Object> args) {
    if (filter == null && StringUtils.isBlank(searchText)) {
      return appendOnlyActiveUserElementsCondition(userId, originalSql, args);
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

    if (!StringUtils.isBlank(searchText)) {
      String likeSearchString = createEscapedLikeString(searchText);
      if (whereSb.length() > 0) {
        whereSb.append(" AND");
      }
      whereSb.append(" (a.").append(SysItem.SysItemEntry.COLUMN_NAME_TITLE).append(" LIKE ? ESCAPE '\\'")
          .append(" OR a.").append(SysItem.SysItemEntry.COLUMN_NAME_BODY).append(" LIKE ? ESCAPE '\\'")
          .append(")");

      args.add(likeSearchString);
      args.add(likeSearchString);
    }

    appendActiveFilterToWherePart(whereSb, args);
    appendUserFilterToWherePart(whereSb, userId, args);

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

  private void appendUserFilterToWherePart(StringBuilder whereSb, long userId, List<Object> args) {
    if (whereSb.length() > 0) {
      whereSb.append(" AND");
    }

    whereSb.append(" a." + SysItem.SysItemEntry.COLUMN_USER_ID + " = ?");
    args.add(userId);
  }

  private void appendActiveFilterToWherePart(StringBuilder whereSb, List<Object> args) {
    if (whereSb.length() > 0) {
      whereSb.append(" AND");
    }

    whereSb.append(" a." + SysItem.SysItemEntry.COLUMN_STATUS + " = ?");
    args.add(SysItem.STATUS_ACTIVE);
  }

  private String createEscapedLikeString(String searchText) {
    return "%" + searchText.trim()
        .replace("\\", "\\\\")
        .replace("%", "\\%")
        .replace("_", "\\_") + "%";
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
    String sql = String.format("SELECT * FROM %s a WHERE a.%s = ? AND a.%s = ?",
        SysItem.SysItemEntry.TABLE_NAME,
        SysItem.SysItemEntry.COLUMN_NAME_ID,
        SysItem.SysItemEntry.COLUMN_STATUS);

    SysItem sysItem = SqlUtils.queryForObject(db, sql, SysItemMapper.INSTANCE, id, SysItem.STATUS_ACTIVE);
    if (sysItem != null) {
      saveSysItem(sysItem, new SavingOptions().setOverwriteLastViewedTime(true));
    }
    return sysItem;
  }

  /**
   * Finds a sys item by the remote id.
   *
   * @param remoteId Remote id.
   * @return A list of SysItems with the given remote id.
   */
  @NonNull
  public List<SysItem> findSysItemsByRemoteId(Long remoteId) {
    SQLiteDatabase db = getReadableDatabase();
    String sql = String.format("SELECT * FROM %s a WHERE a.%s = ?",
        SysItem.SysItemEntry.TABLE_NAME,
        SysItem.SysItemEntry.COLUMN_REMOTE_ID);

    List<SysItem> sysItems = SqlUtils.queryForList(db, sql, SysItemMapper.INSTANCE, remoteId);
    return sysItems;
  }

  /**
   * Finds all unsynchronized sys items.
   *
   * @return A list of unsynchronized SysItems.
   */
  @NonNull
  public List<SysItem> findUnsyncedSysItems() {
    SQLiteDatabase db = getReadableDatabase();
    String sql = String.format("SELECT * FROM %s a WHERE a.%s = ?",
        SysItem.SysItemEntry.TABLE_NAME,
        SysItem.SysItemEntry.COLUMN_SYNCED);

    List<SysItem> sysItems = SqlUtils.queryForList(db, sql, SysItemMapper.INSTANCE, Collections.singletonList(false));
    return sysItems;
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
        forceRemoveAllSysItems();
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
          forceRemoveAllSysItems();
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
  public int forceRemoveAllSysItems() {
    SQLiteDatabase db = getWritableDatabase();
    return db.delete(SysItem.SysItemEntry.TABLE_NAME, null, null);
  }

  /**
   * Marks sys item with the given id as removed.
   *
   * @param userId User id.
   */
  public int removeSysItemsByUserId(Long userId) {
    SQLiteDatabase db = getWritableDatabase();

    ContentValues values = new ContentValuesBuilder()
        .put(SysItem.SysItemEntry.COLUMN_STATUS, SysItem.STATUS_REMOVED)
        .put(SysItem.SysItemEntry.COLUMN_SYNCED, false)
        .build();
    int deletedEntries = db.update(SysItem.SysItemEntry.TABLE_NAME,
        values,
        SysItem.SysItemEntry.COLUMN_USER_ID + " = ?",
        new String[]{String.valueOf(userId)});

    if (deletedEntries > 0) {
      GlobalDbState.notifySysItemDeleted(null);
    }

    return deletedEntries;
  }

  /**
   * Marks sys item with the given id as removed.
   *
   * @param id Sys item id.
   */
  public void removeSysItemById(Long id) {
    SQLiteDatabase db = getWritableDatabase();

    ContentValues values = new ContentValuesBuilder()
        .put(SysItem.SysItemEntry.COLUMN_STATUS, SysItem.STATUS_REMOVED)
        .put(SysItem.SysItemEntry.COLUMN_SYNCED, false)
        .build();
    int deletedEntries = db.update(SysItem.SysItemEntry.TABLE_NAME,
        values,
        SysItem.SysItemEntry.COLUMN_NAME_ID + " = ?",
        new String[]{String.valueOf(id)});

    if (deletedEntries > 0) {
      GlobalDbState.notifySysItemDeleted(id);
    }
  }

  /**
   * Removes sys item by the id.
   *
   * @param id Sys item id.
   */
  public void forceRemoveSysItemById(Long id) {
    SQLiteDatabase db = getWritableDatabase();

    int deletedEntries = db.delete(SysItem.SysItemEntry.TABLE_NAME,
        SysItem.SysItemEntry.COLUMN_NAME_ID + " = ?",
        new String[]{String.valueOf(id)});

    if (deletedEntries > 0) {
      GlobalDbState.notifySysItemDeleted(id);
    }
  }

  public void forceRemoveSysItem(SysItem sysItem) {
    forceRemoveSysItemById(sysItem.getId());
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
      sysItem.setImageUrl(tryGetString(cursor, SysItem.SysItemEntry.COLUMN_IMAGE_URL));
      sysItem.setRemoteId(tryGetLong(cursor, SysItem.SysItemEntry.COLUMN_REMOTE_ID));
      sysItem.setSynced(tryGetBoolean(cursor, SysItem.SysItemEntry.COLUMN_SYNCED, false));
      sysItem.setUserId(tryGetLong(cursor, SysItem.SysItemEntry.COLUMN_USER_ID));
      sysItem.setStatus(tryGetLong(cursor, SysItem.SysItemEntry.COLUMN_STATUS));
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
    private boolean notifySynced;
    private Consumer<Integer> onNotesSaved;

    public static SavingOptions getDefault() {
      return new SavingOptions()
          .setNotifyItemInserted(true)
          .setNotifyItemUpdated(true)
          .setOverwriteCreatedTime(true)
          .setOverwriteLastEditedTime(true)
          .setOverwriteLastViewedTime(true)
          .setNotifySynced(false);
    }

    public static SavingOptions withoutNotifications() {
      return new SavingOptions()
          .setNotifyItemInserted(false)
          .setNotifyItemUpdated(false)
          .setOverwriteCreatedTime(false)
          .setOverwriteLastEditedTime(false)
          .setOverwriteLastViewedTime(false)
          .setNotifySynced(false);
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

    public boolean isNotifySynced() {
      return notifySynced;
    }

    public SavingOptions setNotifySynced(boolean notifySynced) {
      this.notifySynced = notifySynced;
      return this;
    }
  }
}
