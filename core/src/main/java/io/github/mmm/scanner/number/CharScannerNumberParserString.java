package io.github.mmm.scanner.number;

import java.math.BigDecimal;
import java.math.BigInteger;

/// [CharScannerNumberParser] to parse java.lang [Number] values such as [Long], [Integer],
/// [Double] or [Float].
public class CharScannerNumberParserString extends CharScannerNumberParserBase {

  private final boolean acceptDecimal;

  private final boolean addRadix;

  /// The constructor.
  ///
  /// @param radixMode the [CharScannerRadixHandler] for [#radix(int, char)].
  /// @param acceptDecimal - `true` to accept decimal numbers ([BigDecimal]), `false` otherwise.
  /// @param addRadix - `true` to append the radix syntax to the number [String], `false` otherwise.
  public CharScannerNumberParserString(CharScannerRadixHandler radixMode, boolean acceptDecimal, boolean addRadix) {

    this(radixMode, acceptDecimal, addRadix, "", false);
  }

  /// The constructor.
  ///
  /// @param radixMode the [CharScannerRadixHandler] for [#radix(int, char)].
  /// @param acceptDecimal - `true` to accept decimal numbers ([BigDecimal]), `false` otherwise.
  /// @param addRadix - `true` to append the radix syntax to the number [String], `false` otherwise.
  /// @param delimiters the [String] with the accepted delimiter characters (e.g. "", "_", or "_,").
  /// @param supportSpecials - `true` to support special numbers like NaN or Infinity.
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

    return this.builder.length() == 0;
  }

  /// @return the number as [BigDecimal].
  public BigDecimal asBigDecimal() {

    if (isEmpty()) {
      return null;
    }
    // radix != 10 is not supported, simply use ONLY_10 for BigDecimal
    return new BigDecimal(this.builder.toString());
  }

  /// @return the number as [BigInteger].
  public BigInteger asBigInteger() {

    if (isEmpty()) {
      return null;
    }
    return new BigInteger(this.builder.toString(), this.radix);
  }

  /// @return the number as [Double] or `null` if no number was found.
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

  /// @return the number as [Float] or `null` if no number was found.
  public Float asFloat() {

    if (isEmpty()) {
      return null;
    }
    return Float.valueOf(this.builder.toString());
  }

  /// @return the number as [String] or `null` if no number was found.
  public String asString() {

    if (isEmpty()) {
      return null;
    }
    return this.builder.toString();
  }

}
