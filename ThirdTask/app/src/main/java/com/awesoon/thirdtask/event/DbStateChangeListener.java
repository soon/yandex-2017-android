package com.awesoon.thirdtask.event;

import com.awesoon.thirdtask.domain.SysItem;

public interface DbStateChangeListener {
  void onSysItemAdded(SysItem sysItem);

  void onSysItemUpdated(SysItem sysItem);

  void onSysItemDeleted(Long id);
}
