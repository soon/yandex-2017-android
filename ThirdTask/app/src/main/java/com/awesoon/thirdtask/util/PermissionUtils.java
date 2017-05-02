package com.awesoon.thirdtask.util;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

public class PermissionUtils {
  private static final String TAG = "PermissionUtils";

  public static boolean requestWriteExternalStoragePermissionIfNecessary(Activity activity, String permission) {
    if (Build.VERSION.SDK_INT >= 23) {
      if (activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
        Log.v(TAG, String.format("Permission %s is granted (activity %s)", permission, activity));
        return true;
      } else {
        Log.v(TAG, String.format("Permission %s is revoked (activity %s)", permission, activity));
        ActivityCompat.requestPermissions(activity, new String[]{permission}, 1);
        return false;
      }
    } else {
      // permission is automatically granted on sdk<23 upon installation
      Log.v(TAG, "Permission " + permission + " is granted (activity " + activity + ")");
      return true;
    }
  }
}
