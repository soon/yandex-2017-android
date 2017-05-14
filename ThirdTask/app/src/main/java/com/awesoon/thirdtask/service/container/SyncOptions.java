package com.awesoon.thirdtask.service.container;

public class SyncOptions {
  private Overwrite overwriteOptions;

  public Overwrite getOverwriteOptions() {
    return overwriteOptions;
  }

  public SyncOptions setOverwriteOptions(Overwrite overwriteOptions) {
    this.overwriteOptions = overwriteOptions;
    return this;
  }

  public enum  Overwrite {
    OVERWRITE_LOCAL,
    OVERWRITE_REMOTE,
  }
}
