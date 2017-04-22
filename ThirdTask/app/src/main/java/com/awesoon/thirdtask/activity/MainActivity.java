package com.awesoon.thirdtask.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.awesoon.thirdtask.R;
import com.awesoon.thirdtask.db.DbHelper;
import com.awesoon.thirdtask.domain.SysItem;
import com.awesoon.thirdtask.util.Assert;

import java.util.List;

public class MainActivity extends AppCompatActivity {
  private DbHelper dbHelper;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = findViewById(R.id.toolbar, "R.id.toolbar");
    setSupportActionBar(toolbar);

    FloatingActionButton fab = findViewById(R.id.fab, "R.id.fab");
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        openNewElementEditorActivity();
      }
    });

    this.dbHelper = new DbHelper(this);
    new GetAllSysItemsTask(this, dbHelper).execute();
  }

  private void openNewElementEditorActivity() {
    openElementEditorActivity(null);
  }

  private void openElementEditorActivity(Long sysItemId) {
    Intent intent = new Intent(MainActivity.this, ElementEditorActivity.class);
    if (sysItemId != null) {
      intent.putExtra(ElementEditorActivity.EXTRA_SYS_ITEM_ID, sysItemId.longValue());
    }
    startActivity(intent);
  }

  private void setSysItems(List<SysItem> sysItems) {
    int a = 2;
  }

  private <T> T findViewById(int id, String name) {
    View view = findViewById(id);
    Assert.notNull(view, "Unable to find view " + name);
    return (T) view;
  }

  private static class GetAllSysItemsTask extends AsyncTask<Void, Void, List<SysItem>> {
    private MainActivity activity;
    private DbHelper dbHelper;

    public GetAllSysItemsTask(MainActivity activity, DbHelper dbHelper) {
      this.activity = activity;
      this.dbHelper = dbHelper;
    }

    @Override
    protected List<SysItem> doInBackground(Void... params) {
      return dbHelper.findAllSysItems();
    }

    @Override
    protected void onPostExecute(final List<SysItem> sysItems) {
      activity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          activity.setSysItems(sysItems);
        }
      });

    }
  }
}
