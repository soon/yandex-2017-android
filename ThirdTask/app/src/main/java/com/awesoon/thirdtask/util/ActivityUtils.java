package com.awesoon.thirdtask.util;

import android.app.Activity;
import android.view.View;

public final class ActivityUtils {
  /**
   * Finds a view by the given id.
   *
   * @param activity Activity with a view.
   * @param id       View id.
   * @param <T>      View type.
   * @return Found id.
   * @throws AssertionError if the view not found.
   */
  public static <T> T findViewById(Activity activity, int id) {
    return findViewById(activity, id, String.valueOf(id));
  }

  /**
   * Finds a view by the given id.
   *
   * @param activity Activity with a view.
   * @param id       View id.
   * @param name     View name.
   * @param <T>      View type.
   * @return Found id.
   * @throws AssertionError if the view not found.
   */
  public static <T> T findViewById(Activity activity, int id, String name) {
    View view = activity.findViewById(id);
    Assert.notNull(view, "Unable to find view " + name + " in the activity " + activity);
    return (T) view;
  }
}
