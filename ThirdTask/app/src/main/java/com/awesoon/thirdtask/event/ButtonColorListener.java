package com.awesoon.thirdtask.event;

public interface ButtonColorListener {
  /**
   * Called just before the button wants to select the new color.
   * The button changes color only when all listeners returns true.
   * If any of the listeners returns false, the other listeners will not be notified.
   *
   * @return true, if button can select color, false otherwise.
   */
  boolean onBeforeColorSelected(int newColor);

  /**
   * Called when the button changes it's color.
   * @param newColor A new button color.
   */
  void onColorChanged(int newColor);

  /**
   * Called when button selects a new color.
   * @param newColor A selected color.
   */
  void onColorSelected(int newColor);
}
