package com.awesoon.thirdtask.activity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import com.awesoon.thirdtask.R;
import com.awesoon.thirdtask.util.ActivityUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class FilterEditorActivity extends AppCompatActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_filter_editor);
    overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
    Toolbar toolbar = ActivityUtils.findViewById(this, R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    final Calendar myCalendar = Calendar.getInstance();

    final EditText edittext = ActivityUtils.findViewById(this, R.id.created_date_start_filter, "R.id.created_date_start_filter");

    final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
      @Override
      public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        myCalendar.set(Calendar.YEAR, year);
        myCalendar.set(Calendar.MONTH, monthOfYear);
        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        updateLabel();
      }

      private void updateLabel() {

        String myFormat = "MM/dd/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        edittext.setText(sdf.format(myCalendar.getTime()));
      }
    };

    edittext.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

        new DatePickerDialog(FilterEditorActivity.this, date,
            myCalendar.get(Calendar.YEAR),
            myCalendar.get(Calendar.MONTH),
            myCalendar.get(Calendar.DAY_OF_MONTH)).show();
      }
    });
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    switch (id) {
      case android.R.id.home:
        NavUtils.navigateUpFromSameTask(this);
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        return true;
    }

    return super.onOptionsItemSelected(item);
  }
}
