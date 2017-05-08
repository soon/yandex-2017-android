package com.awesoon.core.async;

public interface AsyncTaskAction<T, R> {
  R apply(T data);
}
