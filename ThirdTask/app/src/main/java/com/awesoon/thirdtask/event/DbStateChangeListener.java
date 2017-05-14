package com.awesoon.thirdtask.event;

import com.awesoon.thirdtask.domain.SysItem;

import java.util.List;

public interface DbStateChangeListener {
  void onSysItemAdded(SysItem sysItem);

  void onSysItemSynced(SysItem sysItem);

  void onSysItemNotSynced(SysItem sysItem);

  void onSysItemUpdated(SysItem sysItem);

  void onSysItemDeleted(Long id);

  void onSysItemsAdded(List<SysItem> sysItems);
}
