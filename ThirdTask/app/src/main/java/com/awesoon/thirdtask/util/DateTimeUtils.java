package com.awesoon.thirdtask.util;

import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;

public final class DateTimeUtils {
  public static boolean isBeforeDateOnly(DateTime t1, DateTime t2) {
    Assert.notNull(t1, "t1 must not be null");
    Assert.notNull(t2, "t2 must not be null");

    DateTimeComparator comparator = DateTimeComparator.getDateOnlyInstance();
    return comparator.compare(t1, t2) < 0;
  }

  public static boolean isAfterDateOnly(DateTime t1, DateTime t2) {
    Assert.notNull(t1, "t1 must not be null");
    Assert.notNull(t2, "t2 must not be null");

    DateTimeComparator comparator = DateTimeComparator.getDateOnlyInstance();
    return comparator.compare(t1, t2) > 0;
  }
}
