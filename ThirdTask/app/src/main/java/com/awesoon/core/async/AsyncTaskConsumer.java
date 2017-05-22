package com.awesoon.core.async;

public abstract class AsyncTaskConsumer<T> extends AbstractAsyncTaskAction<T, Void> {
  @Override
  public Void apply(T data) {
    doApply(data);
    return null;
  }

  protected abstract void doApply(T data);
}
