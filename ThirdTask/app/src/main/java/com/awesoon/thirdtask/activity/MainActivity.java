package com.awesoon.thirdtask.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.awesoon.thirdtask.NotesApplication;
import com.awesoon.thirdtask.R;
import com.awesoon.thirdtask.db.DbHelper;
import com.awesoon.thirdtask.db.GlobalDbState;
import com.awesoon.thirdtask.domain.SysItem;
import com.awesoon.thirdtask.event.DbStateChangeListener;
import com.awesoon.thirdtask.event.SysItemRemoveListener;
import com.awesoon.thirdtask.repository.FilteredItemsContainer;
import com.awesoon.thirdtask.repository.SysItemFilterRepository;
import com.awesoon.thirdtask.repository.SysItemRepository;
import com.awesoon.thirdtask.repository.filter.SysItemFilter;
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
  public static final String STATE_FILTER_QUERY_TEXT = makeExtraIdent("STATE_FILTER_QUERY_TEXT");

  private Integer defaultItemColor;
  private ListView elementsList;
  private SysItemsAdapter sysItemsAdapter;
  private DrawerLayout drawerLayout;
  private NavigationView drawerNavView;
  private String filterQueryText;

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
        refreshData(dbHelper);
      }

      @Override
      public void onSysItemUpdated(SysItem sysItem) {
        refreshData(dbHelper);
      }

      @Override
      public void onSysItemDeleted(Long id) {
        refreshData(dbHelper);
      }

      @Override
      public void onSysItemsAdded(List<SysItem> sysItems) {
        refreshData(dbHelper);
      }
    });

    refreshData(dbHelper);
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    if (defaultItemColor != null) {
      outState.putInt(STATE_DEFAULT_ITEM_COLOR, defaultItemColor);
    }
    if (filterQueryText != null) {
      outState.putString(STATE_FILTER_QUERY_TEXT, filterQueryText);
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
      case EDIT_FILTER_ACTIVITY:
        refreshData();
        break;
      default:
        Log.w(TAG, "Received unknown request code " + requestCode);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_main_activity, menu);

    final MenuItem searchViewMenuItem = menu.findItem(R.id.menu_search);
    final SearchView searchView = (SearchView) searchViewMenuItem.getActionView();

    searchView.setQueryHint(getResources().getString(R.string.search_hint));
    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(String query) {
        applyFilterText(query);
        return true;
      }

      @Override
      public boolean onQueryTextChange(String newText) {
        applyFilterText(newText);
        return false;
      }
    });

    final String currentFilterQuery = filterQueryText;
    MenuItemCompat.setOnActionExpandListener(searchViewMenuItem, new MenuItemCompat.OnActionExpandListener() {
      @Override
      public boolean onMenuItemActionExpand(MenuItem item) {
        if (item.getActionView() instanceof SearchView) {
          final SearchView searchView = (SearchView) item.getActionView();
          if (currentFilterQuery != null) {
            searchView.post(new Runnable() {
              @Override
              public void run() {
                searchView.setQuery(currentFilterQuery, false);
              }
            });
          }
        }

        return true;
      }

      @Override
      public boolean onMenuItemActionCollapse(MenuItem item) {
        return true;
      }
    });

    if (currentFilterQuery != null) {
      MenuItemCompat.expandActionView(searchViewMenuItem);
    }

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
   * Refreshes the list data.
   */
  private void refreshData() {
    NotesApplication app = (NotesApplication) getApplication();
    final DbHelper dbHelper = app.getDbHelper();
    refreshData(dbHelper);
  }

  /**
   * Refreshes the list data.
   *
   * @param dbHelper A db helper.
   */
  private void refreshData(DbHelper dbHelper) {
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
    if (savedInstanceState == null) {
      return;
    }

    if (savedInstanceState.containsKey(STATE_DEFAULT_ITEM_COLOR)) {
      defaultItemColor = savedInstanceState.getInt(STATE_DEFAULT_ITEM_COLOR);
    }
    if (savedInstanceState.containsKey(STATE_FILTER_QUERY_TEXT)) {
      applyFilterText(savedInstanceState.getString(STATE_FILTER_QUERY_TEXT));
    }
  }

  private void initElementsList() {
    NotesApplication app = (NotesApplication) getApplication();
    final DbHelper dbHelper = app.getDbHelper();

    sysItemsAdapter = new SysItemsAdapter(this, R.layout.element_view, new ArrayList<SysItem>(),
        R.string.remove_sys_item_dialog_message, R.string.yes, R.string.no);

    sysItemsAdapter.addOnSysItemRemoveListener(new SysItemRemoveListener() {
      @Override
      public void onSysItemRemove(SysItem sysItem, int position) {
        new RemoveSysItemTask(dbHelper).execute(sysItem.getId());
      }
    });

    elementsList.setAdapter(sysItemsAdapter);
    elementsList.setTextFilterEnabled(true);
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

  private void clearFilterText() {
    applyFilterText(null);
  }

  private void applyFilterText(String text) {
    if (TextUtils.isEmpty(text)) {
      elementsList.clearTextFilter();
      filterQueryText = null;
    } else {
      elementsList.setFilterText(text);
      filterQueryText = text;
    }
  }

  /**
   * Updates current list data.
   *
   * @param sysItems New list data.
   */
  private void updateListData(List<SysItem> sysItems) {
    sysItemsAdapter.clear();

    if (sysItems.isEmpty()) {
      List<SysItem> defaultItems = getDefaultSysItems();
      sysItemsAdapter.addAll(defaultItems);
    } else {
      sysItemsAdapter.addAll(sysItems);
    }
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
    Intent intent = FilterEditorActivity.getInstance(this);
    startActivityForResult(intent, EDIT_FILTER_ACTIVITY);
  }

  /**
   * Sets new sys items to the list view.
   *
   * @param items Items.
   */
  private void setSysItems(FilteredItemsContainer items) {
    List<SysItem> filteredItems = items.getFilteredItems();
    List<SysItem> originalItems = items.getOriginalItems();
    if (filteredItems != null) {
      int delta = originalItems.size() - filteredItems.size();

      if (delta > 0) {
        Context context = getApplicationContext();
        String message = getResources().getQuantityString(R.plurals.some_notes_are_hidden, delta, delta);
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, message, duration);
        toast.show();
      }
    }

    updateListData(filteredItems);
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
  private static class GetAllSysItemsTask extends AsyncTask<Void, Void, FilteredItemsContainer> {
    private MainActivity activity;
    private DbHelper dbHelper;
    private SysItemFilter filter;

    public GetAllSysItemsTask(MainActivity activity, DbHelper dbHelper) {
      this.activity = activity;
      this.dbHelper = dbHelper;
    }

    @Override
    protected FilteredItemsContainer doInBackground(Void... params) {
      filter = SysItemFilterRepository.getCurrentFilter(activity);
      FilteredItemsContainer filteredItems = SysItemRepository.getAllItemsFiltered(dbHelper, filter);
      return filteredItems;
    }

    @Override
    protected void onPostExecute(final FilteredItemsContainer items) {
      activity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          activity.setSysItems(items);
        }
      });
    }
  }
}
