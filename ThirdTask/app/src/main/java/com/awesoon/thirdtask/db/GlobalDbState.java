package com.awesoon.thirdtask.db;

import com.awesoon.thirdtask.domain.SysItem;
import com.awesoon.thirdtask.event.DbStateChangeListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GlobalDbState {
  private static final Map<Object, List<DbStateChangeListener>> subscriberToListeners = new HashMap<>();

  /**
   * Allows the given subscriber to receive updates via the given listener.
   *
   * @param subscriber A subscriber.
   * @param listener   A listener.
   */
  public static void subscribe(Object subscriber, DbStateChangeListener listener) {
    synchronized (subscriberToListeners) {
      if (!subscriberToListeners.containsKey(subscriber)) {
        subscriberToListeners.put(subscriber, new ArrayList<DbStateChangeListener>());
      }
      subscriberToListeners.get(subscriber).add(listener);
    }
  }

  /**
   * Removes all subscriber listeners.
   *
   * @param subscriber A subscriber.
   */
  public static void unsubscribe(Object subscriber) {
    synchronized (subscriberToListeners) {
      subscriberToListeners.remove(subscriber);
    }
  }

  /**
   * Notifies all subscribers when several sys item were added.
   *
   * @param sysItems Added sys item s.
   */
  public static void notifySysItemsAdded(final List<SysItem> sysItems) {
    runForEachListener(new Consumer<DbStateChangeListener>() {
      @Override
      public void accept(DbStateChangeListener listener) {
        listener.onSysItemsAdded(sysItems);
      }
    });
  }

  /**
   * Notifies all subscribers when the sys item added.
   *
   * @param sysItem Added sys item.
   */
  public static void notifySysItemAdded(final SysItem sysItem) {
    runForEachListener(new Consumer<DbStateChangeListener>() {
      @Override
      public void accept(DbStateChangeListener listener) {
        listener.onSysItemAdded(sysItem);
      }
    });
  }

  /**
   * Notifies all subscribers when the sys item updated.
   *
   * @param sysItem Updated sys item.
   */
  public static void notifySysItemUpdated(final SysItem sysItem) {
    runForEachListener(new Consumer<DbStateChangeListener>() {
      @Override
      public void accept(DbStateChangeListener listener) {
        listener.onSysItemUpdated(sysItem);
      }
    });
  }

  /**
   * Notifies all subscribers when the sys item deleted.
   *
   * @param sysItemId Deleted Sys item id.
   */
  public static void notifySysItemDeleted(final long sysItemId) {
    runForEachListener(new Consumer<DbStateChangeListener>() {
      @Override
      public void accept(DbStateChangeListener listener) {
        listener.onSysItemDeleted(sysItemId);
      }
    });
  }

  /**
   * Executes the given action for each listener.
   *
   * @param action An action.
   */
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
