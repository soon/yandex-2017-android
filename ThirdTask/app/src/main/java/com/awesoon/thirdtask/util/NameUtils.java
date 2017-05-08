package com.awesoon.thirdtask.util;

import com.android.internal.util.Predicate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class NameUtils {
  public static String createUniqueName(String currentName, List<String> existingNames) {
    Assert.notNull(existingNames, "existingNames must not be null");

    if (StringUtils.isBlank(currentName)) {
      return "";
    }

    final String finalCurrentName = currentName.trim();
    List<String> sameNames = CollectionUtils.filter(existingNames, new Predicate<String>() {
      @Override
      public boolean apply(String s) {
        return StringUtils.startsWithTrimmed(s, finalCurrentName);
      }
    });

    if (sameNames.isEmpty()) {
      return finalCurrentName.trim();
    }

    final Pattern numberPattern = Pattern.compile("\\((\\d+)\\)");

    List<BigDecimal> nameNumbers = CollectionUtils.mapNotNull(sameNames, new Function<String, BigDecimal>() {
      @Override
      public BigDecimal apply(String s) {
        String numberPart = s.substring(finalCurrentName.length()).trim();
        Matcher matcher = numberPattern.matcher(numberPart);
        if (!matcher.matches()) {
          return null;
        }

        String numberGroup = matcher.group(1);
        BigDecimal nameNumber = NumberUtils.tryParseBigDecimal(numberGroup);
        if (nameNumber == null || nameNumber.compareTo(BigDecimal.ONE) < 0) {
          return null;
        }

        return nameNumber;
      }
    });

    if (nameNumbers.isEmpty()) {
      return finalCurrentName + " (1)";
    }

    Collections.sort(nameNumbers);
    BigDecimal largestNumber = nameNumbers.get(nameNumbers.size() - 1);
    return finalCurrentName + " (" + largestNumber.add(BigDecimal.ONE).toString() + ")";
  }
}
