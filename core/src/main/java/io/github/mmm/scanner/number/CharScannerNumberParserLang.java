package io.github.mmm.scanner.number;

import java.util.Locale;

import io.github.mmm.base.number.NumberType;

/**
 * {@link CharScannerNumberParser} to parse java.lang {@link Number} values such as {@link Long}, {@link Integer},
 * {@link Double} or {@link Float}.
 */
public class CharScannerNumberParserLang extends CharScannerNumberParserBase {

  private static final int EXPONENT_BIAS = 0x3FF; // 1024

  private static final int EXPONENT_LIMIT = 0x7FE; // 2046

  private static final long DOUBLE_EEE_NEGATIVE = 0x8000_0000_0000_0000L; // Long.MIN_VALUE

  private static final long DOUBLE_EEE_MASK_FRACTION = 0x000F_FFFF_FFFF_FFFFL;

  private static final int DOUBLE_MIN_E = -324;

  private static final double[] POWER_10_D = { //
  1.0e000, 1.0e001, 1.0e002, 1.0e003, 1.0e004, 1.0e005, 1.0e006, 1.0e007, 1.0e008, 1.0e009, //
  1.0e010, 1.0e011, 1.0e012, 1.0e013, 1.0e014, 1.0e015, 1.0e016, 1.0e017, 1.0e018, 1.0e019, //
  1.0e020, 1.0e021, 1.0e022, 1.0e023, 1.0e024, 1.0e025, 1.0e026, 1.0e027, 1.0e028, 1.0e029, //
  1.0e030, 1.0e031, 1.0e032, 1.0e033, 1.0e034, 1.0e035, 1.0e036, 1.0e037, 1.0e038, 1.0e039, //
  1.0e040, 1.0e041, 1.0e042, 1.0e043, 1.0e044, 1.0e045, 1.0e046, 1.0e047, 1.0e048, 1.0e049, //
  1.0e050, 1.0e051, 1.0e052, 1.0e053, 1.0e054, 1.0e055, 1.0e056, 1.0e057, 1.0e058, 1.0e059, //
  1.0e060, 1.0e061, 1.0e062, 1.0e063, 1.0e064, 1.0e065, 1.0e066, 1.0e067, 1.0e068, 1.0e069, //
  1.0e070, 1.0e071, 1.0e072, 1.0e073, 1.0e074, 1.0e075, 1.0e076, 1.0e077, 1.0e078, 1.0e079, //
  1.0e080, 1.0e081, 1.0e082, 1.0e083, 1.0e084, 1.0e085, 1.0e086, 1.0e087, 1.0e088, 1.0e089, //
  1.0e090, 1.0e091, 1.0e092, 1.0e093, 1.0e094, 1.0e095, 1.0e096, 1.0e097, 1.0e098, 1.0e099, //
  1.0e100, 1.0e101, 1.0e102, 1.0e103, 1.0e104, 1.0e105, 1.0e106, 1.0e107, 1.0e108, 1.0e109, //
  1.0e110, 1.0e111, 1.0e112, 1.0e113, 1.0e114, 1.0e115, 1.0e116, 1.0e117, 1.0e118, 1.0e119, //
  1.0e120, 1.0e121, 1.0e122, 1.0e123, 1.0e124, 1.0e125, 1.0e126, 1.0e127, 1.0e128, 1.0e129, //
  1.0e130, 1.0e131, 1.0e132, 1.0e133, 1.0e134, 1.0e135, 1.0e136, 1.0e137, 1.0e138, 1.0e139, //
  1.0e140, 1.0e141, 1.0e142, 1.0e143, 1.0e144, 1.0e145, 1.0e146, 1.0e147, 1.0e148, 1.0e149, //
  1.0e150, 1.0e151, 1.0e152, 1.0e153, 1.0e154, 1.0e155, 1.0e156, 1.0e157, 1.0e158, 1.0e159, //
  1.0e160, 1.0e161, 1.0e162, 1.0e163, 1.0e164, 1.0e165, 1.0e166, 1.0e167, 1.0e168, 1.0e169, //
  1.0e170, 1.0e171, 1.0e172, 1.0e173, 1.0e174, 1.0e175, 1.0e176, 1.0e177, 1.0e178, 1.0e179, //
  1.0e180, 1.0e181, 1.0e182, 1.0e183, 1.0e184, 1.0e185, 1.0e186, 1.0e187, 1.0e188, 1.0e189, //
  1.0e190, 1.0e191, 1.0e192, 1.0e193, 1.0e194, 1.0e195, 1.0e196, 1.0e197, 1.0e198, 1.0e199, //
  1.0e200, 1.0e201, 1.0e202, 1.0e203, 1.0e204, 1.0e205, 1.0e206, 1.0e207, 1.0e208, 1.0e209, //
  1.0e210, 1.0e211, 1.0e212, 1.0e213, 1.0e214, 1.0e215, 1.0e216, 1.0e217, 1.0e218, 1.0e219, //
  1.0e220, 1.0e221, 1.0e222, 1.0e223, 1.0e224, 1.0e225, 1.0e226, 1.0e227, 1.0e228, 1.0e229, //
  1.0e230, 1.0e231, 1.0e232, 1.0e233, 1.0e234, 1.0e235, 1.0e236, 1.0e237, 1.0e238, 1.0e239, //
  1.0e240, 1.0e241, 1.0e242, 1.0e243, 1.0e244, 1.0e245, 1.0e246, 1.0e247, 1.0e248, 1.0e249, //
  1.0e250, 1.0e251, 1.0e252, 1.0e253, 1.0e254, 1.0e255, 1.0e256, 1.0e257, 1.0e258, 1.0e259, //
  1.0e260, 1.0e261, 1.0e262, 1.0e263, 1.0e264, 1.0e265, 1.0e266, 1.0e267, 1.0e268, 1.0e269, //
  1.0e270, 1.0e271, 1.0e272, 1.0e273, 1.0e274, 1.0e275, 1.0e276, 1.0e277, 1.0e278, 1.0e279, //
  1.0e280, 1.0e281, 1.0e282, 1.0e283, 1.0e284, 1.0e285, 1.0e286, 1.0e287, 1.0e288, 1.0e289, //
  1.0e290, 1.0e291, 1.0e292, 1.0e293, 1.0e294, 1.0e295, 1.0e296, 1.0e297, 1.0e298, 1.0e299, //
  1.0e300, 1.0e301, 1.0e302, 1.0e303, 1.0e304, 1.0e305, 1.0e306, 1.0e307, 1.0e308 //
  };

