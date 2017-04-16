package com.awesoon.secondtask;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.awesoon.secondtask.event.ColorChangeListener;
import com.awesoon.secondtask.util.Assert;
import com.awesoon.secondtask.view.ColorPickerInfo;
import com.awesoon.secondtask.view.ColorPickerView;

public class MainActivity extends AppCompatActivity {
  public static final String STATE_CURRENT_COLOR_IDENT = "CURRENT_COLOR";
  public static final String STATE_SCROLL_POSITION_IDENT = "SCROLL_POSITION";
  public static final String STATE_PREV_SCROLL_VIEW_WIDTH_IDENT = "PREV_SCROLL_VIEW";
  public static final String STATE_BUTTON_COLORS_IDENT = "BUTTON_COLORS";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    final ColorPickerView colorPickerView = (ColorPickerView) findViewById(R.id.colorPickerView);
    final ColorPickerInfo colorPickerInfo = (ColorPickerInfo) findViewById(R.id.colorPickerInfo);

    Assert.notNull(colorPickerInfo, "colorPickerInfo must not be null");
    Assert.notNull(colorPickerView, "colorPickerView must not be null");

    colorPickerView.setOnColorChangeListener(new ColorChangeListener() {
      @Override
      public void onColorChanged(int newColor) {
        colorPickerInfo.setColor(newColor);
      }
    });

    if (savedInstanceState != null) {
      colorPickerView.setCurrentScrollPosition(savedInstanceState.getInt(STATE_SCROLL_POSITION_IDENT));
      colorPickerView.setPreviousScrollViewWidth(savedInstanceState.getInt(STATE_PREV_SCROLL_VIEW_WIDTH_IDENT));
      colorPickerView.setCurrentButtonColors(savedInstanceState.getIntegerArrayList(STATE_BUTTON_COLORS_IDENT));

      if (savedInstanceState.containsKey(STATE_CURRENT_COLOR_IDENT)) {
        colorPickerInfo.setColor(savedInstanceState.getInt(STATE_CURRENT_COLOR_IDENT));
      } else {
        colorPickerInfo.setColor(null);
      }
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    final ColorPickerView colorPickerView = (ColorPickerView) findViewById(R.id.colorPickerView);
    final ColorPickerInfo colorPickerInfo = (ColorPickerInfo) findViewById(R.id.colorPickerInfo);

    Assert.notNull(colorPickerInfo, "colorPickerInfo must not be null");
    Assert.notNull(colorPickerView, "colorPickerView must not be null");

    outState.putInt(STATE_SCROLL_POSITION_IDENT, colorPickerView.getCurrentScrollPosition());
    outState.putInt(STATE_PREV_SCROLL_VIEW_WIDTH_IDENT, colorPickerView.getCurrentScrollViewWidth());
    outState.putIntegerArrayList(STATE_BUTTON_COLORS_IDENT, colorPickerView.getCurrentButtonColors());

    Integer color = colorPickerInfo.getColor();
    if (color != null) {
      outState.putInt(STATE_CURRENT_COLOR_IDENT, color);
    }
  }
}
