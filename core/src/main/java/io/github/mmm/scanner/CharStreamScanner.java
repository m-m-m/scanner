/* Copyright (c) The m-m-m Team, Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0 */
package io.github.mmm.scanner;

import io.github.mmm.base.filter.CharFilter;
import io.github.mmm.base.text.TextFormatMessageType;
import io.github.mmm.base.text.TextFormatProcessor;
import io.github.mmm.scanner.number.CharScannerNumberParser;
import io.github.mmm.scanner.number.CharScannerRadixHandler;
import io.github.mmm.scanner.number.CharScannerRadixMode;

/**
 * This is the interface for a scanner that can be used to parse a stream or sequence of characters.
 */
public interface CharStreamScanner extends TextFormatProcessor {

  /**
   * The NULL character {@code '\0'} used to indicate the end of stream (EOS).<br>
   * ATTENTION: Do not confuse and mix {@code '\0'} with {@code '0'}.
   *
   * @see #next()
   * @see #peek()
   */
  char EOS = '\0';

  /**
   * This method determines if there is at least one more character available.
   *
   * @return {@code true} if there is at least one character available, {@code false} if the end of data has been
   *         reached.
   */
  boolean hasNext();

  /**
   * This method reads the current character from the stream and increments the index stepping to the next character.
   * You should {@link #hasNext() check} if a character is available before calling this method. Otherwise if your
   * stream may contain the NUL character ('\0') you can not distinguish if the end of the stream was reached or you
   * actually read the NUL character.
   *
   * @return the {@link #next()} character or {@link #EOS} if none is {@link #hasNext() available}.
   */
  char next();

  /**
   * This method reads the current character without {@link #next() consuming} characters and will therefore not change
   * the state of this scanner.
   *
   * @return the current character or {@link #EOS} if none is {@link #hasNext() available}.
   */
  char peek();

  /**
   * Like {@link #peek()} but with further lookahead.<br>
   * <b>Attention:</b><br>
   * This method requires lookahead. For implementations that are backed by an underlying stream (or reader) the given
   * {@code lookaheadOffset} shall not exceed the available lookahead size (buffer capacity given at construction time).
   * Otherwise the method may fail.
   *
   * @param lookaheadOffset the lookahead offset. If {@code 0} this method will behave like {@link #peek()}. In case of
   *        {@code 1} it will return the character after the next one and so forth.
   * @return the {@link #peek() peeked} character at the given {@code lookaheadOffset} or {@link #EOS} if no such
   *         character exists.
   */
  char peek(int lookaheadOffset);

  /**
   * This method peeks the number of {@link #peek() next characters} given by {@code count} and returns them as
   * {@link String}. If there are less characters {@link #hasNext() available} the returned {@link String} will be
   * shorter than {@code count} and only contain the available characters. Unlike {@link #read(int)} this method does
   * not {@link #next() consume} the characters and will therefore not change the state of this scanner.<br>
   * <b>Attention:</b><br>
   * This method requires lookahead. For implementations that are backed by an underlying stream (or reader) the given
   * {@code count} shall not exceed the available lookahead size (buffer capacity given at construction time). Otherwise
   * the method may fail.
   *
   * @param count is the number of characters to peek. You may use {@link Integer#MAX_VALUE} to peek until the end of
   *        text (EOT) if the data-size is suitable.
   * @return a string with the given number of characters or all available characters if less than {@code count}. Will
   *         be the empty string if no character is {@link #hasNext() available} at all.
   */
  String peekString(int count);

  /**
   * @param filter the {@link CharFilter} {@link CharFilter#accept(char) accepting} only the characters to peek.
   * @param maxLen the maximum number of characters to peek (to get as lookahead without modifying this stream).
   * @return a {@link String} with the {@link #peek() peeked} characters of the given {@code maxLen} or less if a
   *         character was hit that is <em>not</em> {@link CharFilter#accept(char) accepted} by the given {@code filter}
   *         or the end-of-text has been reached before. The state of this stream remains unchanged.
   * @see #readWhile(CharFilter)
   * @see #skip(int)
   */
  String peekWhile(CharFilter filter, int maxLen);

  /**
   * This method reads the number of {@link #next() next characters} given by {@code count} and returns them as string.
   * If there are less characters {@link #hasNext() available} the returned string will be shorter than {@code count}
   * and only contain the available characters.
   *
   * @param count is the number of characters to read. You may use {@link Integer#MAX_VALUE} to read until the end of
   *        data if the data-size is suitable.
   * @return a string with the given number of characters or all available characters if less than {@code count}. Will
   *         be the empty string if no character is {@link #hasNext() available} at all.
   */
  String read(int count);

  /**
   * This method reads the number of {@link #next() next characters} given by {@code count} and
   * {@link StringBuilder#append(char) appends} them to the given {@link StringBuilder}. If there are less characters
   * {@link #hasNext() available} then only the remaining characters will be appended resulting in less characters than
   * {@code count}.
   *
   * @param count is the number of characters to read. You may use {@link Integer#MAX_VALUE} to read until the end of
   *        data if the data-size is suitable.
   * @param builder the {@link StringBuilder} where to {@link StringBuilder#append(char) append} the characters to read.
   */
  void read(int count, StringBuilder builder);

  /**
   * @return the position in the sequence to scan or in other words the number of characters that have been read. Will
   *         initially be {@code 0}. Please note that this API is designed for scanning textual content (for parsers).
   *         Therefore we consider 2.1 terabyte as a suitable {@link Integer#MAX_VALUE limit}.
   */
  int getPosition();

  /**
   * This method reads all {@link #next() next characters} until the given {@code stop} character or the end is reached.
   * <br>
   * After the call of this method, the current index will point to the next character after the (first) {@code stop}
   * character or to the end if NO such character exists.
   *
   * @param stop is the character to read until.
   * @param acceptEnd if {@code true} the end of data will be treated as {@code stop}, too.
   * @return the string with all read characters excluding the {@code stop} character or {@code null} if there was no
   *         {@code stop} character and {@code acceptEnd} is {@code false}.
   */
  String readUntil(char stop, boolean acceptEnd);

