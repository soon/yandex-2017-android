package com.awesoon.thirdtask.util;

import android.view.View;

public class ViewUtils {
  public static <T extends View> T findViewById(View view, int id) {
    return findViewById(view, id, String.valueOf(id));
  }

  public static <T extends View> T findViewById(View view, int id, String name) {
    View innerView = view.findViewById(id);
    Assert.notNull(innerView, "Unable to find view " + name + " in the view " + view);
    return (T) innerView;
  }
}
