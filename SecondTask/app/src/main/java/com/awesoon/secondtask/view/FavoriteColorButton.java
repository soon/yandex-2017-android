package com.awesoon.secondtask.view;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;

public class FavoriteColorButton extends AppCompatButton {
  private int color;

  public FavoriteColorButton(Context context) {
    super(context);
  }

  public FavoriteColorButton(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public FavoriteColorButton(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public int getColor() {
    return color;
  }

  public void setColor(int color) {
    this.color = color;
    setBackgroundColor(color);
  }
}
