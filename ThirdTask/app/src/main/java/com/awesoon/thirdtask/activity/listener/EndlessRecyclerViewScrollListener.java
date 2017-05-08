package com.awesoon.thirdtask.activity.listener;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public abstract class EndlessRecyclerViewScrollListener extends RecyclerView.OnScrollListener {
  private static final int VISIBLE_THRESHOLD = 10;
  private int page = 0;
  private int prevItemCount = 0;
  private boolean loading = true;

  private LinearLayoutManager layoutManager;

  public EndlessRecyclerViewScrollListener(LinearLayoutManager layoutManager) {
    this.layoutManager = layoutManager;
  }

  @Override
  public void onScrolled(RecyclerView view, int dx, int dy) {
    int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
    int itemCount = layoutManager.getItemCount();

    if (loading && itemCount > prevItemCount) {
      loading = false;
      prevItemCount = itemCount;
    }

    if (!loading && itemCount < lastVisibleItemPosition + VISIBLE_THRESHOLD) {
      loading = true;
      page++;
      doLoadItems(page, itemCount, view);
    }
  }

  public abstract void doLoadItems(int newPage, int totalItemsCount, RecyclerView view);
}
