package com.awesoon.thirdtask.activity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import com.awesoon.thirdtask.R;
import com.awesoon.thirdtask.repository.SysItemFilterRepository;
import com.awesoon.thirdtask.repository.filter.DatePeriodFilter;
import com.awesoon.thirdtask.repository.filter.SortFilter;
import com.awesoon.thirdtask.repository.filter.SysItemFilter;
import com.awesoon.thirdtask.util.ActivityUtils;
import com.awesoon.thirdtask.util.BeautifulColors;
import com.awesoon.thirdtask.util.CollectionUtils;
import com.awesoon.thirdtask.util.StringUtils;
import com.awesoon.thirdtask.view.ColorsFilterAdapter;
import com.awesoon.thirdtask.view.SortFiltersAdapter;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class FilterEditorActivity extends AppCompatActivity {
  public static final String DATE_FORMAT_STRING = "dd.MM.yyyy";
  public static final int SELECT_COLOR_ACTION_CODE = 1;
  public static final String EXTRA_SHOULD_CREATE_NEW_FILTER = makeExtraIdent("EXTRA_SHOULD_CREATE_NEW_FILTER");
  public static final String STATE_INITIAL_FILTER = makeExtraIdent("STATE_INITIAL_FILTER");

  private EditText nameEditText;
  private RecyclerView sortsRecyclerView;
  private SortFiltersAdapter sortFiltersAdapter;
  private Button addSortButton;
  private EditText createdStartDatePicker;
  private EditText createdEndDatePicker;
  private EditText editedStartDatePicker;
  private EditText editedEndDatePicker;
  private EditText viewedStartDatePicker;
  private EditText viewedEndDatePicker;
  private RecyclerView colorsRecyclerView;
  private ColorsFilterAdapter colorsAdapter;
  private Button addColorButton;

  private SysItemFilter initialFilter;

  /**
   * Creates intent instance for starting this activity.
   *
   * @param context A parent context.
   * @param isNew   Whether the editor should create a new filter (true) or edit currently selected (false).
   * @return An intent.
   */
  public static Intent getInstance(Context context, boolean isNew) {
    Intent intent = new Intent(context, FilterEditorActivity.class);
    intent.putExtra(EXTRA_SHOULD_CREATE_NEW_FILTER, isNew);
    return intent;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_filter_editor);
    overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

    initToolBar();
    initMembers(savedInstanceState);

    initName();
    initSortsRecyclerView();
    initColorsRecyclerView();

    initializeDatePickers(createdStartDatePicker, createdEndDatePicker, initialFilter.getCreatedTimeFilter());
    initializeDatePickers(editedStartDatePicker, editedEndDatePicker, initialFilter.getLastEditedTimeFilter());
    initializeDatePickers(viewedStartDatePicker, viewedEndDatePicker, initialFilter.getLastViewedTimeFilter());
  }

  private void initName() {
    nameEditText.setText(initialFilter.getName());
  }

  private void initColorsRecyclerView() {
    colorsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    colorsAdapter = new ColorsFilterAdapter(this, initialFilter.getColors());
    colorsRecyclerView.setAdapter(colorsAdapter);

    addColorButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = ColorPickerActivity.getInstance(FilterEditorActivity.this, BeautifulColors.getBeautifulColor());
        startActivityForResult(intent, SELECT_COLOR_ACTION_CODE);
      }
    });
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (resultCode != RESULT_OK) {
      return;
    }

    if (requestCode == SELECT_COLOR_ACTION_CODE) {
      Bundle extras = data.getExtras();
      if (extras != null && extras.containsKey(ColorPickerActivity.EXTRA_CURRENT_COLOR)) {
        int color = extras.getInt(ColorPickerActivity.EXTRA_CURRENT_COLOR);
        colorsAdapter.add(color);
      }
    }
  }

  private void initializeDatePickers(final EditText fromEditText, final EditText toEditText,
                                     @Nullable DatePeriodFilter initialPeriod) {
    DateTime fromDate = initialPeriod == null ? null : initialPeriod.getFrom();
    DateTime toDate = initialPeriod == null ? null : initialPeriod.getTo();

    initializeDatePicker(fromEditText, fromDate);
    initializeDatePicker(toEditText, toDate);
  }

  private void initializeDatePicker(final EditText editText, @Nullable DateTime initialTime) {
    final Calendar calendar = Calendar.getInstance();

    final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
      @Override
      public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, monthOfYear);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        updateLabel();
      }

      private void updateLabel() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_STRING, Locale.getDefault());
        editText.setText(sdf.format(calendar.getTime()));
      }
    };

    editText.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        new DatePickerDialog(FilterEditorActivity.this, date,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)).show();
      }
    });

    if (initialTime != null) {
      DateTimeFormatter dateTimeFormatter = getDateTimeFormatter();
      editText.setText(dateTimeFormatter.print(initialTime));
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_filter_editor, menu);

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    switch (id) {
      case R.id.save_filter:
        saveSysItemFilterAndFinish();
        return true;

      case android.R.id.home:
        handleDiscardChangesAction();
        return true;
    }

    return super.onOptionsItemSelected(item);
  }

  private void initSortsRecyclerView() {
    sortsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    sortFiltersAdapter = new SortFiltersAdapter(this, initialFilter.getSorts());
    sortsRecyclerView.setAdapter(sortFiltersAdapter);

    addSortButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        sortFiltersAdapter.add(new SortFilter());
      }
    });
  }

  private void initMembers(Bundle savedInstanceState) {
    nameEditText = ActivityUtils.findViewById(this, R.id.filter_name, "R.id.filter_name");
    sortsRecyclerView = ActivityUtils.findViewById(this, R.id.orders_list, "R.id.orders_list");
    addSortButton = ActivityUtils.findViewById(this, R.id.add_new_order, "R.id.add_new_order");
    createdStartDatePicker = ActivityUtils.findViewById(this,
        R.id.created_date_start_filter, "R.id.created_date_start_filter");
    createdEndDatePicker = ActivityUtils.findViewById(this,
        R.id.created_date_end_filter, "R.id.created_date_end_filter");
    editedStartDatePicker = ActivityUtils.findViewById(this,
        R.id.edited_date_start_filter, "R.id.edited_date_start_filter");
    editedEndDatePicker = ActivityUtils.findViewById(this,
        R.id.edited_date_end_filter, "R.id.edited_date_end_filter");
    viewedStartDatePicker = ActivityUtils.findViewById(this,
        R.id.viewed_date_start_filter, "R.id.viewed_date_start_filter");
    viewedEndDatePicker = ActivityUtils.findViewById(this,
        R.id.viewed_date_end_filter, "R.id.viewed_date_end_filter");

    colorsRecyclerView = ActivityUtils.findViewById(this, R.id.filtered_colors, "R.id.filtered_colors");
    addColorButton = ActivityUtils.findViewById(this, R.id.add_new_color, "R.id.add_new_color");

    if (savedInstanceState != null && savedInstanceState.containsKey(STATE_INITIAL_FILTER)) {
      String json = savedInstanceState.getString(STATE_INITIAL_FILTER);
      initialFilter = SysItemFilter.tryParseJson(json);
    } else {
      Intent intent = getIntent();
      if (intent != null && intent.getExtras() != null) {
        boolean isNew = intent.getExtras().getBoolean(EXTRA_SHOULD_CREATE_NEW_FILTER, false);
        initialFilter = isNew ? new SysItemFilter() : SysItemFilterRepository.getCurrentFilter(this);
      }
    }

    if (initialFilter == null) {
      initialFilter = SysItemFilterRepository.getCurrentFilter(this);
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    if (initialFilter != null) {
      initialFilter = getUpdatedFilter(false);
      outState.putString(STATE_INITIAL_FILTER, initialFilter.toJson());
    }
  }

  private void initToolBar() {
    Toolbar toolbar = ActivityUtils.findViewById(this, R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
  }

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

  private boolean wasElementChanged() {
    SysItemFilter newFilter = getUpdatedFilter(false);
    SysItemFilter currentFilter = SysItemFilterRepository.getCurrentFilter(this);
    return !Objects.equals(newFilter, currentFilter);
  }

  /**
   * Discards current changes and moves back.
   */
  private void discardChangesAndNavigateUpFromTask() {
    NavUtils.navigateUpFromSameTask(this);
    overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
  }

  private void saveSysItemFilterAndFinish() {
    SysItemFilter filter = getUpdatedFilter(true);
    SysItemFilterRepository.saveFilter(this, filter, true);

    finish();
    overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
  }

  private SysItemFilter getUpdatedFilter(boolean setName) {
    SysItemFilter filter = SysItemFilterRepository.getFilterByUuid(this, initialFilter.getUuid());
    if (filter == null) {
      filter = SysItemFilter.empty();
    }
    List<SortFilter> sortFilters = sortFiltersAdapter.getItems();

    List<Integer> colors = colorsAdapter.getItems();

    filter
        .setSorts(sortFilters)
        .setCreatedDateFilter(new DatePeriodFilter(
            extractDateTime(createdStartDatePicker),
            extractDateTime(createdEndDatePicker)))
        .setLastEditedDateFilter(new DatePeriodFilter(
            extractDateTime(editedStartDatePicker),
            extractDateTime(editedEndDatePicker)))
        .setLastViewedDateFilter(new DatePeriodFilter(
            extractDateTime(viewedStartDatePicker),
            extractDateTime(viewedEndDatePicker)))
        .setColors(new HashSet<>(colors));

    if (setName) {
      String name = nameEditText.getText().toString();
      if (StringUtils.isBlank(name)) {
        name = getDefaultFilterName(filter);
      }

      filter.setName(name);
    }

    return filter;
  }

  private String getDefaultFilterName(SysItemFilter filter) {
    if (filter == null) {
      return getString(R.string.empty_filter_name);
    }

    List<SortFilter> sorts = filter.getSorts();
    if (CollectionUtils.isEmpty(filter.getColors()) &&
        CollectionUtils.isEmpty(sorts) &&
        (filter.getCreatedTimeFilter() == null || filter.getCreatedTimeFilter().isEmpty()) &&
        (filter.getLastViewedTimeFilter() == null || filter.getLastEditedTimeFilter().isEmpty()) &&
        (filter.getLastViewedTimeFilter() == null || filter.getLastViewedTimeFilter().isEmpty())) {
      return getString(R.string.empty_filter_name);
    }

    if (!CollectionUtils.isEmpty(sorts)) {
      SortFilter sortFilter = sorts.get(0);
      switch (sortFilter.getFilteredColumn()) {
        case TITLE:
          return getString(R.string.title_sort_name);
        case BODY:
          return getString(R.string.body_sort_name);
        case CREATED:
          return getString(R.string.created_sort_name);
        case EDITED:
          return getString(R.string.edited_sort_name);
        case VIEWED:
          return getString(R.string.viewed_sort_name);
      }
    }

    if (!CollectionUtils.isEmpty(filter.getColors())) {
      return getString(R.string.color_filter_name);
    }

    return getString(R.string.date_filter_name);
  }

  @Nullable
  private DateTime extractDateTime(EditText editText) {
    String date = editText.getText().toString();
    if (StringUtils.isBlank(date)) {
      return null;
    }

    DateTimeFormatter formatter = getDateTimeFormatter();
    try {
      return formatter.parseDateTime(date);
    } catch (Exception e) {
      return null;
    }
  }

  private DateTimeFormatter getDateTimeFormatter() {
    return DateTimeFormat.forPattern(DATE_FORMAT_STRING);
  }

  private static String makeExtraIdent(String name) {
    return FilterEditorActivity.class.getCanonicalName() + "." + name;
  }
}
