package com.awesoon.thirdtask.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.awesoon.core.async.AsyncTaskAction;
import com.awesoon.core.async.AsyncTaskBuilder;
import com.awesoon.core.async.AsyncTaskConsumer;
import com.awesoon.core.async.AsyncTaskProducer;
import com.awesoon.thirdtask.NotesApplication;
import com.awesoon.thirdtask.R;
import com.awesoon.thirdtask.adapter.text.TextWatcherAdapter;
import com.awesoon.thirdtask.db.DbHelper;
import com.awesoon.thirdtask.domain.SysItem;
import com.awesoon.thirdtask.service.SyncService;
import com.awesoon.thirdtask.service.UserService;
import com.awesoon.thirdtask.service.container.SyncOptions;
import com.awesoon.thirdtask.util.ActivityUtils;
import com.awesoon.thirdtask.util.Assert;
import com.awesoon.thirdtask.util.BeautifulColors;
import com.awesoon.thirdtask.util.StringUtils;
import com.awesoon.thirdtask.view.ElementColorView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import net.danlew.android.joda.DateUtils;

import org.joda.time.DateTime;

import java.util.Objects;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

public class ElementEditorActivity extends AppCompatActivity {
  private static final String TAG = "ElementEditorActivity";
  public static final int SELECT_ELEMENT_COLOR_REQUEST_CODE = 1;

  public static final String EXTRA_SYS_ITEM_ID = makeExtraIdent("EXTRA_SYS_ITEM_ID");
  public static final String EXTRA_SAVED_SYS_ITEM = makeExtraIdent("SAVED_SYS_ITEM");

  public static final String STATE_CURRENT_COLOR = makeExtraIdent("STATE_CURRENT_COLOR");
  public static final String STATE_CURRENT_TITLE = makeExtraIdent("STATE_CURRENT_TITLE");
  public static final String STATE_CURRENT_BODY = makeExtraIdent("STATE_CURRENT_BODY");
  public static final String STATE_CURRENT_IMAGE_URL = makeExtraIdent("STATE_CURRENT_IMAGE_URL");

  private SysItem sysItem;
  private EditText titleEditText;
  private EditText bodyEditText;
  private EditText imageUrlEditText;
  private TextView createdTimeTextView;
  private TextView lastUpdatedTimeTextView;
  private TextView lastViewedTimeTextView;
  private ElementColorView elementColorView;
  private ImageView imageView;
  private ProgressBar imageProgressLoader;

  @Inject
  SyncService syncService;