  /**
   * This method reads all {@link #next() next characters} until the first character {@link CharFilter#accept(char)
   * accepted} by the given {@code filter} or the end is reached. <br>
   * After the call of this method, the current index will point to the first {@link CharFilter#accept(char) accepted}
   * stop character or to the end if NO such character exists.
   *
   * @param filter is used to {@link CharFilter#accept(char) decide} where to stop.
   * @param acceptEnd if {@code true} if end of data should be treated like the {@code stop} character and the rest of
   *        the text will be returned, {@code false} otherwise (to return {@code null} if the end of data was reached
   *        and the scanner has been consumed).
   * @return the string with all read characters not {@link CharFilter#accept(char) accepted} by the given
   *         {@link CharFilter} or {@code null} if there was no {@link CharFilter#accept(char) accepted} character and
   *         {@code acceptEnd} is {@code false}.
   */
  String readUntil(CharFilter filter, boolean acceptEnd);

  /**
   * This method reads all {@link #next() next characters} until the first character {@link CharFilter#accept(char)
   * accepted} by the given {@code filter}, the given {@code stop} {@link String} or the end is reached. <br>
   * After the call of this method, the current index will point to the first {@link CharFilter#accept(char) accepted}
   * stop character, or to the first character of the given {@code stop} {@link String} or to the end if NO such
   * character exists.
   *
   * @param filter is used to {@link CharFilter#accept(char) decide} where to stop.
   * @param acceptEnd if {@code true} if the end of data should be treated like the {@code stop} character and the rest
   *        of the text will be returned, {@code false} otherwise (to return {@code null} if end of data was reached and
   *        the scanner has been consumed).
   * @param stop the {@link String} where to stop consuming data. Should be at least two characters long (otherwise
   *        accept by {@link CharFilter} instead).
   * @return the string with all read characters not {@link CharFilter#accept(char) accepted} by the given
   *         {@link CharFilter} or until the given {@code stop} {@link String} was detected. If end of data was reached
   *         without a stop signal the entire rest of the data is returned or {@code null} if {@code acceptEnd} is
   *         {@code false}.
   */
  default String readUntil(CharFilter filter, boolean acceptEnd, String stop) {

    return readUntil(filter, acceptEnd, stop, false);
  }

  /**
   * This method reads all {@link #next() next characters} until the first character {@link CharFilter#accept(char)
   * accepted} by the given {@code filter}, the given {@code stop} {@link String} or the end is reached. <br>
   * After the call of this method, the current index will point to the first {@link CharFilter#accept(char) accepted}
   * stop character, or to the first character of the given {@code stop} {@link String} or to the end if NO such
   * character exists.
   *
   * @param filter is used to {@link CharFilter#accept(char) decide} where to stop.
   * @param acceptEnd if {@code true} if the end of data should be treated like the {@code stop} character and the rest
   *        of the text will be returned, {@code false} otherwise (to return {@code null} if the end of data was reached
   *        and the scanner has been consumed).
   * @param stop the {@link String} where to stop consuming data. Should be at least two characters long (otherwise
   *        accept by {@link CharFilter} instead).
   * @param ignoreCase - if {@code true} the case of the characters is ignored when compared with characters from
   *        {@code stop} {@link String}.
   * @return the string with all read characters not {@link CharFilter#accept(char) accepted} by the given
   *         {@link CharFilter} or until the given {@code stop} {@link String} was detected. If the end of data was
   *         reached without a stop signal the entire rest of the data is returned or {@code null} if {@code acceptEnd}
   *         is {@code false}.
   */
  default String readUntil(CharFilter filter, boolean acceptEnd, String stop, boolean ignoreCase) {

    return readUntil(filter, acceptEnd, stop, ignoreCase, false);
  }

  /**
   * This method reads all {@link #next() next characters} until the first character {@link CharFilter#accept(char)
   * accepted} by the given {@code filter}, the given {@code stop} {@link String} or the end is reached. <br>
   * After the call of this method, the current index will point to the first {@link CharFilter#accept(char) accepted}
   * stop character, or to the first character of the given {@code stop} {@link String} or to the end if NO such
   * character exists.
   *
   * @param filter is used to {@link CharFilter#accept(char) decide} where to stop.
   * @param acceptEnd if {@code true} if the end of data should be treated like the {@code stop} character and the rest
   *        of the text will be returned, {@code false} otherwise (to return {@code null} if the end of data was reached
   *        and the scanner has been consumed).
   * @param stop the {@link String} where to stop consuming data. Should be at least two characters long (otherwise
   *        accept by {@link CharFilter} instead).
   * @param ignoreCase - if {@code true} the case of the characters is ignored when compared with characters from
   *        {@code stop} {@link String}.
   * @param trim - {@code true} if the result should be {@link String#trim() trimmed}, {@code false} otherwise.
   * @return the string with all read characters not {@link CharFilter#accept(char) accepted} by the given
   *         {@link CharFilter} or until the given {@code stop} {@link String} was detected. If the end of data was
   *         reached without hitting {@code stop} the entire rest of the data is returned or {@code null} if
   *         {@code acceptEnd} is {@code false}. Thre result will be {@link String#trim() trimmed} if {@code trim} is
   *         {@code true}.
   */
  String readUntil(CharFilter filter, boolean acceptEnd, String stop, boolean ignoreCase, boolean trim);

  /**
   * This method reads all {@link #next() next characters} until the given (un-escaped) {@code stop} character or the
   * end is reached. <br>
   * In advance to {@link #readUntil(char, boolean)}, this method allows that the {@code stop} character may be used in
   * the input-string by adding the given {@code escape} character. After the call of this method, the current index
   * will point to the next character after the (first) {@code stop} character or to the end if NO such character
   * exists. <br>
   * This method is especially useful when quoted strings should be parsed. E.g.:
   *
   * <pre>
   * {@link CharStreamScanner} scanner = getScanner();
   * doSomething();
   * char c = scanner.{@link #next()};
   * if ((c == '"') || (c == '\'')) {
   *   char escape = c; // may also be something like '\'
   *   String quote = scanner.{@link #readUntil(char, boolean, char) readUntil}(c, false, escape)
   * } else {
   *   doOtherThings();
   * }
   * </pre>
   *
   * @param stop is the character to read until.
   * @param acceptEnd if {@code true} the end of data will be treated as {@code stop}, too.
   * @param escape is the character used to escape the {@code stop} character. To add an occurrence of the
   *        {@code escape} character it has to be duplicated (occur twice). The {@code escape} character may also be
   *        equal to the {@code stop} character. If other regular characters are escaped the {@code escape} character is
   *        simply ignored.
   * @return the string with all read characters excluding the {@code stop} character or {@code null} if there was no
   *         {@code stop} character and {@code acceptEnd} is {@code false}.
   */
  String readUntil(char stop, boolean acceptEnd, char escape);

