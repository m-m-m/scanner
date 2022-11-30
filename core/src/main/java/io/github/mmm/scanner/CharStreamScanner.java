/* Copyright (c) The m-m-m Team, Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0 */
package io.github.mmm.scanner;

import io.github.mmm.base.filter.CharFilter;

/**
 * This is the interface for a scanner that can be used to parse a stream or sequence of characters.
 */
public interface CharStreamScanner {

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
   * This method reads the current character without incrementing the index.
   *
   * @return the current character or {@link #EOS} if none is {@link #hasNext() available}.
   */
  char peek();

  /**
   * @return the position in the sequence to scan or in other words the number of bytes that have been read. Will
   *         initially be {@code 0}. Please note that this API is designed for scanning textual content (for parsers).
   *         Therefore we consider 2.1 terabyte as a suitable {@link Integer#MAX_VALUE limit}.
   */
  int getPosition();

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
   *        zero). Use {@code 19} or higher to be able to read any long number.
   * @return the parsed number.
   * @throws NumberFormatException if the current current position does NOT point to a number.
   */
  long readLong(int maxDigits) throws NumberFormatException;

  /**
   * This method reads the double value (decimal number) starting at the current position by reading as many matching
   * characters as available and returns its {@link Double#parseDouble(String) parsed} value. <br>
   *
   * @return the parsed number.
   * @throws NumberFormatException if the current current position does NOT point to a number.
   */
  default double readDouble() throws NumberFormatException {

    String number = consumeDecimal();
    return Double.parseDouble(number);
  }

  /**
   * This method reads the float value (decimal number) starting at the current position by reading as many matching
   * characters as available and returns its {@link Float#parseFloat(String) parsed} value. <br>
   *
   * @return the parsed number.
   * @throws NumberFormatException if the current current position does NOT point to a number.
   */
  default float readFloat() throws NumberFormatException {

    String number = consumeDecimal();
    return Float.parseFloat(number);
  }

