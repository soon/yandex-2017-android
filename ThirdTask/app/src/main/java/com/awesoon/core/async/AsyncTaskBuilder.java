package com.awesoon.core.async;

import android.os.AsyncTask;
import android.support.annotation.Nullable;

import com.awesoon.thirdtask.util.Consumer;

import java.util.ArrayList;
import java.util.List;

public class AsyncTaskBuilder<T, R> {
  private List<AsyncSubTask> subTasks = new ArrayList<>();

  public AsyncTask<T, PipedAsyncTaskProgress, R> setGlobalListenersAndBuild(@Nullable SuccessConsumer<R> onSuccess,
                                                                            @Nullable ExceptionConsumer onError) {
    return new PipedAsyncTask<>(new ArrayList<>(subTasks), onSuccess, onError);
  }

  public AsyncTask<T, PipedAsyncTaskProgress, R> build() {
    return setGlobalListenersAndBuild(null, null);
  }

  public static <T, R> AsyncTaskBuilder<T, R> firstly(AbstractAsyncTaskAction<T, R> action) {
    return firstly(action, null, null);
  }

  public static <T, R> AsyncTaskBuilder<T, R> firstly(AbstractAsyncTaskAction<T, R> action, final Consumer<R> onSuccess) {
    return firstly(action, new SuccessConsumer<R>() {
      @Override
      public void apply(R data) {
        onSuccess.apply(data);
      }
    }, null);
  }

  public static <T, R> AsyncTaskBuilder<T, R> firstly(AbstractAsyncTaskAction<T, R> action,
                                                      @Nullable SuccessConsumer<R> onSuccess,
                                                      @Nullable ExceptionConsumer onError) {
    return new AsyncTaskBuilder<T, R>()
        .addSubTask(new AsyncSubTask<>(action, onSuccess, onError));
  }

  public <U> AsyncTaskBuilder<T, U> then(AbstractAsyncTaskAction<R, U> action) {
    return then(action, null, null);
  }

  public <U> AsyncTaskBuilder<T, U> then(AbstractAsyncTaskAction<R, U> action, @Nullable SuccessConsumer<U> onSuccess,
                                         @Nullable ExceptionConsumer onError) {
    return (AsyncTaskBuilder<T, U>) addSubTask(new AsyncSubTask<>(action, onSuccess, onError));
  }

  private AsyncTaskBuilder<T, R> addSubTask(AsyncSubTask subTask) {
    subTasks.add(subTask);
    return this;
  }

}
