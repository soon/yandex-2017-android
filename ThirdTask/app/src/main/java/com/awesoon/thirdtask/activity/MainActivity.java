package com.awesoon.thirdtask.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.awesoon.thirdtask.R;
import com.awesoon.thirdtask.db.DbHelper;
import com.awesoon.thirdtask.db.GlobalDbState;
import com.awesoon.thirdtask.domain.SysItem;
import com.awesoon.thirdtask.event.DbStateChangeListener;
import com.awesoon.thirdtask.util.Assert;
import com.awesoon.thirdtask.view.SysItemsAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
  private static final String TAG = "MainActivity";

  public static final int ADD_NEW_SYS_ITEM_REQUEST_CODE = 1;
  public static final int EDIT_EXISTING_SYS_ITEM_REQUEST_CODE = 2;

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

    ListView elementsList = getElementsList();
    SysItemsAdapter adapter = new SysItemsAdapter(this, R.layout.element_view, new ArrayList<SysItem>());
    elementsList.setAdapter(adapter);
    elementsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SysItem sysItem = (SysItem) parent.getItemAtPosition(position);
        openElementEditorActivity(sysItem.getId());
      }
    });

    this.dbHelper = new DbHelper(this);
    GlobalDbState.subscribe(this, new DbStateChangeListener() {
      @Override
      public void onSysItemAdded(SysItem sysItem) {
        new GetAllSysItemsTask(MainActivity.this, dbHelper).execute();
      }

      @Override
      public void onSysItemUpdated(SysItem sysItem) {
        new GetAllSysItemsTask(MainActivity.this, dbHelper).execute();
      }

      @Override
      public void onSysItemDeleted(Long id) {
        new GetAllSysItemsTask(MainActivity.this, dbHelper).execute();
      }
    });

    new GetAllSysItemsTask(MainActivity.this, dbHelper).execute();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    GlobalDbState.unsubscribe(this);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
      case ADD_NEW_SYS_ITEM_REQUEST_CODE:
        // the data updated via GlobalDbState
        break;
      case EDIT_EXISTING_SYS_ITEM_REQUEST_CODE:
        // the data updated via GlobalDbState
        break;
      default:
        Log.w(TAG, "Received unknown request code " + requestCode);
    }
  }

  private void updateListData(List<SysItem> sysItems) {
    SysItemsAdapter adapter = getElementsAdapter();
    adapter.clear();
    adapter.addAll(sysItems);
  }

  private SysItemsAdapter getElementsAdapter() {
    ListView elementsList = getElementsList();
    return (SysItemsAdapter) elementsList.getAdapter();
  }

  private void openNewElementEditorActivity() {
    openElementEditorActivity(null);
  }

  private void openElementEditorActivity(Long sysItemId) {
    Intent intent = new Intent(MainActivity.this, ElementEditorActivity.class);
    if (sysItemId != null) {
      intent.putExtra(ElementEditorActivity.EXTRA_SYS_ITEM_ID, sysItemId.longValue());
      startActivityForResult(intent, EDIT_EXISTING_SYS_ITEM_REQUEST_CODE);
    } else {
      startActivityForResult(intent, ADD_NEW_SYS_ITEM_REQUEST_CODE);
    }
  }

  private void setSysItems(List<SysItem> sysItems) {
    updateListData(sysItems);
  }

  private ListView getElementsList() {
    return findViewById(R.id.elements_list, "R.id.elements_list");
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