  /**
   * Consumes the characters of a decimal number (double or float).
   *
   * @return the decimal number as {@link String}.
   */
  String consumeDecimal();

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
   * This method skips all {@link #next() next characters} as long as they equal to the according character of the
   * {@code expected} string. <br>
   * If a character differs this method stops and the parser points to the first character that differs from
   * {@code expected}. Except for the latter circumstance, this method behaves like the following code:
   *
   * <pre>
   * {@link #read(int) read}(expected.length).equals(expected)
   * </pre>
   *
   * <b>ATTENTION:</b><br>
   * Be aware that if already the first character differs, this method will NOT change the state of the scanner. So take
   * care NOT to produce infinity loops.
   *
   * @param expected is the expected string.
   * @return {@code true} if the {@code expected} string was successfully consumed from this scanner, {@code false}
   *         otherwise.
   */
  default boolean expectUnsafe(String expected) {

    return expectUnsafe(expected, false);
  }

  /**
   * This method skips all {@link #next() next characters} as long as they equal to the according character of the
   * {@code expected} string. <br>
   * If a character differs this method stops and the parser points to the first character that differs from
   * {@code expected}. Except for the latter circumstance, this method behaves like the following code:
   *
   * <pre>
   * {@link #read(int) read}(expected.length).equals[IgnoreCase](expected)
   * </pre>
   *
   * <b>ATTENTION:</b><br>
   * Be aware that if already the first character differs, this method will NOT change the state of the scanner. So take
   * care NOT to produce infinity loops.
   *
   * @param expected is the expected string.
   * @param ignoreCase - if {@code true} the case of the characters is ignored when compared.
   * @return {@code true} if the {@code expected} string was successfully consumed from this scanner, {@code false}
   *         otherwise.
   */
  boolean expectUnsafe(String expected, boolean ignoreCase);

  /**
   * This method acts as {@link #expectUnsafe(String, boolean)} but if the expected String is NOT completely present, no
   * character is {@link #next() consumed} and the state of the scanner remains unchanged.<br>
   * <b>Attention:</b><br>
   * This method requires lookahead. For implementations that are backed by an underlying stream (or reader) the
   * {@link String#length() length} of the expected {@link String} shall not exceed the available lookahead size (buffer
   * capacity given at construction time). Otherwise the method may fail.
   *
   * @param expected is the expected string.
   * @return {@code true} if the {@code expected} string was successfully consumed from this scanner, {@code false}
   *         otherwise.
   */
  default boolean expectStrict(String expected) {

    return expectStrict(expected, false);
  }

  /**
   * This method acts as {@link #expectUnsafe(String, boolean)} but if the expected String is NOT completely present, no
   * character is {@link #next() consumed} and the state of the scanner remains unchanged.<br>
   * <b>Attention:</b><br>
   * This method requires lookahead. For implementations that are backed by an underlying stream (or reader) the
   * {@link String#length() length} of the expected {@link String} shall not exceed the available lookahead size (buffer
   * capacity given at construction time). Otherwise the method may fail.
   *
   * @param expected is the expected string.
   * @param ignoreCase - if {@code true} the case of the characters is ignored when compared.
   * @return {@code true} if the {@code expected} string was successfully consumed from this scanner, {@code false}
   *         otherwise.
   */
  default boolean expectStrict(String expected, boolean ignoreCase) {

    return expectStrict(expected, ignoreCase, false);
  }

  /**
   * This method acts as {@link #expectUnsafe(String, boolean)} but if the expected String is NOT completely present, no
   * character is {@link #next() consumed} and the state of the scanner remains unchanged.<br>
   * <b>Attention:</b><br>
   * This method requires lookahead. For implementations that are backed by an underlying stream (or reader) the
   * {@link String#length() length} of the expected {@link String} shall not exceed the available lookahead size (buffer
   * capacity given at construction time). Otherwise the method may fail.
   *
   * @param expected is the expected string.
   * @param ignoreCase - if {@code true} the case of the characters is ignored when compared.
   * @param lookahead - if {@code true} the state of the scanner remains unchanged even if the expected {@link String}
   *        has been found, {@code false} otherwise.
   * @return {@code true} if the {@code expected} string was successfully consumed from this scanner, {@code false}
   *         otherwise.
   */
  boolean expectStrict(String expected, boolean ignoreCase, boolean lookahead);

  /**
   * This method checks that the {@link #next() next character} is equal to the given {@code expected} character. <br>
   * If the current character was as expected, the parser points to the next character. Otherwise its position will
   * remain unchanged.
   *
   * @param expected is the expected character.
   * @return {@code true} if the current character is the same as {@code expected}, {@code false} otherwise.
   */
  boolean expectOne(char expected);

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
   * This method verifies that the {@link #next() next character} is equal to the given {@code expected} character. <br>
   * If the current character was as expected, the parser points to the next character. Otherwise an exception is thrown
   * indicating the problem.
   *
   * @param expected is the expected character.
   */
  default void requireOne(char expected) {

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
   * This method skips the number of {@link #next() next characters} given by {@code count}.
   *
   * @param count is the number of characters to skip. You may use {@link Integer#MAX_VALUE} to read until the end of
   *        data if the data-size is suitable.
   * @return a to total number of characters that have been skipped. Typically equal to {@code count}. Will be less in
   *         case the end of data was reached.
   */
  int skip(int count);

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
   * Reads and parses a Java {@link String} literal value according to JLS 3.10.6. <br>
   * As a complex example for the input "Hi \"\176\477\579\u2022\uuuuu2211\"\n" this scanner would return the
   * {@link String} output {@code Hi "~'7/9•∑"} followed by a newline character.
   *
   * @return the parsed Java {@link String} literal value or {@code null} if not pointing to a {@link String} literal.
   */
  default String readJavaStringLiteral() {

    return readJavaStringLiteral(false);
  }

  /**
   * Reads and parses a Java {@link String} literal value according to JLS 3.10.6. <br>
   * As a complex example for the input "Hi \"\176\477\579\u2022\uuuuu2211\"\n" this scanner would return the
   * {@link String} output {@code Hi "~'7/9•∑"} followed by a newline character.
   *
   * @return the parsed Java {@link String} literal value or {@code null} if not pointing to a {@link String} literal.
   * @param tolerant - {@code true} if invalid escape sequences should be tolerated (as '?'), {@code false} to throw an
   *        exception in such case.
   */
  String readJavaStringLiteral(boolean tolerant);

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

    return readJavaCharLiteral(false);
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
   * @return the parsed Java {@link String} literal value or {@code null} if not pointing to a {@link String} literal.
   * @param tolerant - {@code true} if an invalid char literal should be tolerated (as '?'), {@code false} to throw an
   *        exception in such case.
   */
  Character readJavaCharLiteral(boolean tolerant);

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