  /**
   * This method reads all {@link #next() next characters} until the given {@code stop} character or the end of the
   * string to parse is reached. In advance to {@link #readUntil(char, boolean)}, this method will scan the input using
   * the given {@code syntax} which e.g. allows to {@link CharScannerSyntax#getEscape() escape} the stop character. <br>
   * After the call of this method, the current index will point to the next character after the (first) {@code stop}
   * character or to the end of the string if NO such character exists.
   *
   * @param stop is the character to read until.
   * @param acceptEnd if {@code true} the end of data will be treated as {@code stop}, too.
   * @param syntax contains the characters specific for the syntax to read.
   * @return the string with all read characters excluding the {@code stop} character or {@code null} if there was no
   *         {@code stop} character.
   * @see #readUntil(CharFilter, boolean, CharScannerSyntax)
   */
  String readUntil(char stop, boolean acceptEnd, CharScannerSyntax syntax);

  /**
   * This method reads all {@link #next() next characters} until the given {@link CharFilter}
   * {@link CharFilter#accept(char) accepts} the current character as stop character or the end of data is reached. In
   * advance to {@link #readUntil(char, boolean)}, this method will scan the input using the given {@code syntax} which
   * e.g. allows to {@link CharScannerSyntax#getEscape() escape} the stop character. <br>
   * After the call of this method, the current index will point to the next character after the (first) {@code stop}
   * character or to the end of the string if NO such character exists.
   *
   * @param filter is used to {@link CharFilter#accept(char) decide} where to stop.
   * @param acceptEnd if {@code true} the end of data will be treated as {@code stop}, too.
   * @param syntax contains the characters specific for the syntax to read.
   * @return the string with all read characters excluding the {@code stop} character or {@code null} if there was no
   *         {@code stop} character.
   * @see #readUntil(char, boolean, CharScannerSyntax)
   */
  String readUntil(CharFilter filter, boolean acceptEnd, CharScannerSyntax syntax);

  /**
   * This method reads all {@link #next() next characters} that are {@link CharFilter#accept(char) accepted} by the
   * given {@code filter}. <br>
   * After the call of this method, the current index will point to the next character that was NOT
   * {@link CharFilter#accept(char) accepted} by the given {@code filter} or to the end if NO such character exists.
   *
   * @see #skipWhile(CharFilter)
   *
   * @param filter is used to {@link CharFilter#accept(char) decide} which characters should be accepted.
   * @return a string with all characters {@link CharFilter#accept(char) accepted} by the given {@code filter}. Will be
   *         the empty string if no character was accepted.
   */
  default String readWhile(CharFilter filter) {

    return readWhile(filter, Integer.MAX_VALUE);
  }

  /**
   * This method reads all {@link #next() next characters} that are {@link CharFilter#accept(char) accepted} by the
   * given {@code filter}. <br>
   * After the call of this method, the current index will point to the next character that was NOT
   * {@link CharFilter#accept(char) accepted} by the given {@code filter}. If the next {@code max} characters or the
   * characters left until the {@link #hasNext() end} of this scanner are {@link CharFilter#accept(char) accepted}, only
   * that amount of characters are skipped.
   *
   * @see #skipWhile(char)
   *
   * @param filter is used to {@link CharFilter#accept(char) decide} which characters should be accepted.
   * @param max is the maximum number of characters that should be read.
   * @return a string with all characters {@link CharFilter#accept(char) accepted} by the given {@code filter} limited
   *         to the length of {@code max} and the {@link #hasNext() end} of this scanner. Will be the empty string if no
   *         character was accepted.
   */
  String readWhile(CharFilter filter, int max);

  /**
   * @return a {@link String} with the data until the end of the current line or the end of the data. Will be
   *         {@code null} if the end has already been reached and {@link #hasNext()} returns {@code false}.
   */
  default String readLine() {

    return readLine(false);
  }

  /**
   * @param trim - {@code true} if the result should be {@link String#trim() trimmed}, {@code false} otherwise.
   * @return a {@link String} with the data until the end of the current line ({@link String#trim() trimmed} if
   *         {@code trim} is {@code true}) or the end of the data. Will be {@code null} if the end has already been
   *         reached and {@link #hasNext()} returns {@code false}.
   */
  String readLine(boolean trim);

  /**
   * Reads a {@link Boolean} value from this scanner if available.
   *
   * @return the consumed {@link Boolean} value or {@code null} if no such value was available and the
   *         {@link #getPosition() position} remains unchanged.
   */
  default Boolean readBoolean() {

    return readBoolean(false, false);
  }

  /**
   * Reads a {@link Boolean} value from this scanner if available.
   *
   * @param ignoreCase - if {@code true} the case of the characters is ignored when compared, {@code false} otherwise
   *        (only lower case is accepted).
   * @return the consumed {@link Boolean} value or {@code null} if no such value was available and the
   *         {@link #getPosition() position} remains unchanged.
   */
  default Boolean readBoolean(boolean ignoreCase) {

    return readBoolean(ignoreCase, false);
  }

  /**
   * Reads a {@link Boolean} value from this scanner if available.
   *
   * @param ignoreCase - if {@code true} the case of the characters is ignored when compared, {@code false} otherwise
   *        (only lower case is accepted).
   * @param acceptYesNo - if {@code true} also "yes" is accepted for {@code true} and "no" for {@code false},
   *        {@code false} otherwise.
   * @return the consumed {@link Boolean} value or {@code null} if no such value was available and the
   *         {@link #getPosition() position} remains unchanged.
   */
  default Boolean readBoolean(boolean ignoreCase, boolean acceptYesNo) {

    if (expect("true", ignoreCase)) {
      return Boolean.TRUE;
    } else if (expect("false", ignoreCase)) {
      return Boolean.FALSE;
    } else if (!acceptYesNo) {
      return null;
    } else if (expect("yes", ignoreCase)) {
      return Boolean.TRUE;
    } else if (expect("no", ignoreCase)) {
      return Boolean.FALSE;
    }
    return null;
  }