  private static final long[] POWER_10_L = { 1L, 10L, 100L, 1_000L, 10_000L, 100_000L, 1_000_000L, 10_000_000L,
  100_000_000L, 1_000_000_000L, 10_000_000_000L, 100_000_000_000L, 1_000_000_000_000L, 10_000_000_000_000L,
  100_000_000_000_000L, 1_000_000_000_000_000L, 10_000_000_000_000_000L, 100_000_000_000_000_000L,
  1_000_000_000_000_000_000L };

  private final NumberType<?> numberType;

  private long min;

  private long minMul;

  private long mantissa;

  private long exponent;

  private Boolean upperCase;

  private double decimal;

  private int firstNonZeroDigit;

  private int digitsOverflow;

  /**
   * The constructor.
   *
   * @param radixMode the {@link CharScannerRadixHandler} for {@link #radix(int, char)}.
   * @param numberType the {@link NumberType}.
   */
  public CharScannerNumberParserLang(CharScannerRadixHandler radixMode, NumberType<?> numberType) {

    this(radixMode, numberType, "", nonDecimalMax(numberType));
  }

  /**
   * The constructor.
   *
   * @param radixMode the {@link CharScannerRadixHandler} for {@link #radix(int, char)}.
   * @param numberType the {@link NumberType}.
   * @param delimiters the accepted {@link #special(char) delimiter} characters.
   */
  public CharScannerNumberParserLang(CharScannerRadixHandler radixMode, NumberType<?> numberType, String delimiters) {

    this(radixMode, numberType, delimiters, nonDecimalMax(numberType));
  }

