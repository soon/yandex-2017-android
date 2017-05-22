package com.awesoon.thirdtask.util;

import java.util.Random;

public class FastRandom {
  private long x;
  private long y;
  private long z;

  public FastRandom() {
    Random rnd = new Random();
    x = rnd.nextLong();
    y = rnd.nextLong();
    z = rnd.nextLong();
  }

  public long nextLong() {
    long t;
    x ^= x << 16;
    x ^= x >> 5;
    x ^= x << 1;

    t = x;
    x = y;
    y = z;
    z = t ^ x ^ y;

    return Math.abs(z);
  }

  public int nextInt(int start, int endExclusive) {
    Assert.isTrue(endExclusive > start, "endExclusive should be greater than start");

    long l = nextLong();
    int d = (int) (l % (endExclusive - start));
    return start + d;
  }

  public int nextInt(int endExclusive) {
    return nextInt(0, endExclusive);
  }
}
