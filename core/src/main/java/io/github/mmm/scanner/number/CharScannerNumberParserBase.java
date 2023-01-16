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
  protected StringBuilder number;

  protected boolean error;

  protected char sign;

  protected int radix;

  protected char radixChar;

  protected int digitsTotal;

  protected int digitsLeadingZeros;

  protected int digitsTrailingZeros;

  protected int dotPosition;

  protected char exponentSymbol;

  protected char exponentSign;

  protected int exponentDigitsTotal;

  protected int exponentDigitsLeadingZeros;

  protected boolean openDelimiter;

  /**
   * The constructor.
   *
   * @param radixMode the {@link CharScannerRadixHandler} for {@link #radix(int, char)}.
   * @param specials the {@link #special(String) special numbers} and {@link #special(char) delimiters}.
   */
  public CharScannerNumberParserBase(CharScannerRadixHandler radixMode, CharScannerNumberSpecial... specials) {

    super();
    this.radixMode = radixMode;
    this.specials = specials;
    this.radix = 10;
  }

  /**
   * @param delimiters the accepted {@link #special(char) delimiter} characters.
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
      result[i++] = new CharScannerNumberSpecialDelimiter(delimiters.charAt(pos++));
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

    return this.number;
  }

  @Override
  public boolean sign(char signChar) {

    assert (this.sign == 0);
    this.sign = signChar;
    if (this.number != null) {
      this.number.append(signChar);
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
      if (this.number != null) {
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
      this.number.append('0');
      if (this.radixChar != '0') {
        this.number.append(this.radixChar);
      }
    }
  }

  @Override
  public boolean digit(int digit, char digitChar) {

    if (this.number != null) {
      this.number.append(digitChar);
    }
    if (this.exponentSymbol == 0) {
      if (digit == 0) {
        if (this.digitsTotal == this.digitsLeadingZeros) {
          this.digitsLeadingZeros++;
        } else {
          this.digitsTrailingZeros++;
        }
      } else {
        this.digitsTrailingZeros = 0;
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
      if (this.number != null) {
        this.number.append('.');
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
        this.number.append(signChar);
      }
      return true;
    }
    if (isDecimal()) {
      assert (this.exponentSymbol == 0); // method shall be called only once
      this.exponentSymbol = e;
      this.exponentSign = signChar;
      if (this.number != null) {
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
      this.number.append(this.exponentSymbol);
      if ((this.exponentSign == '+') || ((this.exponentSign == '-' && !lazy))) {
        this.number.append(this.exponentSign);
      }
    }
  }

  @Override
  public String special(char c) {

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
    if (this.number != null) {
      this.number.append(special);
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

    return this.number.toString();
  }

  /**
   * Interface for handling of a special number syntax.
   *
   * @see CharScannerNumberParser#special(char)
   * @see CharScannerNumberParser#special(String)
   */
  public static interface CharScannerNumberSpecial {

    /**
     * @param c the {@link CharScannerNumberParser#special(char) special character} to check.
     * @return {@code true} if the given character shall be handled by this {@link CharScannerNumberSpecial}.
     */
    boolean isSpecialStart(char c);

    /**
     * @return the {@link CharScannerNumberParser#special(String) special} {@link String} to expect.
     */
    String getSpecial();

  }

  /**
   * {@link CharScannerNumberSpecial} for digit delimiters like '_'.
   */
  public static class CharScannerNumberSpecialDelimiter implements CharScannerNumberSpecial {

    private final char delimiter;

    private final String delimiterString;

    /**
     * The constructor.
     *
     * @param delimiter the delimiter.
     */
    public CharScannerNumberSpecialDelimiter(char delimiter) {

      super();
      this.delimiter = delimiter;
      this.delimiterString = Character.toString(delimiter);
    }

    @Override
    public boolean isSpecialStart(char c) {

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
    public boolean isSpecialStart(char c) {

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
