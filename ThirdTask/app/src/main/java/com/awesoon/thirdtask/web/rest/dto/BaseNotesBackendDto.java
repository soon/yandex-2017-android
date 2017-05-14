package com.awesoon.thirdtask.web.rest.dto;

public class BaseNotesBackendDto<T> {
  private NotesBackendStatus status;
  private String error;
  private T data;

  public NotesBackendStatus getStatus() {
    return status;
  }

  public BaseNotesBackendDto setStatus(NotesBackendStatus status) {
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
    return status == NotesBackendStatus.OK;
  }
}