  /**
   * Generic way to read and parse any kind of {@link Number}.
   *
   * @param numberParser the {@link CharScannerNumberParser}. Can decide if sign, digits, radix, exponent, or even
   *        specials are
   */
  void readNumber(CharScannerNumberParser numberParser);

  /**
   * This method reads the double value (decimal number) starting at the current position by reading as many matching
   * characters as available and returns its {@link Double#parseDouble(String) parsed} value. <br>
   *
   * @return the parsed {@code double} number or {@code null} if the current current position does not point to a
   *         number.
   * @throws NumberFormatException if the number at the current position could not be parsed.
   */
  default Double readDouble() throws NumberFormatException {

    return readDouble(CharScannerRadixMode.ONLY_10);
  }

  /**
   * This method reads the double value (decimal number) starting at the current position by reading as many matching
   * characters as available and returns its {@link Double#parseDouble(String) parsed} value. <br>
   *
   * @param radixMode the {@link CharScannerRadixHandler} - e.g. {@link CharScannerRadixMode#ALL}.
   * @return the parsed {@code double} number or {@code null} if the current current position does not point to a
   *         number.
   * @throws NumberFormatException if the number at the current position could not be parsed.
   */
  Double readDouble(CharScannerRadixHandler radixMode) throws NumberFormatException;

  /**
   * This method reads a {@link Float} value from the current position {@link #next() consuming} as many matching
   * characters as available.
   *
   * @return the parsed {@link Float} value or {@code null} if the current current position does not point to a
   *         {@link Float} number.
   * @throws NumberFormatException if the number at the current position could not be parsed.
   */
  default Float readFloat() throws NumberFormatException {

    return readFloat(CharScannerRadixMode.ONLY_10);
  }

  /**
   * This method reads a {@link Float} value from the current position {@link #next() consuming} as many matching
   * characters as available.
   *
   * @param radixMode the {@link CharScannerRadixHandler} - e.g. {@link CharScannerRadixMode#ALL}.
   * @return the parsed {@link Float} value or {@code null} if the current current position does not point to a
   *         {@link Float} number.
   * @throws NumberFormatException if the number at the current position could not be parsed.
   */
  Float readFloat(CharScannerRadixHandler radixMode) throws NumberFormatException;

  /**
   * @return the consumed {@link Long} value or {@code null} if no number was present and the {@link #getPosition()
   *         position} remains unchanged.
   * @throws NumberFormatException if the current current position points to a number that is not a {@link Long} value.
   */
  default Long readLong() throws NumberFormatException {

    return readLong(CharScannerRadixMode.ONLY_10);
  }

  /**
   * @param radixMode the {@link CharScannerRadixHandler} - e.g. {@link CharScannerRadixMode#ALL}.
   * @return the consumed {@link Long} value or {@code null} if no such value was present and the {@link #getPosition()
   *         position} remains unchanged.
   * @throws NumberFormatException if the current current position points to a number that is not a {@link Long} value.
   */
  Long readLong(CharScannerRadixHandler radixMode);

  /**
   * @return the consumed {@link Integer} value or {@code null} if no such value was present and the
   *         {@link #getPosition() position} remains unchanged.
   * @throws NumberFormatException if the current current position does not point to a {@link Integer} value.
   */
  default Integer readInteger() throws NumberFormatException {

    return readInteger(CharScannerRadixMode.ONLY_10);
  }

  /**
   * @param radixMode the {@link CharScannerRadixHandler} - e.g. {@link CharScannerRadixMode#ALL}.
   * @param noSign - {@code true} if no sign ('+' or '-')is accepted, {@code false} otherwise (read sign if present).
   * @return the consumed {@link Integer} value or {@code null} if no such value was present and the
   *         {@link #getPosition() position} remains unchanged.
   * @throws NumberFormatException if the current current position does not point to a {@link Long} value.
   */
  Integer readInteger(CharScannerRadixHandler radixMode) throws NumberFormatException;

  /**
   * Reads a Java {@link Number} literal (e.g. "1L" or "1.3F").
   *
   * @return the consumed {@link Number} or {@code null} if no number literal was found and the {@link #getPosition()
   *         position} remains unchainged.
   * @throws NumberFormatException if a number literal was found that has an illegal format.
   */
  Number readJavaNumberLiteral();

  /**
   * This method reads the {@link #next() next character} if it is a digit. Else the state remains unchanged.
   *
   * @return the numeric value of the next Latin digit (e.g. {@code 0} if {@code '0'}) or {@code -1} if the
   *         {@link #next() next character} is no Latin digit.
   */
  default int readDigit() {

    return readDigit(10);
  }

  /**
   * This method reads the {@link #next() next character} if it is a digit within the given {@code radix}. Else the
   * state remains unchanged.
   *
   * @param radix the radix that defines the range of the digits. See {@link Integer#parseInt(String, int)}. E.g.
   *        {@code 10} to read any Latin digit (see {@link #readDigit()}), {@code 8} to read octal digit, {@code 16} to
   *        read hex decimal digits.
   * @return the numeric value of the next digit within the given {@code radix} or {@code -1} if the {@link #next() next
   *         character} is no such digit.
   */
  int readDigit(int radix);

  /**
   * This method reads the long starting at the current position by reading as many Latin digits as available but at
   * maximum the given {@code maxDigits} and returns its {@link Long#parseLong(String) parsed} value. <br>
   * <b>ATTENTION:</b><br>
   * This method does NOT treat signs ({@code +} or {@code -}) to do so, scan them yourself before and negate the result
   * as needed.
   *
   * @param maxDigits is the maximum number of digits that will be read. The value has to be positive (greater than
   *        zero). Should not be greater than {@code 19} as this will exceed the range of {@code long}.
   * @return the parsed number.
   * @throws NumberFormatException if the number at the current position could not be parsed.
   */
  long readUnsignedLong(int maxDigits) throws NumberFormatException;

