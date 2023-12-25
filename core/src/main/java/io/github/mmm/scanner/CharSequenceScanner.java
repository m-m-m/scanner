/* Copyright (c) The m-m-m Team, Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0 */
package io.github.mmm.scanner;

import io.github.mmm.base.filter.CharFilter;
import io.github.mmm.base.text.TextFormatMessageHandler;

/**
 * This class represents a {@link String} or better a sequence of characters ({@code char[]}) together with a
 * {@link #getCurrentIndex() position} in that sequence. <br>
 * It has various useful methods for scanning the sequence. This scanner is designed to be fast on long sequences and
 * therefore internally {@link String#toCharArray() converts} {@link String}s to a char array instead of frequently
 * calling {@link String#charAt(int)}. <br>
 * <b>ATTENTION:</b><br>
 * This implementation is NOT thread-safe (intended by design).
 *
 * @since 1.0.0
 */
public class CharSequenceScanner extends AbstractCharStreamScanner {

  private String string;

  /** The initial {@link #offset} in the {@link #buffer}. */
  private final int initialOffset;

  /**
   * The constructor.
   *
   * @param charSequence is the {@link #getOriginalString() string} to scan.
   */
  public CharSequenceScanner(CharSequence charSequence) {

    this(charSequence, null);
  }

  /**
   * The constructor.
   *
   * @param charSequence is the {@link #getOriginalString() string} to scan.
   * @param messageHandler the {@link TextFormatMessageHandler}.
   */
  public CharSequenceScanner(CharSequence charSequence, TextFormatMessageHandler messageHandler) {

    this(charSequence.toString(), messageHandler);
  }

  /**
   * The constructor.
   *
   * @param string is the {@link #getOriginalString() string} to parse.
   */
  public CharSequenceScanner(String string) {

    this(string, null);
  }

  /**
   * The constructor.
   *
   * @param string is the {@link #getOriginalString() string} to parse.
   * @param messageHandler the {@link TextFormatMessageHandler}.
   */
  public CharSequenceScanner(String string, TextFormatMessageHandler messageHandler) {

    this(string, messageHandler, 1, 1);
  }

  /**
   * The constructor.
   *
   * @param string is the {@link #getOriginalString() string} to parse.
   * @param messageHandler the {@link TextFormatMessageHandler}.
   * @param line the initial {@link #getLine() line}.
   * @param column the initial {@link #getColumn() column}.
   */
  public CharSequenceScanner(String string, TextFormatMessageHandler messageHandler, int line, int column) {

    this(string.toCharArray(), messageHandler, line, column);
    this.string = string;
  }

  /**
   * The constructor.
   *
   * @param characters is an array containing the characters to scan.
   */
  public CharSequenceScanner(char[] characters) {

    this(characters, null);
  }

  /**
   * The constructor.
   *
   * @param characters is an array containing the characters to scan.
   * @param messageHandler the {@link TextFormatMessageHandler}.
   */
  public CharSequenceScanner(char[] characters, TextFormatMessageHandler messageHandler) {

    this(characters, 0, characters.length, messageHandler);
  }

  /**
   * The constructor.
   *
   * @param characters is an array containing the characters to scan.
   * @param messageHandler the {@link TextFormatMessageHandler}.
   * @param line the initial {@link #getLine() line}.
   * @param column the initial {@link #getColumn() column}.
   */
  public CharSequenceScanner(char[] characters, TextFormatMessageHandler messageHandler, int line, int column) {

    this(characters, 0, characters.length, messageHandler, line, column);
  }

  /**
   * The constructor.
   *
   * @param characters is an array containing the characters to scan.
   * @param offset is the index of the first char to scan in {@code characters} (typically {@code 0} to start at the
   *        beginning of the array).
   * @param length is the {@link #getLength() number of characters} to scan from {@code characters} starting at
   *        {@code offset} (typically <code>characters.length - offset</code>).
   */
  public CharSequenceScanner(char[] characters, int offset, int length) {

    this(characters, offset, length, null);
  }

  /**
   * The constructor.
   *
   * @param characters is an array containing the characters to scan.
   * @param offset is the index of the first char to scan in {@code characters} (typically {@code 0} to start at the
   *        beginning of the array).
   * @param length is the {@link #getLength() number of characters} to scan from {@code characters} starting at
   *        {@code offset} (typically <code>characters.length - offset</code>).
   * @param messageHandler the {@link TextFormatMessageHandler}.
   */
  public CharSequenceScanner(char[] characters, int offset, int length, TextFormatMessageHandler messageHandler) {

    this(characters, offset, length, messageHandler, 1, 1);
  }

