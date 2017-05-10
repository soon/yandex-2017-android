package com.awesoon.thirdtask.activity.listener;

import android.support.v7.widget.SearchView;

import java.util.Timer;
import java.util.TimerTask;

public abstract class DebouncedQueryTextListener implements SearchView.OnQueryTextListener {
  private final long interval;
  private String lastQuery;
  private Timer timer = new Timer();

  public DebouncedQueryTextListener(long interval) {
    this.interval = interval;
  }

  @Override
  public boolean onQueryTextSubmit(String query) {
    lastQuery = query;
    scheduleDebouncedCall();
    return true;
  }

  @Override
  public boolean onQueryTextChange(String newText) {
    lastQuery = newText;
    scheduleDebouncedCall();
    return true;
  }

  private void scheduleDebouncedCall() {
    timer.cancel();
    timer.purge();
    timer = new Timer();
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        debouncedQueryChangedOrSubmitted(lastQuery);
      }
    }, interval);
  }

  public abstract void debouncedQueryChangedOrSubmitted(String query);
}
