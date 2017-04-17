package com.awesoon.secondtask.view;

import android.content.Context;
import android.graphics.Color;
import android.os.Vibrator;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.awesoon.secondtask.event.ButtonColorListener;
import com.awesoon.secondtask.event.EditingModeChangeListener;
import com.awesoon.secondtask.util.MathUtil;

import java.util.ArrayList;
import java.util.List;

public class ColorPickerButton extends AppCompatButton {
  private static final String TAG = "ColorPickerButton";
  public static final int VIBRATION_MS = 100;
  public static final float HUE_CHANGE_SPEED = 1.f / 20;
  public static final float BRIGHTNESS_CHANGE_SPEED = 1.f / 200;
  public static final int DOUBLE_TAP_MS_DELTA = 200;
  public static final int MIN_VIBRATION_DELTA_MS = 1000;

  private Integer color;
  private int defaultColor;
  private float hueMinValue;
  private float hueMaxValue;
  private float[] hsv = new float[3];
  private List<ButtonColorListener> colorChangeListeners = new ArrayList<>();
  private List<EditingModeChangeListener> editingModeChangeListeners = new ArrayList<>();
  private boolean isEditingMode = false;
  private float prevMotionX = -1;
  private float prevMotionY = -1;
  private long lastTouchTime = -1;
  private long lastVibrationMills = -1;

  public ColorPickerButton(Context context) {
    super(context);
    init();
  }