  /**
   * Reads and parses a Java {@link String} literal value according to JLS 3.10.6. <br>
   * As a complex example for the input "Hi \"\176\477\579\u2022\uuuuu2211\"\n" this scanner would return the
   * {@link String} output {@code Hi "~'7/9•∑"} followed by a newline character.
   *
   * @return the parsed Java {@link String} literal value or {@code null} if not pointing to a {@link String} literal.
   */
  default String readJavaStringLiteral() {

    return readJavaStringLiteral(TextFormatMessageType.ERROR);
  }

  /**
   * Reads and parses a Java {@link String} literal value according to JLS 3.10.6. <br>
   * As a complex example for the input "Hi \"\176\477\579\u2022\uuuuu2211\"\n" this scanner would return the
   * {@link String} output {@code Hi "~'7/9•∑"} followed by a newline character.
   *
   * @param severity the {@link TextFormatMessageType} to use to report invalid escape sequences or missing terminating
   *        quotation.
   * @return the parsed Java {@link String} literal value or {@code null} if not pointing to a {@link String} literal.
   */
  String readJavaStringLiteral(TextFormatMessageType severity);

  /**
   * Reads and parses a Java {@link Character} literal value according to JLS 3.10.6. <br>
   * Examples are given in the following table:
   * <table border="1">
   * <tr>
   * <th>literal</th>
   * <th>result</th>
   * <th>comment</th>
   * </tr>
   * <tr>
   * <td>{@code 'a'}</td>
   * <td>a</td>
   * <td>regular char</td>
   * </tr>
   * <tr>
   * <td>{@code '\''}</td>
   * <td>'</td>
   * <td>escaped char</td>
   * </tr>
   * <tr>
   * <td>{@code '\176'}</td>
   * <td>~</td>
   * <td>escaped octal representation</td>
   * </tr>
   * <tr>
   * <td>{@code '\u2022'}</td>
   * <td>•</td>
   * <td>escaped unicode representation</td>
   * </tr>
   * </table>
   *
   * @return the parsed Java {@link String} literal value or {@code null} if not pointing to a {@link String} literal.
   */
  default Character readJavaCharLiteral() {

    return readJavaCharLiteral(TextFormatMessageType.ERROR);
  }

  /**
   * Reads and parses a Java {@link Character} literal value according to JLS 3.10.6. <br>
   * Examples are given in the following table:
   * <table border="1">
   * <tr>
   * <th>literal</th>
   * <th>result</th>
   * <th>comment</th>
   * </tr>
   * <tr>
   * <td>{@code 'a'}</td>
   * <td>a</td>
   * <td>regular char</td>
   * </tr>
   * <tr>
   * <td>{@code '\''}</td>
   * <td>'</td>
   * <td>escaped char</td>
   * </tr>
   * <tr>
   * <td>{@code '\176'}</td>
   * <td>~</td>
   * <td>escaped octal representation</td>
   * </tr>
   * <tr>
   * <td>{@code '\u2022'}</td>
   * <td>•</td>
   * <td>escaped unicode representation</td>
   * </tr>
   * </table>
   *
   * @param severity the {@link TextFormatMessageType} to use to report invalid escape sequences or missing terminating
   *        quotation.
   * @return the parsed Java {@link Character} literal value or {@code null} if not pointing to a {@link Character}
   *         literal.
   */
  Character readJavaCharLiteral(TextFormatMessageType severity);

  /**
   * This method determines if the given {@code expected} {@link String} is completely present at the current position.
   * It will only {@link #next() consume} characters and change the state if the {@code expected} {@link String} was
   * found (entirely).<br>
   * <b>Attention:</b><br>
   * This method requires lookahead. For implementations that are backed by an underlying stream (or reader) the
   * {@link String#length() length} of the expected {@link String} shall not exceed the available lookahead size (buffer
   * capacity given at construction time). Otherwise the method may fail.
   *
   * @param expected is the expected string.
   * @return {@code true} if the {@code expected} string was successfully consumed from this scanner, {@code false}
   *         otherwise.
   * @see #expectUnsafe(String)
   */
  default boolean expect(String expected) {

    return expect(expected, false, false, 0);
  }

  /**
   * This method determines if the given {@code expected} {@link String} is completely present at the current position.
   * It will only {@link #next() consume} characters and change the state if the {@code expected} {@link String} was
   * found (entirely).<br>
   * <b>Attention:</b><br>
   * This method requires lookahead. For implementations that are backed by an underlying stream (or reader) the
   * {@link String#length() length} of the expected {@link String} shall not exceed the available lookahead size (buffer
   * capacity given at construction time). Otherwise the method may fail.
   *
   * @param expected the expected {@link String} to search for.
   * @param ignoreCase - if {@code true} the case of the characters is ignored when compared, {@code false} otherwise.
   * @return {@code true} if the {@code expected} string was successfully found and {@link #next() consume} from this
   *         scanner, {@code false} otherwise.
   * @see #expectUnsafe(String, boolean)
   */
  default boolean expect(String expected, boolean ignoreCase) {

    return expect(expected, ignoreCase, false, 0);
  }

  /**
   * This method determines if the given {@code expected} {@link String} is completely present at the current position.
   * It will only {@link #next() consume} characters and change the state if {@code lookahead} is {@code false} and the
   * {@code expected} {@link String} was found (entirely).<br>
   * <b>Attention:</b><br>
   * This method requires lookahead. For implementations that are backed by an underlying stream (or reader) the
   * {@link String#length() length} of the expected {@link String} shall not exceed the available lookahead size (buffer
   * capacity given at construction time). Otherwise the method may fail.
   *
   * @param expected the expected {@link String} to search for.
   * @param ignoreCase - if {@code true} the case of the characters is ignored when compared, {@code false} otherwise.
   * @param lookahead - if {@code true} the state of the scanner remains unchanged even if the expected {@link String}
   *        has been found, {@code false} otherwise (expected {@link String} is consumed on match).
   * @return {@code true} if the {@code expected} string was successfully found, {@code false} otherwise.
   */
  default boolean expect(String expected, boolean ignoreCase, boolean lookahead) {

    return expect(expected, ignoreCase, lookahead, 0);
  }

