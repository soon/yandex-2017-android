package com.awesoon.thirdtask.task;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;

import com.awesoon.thirdtask.db.DbHelper;
import com.awesoon.thirdtask.domain.SysItem;
import com.awesoon.thirdtask.repository.SysItemRepository;
import com.awesoon.thirdtask.util.Action;
import com.awesoon.thirdtask.util.Consumer;

import java.util.List;

public class NotesToJsonExporterThread extends HandlerThread {
  public static final String THREAD_NAME = "NotesToJsonExporterThread";

  private DbHelper dbHelper;
  private Handler handler;
  private Listener listener;

  public NotesToJsonExporterThread(DbHelper dbHelper, Listener listener) {
    super(THREAD_NAME);
    this.dbHelper = dbHelper;
    this.listener = listener;
  }

  @Override
  protected void onLooperPrepared() {
    handler = new Handler(getLooper());
  }

  public void loadAllItemsFromFile(final ContentResolver contentResolver, final Uri uri) {
    handler.post(new Runnable() {
      @Override
      public void run() {
        try {
          if (listener != null && listener.onImportLoadingFromFile != null) {
            listener.onImportLoadingFromFile.call();
          }
          final List<SysItem> sysItems = SysItemRepository.loadAllItemsFromFile(contentResolver, uri);
          if (listener != null && listener.onImportLoadedFromFile != null) {
            listener.onImportLoadedFromFile.apply(sysItems);
          }
          dbHelper.removeAllSysItems();
          List<SysItem> savedSysItems = dbHelper.addSysItems(sysItems,
              listener == null ? null : new Consumer<Integer>() {
            @Override
            public void apply(Integer saved) {
              if (listener != null && listener.onImportNoteSaved != null) {
                listener.onImportNoteSaved.apply(new SavedTotalPair(saved, sysItems.size()));
              }
            }
          });
          if (listener != null && listener.onImportCompleted != null) {
            listener.onImportCompleted.apply(savedSysItems);
          }
        } catch (Exception e) {
          if (listener != null && listener.onImportErrorOccurred != null) {
            listener.onImportErrorOccurred.apply(e);
          }
        }
      }
    });
  }

  public void storeAllItemsToFile(final ContentResolver contentResolver, final Uri uri) {
    handler.post(new Runnable() {
      @Override
      public void run() {
        try {
          if (listener != null && listener.onExportLoadingFromDb != null) {
            listener.onExportLoadingFromDb.call();
          }
          List<SysItem> allItems = dbHelper.findAllSysItems();
          if (listener != null && listener.onExportSavingToFile != null) {
            listener.onExportSavingToFile.apply(allItems);
          }
          SysItemRepository.storeAllItemsToFile(allItems, contentResolver, uri);
          if (listener != null && listener.onExportCompleted != null) {
            listener.onExportCompleted.apply(allItems);
          }
        } catch (Exception e) {
          if (listener != null && listener.onExportErrorOccurred != null) {
            listener.onExportErrorOccurred.apply(e);
          }
        }
      }
    });
  }

  public static class Listener {
    private Action onImportLoadingFromFile;
    private Consumer<List<SysItem>> onImportLoadedFromFile;
    private Consumer<SavedTotalPair> onImportNoteSaved;
    private Consumer<List<SysItem>> onExportCompleted;
    private Consumer<List<SysItem>> onImportCompleted;
    private Consumer<Exception> onImportErrorOccurred;
    private Consumer<Exception> onExportErrorOccurred;

    private Action onExportLoadingFromDb;
    private Consumer<List<SysItem>> onExportSavingToFile;

    public Action getOnImportLoadingFromFile() {
      return onImportLoadingFromFile;
    }

    public Listener setOnImportLoadingFromFile(Action onImportLoadingFromFile) {
      this.onImportLoadingFromFile = onImportLoadingFromFile;
      return this;
    }

    public Consumer<List<SysItem>> getOnImportLoadedFromFile() {
      return onImportLoadedFromFile;
    }

    public Listener setOnImportLoadedFromFile(Consumer<List<SysItem>> onImportLoadedFromFile) {
      this.onImportLoadedFromFile = onImportLoadedFromFile;
      return this;
    }

    public Consumer<SavedTotalPair> getOnImportNoteSaved() {
      return onImportNoteSaved;
    }

    public Listener setOnImportNoteSaved(Consumer<SavedTotalPair> onImportNoteSaved) {
      this.onImportNoteSaved = onImportNoteSaved;
      return this;
    }

    public Consumer<List<SysItem>> getOnExportCompleted() {
      return onExportCompleted;
    }

    public Listener setOnExportCompleted(Consumer<List<SysItem>> onExportCompleted) {
      this.onExportCompleted = onExportCompleted;
      return this;
    }

    public Consumer<List<SysItem>> getOnImportCompleted() {
      return onImportCompleted;
    }

    public Listener setOnImportCompleted(Consumer<List<SysItem>> onImportCompleted) {
      this.onImportCompleted = onImportCompleted;
      return this;
    }

    public Consumer<Exception> getOnImportErrorOccurred() {
      return onImportErrorOccurred;
    }

    public Listener setOnImportErrorOccurred(Consumer<Exception> onImportErrorOccurred) {
      this.onImportErrorOccurred = onImportErrorOccurred;
      return this;
    }

    public Action getOnExportLoadingFromDb() {
      return onExportLoadingFromDb;
    }

    public Listener setOnExportLoadingFromDb(Action onExportLoadingFromDb) {
      this.onExportLoadingFromDb = onExportLoadingFromDb;
      return this;
    }

    public Consumer<List<SysItem>> getOnExportSavingToFile() {
      return onExportSavingToFile;
    }

    public Listener setOnExportSavingToFile(Consumer<List<SysItem>> onExportSavingToFile) {
      this.onExportSavingToFile = onExportSavingToFile;
      return this;
    }

    public Consumer<Exception> getOnExportErrorOccurred() {
      return onExportErrorOccurred;
    }

    public Listener setOnExportErrorOccurred(Consumer<Exception> onExportErrorOccurred) {
      this.onExportErrorOccurred = onExportErrorOccurred;
      return this;
    }
  }

  public static class SavedTotalPair {
    private int saved;
    private int total;

    public SavedTotalPair(int saved, int total) {
      this.saved = saved;
      this.total = total;
    }

    public int getSaved() {
      return saved;
    }

    public SavedTotalPair setSaved(int saved) {
      this.saved = saved;
      return this;
    }

    public int getTotal() {
      return total;
    }

    public SavedTotalPair setTotal(int total) {
      this.total = total;
      return this;
    }
  }
}
