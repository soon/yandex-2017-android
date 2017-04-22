package com.awesoon.thirdtask.event;

import com.awesoon.thirdtask.domain.SysItem;

public interface SysItemRemoveListener {
  /**
   * Called when a sys item removed from a list view.
   *
   * @param sysItem A removed sys item.
   */
  void onSysItemRemove(SysItem sysItem);
}
