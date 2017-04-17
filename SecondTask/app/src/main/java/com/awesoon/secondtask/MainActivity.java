package com.awesoon.secondtask;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.awesoon.secondtask.db.FavoriteColorDbHelper;
import com.awesoon.secondtask.domain.FavoriteColor;
import com.awesoon.secondtask.event.ColorChangeListener;
import com.awesoon.secondtask.event.FavoriteColorListener;
import com.awesoon.secondtask.util.Assert;
import com.awesoon.secondtask.view.ColorPickerInfo;
import com.awesoon.secondtask.view.ColorPickerView;

import java.util.List;

public class MainActivity extends AppCompatActivity {
  public static final String STATE_CURRENT_COLOR_IDENT = "CURRENT_COLOR";
  public static final String STATE_SCROLL_POSITION_IDENT = "SCROLL_POSITION";
  public static final String STATE_PREV_SCROLL_VIEW_WIDTH_IDENT = "PREV_SCROLL_VIEW";
  public static final String STATE_BUTTON_COLORS_IDENT = "BUTTON_COLORS";

  private FavoriteColorDbHelper dbHelper;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    this.dbHelper = new FavoriteColorDbHelper(this);

    final ColorPickerView colorPickerView = (ColorPickerView) findViewById(R.id.colorPickerView);
    final ColorPickerInfo colorPickerInfo = getColorPickerInfo();
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

  @NonNull
  private ColorPickerInfo getColorPickerInfo() {
    final ColorPickerInfo colorPickerInfo = (ColorPickerInfo) findViewById(R.id.colorPickerInfo);
    Assert.notNull(colorPickerInfo, "colorPickerInfo must not be null");
    return colorPickerInfo;
  }

  @NonNull
  private ColorPickerView getColorPickerView() {
    final ColorPickerView colorPickerView = (ColorPickerView) findViewById(R.id.colorPickerView);
    Assert.notNull(colorPickerView, "colorPickerView must not be null");
    return colorPickerView;
  }

  /**
   * A task for changing color favorite status.
   */
  private static class ChangeColorFavoriteStatusTask extends AsyncTask<Integer, Void, Void> {
    private FavoriteColorDbHelper dbHelper;
    private boolean isFavorite;

    public ChangeColorFavoriteStatusTask(FavoriteColorDbHelper dbHelper, boolean isFavorite) {
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
    private MainActivity mainActivity;
    private FavoriteColorDbHelper dbHelper;

    private GetAllFavoriteColorsTask(MainActivity mainActivity, FavoriteColorDbHelper dbHelper) {
      this.mainActivity = mainActivity;
      this.dbHelper = dbHelper;
    }

    @Override
    protected List<FavoriteColor> doInBackground(Void... params) {
      return dbHelper.findAllFavoriteColors();
    }

    @Override
    protected void onPostExecute(final List<FavoriteColor> favoriteColors) {
      mainActivity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          ColorPickerInfo colorPickerInfo = mainActivity.getColorPickerInfo();
          colorPickerInfo.setFavoriteColors(favoriteColors);
        }
      });
    }
  }
}
