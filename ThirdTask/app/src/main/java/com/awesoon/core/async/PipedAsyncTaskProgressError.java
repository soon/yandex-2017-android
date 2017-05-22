package com.awesoon.core.async;

public class PipedAsyncTaskProgressError implements PipedAsyncTaskProgress {
  private final AsyncSubTask subTask;
  private final Exception exception;

  public PipedAsyncTaskProgressError(AsyncSubTask subTask, Exception exception) {
    this.subTask = subTask;
    this.exception = exception;
  }

  @Override
  public void execute() {
    if (subTask != null && subTask.getOnError() != null) {
      subTask.getOnError().apply(exception);
    }
  }
}
