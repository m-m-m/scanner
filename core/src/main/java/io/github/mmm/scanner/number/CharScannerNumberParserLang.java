package io.github.mmm.scanner.number;

import java.util.Locale;

import io.github.mmm.base.number.NumberType;

/**
 * {@link CharScannerNumberParser} to parse java.lang {@link Number} values such as {@link Long}, {@link Integer},
 * {@link Double} or {@link Float}.
 */
public class CharScannerNumberParserLang extends CharScannerNumberParserBase {

  private final NumberType<?> numberType;

  private long min;

  private long minMul;

  private long mantissa;

  private long exponent;

  private Boolean upperCase;

  private double decimal;

  /**
   * The constructor.
   *
   * @param radixMode the {@link CharScannerRadixHandler} for {@link #radix(int, char)}.
   * @param numberType the {@link NumberType}.
   */
  public CharScannerNumberParserLang(CharScannerRadixHandler radixMode, NumberType<?> numberType) {

    this(radixMode, numberType, nonDecimalMax(numberType));
  }

  /**
   * The constructor.
   *
   * @param radixMode the {@link CharScannerRadixHandler} for {@link #radix(int, char)}.
   * @param numberType the {@link NumberType}.
   * @param maxNonDecimal the maximum allowed number (e.g. {@link Integer#MAX_VALUE} to parse an {@link Integer} value).
   */
  public CharScannerNumberParserLang(CharScannerRadixHandler radixMode, NumberType<?> numberType, long maxNonDecimal) {

    super(radixMode);
    this.numberType = numberType;
    this.min = -maxNonDecimal;
    this.dotPosition = -1;
  }

  private static long nonDecimalMax(NumberType<?> numberType) {

    long max = Long.MAX_VALUE;
    if (numberType.isDecimal()) {

    } else {
      Number maxNumber = numberType.getMax();
      if (maxNumber != null) {
        max = maxNumber.longValue();
      }
    }
    return max;
  }

  @Override
  protected boolean isDecimal() {

    return this.numberType.isDecimal();
  }

  @Override
  public boolean sign(char signChar) {

    if (signChar == '-') {
      if (this.min == -Integer.MAX_VALUE) {
        this.min = Integer.MIN_VALUE;
      } else if (this.min == -Long.MAX_VALUE) {
        this.min = Long.MIN_VALUE;
      }
    }
    return super.sign(signChar);
  }

  @Override
  public void special(String special) {

    if (special.length() > 1) {
      switch (special) {
        case NAN:
          this.decimal = Double.NaN;
          break;
        case INFINITY:
          if (this.sign == '-') {
            this.decimal = Double.NEGATIVE_INFINITY;
          } else {
            this.decimal = Double.POSITIVE_INFINITY;
          }
          break;
      }
    } else {

    }
    super.special(special);
  }

  @Override
  protected StringBuilder builder() {

    if (this.number == null) {
      this.number = new StringBuilder();
      long v = this.mantissa;
      if (this.sign == '+') {
        this.number.append(this.sign);
      }
      if (this.sign != '-') {
        v = -v;
      }
      appendRadix();
      for (int i = 0; i < this.digitsLeadingZeros; i++) {
        this.number.append('0');
      }
      String num = Long.toString(v, this.radix);
      if (Boolean.TRUE.equals(this.upperCase)) {
        num = num.toUpperCase(Locale.ROOT);
      }
      if (this.dotPosition >= 0) {
        this.number.append(num.substring(0, this.dotPosition));
        this.number.append('.');
        this.number.append(num.substring(this.dotPosition));
      }
      this.number.append(num);
      if (this.exponentSymbol != 0) {
        appendExponent(true);
        for (int i = 0; i < this.exponentDigitsLeadingZeros; i++) {
          this.number.append('0');
        }
        this.number.append(Long.toString(this.exponent));
      }
    }
    return this.number;
  }

