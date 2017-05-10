package com.awesoon.thirdtask.util;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.util.Log;

import com.awesoon.core.sql.Page;
import com.awesoon.core.sql.Pageable;

import java.util.ArrayList;
import java.util.List;

public class SqlUtils {
  private static final String TAG = "SqlUtils";
  public static final String INTEGER_TYPE = "INTEGER";
  public static final String TEXT_TYPE = "TEXT";

  /**
   * Makes CREATE TABLE statement using given table name and fields.
   *
   * @param tableName The table name.
   * @param fields    The table fields.
   * @return CREATE TABLE sql.
   */
  public static String makeCreateTableSql(String tableName, TableFieldDescription... fields) {
    StringBuilder sqlBuilder = new StringBuilder();
    sqlBuilder.append("CREATE TABLE ")
        .append(tableName)
        .append(" (");

    String separator = "";
    for (TableFieldDescription field : fields) {
      sqlBuilder.append(separator).append(field.render());
      separator = ", ";
    }

    sqlBuilder.append(")");

    return sqlBuilder.toString();
  }

  public static String makeCreateIndexSql(String tableName, String indexColumn, String indexName) {
    return String.format("CREATE INDEX %s ON %s (%s)", indexColumn, tableName, indexColumn);
  }

  public static String makeCreateIndexSql(String tableName, String indexColumn) {
    return makeCreateIndexSql(tableName, indexColumn, tableName + "_" + indexColumn + "_idx");
  }

  public static List<String> makeCreateIndicesSql(String tableName, String... indexColumns) {
    List<String> indices = new ArrayList<>();
    for (String indexColumn : indexColumns) {
      indices.add(makeCreateIndexSql(tableName, indexColumn));
    }
    return indices;
  }

  public static AlterTableBuilder makeAlterTableBuilder(String tableName) {
    return new AlterTableBuilder(tableName);
  }

  /**
   * Creates int field.
   *
   * @param name Field name.
   * @return Table field.
   */
  public static TableFieldDescription intField(String name) {
    return new TableFieldDescription().setName(name).setType(INTEGER_TYPE);
  }

  /**
   * Creates text field.
   *
   * @param name Field name.
   * @return Table field.
   */
  public static TableFieldDescription textField(String name) {
    return new TableFieldDescription().setName(name).setType(TEXT_TYPE);
  }

  /**
   * Creates a field for storing datetime values. This is actually just a text field.
   *
   * @param name Field name.
   * @return Table field.
   */
  public static TableFieldDescription dateTimeField(String name) {
    return new TableFieldDescription().setName(name).setType(TEXT_TYPE);
  }

  /**
   * Creates primary key integer autoincrement field.
   *
   * @param name Field name.
   * @return Table field.
   */
  public static TableFieldDescription pkIntAutoincrement(String name) {
    return intField(name).setAutoincrement(true).setPk(true);
  }

  /**
   * Creates DROP TABLE IF EXISTS sql.
   *
   * @param tableName Table to drop.
   * @return DROP TABLE sql.
   */
  public static String makeDropTableIfExistsSql(String tableName) {
    return "DROP TABLE IF EXISTS " + tableName;
  }

  /**
   * Creates a string of form ?,?,...,?
   *
   * @param size The number of placeholders.
   * @return A string of form ?,?,...,?
   */
  public static String createInPlaceholders(int size) {
    Assert.isTrue(size > 0, "size must be greater than zero");
    StringBuilder sb = new StringBuilder(size * 2 - 1);
    sb.append("?");
    for (int i = 1; i < size; i++) {
      sb.append(",?");
    }
    return sb.toString();
  }

  /**
   * Executes given sql and retrieves the first result.
   *
   * @param db            Db connection.
   * @param sql           Sql to execute.
   * @param rowMapper     Row mapper.
   * @param selectionArgs Selection arguments. Nullable.
   * @param <T>           A row type.
   * @return The first retrieved result. null, if the query returns an empty set.
   */
  public static <T> T queryForObject(SQLiteDatabase db, String sql, RowMapper<T> rowMapper,
                                     Object... selectionArgs) {
    List<T> data = queryForList(db, sql, rowMapper, false, selectionArgs);
    return data.isEmpty() ? null : data.get(0);
  }

  /**
   * Executes given sql and retrieves the first result.
   *
   * @param db            Db connection.
   * @param sql           Sql to execute.
   * @param rowMapper     Row mapper.
   * @param selectionArgs Selection arguments. Nullable.
   * @param <T>           A row type.
   * @return The first retrieved result. null, if the query returns an empty set.
   */
  public static <T> T queryForObject(SQLiteDatabase db, String sql, RowMapper<T> rowMapper,
                                     String... selectionArgs) {
    List<T> data = queryForList(db, sql, rowMapper, false, selectionArgs);
    return data.isEmpty() ? null : data.get(0);
  }

