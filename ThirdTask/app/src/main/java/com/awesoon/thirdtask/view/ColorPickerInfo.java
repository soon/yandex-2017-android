package com.awesoon.thirdtask.view;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.awesoon.thirdtask.R;
import com.awesoon.thirdtask.event.ButtonColorListenerAdapter;
import com.awesoon.thirdtask.util.Assert;

import java.util.Locale;

public class ColorPickerInfo extends LinearLayout {
  private Integer color;
  private TextView currentColorRgbText;
  private TextView currentColorHsvText;
  private ColorPickerButton currentColorButton;

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

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();

    currentColorRgbText = (TextView) findViewById(R.id.currentColorRgbText);
    currentColorHsvText = (TextView) findViewById(R.id.currentColorHsvText);
    currentColorButton = getCurrentColorView();

    currentColorButton.setOnColorChangeListener(new ButtonColorListenerAdapter() {
      @Override
      public void onColorChanged(int newColor) {
        ColorPickerInfo.this.color = newColor;
        updateTextColorInfo(newColor);
      }
    });
  }

  /**
   * Updates the info view with the given color.
   *
   * @param color A new color.
   */
  public void setColor(Integer color) {
    this.color = color;

    if (color == null) {
      currentColorButton.setDefaultColor(Color.TRANSPARENT);
      currentColorButton.setColorAnimated(Color.TRANSPARENT);
      currentColorButton.setHueMinValue(ColorPickerButton.MIN_HUE_VALUE);
      currentColorButton.setHueMaxValue(ColorPickerButton.MAX_HUE_VALUE);
    } else {
      currentColorButton.setDefaultColor(color);
      currentColorButton.setColorAnimated(color);

      float[] hsv = new float[3];
      Color.colorToHSV(color, hsv);
      // e.g. the TOTAL_BUTTONS_COUNT is 3
      // [-b--b--b-], where b is a button, - is a button delta, [ is the left bound and ] is the right bound
      // so, the total number of deltas is 2*N
      float delta = ColorPickerButton.MAX_HUE_VALUE / (ColorPickerView.TOTAL_BUTTONS_COUNT * 2);
      currentColorButton.setHueMinValue(hsv[0] - delta / 2);
      currentColorButton.setHueMaxValue(hsv[0] + delta / 2);
    }
  }

  private void updateTextColorInfo(int color) {
    if (color == Color.TRANSPARENT) {
      currentColorRgbText.setText("");
      currentColorHsvText.setText("");
    } else {
      currentColorRgbText.setText(formatToRgb(color));
      currentColorHsvText.setText(formatToHsv(color));
    }
  }

  /**
   * Retrieves current color view.
   *
   * @return Current color view.
   */
  private ColorPickerButton getCurrentColorView() {
    ColorPickerButton currentColorView =  (ColorPickerButton) findViewById(R.id.currentColorBlock);
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
