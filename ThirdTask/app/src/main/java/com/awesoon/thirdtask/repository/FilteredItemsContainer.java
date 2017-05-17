package com.awesoon.thirdtask.repository;

import com.awesoon.thirdtask.domain.SysItem;

import java.util.List;

public class FilteredItemsContainer {
  private int originalItemsCount;
  private List<SysItem> filteredItems;
  private int allFilteredItemsSize;

  public FilteredItemsContainer() {
  }

  public FilteredItemsContainer(int originalItemsCount, List<SysItem> filteredItems, int allFilteredItemsSize) {
    this.originalItemsCount = originalItemsCount;
    this.filteredItems = filteredItems;
    this.allFilteredItemsSize = allFilteredItemsSize;
  }

  public int getOriginalItemsCount() {
    return originalItemsCount;
  }

  public FilteredItemsContainer setOriginalItemsCount(int originalItemsCount) {
    this.originalItemsCount = originalItemsCount;
    return this;
  }

  public List<SysItem> getFilteredItems() {
    return filteredItems;
  }

  public FilteredItemsContainer setFilteredItems(List<SysItem> filteredItems) {
    this.filteredItems = filteredItems;
    return this;
  }

  public int getFilteredItemsSize() {
    return filteredItems == null ? 0 : filteredItems.size();
  }
}
