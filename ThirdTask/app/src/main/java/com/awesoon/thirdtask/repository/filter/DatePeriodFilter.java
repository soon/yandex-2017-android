package com.awesoon.thirdtask.repository.filter;

import android.support.annotation.Nullable;

import org.joda.time.DateTime;

import java.util.Objects;

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
    return Objects.equals(from, that.from) &&
        Objects.equals(to, that.to);
  }

  @Override
  public int hashCode() {
    return Objects.hash(from, to);
  }
}
