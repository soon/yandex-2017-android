package com.awesoon.thirdtask.util;

import java.util.Random;

public final class BeautifulColors {
  public static final int[] BEAUTIFUL_COLORS = new int[]{
      0xFFF44336,
      0xFFE91E63,
      0xFF9C27B0,
      0xFF673AB7,
      0xFF3F51B5,
      0xFF2196F3,
      0xFF03A9F4,
      0xFF00BCD4,
      0xFF009688,
      0xFF4CAF50,
      0xFF8BC34A,
      0xFFCDDC39,
      0xFFFFEB3B,
      0xFFFFC107,
      0xFFFF9800,
      0xFFFF5722
  };

  private static final Random rnd = new Random();

  private BeautifulColors() {
  }

  /**
   * Retrieves random color.
   *
   * @return A color.
   */
  public static int getBeautifulColor() {
    int idx = rnd.nextInt(BEAUTIFUL_COLORS.length);
    return BEAUTIFUL_COLORS[idx];
  }
}
