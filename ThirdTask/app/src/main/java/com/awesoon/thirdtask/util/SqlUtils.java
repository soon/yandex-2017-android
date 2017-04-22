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
      sqlBuilder
          .append(separator)
          .append(field.getName())
          .append(" ")
          .append(field.getType());

      if (field.isPk) {
        sqlBuilder.append(" PRIMARY KEY");
      } else {
        if (field.isNull) {
          sqlBuilder.append(" NULL");
        } else {
          sqlBuilder.append(" NOT NULL");
        }
      }

      if (field.isAutoincrement) {
        sqlBuilder.append(" AUTOINCREMENT");
      }

      if (field.isUnique) {
        sqlBuilder.append(" UNIQUE");
      }

      separator = ", ";
    }

    sqlBuilder.append(")");

    return sqlBuilder.toString();
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
  }
}