  public ColorPickerButton(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public ColorPickerButton(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {
    setLongClickable(true);
    setOnLongClickListener(new OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        return enterEditingMode();
      }
    });
    setOnTouchListener(new OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        float currX = event.getRawX();
        float currY = event.getRawY();
        float prevX = currX;
        float prevY = currY;

        if (prevMotionX > 0 && prevMotionY > 0) {
          prevX = prevMotionX;
          prevY = prevMotionY;
        }

        prevMotionX = currX;
        prevMotionY = currY;

        float dx = currX - prevX;
        float dy = currY - prevY;

        return doHandleOnTouch(event, dx, dy);
      }
    });
  }

  /**
   * Handles touch event.
   *
   * @param event Event.
   * @param dx    Delta x.
   * @param dy    Delta y.
   * @return Whether the event was handled.
   */
  private boolean doHandleOnTouch(MotionEvent event, float dx, float dy) {
    int action = event.getActionMasked();
    switch (action) {
      case MotionEvent.ACTION_DOWN:
        long currentTime = System.currentTimeMillis();
        if (isDoubleTap(currentTime)) {
          lastTouchTime = -1;
          restoreDefaultColor();
        } else {
          lastTouchTime = currentTime;
        }
        return false;

      case MotionEvent.ACTION_MOVE:
        if (isEditingMode) {
          moveColor(dx, dy);
        }
        return true;

      case MotionEvent.ACTION_OUTSIDE:
        if (isEditingMode) {
          exitEditingMode();
        }
        return false;

      case MotionEvent.ACTION_UP:
        if (isEditingMode) {
          exitEditingMode();
        } else {
          if (notifyBeforeColorSelected()) {
            notifyColorSelected();
          }
        }
        prevMotionX = -1;
        prevMotionY = -1;
        return false;
    }

    return false;
  }

  /**
   * Checks if the button was pressed twice.
   *
   * @param currentTime Current time in milliseconds.
   * @return Whether the button was pressed twice.
   */
  private boolean isDoubleTap(long currentTime) {
    return currentTime - lastTouchTime < DOUBLE_TAP_MS_DELTA;
  }

  /**
   * Reverts current color to default value.
   */
  private void restoreDefaultColor() {
    setColor(defaultColor);
  }

  /**
   * Retrieves current button color.
   *
   * @return Current color.
   */
  public int getColor() {
    return color;
  }

  /**
   * Moves the color according to the given deltas.
   *
   * @param dx Delta x.
   * @param dy Delta y.
   */
  public void moveColor(float dx, float dy) {
    if (dx == 0 && dy == 0) {
      return;
    }

    float dh = dx * HUE_CHANGE_SPEED;
    float db = dy * BRIGHTNESS_CHANGE_SPEED;

    float d = Math.abs(dx / dy);

    if (d > 0.5) {
      hsv[0] += dh;
      hsv[0] = MathUtil.fitToBounds(hueMinValue, hsv[0], hueMaxValue);
    }
    if (d < 1.5) {
      hsv[2] += db;
      hsv[2] = MathUtil.fitToBounds(0, hsv[2], 1);
    }

    if (hsv[0] == hueMinValue || hsv[0] == hueMaxValue || hsv[2] == 0 || hsv[2] == 1) {
      performVibration();
    }

    color = Color.HSVToColor(hsv);
    setBackgroundColor(color);
    notifyColorChanged();
  }

  /**
   * Sets current button color.
   *
   * @param color A new color.
   */
  public void setColor(int color) {
    this.color = color;
    Color.colorToHSV(color, this.hsv);
    setBackgroundColor(color);
    notifyColorChanged();
  }

  /**
   * Sets current color if current color is null.
   *
   * @param color A new color.
   */
  public void setColorIfAbsent(int color) {
    if (this.color == null) {
      setColor(color);
    }
  }

  /**
   * Changes default button color.
   *
   * @param defaultColor A new default color.
   */
  public void setDefaultColor(int defaultColor) {
    this.defaultColor = defaultColor;
  }

  /**
   * Retrieves default color.
   *
   * @return Default color.
   */
  public int getDefaultColor() {
    return defaultColor;
  }

  /**
   * Enters the editing mode.
   *
   * @return true if the button enters editing mode, false otherwise.
   */
  public boolean enterEditingMode() {
    if (!notifyBeforeEnterEditingMode()) {
      return false;
    }
    isEditingMode = true;
    forceVibrate();
    notifyEnterEditingMode();
    return true;
  }

  /**
   * Exits the editing mode.
   */
  public void exitEditingMode() {
    isEditingMode = false;
    forceVibrate();
    notifyExitEditingMode();
  }

  /**
   * Adds color change listener.
   *
   * @param listener A listener.
   */
  public void setOnColorChangeListener(ButtonColorListener listener) {
    colorChangeListeners.add(listener);
  }

  /**
   * Adds editing mode change listener.
   *
   * @param listener A listener.
   */
  public void setOnEditingModeChangeListener(EditingModeChangeListener listener) {
    editingModeChangeListeners.add(listener);
  }

  /**
   * Retrieves Hue max value.
   *
   * @return Hue max value.
   */
  public float getHueMaxValue() {
    return hueMaxValue;
  }

  /**
   * Sets the Hue max value.
   *
   * @param hueMaxValue new Hue max value.
   */
  public void setHueMaxValue(float hueMaxValue) {
    this.hueMaxValue = hueMaxValue;
  }

  /**
   * Retrieves Hue min value.
   *
   * @return Hue min value.
   */
  public float getHueMinValue() {
    return hueMinValue;
  }

  /**
   * Sets Hue max value.
   *
   * @param hueMinValue new Hue max value.
   */
  public void setHueMinValue(float hueMinValue) {
    this.hueMinValue = hueMinValue;
  }

  /**
   * Vibrates the device.
   */
  private void performVibration() {
    long currentTime = System.currentTimeMillis();
    if (currentTime - lastVibrationMills > MIN_VIBRATION_DELTA_MS) {
      forceVibrate();
      lastVibrationMills = currentTime;
    }
  }

  /**
   * Vibrates device even if it was vibrating recently.
   */
  private void forceVibrate() {
    try {
      Vibrator v = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
      v.vibrate(VIBRATION_MS);
    } catch (Exception e) {
      Log.e(TAG, "Unable to vibrate device", e);
    }
  }

  /**
   * Notifies about button color changed.
   */
  private void notifyColorChanged() {
    for (ButtonColorListener listener : colorChangeListeners) {
      listener.onColorChanged(color);
    }
  }

  /**
   * Notified all listeners about color selection.
   *
   * @return true, if the button can select the color, false otherwise.
   */
  private boolean notifyBeforeColorSelected() {
    for (ButtonColorListener listener : colorChangeListeners) {
      if (!listener.onBeforeColorSelected(color)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Notifies all listeners about selected color.
   */
  private void notifyColorSelected() {
    for (ButtonColorListener listener : colorChangeListeners) {
      listener.onColorSelected(color);
    }
  }

  /**
   * Notifies all listeners that button wants to enter the editing mode.
   *
   * @return true, if button is allowed to change the editing mode, false, otherwise.
   */
  private boolean notifyBeforeEnterEditingMode() {
    for (EditingModeChangeListener listener : editingModeChangeListeners) {
      if (!listener.onBeforeEnteringEditingMode()) {
        return false;
      }
    }

    return true;
  }

  /**
   * Notifies all listeners about entering editing mode.
   */
  private void notifyEnterEditingMode() {
    for (EditingModeChangeListener listener : editingModeChangeListeners) {
      listener.onEnterEditingMode(color);
    }
  }

  /**
   * Notifies all listeners about exiting editing mode.
   */
  private void notifyExitEditingMode() {
    for (EditingModeChangeListener listener : editingModeChangeListeners) {
      listener.onExitEditingMode();
    }
  }
}
