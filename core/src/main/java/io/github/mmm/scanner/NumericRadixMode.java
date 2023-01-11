package io.github.mmm.scanner;

/**
 * {@link Enum} for to decide on {@link #acceptRadix(int) accepting radix} when parsing numbers.
 *
 * @see CharStreamScanner#readLong(NumericRadixMode, boolean)
 * @see CharStreamScanner#readInteger(NumericRadixMode, boolean)
 */
public enum NumericRadixMode {

  /** Accept any radix (2, 8, 10, 16). */
  ALL,

  /** Accept only decimal raidx (10). */
  ONLY_10,

  /** Accept all radix except octal (2, 10, 16). */
  NO_OCTAL;

  /**
   * @param radix the radix. Will be {@code 16} for "0x", {@code 2} for "0b", {@code 8} for "0" followed by a digit, and
   *        {@code 10} in any other case.
   * @return the actual radix to use for further processing. If {@code 0} is returned the radix is not accepted (only
   *         valid if given {@code radix} was not {@code 10}). {@link #ALL} will always return the given {@code radix}.
   *         Use {@link #NO_OCTAL} to prevent octal parsing (Java/C legacy) where leasing zeros can cause problems as
   *         "010" is "8" instead of "10". {@link #ONLY_10} will only accept the decimal radix.
   */
  public int acceptRadix(int radix) {

    switch (this) {
      case ALL:
        return radix;
      case ONLY_10:
        if (radix == 10) {
          return 10;
        }
        return 0;
      case NO_OCTAL:
        if (radix == 8) {
          return 10;
        }
    }
    return radix;
  }

}
