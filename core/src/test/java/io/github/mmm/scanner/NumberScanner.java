package io.github.mmm.scanner;

import java.util.Locale;
import java.util.function.Function;

import org.assertj.core.api.Assertions;

import io.github.mmm.base.number.NumberType;

abstract class NumberScanner<N extends Number & Comparable<N>> extends Assertions {

  private final NumberType<N> type;

  private final Function<String, CharStreamScanner> factory;

  private static final String[] BASIC_INTS = { "0", "+0", "-0", "1", "+1", "-1" };

  public NumberScanner(NumberType<N> type, Function<String, CharStreamScanner> factory) {

    super();
    this.type = type;
    this.factory = factory;
  }

  N scan(String number, NumericRadixMode radixMode) {

    return scan(number, radixMode, false, null);
  }

  N scan(String number, NumericRadixMode radixMode, boolean noSign) {

    return scan(number, radixMode, noSign, null);
  }

  N scan(String number, NumericRadixMode radixMode, boolean noSign, N max) {

    CharStreamScanner scanner = this.factory.apply(number);
    return scan(scanner, radixMode, noSign, max);
  }

  N scan(CharStreamScanner scanner) {

    return scan(scanner, NumericRadixMode.ONLY_10, false, null);
  }

  N scan(CharStreamScanner scanner, NumericRadixMode radixMode) {

    return scan(scanner, radixMode, false, null);
  }

  N scan(CharStreamScanner scanner, NumericRadixMode radixMode, boolean noSign) {

    return scan(scanner, radixMode, noSign, null);
  }

  N scan(CharStreamScanner scanner, NumericRadixMode radixMode, boolean noSign, N max) {

    if (max == null) {
      max = this.type.getMax();
    }
    return doScan(scanner, radixMode, noSign, max);
  }

  abstract N doScan(CharStreamScanner scanner, NumericRadixMode radixMode, boolean noSign, N max);

  public void test() {

    for (String number : BASIC_INTS) {
      check(number);
    }
    N max = this.type.getMax();
    if (max != null) {
      checkLimit(max);
      checkLimit(this.type.getMin());
    }
    N nan = this.type.getNaN();
    if (nan != null) {
      String number = nan.toString();
      assertThat(check(number)).isEqualTo(nan);
      assertThat(check("+" + number)).isEqualTo(nan);
      assertThat(check("-" + number)).isEqualTo(nan);
    }
    if (this.type.isDecimal()) {
      N n12345 = this.type.valueOf("123.45E+0");
      checkRadix("123.45E+0", n12345);
      double d = 0b0111101;
      double d2 = 0x01.23456789ABCDEP+10D;
      // TODO
    } else {
      N n123 = this.type.valueOf("123");
      checkRadix("123", n123);
      checkRadix("0b01111011", n123);
      checkRadix("0x7b", n123);
      checkRadix("0173", n123);
    }
  }

  private void checkRadix(String string, N number) {

    assertThat(scan(string, NumericRadixMode.ALL, false)).isEqualTo(number);
    assertThat(scan(string.toUpperCase(Locale.ROOT), NumericRadixMode.ALL, false)).isEqualTo(number);
    N negative = this.type.subtract(this.type.getZero(), number);
    assertThat(scan("-" + string, NumericRadixMode.ALL, false)).isEqualTo(negative);
    CharStreamScanner scanner = this.factory.apply(number.toString() + ".e");
    assertThat(scan(scanner, NumericRadixMode.ALL, false)).isEqualTo(number);
    assertThat(scanner.expect(".e")).isTrue();
    assertThat(scanner.hasNext()).isFalse();
  }

  private void checkLimit(N number) {

    check(number);
    String string = number.toString();
    int length = string.length();
    char lastDigit = string.charAt(length - 1);
    assert (lastDigit != '9');
    lastDigit++;
    string = string.substring(0, length - 1) + lastDigit;
    N result = check(string);
    if (result != null) {
      if (this.type.getMax().equals(number)) {
        assertThat(result).isEqualTo(this.type.getPositiveInfinity());
      } else {
        assertThat(result).isEqualTo(this.type.getNegativeInfinity());
      }
    }
  }

  private N check(N number) {

    String string = number.toString();
    N result = check(string);
    assertThat(result).isEqualTo(number);
    if (!string.startsWith("-")) {
      check("+" + string);
    }
    return result;
  }

  private N check(String number) {

    // when java.lang
    N javaNumber = null;
    NumberFormatException javaError = null;
    try {
      javaNumber = this.type.valueOf(number);
    } catch (NumberFormatException e) {
      javaError = e;
    }
    // and when mmm scanner
    N scannerNumber = null;
    NumberFormatException scannerError = null;
    try {
      CharStreamScanner scanner = this.factory.apply(number);
      scannerNumber = scan(scanner);
    } catch (NumberFormatException e) {
      scannerError = e;
    }
    // then
    assertThat(scannerNumber).isEqualTo(javaNumber);
    if (javaNumber == null) {
      assertThat(javaError).isNotNull();
      if (scannerError != null) {
        assertThat(javaError).hasMessage(scannerError.getMessage());
      }
    }
    return scannerNumber;
  }

}
