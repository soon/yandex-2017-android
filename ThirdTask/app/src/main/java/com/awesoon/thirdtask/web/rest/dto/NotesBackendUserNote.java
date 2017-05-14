package com.awesoon.thirdtask.web.rest.dto;

public class NotesBackendUserNote {
  private Long id;
  private String title;
  private String description;
  private String color;
  private String created;
  private String edited;
  private String viewed;

  public Long getId() {
    return id;
  }

  public NotesBackendUserNote setId(Long id) {
    this.id = id;
    return this;
  }

  public String getTitle() {
    return title;
  }

  public NotesBackendUserNote setTitle(String title) {
    this.title = title;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public NotesBackendUserNote setDescription(String description) {
    this.description = description;
    return this;
  }

  public String getColor() {
    return color;
  }

  public NotesBackendUserNote setColor(String color) {
    this.color = color;
    return this;
  }

  public String getCreated() {
    return created;
  }

  public NotesBackendUserNote setCreated(String created) {
    this.created = created;
    return this;
  }

  public String getEdited() {
    return edited;
  }

  public NotesBackendUserNote setEdited(String edited) {
    this.edited = edited;
    return this;
  }

  public String getViewed() {
    return viewed;
  }

  public NotesBackendUserNote setViewed(String viewed) {
    this.viewed = viewed;
    return this;
  }
}
