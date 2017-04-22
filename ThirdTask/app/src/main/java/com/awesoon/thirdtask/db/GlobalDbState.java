package com.awesoon.thirdtask.db;

import com.awesoon.thirdtask.domain.SysItem;
import com.awesoon.thirdtask.event.DbStateChangeListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GlobalDbState {
  private static final Map<Object, List<DbStateChangeListener>> subscriberToListeners = new HashMap<>();

  public static void subscribe(Object subscriber, DbStateChangeListener listener) {
    synchronized (subscriberToListeners) {
      if (!subscriberToListeners.containsKey(subscriber)) {
        subscriberToListeners.put(subscriber, new ArrayList<DbStateChangeListener>());
      }
      subscriberToListeners.get(subscriber).add(listener);
    }
  }

  public static void unsubscribe(Object subscriber) {
    synchronized (subscriberToListeners) {
      subscriberToListeners.remove(subscriber);
    }
  }

  public static void notifySysItemAdded(final SysItem sysItem) {
    runForEachListener(new Consumer<DbStateChangeListener>() {
      @Override
      public void accept(DbStateChangeListener listener) {
        listener.onSysItemAdded(sysItem);
      }
    });
  }

  public static void notifySysItemUpdated(final SysItem sysItem) {
    runForEachListener(new Consumer<DbStateChangeListener>() {
      @Override
      public void accept(DbStateChangeListener listener) {
        listener.onSysItemUpdated(sysItem);
      }
    });
  }

  public static void notifySysItemDeleted(final long sysItemId) {
    runForEachListener(new Consumer<DbStateChangeListener>() {
      @Override
      public void accept(DbStateChangeListener listener) {
        listener.onSysItemDeleted(sysItemId);
      }
    });
  }

  private static void runForEachListener(Consumer<DbStateChangeListener> action) {
    synchronized (subscriberToListeners) {
      for (Map.Entry<Object, List<DbStateChangeListener>> entry : subscriberToListeners.entrySet()) {
        List<DbStateChangeListener> listeners = entry.getValue();
        for (DbStateChangeListener listener : listeners) {
          action.accept(listener);
        }
      }
    }
  }

  private interface Consumer<T> {
    void accept(T value);
  }
}
