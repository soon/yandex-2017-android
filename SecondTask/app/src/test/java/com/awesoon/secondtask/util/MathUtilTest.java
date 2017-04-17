package com.awesoon.secondtask.util;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class MathUtilTest {
  @Test
  public void fitToBounds() throws Exception {
    assertThat(MathUtil.fitToBounds(0, -1, 3), is(0f));
    assertThat(MathUtil.fitToBounds(0, 0, 3), is(0f));
    assertThat(MathUtil.fitToBounds(0, 1, 3), is(1f));
    assertThat(MathUtil.fitToBounds(0, 2, 3), is(2f));
    assertThat(MathUtil.fitToBounds(0, 3, 3), is(3f));
    assertThat(MathUtil.fitToBounds(0, 4, 3), is(3f));
  }
}