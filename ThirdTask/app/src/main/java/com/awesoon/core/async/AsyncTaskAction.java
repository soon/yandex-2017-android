package com.awesoon.core.async;

public abstract class AsyncTaskAction extends AbstractAsyncTaskAction<Void, Void> {
  @Override
  public Void apply(Void data) {
    doApply();
    return null;
  }

  public abstract void doApply();
}
