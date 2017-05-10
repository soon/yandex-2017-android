package com.awesoon.core.async;

import com.awesoon.thirdtask.util.Consumer;

public abstract class AbstractAsyncTaskAction<T, R> {
  private Consumer<Object> onPublishResultsConsumer;

  public abstract R apply(T data);

  protected void publishResults(Object data) {
    if (onPublishResultsConsumer != null) {
      onPublishResultsConsumer.apply(data);
    }
  }

  public void setOnPublishResultsConsumer(Consumer<Object> onPublishResultsConsumer) {
    this.onPublishResultsConsumer = onPublishResultsConsumer;
  }
}
