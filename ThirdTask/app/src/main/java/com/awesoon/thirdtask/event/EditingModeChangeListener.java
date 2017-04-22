package com.awesoon.thirdtask.event;

public interface EditingModeChangeListener {
  /**
   * Called just before the button wants to change the editing mode.
   * The button changes mode only when all listeners returns true.
   * If any of the listeners returns false, the other listeners will not be notified.
   *
   * @return true, if button can enter editing mode, false otherwise.
   */
  boolean onBeforeEnteringEditingMode();

  /**
   * Called when button enters editing mode.
   *
   * @param color Current button color.
   */
  void onEnterEditingMode(int color);

  /**
   * Called when button exits editing mode.
   */
  void onExitEditingMode();
}
