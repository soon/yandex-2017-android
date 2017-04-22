package com.awesoon.thirdtask.util;

import org.junit.Test;

import static com.awesoon.thirdtask.util.SqlUtils.intField;
import static com.awesoon.thirdtask.util.SqlUtils.pkIntAutoincrement;
import static com.awesoon.thirdtask.util.SqlUtils.textField;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SqlUtilsTest {
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
}