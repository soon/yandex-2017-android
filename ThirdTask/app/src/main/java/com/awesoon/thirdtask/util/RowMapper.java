package com.awesoon.thirdtask.util;

import android.database.Cursor;

public interface RowMapper<T> {
  /**
   * Extracts a row from the given cursor.
   *
   * @param cursor    Db cursor.
   * @param rowNumber Current row number (zero based)
   * @return A current row.
   */
  T mapRow(Cursor cursor, int rowNumber);
}
