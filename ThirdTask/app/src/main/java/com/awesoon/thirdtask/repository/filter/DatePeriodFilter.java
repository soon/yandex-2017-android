package com.awesoon.thirdtask.repository.filter;

import org.joda.time.DateTime;

public class DatePeriodFilter {
  private DateTime from;
  private DateTime to;

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