  /**
   * The constructor.
   *
   * @param radixMode the {@link CharScannerRadixHandler} for {@link #radix(int, char)}.
   * @param numberType the {@link NumberType}.
   * @param delimiters the accepted {@link #special(char) delimiter} characters.
   * @param maxNonDecimal the maximum allowed number (e.g. {@link Integer#MAX_VALUE} to parse an {@link Integer} value).
   */
  public CharScannerNumberParserLang(CharScannerRadixHandler radixMode, NumberType<?> numberType, String delimiters,
      long maxNonDecimal) {

    super(radixMode, specials(delimiters, (numberType == null) || (numberType.isImpreciseDecimal())));
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

    if (this.numberType == null) {
      return true;
    }
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

    if (this.builder == null) {
      this.builder = new StringBuilder();
      long v = this.mantissa;
      if (this.sign == '+') {
        this.builder.append(this.sign);
      }
      if (this.sign != '-') {
        v = -v;
      }
      appendRadix();
      appendZeros(this.digitsLeadingZeros);
      String num = Long.toString(v, this.radix);
      if (Boolean.TRUE.equals(this.upperCase)) {
        num = num.toUpperCase(Locale.ROOT);
      }
      if (this.dotPosition >= 0) {
        this.builder.append(num.substring(0, this.dotPosition));
        this.builder.append('.');
        this.builder.append(num.substring(this.dotPosition));
        appendZeros(this.digitsTrailingZeros);
      } else {
        this.builder.append(num);
      }
      if (this.exponentSymbol != 0) {
        appendExponent(true);
        appendZeros(this.exponentDigitsLeadingZeros);
        this.builder.append(Long.toString(this.exponent));
      }
    }
    return this.builder;
  }

  private void appendZeros(int count2) {

    for (int i = 0; i < count2; i++) {
      this.builder.append('0');
    }
  }

