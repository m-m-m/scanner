/* Copyright (c) The m-m-m Team, Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0 */
package io.github.mmm.scanner;

import java.io.IOException;
import java.io.Reader;

import io.github.mmm.base.filter.CharFilter;
import io.github.mmm.base.text.TextFormatMessageHandler;

/**
 * Implementation of {@link CharStreamScanner} that adapts a {@link Reader} to read and parse textual data.
 */
public class CharReaderScanner extends AbstractCharStreamScanner {

  private Reader reader;

  private char[] lookaheadBuffer;

  private int lookaheadLimit;

  /** @see #getPosition() */
  protected int position;

  /**
   * The constructor.
   */
  public CharReaderScanner() {

    this(null, null);
  }

  /**
   * The constructor.
   *
   * @param messageHandler the {@link TextFormatMessageHandler}.
   */
  public CharReaderScanner(TextFormatMessageHandler messageHandler) {

    this(messageHandler, null);
  }

  /**
   * The constructor.
   *
   * @param reader the (initial) {@link Reader}.
   */
  public CharReaderScanner(Reader reader) {

    this(null, reader);
  }

  /**
   * The constructor.
   *
   * @param messageHandler the {@link TextFormatMessageHandler}.
   * @param reader the (initial) {@link Reader}.
   */
  public CharReaderScanner(TextFormatMessageHandler messageHandler, Reader reader) {

    this(4096, messageHandler, reader);
  }

  /**
   * The constructor.
   *
   * @param capacity the buffer capacity.
   */
  public CharReaderScanner(int capacity) {

    this(capacity, null, null);
  }

  /**
   * The constructor.
   *
   * @param capacity the buffer capacity.
   * @param messageHandler the {@link TextFormatMessageHandler}.
   */
  public CharReaderScanner(int capacity, TextFormatMessageHandler messageHandler) {

    this(capacity, messageHandler, null);
  }

  /**
   * The constructor.
   *
   * @param capacity the buffer capacity.
   * @param reader the (initial) {@link Reader}.
   */
  public CharReaderScanner(int capacity, Reader reader) {

    this(capacity, null, reader);
  }

  /**
   * The constructor.
   *
   * @param capacity the buffer capacity.
   * @param messageHandler the {@link TextFormatMessageHandler}.
   * @param reader the (initial) {@link Reader}.
   */
  public CharReaderScanner(int capacity, TextFormatMessageHandler messageHandler, Reader reader) {

    super(capacity, messageHandler);
    this.reader = reader;
  }

  @Override
  public int getPosition() {

    return this.position + this.offset;
  }

  @Override
  public char peek(int lookaheadOffset) {

    if (hasNext()) {
      int i = this.offset + lookaheadOffset;
      if (i < this.limit) {
        return this.buffer[i];
      }
      if (fillLookahead()) {
        i = i - this.limit;
        if (i < this.lookaheadLimit) {
          return this.lookaheadBuffer[i];
        } else {
          throwLookaheadError(lookaheadOffset);
        }
      }
    }
    return 0;
  }

  @Override
  public String peekString(int count) {

    if (!hasNext()) {
      return "";
    }
    int rest = this.limit - this.offset;
    if (rest >= count) {
      return new String(this.buffer, this.offset, count);
    } else if (fillLookahead()) {
      int fullRest = rest + this.lookaheadLimit;
      if ((count > fullRest) && !isEos()) {
        throwLookaheadError(count);
      }
      StringBuilder sb = new StringBuilder(fullRest);
      sb.append(this.buffer, this.offset, rest);
      sb.append(this.lookaheadBuffer, 0, count - rest);
      return sb.toString();
    } else {
      return new String(this.buffer, this.offset, rest);
    }
  }

  @Override
  public String peekWhile(CharFilter filter, int maxLen) {

    if (!hasNext()) {
      return "";
    }
    int rest = this.limit - this.offset;
    if (rest > maxLen) {
      rest = maxLen;
    }
    int len = 0;
    while (len < rest) {
      char c = this.buffer[this.offset + len];
      if (!filter.accept(c)) {
        return new String(this.buffer, this.offset, len);
      }
      len++;
    }
    if (fillLookahead()) {
      int fullRest = rest + this.lookaheadLimit;
      if ((maxLen > fullRest) && !isEos()) {
        throwLookaheadError(maxLen);
      }
      len = 0;
      int end = maxLen - rest;
      while (len < end) {
        char c = this.lookaheadBuffer[len];
        if (!filter.accept(c)) {
          break;
        }
        len++;
      }
      StringBuilder sb = new StringBuilder(rest + len);
      sb.append(this.buffer, this.offset, rest);
      sb.append(this.lookaheadBuffer, 0, len);
      return sb.toString();
    } else {
      return new String(this.buffer, this.offset, rest);
    }
  }

  @Override
  public String getBufferToParse() {

    if (this.offset < this.limit) {
      int count = this.limit - this.offset;
      if (this.lookaheadLimit > 0) {
        StringBuilder sb = new StringBuilder(this.lookaheadLimit + count);
        sb.append(this.buffer, this.offset, count);
        sb.append(this.lookaheadBuffer, 0, this.lookaheadLimit);
        return sb.toString();
      } else {
        return new String(this.buffer, this.offset, count);
      }
    } else {
      return "";
    }
  }

  /**
   * Resets this buffer for reuse with a new {@link Reader}.<br>
   * This will also reset the {@link #getPosition() position}.
   *
   * @param reader the new {@link Reader} to set. May be {@code null} to entirely clear this buffer.
   */
  public void setReader(Reader reader) {

    this.position = 0;
    reset();
    this.reader = reader;
  }

