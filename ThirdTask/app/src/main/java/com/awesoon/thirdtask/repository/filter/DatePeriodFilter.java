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
}
