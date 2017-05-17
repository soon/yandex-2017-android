package com.awesoon.thirdtask.web.rest.dto;

public class BaseNotesBackendDto<T> {
  private String status;
  private String error;
  private T data;

  public String getStatus() {
    return status;
  }

  public BaseNotesBackendDto setStatus(String status) {
    this.status = status;
    return this;
  }

  public String getError() {
    return error;
  }

  public BaseNotesBackendDto setError(String error) {
    this.error = error;
    return this;
  }

  public T getData() {
    return data;
  }

  public BaseNotesBackendDto setData(T data) {
    this.data = data;
    return this;
  }

  public boolean isOk() {
    return "ok".equalsIgnoreCase(status);
  }

  public boolean isNotFound() {
    return "not_found".equals(error);
  }

  @Override
  public String toString() {
    return "BaseNotesBackendDto{" +
        "status='" + status + '\'' +
        ", error='" + error + '\'' +
        ", data=" + data +
        '}';
  }
}
