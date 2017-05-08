package com.awesoon.core.async;

public abstract class AsyncTaskConsumer<T> implements AsyncTaskAction<T, Object> {
  @Override
  public Object apply(T data) {
    doApply(data);
    return null;
  }

  protected abstract void doApply(T data);
}
