package com.awesoon.thirdtask.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.awesoon.thirdtask.R;
import com.awesoon.thirdtask.event.ButtonColorListener;
import com.awesoon.thirdtask.event.ColorChangeListener;
import com.awesoon.thirdtask.event.EditingModeChangeListener;
import com.awesoon.thirdtask.util.Assert;

import java.util.ArrayList;
import java.util.List;

public class ColorPickerView extends LinearLayout {
  public static final int TOTAL_BUTTONS_COUNT = 16;
  public static final float MAX_HUE_VALUE = 360;
  public static final int BUTTON_WIDTH_DPI = 80;
  public static final int BUTTON_HEIGHT_DPI = 80;
  public static final int BUTTON_MARGIN_DPI = 20;

  private int color;
  private List<ColorChangeListener> colorChangeListeners = new ArrayList<>();
  private int prevButtonsContainerWidth = 0;
  private int currentScrollPosition = 0;
  private int prevScrollViewWidth = 0;
  private boolean isEditingMode = false;

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

    LinearLayout buttonsContainer = getButtonsContainer();
    for (int i = 0; i < TOTAL_BUTTONS_COUNT; i++) {
      addNewColorPickerButton(buttonsContainer);
    }

    HorizontalScrollView scrollView = getScrollView();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      scrollView.setOnScrollChangeListener(new OnScrollChangeListener() {
        @Override
        public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
          currentScrollPosition = scrollX;
        }
      });
    }
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
   * Creates new button and adds it to the given container.
   *
   * @param buttonsContainer A buttons container.
   * @return Created button.
   */
  @NonNull
  private ColorPickerButton addNewColorPickerButton(final LinearLayout buttonsContainer) {
    ColorPickerButton button = new ColorPickerButton(getContext());
    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

    int width = (int) (displayMetrics.density * BUTTON_WIDTH_DPI);
    int height = (int) (displayMetrics.density * BUTTON_HEIGHT_DPI);
    int margin = (int) (displayMetrics.density * BUTTON_MARGIN_DPI);

    LayoutParams params = new LayoutParams(width, height);
    params.setMargins(margin, margin, margin, margin);
    buttonsContainer.addView(button, params);

    button.setOnColorChangeListener(new ButtonColorListener() {
      @Override
      public void onColorChanged(int newColor) {
        if (isEditingMode) {
          buttonsContainer.setBackgroundColor(newColor);
        }
      }

      @Override
      public boolean onBeforeColorSelected(int newColor) {
        // Forbid color selection when someone edits a button color.
        return !isEditingMode;
      }

      @Override
      public void onColorSelected(int newColor) {
        color = newColor;
        notifyColorChanged();
      }
    });

    button.setOnEditingModeChangeListener(new EditingModeChangeListener() {
      @Override
      public boolean onBeforeEnteringEditingMode() {
        // Allow to enter editing mode when the scroll view is unlocked.
        return !getScrollView().isLocked();
      }

      @Override
      public void onEnterEditingMode(int color) {
        enterEditingMode();
        buttonsContainer.setBackgroundColor(color);
      }

      @Override
      public void onExitEditingMode() {
        exitEditingMode();
        setBackgroundGradient(buttonsContainer);
      }
    });

    return button;
  }

  /**
   * Initializes buttons colors and sets background gradient.
   *
   * @param buttonsContainer A buttons container.
   */
  private void initializeColors(LinearLayout buttonsContainer) {
    ShapeDrawable gradient = setBackgroundGradient(buttonsContainer);

    List<ColorPickerButton> allButtons = getAllButtons();
    Bitmap bitmap = fillBitmapWithGradient(buttonsContainer.getWidth(), gradient);
    initializeButtonColors(allButtons, bitmap);
  }

  /**
   * Sets background gradient to the given container.
   *
   * @param buttonsContainer A buttons container.
   * @return A background gradient.
   */
  @NonNull
  private ShapeDrawable setBackgroundGradient(LinearLayout buttonsContainer) {
    ShapeDrawable gradient = createGradient(buttonsContainer);
    buttonsContainer.setBackground(gradient);
    return gradient;
  }

  /**
   * Creates gradient for the given buttons container.
   *
   * @param buttonsContainer A buttons container.
   * @return A drawable gradient.
   */
  @NonNull
  private ShapeDrawable createGradient(LinearLayout buttonsContainer) {
    // TOTAL_BUTTONS_COUNT + 1 because we are actually relying on N + 1 points:
    // before the first button, between all buttons (n - 1) and after the last button
    int[] gradientColors = new int[TOTAL_BUTTONS_COUNT + 1];
    for (int i = 0; i < gradientColors.length; i++) {
      gradientColors[i] = Color.HSVToColor(new float[]{getButtonMinHueValue(i), 1, 1});
    }

    return createGradient(buttonsContainer.getWidth(), gradientColors);
  }

  /**
   * Initializes buttons colors according to the given bitmap, filled with gradient.
   *
   * @param buttons A list of gradient buttons.
   * @param bitmap  A bitmap with filled background according to the gradient.
   */
  private void initializeButtonColors(List<ColorPickerButton> buttons, Bitmap bitmap) {
    for (int i = 0; i < buttons.size(); i++) {
      ColorPickerButton button = buttons.get(i);

      float x = button.getX();
      int width = button.getWidth();
      int color = bitmap.getPixel((int) (x + width / 2), 0);
      button.setDefaultColor(color);
      button.setColorIfAbsent(color);
      button.setHueMinValue(getButtonMinHueValue(i));
      button.setHueMaxValue(getButtonMaxHueValue(i));
    }
  }

  /**
   * Retrieves min Hue value for the button with the given index.
   *
   * @param buttonIndex A button index.
   * @return Min hue value for the button.
   */
  private float getButtonMinHueValue(int buttonIndex) {
    return MAX_HUE_VALUE / TOTAL_BUTTONS_COUNT * buttonIndex;
  }

  /**
   * Retrieves max Hue value for the button with the given index.
   *
   * @param buttonIndex A button index.
   * @return Max hue value for the button.
   */
  private float getButtonMaxHueValue(int buttonIndex) {
    return getButtonMinHueValue(buttonIndex + 1);
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
   * Retrieves current button colors.
   *
   * @return A list of current button colors.
   */
  public ArrayList<Integer> getCurrentButtonColors() {
    List<ColorPickerButton> buttons = getAllButtons();
    ArrayList<Integer> buttonColors = new ArrayList<>();

    for (ColorPickerButton button : buttons) {
      buttonColors.add(button.getColor());
    }

    return buttonColors;
  }

  /**
   * Sets current button colors.
   *
   * @param colors A list of current button colors.
   */
  public void setCurrentButtonColors(ArrayList<Integer> colors) {
    List<ColorPickerButton> buttons = getAllButtons();
    Assert.isTrue(buttons.size() == colors.size(), "Expected a list with size of " + buttons.size());
    for (int i = 0; i < buttons.size(); i++) {
      ColorPickerButton button = buttons.get(i);
      Integer color = colors.get(i);
      button.setColor(color);
    }
  }

  /**
   * Retrieves buttons container.
   *
   * @return A view, contains all buttons.
   */
  private LinearLayout getButtonsContainer() {
    LinearLayout container = (LinearLayout) findViewById(R.id.buttonsContainer);
    Assert.notNull(container, "Unable to find R.id.buttonsContainer");
    return container;
  }

  /**
   * Retrieves scroll view.
   *
   * @return A scroll view.
   */
  private LockableHorizontalScrollView getScrollView() {
    return ((LockableHorizontalScrollView) getChildAt(0));
  }

  /**
   * Locks scroll view.
   */
  private void enterEditingMode() {
    getScrollView().lock();
    isEditingMode = true;
  }

  /**
   * Unlocks scroll view.
   */
  private void exitEditingMode() {
    getScrollView().unlock();
    isEditingMode = false;
  }
}
