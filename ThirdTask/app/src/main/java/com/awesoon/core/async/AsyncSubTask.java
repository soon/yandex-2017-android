package com.awesoon.core.async;

public class AsyncSubTask<T, R> {
  private AsyncTaskAction<T, R> action;
  private SuccessConsumer<R> onSuccess;
  private ExceptionConsumer onError;

  public AsyncSubTask() {
  }

  public AsyncSubTask(AsyncTaskAction<T, R> action, SuccessConsumer<R> onSuccess, ExceptionConsumer onError) {
    this.action = action;
    this.onSuccess = onSuccess;
    this.onError = onError;
  }

  public AsyncTaskAction<T, R> getAction() {
    return action;
  }

  public AsyncSubTask<T, R> setAction(AsyncTaskAction<T, R> action) {
    this.action = action;
    return this;
  }

  public SuccessConsumer<R> getOnSuccess() {
    return onSuccess;
  }

  public AsyncSubTask<T, R> setOnSuccess(SuccessConsumer<R> onSuccess) {
    this.onSuccess = onSuccess;
    return this;
  }

  public ExceptionConsumer getOnError() {
    return onError;
  }

  public AsyncSubTask<T, R> setOnError(ExceptionConsumer onError) {
    this.onError = onError;
    return this;
  }
}
