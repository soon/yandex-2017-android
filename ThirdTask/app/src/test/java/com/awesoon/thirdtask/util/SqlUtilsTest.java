package com.awesoon.thirdtask.util;

import org.junit.Test;

import java.util.List;

import static com.awesoon.thirdtask.util.SqlUtils.dateTimeField;
import static com.awesoon.thirdtask.util.SqlUtils.intField;
import static com.awesoon.thirdtask.util.SqlUtils.pkIntAutoincrement;
import static com.awesoon.thirdtask.util.SqlUtils.textField;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SqlUtilsTest {
  @Test
  public void testDateTimeField() throws Exception {
    // given
    // when
    String sql = SqlUtils.makeCreateTableSql("test_table",
        pkIntAutoincrement("id"),
        dateTimeField("value").setNull(true)
    );

    // then
    assertThat(sql, is("CREATE TABLE test_table (" +
        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "value TEXT NULL)"));
  }

  @Test
  public void testMakeCreateTableSql() throws Exception {
    // given
    // when
    String sql = SqlUtils.makeCreateTableSql("test_table",
        pkIntAutoincrement("id"),
        intField("value").setNull(false),
        textField("name").setUnique(true)
    );

    // then
    assertThat(sql, is("CREATE TABLE test_table (" +
        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "value INTEGER NOT NULL, " +
        "name TEXT NULL UNIQUE)"));
  }

  @Test
  public void testAlterTable() throws Exception {
    // given
    SqlUtils.AlterTableBuilder alterTableBuilder = SqlUtils.makeAlterTableBuilder("foo")
        .addColumn(textField("t"))
        .addColumn(dateTimeField("d").setNull(false));

    // when
    List<String> sql = alterTableBuilder.build();

    // then
    assertThat(sql.size(), is(2));
    assertThat(sql.get(0), is("ALTER TABLE foo ADD COLUMN t TEXT NULL"));
    assertThat(sql.get(1), is("ALTER TABLE foo ADD COLUMN d TEXT NOT NULL"));
  }
}