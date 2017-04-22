package com.awesoon.thirdtask.event;

public interface ColorChangeListener {
  /**
   * Called when the color is changed.
   *
   * @param newColor A new color.
   */
  void onColorChanged(int newColor);
}
