package com.awesoon.core.async;

public abstract class AsyncTaskProducer<T> extends AbstractAsyncTaskAction<Object, T> {
  @Override
  public T apply(Object data) {
    return doApply();
  }

  public abstract T doApply();
}
