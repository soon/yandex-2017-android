package com.awesoon.core.sql;

public class PageRequest implements Pageable {
  private int pageNumber;
  private int pageSize;

  public PageRequest(int pageNumber, int pageSize) {
    this.pageNumber = pageNumber;
    this.pageSize = pageSize;
  }

  @Override
  public int getOffset() {
    return pageNumber * pageSize;
  }

  @Override
  public int getPageNumber() {
    return pageNumber;
  }

  @Override
  public int getPageSize() {
    return pageSize;
  }
}