  /**
   * This method determines if the given {@code expected} {@link String} is completely present at the current position.
   * It will only {@link #next() consume} characters and change the state if {@code lookahead} is {@code false} and the
   * {@code expected} {@link String} was found (entirely).<br>
   * <b>Attention:</b><br>
   * This method requires lookahead. For implementations that are backed by an underlying stream (or reader) the
   * {@link String#length() length} of the expected {@link String} shall not exceed the available lookahead size (buffer
   * capacity given at construction time). Otherwise the method may fail.
   *
   * @param expected the expected {@link String} to search for.
   * @param ignoreCase - if {@code true} the case of the characters is ignored when compared, {@code false} otherwise.
   * @param lookahead - if {@code true} the state of the scanner remains unchanged even if the expected {@link String}
   *        has been found, {@code false} otherwise (expected {@link String} is consumed on match).
   * @param offset the number of characters that have already been {@link #peek(int) peeked} and after which the given
   *        {@link String} is expected. Will typically be {@code 0}. If {@code lookahead} is {@code false} and the
   *        expected {@link String} was found these characters will be {@link #skip(int) skipped} together with the
   *        expected {@link String}.
   * @return {@code true} if the {@code expected} string was successfully found, {@code false} otherwise.
   */
  boolean expect(String expected, boolean ignoreCase, boolean lookahead, int offset);

  /**
   * This method determines if the given {@code expected} {@link String} is completely present at the current position.
   * It will only {@link #next() consume} characters and change the state if {@code lookahead} is {@code false} and the
   * {@code expected} {@link String} was found (entirely).<br>
   * <b>Attention:</b><br>
   * This method requires lookahead. For implementations that are backed by an underlying stream (or reader) the
   * {@link String#length() length} of the expected {@link String} shall not exceed the available lookahead size (buffer
   * capacity given at construction time). Otherwise the method may fail.
   *
   * @param expected the expected {@link String} to search for.
   * @param ignoreCase - if {@code true} the case of the characters is ignored when compared, {@code false} otherwise.
   * @param lookahead - if {@code true} the state of the scanner remains unchanged even if the expected {@link String}
   *        has been found, {@code false} otherwise (expected {@link String} is consumed on match).
   * @param offset the number of characters that have already been {@link #peek(int) peeked} and after which the given
   *        {@link String} is expected. Will typically be {@code 0}. If {@code lookahead} is {@code false} and the
   *        expected {@link String} was found these characters will be {@link #skip(int) skipped} together with the
   *        expected {@link String}.
   * @param warning {@code true} to {@link #addWarning(String) add a warning} in case the expected {@link String} was
   *        not found, {@code false} otherwise.
   * @return {@code true} if the {@code expected} string was successfully found, {@code false} otherwise.
   */
  default boolean expect(String expected, boolean ignoreCase, boolean lookahead, int offset, boolean warning) {

    boolean found = expect(expected, ignoreCase, lookahead, offset);
    if (!found && warning) {
      addWarning("Expected '" + expected + "'");
    }
    return found;
  }

  /**
   * This method checks if the {@link #next() next character} is equal to the given {@code expected} character. <br>
   * If the character matched with the {@code expected} character, the parser points to the next character. Otherwise
   * its position will remain unchanged.
   *
   * @param expected is the expected character.
   * @return {@code true} if the current character is the same as {@code expected}, {@code false} otherwise.
   */
  default boolean expectOne(char expected) {

    return expectOne(expected, false);
  }

  /**
   * This method checks if the {@link #next() next character} is equal to the given {@code expected} character. <br>
   * If the character matched with the {@code expected} character, the parser points to the next character. Otherwise
   * its position will remain unchanged.
   *
   * @param expected the character to expect as {@link #next() next} in this stream.
   * @param warning {@code true} to {@link #addWarning(String) add a warning} in case the expected character was not
   *        present, {@code false} otherwise.
   * @return {@code true} if the expected character was found and consumer, {@code false} otherwise (and this stream
   *         remains unchanged).
   */
  boolean expectOne(char expected, boolean warning);

  /**
   * This method checks that the {@link #next() next character} is {@link CharFilter#accept(char) accepted} by the given
   * {@link CharFilter}. <br>
   * If the current character was as expected, the parser points to the next character. Otherwise its position will
   * remain unchanged.
   *
   * @param expected is the {@link CharFilter} {@link CharFilter#accept(char) accepting} the expected chars.
   * @return {@code true} if the current character is {@link CharFilter#accept(char) accepted}, {@code false} otherwise.
   */
  default boolean expectOne(CharFilter expected) {

    if (!hasNext()) {
      return false;
    }
    if (expected.accept(peek())) {
      next();
      return true;
    }
    return false;
  }

  /**
   * This method skips all {@link #next() next characters} as long as they equal to the according character of the
   * {@code expected} {@link String}. <br>
   * If a character differs this method stops and the parser points to the first character that differs from
   * {@code expected}. Except for the latter circumstance, this method behaves similar to the following code:
   *
   * <pre>
   * {@link #read(int) read}(expected.length).equals(expected)
   * </pre>
   *
   * <b>ATTENTION:</b><br>
   * In most cases you want to prefer {@link #expect(String)} instead of using this method. Only in specific cases and
   * for highly optimized performance it may make sense to use it. In such case be careful and consider to combine with
   * {@link #getPosition()} to be able to determine whether characters have been consumed if {@code false} was returned
   * (e.g. otherwise when doing {@link #expectUnsafe(String) expectUnsafe}("false") and else doing
   * {@link #expectUnsafe(String) expectUnsafe}("true") to parse a {@code boolean} literal your code could accept
   * "falstrue" as "true").
   *
   * @param expected is the expected string.
   * @return {@code true} if the {@code expected} string was successfully consumed from this scanner, {@code false}
   *         otherwise.
   * @see #expect(String)
   */
  default boolean expectUnsafe(String expected) {

    return expectUnsafe(expected, false);
  }