  private void error(char c) {

    if (this.builder == null) {
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
    if (this.exponentSymbol == 0) {
      if (this.minMul == 0) {
        this.minMul = this.min / this.radix;
      }
      preventCase(digit, digitChar);
      if ((this.dotPosition >= 0) && (this.digitsTrailingZeros > 0)) {
        return true; // after . we first ignore trailing zeros, see resetTrailingZeros() for later non-zero digit
      }
      if ((digit > 0) && (this.firstNonZeroDigit == 0)) {
        this.firstNonZeroDigit = digit;
      }
      if (digit >= this.radix) {
        error(digitChar);
        return true;
      }
      if (this.mantissa < this.minMul) {
        if (isDecimal()) {
          if (this.builder == null) {
            // we have to create builder as otherwise we are loosing information that we cannot recreate
            builder().append(digitChar);
            // overflow, rounding of next digit
            this.digitsOverflow = this.digitsTotal;
            if ((2 * digit) >= this.radix) {
              long m = this.mantissa - 1;
              if (m < this.mantissa) {
                this.mantissa = m;
              }
            }
          }
        } else {
          error(digitChar);
        }
        return true;
      } else {
        this.mantissa = this.mantissa * this.radix;
        if (this.mantissa < this.min + digit) {
          this.mantissa = this.mantissa / this.radix;
          error(digitChar);
          return true;
        }
        this.mantissa = this.mantissa - digit;
      }
    } else {
      if (this.exponentDigitsTotal > this.exponentDigitsLeadingZeros) { // ignore leading zeros
        // exponent always has radix 10
        if (this.exponentSign == '-') {
          this.exponent = this.exponent * 10 - digit;
        } else {
          this.exponent = this.exponent * 10 + digit;
        }
      }
    }
    return true;
  }

  @Override
  protected void resetTrailingZeros() {

    if ((this.digitsTrailingZeros > 0) && (this.dotPosition >= 0) && !this.error) {
      // we first ignored training zeros after dot
      // e.g. "1.2000000" so we have mantissa 12 with dotPosition = 1
      // now we have hit a non-zero digit "1.20000009" so we have to proceed as if we had already consumed the zeros
      if (this.radix == 10) {
        if (this.digitsTrailingZeros < POWER_10_L.length) {
          long m = this.mantissa * POWER_10_L[this.digitsTrailingZeros];
          if (m >= this.mantissa) {
            this.digitsOverflow = this.digitsTotal - this.digitsTrailingZeros;
          } else {
            this.mantissa = m;
          }
        } else {
          this.digitsOverflow = this.digitsTotal - this.digitsTrailingZeros;
        }
      } else {
        int bits = getRadixBits();
        long l;
        if (this.mantissa < 0) {
          l = -this.mantissa >>> -bits - 1;
        } else {
          l = this.mantissa >>> -bits - 1;
        }
        if (l != 0) { // l contains the bits that are shifted out
          this.digitsOverflow = this.digitsTotal - this.digitsTrailingZeros;
        } else {
          this.mantissa = this.mantissa << bits;
        }
      }
      if (this.error) {
        builder();
      }
    }
    super.resetTrailingZeros();
  }

  private int getRadixBits() {

    switch (this.radix) {
      case 64:
        return 6;
      case 32:
        return 5;
      case 16:
        return 4;
      case 8:
        return 3;
      case 4:
        return 2;
      case 2:
        return 1;
      default:
        // currently unsupported (except 10)
        throw new IllegalStateException("Illegal radix: " + this.radix);
    }
  }

  private void preventCase(int digit, char digitChar) {

    if ((digit > 9) && (this.builder == null)) { // prevent case of letter digits (e.g. hex)
      boolean upper = Character.isUpperCase(digitChar);
      if (this.upperCase == null) {
        this.upperCase = Boolean.valueOf(upper);
      } else if (this.upperCase.booleanValue() != upper) {
        // mixed case - to preserve original string, we start building what we can otherwise prevent for performance
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

    assert (this.numberType == NumberType.INTEGER) || (this.numberType == null);
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

    assert (this.numberType == NumberType.LONG) || (this.numberType == null);
    assert (this.exponent == 0);
    assert (this.decimal == 0.0);
    if (isEmpty()) {
      return null;
    }
    return Long.valueOf(getLong());
  }

  private long getLong() {

    throwOnError();
    if (this.sign != '-') {
      return -this.mantissa;
    } else {
      return this.mantissa;
    }
  }

  private void throwOnError() {

    if (this.error) {
      throw new NumberFormatException(getErrorMessage());
    }
  }

  private String getErrorMessage() {

    int capacity = builder().length() + 20;
    if (this.radix != 10) {
      capacity += 15;
    }
    StringBuilder errorMessage = new StringBuilder(capacity);
    errorMessage.append("For input string: \"");
    errorMessage.append(this.builder);
    errorMessage.append('"');
    if (this.radix != 10) {
      errorMessage.append(" under radix ");
      errorMessage.append(this.radix);
    }
    return errorMessage.toString();
  }

  /**
   * @return the parsed value as {@link Double}.
   * @throws NumberFormatException if parsing failed.
   */
  public Double asDouble() {

    assert (this.numberType == NumberType.DOUBLE) || (this.numberType == null);
    if (isEmpty()) {
      return null;
    }
    return Double.valueOf(getDouble());
  }

  private double getDouble() {

    if (this.decimal != 0.0) { // if d is not zero, we already have the value such as NaN or +/-Infinity, ...
      return this.decimal;
    }
    long m = getLong(); // mantissa / significant
    if (m == 0) {
      return 0; // if mantissa is 0, result is 0 and exponent does not matter
    }
    if ((this.exponent == 0) && (this.dotPosition < 0) && (this.digitsOverflow <= 0)) {
      return m;
    }
    if (this.radix == 10) {
      long ex = this.exponent;
      if (this.dotPosition >= 0) {
        if (this.digitsOverflow > 0) {
          ex = ex - (this.digitsOverflow - this.digitsTrailingZeros - this.dotPosition);
          // rounding
          if (m < 0) {
            m--;
          } else {
            m++;
          }
        } else {
          ex = ex - (this.digitsTotal - this.digitsTrailingZeros - this.dotPosition);
        }
      } else if (this.digitsOverflow > 0) {
        ex = ex + (this.digitsTotal - this.digitsOverflow + 1);
        // rounding
        if (m < 0) {
          m--;
        } else {
          m++;
        }
      }
      int e = (int) ex;
      if (e != ex) { // exponent overflow?
        if (this.exponent < 0) {
          return zero();
        } else {
          return infinity();
        }
      }
      if (e > 0) {
        if (e < POWER_10_D.length) {
          return m * POWER_10_D[e];
        }
        return infinity();
      } else {
        if (-e < POWER_10_D.length) {
          return m / POWER_10_D[-e];
        } else if (e < DOUBLE_MIN_E) {
          return zero();
        } else {
          if (this.sign == '-') {
            return -Double.MIN_NORMAL;
          } else {
            return Double.MIN_NORMAL;
          }
        }
      }
    } else {
      int radixBits = getRadixBits();
      long ex = this.exponent;
      int exOffset;
      if (this.dotPosition >= 0) {
        exOffset = this.dotPosition - this.digitsLeadingZeros - 1;
      } else {
        exOffset = this.digitsTotal - this.digitsLeadingZeros - 1;
      }
      ex = ex + (radixBits * exOffset);
      if (this.firstNonZeroDigit == 1) {
        ex += 0;
      } else if (this.firstNonZeroDigit <= 3) {
        ex += 1;
      } else if (this.firstNonZeroDigit <= 7) {
        ex += 2;
      } else if (this.firstNonZeroDigit <= 15) {
        ex += 3;
      } else if (this.firstNonZeroDigit <= 31) {
        ex += 4;
      } else if (this.firstNonZeroDigit <= 63) {
        ex += 5;
      }
      long ieee754Bits = 0;
      if (m < 0) {
        if (m == Long.MIN_VALUE) {
          // TODO special handling to prevent overflow...
        }
        m = -m;
        ieee754Bits = DOUBLE_EEE_NEGATIVE;
      }
      ex += EXPONENT_BIAS;
      if ((ex <= 0) || (ex > EXPONENT_LIMIT)) {
        return infinity();
      } else {
        ieee754Bits = ieee754Bits | (ex << 52);
      }
      int bits = Integer.numberOfLeadingZeros((int) (m >>> 32));
      if (bits == 32) {
        bits = 32 + Integer.numberOfLeadingZeros((int) m);
      }
      int shift = bits - 12 + 1; // 12 = 64 - 52 = bits for sign(1) + exponent(11) [without mantissa(52)]
      if (shift > 0) {
        m = m << shift;
      } else if (shift < 0) {
        if ((m << -bits - 1) != 0) {
          // rounding: if last lost bit was 1, then add 1 to m
          this.digitsOverflow = this.digitsTotal;
        }
        m = m >> -shift;
      } // else nothing to shift
      if (this.digitsOverflow != 0) {
        m = m + 1;
      }
      ieee754Bits = ieee754Bits | (m & DOUBLE_EEE_MASK_FRACTION);
      return Double.longBitsToDouble(ieee754Bits);
    }
  }

  private double zero() {

    if (this.sign == '-') {
      return -0.0D;
    } else {
      return +0.0D;
    }
  }

  private double infinity() {

    if (this.sign == '-') {
      return Double.NEGATIVE_INFINITY;
    } else {
      return Double.POSITIVE_INFINITY;
    }
  }

  @Override
  public String toString() {

    if (this.error) {
      return getErrorMessage();
    }
    if (this.builder != null) {
      return this.builder.toString();
    }
    if ((this.dotPosition >= 0) || (this.exponentSymbol != 0) || (this.decimal != 0.0)) {
      return Double.toString(getDouble());
    } else {
      return Long.toString(this.mantissa);
    }
  }

}