  @Override
  protected boolean fill() {

    if (this.lookaheadLimit > 0) {
      shiftLookahead();
      return true;
    }
    if (this.reader == null) {
      this.limit = this.offset;
      return false;
    }
    setOffset(this.limit);
    this.position += this.limit;
    this.offset = 0;
    try {
      this.limit = 0;
      while (this.limit == 0) {
        this.limit = this.reader.read(this.buffer);
      }
      if (this.limit == -1) {
        close();
        this.limit = 0;
        return false;
      }
      return true;
    } catch (IOException e) {
      throw new IllegalStateException("Read error.", e);
    }
  }

  private boolean fillLookahead() {

    if (this.lookaheadLimit > 0) {
      return true;
    }
    if (this.reader == null) {
      return false;
    }
    if (this.lookaheadBuffer == null) {
      this.lookaheadBuffer = new char[this.buffer.length];
    }
    try {
      this.lookaheadLimit = 0;
      while (this.lookaheadLimit == 0) {
        this.lookaheadLimit = this.reader.read(this.lookaheadBuffer);
      }
      if (this.lookaheadLimit == -1) {
        close();
        this.lookaheadLimit = 0;
        return false;
      }
      return true;
    } catch (IOException e) {
      throw new IllegalStateException("Read error.", e);
    }
  }

  private void shiftLookahead() {

    this.position += this.limit;
    setOffset(this.limit);
    char[] tmp = this.lookaheadBuffer;
    this.lookaheadBuffer = this.buffer;
    this.buffer = tmp;
    this.offset = 0;
    this.limit = this.lookaheadLimit;
    this.lookaheadLimit = 0;
  }

  @Override
  public void close() {

    if (this.reader == null) {
      return;
    }
    try {
      this.reader.close();
    } catch (IOException e) {
      LOG.warn("Failed to close reader.", e);
    }
    this.reader = null;
  }

  @Override
  public boolean isEos() {

    return (this.reader == null);
  }

  @Override
  protected boolean isEob() {

    if (this.reader != null) {
      return false;
    }
    if (this.lookaheadLimit > 0) {
      return false;
    }
    return true;
  }

  @Override
  public boolean isEot() {

    if (this.offset < this.limit) {
      return false;
    }
    if (this.lookaheadLimit > 0) {
      return false;
    }
    if (this.reader != null) {
      return false;
    }
    return true;
  }

  @Override
  public boolean expect(String expected, boolean ignoreCase, boolean lookahead, int off) {

    int expectedLength = expected.length();
    if (expectedLength == 0) {
      return true;
    }
    if (!hasNext()) {
      return false;
    }
    int myOffset = this.offset + off;
    if (isEos()) {
      int rest = this.lookaheadLimit + (this.limit - myOffset);
      if (expectedLength > rest) {
        return false;
      }
    } else {
      verifyLookahead(expectedLength);
    }
    int myLimit = this.limit;
    char[] myBuffer = this.buffer;
    int expectedIndex = 0;
    while (expectedIndex < expectedLength) {
      char c = myBuffer[myOffset++];
      char exp = expected.charAt(expectedIndex++);
      if (c != exp) {
        if (!ignoreCase) {
          return false;
        }
        if (Character.toLowerCase(c) != Character.toLowerCase(exp)) {
          return false;
        }
      }
      if ((myOffset >= myLimit) && (expectedIndex < expectedLength)) {
        if (myBuffer != this.buffer) {
          throw new IllegalStateException();
        }
        if (!fillLookahead()) {
          return false;
        }
        myBuffer = this.lookaheadBuffer;
        myOffset = 0;
        myLimit = this.lookaheadLimit;
      }
    }
    if (!lookahead) {
      if (myBuffer == this.lookaheadBuffer) {
        shiftLookahead();
      }
      setOffset(myOffset);
    }
    return true;
  }

  @Override
  protected void verifyLookahead(int length) {

    if (length > this.buffer.length) {
      throwLookaheadError(length);
    }
  }

  private void throwLookaheadError(int length) {

    throw new IllegalArgumentException(
        "Lookahead size of " + length + " characters exceeds the configured buffer size of " + this.buffer.length);
  }

  @Override
  protected boolean expectRestWithLookahead(char[] stopChars, boolean ignoreCase, Runnable appender, boolean skip) {

    int myCharsIndex = this.offset + 1;
    int subCharsIndex = 1;
    while (subCharsIndex < stopChars.length) {
      if (myCharsIndex == this.limit) { // lookahead required?
        if (!fillLookahead()) {
          if (skip) {
            setOffset(this.limit);
          }
          return false;
        }
        int lookaheadIndex = 0;
        while (subCharsIndex < stopChars.length) {
          char c = this.lookaheadBuffer[lookaheadIndex++];
          char stopChar = stopChars[subCharsIndex++];
          if (c != stopChar && (!ignoreCase || (Character.toLowerCase(c) != stopChar))) {
            return false;
          }
        }
        if (appender != null) {
          appender.run();
        }
        if (skip) {
          shiftLookahead();
          setOffset(lookaheadIndex);
        }
        return true;
      } else {
        char c = this.buffer[myCharsIndex++];
        char stopChar = stopChars[subCharsIndex++];
        if (c != stopChar && (!ignoreCase || (Character.toLowerCase(c) != stopChar))) {
          return false;
        }
      }
    }
    if (skip) {
      setOffset(myCharsIndex);
    }
    return true;
  }

  @Override
  protected void reset() {

    super.reset();
    this.lookaheadLimit = 0;
    this.position = 0;
  }

}
