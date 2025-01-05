/* Copyright (c) The m-m-m Team, Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0 */
package io.github.mmm.scanner;

import java.io.IOException;
import java.io.Reader;

import io.github.mmm.base.filter.CharFilter;
import io.github.mmm.base.io.ReaderHelper;
import io.github.mmm.base.text.TextFormatMessageHandler;

/**
 * Implementation of {@link CharStreamScanner} that adapts a {@link Reader} to read and parse textual data. Unlike
 * {@link CharStreamScanner} it allows to parse very long textual data without reading it entirely into the heap memory
 * as a {@link String}.
 *
 * @since 1.0.0
 */
public class CharReaderScanner extends AbstractCharStreamScanner {

  private Reader reader;

  private final char[] charBuffer;

  private String lookaheadBuffer;

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

    super("", messageHandler);
    this.charBuffer = new char[capacity + 1];
    this.reader = reader;
  }

  @Override
  public int getPosition() {

    return this.position + this.offset;
  }

  @Override
  public int peek(int lookaheadOffset) {

    if (hasNext()) {
      int i = this.offset + lookaheadOffset;
      if (i < this.limit) {
        return this.buffer.codePointAt(i);
      }
      if (fillLookahead()) {
        i = i - this.limit;
        if (i < this.lookaheadLimit) {
          return this.lookaheadBuffer.codePointAt(i);
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
      return this.buffer.substring(this.offset, this.offset + count);
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
      return this.buffer.substring(this.offset, this.limit);
    }
  }

  @Override
  public String peekWhile(CharFilter filter, int maxLen) {

    if (!hasNext()) {
      return "";
    }
    int end = this.offset + maxLen;
    if (end > this.limit) {
      end = this.limit;
    }
    int i = this.offset;
    while (i < end) {
      int cp = this.buffer.codePointAt(i);
      if (!filter.accept(cp)) {
        return this.buffer.substring(this.offset, i);
      }
      i++;
    }
    if (fillLookahead()) {
      int rest = i - this.offset;
      int fullRest = rest + this.lookaheadLimit;
      if ((maxLen > fullRest) && !isEos()) {
        throwLookaheadError(maxLen);
      }
      i = 0;
      end = maxLen - rest;
      while (i < end) {
        int cp = this.lookaheadBuffer.codePointAt(i);
        if (!filter.accept(cp)) {
          break;
        }
        i++;
      }
      StringBuilder sb = new StringBuilder(rest + i);
      sb.append(this.buffer, this.offset, this.limit);
      sb.append(this.lookaheadBuffer, 0, i);
      return sb.toString();
    } else {
      return this.buffer.substring(this.offset, end);
    }
  }

  @Override
  public String getBufferToParse() {

    if (this.offset < this.limit) {
      if (this.lookaheadLimit > 0) {
        int count = this.limit - this.offset;
        StringBuilder sb = new StringBuilder(this.lookaheadLimit + count);
        sb.append(this.buffer, this.offset, count);
        sb.append(this.lookaheadBuffer, 0, this.lookaheadLimit);
        return sb.toString();
      } else {
        return this.buffer.substring(this.offset, this.limit);
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
    this.limit = ReaderHelper.read(this.reader, this.charBuffer);
    if (this.limit == -1) {
      close();
      this.buffer = "";
      this.limit = 0;
      return false;
    }
    this.buffer = new String(this.charBuffer, 0, this.limit);
    this.limit = this.buffer.length();
    return true;
  }

  private boolean fillLookahead() {

    if (this.lookaheadLimit > 0) {
      return true;
    }
    if (this.reader == null) {
      return false;
    }
    try {
      this.lookaheadLimit = 0;
      while (this.lookaheadLimit == 0) {
        this.lookaheadLimit = this.reader.read(this.charBuffer);
      }
      if (this.lookaheadLimit == -1) {
        close();
        this.lookaheadBuffer = "";
        this.lookaheadLimit = 0;
        return false;
      }
      this.lookaheadBuffer = new String(this.charBuffer, 0, this.lookaheadLimit);
      this.lookaheadLimit = this.lookaheadBuffer.length();
      return true;
    } catch (IOException e) {
      throw new IllegalStateException("Read error.", e);
    }
  }

  private void shiftLookahead() {

    this.position += this.limit;
    setOffset(this.limit);
    String tmp = this.lookaheadBuffer;
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
    String myBuffer = this.buffer;
    int expectedIndex = 0;
    while (expectedIndex < expectedLength) {
      int cp = myBuffer.codePointAt(myOffset++);
      int expCp = expected.codePointAt(expectedIndex++);
      if (cp != expCp) {
        if (!ignoreCase) {
          return false;
        }
        if (Character.toLowerCase(cp) != Character.toLowerCase(expCp)) {
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

    if (length > this.charBuffer.length) {
      throwLookaheadError(length);
    }
  }

  private void throwLookaheadError(int length) {

    throw new IllegalArgumentException(
        "Lookahead size of " + length + " characters exceeds the configured buffer size of " + this.charBuffer.length);
  }

  @Override
  protected boolean expectRestWithLookahead(String stopChars, boolean ignoreCase, Runnable appender, boolean skip) {

    int bufferIndex = this.offset + 1;
    int stopIndex = 1;
    int stopLength = stopChars.length();
    while (stopIndex < stopLength) {
      if (bufferIndex == this.limit) { // lookahead required?
        if (!fillLookahead()) {
          if (skip) {
            setOffset(this.limit);
          }
          return false;
        }
        int lookaheadIndex = 0;
        while (stopIndex < stopLength) {
          int cp = this.lookaheadBuffer.codePointAt(lookaheadIndex++);
          int stopCp = stopChars.codePointAt(stopIndex++);
          if (cp != stopCp && (!ignoreCase || (Character.toLowerCase(cp) != stopCp))) {
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
        int cp = this.buffer.codePointAt(bufferIndex++);
        int stopCp = stopChars.codePointAt(stopIndex++);
        if (cp != stopCp && (!ignoreCase || (Character.toLowerCase(cp) != stopCp))) {
          return false;
        }
      }
    }
    if (skip) {
      setOffset(bufferIndex);
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
