package com.awesoon.thirdtask.view;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class CurrentColorView extends View {
  public static final int COLOR_ANIMATION_DURATION = 200;

  private Paint backgroundPaint = new Paint(Color.TRANSPARENT);
  private Integer color;

  public CurrentColorView(Context context) {
    super(context);
    init();
  }

  public CurrentColorView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public CurrentColorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  public CurrentColorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    init();
  }

  private void init() {
    setWillNotDraw(false);
  }

  /**
   * Retrieves current color.
   *
   * @return Current color.
   */
  public Integer getColor() {
    return color;
  }

  /**
   * Sets current color.
   *
   * @param color Current color.
   */
  public void setColor(Integer color) {
    startColorAnimation(this.color, color);
    this.color = color;
  }

  /**
   * Starts color animation.
   *
   * @param startColor Start color.
   * @param endColor   End color.
   */
  private void startColorAnimation(Integer startColor, Integer endColor) {
    if (startColor == null) {
      backgroundPaint.setColor(endColor);
      invalidate();
      return;
    }

    if (endColor == null) {
      endColor = Color.TRANSPARENT;
    }

    ObjectAnimator animator = ObjectAnimator.ofObject(backgroundPaint, "color", new ArgbEvaluator(),
        startColor, endColor);
    animator.setDuration(COLOR_ANIMATION_DURATION);
    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        invalidate();
      }
    });

    animator.start();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    if (color == null) {
      canvas.drawColor(Color.TRANSPARENT);
    } else {
      canvas.drawPaint(backgroundPaint);
    }
  }
}
