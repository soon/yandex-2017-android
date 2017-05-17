package com.awesoon.thirdtask.util;

import com.android.internal.util.Predicate;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectionUtils {
  public static <T> boolean isEmpty(T[] array) {
    return array == null || array.length == 0;
  }

  public static <T> boolean isEmpty(Collection<T> collection) {
    return collection == null || collection.isEmpty();
  }

  public static <T> int size(Collection<T> collection) {
    return collection == null ? 0 : collection.size();
  }

  @SuppressWarnings("unchecked")
  public static <T> T[] toArray(Collection<T> collection, Class<? super T> clazz) {
    if (collection == null) {
      return (T[]) Array.newInstance(clazz, 0);
    }

    T[] array = (T[]) Array.newInstance(clazz, collection.size());
    return collection.toArray(array);
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

  public static <T> List<T> filter(List<T> data, Predicate<T> predicate) {
    if (data == null) {
      return new ArrayList<>();
    }

    ArrayList<T> result = new ArrayList<>(data.size());
    for (T element : data) {
      if (predicate.apply(element)) {
        result.add(element);
      }
    }

    return result;
  }

  public static <T, U> List<U> mapNotNull(List<T> data, Function<T, U> transformer) {
    if (data == null) {
      return new ArrayList<>();
    }

    ArrayList<U> result = new ArrayList<>(data.size());
    for (T element : data) {
      if (element != null) {
        U transformed = transformer.apply(element);
        if (transformed != null) {
          result.add(transformed);
        }
      }
    }

    return result;
  }

  public static <T, K, V> Map<K, V> collectToMap(List<T> data, Function<T, K> keyTransformer,
                                                 Function<T, V> valueTransformer) {
    if (data == null) {
      return new HashMap<>();
    }

    Map<K, V> result = new HashMap<>();
    for (T element : data) {
      K key = keyTransformer.apply(element);
      if (key == null) {
        continue;
      }

      result.put(key, valueTransformer.apply(element));
    }

    return result;
  }

  public static <T, K> Map<K, T> collectToMap(List<T> data, Function<T, K> keyTransformer) {
    if (data == null) {
      return new HashMap<>();
    }

    Map<K, T> result = new HashMap<>();
    for (T element : data) {
      K key = keyTransformer.apply(element);
      if (key == null) {
        continue;
      }

      result.put(key, element);
    }

    return result;
  }
}
