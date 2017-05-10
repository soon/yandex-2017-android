package com.awesoon.core.sql;

import com.awesoon.thirdtask.util.CollectionUtils;

import java.util.List;

public class Page<T> {
  private List<T> data;
  private int size;
  private int totalPages;
  private int number;
  private int totalElements;

  public List<T> getData() {
    return data;
  }

  public Page<T> setData(List<T> data) {
    this.data = data;
    return this;
  }

  public int getSize() {
    return size;
  }

  public Page<T> setSize(int size) {
    this.size = size;
    return this;
  }

  public int getTotalPages() {
    return totalPages;
  }

  public Page<T> setTotalPages(int totalPages) {
    this.totalPages = totalPages;
    return this;
  }

  public int getNumber() {
    return number;
  }

  public Page<T> setNumber(int number) {
    this.number = number;
    return this;
  }

  public int getTotalElements() {
    return totalElements;
  }

  public Page<T> setTotalElements(int totalElements) {
    this.totalElements = totalElements;
    return this;
  }

  public boolean isEmpty() {
    return CollectionUtils.isEmpty(data);
  }
}
