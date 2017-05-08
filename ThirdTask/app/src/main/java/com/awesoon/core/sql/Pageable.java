package com.awesoon.core.sql;

public interface Pageable {
  /**
   * The offset to the first element.
   * @return The offset.
   */
  int getOffset();

  /**
   * The page number. Must be greater or equal to zero.
   * @return The page number.
   */
  int getPageNumber();

  /**
   * The size of a page. Must be greater than zero.
   * @return The page size.
   */
  int getPageSize();
}
