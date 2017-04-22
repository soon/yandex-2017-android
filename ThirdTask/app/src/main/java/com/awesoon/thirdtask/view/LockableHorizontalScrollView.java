package com.awesoon.thirdtask.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

public class LockableHorizontalScrollView extends HorizontalScrollView {

  private boolean isLocked;

  public LockableHorizontalScrollView(Context context) {
    super(context);
  }

  public LockableHorizontalScrollView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public LockableHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public LockableHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  public boolean isLocked() {
    return isLocked;
  }

  public void lock() {
    isLocked = true;
  }

  public void unlock() {
    isLocked = false;
  }

  @Override
  public boolean onTouchEvent(MotionEvent ev) {
    if (isLocked) {
      return false;
    }

    return super.onTouchEvent(ev);
  }

  @Override
  public boolean onInterceptTouchEvent(MotionEvent ev) {
    if (isLocked) {
      return false;
    }

    return super.onInterceptTouchEvent(ev);
  }
}
