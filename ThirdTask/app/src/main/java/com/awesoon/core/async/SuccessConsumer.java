package com.awesoon.core.async;

public interface SuccessConsumer<T> {
  void apply(T data);
}
