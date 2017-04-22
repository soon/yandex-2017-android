package com.awesoon.thirdtask.view;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.awesoon.thirdtask.R;
import com.awesoon.thirdtask.event.FavoriteColorListener;
import com.awesoon.thirdtask.util.Assert;
import com.awesoon.thirdtask.util.MathUtil;

import java.util.ArrayList;
import java.util.List;

public class CurrentColorView extends View {
  public static final int STAR_TOP = 10;
  public static final int STAR_LEFT = 10;
  public static final int STAR_RIGHT = 100;
  public static final int STAR_BOTTOM = 100;
  public static final int STAR_ANIMATION_DELAY = 10;
  public static final int STAR_ANIMATION_OPACITY_DELTA = 30;
  public static final int COLOR_ANIMATION_DURATION = 200;

  private Paint backgroundPaint = new Paint(Color.TRANSPARENT);
  private Integer color;
  private boolean isFavorite;
  private Drawable star;

  private List<FavoriteColorListener> listeners = new ArrayList<>();

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
    setClickable(true);
    setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (color != null) {
          toggleFavorite();
        }
      }
    });
    getStar().setAlpha(0);
  }

  /**
   * Sets favorite color listener.
   *
   * @param listener A listener.
   */
  public void setOnFavoriteColorListener(FavoriteColorListener listener) {
    listeners.add(listener);
  }

  /**
   * @return true if the current view contains favorite color, false otherwise.
   */
  public boolean isFavorite() {
    return isFavorite;
  }

  /**
   * Sets current color favorite state.
   *
   * @param favorite Whether the color is favorite.
   */
  public void setFavorite(boolean favorite) {
    if (isFavorite == favorite) {
      return;
    }
    isFavorite = favorite;
    invalidate();
  }

  /**
   * Toggles favorite mode.
   */
  private void toggleFavorite() {
    setFavorite(!isFavorite);
    notifyFavoriteChanged();
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

    if (color != null) {
      Drawable star = getStar();
      star.setBounds(STAR_LEFT, STAR_TOP, STAR_RIGHT, STAR_BOTTOM);

      if (isFavorite && star.getAlpha() < 255) {
        star.setAlpha(MathUtil.fitToBounds(0, star.getAlpha() + STAR_ANIMATION_OPACITY_DELTA, 255));
        postInvalidateDelayed(STAR_ANIMATION_DELAY);
      } else if (!isFavorite && star.getAlpha() > 0) {
        star.setAlpha(MathUtil.fitToBounds(0, star.getAlpha() - STAR_ANIMATION_OPACITY_DELTA, 255));
        postInvalidateDelayed(STAR_ANIMATION_DELAY);
      }

      star.draw(canvas);
    }
  }

  /**
   * Retrieves drawable star.
   *
   * @return A drawable star.
   */
  private Drawable getStar() {
    if (star == null) {
      star = getContext().getDrawable(R.drawable.star_outline);
      Assert.notNull(star, "star");
      star = star.mutate();
    }

    return star;
  }

  /**
   * Notifies all listeners about favorite mode change.
   */
  private void notifyFavoriteChanged() {
    for (FavoriteColorListener listener : listeners) {
      if (isFavorite) {
        listener.addToFavorites(color);
      } else {
        listener.removeFromFavorites(color);
      }
    }
  }
}
