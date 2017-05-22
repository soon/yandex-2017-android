package com.awesoon.core.async;

public class PipedAsyncTaskProgressSuccess implements PipedAsyncTaskProgress {
  private final AsyncSubTask action;
  private final Object inputData;

  public PipedAsyncTaskProgressSuccess(AsyncSubTask action, Object inputData) {
    this.action = action;
    this.inputData = inputData;
  }

  @Override
  public void execute() {
    if (action != null && action.getOnSuccess() != null) {
      action.getOnSuccess().apply(inputData);
    }
  }
}
