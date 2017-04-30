package com.awesoon.thirdtask.repository.filter;

import java.util.List;
import java.util.Set;

public class SysItemFilter {
  private DatePeriodFilter createdDateFilter;
  private DatePeriodFilter lastEditedDateFilter;
  private DatePeriodFilter lastViewedDateFilter;
  private Set<Integer> colors;
  private List<SortFilter> sorts;

  public DatePeriodFilter getCreatedTimeFilter() {
    return createdDateFilter;
  }

  public SysItemFilter setCreatedDateFilter(DatePeriodFilter createdDateFilter) {
    this.createdDateFilter = createdDateFilter;
    return this;
  }

  public DatePeriodFilter getLastEditedTimeFilter() {
    return lastEditedDateFilter;
  }

  public SysItemFilter setLastEditedDateFilter(DatePeriodFilter lastEditedDateFilter) {
    this.lastEditedDateFilter = lastEditedDateFilter;
    return this;
  }

  public DatePeriodFilter getLastViewedTimeFilter() {
    return lastViewedDateFilter;
  }

  public SysItemFilter setLastViewedDateFilter(DatePeriodFilter lastViewedDateFilter) {
    this.lastViewedDateFilter = lastViewedDateFilter;
    return this;
  }

  public Set<Integer> getColors() {
    return colors;
  }

  public SysItemFilter setColors(Set<Integer> colors) {
    this.colors = colors;
    return this;
  }

  public List<SortFilter> getSorts() {
    return sorts;
  }

  public SysItemFilter setSorts(List<SortFilter> sorts) {
    this.sorts = sorts;
    return this;
  }
}
