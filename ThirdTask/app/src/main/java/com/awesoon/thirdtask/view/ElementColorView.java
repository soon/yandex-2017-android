package com.awesoon.thirdtask.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class ElementColorView extends View {
  private Integer color;

  public ElementColorView(Context context) {
    super(context);
    init();
  }

  public ElementColorView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public ElementColorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  public ElementColorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    init();
  }

  private void init() {
    setColor(null);
  }

  public Integer getColor() {
    return color;
  }

  public void setColor(Integer color) {
    this.color = color;
    if (color == null) {
      doSetBackgroundColor(Color.TRANSPARENT);
    } else {
      doSetBackgroundColor(color);
    }
  }

  private void doSetBackgroundColor(int color) {
    Drawable background = getBackground();
    if (background instanceof GradientDrawable) {
      GradientDrawable shapeDrawable = (GradientDrawable) background;
      shapeDrawable.setColor(color);
    } else if (background instanceof ShapeDrawable) {
      ShapeDrawable shapeDrawable = (ShapeDrawable) background;
      shapeDrawable.getPaint().setColor(color);
    } else {
      setBackgroundColor(color);
    }
  }
}
