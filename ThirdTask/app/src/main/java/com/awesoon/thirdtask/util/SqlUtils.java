package com.awesoon.thirdtask.util;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class SqlUtils {
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
    List<T> data = queryForList(db, sql, rowMapper, selectionArgs);
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
    if (selectionArgs == null) {
      return queryForList(db, sql, rowMapper, (String[]) null);
    }
    String[] args = new String[selectionArgs.length];
    for (int i = 0; i < args.length; i++) {
      args[i] = selectionArgs[i] == null ? null : selectionArgs[i].toString();
    }

    return queryForList(db, sql, rowMapper, args);
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
                                         String... selectionArgs) {
    List<T> rows = new ArrayList<>();

    try (Cursor cursor = db.rawQuery(sql, selectionArgs)) {
      int rowNumber = 0;

      while (cursor.moveToNext()) {
        T row = rowMapper.mapRow(cursor, rowNumber);
        rows.add(row);
        ++rowNumber;
      }
    }

    return rows;
  }

  /**
   * Executes given sql and retrieves data using the given row mapper.
   *
   * @param db        Db connection.
   * @param sql       Sql to execute.
   * @param rowMapper Row mapper.
   * @param <T>       A row type.
   * @return A list of selected rows.
   */
  public static <T> List<T> queryForList(SQLiteDatabase db, String sql, RowMapper<T> rowMapper) {
    return queryForList(db, sql, rowMapper, (String[]) null);
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
