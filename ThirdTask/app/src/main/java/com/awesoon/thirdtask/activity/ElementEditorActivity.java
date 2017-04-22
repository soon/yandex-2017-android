package com.awesoon.thirdtask.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.awesoon.thirdtask.util.StringUtils;
import com.awesoon.thirdtask.view.ElementColorView;

import java.util.Random;

public class ElementEditorActivity extends AppCompatActivity {
  private static final String TAG = "ElementEditorActivity";
  public static final int SELECT_ELEMENT_COLOR_REQUEST_CODE = 1;

  public static final String EXTRA_SYS_ITEM_ID = makeExtraIdent("EXTRA_SYS_ITEM_ID");
  public static final String EXTRA_SAVED_SYS_ITEM = makeExtraIdent("SAVED_SYS_ITEM");
  public static final int[] BEAUTIFUL_COLORS = new int[]{
      0xFFF44336,
      0xFFE91E63,
      0xFF9C27B0,
      0xFF673AB7,
      0xFF3F51B5,
      0xFF2196F3,
      0xFF03A9F4,
      0xFF00BCD4,
      0xFF009688,
      0xFF4CAF50,
      0xFF8BC34A,
      0xFFCDDC39,
      0xFFFFEB3B,
      0xFFFFC107,
      0xFFFF9800,
      0xFFFF5722
  };

  private DbHelper dbHelper;
  private SysItem sysItem;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_element_editor);
    overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

    Toolbar toolbar = findViewById(R.id.toolbar, "R.id.toolbar");
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

    initializeEditorContent();
  }

  private void openColorPickerActivity(Integer color) {
    Intent intent = new Intent(ElementEditorActivity.this, ColorPickerActivity.class);
    if (color != null) {
      intent.putExtra(ColorPickerActivity.EXTRA_CURRENT_COLOR, color.intValue());
    }
    startActivityForResult(intent, SELECT_ELEMENT_COLOR_REQUEST_CODE);
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

  private void handleSelectElementColorResult(int requestCode, Intent data) {
    if (requestCode != Activity.RESULT_OK) {
      return;
    }

    if (data == null || data.getExtras() == null ||
        !data.getExtras().containsKey(ColorPickerActivity.EXTRA_CURRENT_COLOR)) {
      return;
    }

    int color = data.getExtras().getInt(ColorPickerActivity.EXTRA_CURRENT_COLOR);
    getElementColorView().setColor(color);
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

  private void saveSysItemAndFinish() {
    saveSysItem();
    Intent resultIntent = new Intent();
    resultIntent.putExtra(EXTRA_SAVED_SYS_ITEM, sysItem);
    setResult(Activity.RESULT_OK, resultIntent);
    finish();
    overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
  }

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

  private void setDefaultColor() {
    Random rnd = new Random();
    int idx = rnd.nextInt(BEAUTIFUL_COLORS.length);
    int color = BEAUTIFUL_COLORS[idx];
    ElementColorView elementColorView = getElementColorView();
    elementColorView.setColor(color);
  }

  private void setSysItem(SysItem sysItem) {
    this.sysItem = sysItem;

    if (sysItem == null) {
      setActionBarTitle(getString(R.string.element_editor_default_title));
      setDefaultColor();
    } else {
      setActionBarTitle(sysItem.getTitle());
      setTitleEditText(sysItem);
      setBodyEditText(sysItem);
      setColorEditColor(sysItem);
    }
  }

  private void setBodyEditText(SysItem sysItem) {
    EditText bodyEditText = getBodyEditText();
    bodyEditText.setText(sysItem.getBody());
  }

  private void setTitleEditText(SysItem sysItem) {
    EditText titleEditText = getTitleEditText();
    titleEditText.setText(sysItem.getTitle());
  }

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

  private void setActionBarTitle(CharSequence title) {
    String resultTitle = StringUtils.makeEmptyIfNull(title).trim();
    if (resultTitle.isEmpty()) {
      resultTitle = getString(R.string.element_editor_default_title);
    }

    ActionBar actionBar = getSupportActionBar();
    Assert.notNull(actionBar, "Unable to find action bar");
    actionBar.setTitle(resultTitle);
  }

  public static String makeExtraIdent(String name) {
    return "com.awesoon.thirdtask.activity.ElementEditorActivity." + name;
  }

  private void initializeEditorContent() {
    Long id = null;
    Intent intent = getIntent();
    if (intent != null) {
      Bundle extras = intent.getExtras();
      if (extras != null && extras.containsKey(EXTRA_SYS_ITEM_ID)) {
        id = extras.getLong(EXTRA_SYS_ITEM_ID);
      }
    }

    if (id != null) {
      new GetSysItemByIdTask(this, dbHelper).execute(id);
    } else {
      setSysItem(null);
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

  private <T> T findViewById(int id, String name) {
    View view = findViewById(id);
    Assert.notNull(view, "Unable to find view " + name);
    return (T) view;
  }

  public void setColorEditColor(SysItem sysItem) {
    ElementColorView elementColorView = getElementColorView();
    elementColorView.setColor(sysItem.getColor());
  }

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

  private static class GetSysItemByIdTask extends AsyncTask<Long, Void, SysItem> {
    private ElementEditorActivity activity;
    private DbHelper dbHelper;

    public GetSysItemByIdTask(ElementEditorActivity activity, DbHelper dbHelper) {
      this.activity = activity;
      this.dbHelper = dbHelper;
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
          activity.setSysItem(sysItem);
        }
      });
    }
  }
}
