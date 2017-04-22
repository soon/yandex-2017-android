package com.awesoon.thirdtask.db;

import com.awesoon.thirdtask.BuildConfig;
import com.awesoon.thirdtask.domain.SysItem;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.List;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = LOLLIPOP, packageName = "com.awesoon.thirdtask")
public class DbHelperTest {
  private DbHelper dbHelper;

  @Before
  public void setUp() throws Exception {
    dbHelper = new DbHelper(RuntimeEnvironment.application);
    dbHelper.clearDbAndRecreate();
  }

  @After
  public void tearDown() throws Exception {
    dbHelper.clearDbAndRecreate();
  }

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

    assertThat(allItems.size(), is(1));
    assertThat(allItems.get(0).getId(), is(item.getId()));
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
}
