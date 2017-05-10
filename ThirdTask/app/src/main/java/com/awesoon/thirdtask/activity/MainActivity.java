package com.awesoon.thirdtask.activity;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.awesoon.core.async.AsyncTaskBuilder;
import com.awesoon.core.async.AsyncTaskProducer;
import com.awesoon.core.sql.FilteredPage;
import com.awesoon.core.sql.Page;
import com.awesoon.core.sql.PageRequest;
import com.awesoon.thirdtask.NotesApplication;
import com.awesoon.thirdtask.R;
import com.awesoon.thirdtask.activity.listener.DebouncedQueryTextListener;
import com.awesoon.thirdtask.activity.listener.EndlessRecyclerViewScrollListener;
import com.awesoon.thirdtask.db.DbHelper;
import com.awesoon.thirdtask.db.GlobalDbState;
import com.awesoon.thirdtask.domain.SysItem;
import com.awesoon.thirdtask.event.DbStateChangeListener;
import com.awesoon.thirdtask.event.SysItemRemoveListener;
import com.awesoon.thirdtask.repository.SysItemFilterRepository;
import com.awesoon.thirdtask.repository.SysItemRepository;
import com.awesoon.thirdtask.repository.filter.SysItemFilter;
import com.awesoon.thirdtask.util.ActivityUtils;
import com.awesoon.thirdtask.util.Assert;
import com.awesoon.thirdtask.util.BeautifulColors;
import com.awesoon.thirdtask.util.BiPredicate;
import com.awesoon.thirdtask.util.CollectionUtils;
import com.awesoon.thirdtask.util.Consumer;
import com.awesoon.thirdtask.util.Function;
import com.awesoon.thirdtask.util.NumberUtils;
import com.awesoon.thirdtask.util.PermissionUtils;
import com.awesoon.thirdtask.util.StringUtils;
import com.awesoon.thirdtask.view.SysItemsAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {
  private static final String TAG = "MainActivity";
  private static final int SEARCH_TOAST_TOP_OFFSET_DPI = 64;
  public static final int NOTES_PAGE_SIZE = 25;

  public static final int ADD_NEW_SYS_ITEM_REQUEST_CODE = 1;
  public static final int EDIT_EXISTING_SYS_ITEM_REQUEST_CODE = 2;
  public static final int EDIT_FILTER_REQUEST_CODE = 3;
  public static final int SELECT_FILE_TO_EXPORT_REQUEST_CODE = 4;
  public static final int SELECT_FILE_TO_IMPORT_REQUEST_CODE = 5;

  public static final String STATE_DEFAULT_ITEM_COLOR = makeExtraIdent("STATE_DEFAULT_ITEM_COLOR");
  public static final String STATE_FILTER_QUERY_TEXT = makeExtraIdent("STATE_FILTER_QUERY_TEXT");

  private Integer defaultItemColor;
  private RecyclerView elementsList;
  private SysItemsAdapter sysItemsAdapter;
  private NavigationView drawerNavView;
  private String filterQueryText;
  private Toast searchToast;
  private String searchToastMessage;
  private EndlessRecyclerViewScrollListener endlessRecyclerViewScrollListener;
  private ProgressBar elementsListProgressBar;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

    setContentView(R.layout.activity_main);

    initMembers();
    initToolbar();
    initFab();

    initState(savedInstanceState);

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
          case R.id.drawer_select_filter:
            showSelectFilterDialog();
            return true;
          case R.id.drawer_remove_filters:
            showRemoveFiltersDialog();
            return true;
          case R.id.drawer_add_filter:
            openNewFilterEditorActivity();
            return true;
          case R.id.generate_notes:
            generateNotes();
            return true;
          case R.id.remove_all_notes:
            removeAllNotes();
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

  private void removeAllNotes() {
    NotesApplication app = (NotesApplication) getApplication();
    final DbHelper dbHelper = app.getDbHelper();
    AsyncTaskBuilder.firstly(new AsyncTaskProducer<Integer>() {
      @Override
      public Integer doApply() {
        return dbHelper.removeAllSysItems();
      }
    }, new Consumer<Integer>() {
      @Override
      public void apply(Integer removedNotesCount) {
        Assert.notNull(removedNotesCount, "removedNotesCount must not be null");
        refreshData(dbHelper);
        showAllNotesRemovedDialog(removedNotesCount);
      }
    }).build().execute();
  }

  private void showAllNotesRemovedDialog(int removedNotesCount) {
    String message = getResources()
        .getQuantityString(R.plurals.removed_n_notes, removedNotesCount, removedNotesCount);
    new AlertDialog.Builder(this)
        .setTitle(message)
        .setPositiveButton(R.string.ok, null)
        .show();
  }

  private void generateNotes() {
    new AlertDialog.Builder(this)
        .setTitle(getString(R.string.started_notes_generation))
        .setPositiveButton(R.string.ok, null)
        .show();

    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this);
    builder.setContentTitle(getString(R.string.notes_generation_title))
        .setContentText(getString(R.string.notes_generation_text))
        .setSmallIcon(R.drawable.ic_note_add_white_24dp);

    NotesApplication app = (NotesApplication) getApplication();
    DbHelper dbHelper = app.getDbHelper();
    new SysItemsGenerator(notificationManager, builder, this, dbHelper,
        getString(R.string.notes_generation_progress), getString(R.string.notes_saving_progress), 100_000).execute();
  }

  private void showRemoveFiltersDialog() {
    final List<SysItemFilter> allFilters = SysItemFilterRepository.getAllFilters(this);
    if (allFilters.isEmpty()) {
      showThereAreNoFiltersDialog();
      return;
    }

    String[] items = CollectionUtils.mapToArray(allFilters, String.class, new Function<SysItemFilter, String>() {
      @Override
      public String apply(SysItemFilter filter) {
        return filter == null || filter.getName() == null ? getString(R.string.empty_filter_name) : filter.getName();
      }
    });

    final Set<Integer> itemIndicesToRemove = new HashSet<>();
    new AlertDialog.Builder(this)
        .setMultiChoiceItems(items, null, new DialogInterface.OnMultiChoiceClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which, boolean isChecked) {
            if (isChecked) {
              itemIndicesToRemove.add(which);
            } else {
              itemIndicesToRemove.remove(which);
            }
          }
        })
        .setPositiveButton(R.string.remove, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            showRemoveFiltersDialog(allFilters, itemIndicesToRemove);
          }
        })
        .setNegativeButton(R.string.cancel, null)
        .show();
  }

  private void showThereAreNoFiltersDialog() {
    new AlertDialog.Builder(this)
        .setTitle(R.string.you_do_not_have_filters)
        .setMessage(R.string.do_you_want_to_add_one_filter)
        .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            openNewFilterEditorActivity();
          }
        })
        .setNegativeButton(R.string.cancel, null)
        .show();
  }

  private void showRemoveFiltersDialog(List<SysItemFilter> allFilters, Set<Integer> itemIndicesToRemove) {
    if (CollectionUtils.isEmpty(itemIndicesToRemove)) {
      return;
    }

    final Set<UUID> uuidsToRemove = new HashSet<>();
    for (Integer idx : itemIndicesToRemove) {
      UUID uuid = allFilters.get(idx).getUuid();
      if (uuid != null) {
        uuidsToRemove.add(uuid);
      }
    }

    if (uuidsToRemove.isEmpty()) {
      return;
    }

    String message = getResources()
        .getQuantityString(R.plurals.are_you_sure_you_want_to_delete_n_items,
            itemIndicesToRemove.size(), itemIndicesToRemove.size());

    new AlertDialog.Builder(this)
        .setTitle(R.string.remove_filters_title)
        .setMessage(message)
        .setPositiveButton(R.string.remove, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            SysItemFilterRepository.removeFiltersByUuids(MainActivity.this, uuidsToRemove);
            refreshData();
          }
        })
        .setNegativeButton(R.string.cancel, null)
        .show();
  }

  private void showSelectFilterDialog() {
    final List<SysItemFilter> allFilters = SysItemFilterRepository.getAllFilters(this);
    if (allFilters.isEmpty()) {
      showThereAreNoFiltersDialog();
      return;
    }
    allFilters.add(0, new SysItemFilter(getString(R.string.no_filter_name)));

    UUID currentFilterUuid = SysItemFilterRepository.getCurrentFilterUuid(this);
    int selectedIndex = CollectionUtils.indexOf(allFilters, currentFilterUuid, new BiPredicate<SysItemFilter, UUID>() {
      @Override
      public boolean apply(SysItemFilter filter, UUID uuid) {
        return filter != null && Objects.equals(filter.getUuid(), uuid);
      }
    });
    if (selectedIndex == -1) {
      selectedIndex = 0;
    }
    String[] items = CollectionUtils.mapToArray(allFilters, String.class, new Function<SysItemFilter, String>() {
      @Override
      public String apply(SysItemFilter filter) {
        return filter == null || filter.getName() == null ? getString(R.string.empty_filter_name) : filter.getName();
      }
    });

    new AlertDialog.Builder(this)
        .setSingleChoiceItems(items, selectedIndex, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            SysItemFilterRepository.setCurrentFilter(MainActivity.this, allFilters.get(which).getUuid());
            refreshData();
            dialog.dismiss();
          }
        })
        .setNegativeButton(R.string.cancel, null)
        .show();
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
      case EDIT_FILTER_REQUEST_CODE:
        refreshData();
        break;
      case SELECT_FILE_TO_EXPORT_REQUEST_CODE:
        handleExportNotesToFile(resultCode, data);
        break;
      case SELECT_FILE_TO_IMPORT_REQUEST_CODE:
        handleImportNotesFromFile(resultCode, data);
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
    searchView.setOnQueryTextListener(new DebouncedQueryTextListener(500) {
      @Override
      public void debouncedQueryChangedOrSubmitted(String query) {
        applyFilterText(query);
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
        hideSearchToastMessage();
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

  private void setSearchToastMessage(@Nullable String message) {
    if (StringUtils.isBlank(message)) {
      hideSearchToastMessage();
    }

    searchToastMessage = StringUtils.trim(message);
    if (searchToast == null) {
      searchToast = Toast.makeText(this, searchToastMessage, Toast.LENGTH_LONG);
    }

    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

    int yOffset = (int) (SEARCH_TOAST_TOP_OFFSET_DPI * displayMetrics.density);

    searchToast.setText(searchToastMessage);
    searchToast.setGravity(Gravity.TOP | Gravity.CENTER, 0, yOffset);
    searchToast.show();
  }

  private void hideSearchToastMessage() {
    if (searchToast != null) {
      searchToast.cancel();
      searchToast = null;
      searchToastMessage = null;
    }
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

    openSelectFileToImportDialog();
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
    final DbHelper dbHelper = app.getDbHelper();
    AsyncTaskBuilder.firstly(new AsyncTaskProducer<Integer>() {
      @Override
      public Integer doApply() {
        return dbHelper.countAllItems();
      }
    }, new Consumer<Integer>() {
      @Override
      public void apply(Integer size) {
        if (size == 0) {
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
        } else {
          openSelectFileToExportDialog();
        }
      }
    }).build().execute();
  }

  private void handleExportNotesToFile(int resultCode, Intent data) {
    if (resultCode != RESULT_OK || data == null) {
      return;
    }

    Uri uri = data.getData();
    NotesApplication app = (NotesApplication) getApplication();
    final DbHelper dbHelper = app.getDbHelper();

    SysItemRepository.storeAllItemsToFileAsync(dbHelper, getContentResolver(), uri, new Consumer<List<SysItem>>() {
      @Override
      public void apply(List<SysItem> items) {
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

  private void handleImportNotesFromFile(int resultCode, Intent data) {
    if (resultCode != RESULT_OK || data == null) {
      return;
    }

    final Uri uri = data.getData();
    NotesApplication app = (NotesApplication) getApplication();
    final DbHelper dbHelper = app.getDbHelper();

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

    doImportNotesFromFile(uri, dbHelper, exceptionConsumer);
  }

  private void doImportNotesFromFile(Uri uri, final DbHelper dbHelper, final Consumer<Exception> exceptionConsumer) {
    SysItemRepository.loadAllItemsFromFileAsync(getContentResolver(), uri, new Consumer<List<SysItem>>() {
      @Override
      public void apply(final List<SysItem> importedItems) {
        if (importedItems == null || importedItems.isEmpty()) {
          showThereAreNoNotesToImportDialog();
          return;
        }

        AsyncTaskBuilder.firstly(new AsyncTaskProducer<Integer>() {
          @Override
          public Integer doApply() {
            return dbHelper.countAllItems();
          }
        }, new Consumer<Integer>() {
          @Override
          public void apply(Integer size) {
            if (size == 0) {
              replaceCurrentNotes(dbHelper, importedItems, exceptionConsumer);
              return;
            }

            showRemoveCurrentNotesDialog(dbHelper, size, importedItems, exceptionConsumer);
          }
        }).build().execute();
      }
    }, exceptionConsumer);
  }

  private void showRemoveCurrentNotesDialog(final DbHelper dbHelper, int currentItemsCount,
                                            final List<SysItem> importedItems,
                                            final Consumer<Exception> exceptionConsumer) {
    String message = getResources()
        .getQuantityString(R.plurals.are_you_sure_you_want_to_delete_n_notes, currentItemsCount, currentItemsCount);
    new AlertDialog.Builder(MainActivity.this)
        .setTitle(R.string.all_notes_will_be_deleted)
        .setMessage(message)
        .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            replaceCurrentNotes(dbHelper, importedItems, exceptionConsumer);
          }
        })
        .setNegativeButton(R.string.cancel, null)
        .show();
  }

  private void replaceCurrentNotes(DbHelper dbHelper, List<SysItem> importedItems,
                                   Consumer<Exception> exceptionConsumer) {

    dbHelper.replaceAllSysItemsAsync(importedItems, new Consumer<List<SysItem>>() {
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

  private void showThereAreNoNotesToImportDialog() {
    new AlertDialog.Builder(MainActivity.this)
        .setTitle(R.string.unable_to_find_notes_to_import_title)
        .setMessage(R.string.unable_to_find_notes_to_import_message)
        .setPositiveButton(R.string.ok, null)
        .show();
    return;
  }

  private void openSelectFileToExportDialog() {
    Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
    intent.addCategory(Intent.CATEGORY_OPENABLE);
    intent.setType("*/*");
    doStartActivityForResult(intent, SELECT_FILE_TO_EXPORT_REQUEST_CODE);
  }

  private void openSelectFileToImportDialog() {
    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
    intent.addCategory(Intent.CATEGORY_OPENABLE);
    intent.setType("*/*");
    doStartActivityForResult(intent, SELECT_FILE_TO_IMPORT_REQUEST_CODE);
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

    sysItemsAdapter.addOnItemClickListener(new Consumer<SysItem>() {
      @Override
      public void apply(SysItem sysItem) {
        openElementEditorActivity(sysItem.getId());
      }
    });

    elementsList.setAdapter(sysItemsAdapter);

    final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
    elementsList.setLayoutManager(linearLayoutManager);
    endlessRecyclerViewScrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
      @Override
      public void doLoadItems(int page, int totalItemsCount, RecyclerView view) {
        showItemsLoadingProgress();
        SysItemFilter filter = SysItemFilterRepository.getCurrentFilter(MainActivity.this);
        dbHelper.findSysItemsAsync(new PageRequest(page, NOTES_PAGE_SIZE), filterQueryText, filter, false,
            new Consumer<Page<SysItem>>() {
              @Override
              public void apply(Page<SysItem> sysItemPage) {
                if (!sysItemPage.isEmpty()) {
                  sysItemsAdapter.addAll(sysItemPage.getData());
                }
                hideItemsLoadingProgress();
              }
            });
      }
    };

    elementsList.addOnScrollListener(endlessRecyclerViewScrollListener);
  }

  private void showFoundNItemsToast(int size) {
    String message = getResources().getQuantityString(R.plurals.found_n_notes, size, size);
    setSearchToastMessage(message);
  }

  private void hideItemsLoadingProgress() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        elementsListProgressBar.setVisibility(View.GONE);
      }
    });
  }

  private void showItemsLoadingProgress() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        elementsListProgressBar.setVisibility(View.VISIBLE);
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
    drawerNavView = ActivityUtils.findViewById(this, R.id.drawer_nav_view, "R.id.drawer_nav_view");
    elementsListProgressBar = ActivityUtils.findViewById(this, R.id.elements_list_progress_bar,
        "R.id.elements_list_progress_bar");
  }

  private void applyFilterText(String text) {
    if (TextUtils.isEmpty(text)) {
      filterQueryText = null;
    } else {
      filterQueryText = text;
    }

    refreshData();
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

    endlessRecyclerViewScrollListener.clear();
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
      doStartActivityForResult(intent, EDIT_EXISTING_SYS_ITEM_REQUEST_CODE);
    } else {
      doStartActivityForResult(intent, ADD_NEW_SYS_ITEM_REQUEST_CODE);
    }
  }

  private void openNewFilterEditorActivity() {
    Intent intent = FilterEditorActivity.getInstance(this, true);
    doStartActivityForResult(intent, EDIT_FILTER_REQUEST_CODE);
  }

  private void openFilterEditorActivity() {
    Intent intent = FilterEditorActivity.getInstance(this, false);
    doStartActivityForResult(intent, EDIT_FILTER_REQUEST_CODE);
  }

  private void doStartActivityForResult(Intent intent, int requestCode) {
    leaveFromActivity();
    startActivityForResult(intent, requestCode);
  }

  private void leaveFromActivity() {
    hideSearchToastMessage();
  }

  /**
   * Sets new sys items to the list view.
   *
   * @param page Items.
   */
  private void setSysItems(FilteredPage<SysItem> page) {
    List<SysItem> filteredItems = page.getData();
    if (filteredItems != null && page.getNumber() == 0) {
      int delta = page.getTotalSourceElements() - page.getTotalElements();

      if (!StringUtils.isBlank(filterQueryText)) {
        showFoundNItemsToast(page.getTotalElements());
      } else if (delta > 0) {
        Context context = getApplicationContext();
        String message;
        if (delta >= 1000) {
          String shortDelta = NumberUtils.makeShortString(delta);
          message = getResources().getString(R.string.some_notes_are_hidden_short_number, shortDelta);
        } else {
          message = getResources().getQuantityString(R.plurals.some_notes_are_hidden, delta, delta);
        }

        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, message, duration);
        toast.show();
      }
    }

    updateListData(filteredItems);
    setTotalNotesCount(page.getTotalElements());
  }

  private void setTotalNotesCount(int totalNotesCount) {
    String title = getResources().getString(R.string.n_notes_title, NumberUtils.makeShortString(totalNotesCount));

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setTitle(title);
    }
  }

  /**
   * Generates an ident.
   *
   * @param name Ident name.
   * @return Full ident name.
   */
  public static String makeExtraIdent(String name) {
    return MainActivity.class.getCanonicalName() + "." + name;
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
  private static class GetAllSysItemsTask extends AsyncTask<Void, Void, FilteredPage<SysItem>> {
    private MainActivity activity;
    private DbHelper dbHelper;
    private SysItemFilter filter;
    private String filterQueryText;

    public GetAllSysItemsTask(MainActivity activity, DbHelper dbHelper) {
      this.activity = activity;
      this.dbHelper = dbHelper;
      this.filterQueryText = activity.filterQueryText;
    }

    @Override
    protected void onPreExecute() {
      activity.showItemsLoadingProgress();
    }

    @Override
    protected FilteredPage<SysItem> doInBackground(Void... params) {
      filter = SysItemFilterRepository.getCurrentFilter(activity);
      FilteredPage<SysItem> page = dbHelper.findSysItems(new PageRequest(0, 25), filterQueryText, filter, true);
      return page;
    }

    @Override
    protected void onPostExecute(final FilteredPage<SysItem> items) {
      if (Objects.equals(activity.filterQueryText, filterQueryText)) {
        activity.setSysItems(items);
        activity.hideItemsLoadingProgress();
      }
    }
  }

  private class SysItemsGenerator extends AsyncTask<Void, SysItemsGenerator.ProgressUpdate, Integer> {
    private static final String TAG = "SysItemsGenerator";

    private final NotificationManager notificationManager;
    private final NotificationCompat.Builder notificationBuilder;
    private final MainActivity mainActivity;
    private final DbHelper dbHelper;
    private final String notesGenerationProgress;
    private final String notesSavingProgress;
    private static final int ID = NotesApplication.NOTES_GENERATOR_NOTIFICATION_ID;
    private final int itemsCount;

    public SysItemsGenerator(NotificationManager notificationManager, NotificationCompat.Builder notificationBuilder,
                             MainActivity mainActivity, DbHelper dbHelper, String notesGenerationProgress,
                             String notesSavingProgress, int itemsCount) {
      this.notificationManager = notificationManager;
      this.notificationBuilder = notificationBuilder;
      this.mainActivity = mainActivity;
      this.dbHelper = dbHelper;
      this.notesGenerationProgress = notesGenerationProgress;
      this.notesSavingProgress = notesSavingProgress;
      this.itemsCount = itemsCount;
    }

    @Override
    protected void onPreExecute() {
      notificationBuilder.setProgress(100, 0, false);
      notificationManager.notify(ID, notificationBuilder.build());
    }

    @Override
    protected void onProgressUpdate(ProgressUpdate... values) {
      notificationBuilder.setContentText(values[0].title);
      notificationBuilder.setProgress(100, values[0].percentage, false);
      notificationManager.notify(ID, notificationBuilder.build());
    }

    private void onNoteGenerated(int noteNumber) {
      if (noteNumber % 1000 == 0) {
        int percentage = getPercentage(noteNumber) / 2;
        publishProgress(new ProgressUpdate(notesGenerationProgress + " " + percentage + "%", percentage));
      }
    }

    private void onNoteSaved(int noteNumber) {
      if (noteNumber % 1000 == 0) {
        int percentage = getPercentage(noteNumber) / 2 + 50;
        publishProgress(new ProgressUpdate(notesSavingProgress + " " + percentage + "%", percentage));
      }
    }

    private int getPercentage(int noteNumber) {
      return (int) ((double) noteNumber / itemsCount * 100);
    }

    @Override
    protected Integer doInBackground(Void... params) {
      try {
        return doGenerateItems();
      } catch (Exception e) {
        Log.e(TAG, "Unable to generate notes", e);
        return null;
      }
    }

    @NonNull
    private Integer doGenerateItems() {
      List<SysItem> generatedItems = SysItemRepository.generateNotes(dbHelper, itemsCount, new Consumer<Integer>() {
        @Override
        public void apply(Integer noteNumber) {
          onNoteGenerated(noteNumber);
        }
      }, new Consumer<Integer>() {
        @Override
        public void apply(Integer noteNumber) {
          onNoteSaved(noteNumber);
        }
      });
      return generatedItems.size();
    }

    @Override
    protected void onPostExecute(Integer result) {
      String message;
      if (result != null) {
        message = mainActivity.getString(R.string.generated_n_notes, NumberUtils.makeShortString(result));
      } else {
        message = mainActivity.getString(R.string.notes_generation_failed);
      }

      notificationBuilder.setContentText(message);
      notificationBuilder.setProgress(0, 0, false);
      notificationManager.notify(ID, notificationBuilder.build());
      mainActivity.refreshData();
    }

    public class ProgressUpdate {
      public String title;
      public int percentage;

      public ProgressUpdate(String title, int percentage) {
        this.title = title;
        this.percentage = percentage;
      }
    }
  }
}
