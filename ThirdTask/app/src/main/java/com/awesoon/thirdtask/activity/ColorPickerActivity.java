package com.awesoon.thirdtask.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.awesoon.thirdtask.R;
import com.awesoon.thirdtask.event.ColorChangeListener;
import com.awesoon.thirdtask.util.ActivityUtils;
import com.awesoon.thirdtask.view.ColorPickerInfo;
import com.awesoon.thirdtask.view.ColorPickerView;

import java.util.Objects;

public class ColorPickerActivity extends AppCompatActivity {
  public static final String EXTRA_CURRENT_COLOR = makeExtraIdent("CURRENT_COLOR");

  public static final String STATE_CURRENT_COLOR_IDENT = makeExtraIdent("STATE_CURRENT_COLOR");
  public static final String STATE_SCROLL_POSITION_IDENT = makeExtraIdent("STATE_SCROLL_POSITION");
  public static final String STATE_PREV_SCROLL_VIEW_WIDTH_IDENT = makeExtraIdent("STATE_PREV_SCROLL_VIEW");
  public static final String STATE_BUTTON_COLORS_IDENT = makeExtraIdent("STATE_BUTTON_COLORS");

  private ColorPickerInfo colorPickerInfo;
  private ColorPickerView colorPickerView;
  private Integer initialColor;

  /**
   * Creates intent instance for starting this activity.
   *
   * @param context A parent context.
   * @param color   A current color. Nullable.
   * @return An intent.
   */
  public static Intent getInstance(Context context, @Nullable Integer color) {
    Intent intent = new Intent(context, ColorPickerActivity.class);
    if (color != null) {
      intent.putExtra(ColorPickerActivity.EXTRA_CURRENT_COLOR, color.intValue());
    }
    return intent;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_color_picker);
    overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

    colorPickerInfo = ActivityUtils.findViewById(this, R.id.colorPickerInfo, "R.id.colorPickerInfo");
    colorPickerView = ActivityUtils.findViewById(this, R.id.colorPickerView, "R.id.colorPickerView");

    Toolbar toolbar = ActivityUtils.findViewById(this, R.id.toolbar, "R.id.toolbar");
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    colorPickerView.setOnColorChangeListener(new ColorChangeListener() {
      @Override
      public void onColorChanged(int newColor) {
        colorPickerInfo.setColor(newColor);
      }
    });

    restoreSavedInstance(savedInstanceState, colorPickerView, colorPickerInfo);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_color_picker, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    switch (id) {
      case R.id.save_color:
        Intent resultIntent = new Intent();
        Integer color = colorPickerInfo.getColor();
        if (color == null) {
          color = Color.TRANSPARENT;
        }

        resultIntent.putExtra(EXTRA_CURRENT_COLOR, color);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        return true;
      case android.R.id.home:
        handleDiscardChangesAction();
        return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    outState.putInt(STATE_SCROLL_POSITION_IDENT, colorPickerView.getCurrentScrollPosition());
    outState.putInt(STATE_PREV_SCROLL_VIEW_WIDTH_IDENT, colorPickerView.getCurrentScrollViewWidth());
    outState.putIntegerArrayList(STATE_BUTTON_COLORS_IDENT, colorPickerView.getCurrentButtonColors());

    Integer color = colorPickerInfo.getColor();
    if (color != null) {
      outState.putInt(STATE_CURRENT_COLOR_IDENT, color);
    }
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
  }

  /**
   * Handles discard changes action (e.g. android.R.id.home).
   */
  private void handleDiscardChangesAction() {
    if (!wasElementChanged()) {
      discardChangesAndNavigateUpFromTask();
      return;
    }

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        switch (which) {
          case DialogInterface.BUTTON_POSITIVE:
            discardChangesAndNavigateUpFromTask();
            break;

          case DialogInterface.BUTTON_NEGATIVE:
            break;
        }
      }
    };

    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setMessage(R.string.move_back_dialog_message)
        .setPositiveButton(R.string.yes, dialogClickListener)
        .setNegativeButton(R.string.no, dialogClickListener)
        .show();
  }

  /**
   * Checks if a user changes initial color.
   *
   * @return true if a user changes color, false otherwise.
   */
  private boolean wasElementChanged() {
    return !Objects.equals(initialColor, colorPickerInfo.getColor());
  }

  /**
   * Discards current changes and moves back.
   */
  private void discardChangesAndNavigateUpFromTask() {
    NavUtils.navigateUpFromSameTask(ColorPickerActivity.this);
    overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
  }

  /**
   * Restores previously saved instance.
   *
   * @param savedInstanceState Saved instance.
   * @param colorPickerView    Color picker view.
   * @param colorPickerInfo    Color picker info.
   */
  private void restoreSavedInstance(Bundle savedInstanceState, ColorPickerView colorPickerView,
                                    ColorPickerInfo colorPickerInfo) {
    if (savedInstanceState == null) {
      setColorFromIntent(colorPickerInfo);
      return;
    }

    colorPickerView.setCurrentScrollPosition(savedInstanceState.getInt(STATE_SCROLL_POSITION_IDENT));
    colorPickerView.setPreviousScrollViewWidth(savedInstanceState.getInt(STATE_PREV_SCROLL_VIEW_WIDTH_IDENT));
    colorPickerView.setCurrentButtonColors(savedInstanceState.getIntegerArrayList(STATE_BUTTON_COLORS_IDENT));

    if (savedInstanceState.containsKey(STATE_CURRENT_COLOR_IDENT)) {
      colorPickerInfo.setColor(savedInstanceState.getInt(STATE_CURRENT_COLOR_IDENT));
    } else {
      setColorFromIntent(colorPickerInfo);
    }
  }

  /**
   * Sets current color from the given intent. If the intent is null, does nothing.
   *
   * @param colorPickerInfo Color picker info.
   * @return Whether the color was changed from the intent.
   */
  private boolean setColorFromIntent(ColorPickerInfo colorPickerInfo) {
    Intent intent = getIntent();
    Bundle extras = intent == null ? null : intent.getExtras();

    if (extras != null && extras.containsKey(EXTRA_CURRENT_COLOR)) {
      initialColor = extras.getInt(EXTRA_CURRENT_COLOR);
      colorPickerInfo.setColor(initialColor);
      return true;
    } else {
      return false;
    }
  }

  /**
   * Generates an ident.
   *
   * @param name Ident name.
   * @return Full ident name.
   */
  public static String makeExtraIdent(String name) {
    return ColorPickerActivity.class.getCanonicalName() + "." + name;
  }
}
