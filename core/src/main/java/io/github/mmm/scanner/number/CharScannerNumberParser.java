package io.github.mmm.scanner.number;

/**
 * Callback interface to parse a number as sequence of digits, signs, and symbols.
 */
public interface CharScannerNumberParser extends CharScannerRadixHandler {

  /**
   * This method will only be called if the first character if the number is '+' or '-'.
   *
   * @param sign '+' for positive number and '-' for negative number. If no sign is present this method will never be
   *        called.
   * @return {@code true} if the sign shall be accepted and further characters shall be received, {@code false} to
   *         prevent consuming the sign or any further characters and abort the process (e.g. to parse only positive
   *         numbers without any sign).
   */
  boolean sign(char sign);

  /**
   * @param digit the parsed {@code digitChar} as numeric digit to "append". Will never be negative but may be greater
   *        or equal to the radix returned by {@link #radix(int, char)} as {@link NumberFormatException} has to be
   *        handled inside the receiver.
   * @param digitChar the original digit character. In case the number shall be received as {@link String} this makes
   *        your life simpler and allows to preserve the case.
   * @return {@code true} if the given digit is accepted, {@code false} otherwise (exceeds the range of the number to
   *         parse and the digit should not be consumed). Typical implementations should always return {@code true}.
   */
  boolean digit(int digit, int digitChar);

  /**
   * @return {@code true} if the decimal dot ('.') shall be accepted, {@code false} otherwise (stop further processing
   *         e.g. to parse only integer numbers).
   */
  boolean dot();

  /**
   * @param e the exponent character. Typically 'e' or 'E' but may also be 'p' or 'P' (for power used for hex base as
   *        'E' is a hex-digit).
   * @param sign the sign character ('+' or '-') or {@code 0} for no sign.
   * @return {@code true} if the scientific notation exponent is supported and the characters shall be consumed,
   *         {@code false} otherwise (stop further processing and do not consume characters).
   */
  boolean exponent(char e, char sign);

  /**
   * This method allows handling special characters like thousand delimiter (e.g. '_' or ',') or for the start of
   * special numbers such as "NaN" or "Infinity". So for 'N' it can return "NaN" and for 'I' it can return "Infinity" to
   * support these special numbers. For a delimiter it can return @{@code other}. Otherwise return {@code null} here.
   *
   * @param other the special charater {@link String#codePointAt(int) code-point} that was found (no digit, no dot, no
   *        exponent).
   * @return {@code null} to stop without consuming the given character or a {@link String} that is expected (and shall
   *         start with the given special character). If that {@link String} was found in the scanner,
   *         {@link #special(String)} is called. Otherwise again not even the given characters gets consumed.
   */
  String special(int other);

  /**
   * @param special the special {@link String} that was found and consumed.
   * @see #special(int)
   */
  void special(String special);

}
