package com.awesoon.firsttask.util;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class LogTest {
  @Before
  public void setUp() throws Exception {
    Log.getGlobalLog().clear();
  }

  @Test
  public void testGlobalLog() throws Exception {
    // given
    Log l1 = new Log();
    Log l2 = new Log();
    Log l3 = new Log();

    // when
    l1.addEntry(1, "TAG1", "MSG1");
    l2.addEntry(2, "TAG2", "MSG2");
    l3.addEntry(3, "TAG3", "MSG3");
    l3.addEntry(4, "TAG31", "MSG31");
    l2.addEntry(5, "TAG21", "MSG21");
    l1.addEntry(6, "TAG11", "MSG11");

    // then
    Log globalLog = Log.getGlobalLog();
    assertThat(globalLog.size(), is(6));
    assertThat(globalLog.get(0), is(l1.get(0)));
    assertThat(globalLog.get(1), is(l2.get(0)));
    assertThat(globalLog.get(2), is(l3.get(0)));
    assertThat(globalLog.get(3), is(l3.get(1)));
    assertThat(globalLog.get(4), is(l2.get(1)));
    assertThat(globalLog.get(5), is(l1.get(1)));
  }
}