  /**
   * Executes given sql and retrieves data using the given row mapper.
   *
   * @param db            Db connection.
   * @param sql           Sql to execute.
   * @param rowMapper     Row mapper.
   * @param selectionArgs Selection arguments. Nullable.
   * @param <T>           A row type.
   * @return A list of selected rows.
   */
  public static <T> List<T> queryForList(SQLiteDatabase db, String sql, RowMapper<T> rowMapper,
                                         Object... selectionArgs) {
    return queryForList(db, sql, rowMapper, true, selectionArgs);
  }

  /**
   * Executes given sql and retrieves data using the given row mapper.
   *
   * @param db            Db connection.
   * @param sql           Sql to execute.
   * @param rowMapper     Row mapper.
   * @param countAllFilteredItems Whether the additional COUNT(*) query should be performed.
   * @param selectionArgs Selection arguments. Nullable.
   * @param <T>           A row type.
   * @return A list of selected rows.
   */
  public static <T> List<T> queryForList(SQLiteDatabase db, String sql,
                                         RowMapper<T> rowMapper, boolean countAllFilteredItems,
                                         Object... selectionArgs) {
    return queryForList(db, sql, rowMapper, countAllFilteredItems, convertToStringArgs(selectionArgs));
  }

  @Nullable
  private static String[] convertToStringArgs(Object[] selectionArgs) {
    if (selectionArgs == null) {
      return null;
    }

    String[] args = new String[selectionArgs.length];
    for (int i = 0; i < args.length; i++) {
      args[i] = selectionArgs[i] == null ? null : selectionArgs[i].toString();
    }

    return args;
  }

  /**
   * Executes given sql and retrieves data using the given row mapper.
   *
   * @param db            Db connection.
   * @param sql           Sql to execute.
   * @param rowMapper     Row mapper.
   * @param countAllFilteredItems Whether the additional COUNT(*) query should be performed.
   * @param selectionArgs Selection arguments. Nullable.
   * @param <T>           A row type.
   * @return A list of selected rows.
   */
  public static <T> List<T> queryForList(SQLiteDatabase db, String sql,
                                         RowMapper<T> rowMapper, boolean countAllFilteredItems,
                                         String... selectionArgs) {
    Page<T> page = queryForPage(db, sql, null, rowMapper, countAllFilteredItems, selectionArgs);
    return page.getData();
  }

  /**
   * Executes given sql and retrieves page of data using the given row mapper.
   *
   * @param db            Db connection.
   * @param sql           Sql to execute.
   * @param pageable      Pagination options. Nullable.
   * @param rowMapper     Row mapper.
   * @param countAllFilteredItems Whether the additional COUNT(*) query should be performed.
   * @param selectionArgs Selection arguments. Nullable.
   * @param <T>           A row type.
   * @return A page of selected rows.
   */
  public static <T> Page<T> queryForPage(SQLiteDatabase db, String sql, @Nullable Pageable pageable,
                                         RowMapper<T> rowMapper, boolean countAllFilteredItems,
                                         List<Object> selectionArgs) {
    Object[] argsArray = CollectionUtils.toArray(selectionArgs, Object.class);
    return queryForPage(db, sql, pageable, rowMapper, countAllFilteredItems, argsArray);
  }

  /**
   * Executes given sql and retrieves page of data using the given row mapper.
   *
   * @param db            Db connection.
   * @param sql           Sql to execute.
   * @param pageable      Pagination options. Nullable.
   * @param rowMapper     Row mapper.
   * @param countAllFilteredItems Whether the additional COUNT(*) query should be performed.
   * @param selectionArgs Selection arguments. Nullable.
   * @param <T>           A row type.
   * @return A page of selected rows.
   */
  public static <T> Page<T> queryForPage(SQLiteDatabase db, String sql, @Nullable Pageable pageable,
                                         RowMapper<T> rowMapper, boolean countAllFilteredItems,
                                         Object... selectionArgs) {
    return queryForPage(db, sql, pageable, rowMapper, countAllFilteredItems, convertToStringArgs(selectionArgs));
  }

  /**
   * Executes given sql and retrieves page of data using the given row mapper.
   *
   * @param db                    Db connection.
   * @param sql                   Sql to execute.
   * @param pageable              Pagination options. Nullable.
   * @param rowMapper             Row mapper.
   * @param countAllFilteredItems Whether the additional COUNT(*) query should be performed.
   * @param selectionArgs         Selection arguments. Nullable.
   * @param <T>                   A row type.
   * @return A page of selected rows.
   */
  public static <T> Page<T> queryForPage(SQLiteDatabase db, String sql, @Nullable Pageable pageable,
                                         RowMapper<T> rowMapper, boolean countAllFilteredItems,
                                         String... selectionArgs) {
    List<T> rows = new ArrayList<>();

    String pagedSql = sql;
    if (pageable != null) {
      pagedSql += " LIMIT " + pageable.getOffset() + ", " + pageable.getPageSize();
    }

    Log.d(TAG, "queryForPage: [" + pagedSql + "], args: [" + concatenateAllStringArgs(selectionArgs) + "]");

    try (Cursor cursor = db.rawQuery(pagedSql, selectionArgs)) {
      int rowNumber = 0;

      while (cursor.moveToNext()) {
        T row = rowMapper.mapRow(cursor, rowNumber);
        rows.add(row);
        ++rowNumber;
      }
    }

    int totalElementsCount;
    if (pageable != null && countAllFilteredItems) {
      totalElementsCount = queryCountAll(db, sql, selectionArgs);
    } else {
      totalElementsCount = rows.size();
    }

    int pageNumber = pageable == null ? 0 : pageable.getPageNumber();
    int pageSize = pageable == null ? totalElementsCount : pageable.getPageSize();
    int totalPages = 0;
    if (pageSize != 0) {
      totalPages = totalElementsCount / pageSize;
      if (totalElementsCount % pageSize != 0) {
        totalPages++;
      }
    }

    Page<T> page = new Page<T>()
        .setData(rows)
        .setTotalElements(totalElementsCount)
        .setNumber(pageNumber)
        .setSize(pageSize)
        .setTotalPages(totalPages);

    return page;
  }

