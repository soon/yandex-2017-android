package com.awesoon.core.sql;

import java.util.List;

public class FilteredPage<T> extends Page<T> {
  private int totalSourceElements;

  public int getTotalSourceElements() {
    return totalSourceElements;
  }

  public FilteredPage<T> setTotalSourceElements(int totalSourceElements) {
    this.totalSourceElements = totalSourceElements;
    return this;
  }

  @Override
  public FilteredPage<T> setData(List<T> data) {
    super.setData(data);
    return this;
  }

  @Override
  public FilteredPage<T> setSize(int size) {
    super.setSize(size);
    return this;
  }

  @Override
  public FilteredPage<T> setTotalPages(int totalPages) {
    super.setTotalPages(totalPages);
    return this;
  }

  @Override
  public FilteredPage<T> setNumber(int number) {
    super.setNumber(number);
    return this;
  }

  @Override
  public FilteredPage<T> setTotalElements(int totalElements) {
    super.setTotalElements(totalElements);
    return this;
  }
}
