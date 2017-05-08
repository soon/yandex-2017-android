package com.awesoon.thirdtask.db;

import com.awesoon.thirdtask.domain.SysItem;

import org.joda.time.DateTime;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;


public class DbHelperTest extends BaseDbHelperTest {
  @Test
  public void testAddSysItem() throws Exception {
    // given
    SysItem item = new SysItem()
        .setTitle("title")
        .setBody("body")
        .setColor(1234);

    // when
    dbHelper.addSysItem(item);
    List<SysItem> allItems = dbHelper.findAllSysItems();

    // then
    assertThat(item.getId(), is(notNullValue()));
    assertThat(item.getTitle(), is("title"));
    assertThat(item.getBody(), is("body"));
    assertThat(item.getColor(), is(1234));
    assertThat(item.getCreatedTime(), is(notNullValue()));
    assertThat(item.getLastEditedTime(), is(notNullValue()));
    assertThat(item.getCreatedTime(), is(notNullValue()));

    assertThat(allItems.size(), is(1));
    assertThat(allItems.get(0).getId(), is(item.getId()));
  }

  @Test
  public void testUpdateExistingItem() throws Exception {
    // given
    SysItem item = new SysItem()
        .setTitle("title")
        .setBody("body")
        .setColor(1234);

    dbHelper.addSysItem(item);

    Long id = item.getId();
    DateTime createdTime = new DateTime(2016, 1, 1, 2, 3, 4);
    item.setCreatedTime(createdTime);
    DateTime lastEditedTime = new DateTime(2016, 1, 2, 2, 3, 4);
    item.setLastEditedTime(lastEditedTime);
    DateTime lastViewedTime = new DateTime(2016, 1, 3, 2, 3, 4);
    item.setLastViewedTime(lastViewedTime);
    item.setTitle("title2");
    item.setBody("body2");
    item.setColor(4321);

    // when
    dbHelper.saveSysItem(item);

    // then
    assertThat(item.getId(), is(id));
    assertThat(item.getTitle(), is("title2"));
    assertThat(item.getBody(), is("body2"));
    assertThat(item.getColor(), is(4321));
    assertThat(item.getCreatedTime(), is(createdTime));
    assertThat(item.getLastEditedTime().isAfter(lastEditedTime), is(true));
    assertThat(item.getLastViewedTime().isAfter(lastViewedTime), is(true));

    List<SysItem> allSysItems = dbHelper.findAllSysItems();
    assertThat(allSysItems.size(), is(1));
    assertThat(allSysItems.get(0).getId(), is(id));
  }

  @Test
  public void testFindAllItems__empty() throws Exception {
    // given

    // when
    List<SysItem> allItems = dbHelper.findAllSysItems();

    // then
    assertThat(allItems, is(empty()));
  }

  @Test
  public void testFindAllItems() throws Exception {
    // given
    dbHelper.addSysItem(new SysItem().setTitle("title 1").setBody("body 1").setColor(1234));
    dbHelper.addSysItem(new SysItem().setTitle("title 2").setBody("body 2").setColor(5678));
    dbHelper.addSysItem(new SysItem().setTitle("title 3").setBody("body 3").setColor(9012));

    // when
    List<SysItem> allItems = dbHelper.findAllSysItems();

    // then
    assertThat(allItems.size(), is(3));
  }

  @Test
  public void testFindSysItemById() throws Exception {
    // given
    dbHelper.addSysItem(new SysItem().setTitle("title 1").setBody("body 1").setColor(1234));
    SysItem sysItem = dbHelper.addSysItem(new SysItem().setTitle("title 2").setBody("body 2").setColor(5678));
    dbHelper.addSysItem(new SysItem().setTitle("title 3").setBody("body 3").setColor(9012));

    // when
    SysItem foundSysItem = dbHelper.findSysItemById(sysItem.getId());

    // then
    assertThat(foundSysItem, is(notNullValue()));
    assertThat(foundSysItem.getId(), is(sysItem.getId()));
    assertThat(foundSysItem.getTitle(), is("title 2"));
    assertThat(foundSysItem.getBody(), is("body 2"));
    assertThat(foundSysItem.getColor(), is(5678));
  }

  @Test
  public void testFindSysItemById__notFound() throws Exception {
    // given

    // when
    SysItem foundSysItem = dbHelper.findSysItemById(42L);

    // then
    assertThat(foundSysItem, is(nullValue()));
  }

  @Test
  public void testRemoveSysItemById() throws Exception {
    // given
    dbHelper.addSysItem(new SysItem().setTitle("title 1").setBody("body 1").setColor(1234));
    SysItem sysItem = dbHelper.addSysItem(new SysItem().setTitle("title 2").setBody("body 2").setColor(5678));
    dbHelper.addSysItem(new SysItem().setTitle("title 3").setBody("body 3").setColor(9012));

    // when
    dbHelper.removeSysItemById(sysItem.getId());
    List<SysItem> allSysItems = dbHelper.findAllSysItems();

    // then
    assertThat(allSysItems.size(), is(2));
  }
}
