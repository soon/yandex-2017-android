package com.awesoon.secondtask.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.awesoon.secondtask.event.ColorChangeListener;

import java.util.ArrayList;
import java.util.List;

public class ColorPickerButton extends android.support.v7.widget.AppCompatButton {
  private int color;
  private List<ColorChangeListener> colorChangeListeners = new ArrayList<>();

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
    setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        notifyColorChanged();
      }
    });
  }

  /**
   * Notifies all listeners about changed color.
   */
  private void notifyColorChanged() {
    for (ColorChangeListener listener : colorChangeListeners) {
      listener.onColorChanged(color);
    }
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
   * Changes current button color.
   *
   * @param color A new color.
   */
  public void setColor(int color) {
    this.color = color;
    setBackgroundColor(color);
  }

  /**
   * Adds color change listener.
   *
   * @param listener A listener.
   */
  public void setOnColorChangeListener(ColorChangeListener listener) {
    colorChangeListeners.add(listener);
  }
}
