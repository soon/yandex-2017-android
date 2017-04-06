package com.awesoon.firsttask;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
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
  }


  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    log.addEntry(LOG_LEVEL, TAG, "On attached to window");
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    log.addEntry(LOG_LEVEL, TAG, "On finish inflate");
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    log.addEntry(LOG_LEVEL, TAG, "On measure");
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);
    log.addEntry(LOG_LEVEL, TAG, "On layout");
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    log.addEntry(LOG_LEVEL, TAG, "On draw");
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    log.addEntry(LOG_LEVEL, TAG, "On detached from window");
  }

  public AwesomeView getInnerView() {
    return ((AwesomeView) getChildAt(0));
  }

  public Log getLog() {
    return log;
  }
}
