package com.awesoon.thirdtask.domain;

import org.joda.time.DateTime;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class SysItemTest {
  @Test
  public void testParseJson() throws Exception {
    // given
    // when
    SysItem sysItem = SysItem.parseJson(
        "{" +
            "\"title\":\"title\", " +
            "\"description\":\"description\", " +
            "\"color\":\"#123456\", " +
            "\"created\":\"2017-04-24T12:00:00+05:00\", " +
            "\"edited\":\"2017-04-23T12:00:00+05:00\", " +
            "\"viewed\":\"2017-04-22T12:00:00+05:00\"" +
            "}");

    // then
    assertThat(sysItem.getTitle(), is("title"));
    assertThat(sysItem.getBody(), is("description"));
    assertThat(sysItem.getColor(), is(0x123456));
    assertThat(sysItem.getCreatedTime(), is(new DateTime("2017-04-24T12:00:00+05:00")));
    assertThat(sysItem.getLastEditedTime(), is(new DateTime("2017-04-23T12:00:00+05:00")));
    assertThat(sysItem.getLastViewedTime(), is(new DateTime("2017-04-22T12:00:00+05:00")));
  }

  @Test
  public void testParseJsonExtraFields() throws Exception {
    // given
    // when
    SysItem sysItem = SysItem.parseJson(
        "{" +
            "\"extraTitle\":\"title\", " +
            "\"title\":\"title\", " +
            "\"description\":\"description\", " +
            "\"color\":\"#123456\", " +
            "\"created\":\"2017-04-24T12:00:00+05:00\", " +
            "\"edited\":\"2017-04-23T12:00:00+05:00\", " +
            "\"viewed\":\"2017-04-22T12:00:00+05:00\"" +
            "}");

    // then
    assertThat(sysItem.getTitle(), is("title"));
    assertThat(sysItem.getBody(), is("description"));
    assertThat(sysItem.getColor(), is(0x123456));
    assertThat(sysItem.getCreatedTime(), is(new DateTime("2017-04-24T12:00:00+05:00")));
    assertThat(sysItem.getLastEditedTime(), is(new DateTime("2017-04-23T12:00:00+05:00")));
    assertThat(sysItem.getLastViewedTime(), is(new DateTime("2017-04-22T12:00:00+05:00")));
  }

  @Test
  public void testParseJsonMissedFields() throws Exception {
    // given
    // when
    SysItem sysItem = SysItem.parseJson("{}");

    // then
    assertThat(sysItem.getTitle(), is(nullValue()));
    assertThat(sysItem.getBody(), is(nullValue()));
    assertThat(sysItem.getColor(), is(0));
    assertThat(sysItem.getCreatedTime(), is(nullValue()));
    assertThat(sysItem.getLastEditedTime(), is(nullValue()));
    assertThat(sysItem.getLastViewedTime(), is(nullValue()));
  }

  @Test
  public void testToJson() throws Exception {
    // given
    SysItem sysItem = new SysItem()
        .setTitle("title")
        .setBody("body")
        .setColor(123)
        .setCreatedTime(new DateTime("2017-04-24T12:00:00+05:00"))
        .setLastEditedTime(new DateTime("2017-04-23T12:00:00+05:00"))
        .setLastViewedTime(new DateTime("2017-04-22T12:00:00+05:00"));

    // when
    String json = sysItem.toJson();
    SysItem parsed = SysItem.parseJson(json);

    // then
    assertThat(sysItem.getTitle(), is(parsed.getTitle()));
    assertThat(sysItem.getBody(), is(parsed.getBody()));
    assertThat(sysItem.getColor(), is(parsed.getColor()));
    assertThat(sysItem.getCreatedTime(), is(parsed.getCreatedTime()));
    assertThat(sysItem.getLastEditedTime(), is(parsed.getLastEditedTime()));
    assertThat(sysItem.getLastViewedTime(), is(parsed.getLastViewedTime()));
  }
}