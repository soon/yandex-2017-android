package com.awesoon.thirdtask.event;

import com.awesoon.thirdtask.domain.SysItem;

import java.util.List;

public abstract class DbStateChangeListenerAdapter implements DbStateChangeListener {
  @Override
  public void onSysItemAdded(SysItem sysItem) {

  }

  @Override
  public void onSysItemUpdated(SysItem sysItem) {

  }

  @Override
  public void onSysItemDeleted(Long id) {

  }

  @Override
  public void onSysItemsAdded(List<SysItem> sysItems) {

  }
}
