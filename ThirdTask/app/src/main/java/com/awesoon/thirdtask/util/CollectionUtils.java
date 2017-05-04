package com.awesoon.thirdtask.util;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;

public class CollectionUtils {
  public static <T> boolean isEmpty(Collection<T> collection) {
    return collection == null || collection.isEmpty();
  }

  public static <T, U> int indexOf(List<T> list, U value, BiPredicate<T, U> cmp) {
    for (int i = 0; i < list.size(); i++) {
      if (cmp.apply(list.get(i), value)) {
        return i;
      }
    }

    return -1;
  }

  @SuppressWarnings("unchecked")
  public static <T, U> U[] mapToArray(List<T> data, Class<U> clazz, Function<T, U> transformer) {
    if (data == null) {
      return (U[]) Array.newInstance(clazz, 0);
    }

    U[] transformed = (U[]) Array.newInstance(clazz, data.size());
    for (int i = 0; i < data.size(); i++) {
      transformed[i] = transformer.apply(data.get(i));
    }

    return transformed;
  }
}
