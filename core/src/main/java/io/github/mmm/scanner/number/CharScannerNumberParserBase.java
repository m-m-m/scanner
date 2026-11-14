package io.github.mmm.scanner.number;

/// [CharScannerNumberParser] to parse java.lang [Number] values such as [Long], [Integer],
/// [Double] or [Float].
public abstract class CharScannerNumberParserBase implements CharScannerNumberParser {

  /// [Not a Number][Double#NaN].
  protected static final String NAN = "NaN";

  /// [Infinity][Double#POSITIVE_INFINITY].
  protected static final String INFINITY = "Infinity";

  /// The [CharScannerRadixHandler] for [#radix(int, char)].
  private final CharScannerRadixHandler radixMode;

  /// the characters [accepted][#special(char)] as delimiter.
  private final CharScannerNumberSpecial[] specials;

  /// [StringBuilder] to build the number as [String]. Initialized by sub-class to support lazy init.
  protected StringBuilder builder;

  /// `true` if an error was detected and the result can only be an exception, `false` otherwise.
  protected boolean error;

  /// The initial sign character +/- or '\0' for none.
  protected char sign;

  /// The radix of the digits, initially `10` but can be changed by [#radix(int, char)].
  protected int radix;

  /// The radix character used by [#builder()] if lazy created only for error.
  protected char radixChar;

  /// The total number of digits of mantissa that have been parsed.
  protected int digitsTotal;

  /// The leading zeros of mantissa.
  protected int digitsLeadingZeros;

  /// The current number of trailing zeros of mantissa.
  protected int digitsTrailingZeros;

  /// The position of the decimal dot in the mantissa.
  protected int dotPosition;

  /// The exponent symbol character e/E/p/P or '\0' for none.
  protected char exponentSymbol;

  /// The exponent sign character +/- or '\0' for none.
  protected char exponentSign;

  /// The total number of digits of the exponent.
  protected int exponentDigitsTotal;

  /// The leading zeros of the exponent.
  protected int exponentDigitsLeadingZeros;

  /// `true` in case of a delimiter that has not been completed by a digit, `false` otherwise.
  protected boolean openDelimiter;

  /// The constructor.
  ///
  /// @param radixMode the [CharScannerRadixHandler] for [#radix(int, char)].
  /// @param specials the [special numbers][#special(String)] and [delimiters][#special(int)].
  public CharScannerNumberParserBase(CharScannerRadixHandler radixMode, CharScannerNumberSpecial... specials) {

    super();
    this.radixMode = radixMode;
    this.specials = specials;
    this.radix = 10;
  }

  /// @param delimiters the accepted [delimiter][#special(int)] characters.
  /// @param specialNumbers - `true` to accept the special numbers [#NAN] and [#INFINITY].
  /// @return the resulting [String] array to pass to
  ///         [constructor][#CharScannerNumberParserBase(CharScannerRadixHandler, CharScannerNumberSpecial...)].
  public static CharScannerNumberSpecial[] specials(String delimiters, boolean specialNumbers) {

    int len = delimiters.length();
    if (specialNumbers) {
      len += 2;
    }
    CharScannerNumberSpecial[] result = new CharScannerNumberSpecial[len];
    int i = 0;
    if (specialNumbers) {
      result[i++] = new CharScannerNumberSpecialNonNumber(NAN);
      result[i++] = new CharScannerNumberSpecialNonNumber(INFINITY);
    }
    int pos = 0;
    while (i < len) {
      result[i++] = new CharScannerNumberSpecialDelimiter(delimiters.codePointAt(pos++));
    }
    return result;
  }

  /// @param c the character to check.
  /// @return `true` in case of a digit, `false` otherwise.
  protected final boolean isDigit(char c) {

    return Character.isDigit(c);
  }

  /// @return the [StringBuilder] to build the number. Ensures initialization in case of lazy-init.
  protected StringBuilder builder() {

    return this.builder;
  }

  @Override
  public boolean sign(char signChar) {

    assert (this.sign == 0);
    this.sign = signChar;
    if (this.builder != null) {
      this.builder.append(signChar);
    }
    return true;
  }

  @Override
  public int radix(int newRadix, char c) {

    int r = this.radixMode.radix(newRadix, c);
    if (r != 0) {
      this.radix = r;
      if ((r == 8) && isDigit(c)) {
        this.radixChar = '0';
      } else {
        this.radixChar = c;
      }
      if (this.builder != null) {
        appendRadix();
      }
    }
    return r;
  }

  /// Appends the radix to the number [StringBuilder].
  protected void appendRadix() {

    if (this.radixChar != 0) {
      this.builder.append('0');
      if (this.radixChar != '0') {
        this.builder.append(this.radixChar);
      }
    }
  }

  @Override
  public boolean digit(int digit, int digitChar) {

    if (this.builder != null) {
      this.builder.appendCodePoint(digitChar);
    }
    if (this.exponentSymbol == 0) {
      if (digit == 0) {
        if (this.digitsTotal == this.digitsLeadingZeros) {
          this.digitsLeadingZeros++;
        } else {
          this.digitsTrailingZeros++;
        }
      } else {
        resetTrailingZeros();
      }
      this.digitsTotal++;
    } else {
      if ((digit == 0) && (this.exponentDigitsTotal == this.exponentDigitsLeadingZeros)) {
        this.exponentDigitsLeadingZeros++;
      }
      this.exponentDigitsTotal++;
    }
    return true;
  }

