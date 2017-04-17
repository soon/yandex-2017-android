package com.awesoon.secondtask.view;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.awesoon.secondtask.R;
import com.awesoon.secondtask.domain.FavoriteColor;
import com.awesoon.secondtask.event.FavoriteColorListener;
import com.awesoon.secondtask.util.Assert;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ColorPickerInfo extends LinearLayout {
  private Integer color;
  private Set<Integer> favoriteColors = new HashSet<>();
  private List<FavoriteColorListener> listeners = new ArrayList<>();

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

    CurrentColorView currentColorView = getCurrentColorView();
    currentColorView.setOnFavoriteColorListener(new FavoriteColorListener() {
      @Override
      public void addToFavorites(int color) {
        favoriteColors.add(color);
        notifyFavoriteChanged(color, true);
      }

      @Override
      public void removeFromFavorites(int color) {
        favoriteColors.remove(color);
        notifyFavoriteChanged(color, false);
      }
    });
  }

  /**
   * Adds new favorite listener.
   *
   * @param listener A listener.
   */
  public void setOnFavoriteColorListener(FavoriteColorListener listener) {
    listeners.add(listener);
  }

  /**
   * Notifies all listeners about changed favorite color.
   *
   * @param color      A color.
   * @param isFavorite When the color becomes favorite.
   */
  private void notifyFavoriteChanged(int color, boolean isFavorite) {
    for (FavoriteColorListener listener : listeners) {
      if (isFavorite) {
        listener.addToFavorites(color);
      } else {
        listener.removeFromFavorites(color);
      }
    }
  }

  /**
   * Initializes all favorite colors.
   *
   * @param favoriteColors All favorite colors.
   */
  public void setFavoriteColors(List<FavoriteColor> favoriteColors) {
    Set<Integer> colors = new HashSet<>();
    for (FavoriteColor color : favoriteColors) {
      colors.add(color.getColor());
    }
    this.favoriteColors = colors;
    updateCurrentColorViewFavorite();
  }

  /**
   * Updates isFavorite state of the current color view.
   */
  private void updateCurrentColorViewFavorite() {
    CurrentColorView currentColorView = getCurrentColorView();
    boolean isFavorite = isFavorite(currentColorView.getColor());
    if (isFavorite != currentColorView.isFavorite()) {
      currentColorView.setFavorite(isFavorite);
    }
  }

  /**
   * Checks whether the given color is a favorite or not according to the current favorites colors.
   *
   * @param color A color.
   * @return Whether the given color is a favorite or not.
   */
  private boolean isFavorite(Integer color) {
    return favoriteColors.contains(color);
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
      currentColorView.setFavorite(false);
    } else {
      currentColorRgbText.setText(formatToRgb(color));
      currentColorHsvText.setText(formatToHsv(color));
      currentColorView.setColor(color);
      currentColorView.setFavorite(isFavorite(color));
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
