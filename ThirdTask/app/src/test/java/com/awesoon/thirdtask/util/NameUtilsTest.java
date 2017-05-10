package com.awesoon.thirdtask.util;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import java.util.ArrayList;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class NameUtilsTest {
  @Test
  public void testCreateUniqueName() throws Exception {
    assertThat(NameUtils.createUniqueName(" current name  ", ImmutableList.of(
        "current name (-1)",
        "current name (none)",
        "current name ()",
        "current name",
        "another name",
        "",
        "current name (9999999999999999999999999999999999)",
        "current name (42)"
    )), is("current name (10000000000000000000000000000000000)"));
  }

  @Test
  public void testCreateUniqueName__nullNames() {
    ArrayList<String> nullValues = new ArrayList<>();
    nullValues.add(null);
    assertThat(NameUtils.createUniqueName(" current name  ", nullValues), is("current name"));
  }

  @Test
  public void testCreateUniqueName__emptyList() {
    assertThat(NameUtils.createUniqueName(" current name  ", ImmutableList.<String>of()),
        is("current name"));
  }
}