  /// Resets the trailing zeros if a non zero digit was found for mantissa.
  protected void resetTrailingZeros() {

    this.digitsTrailingZeros = 0;
  }

  /// @return `true` in case decimal numbers with [#dot()] and/or [exponent][#exponent(char, char)] are
  ///         accepted, `false` otherwise.
  protected abstract boolean isDecimal();

  @Override
  public boolean dot() {

    if (isDecimal()) {
      if (this.dotPosition == -1) { // first dot?
        this.dotPosition = this.digitsTotal;
      } else {
        this.error = true; // only one dot allowed
        builder();
      }
      if (this.builder != null) {
        this.builder.append('.');
      }
      return true;
    }
    return false;
  }

  @Override
  public boolean exponent(char e, char signChar) {

    if (this.openDelimiter) {
      this.error = true;
      builder().append(e);
      if (signChar != 0) {
        this.builder.append(signChar);
      }
      return true;
    }
    if (isDecimal()) {
      assert (this.exponentSymbol == 0); // method shall be called only once
      this.exponentSymbol = e;
      this.exponentSign = signChar;
      if (this.builder != null) {
        appendExponent(false);
      }
      return true;
    }
    return false;
  }

  /// Appends the [exponent][#exponent(char, char)] symbols.
  ///
  /// @param lazy - `true` if called lazily to recreate the parsed [String] on numeric error, `false`
  ///        otherwise (regular appending in [String] mode).
  protected void appendExponent(boolean lazy) {

    if (this.exponentSymbol != 0) {
      this.builder.append(this.exponentSymbol);
      if ((this.exponentSign == '+') || ((this.exponentSign == '-' && !lazy))) {
        this.builder.append(this.exponentSign);
      }
    }
  }

  @Override
  public String special(int c) {

    for (CharScannerNumberSpecial special : this.specials) {
      if (special.isSpecialStart(c)) {
        if (special instanceof CharScannerNumberSpecialNonNumber) {
          if (this.digitsTotal > 0) {
            return null;
          }
        }
        return special.getSpecial();
      }
    }
    return null;
  }

  @Override
  public void special(String special) {

    if (special.length() == 1) {
      this.openDelimiter = true;
    }
    if (this.builder != null) {
      this.builder.append(special);
    }
  }

  /// @return `true` if the current position and state is valid to accept a digit delimiter.
  protected final boolean isValidDelimiterPosition() {

    if (this.exponentSign != 0) {
      if (this.exponentDigitsTotal == 0) {
        return false;
      }
    } else {
      if (this.digitsTotal == 0) {
        return false; // no digit at all ("+_1" is invalid)
      }
      if (this.dotPosition == this.digitsTotal) {
        return false; // no digit after dot before exp ("1._" is invalid)
      }
    }
    return true;
  }

  @Override
  public String toString() {

    return this.builder.toString();
  }

  /// Interface for handling of a special number syntax.
  ///
  /// @see CharScannerNumberParser#special(int)
  /// @see CharScannerNumberParser#special(String)
  public static interface CharScannerNumberSpecial {

    /// @param c the [special character][CharScannerNumberParser#special(int)] to check.
    /// @return `true` if the given character shall be handled by this [CharScannerNumberSpecial].
    boolean isSpecialStart(int c);

    /// @return the [special][CharScannerNumberParser#special(String)] [String] to expect.
    String getSpecial();

  }

  /// [CharScannerNumberSpecial] for digit delimiters like '_'.
  public static class CharScannerNumberSpecialDelimiter implements CharScannerNumberSpecial {

    private final int delimiter;

    private final String delimiterString;

    /// The constructor.
    ///
    /// @param delimiter the delimiter.
    public CharScannerNumberSpecialDelimiter(int delimiter) {

      super();
      this.delimiter = delimiter;
      this.delimiterString = Character.toString(delimiter);
    }

    @Override
    public boolean isSpecialStart(int c) {

      if (c == this.delimiter) {
        // double d = 1_2.0_0e+1_0;
        // double d = 1_2.0_0e+1_0;
        return true;
      }
      return false;
    }

    @Override
    public String getSpecial() {

      return this.delimiterString;
    }

  }

  /// [CharScannerNumberSpecial] for non-numeric number literals like [#NAN].
  public static class CharScannerNumberSpecialNonNumber implements CharScannerNumberSpecial {

    private final String nonNumber;

    private final char first;

    /// The constructor.
    ///
    /// @param nonNumber the [special][#getSpecial()] [String] representing the non-numeric number (e.g. NaN).
    public CharScannerNumberSpecialNonNumber(String nonNumber) {

      super();
      this.nonNumber = nonNumber;
      this.first = nonNumber.charAt(0);
    }

    @Override
    public boolean isSpecialStart(int c) {

      if (c == this.first) {
        return true; // +/- can be followed by "NaN" or "Infinity" but no digits before
      }
      return false;
    }

    @Override
    public String getSpecial() {

      return this.nonNumber;
    }

  }

}
