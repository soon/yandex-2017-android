package com.awesoon.thirdtask.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.awesoon.thirdtask.R;
import com.awesoon.thirdtask.db.DbHelper;
import com.awesoon.thirdtask.domain.SysItem;
import com.awesoon.thirdtask.util.Assert;
import com.awesoon.thirdtask.util.StringUtils;

public class ElementEditorActivity extends AppCompatActivity {
  public static final String EXTRA_SYS_ITEM_ID = makeExtraIdent("EXTRA_SYS_ITEM_ID");

  private DbHelper dbHelper;
  private SysItem sysItem;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_element_editor);
    Toolbar toolbar = findViewById(R.id.toolbar, "R.id.toolbar");
    setSupportActionBar(toolbar);

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

    this.dbHelper = new DbHelper(this);
    new GetSysItemByIdTask(this, dbHelper).execute(42L);
  }

  private EditText getTitleEditText() {
    return findViewById(R.id.edit_title, "R.id.edit_title");
  }

  private EditText getBodyEditText() {
    return findViewById(R.id.edit_body, "R.id.edit_body");
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_element_editor, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.save_element) {
      saveSysItem();
      finish();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  private void setSysItem(SysItem sysItem) {
    this.sysItem = sysItem;

    if (sysItem == null) {
      setActionBarTitle(getString(R.string.element_editor_default_title));
    } else {
      setActionBarTitle(sysItem.getTitle());
    }
  }

  private void saveSysItem() {
    if (sysItem == null) {
      sysItem = new SysItem();
    }

    EditText titleEditText = getTitleEditText();
    sysItem.setTitle(titleEditText.getText().toString());

    EditText bodyEditText = getBodyEditText();
    sysItem.setBody(bodyEditText.getText().toString());

    sysItem.setColor(42); // todo

    new SaveSysItemTask(dbHelper).execute(sysItem);
  }

  private void setActionBarTitle(CharSequence title) {
    String resultTitle = StringUtils.makeEmptyIfNull(title);
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

  private <T> T findViewById(int id, String name) {
    View view = findViewById(id);
    Assert.notNull(view, "Unable to find view " + name);
    return (T) view;
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
