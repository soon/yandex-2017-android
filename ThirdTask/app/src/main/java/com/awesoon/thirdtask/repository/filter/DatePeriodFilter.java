package com.awesoon.thirdtask.repository.filter;

import android.support.annotation.Nullable;

import org.joda.time.DateTime;

public class DatePeriodFilter {
  private DateTime from;
  private DateTime to;

  public DatePeriodFilter() {
  }

  public DatePeriodFilter(@Nullable DateTime from, @Nullable DateTime to) {
    this.from = from;
    this.to = to;
  }

  public DateTime getFrom() {
    return from;
  }

  public DatePeriodFilter setFrom(DateTime from) {
    this.from = from;
    return this;
  }

  public DateTime getTo() {
    return to;
  }

  public DatePeriodFilter setTo(DateTime to) {
    this.to = to;
    return this;
  }

  public boolean isEmpty() {
    return from == null && to == null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    DatePeriodFilter that = (DatePeriodFilter) o;

    if (from != null ? !from.equals(that.from) : that.from != null) {
      return false;
    }
    return to != null ? to.equals(that.to) : that.to == null;

  }

  @Override
  public int hashCode() {
    int result = from != null ? from.hashCode() : 0;
    result = 31 * result + (to != null ? to.hashCode() : 0);
    return result;
  }
}
