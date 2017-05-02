package com.awesoon.thirdtask.repository;

import com.awesoon.thirdtask.domain.SysItem;
import com.awesoon.thirdtask.json.adapter.ColorAdapter;
import com.awesoon.thirdtask.json.adapter.DateTimeAdapter;
import com.awesoon.thirdtask.util.JsonUtils;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SysItemsContainer {
  private List<SysItem> notes = new ArrayList<>();

  public SysItemsContainer(List<SysItem> items) {
    setNotes(items);
  }

  public static SysItemsContainer parseJson(String data) throws IOException {
    Moshi moshi = new Moshi.Builder()
        .add(new ColorAdapter())
        .add(new DateTimeAdapter())
        .build();

    return JsonUtils.parseSingleObject(moshi, data, SysItemsContainer.class);
  }

  public String toJson() throws IOException {
    Moshi moshi = new Moshi.Builder()
        .add(new ColorAdapter())
        .add(new DateTimeAdapter())
        .build();

    return JsonUtils.writeSingleObject(moshi, this, SysItemsContainer.class);
  }

  public List<SysItem> getNotes() {
    return notes;
  }

  public SysItemsContainer setNotes(List<SysItem> notes) {
    this.notes = notes == null ? new ArrayList<SysItem>() : new ArrayList<>(notes);
    return this;
  }
}