  /**
   * The constructor.
   *
   * @param characters is an array containing the characters to scan.
   * @param offset is the index of the first char to scan in {@code characters} (typically {@code 0} to start at the
   *        beginning of the array).
   * @param length is the {@link #getLength() number of characters} to scan from {@code characters} starting at
   *        {@code offset} (typically <code>characters.length - offset</code>).
   * @param messageHandler the {@link TextFormatMessageHandler}.
   * @param line the initial {@link #getLine() line}.
   * @param column the initial {@link #getColumn() column}.
   */
  public CharSequenceScanner(char[] characters, int offset, int length, TextFormatMessageHandler messageHandler,
      int line, int column) {

    super(characters, messageHandler, line, column);
    if (offset < 0) {
      throw new IndexOutOfBoundsException(Integer.toString(offset));
    }
    if (length < 0) {
      throw new IndexOutOfBoundsException(Integer.toString(length));
    }
    if (offset > characters.length - length) {
      throw new IndexOutOfBoundsException(Integer.toString(offset + length));
    }
    this.offset = offset;
    this.initialOffset = offset;
    this.limit = offset + length;
    this.offset = this.initialOffset;
  }

  /**
   * @see java.lang.CharSequence#charAt(int)
   *
   * @param index is the index of the requested character.
   * @return the character at the given {@code index}.
   */
  public char charAt(int index) {

    return this.buffer[this.initialOffset + index];
  }

  @Override
  public int getPosition() {

    return this.offset - this.initialOffset;
  }

  /**
   * @see java.lang.CharSequence#length()
   *
   * @return the total length of the {@link #getOriginalString() string to parse}.
   */
  public int getLength() {

    return this.limit - this.initialOffset;
  }

  /**
   * @see String#substring(int, int)
   *
   * @param start the start index, inclusive.
   * @param end the end index, exclusive.
   * @return the specified substring.
   */
  public String substring(int start, int end) {

    return new String(this.buffer, this.initialOffset + start, end - start);
  }

  /**
   * This method gets the {@link #getOriginalString() original string} where the {@link #substring(int, int) substring}
   * specified by {@code start} and {@code end} is replaced by {@code substitute}.
   *
   * @param substitute is the string used as replacement.
   * @param start is the inclusive start index of the substring to replace.
   * @param end is the exclusive end index of the substring to replace.
   * @return the {@link #getOriginalString() original string} with the specified substring replaced by
   *         {@code substitute}.
   */
  public String getReplaced(String substitute, int start, int end) {

    int restLength = this.limit - end;
    StringBuilder builder = builder(null);
    builder.append(this.buffer, this.initialOffset, start);
    builder.append(substitute);
    builder.append(this.buffer, this.initialOffset + end, restLength);
    return builder.toString();
  }

  /**
   * This method appends the {@link #substring(int, int) substring} specified by {@code start} and {@code end} to the
   * given {@code buffer}. <br>
   * This avoids the overhead of creating a new string and copying the char array.
   *
   * @param appendable is the buffer where to append the substring to.
   * @param start the start index, inclusive.
   * @param end the end index, exclusive.
   */
  public void appendSubstring(StringBuilder appendable, int start, int end) {

    appendable.append(this.buffer, this.initialOffset + start, end - start);
  }

  /**
   * This method gets the current position in the stream to scan. It will initially be {@code 0}. In other words this
   * method returns the number of characters that have already been {@link #next() consumed}.
   *
   * @return the current index position.
   */
  public int getCurrentIndex() {

    return this.offset - this.initialOffset;
  }

  /**
   * This method sets the {@link #getCurrentIndex() current index}.
   *
   * @param index is the next index position to set. The value has to be greater or equal to {@code 0} and less or equal
   *        to {@link #getLength()} .
   */
  public void setCurrentIndex(int index) {

    // yes, index == getLength() is allowed - that is the state when the end is reached and
    // setCurrentIndex(getCurrentPosition()) should NOT cause an exception...
    if ((index < 0) || (index > getLength())) {
      throw new IndexOutOfBoundsException(Integer.toString(index));
    }
    this.offset = this.initialOffset + index;
  }

  @Override
  public boolean hasNext() {

    return (this.offset < this.limit);
  }

  @Override
  public char next() {

    if (this.offset < this.limit) {
      return handleChar(this.buffer[this.offset++]);
    } else {
      return 0;
    }
  }

  @Override
  public char peek() {

    if (this.offset < this.limit) {
      return this.buffer[this.offset];
    } else {
      return 0;
    }
  }

  @Override
  public char peek(int lookaheadOffset) {

    int i = this.offset + lookaheadOffset;
    if ((i < this.limit) && (i >= this.initialOffset)) {
      if (i < this.limit) {
        return this.buffer[i];
      }
    }
    return EOS;
  }