  private void error(char c) {

    if (this.number == null) {
      builder().append(c); // if number was not null before, c has already been appended
    }
    this.error = true;
  }

  @Override
  public boolean digit(int digit, char digitChar) {

    super.digit(digit, digitChar);
    if (this.error) {
      return true;
    } else {
      if (this.decimal != 0) { // special can not be followed by digits
        this.error = true;
        return true;
      }
    }
    if (this.exponentSign == 0) {
      if (this.minMul == 0) {
        this.minMul = this.min / this.radix;
      }
      preventCase(digit, digitChar);
      if ((digit >= this.radix) || (this.mantissa < this.minMul)) {
        error(digitChar);
        return true;
      }
      this.mantissa = this.mantissa * this.radix;
      if (this.mantissa < this.min + digit) {
        this.mantissa = this.mantissa / this.radix;
        error(digitChar);
        return true;
      }
      this.mantissa = this.mantissa - digit;
    } else {
      if (this.exponentDigitsTotal > this.exponentDigitsLeadingZeros) { // ignore leading zeros
        this.exponent = this.exponent * 10 + digit; // exponent always has radix 10
      }
      return true;
    }
    return true;
  }

  private void preventCase(int digit, char digitChar) {

    if ((digit > 9) && (this.number == null)) { // prevent case of letter digits (e.g. hex)
      boolean upper = Character.isUpperCase(digitChar);
      if (this.upperCase == null) {
        this.upperCase = Boolean.valueOf(upper);
      } else if (this.upperCase.booleanValue() != upper) {
        // mixed case - to preserve the original string, we start building what we can otherwise prevent for
        // performance
        builder().append(digitChar);
        this.upperCase = null;
      }
    }
  }

  private boolean isEmpty() {

    return !this.error && ((this.digitsLeadingZeros + this.digitsTotal) == 0) && (this.decimal == 0);
  }

  /**
   * @return the parsed value as {@link Integer}.
   * @throws NumberFormatException if parsing failed.
   */
  public Integer asInteger() {

    assert (this.numberType == NumberType.INTEGER);
    assert (this.exponent == 0);
    assert (this.decimal == 0.0);
    if (isEmpty()) {
      return null;
    }
    return Integer.valueOf((int) getLong());
  }

  /**
   * @return the parsed value as {@link Long}.
   * @throws NumberFormatException if parsing failed.
   */
  public Long asLong() {

    assert (this.numberType == NumberType.LONG);
    assert (this.exponent == 0);
    assert (this.decimal == 0.0);
    if (isEmpty()) {
      return null;
    }
    return Long.valueOf(getLong());
  }

  private long getLong() {

    if (this.error) {
      int capacity = builder().length() + 20;
      if (this.radix != 10) {
        capacity += 15;
      }
      StringBuilder errorMessage = new StringBuilder(capacity);
      errorMessage.append("For input string: \"");
      errorMessage.append(this.number);
      errorMessage.append('"');
      if (this.radix != 10) {
        errorMessage.append(" under radix ");
        errorMessage.append(this.radix);
      }
      throw new NumberFormatException(errorMessage.toString());
    }
    if (this.sign != '-') {
      return -this.mantissa;
    } else {
      return this.mantissa;
    }
  }

  /**
   * @return the parsed value as {@link Double}.
   * @throws NumberFormatException if parsing failed.
   */
  public Double asDouble() {

    assert (this.numberType == NumberType.DOUBLE);
    if (isEmpty()) {
      return null;
    }
    return Double.valueOf(getDouble());
  }

  private double getDouble() {

    double d = this.decimal;
    if (d == 0) {
      d = getLong();
      if (this.exponent != 0) {
        // TODO
        int digits = this.digitsTotal - this.digitsLeadingZeros;

      }
    }
    return d;
  }

  @Override
  public String toString() {

    if (this.number != null) {
      return this.number.toString();
    }
    // TODO
    return Long.toString(this.mantissa);
  }

}
