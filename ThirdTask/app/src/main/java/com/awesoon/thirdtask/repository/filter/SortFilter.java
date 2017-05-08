package com.awesoon.thirdtask.repository.filter;

import java.util.Objects;

public class SortFilter {
  private FilteredColumn filteredColumn;
  private boolean isAsc;

  public SortFilter() {
    this(null, true);
  }

  public SortFilter(FilteredColumn filteredColumn, boolean isAsc) {
    this.filteredColumn = filteredColumn;
    this.isAsc = isAsc;
  }

  public static SortFilter asc(FilteredColumn filteredColumn) {
    return new SortFilter(filteredColumn, true);
  }

  public static SortFilter desc(FilteredColumn filteredColumn) {
    return new SortFilter(filteredColumn, false);
  }

  public FilteredColumn getFilteredColumn() {
    return filteredColumn;
  }

  public SortFilter setFilteredColumn(FilteredColumn filteredColumn) {
    this.filteredColumn = filteredColumn;
    return this;
  }

  public boolean isAsc() {
    return isAsc;
  }

  public SortFilter setAsc(boolean asc) {
    isAsc = asc;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SortFilter that = (SortFilter) o;
    return isAsc == that.isAsc &&
        filteredColumn == that.filteredColumn;
  }

  @Override
  public int hashCode() {
    return Objects.hash(filteredColumn, isAsc);
  }
}
