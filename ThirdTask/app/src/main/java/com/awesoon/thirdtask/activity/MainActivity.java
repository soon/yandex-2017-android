package com.awesoon.thirdtask.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.awesoon.thirdtask.NotesApplication;
import com.awesoon.thirdtask.R;
import com.awesoon.thirdtask.db.DbHelper;
import com.awesoon.thirdtask.db.GlobalDbState;
import com.awesoon.thirdtask.domain.SysItem;
import com.awesoon.thirdtask.event.DbStateChangeListener;
import com.awesoon.thirdtask.event.SysItemRemoveListener;
import com.awesoon.thirdtask.repository.SysItemRepository;
import com.awesoon.thirdtask.util.Action;
import com.awesoon.thirdtask.util.ActivityUtils;
import com.awesoon.thirdtask.util.Assert;
import com.awesoon.thirdtask.util.BeautifulColors;
import com.awesoon.thirdtask.util.Consumer;
import com.awesoon.thirdtask.util.PermissionUtils;
import com.awesoon.thirdtask.view.SysItemsAdapter;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {
  private static final String TAG = "MainActivity";
  private static final String NOTES_FILE_PATH = "/storage/emulated/0/itemlist.ili";

  public static final int ADD_NEW_SYS_ITEM_REQUEST_CODE = 1;
  public static final int EDIT_EXISTING_SYS_ITEM_REQUEST_CODE = 2;
  public static final int EDIT_FILTER_ACTIVITY = 3;

  public static final String STATE_DEFAULT_ITEM_COLOR = makeExtraIdent("STATE_DEFAULT_ITEM_COLOR");

  private Integer defaultItemColor;
  private ListView elementsList;
  private DrawerLayout drawerLayout;
  private NavigationView drawerNavView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    initMembers();
    initState(savedInstanceState);

    initToolbar();
    initFab();

    drawerNavView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
      @Override
      public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
          case R.id.drawer_store_notes_to_file:
            storeNotesToFile();
            return true;
          case R.id.drawer_load_notes_from_file:
            loadNotesFromFile();
            return true;
        }

        return false;
      }
    });

    initElementsList();

    NotesApplication app = (NotesApplication) getApplication();
    final DbHelper dbHelper = app.getDbHelper();

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

      @Override
      public void onSysItemsAdded(List<SysItem> sysItems) {
        new GetAllSysItemsTask(MainActivity.this, dbHelper).execute();
      }
    });

    new GetAllSysItemsTask(MainActivity.this, dbHelper).execute();
  }

  /**
   * Loads notes from the file.
   * Also checks user permissions and shows dialog if a user removes previously added notes.
   */
  private void loadNotesFromFile() {
    if (!PermissionUtils.requestWriteExternalStoragePermissionIfNecessary(this, READ_EXTERNAL_STORAGE)) {
      return;
    }

    NotesApplication app = (NotesApplication) getApplication();
    final DbHelper dbHelper = app.getDbHelper();
    dbHelper.findAllSysItemsAsync(new Consumer<List<SysItem>>() {
      @Override
      public void apply(final List<SysItem> items) {
        if (items.isEmpty()) {
          doLoadNotesFromFile(dbHelper);
        } else {
          String message = getResources()
              .getQuantityString(R.plurals.are_you_sure_you_want_to_delete_n_notes, items.size(), items.size());
          new AlertDialog.Builder(MainActivity.this)
              .setTitle(R.string.all_notes_will_be_deleted)
              .setMessage(message)
              .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                  doLoadNotesFromFile(dbHelper);
                }
              })
              .setNegativeButton(R.string.cancel, null)
              .show();
        }
      }
    });
  }

  /**
   * Internal method. Performs notes loading from the file.
   *
   * @param dbHelper A db helper instance.
   */
  private void doLoadNotesFromFile(final DbHelper dbHelper) {
    final Consumer<Exception> exceptionConsumer = new Consumer<Exception>() {
      @Override
      public void apply(Exception e) {

        new AlertDialog.Builder(MainActivity.this)
            .setTitle(R.string.error_occurred_while_importing_notes_title)
            .setMessage(R.string.error_occurred_while_importing_notes_message)
            .setPositiveButton(R.string.ok, null)
            .show();
      }
    };

    SysItemRepository.loadAllItemsFromFileAsync(NOTES_FILE_PATH, new Consumer<List<SysItem>>() {
      @Override
      public void apply(final List<SysItem> importedItems) {
        if (importedItems == null || importedItems.isEmpty()) {
          new AlertDialog.Builder(MainActivity.this)
              .setTitle(R.string.unable_to_find_notes_to_import_title)
              .setMessage(R.string.unable_to_find_notes_to_import_message)
              .setPositiveButton(R.string.ok, null)
              .show();
          return;
        }

        dbHelper.removeAllSysItemsAsync(new Action() {
          @Override
          public void call() {
            dbHelper.addSysItemsAsync(importedItems, new Consumer<List<SysItem>>() {
              @Override
              public void apply(List<SysItem> sysItems) {
                String message = getResources()
                    .getQuantityString(R.plurals.notes_have_been_imported_message, sysItems.size(), sysItems.size());

                new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.notes_have_been_imported_title)
                    .setMessage(message)
                    .setPositiveButton(R.string.ok, null)
                    .show();
              }
            }, exceptionConsumer);
          }
        });
      }
    }, exceptionConsumer);
  }

  /**
   * Stores current notes to the file.
   * Also checks user permission.
   */
  private void storeNotesToFile() {
    if (!PermissionUtils.requestWriteExternalStoragePermissionIfNecessary(this, WRITE_EXTERNAL_STORAGE)) {
      return;
    }

    NotesApplication app = (NotesApplication) getApplication();
    DbHelper dbHelper = app.getDbHelper();
    dbHelper.findAllSysItemsAsync(new Consumer<List<SysItem>>() {
      @Override
      public void apply(List<SysItem> items) {
        doStoreNotesToFile(items);
      }
    });
  }

  /**
   * Internal method. Performs notes storing to the file.
   *
   * @param items A notes to store.
   */
  private void doStoreNotesToFile(final List<SysItem> items) {
    Assert.notNull(items, "items must not be null");

    if (items.isEmpty()) {
      new AlertDialog.Builder(MainActivity.this)
          .setTitle(R.string.you_do_not_have_notes)
          .setMessage(R.string.do_you_want_to_add_one)
          .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              openNewElementEditorActivity();
            }
          })
          .setNegativeButton(R.string.cancel, null)
          .show();
      return;
    }

    SysItemRepository.storeAllItemsToFileAsync(items, NOTES_FILE_PATH, new Action() {
      @Override
      public void call() {
        String message = getResources()
            .getQuantityString(R.plurals.notes_have_been_saved_message, items.size(), items.size());
        new AlertDialog.Builder(MainActivity.this)
            .setTitle(R.string.notes_have_been_saved_title)
            .setMessage(message)
            .setPositiveButton(R.string.ok, null)
            .show();
      }
    }, new Consumer<Exception>() {
      @Override
      public void apply(Exception e) {
        new AlertDialog.Builder(MainActivity.this)
            .setTitle(R.string.notes_have_not_been_saved_title)
            .setMessage(R.string.notes_have_not_been_saved_message)
            .setPositiveButton(R.string.ok, null)
            .show();
      }
    });
  }

  private void initState(Bundle savedInstanceState) {
    if (savedInstanceState != null && savedInstanceState.containsKey(STATE_DEFAULT_ITEM_COLOR)) {
      defaultItemColor = savedInstanceState.getInt(STATE_DEFAULT_ITEM_COLOR);
    }
  }

  private void initElementsList() {
    NotesApplication app = (NotesApplication) getApplication();
    final DbHelper dbHelper = app.getDbHelper();

    SysItemsAdapter adapter = new SysItemsAdapter(this, R.layout.element_view, new ArrayList<SysItem>(),
        R.string.remove_sys_item_dialog_message, R.string.yes, R.string.no);

    adapter.addOnSysItemRemoveListener(new SysItemRemoveListener() {
      @Override
      public void onSysItemRemove(SysItem sysItem, int position) {
        new RemoveSysItemTask(dbHelper).execute(sysItem.getId());
      }
    });

    elementsList.setAdapter(adapter);
    elementsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SysItem sysItem = (SysItem) parent.getItemAtPosition(position);
        openElementEditorActivity(sysItem.getId());
      }
    });
  }

  private void initFab() {
    FloatingActionButton fab = ActivityUtils.findViewById(this, R.id.fab, "R.id.fab");
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        openNewElementEditorActivity();
      }
    });
  }

  private void initToolbar() {
    Toolbar toolbar = ActivityUtils.findViewById(this, R.id.toolbar, "R.id.toolbar");
    setSupportActionBar(toolbar);
  }

  private void initMembers() {
    elementsList = ActivityUtils.findViewById(this, R.id.elements_list, "R.id.elements_list");
    drawerLayout = ActivityUtils.findViewById(this, R.id.drawer_layout, "R.id.drawer_layout");
    drawerNavView = ActivityUtils.findViewById(this, R.id.drawer_nav_view, "R.id.drawer_nav_view");
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    if (defaultItemColor != null) {
      outState.putInt(STATE_DEFAULT_ITEM_COLOR, defaultItemColor);
    }
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

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_main_activity, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    switch (id) {
      case R.id.edit_filter:
        openFilterEditorActivity();
        return true;
    }

    return super.onOptionsItemSelected(item);
  }

  /**
   * Updates current list data.
   *
   * @param sysItems New list data.
   */
  private void updateListData(List<SysItem> sysItems) {
    SysItemsAdapter adapter = getElementsAdapter();
    adapter.clear();

    if (sysItems.isEmpty()) {
      List<SysItem> defaultItems = getDefaultSysItems();
      adapter.addAll(defaultItems);
    } else {
      adapter.addAll(sysItems);
    }
  }

  /**
   * Retrieves list view elements adapter.
   *
   * @return List view adapter.
   */
  private SysItemsAdapter getElementsAdapter() {
    return (SysItemsAdapter) elementsList.getAdapter();
  }

  /**
   * Opens element editor without initial sys item.
   */
  private void openNewElementEditorActivity() {
    openElementEditorActivity(null);
  }

  /**
   * Opens element editor with the given id as the initial state.
   *
   * @param sysItemId Sys item id. Nullable.
   */
  private void openElementEditorActivity(Long sysItemId) {
    Intent intent = ElementEditorActivity.getInstance(this, sysItemId);
    if (sysItemId != null) {
      startActivityForResult(intent, EDIT_EXISTING_SYS_ITEM_REQUEST_CODE);
    } else {
      startActivityForResult(intent, ADD_NEW_SYS_ITEM_REQUEST_CODE);
    }
  }

  private void openFilterEditorActivity() {
    Intent intent = new Intent(this, FilterEditorActivity.class);
    startActivityForResult(intent, EDIT_FILTER_ACTIVITY);
  }

  /**
   * Sets new sys items to the list view.
   *
   * @param sysItems A new list view data.
   */
  private void setSysItems(List<SysItem> sysItems) {
    updateListData(sysItems);
  }

  /**
   * Generates an ident.
   *
   * @param name Ident name.
   * @return Full ident name.
   */
  public static String makeExtraIdent(String name) {
    return "com.awesoon.thirdtask.activity.MainActivity." + name;
  }

  /**
   * Retrieves a list of default sys items (when the list view is empty).
   *
   * @return A list of default sys items.
   */
  public List<SysItem> getDefaultSysItems() {
    List<SysItem> items = new ArrayList<>();
    if (defaultItemColor == null) {
      defaultItemColor = BeautifulColors.getBeautifulColor();
    }

    SysItem item = new SysItem()
        .setTitle(getString(R.string.default_sys_item_title))
        .setBody(getString(R.string.default_sys_item_body))
        .setColor(defaultItemColor);
    items.add(item);
    return items;
  }

  /**
   * Removes sys item from the database.
   */
  private static class RemoveSysItemTask extends AsyncTask<Long, Void, Void> {
    private DbHelper dbHelper;

    public RemoveSysItemTask(DbHelper dbHelper) {
      this.dbHelper = dbHelper;
    }

    @Override
    protected Void doInBackground(Long... params) {
      Assert.notEmpty(params, "Should pass at least one id");
      Long id = params[0];
      Assert.notNull(id, "Id must not be null");
      dbHelper.removeSysItemById(id);
      return null;
    }
  }

  /**
   * Retrieves all sys items from the database. Calls setSysItems once finished.
   */
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
