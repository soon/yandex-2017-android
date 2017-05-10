package com.awesoon.thirdtask.repository;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SysItemsContainerTest {
  @Test
  public void testParseJson() throws Exception {
    // given
    String json = "{\"notes\": [{" +
        "  \"extraTitle\":\"title\", " +
        "  \"title\":\"title\", " +
        "  \"description\":\"description\", " +
        "  \"color\":\"#123456\", " +
        "  \"created\":\"2017-04-24T12:00:00+05:00\", " +
        "  \"edited\":\"2017-04-23T12:00:00+05:00\", " +
        "  \"viewed\":\"2017-04-22T12:00:00+05:00\"" +
        "  }]" +
        "}";

    // when
    SysItemsContainer container = SysItemsContainer.parseJson(json);

    // then
    assertThat(container.getNotes().size(), is(1));
  }

  @Test
  public void testParseJson__color() throws Exception {
    // given
    String json = "{\"notes\":[{" +
        "  \"color\":\"#ffffc107\"," +
        "  \"created\":\"2017-05-02T17:10:40.577+05:00\"," +
        "  \"description\":\"ngngjtjt\"," +
        "  \"edited\":\"2017-05-02T17:10:40.681+05:00\"," +
        "  \"id\":8," +
        "  \"title\":\"fhfjrj\"," +
        "  \"viewed\":\"2017-05-02T17:10:40.681+05:00\"}]}";

    // when
    SysItemsContainer container = SysItemsContainer.parseJson(json);

    // then
    assertThat(container.getNotes().size(), is(1));
  }
}