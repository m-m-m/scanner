package io.github.mmm.scanner.number;

import io.github.mmm.scanner.CharStreamScanner;

/**
 * {@link Enum} for to decide on {@link #radix(int, char) radix} when parsing numbers.
 *
 * @see CharScannerNumberParser#radix(int, char)
 * @see CharStreamScanner#readLong(CharScannerRadixHandler)
 * @see CharStreamScanner#readInteger(CharScannerRadixHandler)
 */
public enum CharScannerRadixMode implements CharScannerRadixHandler {

  /** Accept any radix (2, 8, 10, 16). */
  ALL,

  /** Accept only decimal raidx (10). */
  ONLY_10,

  /** Accept all radix except octal (2, 10, 16). */
  NO_OCTAL;

  @Override
  public int radix(int radix, char symbol) {

    switch (this) {
      case ALL:
        return radix;
      case ONLY_10:
        return 0;
      case NO_OCTAL:
        if (radix == 8) {
          return 0;
        }
    }
    return radix;
  }

}
