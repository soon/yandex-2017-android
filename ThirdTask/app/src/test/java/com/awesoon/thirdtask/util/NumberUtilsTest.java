package com.awesoon.thirdtask.util;

import org.junit.Test;

import static com.awesoon.thirdtask.util.NumberUtils.makeShortString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class NumberUtilsTest {
  @Test
  public void testMakeShortString() throws Exception {
    assertThat(makeShortString(100), is("100"));
    assertThat(makeShortString(999), is("999"));
    assertThat(makeShortString(1_000), is("1K"));
    assertThat(makeShortString(1_500), is("1.5K"));
    assertThat(makeShortString(1_949), is("1.9K"));
    assertThat(makeShortString(1_950), is("1.9K"));
    assertThat(makeShortString(1_999), is("2K"));
    assertThat(makeShortString(2_000), is("2K"));
    assertThat(makeShortString(1_300_000), is("1.3M"));
    assertThat(makeShortString(2_000_000), is("2M"));
  }
}