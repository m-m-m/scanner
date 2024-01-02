package io.github.mmm.scanner;

import java.util.Locale;
import java.util.function.Function;

import org.assertj.core.api.Assertions;

import io.github.mmm.base.number.NumberType;
import io.github.mmm.scanner.number.CharScannerRadixHandler;
import io.github.mmm.scanner.number.CharScannerRadixMode;

abstract class NumberScanner<N extends Number & Comparable<N>> extends Assertions {

  private final NumberType<N> type;

  private final Function<String, CharStreamScanner> factory;

  private static final String[] BASIC_INTS = { "0", "+0", "-0", "1", "+1", "-1" };

  public NumberScanner(NumberType<N> type, Function<String, CharStreamScanner> factory) {

    super();
    this.type = type;
    this.factory = factory;
  }

  N scan(String number, CharScannerRadixHandler radixMode) {

    CharStreamScanner scanner = this.factory.apply(number);
    return scan(scanner, radixMode);
  }

  N scan(CharStreamScanner scanner) {

    return scan(scanner, CharScannerRadixMode.ONLY_10);
  }

  abstract N scan(CharStreamScanner scanner, CharScannerRadixHandler radixMode);

  public void test() {

    for (String number : BASIC_INTS) {
      check(number);
    }
    N max = this.type.getMax();
    if (max != null) {
      checkLimit(max);
      checkLimit(this.type.getMin());
      if (!this.type.isDecimal()) {
        String maxHex = "0x" + this.type.format(max, 16);
        checkWithRadixAll(maxHex, max);
      }
    }
    N nan = this.type.getNaN();
    if (nan != null) {
      String number = nan.toString();
      assertThat(check(number)).isEqualTo(nan);
      assertThat(check("+" + number)).isEqualTo(nan);
      assertThat(check("-" + number)).isEqualTo(nan);
    }
    if (this.type.isDecimal()) {
      // System.out.println(0xAB.CDP+1D);
      // System.out.println(Double.parseDouble("0xAB.CDP+1D"));
      // System.out.println(34.36015625E+1D);
      N n12345 = this.type.parse("34.36015625E+1");
      checkRadix("34.36015625e+1", n12345);
      checkRadix("0xAB.CDP+1", n12345);
      // double d = 0b0111101;
      double d2 = 0x01.23456789ABCDEP+10D;
      // TODO
    } else {
      N n123 = this.type.parse("123");
      checkRadix("123", n123);
      checkRadix("0b01111011", n123);
      checkRadix("0x7b", n123);
      checkRadix("0X7B", n123);
      checkRadix("0173", n123);
    }
  }

  private void checkRadix(String string, N number) {

    assertThat(scan(string, CharScannerRadixMode.ALL)).isEqualTo(number);
    assertThat(scan(string.toUpperCase(Locale.ROOT), CharScannerRadixMode.ALL)).isEqualTo(number);
    N negative = this.type.subtract(this.type.getZero(), number);
    assertThat(scan("-" + string, CharScannerRadixMode.ALL)).isEqualTo(negative);
    CharStreamScanner scanner = this.factory.apply(number.toString() + "xe");
    assertThat(scan(scanner, CharScannerRadixMode.ALL)).isEqualTo(number);
    assertThat(scanner.expect("xe")).isTrue();
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

  private N checkWithRadixAll(String number, N value) {

    N result = scan(number, CharScannerRadixMode.ALL);
    assertThat(result).as(number).isEqualTo(value);
    return result;
  }

  private N check(String number) {

    // when java.lang
    N javaNumber = null;
    NumberFormatException javaError = null;
    try {
      javaNumber = this.type.parse(number);
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
