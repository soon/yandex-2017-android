package com.awesoon.thirdtask.repository;

import com.awesoon.thirdtask.db.BaseDbHelperTest;
import com.awesoon.thirdtask.domain.SysItem;
import com.awesoon.thirdtask.repository.filter.DatePeriodFilter;
import com.awesoon.thirdtask.repository.filter.FilteredColumn;
import com.awesoon.thirdtask.repository.filter.SortFilter;
import com.awesoon.thirdtask.repository.filter.SysItemFilter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.joda.time.DateTime;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SysItemRepositoryTest extends BaseDbHelperTest {
  @Test
  public void testGetAllItemsFiltered__nullFilter() throws Exception {
    // given
    dbHelper.addSysItem(new SysItem().setTitle("title 1").setBody("body 1").setColor(41));
    dbHelper.addSysItem(new SysItem().setTitle("title 2").setBody("body 2").setColor(42));
    dbHelper.addSysItem(new SysItem().setTitle("title 3").setBody("body 3").setColor(43));

    // when
    FilteredItemsContainer filteredItems = SysItemRepository.getAllItemsFiltered(dbHelper, null);

    // then
    assertThat(filteredItems.getFilteredItemsSize(), is(3));
  }

  @Test
  public void testGetAllItemsFiltered__emptyFilter() throws Exception {
    // given
    dbHelper.addSysItem(new SysItem().setTitle("title 1").setBody("body 1").setColor(41));
    dbHelper.addSysItem(new SysItem().setTitle("title 2").setBody("body 2").setColor(42));
    dbHelper.addSysItem(new SysItem().setTitle("title 3").setBody("body 3").setColor(43));

    // when
    FilteredItemsContainer filteredItems = SysItemRepository.getAllItemsFiltered(dbHelper, new SysItemFilter());

    // then
    assertThat(filteredItems.getFilteredItemsSize(), is(3));
  }

  @Test
  public void testGetAllItemsFiltered__color() throws Exception {
    // given
    dbHelper.addSysItem(new SysItem().setTitle("title 1").setBody("body 1").setColor(41));
    dbHelper.addSysItem(new SysItem().setTitle("title 2").setBody("body 2").setColor(42));
    dbHelper.addSysItem(new SysItem().setTitle("title 3").setBody("body 3").setColor(43));

    // when
    FilteredItemsContainer filteredItems = SysItemRepository.getAllItemsFiltered(
        dbHelper, new SysItemFilter().setColors(ImmutableSet.of(41, 43)));

    // then
    assertThat(filteredItems.getFilteredItemsSize(), is(2));
    assertThat(filteredItems.getFilteredItems().get(0).getColor(), is(41));
    assertThat(filteredItems.getFilteredItems().get(1).getColor(), is(43));
  }

  @Test
  public void testGetAllItemsFiltered__sort() throws Exception {
    // given
    dbHelper.addSysItem(new SysItem().setTitle("title 1").setBody("body 1").setColor(41));
    dbHelper.addSysItem(new SysItem().setTitle("title 1").setBody("body 2").setColor(42));
    dbHelper.addSysItem(new SysItem().setTitle("title 3").setBody("body 3").setColor(43));

    // when
    FilteredItemsContainer filteredItems = SysItemRepository.getAllItemsFiltered(dbHelper,
        new SysItemFilter().setSorts(ImmutableList.of(
            SortFilter.desc(FilteredColumn.TITLE),
            SortFilter.asc(FilteredColumn.BODY)
        )));

    // then
    assertThat(filteredItems.getFilteredItemsSize(), is(3));
    assertThat(filteredItems.getFilteredItems().get(0).getTitle(), is("title 3"));
    assertThat(filteredItems.getFilteredItems().get(0).getBody(), is("body 3"));

    assertThat(filteredItems.getFilteredItems().get(1).getTitle(), is("title 1"));
    assertThat(filteredItems.getFilteredItems().get(1).getBody(), is("body 1"));

    assertThat(filteredItems.getFilteredItems().get(2).getTitle(), is("title 1"));
    assertThat(filteredItems.getFilteredItems().get(2).getBody(), is("body 2"));
  }

  @Test
  public void testGetAllItemsFiltered__sortCreatedTime() throws Exception {
    // given
    SysItem i1 = dbHelper.addSysItem(new SysItem().setTitle("title 1").setBody("body 1").setColor(41));
    SysItem i2 = dbHelper.addSysItem(new SysItem().setTitle("title 2").setBody("body 2").setColor(42));
    SysItem i3 = dbHelper.addSysItem(new SysItem().setTitle("title 3").setBody("body 3").setColor(43));

    dbHelper.saveSysItem(i1.setCreatedTime(new DateTime(2016, 1, 1, 1, 1, 1)));
    dbHelper.saveSysItem(i2.setCreatedTime(new DateTime(2017, 1, 1, 1, 1, 1)));
    dbHelper.saveSysItem(i3.setCreatedTime(new DateTime(2015, 1, 1, 1, 1, 1)));

    // when
    FilteredItemsContainer filteredItems = SysItemRepository.getAllItemsFiltered(dbHelper,
        new SysItemFilter().setSorts(ImmutableList.of(
            SortFilter.desc(FilteredColumn.CREATED)
        )));

    // then
    assertThat(filteredItems.getFilteredItemsSize(), is(3));
    assertThat(filteredItems.getFilteredItems().get(0).getId(), is(i2.getId()));
    assertThat(filteredItems.getFilteredItems().get(1).getId(), is(i1.getId()));
    assertThat(filteredItems.getFilteredItems().get(2).getId(), is(i3.getId()));
  }

  @Test
  public void testGetAllItemsFiltered__filterCreatedTime() throws Exception {
    // given
    SysItem i1 = dbHelper.addSysItem(new SysItem().setTitle("title 1").setBody("body 1").setColor(41));
    SysItem i2 = dbHelper.addSysItem(new SysItem().setTitle("title 2").setBody("body 2").setColor(42));
    SysItem i3 = dbHelper.addSysItem(new SysItem().setTitle("title 3").setBody("body 3").setColor(43));
    SysItem i4 = dbHelper.addSysItem(new SysItem().setTitle("title 4").setBody("body 4").setColor(44));
    SysItem i5 = dbHelper.addSysItem(new SysItem().setTitle("title 5").setBody("body 5").setColor(45));

    dbHelper.saveSysItem(i1.setCreatedTime(new DateTime(2016, 1, 1, 1, 1, 1)));
    dbHelper.saveSysItem(i2.setCreatedTime(new DateTime(2016, 1, 2, 1, 1, 1)));
    dbHelper.saveSysItem(i3.setCreatedTime(new DateTime(2016, 1, 3, 1, 1, 1)));
    dbHelper.saveSysItem(i4.setCreatedTime(new DateTime(2016, 1, 4, 1, 1, 1)));
    dbHelper.saveSysItem(i5.setCreatedTime(new DateTime(2016, 1, 5, 1, 1, 1)));

    // when
    FilteredItemsContainer filteredItems = SysItemRepository.getAllItemsFiltered(dbHelper,
        new SysItemFilter().setCreatedDateFilter(
            new DatePeriodFilter()
                .setFrom(new DateTime(2016, 1, 2, 2, 2, 2))
                .setTo(new DateTime(2016, 1, 4, 2, 2, 2))));

    // then
    assertThat(filteredItems.getFilteredItemsSize(), is(3));
    assertThat(filteredItems.getFilteredItems().get(0).getId(), is(i2.getId()));
    assertThat(filteredItems.getFilteredItems().get(1).getId(), is(i3.getId()));
    assertThat(filteredItems.getFilteredItems().get(2).getId(), is(i4.getId()));
  }
}