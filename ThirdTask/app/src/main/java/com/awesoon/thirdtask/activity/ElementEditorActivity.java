package com.awesoon.thirdtask.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.awesoon.thirdtask.R;
import com.awesoon.thirdtask.db.DbHelper;
import com.awesoon.thirdtask.domain.SysItem;
import com.awesoon.thirdtask.util.Assert;
import com.awesoon.thirdtask.util.BeautifulColors;
import com.awesoon.thirdtask.util.StringUtils;
import com.awesoon.thirdtask.view.ElementColorView;

public class ElementEditorActivity extends AppCompatActivity {
  private static final String TAG = "ElementEditorActivity";
  public static final int SELECT_ELEMENT_COLOR_REQUEST_CODE = 1;

  public static final String EXTRA_SYS_ITEM_ID = makeExtraIdent("EXTRA_SYS_ITEM_ID");
  public static final String EXTRA_SAVED_SYS_ITEM = makeExtraIdent("SAVED_SYS_ITEM");

  public static final String STATE_CURRENT_COLOR = makeExtraIdent("STATE_CURRENT_COLOR");
  public static final String STATE_CURRENT_TITLE = makeExtraIdent("STATE_CURRENT_TITLE");
  public static final String STATE_CURRENT_BODY = makeExtraIdent("STATE_CURRENT_BODY");

  private DbHelper dbHelper;
  private SysItem sysItem;

  /**
   * Creates intent instance for starting this activity.
   *
   * @param context   A parent context.
   * @param sysItemId An item id. Null, if you want to create new item.
   * @return An intent.
   */
  public static Intent getInstance(Context context, @Nullable Long sysItemId) {
    Intent intent = new Intent(context, ElementEditorActivity.class);
    if (sysItemId != null) {
      intent.putExtra(ElementEditorActivity.EXTRA_SYS_ITEM_ID, sysItemId.longValue());
    }
    return intent;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_element_editor);
    overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

    initToolbar();

    EditText titleEditText = getTitleEditText();
    titleEditText.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {

      }