  @Inject
  UserService userService;

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
    AndroidInjection.inject(this);
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_element_editor);
    overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

    titleEditText = ActivityUtils.findViewById(this, R.id.edit_title, "R.id.edit_title");
    bodyEditText = ActivityUtils.findViewById(this, R.id.edit_body, "R.id.edit_body");
    elementColorView = ActivityUtils.findViewById(this, R.id.edit_color, "R.id.edit_color");
    imageUrlEditText = ActivityUtils.findViewById(this, R.id.edit_image_url, "R.id.edit_image_url");
    createdTimeTextView = ActivityUtils.findViewById(this, R.id.created_time, "R.id.created_time");
    lastUpdatedTimeTextView = ActivityUtils.findViewById(this, R.id.last_updated_time, "R.id.last_updated_time");
    lastViewedTimeTextView = ActivityUtils.findViewById(this, R.id.last_viewed_time, "R.id.last_viewed_time");
    imageView = ActivityUtils.findViewById(this, R.id.image_view, "R.id.image_view");
    imageProgressLoader = ActivityUtils.findViewById(this, R.id.image_progress_loader, "R.id.image_progress_loader");

    initToolbar();

    titleEditText.addTextChangedListener(new TextWatcherAdapter() {
      @Override
      public void afterTextChanged(Editable s) {
        setActionBarTitle(s);
      }
    });

    elementColorView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        openColorPickerActivity(elementColorView.getColor());
      }
    });

    imageUrlEditText.addTextChangedListener(new TextWatcherAdapter() {
      @Override
      public void afterTextChanged(Editable s) {
        String path = s.toString();
        if (StringUtils.isBlank(path)) {
          imageProgressLoader.setVisibility(View.GONE);
          Picasso.with(ElementEditorActivity.this)
              .load((String) null)
              .placeholder(R.drawable.default_bg)
              .into(imageView, null);
        } else {
          imageProgressLoader.setVisibility(View.VISIBLE);
          Picasso.with(ElementEditorActivity.this)
              .load(path)
              .placeholder(R.drawable.default_bg)
              .into(imageView, new Callback() {
                @Override
                public void onSuccess() {
                  imageProgressLoader.setVisibility(View.GONE);
                }

                @Override
                public void onError() {
                  imageProgressLoader.setVisibility(View.GONE);
                }
              });
        }
      }
    });

    initializeEditorContent(savedInstanceState);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    outState.putString(STATE_CURRENT_TITLE, titleEditText.getText().toString());
    outState.putString(STATE_CURRENT_BODY, bodyEditText.getText().toString());
    outState.putString(STATE_CURRENT_IMAGE_URL, imageUrlEditText.getText().toString());

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
        handleDiscardChangesAction();
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
   * Checks if a user made any change to the current sys item instance.
   *
   * @return true if a user changes current sys item, false otherwise.
   */
  private boolean wasElementChanged() {
    String title = getNormalizedTitle();
    String body = getNormalizedBody();
    Integer color = elementColorView.getColor();

    if (sysItem == null) {
      return !title.isEmpty() || !body.isEmpty();
    } else {
      return !Objects.equals(title, sysItem.getTitle())
          || !Objects.equals(body, sysItem.getBody())
          || !Objects.equals(color, sysItem.getColor());
    }
  }

  /**
   * Discards current changes and moves back.
   */
  private void discardChangesAndNavigateUpFromTask() {
    NavUtils.navigateUpFromSameTask(ElementEditorActivity.this);
    overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
  }

  /**
   * Initializes toolbar.
   */
  private void initToolbar() {
    Toolbar toolbar = ActivityUtils.findViewById(this, R.id.toolbar, "R.id.toolbar");
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
    elementColorView.setColor(color);
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

    String title = getNormalizedTitle();
    if (title.isEmpty()) {
      titleEditText.setError(getString(R.string.title_edit_text_error));
      isValid = false;
    }

    String body = getNormalizedBody();
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
      hideDateTimeFields();
      setDefaultImageUrl();
    } else {
      setActionBarTitle(sysItem.getTitle());
      setTitleEditText(sysItem.getTitle());
      setBodyEditText(sysItem.getBody());
      setColorEditColor(sysItem.getColor());

      setDateTime(createdTimeTextView, R.string.created_time_info, sysItem.getCreatedTime());
      setDateTime(lastUpdatedTimeTextView, R.string.last_edited_time_info, sysItem.getLastEditedTime());
      setDateTime(lastViewedTimeTextView, R.string.last_viewed_time_info, sysItem.getLastViewedTime());
      imageUrlEditText.setText(sysItem.getImageUrl());
    }
  }

  private void setDefaultImageUrl() {
    imageUrlEditText.setText(null);
  }

  /**
   * Sets datetime to the given text view.
   * If the date time is null, hides text view.
   * Otherwise extracts string resource with the given id, replaces placeholder with the datetime value
   * and sets the string to the text view.
   *
   * @param textView       A text view.
   * @param formatResource A text view format string. Should contain a string placeholder.
   * @param dateTime       Date time. Nullable.
   */
  private void setDateTime(TextView textView, @StringRes int formatResource, @Nullable DateTime dateTime) {
    if (dateTime == null) {
      textView.setVisibility(View.GONE);
    } else {
      textView.setVisibility(View.VISIBLE);
      textView.setText(formatDateTimeString(formatResource, dateTime));
    }
  }

  /**
   * Formats given date time string using the given string resource as a format string.
   *
   * @param formatResource A format string. Should contains a string placeholder.
   * @param dateTime       Date time. Must not be null.
   * @return Formatted datetime string.
   */
  private String formatDateTimeString(@StringRes int formatResource, @NonNull DateTime dateTime) {
    return getResources().getString(formatResource, DateUtils.getRelativeTimeSpanString(this, dateTime, true));
  }

  /**
   * Updates body edit text.
   *
   * @param body Body text.
   */
  private void setBodyEditText(String body) {
    bodyEditText.setText(body);
  }

  /**
   * Updates title edit text.
   *
   * @param text Title text.
   */
  private void setTitleEditText(String text) {
    titleEditText.setText(text);
  }

  /**
   * Saves current sys item to the DB.
   */
  private void saveSysItem() {
    SysItem originalSysItem = null;
    if (sysItem == null) {
      sysItem = new SysItem();
    } else {
      originalSysItem = new SysItem(sysItem);
      originalSysItem.setSynced(true);
    }

    sysItem.setTitle(getNormalizedTitle());
    sysItem.setBody(getNormalizedBody());
    sysItem.setColor(elementColorView.getColor());
    sysItem.setImageUrl(imageUrlEditText.getText().toString().trim());
    sysItem.setUserId(userService.getCurrentUserId());
    sysItem.setSynced(false);

    NotesApplication app = (NotesApplication) getApplication();
    final DbHelper dbHelper = app.getDbHelper();

    final SysItem finalOriginalSysItem = originalSysItem;
    AsyncTaskBuilder.firstly(new AsyncTaskProducer<SysItem>() {
      @Override
      public SysItem doApply() {
        return dbHelper.saveSysItem(sysItem);
      }
    }).then(new AsyncTaskConsumer<SysItem>() {
      @Override
      protected void doApply(SysItem note) {
        dbHelper.createUnderlyingSysItemIfAbsent(finalOriginalSysItem);
      }
    }).then(new AsyncTaskAction() {
      @Override
      public void doApply() {
        syncService.syncAllNotes(new SyncOptions());
      }
    }).build().execute();
  }

  /**
   * Retrieves current body editor text value and normalizes it.
   *
   * @return Normalized body text.
   */
  @NonNull
  private String getNormalizedBody() {
    return bodyEditText.getText().toString().trim();
  }

  /**
   * Retrieves current title editor text value and normalizes it.
   *
   * @return Normalized title text.
   */
  @NonNull
  private String getNormalizedTitle() {
    return titleEditText.getText().toString().trim();
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
    return ElementEditorActivity.class.getCanonicalName() + "." + name;
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
      if (savedInstanceState.containsKey(STATE_CURRENT_IMAGE_URL)) {
        imageUrlEditText.setText(savedInstanceState.getString(STATE_CURRENT_IMAGE_URL));
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
      NotesApplication app = (NotesApplication) getApplication();
      DbHelper dbHelper = app.getDbHelper();
      new GetSysItemByIdTask(this, dbHelper, updateFields).execute(id);
    } else {
      setSysItem(null, updateFields);
      hideDateTimeFields();
    }
  }

  private void hideDateTimeFields() {
    createdTimeTextView.setVisibility(View.GONE);
    lastUpdatedTimeTextView.setVisibility(View.GONE);
    lastViewedTimeTextView.setVisibility(View.GONE);
  }

  /**
   * Updates color edit color value.
   *
   * @param color New color.
   */
  private void setColorEditColor(int color) {
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
      if (activity != null) {
        activity.setSysItem(sysItem, updateFields);
      }
    }
  }
}
