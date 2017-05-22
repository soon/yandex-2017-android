package com.awesoon.thirdtask.service.container;

import java.util.HashSet;
import java.util.Set;

public class SyncOptions {
  private Long userId;
  // all ids are local
  private Set<Long> revertRemovedIds = new HashSet<>();
  private Set<Long> removeRemoteIds = new HashSet<>();
  private Set<Long> createLocallyEditedButRemotelyRemovedIds = new HashSet<>();
  private Set<Long> removeLocallyEditedButRemotelyRemovedIds = new HashSet<>();
  private Set<Long> acceptLocalChangesIds = new HashSet<>();
  private Set<Long> acceptRemoteChangesIds = new HashSet<>();

  public Long getUserId() {
    return userId;
  }

  public SyncOptions setUserId(Long userId) {
    this.userId = userId;
    return this;
  }

  public Set<Long> getRevertRemovedIds() {
    return revertRemovedIds;
  }

  public Set<Long> getRemoveRemoteIds() {
    return removeRemoteIds;
  }

  public Set<Long> getCreateLocallyEditedButRemotelyRemovedIds() {
    return createLocallyEditedButRemotelyRemovedIds;
  }

  public Set<Long> getRemoveLocallyEditedButRemotelyRemovedIds() {
    return removeLocallyEditedButRemotelyRemovedIds;
  }

  public Set<Long> getAcceptLocalChangesIds() {
    return acceptLocalChangesIds;
  }

  public Set<Long> getAcceptRemoteChangesIds() {
    return acceptRemoteChangesIds;
  }

  public void acceptLocalChanges(Long id) {
    acceptLocalChangesIds.add(id);
  }

  public void acceptRemoteChanges(Long id) {
    acceptRemoteChangesIds.add(id);
  }

  public void removeRemote(Long id) {
    removeRemoteIds.add(id);
  }

  public void revertRemoved(Long id) {
    revertRemovedIds.add(id);
  }
}