      @Override
      public void afterTextChanged(Editable s) {
        setActionBarTitle(s);
      }
    });

    final ElementColorView elementColorView = getElementColorView();
    elementColorView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        openColorPickerActivity(elementColorView.getColor());
      }
    });

    this.dbHelper = new DbHelper(this);
    initializeEditorContent(savedInstanceState);
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    EditText titleEditText = getTitleEditText();
    outState.putString(STATE_CURRENT_TITLE, titleEditText.getText().toString());

    EditText bodyEditText = getBodyEditText();
    outState.putString(STATE_CURRENT_BODY, bodyEditText.getText().toString());

    ElementColorView elementColorView = getElementColorView();
    if (elementColorView.getColor() != null) {
      outState.putInt(STATE_CURRENT_COLOR, elementColorView.getColor());
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
      case SELECT_ELEMENT_COLOR_REQUEST_CODE:
        handleSelectElementColorResult(resultCode, data);
        break;
      default:
        Log.w(TAG, "Received unknown request code " + requestCode);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_element_editor, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    switch (id) {
      case R.id.save_element:
        if (validateInput()) {
          saveSysItemAndFinish();
        }
        return true;
      case android.R.id.home:
        NavUtils.navigateUpFromSameTask(this);
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
  }

  /**
   * Initializes toolbar.
   */
  private void initToolbar() {
    Toolbar toolbar = findViewById(R.id.toolbar, "R.id.toolbar");
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
  }

  /**
   * Opens {@link ColorPickerActivity} with the given color.
   *
   * @param color A color to be passed to the activity.
   */
  private void openColorPickerActivity(Integer color) {
    Intent intent = ColorPickerActivity.getInstance(this, color);
    startActivityForResult(intent, SELECT_ELEMENT_COLOR_REQUEST_CODE);
  }

  /**
   * Handles color changing.
   *
   * @param responseCode Status.
   * @param data         Data.
   */
  private void handleSelectElementColorResult(int responseCode, Intent data) {
    if (responseCode != Activity.RESULT_OK) {
      return;
    }

    if (data == null || data.getExtras() == null ||
        !data.getExtras().containsKey(ColorPickerActivity.EXTRA_CURRENT_COLOR)) {
      return;
    }

    int color = data.getExtras().getInt(ColorPickerActivity.EXTRA_CURRENT_COLOR);
    getElementColorView().setColor(color);
  }

  /**
   * Writes current sys item to the DB and finishes the activity.
   */
  private void saveSysItemAndFinish() {
    saveSysItem();
    Intent resultIntent = new Intent();
    resultIntent.putExtra(EXTRA_SAVED_SYS_ITEM, sysItem);
    setResult(Activity.RESULT_OK, resultIntent);
    finish();
    overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
  }

  /**
   * Validates the current user input.
   *
   * @return Whether the user input is valid.
   */
  private boolean validateInput() {
    boolean isValid = true;

    EditText titleEditText = getTitleEditText();
    String title = titleEditText.getText().toString().trim();
    if (title.isEmpty()) {
      titleEditText.setError(getString(R.string.title_edit_text_error));
      isValid = false;
    }

    EditText bodyEditText = getBodyEditText();
    String body = bodyEditText.getText().toString().trim();
    if (body.isEmpty()) {
      bodyEditText.setError(getString(R.string.body_edit_text_error));
      isValid = false;
    }

    return isValid;
  }

  /**
   * Sets default color to the element color view.
   */
  private void setDefaultColor() {
    int color = BeautifulColors.getBeautifulColor();
    ElementColorView elementColorView = getElementColorView();
    elementColorView.setColor(color);
  }

  /**
   * Sets current sys item.
   *
   * @param sysItem      Sys item. Nullable.
   * @param updateFields Whether to update fields data.
   */
  private void setSysItem(SysItem sysItem, boolean updateFields) {
    this.sysItem = sysItem;

    if (!updateFields) {
      return;
    }

    if (sysItem == null) {
      setActionBarTitle(getString(R.string.element_editor_default_title));
      setDefaultColor();
    } else {
      setActionBarTitle(sysItem.getTitle());
      setTitleEditText(sysItem.getTitle());
      setBodyEditText(sysItem.getBody());
      setColorEditColor(sysItem.getColor());
    }
  }

  /**
   * Updates body edit text.
   *
   * @param body Body text.
   */
  private void setBodyEditText(String body) {
    EditText bodyEditText = getBodyEditText();
    bodyEditText.setText(body);
  }

  /**
   * Updates title edit text.
   *
   * @param text Title text.
   */
  private void setTitleEditText(String text) {
    EditText titleEditText = getTitleEditText();
    titleEditText.setText(text);
  }

  /**
   * Saves current sys item to the DB.
   */
  private void saveSysItem() {
    if (sysItem == null) {
      sysItem = new SysItem();
    }

    EditText titleEditText = getTitleEditText();
    sysItem.setTitle(titleEditText.getText().toString().trim());

    EditText bodyEditText = getBodyEditText();
    sysItem.setBody(bodyEditText.getText().toString().trim());

    ElementColorView elementColorView = getElementColorView();
    sysItem.setColor(elementColorView.getColor());

    new SaveSysItemTask(dbHelper).execute(sysItem);
  }

  /**
   * Updates the action bar title.
   *
   * @param title A new action bar title. Nullable.
   */
  private void setActionBarTitle(CharSequence title) {
    String resultTitle = StringUtils.makeEmptyIfNull(title).trim();
    if (resultTitle.isEmpty()) {
      resultTitle = getString(R.string.element_editor_default_title);
    }

    ActionBar actionBar = getSupportActionBar();
    Assert.notNull(actionBar, "Unable to find action bar");
    actionBar.setTitle(resultTitle);
  }

  /**
   * Generates an ident.
   *
   * @param name Ident name.
   * @return Full ident name.
   */
  public static String makeExtraIdent(String name) {
    return "com.awesoon.thirdtask.activity.ElementEditorActivity." + name;
  }

  /**
   * Initializes the editor content according to the passed intent.
   *
   * @param savedInstanceState Sated instance state.
   */
  private void initializeEditorContent(Bundle savedInstanceState) {
    boolean updateFields = true;

    if (savedInstanceState != null) {
      if (savedInstanceState.containsKey(STATE_CURRENT_TITLE)) {
        String title = savedInstanceState.getString(STATE_CURRENT_TITLE);
        setActionBarTitle(title);
        setTitleEditText(title);
        updateFields = false;
      }
      if (savedInstanceState.containsKey(STATE_CURRENT_BODY)) {
        String body = savedInstanceState.getString(STATE_CURRENT_BODY);
        setBodyEditText(body);
        updateFields = false;
      }
      if (savedInstanceState.containsKey(STATE_CURRENT_COLOR)) {
        setColorEditColor(savedInstanceState.getInt(STATE_CURRENT_COLOR));
        updateFields = false;
      }
    }

    Long id = null;
    Intent intent = getIntent();
    if (intent != null) {
      Bundle extras = intent.getExtras();
      if (extras != null && extras.containsKey(EXTRA_SYS_ITEM_ID)) {
        id = extras.getLong(EXTRA_SYS_ITEM_ID);
      }
    }

    if (id != null) {
      new GetSysItemByIdTask(this, dbHelper, updateFields).execute(id);
    } else {
      setSysItem(null, updateFields);
    }
  }

  private EditText getTitleEditText() {
    return findViewById(R.id.edit_title, "R.id.edit_title");
  }

  private EditText getBodyEditText() {
    return findViewById(R.id.edit_body, "R.id.edit_body");
  }

  private ElementColorView getElementColorView() {
    return findViewById(R.id.edit_color, "R.id.edit_color");
  }

  /**
   * Finds a view by the given id.
   *
   * @param id   View id.
   * @param name View name.
   * @param <T>  View type.
   * @return Found id.
   * @throws AssertionError if the view not found.
   */
  private <T> T findViewById(int id, String name) {
    View view = findViewById(id);
    Assert.notNull(view, "Unable to find view " + name);
    return (T) view;
  }

  /**
   * Updates color edit color value.
   *
   * @param color New color.
   */
  private void setColorEditColor(int color) {
    ElementColorView elementColorView = getElementColorView();
    elementColorView.setColor(color);
  }

  /**
   * Saves sys item to the DB.
   */
  private static class SaveSysItemTask extends AsyncTask<SysItem, Void, SysItem> {
    private DbHelper dbHelper;

    public SaveSysItemTask(DbHelper dbHelper) {
      this.dbHelper = dbHelper;
    }

    @Override
    protected SysItem doInBackground(SysItem... params) {
      Assert.notEmpty(params, "Should pass at least one id");
      SysItem sysItem = params[0];
      Assert.notNull(sysItem, "SysItem must not be null");

      return dbHelper.saveSysItem(sysItem);
    }
  }

  /**
   * Retrieves sys item by the id. Calls setSysItem once finished.
   */
  private static class GetSysItemByIdTask extends AsyncTask<Long, Void, SysItem> {
    private ElementEditorActivity activity;
    private DbHelper dbHelper;
    private boolean updateFields;

    public GetSysItemByIdTask(ElementEditorActivity activity, DbHelper dbHelper, boolean updateFields) {
      this.activity = activity;
      this.dbHelper = dbHelper;
      this.updateFields = updateFields;
    }

    @Override
    protected SysItem doInBackground(Long... params) {
      Assert.notEmpty(params, "Should pass at least one id");
      Long id = params[0];
      Assert.notNull(id, "Id must not be null");
      return dbHelper.findSysItemById(id);
    }

    @Override
    protected void onPostExecute(final SysItem sysItem) {
      activity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          activity.setSysItem(sysItem, updateFields);
        }
      });
    }
  }
}