  /**
   * This method skips all {@link #next() next characters} as long as they equal to the according character of the
   * {@code expected} string. <br>
   * If a character differs this method stops and the parser points to the first character that differs from
   * {@code expected}. Except for the latter circumstance, this method behaves similar to the following code:
   *
   * <pre>
   * {@link #read(int) read}(expected.length).equals[IgnoreCase](expected)
   * </pre>
   *
   * <b>ATTENTION:</b><br>
   * In most cases you want to prefer {@link #expect(String, boolean)} instead of using this method. See
   * {@link #expectUnsafe(String)} for details.
   *
   * @param expected is the expected string.
   * @param ignoreCase - if {@code true} the case of the characters is ignored when compared.
   * @return {@code true} if the {@code expected} string was successfully consumed from this scanner, {@code false}
   *         otherwise.
   * @see #expect(String, boolean)
   */
  boolean expectUnsafe(String expected, boolean ignoreCase);

  /**
   * This method verifies that the {@link #next() next character} is equal to the given {@code expected} character. <br>
   * If the current character was as expected, the parser points to the next character. Otherwise an exception is thrown
   * indicating the problem.
   *
   * @param expected is the expected character.
   * @throws IllegalStateException if the required character was not found.
   */
  default void requireOne(char expected) throws IllegalStateException {

    if (!hasNext()) {
      throw new IllegalStateException("Expecting '" + expected + "' but found end-of-stream.");
    }
    char next = peek();
    if (next != expected) {
      throw new IllegalStateException("Expecting '" + expected + "' but found: " + next);
    }
    next();
  }

  /**
   * This method verifies that the {@code expected} string gets consumed from this scanner with respect to
   * {@code ignoreCase}. Otherwise an exception is thrown indicating the problem. <br>
   * This method behaves functionally equivalent to the following code:
   *
   * <pre>
   * if (!scanner.{@link #expectUnsafe(String, boolean) expectUnsafe}(expected, ignoreCase)) {
   *   throw new {@link IllegalStateException}(...);
   * }
   * </pre>
   *
   * @param expected is the expected string.
   * @param ignoreCase - if {@code true} the case of the characters is ignored during comparison.
   */
  void require(String expected, boolean ignoreCase);

  /**
   * @param filter the {@link CharFilter} {@link CharFilter#accept(char) accepting} the expected characters to
   *        {@link #skipWhile(CharFilter, int) skip}.
   * @return the actual number of characters that have been skipped.
   * @throws IllegalStateException if less than {@code 1} or more than {@code 1000} {@link CharFilter#accept(char)
   *         accepted} characters have been consumed.
   */
  default int requireOne(CharFilter filter) {

    return require(filter, 1, -1);
  }

  /**
   * @param filter the {@link CharFilter} {@link CharFilter#accept(char) accepting} the expected characters to
   *        {@link #skipWhile(CharFilter, int) skip}.
   * @return the actual number of characters that have been skipped.
   * @throws IllegalStateException if less than {@code 1} or more than {@code 1000} {@link CharFilter#accept(char)
   *         accepted} characters have been consumed.
   */
  default int requireOneOrMore(CharFilter filter) {

    return require(filter, 1);
  }

  /**
   * @param filter the {@link CharFilter} {@link CharFilter#accept(char) accepting} the expected characters to
   *        {@link #skipWhile(CharFilter, int) skip}.
   * @param min the minimum required number of skipped characters.
   * @return the actual number of characters that have been skipped.
   * @throws IllegalStateException if less than {@code min} or more than {@code 1000} {@link CharFilter#accept(char)
   *         accepted} characters have been consumed.
   */
  default int require(CharFilter filter, int min) {

    return require(filter, min, 1000);
  }

  /**
   * @param filter the {@link CharFilter} {@link CharFilter#accept(char) accepting} the expected characters to
   *        {@link #skipWhile(CharFilter, int) skip}.
   * @param min the minimum required number of skipped characters.
   * @param max the maximum number of skipped characters.
   * @return the actual number of characters that have been skipped.
   * @throws IllegalStateException if less than {@code min} or more than {@code max} {@link CharFilter#accept(char)
   *         accepted} characters have been consumed.
   */
  default int require(CharFilter filter, int min, int max) {

    if ((min < 0) || ((min > max) && (max != -1))) {
      throw new IllegalArgumentException("Invalid range: " + min + "-" + max);
    }
    int num = max;
    if (max == -1) {
      num = min;
    }
    int count = skipWhile(filter, num);
    if (count < min) {
      invalidCharCount("at least " + min, count, filter);
    }
    if (count == max) {
      char c = peek();
      if (!filter.accept(c)) {
        invalidCharCount("up to " + max, count, filter);
      }
    }
    return count;
  }

  private IllegalStateException invalidCharCount(String bound, int count, CharFilter filter) {

    String description = filter.getDescription();
    String chars = " character(s)";
    if (!CharFilter.NO_DESCRIPTION.equals(description)) {
      chars = " character(s) matching " + description;
    }
    throw new IllegalStateException("Require " + bound + chars + " but found only " + count);
  }

  /**
   * This method skips all {@link #next() next characters} until the given {@code stop} character or the end is reached.
   * If the {@code stop} character was reached, this scanner will point to the next character after {@code stop} when
   * this method returns.
   *
   * @param stop is the character to read until.
   * @return {@code true} if the first occurrence of the given {@code stop} character has been passed, {@code false} if
   *         there is no such character.
   */
  boolean skipUntil(char stop);

  /**
   * This method reads all {@link #next() next characters} until the given {@code stop} character or the end of the
   * string to parse is reached. In advance to {@link #skipUntil(char)}, this method will read over the {@code stop}
   * character if it is escaped with the given {@code escape} character.
   *
   * @param stop is the character to read until.
   * @param escape is the character used to escape the stop character (e.g. '\').
   * @return {@code true} if the first occurrence of the given {@code stop} character has been passed, {@code false} if
   *         there is no such character.
   */
  boolean skipUntil(char stop, char escape);

  /**
   * This method skips the number of {@link #next() next characters} given by {@code count}.
   *
   * @param count is the number of characters to skip. You may use {@link Integer#MAX_VALUE} to read until the end of
   *        data if the data-size is suitable.
   * @return a to total number of characters that have been skipped. Typically equal to {@code count}. Will be less in
   *         case the end of data was reached.
   */
  int skip(int count);

