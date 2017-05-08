package com.awesoon.thirdtask.util;

import org.junit.Test;

import static com.awesoon.thirdtask.util.StringUtils.isBlank;
import static com.awesoon.thirdtask.util.StringUtils.makeEmptyIfNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class StringUtilsTest {
  @Test
  public void testMakeEmptyIfNull() throws Exception {
    assertThat(makeEmptyIfNull(null), is(""));
    assertThat(makeEmptyIfNull(""), is(""));
    assertThat(makeEmptyIfNull(" "), is(" "));
    assertThat(makeEmptyIfNull("any text"), is("any text"));
  }

  @Test
  public void testIsBlank() throws Exception {
    assertThat(isBlank(null), is(true));
    assertThat(isBlank(""), is(true));
    assertThat(isBlank("   "), is(true));
    assertThat(isBlank(" \t "), is(true));
    assertThat(isBlank(" \t\n "), is(true));
    assertThat(isBlank(" \t\n "), is(true));
    assertThat(isBlank(" \nany text\t "), is(false));
    assertThat(isBlank("a"), is(false));
  }
}