  private static String concatenateAllStringArgs(String... args) {
    if (CollectionUtils.isEmpty(args)) {
      return "";
    }

    StringBuilder sb = new StringBuilder();
    String separator = "";
    for (int i = 0; i < args.length; i++) {
      String arg = args[i];
      sb.append(separator).append(i + 1).append("=").append(arg);
      separator = ", ";
    }

    return sb.toString();
  }

  /**
   * Executes given sql and retrieves data using the given row mapper.
   *
   * @param db        Db connection.
   * @param sql       Sql to execute.
   * @param rowMapper Row mapper.
   * @param countAllFilteredItems Whether the additional COUNT(*) query should be performed.
   * @param <T>       A row type.
   * @return A list of selected rows.
   */
  public static <T> List<T> queryForList(SQLiteDatabase db, String sql, RowMapper<T> rowMapper,
                                         boolean countAllFilteredItems) {
    return queryForList(db, sql, rowMapper, countAllFilteredItems, (String[]) null);
  }

  public static int queryCountAll(SQLiteDatabase db, String sql, String... selectionArgs) {
    String countAllSql = "SELECT COUNT(*) FROM (" + sql + ")";
    RowMapper<Integer> intRowMapper = new RowMapper<Integer>() {
      @Override
      public Integer mapRow(Cursor cursor, int rowNumber) {
        return cursor.getInt(0);
      }
    };
    Integer size = queryForObject(db, countAllSql, intRowMapper, selectionArgs);
    Assert.notNull(size, "size must not be null");

    return size;
  }

  public static class AlterTableBuilder {
    private String tableName;
    private List<TableFieldDescription> newColumns = new ArrayList<>();

    public AlterTableBuilder(String tableName) {
      this.tableName = tableName;
    }

    public AlterTableBuilder addColumn(TableFieldDescription description) {
      newColumns.add(description);
      return this;
    }

    public List<String> build() {
      List<String> sql = new ArrayList<>();
      for (TableFieldDescription newColumn : newColumns) {
        sql.add(String.format("ALTER TABLE %s ADD COLUMN %s", tableName, newColumn.render()));
      }

      return sql;
    }
  }

  public static class TableFieldDescription {
    private String name;
    private String type;
    private boolean isPk;
    private boolean isNull;
    private boolean isUnique;
    private boolean isAutoincrement;

    public TableFieldDescription() {
      this.isNull = true;
    }

    public String getName() {
      return name;
    }

    public TableFieldDescription setName(String name) {
      this.name = name;
      return this;
    }

    public String getType() {
      return type;
    }

    public TableFieldDescription setType(String type) {
      this.type = type;
      return this;
    }

    public boolean isPk() {
      return isPk;
    }

    public TableFieldDescription setPk(boolean pk) {
      isPk = pk;
      return this;
    }

    public boolean isNull() {
      return isNull;
    }

    public TableFieldDescription setNull(boolean aNull) {
      isNull = aNull;
      return this;
    }

    public boolean isUnique() {
      return isUnique;
    }

    public TableFieldDescription setUnique(boolean unique) {
      isUnique = unique;
      return this;
    }

    public boolean isAutoincrement() {
      return isAutoincrement;
    }

    public TableFieldDescription setAutoincrement(boolean autoincrement) {
      isAutoincrement = autoincrement;
      return this;
    }

    public String renderName() {
      return getName();
    }

    public String render() {
      StringBuilder sb = new StringBuilder();
      render(sb);
      return sb.toString();
    }

    public void render(StringBuilder sqlBuilder) {
      sqlBuilder.append(renderName())
          .append(" ")
          .append(getType());

      if (isPk) {
        sqlBuilder.append(" PRIMARY KEY");
      } else {
        if (isNull) {
          sqlBuilder.append(" NULL");
        } else {
          sqlBuilder.append(" NOT NULL");
        }
      }

      if (isAutoincrement) {
        sqlBuilder.append(" AUTOINCREMENT");
      }

      if (isUnique) {
        sqlBuilder.append(" UNIQUE");
      }
    }
  }
}
