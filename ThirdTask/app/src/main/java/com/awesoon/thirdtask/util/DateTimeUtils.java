package com.awesoon.thirdtask.util;

import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

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

  public static DateTime parseDateTime(String dateTime) {
    return parseDateTime(dateTime, DateTimeZone.getDefault());
  }

  public static DateTime parseDateTime(String dateTime, DateTimeZone timeZone) {
    DateTimeFormatter dateTimeFormatter = hasMillis(dateTime)
        ? ISODateTimeFormat.dateTime()
        : ISODateTimeFormat.dateTimeNoMillis();

    return dateTimeFormatter.parseDateTime(dateTime).toDateTime(timeZone);
  }

  private static boolean hasMillis(String data) {
    // checks whether the data has yyyy-MM-dd'T'HH:mm:ss.SSSZZ format or yyyy-MM-dd'T'HH:mm:ssZZ (note the .SSS part)
    return data.contains(".");
  }
}
