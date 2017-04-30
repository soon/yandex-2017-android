package com.awesoon.thirdtask.view;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.awesoon.thirdtask.R;
import com.awesoon.thirdtask.util.Assert;

import java.util.Locale;

public class ColorPickerInfo extends LinearLayout {
  private Integer color;

  public ColorPickerInfo(Context context) {
    super(context);
  }

  public ColorPickerInfo(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public ColorPickerInfo(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public ColorPickerInfo(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  /**
   * Updates the info view with the given color.
   *
   * @param color A new color.
   */
  public void setColor(Integer color) {
    this.color = color;

    TextView currentColorRgbText = (TextView) findViewById(R.id.currentColorRgbText);
    TextView currentColorHsvText = (TextView) findViewById(R.id.currentColorHsvText);
    CurrentColorView currentColorView = getCurrentColorView();

    if (color == null) {
      currentColorRgbText.setText("");
      currentColorHsvText.setText("");
      currentColorView.setColor(Color.TRANSPARENT);
    } else {
      currentColorRgbText.setText(formatToRgb(color));
      currentColorHsvText.setText(formatToHsv(color));
      currentColorView.setColor(color);
    }
  }

  /**
   * Retrieves current color view.
   *
   * @return Current color view.
   */
  private CurrentColorView getCurrentColorView() {
    CurrentColorView currentColorView = (CurrentColorView) findViewById(R.id.currentColorBlock);
    Assert.notNull(currentColorView, "currentColorView");
    return currentColorView;
  }

  /**
   * Retrieves current color.
   *
   * @return Current color, if present, null otherwise.
   */
  public Integer getColor() {
    return color;
  }

  /**
   * Formats given color to a HSV string.
   *
   * @param color A color.
   * @return A color formatted as HSV.
   */
  private String formatToHsv(int color) {
    float[] hsv = new float[3];
    Color.colorToHSV(color, hsv);
    return String.format(Locale.ROOT, "H: %.2f, S: %.2f, V: %.2f", hsv[0], hsv[1], hsv[2]);
  }

  /**
   * Formats given color to a RGB string.
   *
   * @param color A color.
   * @return A color formatter as RGB.
   */
  private String formatToRgb(int color) {
    return "RGB: #" + Integer.toHexString(color);
  }
}
