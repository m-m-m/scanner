package io.github.mmm.scanner.number;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * {@link CharScannerNumberParser} to parse java.lang {@link Number} values such as {@link Long}, {@link Integer},
 * {@link Double} or {@link Float}.
 */
public class CharScannerNumberParserString extends CharScannerNumberParserBase {

  private final boolean acceptDecimal;

  private final boolean addRadix;

  /**
   * The constructor.
   *
   * @param radixMode the {@link CharScannerRadixHandler} for {@link #radix(int, char)}.
   * @param acceptDecimal - {@code true} to accept decimal numbers ({@link BigDecimal}), {@code false} otherwise.
   * @param addRadix - {@code true} to append the radix syntax to the number {@link String}, {@code false} otherwise.
   */
  public CharScannerNumberParserString(CharScannerRadixHandler radixMode, boolean acceptDecimal, boolean addRadix) {

    this(radixMode, acceptDecimal, addRadix, "", false);
  }

  /**
   * The constructor.
   *
   * @param radixMode the {@link CharScannerRadixHandler} for {@link #radix(int, char)}.
   * @param acceptDecimal - {@code true} to accept decimal numbers ({@link BigDecimal}), {@code false} otherwise.
   * @param addRadix - {@code true} to append the radix syntax to the number {@link String}, {@code false} otherwise.
   * @param delimiters the {@link String} with the accepted delimiter characters (e.g. "", "_", or "_,").
   * @param supportSpecials - {@code true} to support special numbers like NaN or Infinity.
   */
  public CharScannerNumberParserString(CharScannerRadixHandler radixMode, boolean acceptDecimal, boolean addRadix,
      String delimiters, boolean supportSpecials) {

    super(radixMode, specials(delimiters, supportSpecials));
    this.acceptDecimal = acceptDecimal;
    this.addRadix = addRadix;
    this.builder = new StringBuilder();
  }

  @Override
  protected boolean isDecimal() {

    return this.acceptDecimal;
  }

  @Override
  protected void appendRadix() {

    if (this.addRadix) {
      super.appendRadix();
    }
  }

  private boolean isEmpty() {

    return this.builder.isEmpty();
  }

  /**
   * @return the number as {@link BigDecimal}.
   */
  public BigDecimal asBigDecimal() {

    if (isEmpty()) {
      return null;
    }
    // radix != 10 is not supported, simply use ONLY_10 for BigDecimal
    return new BigDecimal(this.builder.toString());
  }

  /**
   * @return the number as {@link BigInteger}.
   */
  public BigInteger asBigInteger() {

    if (isEmpty()) {
      return null;
    }
    return new BigInteger(this.builder.toString(), this.radix);
  }

  /**
   * @return the number as {@link Double} or {@code null} if no number was found.
   */
  public Double asDouble() {

    if (isEmpty()) {
      return null;
    }
    String string = this.builder.toString();
    try {
      return Double.valueOf(string);
    } catch (NumberFormatException e) {
      throw new NumberFormatException("For input: " + string);
    }
  }

  /**
   * @return the number as {@link Float} or {@code null} if no number was found.
   */
  public Float asFloat() {

    if (isEmpty()) {
      return null;
    }
    return Float.valueOf(this.builder.toString());
  }

  /**
   * @return the number as {@link String} or {@code null} if no number was found.
   */
  public String asString() {

    if (isEmpty()) {
      return null;
    }
    return this.builder.toString();
  }

}
