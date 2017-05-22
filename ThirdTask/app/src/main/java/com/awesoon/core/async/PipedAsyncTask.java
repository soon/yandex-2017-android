package com.awesoon.core.async;

import android.os.AsyncTask;
import android.util.Log;

import com.awesoon.thirdtask.util.Assert;
import com.awesoon.thirdtask.util.CollectionUtils;

import java.util.List;

public class PipedAsyncTask<T, R> extends AsyncTask<T, PipedAsyncTaskProgress, R> {
  private static final String TAG = "PipedAsyncTask";

  private List<AsyncSubTask> subTasks;
  private SuccessConsumer<R> onSuccess;
  private ExceptionConsumer onError;

  private Exception exception;

  public PipedAsyncTask(List<AsyncSubTask> subTasks, SuccessConsumer<R> onSuccess, ExceptionConsumer onError) {
    Assert.notNull(subTasks, "subTasks must not be null");

    this.subTasks = subTasks;
    this.onSuccess = onSuccess;
    this.onError = onError;
  }

  @Override
  protected void onProgressUpdate(PipedAsyncTaskProgress... values) {
    PipedAsyncTaskProgress progress = values[0];
    progress.execute();
  }

  @Override
  protected R doInBackground(T... params) {
    if (CollectionUtils.isEmpty(subTasks)) {
      return null;
    }

    Object inputData = params;
    for (AsyncSubTask subTask : subTasks) {
      AbstractAsyncTaskAction action = subTask.getAction();
      try {
        inputData = action.apply(inputData);
        publishProgress(new PipedAsyncTaskProgressSuccess(subTask, inputData));
      } catch (Exception e) {
        Log.e(TAG, "Unable to execute piped async task", e);
        publishProgress(new PipedAsyncTaskProgressError(subTask, e));
        exception = e;
        return null;
      }
    }

    return (R) inputData;
  }

  @Override
  protected void onPostExecute(R r) {
    if (exception == null) {
      if (onSuccess != null) {
        onSuccess.apply(r);
      }
    } else {
      if (onError != null) {
        onError.apply(exception);
      }
    }
  }
}
