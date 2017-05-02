package com.awesoon.thirdtask.util;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RowMapperAdapterTest {
  @Mock
  Cursor cursor;

  @Mock
  Context context;

  @Mock
  Context appContext;

  @Mock
  Resources resources;

  @Before
  public void setUp() throws Exception {
    when(context.getApplicationContext()).thenReturn(appContext);
    when(appContext.getResources()).thenReturn(resources);
    doReturn(new ByteArrayInputStream(new byte[1024])).when(resources).openRawResource(anyInt());
    JodaTimeAndroid.init(context);
  }

  @Test
  public void testGetDateTime() throws Exception {
    // given
    when(cursor.getColumnIndexOrThrow("DATE")).thenReturn(1);
    when(cursor.getString(1)).thenReturn("2017-04-24T12:00:00+00:00");

    DateTimeZone.setDefault(DateTimeZone.forID("+05"));

    RowMapperAdapter<DateTime> mapper = new RowMapperAdapter<DateTime>() {
      @Override
      public DateTime mapRow(Cursor cursor, int rowNumber) {
        return getDateTime(cursor, "DATE");
      }
    };

    // when
    DateTime dateTime = mapper.mapRow(cursor, 0);

    // then
    assertThat(dateTime, is(ISODateTimeFormat.dateTimeNoMillis().parseDateTime("2017-04-24T17:00:00+05:00")));
  }

  @Test
  public void testGetDateTimeUtc() throws Exception {
    // given
    when(cursor.getColumnIndexOrThrow("DATE")).thenReturn(1);
    when(cursor.getString(1)).thenReturn("2017-04-24T12:00:00+00:00");

    DateTimeZone.setDefault(DateTimeZone.forID("+05"));

    RowMapperAdapter<DateTime> mapper = new RowMapperAdapter<DateTime>() {
      @Override
      public DateTime mapRow(Cursor cursor, int rowNumber) {
        return getDateTime(cursor, "DATE", DateTimeZone.forID("+03"));
      }
    };

    // when
    DateTime dateTime = mapper.mapRow(cursor, 0);

    // then
    assertThat(dateTime,
        is(ISODateTimeFormat.dateTimeNoMillis()
            .parseDateTime("2017-04-24T15:00:00+03:00")
            .toDateTime(DateTimeZone.forID("+03"))));
  }

  @Test
  public void testGetDateTimeWithTimeZone() throws Exception {
    // given
    when(cursor.getColumnIndexOrThrow("DATE")).thenReturn(1);
    when(cursor.getString(1)).thenReturn("2017-04-24T12:00:00+03:00");

    DateTimeZone.setDefault(DateTimeZone.forID("+05"));

    RowMapperAdapter<DateTime> mapper = new RowMapperAdapter<DateTime>() {
      @Override
      public DateTime mapRow(Cursor cursor, int rowNumber) {
        return getDateTime(cursor, "DATE");
      }
    };

    // when
    DateTime dateTime = mapper.mapRow(cursor, 0);

    // then
    assertThat(dateTime, is(ISODateTimeFormat.dateTimeNoMillis().parseDateTime("2017-04-24T12:00:00+03:00")));
  }

  @Test
  public void testGetDateTimeWithMills() throws Exception {
    // given
    when(cursor.getColumnIndexOrThrow("DATE")).thenReturn(1);
    when(cursor.getString(1)).thenReturn("2017-04-24T12:00:00.123+00:00");

    DateTimeZone.setDefault(DateTimeZone.forID("+05"));

    RowMapperAdapter<DateTime> mapper = new RowMapperAdapter<DateTime>() {
      @Override
      public DateTime mapRow(Cursor cursor, int rowNumber) {
        return getDateTime(cursor, "DATE");
      }
    };

    // when
    DateTime dateTime = mapper.mapRow(cursor, 0);

    // then
    assertThat(dateTime, is(ISODateTimeFormat.dateTime().parseDateTime("2017-04-24T17:00:00.123+05:00")));
  }
}