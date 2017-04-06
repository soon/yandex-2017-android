package com.awesoon.firsttask;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.awesoon.firsttask.util.Log;

import java.util.List;
import java.util.Locale;

public class AwesomeView extends View {
  public static final String TAG = "AwesomeView";
  public static final int LOG_LEVEL = 3;
  public static final int LOG_TEXT_SIZE = 25;

  private Log log = new Log();
  private Paint applicationPaint;
  private Paint activityPaint;
  private Paint viewGroupPaint;
  private Paint viewPaint;

  public AwesomeView(Context context) {
    super(context);
    init();
  }

  public AwesomeView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public AwesomeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  public AwesomeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    init();
  }

  private void init() {
    setWillNotDraw(false);
    applicationPaint = createPaint(Color.BLACK, LOG_TEXT_SIZE);
    activityPaint = createPaint(Color.MAGENTA, LOG_TEXT_SIZE);
    viewGroupPaint = createPaint(Color.BLUE, LOG_TEXT_SIZE);
    viewPaint = createPaint(Color.RED, LOG_TEXT_SIZE);
  }

  private Paint createPaint(int color, int size) {
    Paint paint = new Paint();
    paint.setColor(color);
    paint.setTextSize(size);
    return paint;
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    log.addEntry(LOG_LEVEL, TAG, "On attached to window");
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
    drawLog(canvas);
  }

  private void drawLog(Canvas canvas) {
    canvas.drawColor(Color.WHITE);
    Log mergedLogs = Log.getAllLogsMerged();

    int x = 100;
    int y = 100;
    final int levelMultiplier = 25;
    final int newEntryOffset = 25;

    List<List<Log.Message>> collapsedMessages = mergedLogs.collapseMessages();
    for (List<Log.Message> block : collapsedMessages) {
      Log.Message entry = block.get(0);
      String displayMessage = entry.getTag() + " " + entry.getMessage();

      if (block.size() > 1) {
        displayMessage = String.format(Locale.ROOT, "[%d]: %s", block.size(), displayMessage);
      }

      Paint paint;
      switch (entry.getTag()) {
        case AwesomeApplication.TAG:
          paint = applicationPaint;
          break;
        case AwesomeActivity.TAG:
          paint = activityPaint;
          break;
        case AwesomeViewGroup.TAG:
          paint = viewGroupPaint;
          break;
        case AwesomeView.TAG:
          paint = viewPaint;
          break;
        default:
          throw new UnsupportedOperationException("Unknown tag: " + entry.getTag());
      }

      canvas.drawText(displayMessage, x + entry.getLevel() * levelMultiplier, y, paint);
      y += newEntryOffset;
    }
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    invalidate();
    return true;
  }

  public Log getLog() {
    return log;
  }
}
