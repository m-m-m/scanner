package io.github.mmm.scanner.number;

/**
 * {@link CharScannerNumberParser} to parse java.lang {@link Number} values such as {@link Long}, {@link Integer},
 * {@link Double} or {@link Float}.
 */
public abstract class CharScannerNumberParserBase implements CharScannerNumberParser {

  /** {@link Double#NaN Not a Number}. */
  protected static final String NAN = "NaN";

  /** {@link Double#POSITIVE_INFINITY Infinity}. */
  protected static final String INFINITY = "Infinity";

  /** The {@link CharScannerRadixHandler} for {@link #radix(int, char)}. */
  private final CharScannerRadixHandler radixMode;

  /** the characters {@link #special(char) accepted} as delimiter. */
  private final CharScannerNumberSpecial[] specials;

  /** {@link StringBuilder} to build the number as {@link String}. Initialized by sub-class to support lazy init. */
  protected StringBuilder builder;

  /** {@code true} if an error was detected and the result can only be an exception, {@code false} otherwise. */
  protected boolean error;

  /** The initial sign character +/- or '\0' for none. */
  protected char sign;

  /** The radix of the digits, initially {@code 10} but can be changed by {@link #radix(int, char)}. */
  protected int radix;

  /** The radix character used by {@link #builder()} if lazy created only for error. */
  protected char radixChar;

  /** The total number of digits of mantissa that have been parsed. */
  protected int digitsTotal;

  /** The leading zeros of mantissa. */
  protected int digitsLeadingZeros;

  /** The current number of trailing zeros of mantissa. */
  protected int digitsTrailingZeros;

  /** The position of the decimal dot in the mantissa. */
  protected int dotPosition;

  /** The exponent symbol character e/E/p/P or '\0' for none. */
  protected char exponentSymbol;

  /** The exponent sign character +/- or '\0' for none. */
  protected char exponentSign;

  /** The total number of digits of the exponent. */
  protected int exponentDigitsTotal;

  /** The leading zeros of the exponent. */
  protected int exponentDigitsLeadingZeros;

  /** {@code true} in case of a delimiter that has not been completed by a digit, {@code false} otherwise. */
  protected boolean openDelimiter;

  /**
   * The constructor.
   *
   * @param radixMode the {@link CharScannerRadixHandler} for {@link #radix(int, char)}.
   * @param specials the {@link #special(String) special numbers} and {@link #special(int) delimiters}.
   */
  public CharScannerNumberParserBase(CharScannerRadixHandler radixMode, CharScannerNumberSpecial... specials) {

    super();
    this.radixMode = radixMode;
    this.specials = specials;
    this.radix = 10;
  }

  /**
   * @param delimiters the accepted {@link #special(int) delimiter} characters.
   * @param specialNumbers - {@code true} to accept the special numbers {@link #NAN} and {@link #INFINITY}.
   * @return the resulting {@link String} array to pass to
   *         {@link #CharScannerNumberParserBase(CharScannerRadixHandler, CharScannerNumberSpecial...) constructor}.
   */
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

  /**
   * @param c the character to check.
   * @return {@code true} in case of a digit, {@code false} otherwise.
   */
  protected final boolean isDigit(char c) {

    return Character.isDigit(c);
  }

  /**
   * @return the {@link StringBuilder} to build the number. Ensures initialization in case of lazy-init.
   */
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

  /**
   * Appends the radix to the number {@link StringBuilder}.
   */
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

  /**
   * Resets the trailing zeros if a non zero digit was found for mantissa.
   */
  protected void resetTrailingZeros() {

    this.digitsTrailingZeros = 0;
  }

  /**
   * @return {@code true} in case decimal numbers with {@link #dot()} and/or {@link #exponent(char, char) exponent} are
   *         accepted, {@code false} otherwise.
   */
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

  /**
   * Appends the {@link #exponent(char, char) exponent} symbols.
   *
   * @param lazy - {@code true} if called lazily to recreate the parsed {@link String} on numeric error, {@code false}
   *        otherwise (regular appending in {@link String} mode).
   */
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

  /**
   * @return {@code true} if the current position and state is valid to accept a digit delimiter.
   */
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

  /**
   * Interface for handling of a special number syntax.
   *
   * @see CharScannerNumberParser#special(int)
   * @see CharScannerNumberParser#special(String)
   */
  public static interface CharScannerNumberSpecial {

    /**
     * @param c the {@link CharScannerNumberParser#special(int) special character} to check.
     * @return {@code true} if the given character shall be handled by this {@link CharScannerNumberSpecial}.
     */
    boolean isSpecialStart(int c);

    /**
     * @return the {@link CharScannerNumberParser#special(String) special} {@link String} to expect.
     */
    String getSpecial();

  }

  /**
   * {@link CharScannerNumberSpecial} for digit delimiters like '_'.
   */
  public static class CharScannerNumberSpecialDelimiter implements CharScannerNumberSpecial {

    private final int delimiter;

    private final String delimiterString;

    /**
     * The constructor.
     *
     * @param delimiter the delimiter.
     */
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

  /**
   * {@link CharScannerNumberSpecial} for non-numeric number literals like {@link #NAN}.
   */
  public static class CharScannerNumberSpecialNonNumber implements CharScannerNumberSpecial {

    private final String nonNumber;

    private final char first;

    /**
     * The constructor.
     *
     * @param nonNumber the {@link #getSpecial() special} {@link String} representing the non-numeric number (e.g. NaN).
     */
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