  /**
   * @return {@code 0} if the {@link #next() next characeter} is not a newline and the stream remains unchanged,
   *         {@code 1} if the {@link #next() next characeter} was '\n' and has been {@link #skip(int) skipped}, or
   *         {@code 2} if the{@link #next() next characeters} have been '\r' and '\n' and have been {@link #skip(int)
   *         skipped}.
   */
  int skipNewLine();

  /**
   * This method reads all {@link #next() next characters} until the given {@code substring} has been detected. <br>
   * After the call of this method, the current index will point to the next character after the first occurrence of
   * {@code substring} or to the end of data if the given {@code substring} was NOT found. <br>
   *
   * @param substring is the substring to search and skip over starting at the current index.
   * @return {@code true} if the given {@code substring} occurred and has been passed and {@code false} if the end of
   *         the string has been reached without any occurrence of the given {@code substring}.
   */
  default boolean skipOver(String substring) {

    return skipOver(substring, false);
  }

  /**
   * This method reads all {@link #next() next characters} until the given {@code substring} has been detected. <br>
   * After the call of this method, the current index will point to the next character after the first occurrence of
   * {@code substring} or to the end of data if the given {@code substring} was NOT found. <br>
   *
   * @param substring is the substring to search and skip over starting at the current index.
   * @param ignoreCase - if {@code true} the case of the characters is ignored when compared with characters from
   *        {@code substring}.
   * @return {@code true} if the given {@code substring} occurred and has been passed and {@code false} if the end of
   *         the string has been reached without any occurrence of the given {@code substring}.
   */
  default boolean skipOver(String substring, boolean ignoreCase) {

    return skipOver(substring, ignoreCase, null);
  }

  /**
   * This method consumes all {@link #next() next characters} until the given {@code substring} has been detected, a
   * character was {@link CharFilter#accept(char) accepted} by the given {@link CharFilter} or the end of data was
   * reached.<br>
   * After the call of this method this scanner will point to the next character after the first occurrence of
   * {@code substring}, to the stop character or to end of data. <br>
   *
   * @param substring is the substring to search and skip over starting at the current index.
   * @param ignoreCase - if {@code true} the case of the characters is ignored when compared with characters from
   *        {@code substring}.
   * @param stopFilter is the filter used to {@link CharFilter#accept(char) detect} stop characters. If such character
   *        was detected, the skip is stopped and the parser points to the character after the stop character. The
   *        {@code substring} should NOT contain a {@link CharFilter#accept(char) stop character}.
   * @return {@code true} if the given {@code substring} occurred and has been passed and {@code false} if a stop
   *         character has been detected or the end of the string has been reached without any occurrence of the given
   *         {@code substring} or stop character.
   */
  boolean skipOver(String substring, boolean ignoreCase, CharFilter stopFilter);

  /**
   * This method reads all {@link #next() next characters} that are identical to the character given by {@code c}. <br>
   * E.g. use {@link #skipWhile(char) readWhile(' ')} to skip all blanks from the current index. After the call of this
   * method, the current index will point to the next character that is different to the given character {@code c} or to
   * the end if NO such character exists.
   *
   * @param c is the character to read over.
   * @return the number of characters that have been skipped.
   */
  int skipWhile(char c);

  /**
   * This method reads all {@link #next() next characters} that are {@link CharFilter#accept(char) accepted} by the
   * given {@code filter}. <br>
   * After the call of this method, the current index will point to the next character that was NOT
   * {@link CharFilter#accept(char) accepted} by the given {@code filter} or to the end if NO such character exists.
   *
   * @see #skipWhile(char)
   *
   * @param filter is used to {@link CharFilter#accept(char) decide} which characters should be accepted.
   * @return the number of characters {@link CharFilter#accept(char) accepted} by the given {@code filter} that have
   *         been skipped.
   */
  default int skipWhile(CharFilter filter) {

    return skipWhile(filter, Integer.MAX_VALUE);
  }

  /**
   * This method reads all {@link #next() next characters} that are {@link CharFilter#accept(char) accepted} by the
   * given {@code filter}. <br>
   * After the call of this method, the current index will point to the next character that was NOT
   * {@link CharFilter#accept(char) accepted} by the given {@code filter}. If the next {@code max} characters or the
   * characters left until the {@link #hasNext() end} of this scanner are {@link CharFilter#accept(char) accepted}, only
   * that amount of characters are skipped.
   *
   * @see #skipWhile(char)
   *
   * @param filter is used to {@link CharFilter#accept(char) decide} which characters should be accepted.
   * @param max is the maximum number of characters that may be skipped.
   * @return the number of skipped characters.
   */
  int skipWhile(CharFilter filter, int max);

  /**
   * Behaves like the following code:
   *
   * <pre>
   * {@link #skipWhile(CharFilter) skipWhile}(filter);
   * return {@link #peek()};
   * </pre>
   *
   * @param filter is used to {@link CharFilter#accept(char) decide} which characters should be accepted.
   * @return the first character that was not {@link CharFilter#accept(char) accepted} by the given {@link CharFilter}.
   *         Only the {@link CharFilter#accept(char) accepted} characters have been consumed, this scanner still points
   *         to the returned character.
   */
  default char skipWhileAndPeek(CharFilter filter) {

    return skipWhileAndPeek(filter, Integer.MAX_VALUE);
  }

  /**
   * Behaves like the following code:
   *
   * <pre>
   * {@link #skipWhile(CharFilter, int) skipWhile}(filter, max);
   * return {@link #peek()};
   * </pre>
   *
   * @param filter is used to {@link CharFilter#accept(char) decide} which characters should be accepted.
   * @param max is the maximum number of characters that may be skipped.
   * @return the first character that was not {@link CharFilter#accept(char) accepted} by the given {@link CharFilter}.
   *         Only the {@link CharFilter#accept(char) accepted} characters have been consumed, this scanner still points
   *         to the returned character.
   */
  default char skipWhileAndPeek(CharFilter filter, int max) {

    skipWhile(filter, max);
    return peek();
  }

  /**
   * @return the {@link String} with the characters that have already been parsed but are still available in the
   *         underlying buffer. May be used for debugging or error messages.
   */
  String getBufferParsed();

  /**
   * @return the {@link String} with the characters that have not yet been parsed but are available in the underlying
   *         buffer. May be used for debugging or error messages.
   */
  String getBufferToParse();

}
