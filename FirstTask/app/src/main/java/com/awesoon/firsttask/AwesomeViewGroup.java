package com.awesoon.firsttask;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.awesoon.firsttask.util.Log;

public class AwesomeViewGroup extends FrameLayout {
  public static final String TAG = "AwesomeViewGroup";
  public static final int LOG_LEVEL = 2;

  private Log log = new Log();

  public AwesomeViewGroup(Context context) {
    super(context);
    init();
  }

  public AwesomeViewGroup(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public AwesomeViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  public AwesomeViewGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    init();
  }

  private void init() {
    setWillNotDraw(false);
    addLogEntry("constructor");
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    addLogEntry("On attached to window");
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    addLogEntry("On finish inflate");
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    addLogEntry("On measure");
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);
    addLogEntry("On layout");
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    addLogEntry("On draw");
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    addLogEntry("On detached from window");
  }

  @Override
  public void onViewAdded(View child) {
    super.onViewAdded(child);
    addLogEntry("On view added");
  }

  @Override
  public void onViewRemoved(View child) {
    super.onViewRemoved(child);
    addLogEntry("On view removed");
  }

  public AwesomeView getInnerView() {
    return ((AwesomeView) getChildAt(0));
  }

  public Log getLog() {
    return log;
  }

  private void addLogEntry(String ctor) {
    log.addEntry(LOG_LEVEL, TAG, ctor);
  }
}
