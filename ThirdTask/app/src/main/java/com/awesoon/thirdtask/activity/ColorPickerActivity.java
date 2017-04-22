package com.awesoon.thirdtask.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.awesoon.thirdtask.R;
import com.awesoon.thirdtask.db.DbHelper;
import com.awesoon.thirdtask.domain.FavoriteColor;
import com.awesoon.thirdtask.event.ColorChangeListener;
import com.awesoon.thirdtask.event.FavoriteColorListener;
import com.awesoon.thirdtask.util.Assert;
import com.awesoon.thirdtask.view.ColorPickerInfo;
import com.awesoon.thirdtask.view.ColorPickerView;

import java.util.List;

public class ColorPickerActivity extends AppCompatActivity {
  public static final String EXTRA_CURRENT_COLOR = makeExtraIdent("CURRENT_COLOR");

  public static final String STATE_CURRENT_COLOR_IDENT = makeExtraIdent("STATE_CURRENT_COLOR");
  public static final String STATE_SCROLL_POSITION_IDENT = makeExtraIdent("STATE_SCROLL_POSITION");
  public static final String STATE_PREV_SCROLL_VIEW_WIDTH_IDENT = makeExtraIdent("STATE_PREV_SCROLL_VIEW");
  public static final String STATE_BUTTON_COLORS_IDENT = makeExtraIdent("STATE_BUTTON_COLORS");

  private DbHelper dbHelper;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_color_picker);
    overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

    Toolbar toolbar = findViewById(R.id.toolbar, "R.id.toolbar");
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    this.dbHelper = new DbHelper(this);

    final ColorPickerView colorPickerView = getColorPickerView();
    final ColorPickerInfo colorPickerInfo = getColorPickerInfo();

    colorPickerView.setOnColorChangeListener(new ColorChangeListener() {
      @Override
      public void onColorChanged(int newColor) {
        colorPickerInfo.setColor(newColor);
      }
    });

    restoreSavedInstance(savedInstanceState, colorPickerView, colorPickerInfo);

    colorPickerInfo.setOnFavoriteColorListener(new FavoriteColorListener() {
      @Override
      public void addToFavorites(int color) {
        new ChangeColorFavoriteStatusTask(dbHelper, true).execute(color);
      }

      @Override
      public void removeFromFavorites(int color) {
        new ChangeColorFavoriteStatusTask(dbHelper, false).execute(color);
      }
    });

    new GetAllFavoriteColorsTask(this, dbHelper).execute();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_color_picker, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    switch (id) {
      case R.id.save_color:
        Intent resultIntent = new Intent();
        Integer color = getColorPickerInfo().getColor();
        if (color == null) {
          color = Color.TRANSPARENT;
        }

        resultIntent.putExtra(EXTRA_CURRENT_COLOR, color);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        return true;
      case android.R.id.home:
        NavUtils.navigateUpFromSameTask(this);
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    final ColorPickerView colorPickerView = getColorPickerView();
    final ColorPickerInfo colorPickerInfo = getColorPickerInfo();

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

  @NonNull
  private ColorPickerInfo getColorPickerInfo() {
    return findViewById(R.id.colorPickerInfo, "R.id.colorPickerInfo");
  }

  @NonNull
  private ColorPickerView getColorPickerView() {
    return findViewById(R.id.colorPickerView, "R.id.colorPickerView");
  }

  private void restoreSavedInstance(Bundle savedInstanceState, ColorPickerView colorPickerView,
                                    ColorPickerInfo colorPickerInfo) {
    if (savedInstanceState == null) {
      setColorFromIntent(colorPickerInfo);
      return;
    }

    colorPickerView.setCurrentScrollPosition(savedInstanceState.getInt(STATE_SCROLL_POSITION_IDENT));
    colorPickerView.setPreviousScrollViewWidth(savedInstanceState.getInt(STATE_PREV_SCROLL_VIEW_WIDTH_IDENT));
    colorPickerView.setCurrentButtonColors(savedInstanceState.getIntegerArrayList(STATE_BUTTON_COLORS_IDENT));

    if (setColorFromIntent(colorPickerInfo)) {
      return;
    }

    if (savedInstanceState.containsKey(STATE_CURRENT_COLOR_IDENT)) {
      colorPickerInfo.setColor(savedInstanceState.getInt(STATE_CURRENT_COLOR_IDENT));
    } else {
      colorPickerInfo.setColor(null);
    }
  }

  private boolean setColorFromIntent(ColorPickerInfo colorPickerInfo) {
    Intent intent = getIntent();
    Bundle extras = intent == null ? null : intent.getExtras();

    if (extras != null && extras.containsKey(EXTRA_CURRENT_COLOR)) {
      int currentColor = extras.getInt(EXTRA_CURRENT_COLOR);
      colorPickerInfo.setColor(currentColor);
      return true;
    } else {
      return false;
    }
  }

  public static String makeExtraIdent(String name) {
    return "com.awesoon.thirdtask.activity.ColorPickerActivity." + name;
  }

  private <T> T findViewById(int id, String name) {
    View view = findViewById(id);
    Assert.notNull(view, "Unable to find view " + name);
    return (T) view;
  }

  /**
   * A task for changing color favorite status.
   */
  private static class ChangeColorFavoriteStatusTask extends AsyncTask<Integer, Void, Void> {
    private DbHelper dbHelper;
    private boolean isFavorite;

    public ChangeColorFavoriteStatusTask(DbHelper dbHelper, boolean isFavorite) {
      this.dbHelper = dbHelper;
      this.isFavorite = isFavorite;
    }

    @Override
    protected Void doInBackground(Integer... colors) {
      for (Integer color : colors) {
        if (isFavorite) {
          dbHelper.addFavoriteColor(color);
        } else {
          dbHelper.removeFavoriteColor(color);
        }
      }

      return null;
    }
  }

  /**
   * A task for retrieving all favorite colors.
   */
  private static class GetAllFavoriteColorsTask extends AsyncTask<Void, Void, List<FavoriteColor>> {
    private ColorPickerActivity activity;
    private DbHelper dbHelper;

    private GetAllFavoriteColorsTask(ColorPickerActivity activity, DbHelper dbHelper) {
      this.activity = activity;
      this.dbHelper = dbHelper;
    }

    @Override
    protected List<FavoriteColor> doInBackground(Void... params) {
      return dbHelper.findAllFavoriteColors();
    }

    @Override
    protected void onPostExecute(final List<FavoriteColor> favoriteColors) {
      activity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          ColorPickerInfo colorPickerInfo = activity.getColorPickerInfo();
          colorPickerInfo.setFavoriteColors(favoriteColors);
        }
      });
    }
  }
}
