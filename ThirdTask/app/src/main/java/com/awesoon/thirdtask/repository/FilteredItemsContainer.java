package com.awesoon.thirdtask.repository;

import com.awesoon.thirdtask.domain.SysItem;

import java.util.List;

public class FilteredItemsContainer {
  private List<SysItem> originalItems;
  private List<SysItem> filteredItems;

  public FilteredItemsContainer() {
  }

  public FilteredItemsContainer(List<SysItem> originalItems, List<SysItem> filteredItems) {
    this.originalItems = originalItems;
    this.filteredItems = filteredItems;
  }

  public List<SysItem> getOriginalItems() {
    return originalItems;
  }

  public FilteredItemsContainer setOriginalItems(List<SysItem> originalItems) {
    this.originalItems = originalItems;
    return this;
  }

  public List<SysItem> getFilteredItems() {
    return filteredItems;
  }

  public FilteredItemsContainer setFilteredItems(List<SysItem> filteredItems) {
    this.filteredItems = filteredItems;
    return this;
  }

  public int getOriginalItemsSize() {
    return originalItems == null ? 0 : originalItems.size();
  }

  public int getFilteredItemsSize() {
    return filteredItems == null ? 0 : filteredItems.size();
  }
}