  /**
   * This method peeks the number of {@link #peek() next characters} given by {@code count} and returns them as string.
   * If there are less characters {@link #hasNext() available} the returned string will be shorter than {@code count}
   * and only contain the available characters. Unlike {@link #read(int)} this method does NOT consume the characters
   * and will therefore NOT change the state of this scanner.
   *
   * @param count is the number of characters to peek. You may use {@link Integer#MAX_VALUE} to peek until the end of
   *        text (EOT) if the data-size is suitable.
   * @return a string with the given number of characters or all available characters if less than {@code count}. Will
   *         be the empty string if no character is {@link #hasNext() available} at all.
   */
  @Override
  public String peekString(int count) {

    int len = this.limit - this.offset;
    if (len > count) {
      len = count;
    }
    String result = new String(this.buffer, this.offset, len);
    return result;
  }

  @Override
  public String peekWhile(CharFilter filter, int maxLen) {

    if (maxLen < 0) {
      throw new IllegalArgumentException("Max must NOT be negative: " + maxLen);
    }
    int len = 0;
    int end = this.limit - this.offset;
    if (end > maxLen) {
      end = maxLen;
    }
    while (len < end) {
      char c = this.buffer[this.offset + len];
      if (!filter.accept(c)) {
        break;
      }
      len++;
    }
    if (len == 0) {
      return "";
    } else {
      return new String(this.buffer, this.offset, len);
    }
  }

  @Override
  public String readUntil(CharFilter filter, boolean acceptEot) {

    int start = this.offset;
    while (this.offset < this.limit) {
      char c = this.buffer[this.offset];
      if (filter.accept(c)) {
        return new String(this.buffer, start, this.offset - start);
      }
      handleChar(c);
      this.offset++;
    }
    if (acceptEot) {
      int len = this.offset - start;
      if (len > 0) {
        return new String(this.buffer, start, len);
      } else {
        return "";
      }
    } else {
      return null;
    }
  }

  @Override
  protected boolean expectRestWithLookahead(char[] stopChars, boolean ignoreCase, Runnable appender, boolean skip) {

    int myCharsIndex = this.offset + 1;
    int stopCharsIndex = 1;
    while (stopCharsIndex < stopChars.length) {
      char c = this.buffer[myCharsIndex++];
      char stopChar = stopChars[stopCharsIndex++];
      if ((c != stopChar) && (!ignoreCase || (Character.toLowerCase(c) != stopChar))) {
        return false;
      }
    }
    if (skip) {
      setOffset(myCharsIndex);
    }
    return true;
  }

  @Override
  public boolean expect(String expected, boolean ignoreCase, boolean lookahead, int off) {

    int len = expected.length();
    int newPos = this.offset + off;
    if (newPos + len > this.limit) {
      return false;
    }
    for (int i = 0; i < len; i++) {
      char c = this.buffer[newPos];
      char exp = expected.charAt(i);
      if (c != exp) {
        if (!ignoreCase) {
          return false;
        }
        if (Character.toLowerCase(c) != Character.toLowerCase(exp)) {
          return false;
        }
      }
      newPos++;
    }
    if (!lookahead) {
      setOffset(newPos);
    }
    return true;
  }

  /**
   * This method gets the tail of this scanner without changing the state.
   *
   * @return the tail of this scanner.
   */
  protected String getTail() {

    String tail = "";
    if (this.offset < this.limit) {
      tail = new String(this.buffer, this.offset, this.limit - this.offset + 1);
    }
    return tail;
  }

  /**
   * This method gets the tail of this scanner limited (truncated) to the given {@code maximum} number of characters
   * without changing the state.
   *
   * @param maximum is the maximum number of characters to return from the {@link #getTail() tail}.
   * @return the tail of this scanner.
   */
  protected String getTail(int maximum) {

    String tail = "";
    if (this.offset < this.limit) {
      int count = this.limit - this.offset + 1;
      if (count > maximum) {
        count = maximum;
      }
      tail = new String(this.buffer, this.offset, count);
    }
    return tail;
  }

  @Override
  public void require(String expected, boolean ignoreCase) {

    if (!expect(expected, ignoreCase)) {
      throw new IllegalStateException("Expecting '" + expected + "' but found: " + getTail(expected.length()));
    }
  }

  @Override
  public String readWhile(CharFilter filter, int min, int max) {

    int currentPos = this.offset;
    int len = skipWhile(filter, max);
    if (len == 0) {
      return "";
    } else {
      return new String(this.buffer, currentPos, len);
    }
  }

  /**
   * This method gets the original string to parse.
   *
   * @see CharSequenceScanner#CharSequenceScanner(String)
   *
   * @return the original string.
   */
  public String getOriginalString() {

    if (this.string != null) {
      this.string = new String(this.buffer, this.initialOffset, getLength());
    }
    return this.string;
  }

  @Override
  public void close() {

    this.buffer = null;
  }

}
