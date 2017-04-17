package com.awesoon.secondtask.util;

public final class MathUtil {
  private MathUtil() {
  }

  /**
   * Ensures that the given value is not less than the min value and not greater than the max value.
   *
   * @param min   The min bound.
   * @param value The current value.
   * @param max   The max bound.
   * @return Min bound, if the value is less than the min value.
   * Max bound, if the value is greater than the max value.
   * Current value otherwise.
   */
  public static float fitToBounds(float min, float value, float max) {
    return Math.max(min, Math.min(value, max));
  }
}
