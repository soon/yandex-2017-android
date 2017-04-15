package com.awesoon.secondtask.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.awesoon.secondtask.event.ColorChangeListener;

import java.util.ArrayList;
import java.util.List;

public class ColorPickerView extends LinearLayout {
  private int color;
  private List<ColorChangeListener> colorChangeListeners = new ArrayList<>();
  private int prevButtonsContainerWidth = 0;
  private int currentScrollPosition = 0;
  private int prevScrollViewWidth = 0;

  public ColorPickerView(Context context) {
    super(context);
  }

  public ColorPickerView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public ColorPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public ColorPickerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    List<ColorPickerButton> allButtons = getAllButtons();
    for (ColorPickerButton button : allButtons) {
      button.setOnColorChangeListener(new ColorChangeListener() {
        @Override
        public void onColorChanged(int newColor) {
          color = newColor;
          notifyColorChanged();
        }
      });
    }
    HorizontalScrollView scrollView = getScrollView();
    scrollView.setOnScrollChangeListener(new OnScrollChangeListener() {
      @Override
      public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        currentScrollPosition = scrollX;
      }
    });
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    LinearLayout buttonsContainer = getButtonsContainer();
    int buttonsContainerWidth = buttonsContainer.getWidth();
    if (buttonsContainerWidth > 0 && buttonsContainerWidth != prevButtonsContainerWidth) {
      prevButtonsContainerWidth = buttonsContainerWidth;

      initializeColors(buttonsContainer);
      HorizontalScrollView scrollView = getScrollView();

      // Move scroll to the center
      currentScrollPosition += prevScrollViewWidth / 2;
      // Move back according to the new width
      // This allows us to restore the center position, not the left.
      currentScrollPosition -= scrollView.getWidth() / 2;

      scrollView.scrollTo(currentScrollPosition, scrollView.getScrollY());
    }
  }

  /**
   * Initializes buttons colors and sets background gradient.
   *
   * @param buttonsContainer A buttons container.
   */
  private void initializeColors(LinearLayout buttonsContainer) {
    List<ColorPickerButton> allButtons = getAllButtons();
    // allButtons.size() + 1 because we are actually relying on N + 1 points:
    // before the first button, between all buttons (n - 1) and after the last button
    int[] gradientColors = new int[allButtons.size() + 1];
    for (int i = 0; i < gradientColors.length; i++) {
      gradientColors[i] = Color.HSVToColor(new float[]{360 / allButtons.size() * i, 1, 1});
    }

    ShapeDrawable gradient = createGradient(buttonsContainer.getWidth(), gradientColors);
    Bitmap bitmap = fillBitmapWithGradient(buttonsContainer.getWidth(), gradient);
    initializeButtonColors(allButtons, bitmap);
    buttonsContainer.setBackground(gradient);
  }

  /**
   * Initializes buttons colors according to the given bitmap, filled with gradient.
   *
   * @param buttons A list of gradient buttons.
   * @param bitmap  A bitmap with filled background according to the gradient.
   */
  private void initializeButtonColors(List<ColorPickerButton> buttons, Bitmap bitmap) {
    for (ColorPickerButton button : buttons) {
      float x = button.getX();
      int width = button.getWidth();
      int color = bitmap.getPixel((int) (x + width / 2), 0);
      button.setColor(color);
    }
  }

  /**
   * Creates bitmap and fills it with the given gradient.
   *
   * @param width A bitmap width.
   * @param shape Gradient shape.
   * @return A bitmap with size of (width x 1), filled with the given gradient.
   */
  @NonNull
  private Bitmap fillBitmapWithGradient(int width, ShapeDrawable shape) {
    Bitmap bitmap = Bitmap.createBitmap(width, 1, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    shape.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
    shape.draw(canvas);
    return bitmap;
  }

  /**
   * Creates gradient.
   *
   * @param width  Gradient width.
   * @param colors Gradient colors.
   * @return A drawable gradient.
   */
  @NonNull
  private ShapeDrawable createGradient(int width, int[] colors) {
    LinearGradient gradient = new LinearGradient(0.f, 0.f, width, 0.0f, colors, null, Shader.TileMode.CLAMP);
    ShapeDrawable shape = new ShapeDrawable(new RectShape());
    shape.getPaint().setShader(gradient);
    return shape;
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
   * Adds color change listener.
   *
   * @param listener A listener.
   */
  public void setOnColorChangeListener(ColorChangeListener listener) {
    colorChangeListeners.add(listener);
  }

  /**
   * Retrieves all view buttons.
   *
   * @return A list of all view buttons.
   */
  private List<ColorPickerButton> getAllButtons() {
    LinearLayout buttonsContainer = getButtonsContainer();
    List<ColorPickerButton> buttons = new ArrayList<>();

    for (int i = 0; i < buttonsContainer.getChildCount(); i++) {
      ColorPickerButton button = (ColorPickerButton) buttonsContainer.getChildAt(i);
      buttons.add(button);
    }

    return buttons;
  }

  /**
   * Sets new scroll position.
   *
   * @param currentScrollPosition A new scroll position.
   */
  public void setCurrentScrollPosition(int currentScrollPosition) {
    this.currentScrollPosition = currentScrollPosition;
    HorizontalScrollView scrollView = getScrollView();
    if (scrollView.getWidth() > 0) {
      scrollView.scrollTo(currentScrollPosition, scrollView.getScrollY());
    }
  }

  /**
   * Retrieves current scroll position
   *
   * @return Current scroll position.
   */
  public int getCurrentScrollPosition() {
    return currentScrollPosition;
  }

  /**
   * Retrieves current scroll view width.
   *
   * @return Current scroll view width.
   */
  public int getCurrentScrollViewWidth() {
    return getScrollView().getWidth();
  }

  /**
   * Sets previous scroll view width.
   *
   * @param width Previous scroll view width.
   */
  public void setPreviousScrollViewWidth(int width) {
    prevScrollViewWidth = width;
  }

  /**
   * Retrieves buttons container.
   *
   * @return A view, contains all buttons.
   */
  private LinearLayout getButtonsContainer() {
    HorizontalScrollView scrollView = getScrollView();
    return (LinearLayout) scrollView.getChildAt(0);
  }

  /**
   * Retrieves scroll view.
   *
   * @return A scroll view.
   */
  private HorizontalScrollView getScrollView() {
    return ((HorizontalScrollView) getChildAt(0));
  }
}
