package com.awesoon.thirdtask.service.container;

public class SysUser {
  private Long id;
  private String name;

  public SysUser() {
  }

  public SysUser(Long id, String name) {
    this.id = id;
    this.name = name;
  }

  public Long getId() {
    return id;
  }

  public SysUser setId(Long id) {
    this.id = id;
    return this;
  }

  public String getName() {
    return name;
  }

  public SysUser setName(String name) {
    this.name = name;
    return this;
  }
}
