/* Copyright (c) The m-m-m Team, Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0 */
package io.github.mmm.scanner;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.mmm.base.filter.CharFilter;
import io.github.mmm.base.number.NumberType;
import io.github.mmm.base.text.CaseHelper;
import io.github.mmm.base.text.TextFormatMessage;
import io.github.mmm.base.text.TextFormatMessageHandler;
import io.github.mmm.base.text.TextFormatMessageType;
import io.github.mmm.scanner.number.CharScannerNumberParser;
import io.github.mmm.scanner.number.CharScannerNumberParserLang;
import io.github.mmm.scanner.number.CharScannerNumberParserString;
import io.github.mmm.scanner.number.CharScannerRadixHandler;
import io.github.mmm.scanner.number.CharScannerRadixMode;

/**
 * Abstract implementation of {@link CharStreamScanner}.<br>
 *
 * @since 1.0.0
 */
public abstract class AbstractCharStreamScanner implements CharStreamScanner {

  static final Logger LOG = LoggerFactory.getLogger(AbstractCharStreamScanner.class);

  private static final CharFilter FILTER_SINGLE_QUOTE = (c) -> (c == '\'');

  private final TextFormatMessageHandler messageHandler;

  /** The internal buffer with character data. */
  protected String buffer;

  /** The start position in the {@link #buffer} from where reading operations consumer data from. */
  protected int offset;

  /**
   * The limit in the {@link #buffer}. If the offset has reached this position no further reading (not even the current
   * {@link #offset}) from the {@link #buffer} is allowed.
   */
  protected int limit;

  /** @see #getLine() */
  protected int line;

  /** @see #getColumn() */
  protected int column;

  /** A {@link StringBuilder} instance that can be shared and reused. May initially be <code>null</code>. */
  private StringBuilder sb;

  /**
   * The constructor.
   *
   * @param charBuffer the internal {@code char[]} buffer.
   * @param messageHandler the {@link TextFormatMessageHandler}.
   */
  public AbstractCharStreamScanner(String charBuffer, TextFormatMessageHandler messageHandler) {

    this(charBuffer, messageHandler, 1, 1);
  }

  /**
   * The constructor.
   *
   * @param buffer the internal {@code char[]} buffer.
   * @param messageHandler the {@link TextFormatMessageHandler}.
   * @param line the initial {@link #getLine() line}.
   * @param column the initial {@link #getColumn() column}.
   */
  public AbstractCharStreamScanner(String buffer, TextFormatMessageHandler messageHandler, int line, int column) {

    super();
    if (messageHandler == null) {
      this.messageHandler = SimpleTextFormatMessageHandler.get();
    } else {
      this.messageHandler = messageHandler;
    }
    this.buffer = buffer;
    this.offset = 0;
    this.limit = 0;
    this.line = line;
    this.column = column;
  }

  @Override
  public int getLine() {

    return this.line;
  }

  @Override
  public int getColumn() {

    return this.column;
  }

  @Override
  public void addMessage(TextFormatMessage message) {

    this.messageHandler.add(message);
  }

  @Override
  public List<TextFormatMessage> getMessages() {

    return this.messageHandler.getMessages();
  }

  /**
   * Resets the internal state.
   */
  protected void reset() {

    this.offset = 0;
    this.limit = 0;
    this.line = 1;
    this.column = 1;
    if (this.sb != null) {
      this.sb.setLength(0);
    }
  }

  /**
   * @param builder a local {@link StringBuilder} variable to be allocated lazily.
   * @return the given {@link StringBuilder} if not {@code null} or otherwise a reused {@link StringBuilder} instance
   *         that has been reset.
   */
  protected StringBuilder builder(StringBuilder builder) {

    if (builder != null) {
      return builder;
    }
    if (this.sb == null) {
      this.sb = new StringBuilder(64);
    }
    this.sb.setLength(0);
    return this.sb;
  }

  /**
   * @param builder a local {@link StringBuilder} variable to be allocated lazily.
   * @param start the start index in the underlying buffer to append.
   * @param end the limit index in the underlying buffer pointing to the next position after the last character to
   *        append.
   * @return the given {@link StringBuilder} if not {@code null} or otherwise a reused {@link StringBuilder} instance
   *         that has been reset.
   */
  protected StringBuilder append(StringBuilder builder, int start, int end) {

    int len = end - start;
    if (len <= 0) {
      return builder;
    }
    StringBuilder b = builder(builder);
    b.append(this.buffer, start, end);
    return b;
  }

  /**
   * @param builder the local {@link StringBuilder} instance where data may already have been appended to. May be
   *        {@code null}.
   * @param start the start index in the underlying buffer to append.
   * @param end the limit index in the underlying buffer pointing to the next position after the last character to
   *        append.
   * @return the {@link String} with the underlying buffer data from {@code start} to {@code end-1} potentially appended
   *         to the given {@link StringBuilder} if not {@code null}.
   */
  protected String getAppended(StringBuilder builder, int start, int end) {

    if (end <= start) {
      return eot(builder, true);
    }
    if (builder == null) {
      return this.buffer.substring(start, end);
    } else {
      builder.append(this.buffer, start, end);
      return builder.toString();
    }
  }

  @Override
  public boolean hasNext() {

    if (this.offset < this.limit) {
      return true;
    }
    return fill();
  }

  /**
   * @return {@code true} if the end of stream (EOS) has been reached, {@code false} otherwise. If {@code true} (EOS)
   *         the internal buffer contains the entire rest of the data to scan in memory. If then also all data is
   *         consumed from the buffer, {@link #isEot() EOT} has been reached. For instances of that are not backed by an
   *         underlying stream of data (like {@link CharSequenceScanner}) this method will always return {@code true}.
   */
  protected boolean isEos() {

    return true;
  }

  /**
   * @return {@code true} if end of buffer (EOB) or in other words no data is available after the current
   *         {@link #buffer}, {@code false} otherwise (e.g. if not {@link #isEos() EOS}).
   */
  protected boolean isEob() {

    return true;
  }

  /**
   * <b>ATTENTION:</b>
   *
   * @return {@code true} if end of text (EOT) is known to have been reached, {@code false} otherwise. The returned
   *         result will be almost the same as <code>!{@link #hasNext()}</code> but this method will not modify the
   *         state of this scanner (read additional data, modify buffers, etc.). However, if the underlying stream is
   *         already consumed without returning {@code -1} to signal {@link #isEos() EOS} this method may return
   *         {@code false} even though the next call of {@link #hasNext()} may also return {@code false}.
   */
  protected boolean isEot() {

    return (this.offset >= this.limit);
  }

  /**
   * Consumes all remaining data in the internal {@link #buffer} and fills the {@link #buffer} with further data (if
   * available from underlying source such as a stream/reader). If the {@link #isEos() end of the stream} has not been
   * reached, all buffers should be filled now.
   *
   * @return {@code true} if data was filled, {@code false} if {@link #isEos() EOS}.
   */
  protected boolean fill() {

    return false;
  }

  @Override
  public int next() {

    if (hasNext()) {
      return handleCodePoint(this.buffer.codePointAt(this.offset));
    }
    return EOS;
  }

  /**
   * Updates {@link #getColumn() column} and {@link #getLine() line} if the given character is consumed.
   *
   * @param codePoint the character to handle.
   * @return the given character.
   */
  protected int handleCodePoint(int codePoint) {

    if (codePoint == '\n') {
      this.line++;
      this.column = 1;
    } else {
      this.column++;
    }
    if (codePoint >= 0x010000) {
      this.offset += 2;
    } else {
      this.offset++;
    }
    return codePoint;
  }

  /**
   * @param newOffset the new {@link #offset} value to set. Should be greater or equal to the current {@link #offset}.
   */
  protected void setOffset(int newOffset) {

    assert (newOffset >= this.offset);
    assert (newOffset <= this.limit);
    while (this.offset < newOffset) {
      handleCodePoint(this.buffer.codePointAt(this.offset));
    }
  }

  @Override
  public int peek() {

    if (hasNext()) {
      return this.buffer.codePointAt(this.offset);
    }
    return EOS;
  }

  /**
   * @param builder the optional {@link StringBuilder} where data may have already been appended.
   * @param acceptEot {@code true} to accept {@link #isEot() EOT}, {@code false} otherwise.
   * @return {@code null} if {@code acceptEot} is {@code false}, otherwise the {@link String} from the given
   *         {@link StringBuilder} or the empty {@link String} in case the {@link StringBuilder} was {@code null}.
   */
  protected String eot(StringBuilder builder, boolean acceptEot) {

    if (acceptEot) {
      if (builder == null) {
        return "";
      }
      return builder.toString();
    }
    return null;
  }

  @Override
  public String readUntil(int stop, boolean acceptEot) {

    if (!hasNext()) {
      return eot(null, acceptEot);
    }
    StringBuilder builder = null;
    while (true) {
      int start = this.offset;
      while (this.offset < this.limit) {
        int codePoint = this.buffer.codePointAt(this.offset);
        handleCodePoint(codePoint);
        if (codePoint == stop) {
          return getAppended(builder, start, this.offset - 1);
        }
      }
      builder = append(builder, start, this.limit);
      if (!fill()) {
        return eot(builder, acceptEot);
      }
    }
  }

  @Override
  public String readUntil(int stop, boolean acceptEot, CharScannerSyntax syntax) {

    String result = readUntil(c -> (c == stop), acceptEot, syntax);
    expectOne(stop);
    return result;
  }

  @Override
  public String readUntil(CharFilter filter, boolean acceptEot, CharScannerSyntax syntax) {

    if (!hasNext()) {
      return eot(null, acceptEot);
    }
    CharScannerSyntaxState state = new CharScannerSyntaxState(syntax, filter);
    while (true) {
      // int end = this.limit;
      while (this.offset < this.limit) {
        int codePoint = this.buffer.codePointAt(this.offset);
        state.parse(codePoint);
        if (state.done) {
          return state.builder.toString();
        }
        handleCodePoint(codePoint);
      }
      boolean eot = isEot();
      if (!eot || acceptEot) {
        int len = this.limit - state.start;
        if (state.quoteEscapeActive) {
          if (state.activeQuoteEscape == state.activeQuoteEnd) {
            // omit quote on appending of rest
            len--;
          }
        }
        if (len > 0) {
          // append rest
          StringBuilder builder;
          if (state.activeEntityEnd == 0) {
            builder = state.builder;
          } else {
            builder = state.getEntityBuilder();
          }
          builder.append(this.buffer, state.start, len);
        }
      }
      state.start = 0;
      eot = !hasNext(); // fill buffers and get sure about EOT
      if (eot) {
        return eot(state.builder, acceptEot);
      }
    }
  }

  @Override
  public String readUntil(int stop, boolean acceptEot, int escape) {

    if (!hasNext()) {
      eot(null, acceptEot);
    }
    StringBuilder builder = null;
    while (true) {
      int start = this.offset;
      while (this.offset < this.limit) {
        int codePoint = this.buffer.codePointAt(this.offset);
        handleCodePoint(codePoint);
        if (codePoint == escape) {
          builder = append(builder, start, this.offset - 1);
          // lookahead
          if (this.offset >= this.limit) {
            if (!fill()) {
              return eot(builder, acceptEot);
            }
          }
          codePoint = this.buffer.codePointAt(this.offset);
          if ((escape == stop) && (codePoint != stop)) {
            return eot(builder, true);
          } else {
            // escape character
            builder = builder(builder);
            builder.appendCodePoint(codePoint);
            handleCodePoint(codePoint);
            start = this.offset;
          }
        } else if (codePoint == stop) {
          return getAppended(builder, start, this.offset - 1);
        }
      }
      builder = append(builder, start, this.limit);
      if (!fill()) {
        return eot(builder, acceptEot);
      }
    }
  }

  @Override
  public String readUntil(CharFilter filter, boolean acceptEot) {

    if (!hasNext()) {
      eot(null, acceptEot);
    }
    StringBuilder builder = null;
    while (true) {
      int start = this.offset;
      while (this.offset < this.limit) {
        int codePoint = this.buffer.codePointAt(this.offset);
        if (filter.accept(codePoint)) {
          return getAppended(builder, start, this.offset);
        }
        handleCodePoint(codePoint);
      }
      builder = append(builder, start, this.limit);
      if (!fill()) {
        return eot(builder, acceptEot);
      }
    }
  }

  @Override
  public String readUntil(CharFilter stopFilter, boolean acceptEot, String stop, boolean ignoreCase,
      final boolean trim) {

    int stopLength = stop.length();
    if (stopLength == 0) {
      return "";
    }
    verifyLookahead(stopLength);
    if (!hasNext()) {
      return eot(null, acceptEot);
    }
    if (trim) {
      skipWhile(' ');
    }
    String stopChars;
    if (ignoreCase) {
      stopChars = CaseHelper.toLowerCase(stop);
    } else {
      stopChars = stop;
    }
    int first = stopChars.codePointAt(0);
    Appender appender = newAppender(trim);
    while (true) {
      appender.start = this.offset;
      appender.trimEnd = this.offset;
      int max = this.limit;
      if (isEos()) {
        // we can only find the substring at a position
        // until where enough chars are left to go...
        max -= stopLength;
      }
      while (this.offset < max) {
        int codePoint = this.buffer.codePointAt(this.offset);
        if (stopFilter.accept(codePoint)) {
          return appender.getAppended();
        }
        if (codePoint == first || (ignoreCase && (Character.toLowerCase(codePoint) == first))) {
          // found first character
          boolean found = expectRestWithLookahead(stopChars, ignoreCase, appender, false);
          if (found) {
            return appender.getAppended(this.offset);
          }
        }
        if (trim && (codePoint != ' ')) {
          appender.foundNonSpace();
        }
        this.offset++;
      }
      appender.append(this.offset);
      if (!fill()) {
        // substring not found (EOT)
        this.offset = this.limit;
        return appender.toString();
      }
    }
  }

  /**
   * @param length the number of characters for lookahead (match without consuming). May fail if the {@code length}
   *        exceeds the buffer size.
   */
  protected void verifyLookahead(int length) {

    // nothing by default
  }

  /**
   * @param stopChars the stop {@link String}. If {@code ignoreCase} is {@code true} in lower case.
   * @param ignoreCase - {@code true} to (also) compare chars in {@link Character#toLowerCase(char) lower case},
   *        {@code false} otherwise.
   * @param appender an optional lambda to {@link Runnable#run() run} before shifting buffers to append data.
   * @param skip - {@code true} to update buffers and offset such that on success this scanner points after the expected
   *        stop {@link String}, {@code false} otherwise (to not consume any character in any case).
   * @return {@code true} if the stop {@link String} ({@code stopChars}) was found and consumed, {@code false} otherwise
   *         (and no data consumed).
   * @see #readUntil(CharFilter, boolean, String, boolean)
   * @see #skipOver(String, boolean, CharFilter)
   */
  protected abstract boolean expectRestWithLookahead(String stopChars, boolean ignoreCase, Runnable appender,
      boolean skip);

  @Override
  public void require(String expected, boolean ignoreCase) {

    int off = this.offset;
    int lim = this.limit;
    String buf = this.buffer;
    if (!expectUnsafe(expected, ignoreCase)) {
      int length = expected.length();
      StringBuilder error = new StringBuilder(24 + 2 * length);
      error.append("Expecting '");
      error.append(expected);
      error.append("' but found: ");
      int len = lim - off;
      if (len > length) {
        len = length;
      }
      error.append(buf.substring(off, lim));
      // rest after shifting buffers?
      len = length - len;
      if ((len > 0) && (buf != this.buffer)) {
        if (len > this.offset) {
          len = this.offset;
        }
        error.append(this.buffer.substring(0, len));
      }
      throw new IllegalStateException(error.toString());
    }
  }

  @Override
  public boolean expectOne(int expected, boolean warning) {

    if (hasNext() && (this.buffer.codePointAt(this.offset) == expected)) {
      handleCodePoint(expected);
      return true;
    }
    if (warning) {
      addWarning("Expected '" + expected + "'");
    }
    return false;
  }

  @Override
  public boolean expectOne(CharFilter expected) {

    if (!hasNext()) {
      return false;
    }
    if (expected.accept(this.buffer.codePointAt(this.offset))) {
      this.offset++;
      return true;
    }
    return false;
  }

  @Override
  public boolean expectUnsafe(String expected, boolean ignoreCase) {

    int len = expected.length();
    for (int i = 0; i < len; i++) {
      if (!hasNext()) {
        return false;
      }
      int codePoint = this.buffer.codePointAt(this.offset);
      int exp = expected.codePointAt(i);
      if (codePoint != exp) {
        if (!ignoreCase) {
          return false;
        }
        if (Character.toLowerCase(codePoint) != Character.toLowerCase(exp)) {
          return false;
        }
      }
      handleCodePoint(codePoint);
    }
    return true;
  }

  @Override
  public String readLine(boolean trim) {

    if (!hasNext()) {
      return null;
    }
    if (trim) {
      skipWhile(' ');
    }
    Appender appender = newAppender(trim);
    while (true) {
      appender.start = this.offset;
      appender.trimEnd = this.offset;
      while (this.offset < this.limit) {
        int codePoint = this.buffer.codePointAt(this.offset);
        if (codePoint == '\r') {
          int end = this.offset;
          handleCodePoint(codePoint);
          if (this.offset < this.limit) {
            codePoint = this.buffer.codePointAt(this.offset);
            if (codePoint == '\n') {
              handleCodePoint(codePoint);
            }
            return appender.getAppended(end);
          } else { // EOL insanity...
            appender.append(end);
            if (fill()) {
              codePoint = this.buffer.codePointAt(this.offset);
              if (codePoint == '\n') {
                handleCodePoint(codePoint);
              }
            }
            return appender.toString();
          }
        } else if (codePoint == '\n') {
          String result = appender.getAppended();
          handleCodePoint(codePoint);
          return result;
        } else if (codePoint != ' ') {
          appender.foundNonSpace();
        }
        handleCodePoint(codePoint);
      }
      appender.append(this.limit);
      if (!fill()) {
        return appender.toString();
      }
    }
  }

  @Override
  public String readJavaStringLiteral(TextFormatMessageType severity) {

    if (!hasNext()) {
      return null;
    }
    int codePoint = this.buffer.codePointAt(this.offset);
    if (codePoint != '"') {
      return null;
    }
    handleCodePoint(codePoint);
    StringBuilder builder = null;
    while (hasNext()) {
      int start = this.offset;
      while (this.offset < this.limit) {
        codePoint = this.buffer.codePointAt(this.offset);
        handleCodePoint(codePoint);
        if (codePoint == '"') {
          return getAppended(builder, start, this.offset - 1);
        } else if (codePoint == '\\') {
          builder = append(builder, start, this.offset - 1);
          builder = builder(builder);
          parseEscapeSequence(builder, severity);
          start = this.offset;
        }
      }
      builder = append(builder, start, this.offset);
    }
    String value = "";
    if (builder != null) {
      value = builder.toString();
    }
    String message = "Java string literal not terminated";
    addMessage(severity, message);
    return value;
  }

  @Override
  public Character readJavaCharLiteral(TextFormatMessageType severity) {

    if (expectOne('\'')) {
      StringBuilder error = null;
      int cp = next();
      int next = 0;
      if (cp == '\\') {
        cp = next();
        if (cp == 'u') {
          cp = parseUnicodeEscapeSequence(severity);
          if (expectOne('\'')) {
            return Character.valueOf((char) cp);
          }
          error = createUnicodeLiteralError(cp);
        } else {
          next = next();
          if (next == '\'') {
            Character character = CharEscapeHelper.resolveEscape(cp);
            if (character != null) {
              return character;
            }
          } else if (CharFilter.OCTAL_DIGIT.accept(cp) && CharFilter.OCTAL_DIGIT.accept(next)) {
            int value = ((cp - '0') * 8) + (next - '0');
            int last = next();
            if (CharFilter.OCTAL_DIGIT.accept(last) && (value <= 37)) {
              value = (value * 8) + (last - '0');
              last = next();
            }
            if (last == '\'') {
              return Character.valueOf((char) value);
            }
            error = new StringBuilder("'\\");
            error.append(Integer.toString(value, 8));
            error.appendCodePoint(last);
          }
          if (error == null) {
            error = new StringBuilder("'\\");
            error.appendCodePoint(cp);
            error.appendCodePoint(next);
          }
        }
      } else if (expectOne('\'')) {
        return Character.valueOf((char) cp);
      } else {
        error = new StringBuilder("'");
        if (cp != CharStreamScanner.EOS) {
          error.appendCodePoint(cp);
        }
      }
      if (next != '\'') {
        String rest = readUntil(FILTER_SINGLE_QUOTE, true);
        error.append(rest);
        if (expectOne('\'')) {
          error.append('\'');
        }
      }
      String message = "Invalid Java character literal: " + error.toString();
      addMessage(severity, message);
      return Character.valueOf('?');
    }
    return null;

  }

  @Override
  public Number readJavaNumberLiteral() {

    CharScannerNumberParserString numberParser = new CharScannerNumberParserString(CharScannerRadixMode.ALL, true, true,
        "_", true);
    readNumber(numberParser);
    String decimal = numberParser.asString();
    if (decimal == null) {
      return null;
    }
    Number number = null;
    int codePoint = peek();
    if ((codePoint == 'l') || (codePoint == 'L')) {
      number = parseLong(decimal);
    } else if ((codePoint == 'f') || (codePoint == 'F')) {
      number = Float.valueOf(decimal);
    } else if ((codePoint == 'd') || (codePoint == 'D')) {
      number = Double.valueOf(decimal);
    }
    if (number == null) {
      if ((decimal.indexOf('.') >= 0) || (decimal.indexOf('e') >= 0)) {
        number = Double.valueOf(decimal);
      } else {
        number = parseInteger(decimal);
      }
    } else {
      next();
    }
    return number;
  }

  private Long parseLong(String number) {

    int radix = 10;
    int len = number.length();
    int i = 0;
    int cp = number.codePointAt(i++);
    char sign = numberSign(cp);
    if (sign != 0) {
      cp = number.codePointAt(i++);
    }
    if (cp == '0') {
      if (i < len) {
        cp = number.codePointAt(i);
        if (isRadix16(cp)) {
          radix = 16;
          i++;
        } else if (isRadix2(cp)) {
          radix = 2;
          i++;
        } else {
          assert (cp >= '0') && (cp <= '7');
          radix = 8;
        }
        number = number.substring(i);
        if (sign != 0) {
          number = sign + number;
        }
      }
    }
    return Long.valueOf(Long.parseLong(number, radix));
  }

  private Integer parseInteger(String number) {

    int radix = 10;
    int len = number.length();
    int i = 0;
    int cp = number.codePointAt(i++);
    char sign = numberSign(cp);
    if (sign != 0) {
      cp = number.codePointAt(i++);
    }
    if (cp == '0') {
      if (i < len) {
        cp = number.codePointAt(i);
        if (isRadix16(cp)) {
          radix = 16;
          i++;
        } else if (isRadix2(cp)) {
          radix = 2;
          i++;
        } else {
          assert (cp >= '0') && (cp <= '7');
          radix = 8;
        }
        number = number.substring(i);
        if (sign != 0) {
          number = sign + number;
        }
      }
    }
    return Integer.valueOf(Integer.parseInt(number, radix));
  }

  private StringBuilder createUnicodeLiteralError(int codePoint) {

    StringBuilder error;
    error = new StringBuilder("'\\");
    error.append('u');
    String hex = Integer.toString(codePoint, 16);
    int length = hex.length();
    if (length == 1) {
      hex = "000" + hex;
    } else if (length == 2) {
      hex = "00" + hex;
    } else if (length == 3) {
      hex = "0" + hex;
    }
    error.append(hex);
    return error;
  }

  private void parseEscapeSequence(StringBuilder builder, TextFormatMessageType severity) {

    int cp = next();
    if (cp == 'u') { // unicode
      int value = parseUnicodeEscapeSequence(severity);
      builder.appendCodePoint(value);
    } else if (CharFilter.OCTAL_DIGIT.accept(cp)) { // octal C legacy stuff
      int value = cp - '0';
      cp = peek();
      if (CharFilter.OCTAL_DIGIT.accept(cp)) {
        next();
        value = (8 * value) + (cp - '0');
        if (value <= 31) {
          cp = peek();
          if (CharFilter.OCTAL_DIGIT.accept(cp)) {
            next();
            value = (8 * value) + (cp - '0');
          }
        }
      }
      builder.appendCodePoint(value);
    } else {
      Character resolved = CharEscapeHelper.resolveEscape(cp);
      if (resolved == null) {
        StringBuilder message = new StringBuilder("Illegal escape sequence \\");
        message.appendCodePoint(cp);
        addMessage(severity, message.toString());
        builder.appendCodePoint(cp);
      } else {
        builder.append(resolved.charValue());
      }
    }
  }

  private int parseUnicodeEscapeSequence(TextFormatMessageType severity) {

    skipWhile('u');
    int i = 0;
    int value = 0;
    int radix = 16;
    while (i < 4) {
      int digit = readDigit(radix);
      if (digit < 0) {
        String hexString;
        if (i == 0) {
          hexString = "";
        } else {
          hexString = Integer.toString(value, radix);
          while (hexString.length() < i) {
            hexString = "0" + hexString;
          }
        }
        String message = "Illegal escape sequence \\u" + hexString;
        addMessage(severity, message);
        return '?';
      }
      value = (value * radix) + digit;
      i++;
    }
    return value;
  }

  @SuppressWarnings("null")
  @Override
  public String read(int count) {

    if (!hasNext() || (count == 0)) {
      return "";
    }
    StringBuilder builder = null;
    int remain = count;
    while (remain > 0) {
      int len = this.limit - this.offset;
      if (len >= remain) {
        if (builder == null) {
          String string = this.buffer.substring(this.offset, this.offset + remain);
          setOffset(this.offset + remain);
          return string;
        }
        len = remain;
      }
      builder = builder(builder);
      builder.append(this.buffer, this.offset, this.offset + len);
      setOffset(this.offset + len);
      remain -= len;
      if ((remain > 0) && !fill()) {
        break;
      }
    }
    return builder.toString();
  }

  @Override
  public void read(int count, StringBuilder builder) {

    if (hasNext() && (count > 0)) {
      int remain = count;
      while (remain > 0) {
        int len = this.limit - this.offset;
        if (len > remain) {
          len = remain;
        }
        builder.append(this.buffer, this.offset, len);
        setOffset(this.offset + len);
        remain -= len;
        if ((remain > 0) && !fill()) {
          break;
        }
      }
    }
  }

  @Override
  public int readDigit(int radix) {

    int result = -1;
    if (hasNext()) {
      int codePoint = this.buffer.codePointAt(this.offset);
      int value = Character.digit(codePoint, radix);
      if ((value >= 0) && (value < radix)) {
        result = value;
        handleCodePoint(codePoint);
      }
    }
    return result;
  }

  private char numberExponent(int cp, int radix) {

    if (radix == 16) {
      if (cp == 'p') {
        return 'p';
      } else if (cp == 'P') {
        return 'P';
      }
    } else if (cp == 'e') {
      return 'e';
    } else if (cp == 'E') {
      return 'E';
    }
    return 0;
  }

  @Override
  public void readNumber(CharScannerNumberParser numberParser) {

    int skipCount = 1;
    int cp = peek();
    char sign = numberSign(cp);
    if ((sign != 0) && numberParser.sign(sign)) {
      cp = peek(skipCount);
      skipCount++;
    }
    int radix = 10;
    if (cp == '0') { // radix?
      if (skipCount == 2) {
        next(); // consume sign as we have found a reasonable number
        skipCount--;
      }
      assert (skipCount == 1);
      int radixChar = peek(skipCount); // peek character after '0'
      int rc = radixChar;
      int r = 10;
      if (isRadix16(radixChar)) {
        r = 16;
      } else if (isRadix2(radixChar)) {
        r = 2;
      } else if (isDigit(radixChar)) { // logically 0-7, see explanation in comment below
        rc = '0';
        r = 8;
      } else {
        r = 0;
      }
      radix = numberParser.radix(r, (char) rc);
      if (radix > 0) {
        if (r == 8) {
          next();
          cp = radixChar;
        } else {
          skip(2);
          cp = peek();
        }
      }
      if (radix < 10) {
        // we use at least radix 10 to to read and consume digits for NumberFormatException (NFE)
        // else we might read "0b1012;078" as "0b101" and leave "2;078" remaining while we want NFE for "0b1012"
        // the actual radix is handled by numberParser
        radix = 10;
      }
    }
    boolean todo = true;
    while (todo) {
      boolean next = false;
      boolean peek = true;
      int digit = Character.digit(cp, radix);
      if (digit >= 0) {
        next = numberParser.digit(digit, (char) cp);
      } else if (cp == '.') {
        next = numberParser.dot();
        if (!next) {
          todo = false;
        }
      } else {
        char e = numberExponent(cp, radix);
        if (e != 0) {
          cp = peek(skipCount);
          char eSign = numberSign(cp);
          if (eSign != 0) {
            skipCount++;
          }
          next = numberParser.exponent(e, eSign);
          if (next && (eSign == 0)) {
            peek = false;
          }
        } else {
          String special = numberParser.special(cp);
          if (special != null) {
            if (expect(special, false, false, skipCount - 1)) {
              skipCount = 0;
              numberParser.special(special);
              next = false; // we accept but have already consumed, no next
            } else {
              todo = false;
            }
          } else {
            todo = false;
          }
        }
      }
      if (next) {
        if (skipCount > 1) {
          skip(skipCount);
          skipCount = 1;
        } else {
          next();
        }
      }
      if (peek && todo) {
        cp = peek();
        todo = (cp != 0);
      }
    }

  }

  private boolean isRadix2(int cp) {

    return (cp == 'b') || (cp == 'B');
  }

  private boolean isRadix16(int cp) {

    return (cp == 'x') || (cp == 'X');
  }

  private boolean isDigit(int cp) {

    return (cp >= '0') && (cp <= '9');
  }

  private char numberSign(int cp) {

    if (cp == '+') {
      return '+';
    } else if (cp == '-') {
      return '-';
    }
    return 0;
  }

  @Override
  public Double readDouble(CharScannerRadixHandler radixMode) {

    CharScannerNumberParserString numberParser = new CharScannerNumberParserString(radixMode, true, true, "_", true);
    readNumber(numberParser);
    return numberParser.asDouble();
  }

  @Override
  public Float readFloat(CharScannerRadixHandler radixMode) {

    CharScannerNumberParserString numberParser = new CharScannerNumberParserString(radixMode, true, true, "_", true);
    readNumber(numberParser);
    return numberParser.asFloat();
  }

  @Override
  public Long readLong(CharScannerRadixHandler radixMode) {

    CharScannerNumberParserLang numberParser = new CharScannerNumberParserLang(radixMode, NumberType.LONG);
    readNumber(numberParser);
    return numberParser.asLong();
  }

  @Override
  public Integer readInteger(CharScannerRadixHandler radixMode) throws NumberFormatException {

    CharScannerNumberParserLang numberParser = new CharScannerNumberParserLang(radixMode, NumberType.INTEGER);
    readNumber(numberParser);
    return numberParser.asInteger();
  }

  @Override
  public long readUnsignedLong(int maxDigits) throws NumberFormatException {

    if (maxDigits <= 0) {
      throw new IllegalArgumentException(Integer.toString(maxDigits));
    }
    StringBuilder builder = null;
    if (this.offset >= this.limit) {
      fill();
    }
    int remain = maxDigits;
    while (true) {
      int start = this.offset;
      int end = this.offset + remain;
      if (end > this.limit) {
        end = this.limit;
      }
      int codePoint = 0;
      while (this.offset < end) {
        codePoint = this.buffer.codePointAt(this.offset);
        if (!isDigit(codePoint)) {
          break;
        }
        this.offset++;
      }
      int len = this.offset - start;
      remain -= len;
      if ((this.offset < end) || (remain == 0) || (start == this.limit)) {
        if ((len == 0) && (builder == null)) {
          throw new IllegalStateException("Invalid character for long number: " + codePoint);
        }
        String number = getAppended(builder, start, this.offset);
        return Long.parseLong(number);
      } else {
        builder = append(builder, start, this.offset);
        fill();
      }
    }
  }

  @Override
  public int skip(int count) {

    if ((count == 0) || !hasNext()) {
      return 0;
    }
    int skipped = 0;
    int remain = count;
    while (remain > 0) {
      int len = this.limit - this.offset;
      if (len >= remain) {
        setOffset(this.offset + remain);
        return count;
      } else {
        setOffset(this.limit);
        skipped = skipped + len;
        remain = remain - len;
      }
      if (!fill()) {
        break;
      }
    }
    return skipped;
  }

  @Override
  public int skipNewLine() {

    int skip = 0;
    if (hasNext()) {
      int codePointAt = this.buffer.codePointAt(this.offset);
      if (codePointAt == '\n') {
        skip = 1;
      } else if (codePointAt == '\r') {
        if (((this.offset + 1) < this.limit) && (this.buffer.codePointAt(this.offset + 1) == '\n')) {
          skip = 2;
        } else if (peek(1) == '\n') {
          skip(2);
          return 2;
        }
      }
    }
    if (skip > 0) {
      this.offset = this.offset + skip;
      this.line++;
      this.column = 1;
    }
    return skip;
  }

  @Override
  public boolean skipUntil(int stop) {

    while (hasNext()) {
      while (this.offset < this.limit) {
        if (this.buffer.codePointAt(this.offset++) == stop) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean skipUntil(int stop, int escape) {

    boolean escapeActive = false;
    while (hasNext()) {
      while (this.offset < this.limit) {
        int codePoint = this.buffer.codePointAt(this.offset++);
        if (codePoint == escape) {
          escapeActive = !escapeActive;
        } else {
          if ((codePoint == stop) && (!escapeActive)) {
            return true;
          }
          escapeActive = false;
        }
      }
    }
    return false;
  }

  @Override
  public int skipWhile(int c) {

    int count = 0;
    while (hasNext()) {
      int start = this.offset;
      while (this.offset < this.limit) {
        if (this.buffer.codePointAt(this.offset) != c) {
          return count + (this.offset - start);
        }
        handleCodePoint(c);
      }
      count += (this.offset - start);
    }
    return count;
  }

  @Override
  public int skipWhile(CharFilter filter, int max) {

    if (max < 0) {
      throw new IllegalArgumentException("Max must NOT be negative: " + max);
    }
    int remain = max;
    while (hasNext()) {
      int start = this.offset;
      int end = start + remain;
      if (end < 0) { // overflow?
        end = remain;
      }
      if (end > this.limit) {
        end = this.limit;
      }
      boolean notAccepted = false;
      while (this.offset < end) {
        int cp = this.buffer.codePointAt(this.offset);
        if (!filter.accept(cp)) {
          notAccepted = true;
          break;
        }
        handleCodePoint(cp);
      }
      int len = this.offset - start;
      remain -= len;
      if (notAccepted || (remain == 0)) {
        break;
      }
    }
    return (max - remain);
  }

  @Override
  public boolean skipOver(String substring, boolean ignoreCase, CharFilter stopFilter) {

    int subLength = substring.length();
    if (subLength == 0) {
      return true;
    }
    verifyLookahead(subLength);
    if (!hasNext()) {
      return false;
    }
    String subChars;
    if (ignoreCase) {
      subChars = CaseHelper.toLowerCase(substring);
    } else {
      subChars = substring;
    }
    int first = subChars.codePointAt(0);
    while (true) {
      int max = this.limit;
      if (isEos()) {
        // we can only find the substring at a position
        // until where enough chars are left to go...
        max -= subLength;
      }
      while (this.offset <= max) {
        int cp = this.buffer.codePointAt(this.offset);
        if ((stopFilter != null) && stopFilter.accept(cp)) {
          return false;
        }
        if (cp == first || (ignoreCase && (Character.toLowerCase(cp) == first))) {
          // found first character
          boolean found = expectRestWithLookahead(subChars, ignoreCase, null, true);
          if (found) {
            return true;
          }
          next();
        } else {
          handleCodePoint(cp);
        }
      }
      if (!fill()) {
        // TODO reset text position
        // substring not found (EOT)
        this.offset = this.limit;
        return false;
      }
    }
  }

  @Override
  public String readWhile(CharFilter filter, int min, int max) {

    if (max < 0) {
      throw new IllegalArgumentException("Max must NOT be negative: " + max);
    }
    if (max < min) {
      throw new IllegalArgumentException("Min (" + min + ") must be less or requal to max (" + max + ")");
    }
    StringBuilder builder = null;
    if (this.offset >= this.limit) {
      fill();
    }
    int remain = max;
    while (true) {
      int start = this.offset;
      int end = start + remain;
      if (end < 0) { // overflow?
        end = remain;
      }
      if (end > this.limit) {
        end = this.limit;
      }
      while (this.offset < end) {
        int cp = this.buffer.codePointAt(this.offset);
        if (!filter.accept(cp)) {
          return requireMin(getAppended(builder, start, this.offset), min, filter);
        }
        handleCodePoint(cp);
      }
      int len = this.offset - start;
      remain -= len;
      builder = append(builder, start, this.offset);
      if ((remain == 0) || !fill()) {
        return requireMin(eot(builder, true), min, filter);
      }
    }
  }

  /**
   * @param actual the actual number of characters.
   * @param min the minimum number of characters required.
   * @param filter the {@link CharFilter} that was used.
   */
  protected void requireMin(int actual, int min, CharFilter filter) {

    if (actual < min) {
      throw new IllegalStateException(
          "Required at least " + min + " character(s) (" + filter.getDescription() + ") but found only " + actual);
    }
  }

  private String requireMin(String result, int min, CharFilter filter) {

    requireMin(result.length(), min, filter);
    return result;
  }

  /**
   * @return the {@link String} with the characters that have already been parsed but are still available in the
   *         underlying buffer. May be used for debugging or error messages.
   */
  @Override
  public String getBufferParsed() {

    return this.buffer.substring(0, this.offset);
  }

  @Override
  public String getBufferToParse() {

    if (this.offset < this.limit) {
      return this.buffer.substring(this.offset, this.limit);
    } else {
      return "";
    }
  }

  @Override
  public String toString() {

    return getBufferParsed() + "\n$^$\n" + getBufferToParse();
  }

  private class CharScannerSyntaxState {

    private final CharScannerSyntax syntax;

    private final CharFilter filter;

    private final int quoteStart;

    private final int quoteEnd;

    private final int escape;

    private final int quoteEscape;

    private final boolean quoteEscapeLazy;

    private final int altQuoteStart;

    private final int altQuoteEnd;

    private final int altQuoteEscape;

    private final boolean altQuoteEscapeLazy;

    private final int entityStart;

    private final int entityEnd;

    private int start;

    private boolean escapeActive;

    private int activeQuoteEnd;

    private int activeQuoteEscape;

    private int activeQuoteLazyEnd;

    private boolean activeQuoteLazy;

    private int activeEntityEnd;

    private StringBuilder builder;

    private StringBuilder entityBuilder;

    private boolean done;

    private boolean quoteEscapeActive;

    private CharScannerSyntaxState(CharScannerSyntax syntax, CharFilter filter) {

      super();
      this.syntax = syntax;
      this.filter = filter;
      // copy to avoid method calls and boost performance
      this.escape = syntax.getEscape();
      this.quoteStart = syntax.getQuoteStart();
      this.quoteEnd = syntax.getQuoteEnd();
      this.quoteEscape = syntax.getQuoteEscape();
      this.quoteEscapeLazy = syntax.isQuoteEscapeLazy();
      this.altQuoteStart = syntax.getAltQuoteStart();
      this.altQuoteEnd = syntax.getAltQuoteEnd();
      this.altQuoteEscape = syntax.getAltQuoteEscape();
      this.altQuoteEscapeLazy = syntax.isAltQuoteEscapeLazy();
      this.entityStart = syntax.getEntityStart();
      this.entityEnd = syntax.getEntityEnd();
      // init state
      this.builder = builder(null);
      this.start = AbstractCharStreamScanner.this.offset;
      this.escapeActive = false;
      this.activeQuoteEnd = 0;
      this.activeQuoteEscape = 0;
      this.activeQuoteLazy = false;
      this.activeQuoteLazyEnd = 0;
      this.activeEntityEnd = 0;
      this.quoteEscapeActive = false;
      this.done = false;
    }

    public StringBuilder getEntityBuilder() {

      if (this.entityBuilder == null) {
        this.entityBuilder = new StringBuilder(4);
      }
      return this.entityBuilder;
    }

    private void parse(int codePoint) {

      boolean append = false;
      if (this.escapeActive) {
        // current character c was escaped
        // it will be taken as is on the next append
        this.escapeActive = false;
      } else if (this.activeQuoteEnd != 0) {
        // parse quote
        if ((this.activeQuoteLazyEnd != 0) && (codePoint == this.activeQuoteLazyEnd)) {
          this.activeQuoteEnd = 0;
          this.builder.appendCodePoint(codePoint); // quote (was escaped lazily)
          this.start = AbstractCharStreamScanner.this.offset + 1;
        } else if (this.quoteEscapeActive) {
          this.quoteEscapeActive = false;
          if (codePoint == this.activeQuoteEnd) {
            this.builder.appendCodePoint(codePoint); // quote (was escaped)
            this.start = AbstractCharStreamScanner.this.offset + 1;
          } else if (this.activeQuoteEscape == this.activeQuoteEnd) {
            // quotation done
            this.activeQuoteEnd = 0;
            this.start = AbstractCharStreamScanner.this.offset;
          }
        } else if ((codePoint == this.activeQuoteEscape)
        // && (!this.activeQuoteLazy || (this.activeQuoteEscape != this.activeQuoteEnd))
        ) {
          // escape in quote
          append = true;
          this.quoteEscapeActive = true;
        } else if (codePoint == this.activeQuoteEnd) {
          // quotation done
          this.activeQuoteEnd = 0;
          append = true;
        }
        this.activeQuoteLazyEnd = 0;
      } else if (this.activeEntityEnd != 0) {
        // parse entity
        if (codePoint == this.activeEntityEnd) {
          // entity end detected...
          this.activeEntityEnd = 0;
          int len = AbstractCharStreamScanner.this.offset - this.start;
          String entity;
          if (this.entityBuilder == null) {
            entity = AbstractCharStreamScanner.this.buffer.substring(this.start, AbstractCharStreamScanner.this.offset);
          } else {
            this.entityBuilder.append(AbstractCharStreamScanner.this.buffer, this.start, len);
            entity = this.entityBuilder.toString();
            this.entityBuilder = null;
          }
          this.builder.append(this.syntax.resolveEntity(entity));
          this.start = AbstractCharStreamScanner.this.offset + 1;
        }
      } else if (this.filter.accept(codePoint)) {
        append = true;
        this.done = true;
      } else if (codePoint == this.escape) {
        append = true;
        this.escapeActive = true;
      } else if (codePoint == this.entityStart) {
        this.activeEntityEnd = this.entityEnd;
        append = true;
      } else {
        if (codePoint == this.quoteStart) {
          this.activeQuoteEnd = this.quoteEnd;
          this.activeQuoteEscape = this.quoteEscape;
          this.activeQuoteLazy = this.quoteEscapeLazy;
        } else if (codePoint == this.altQuoteStart) {
          this.activeQuoteEnd = this.altQuoteEnd;
          this.activeQuoteEscape = this.altQuoteEscape;
          this.activeQuoteLazy = this.altQuoteEscapeLazy;
        }
        if (this.activeQuoteEnd != 0) {
          this.quoteEscapeActive = false;
          append = true;
          if (this.activeQuoteLazy && (this.activeQuoteEnd == this.activeQuoteEscape)
              && (codePoint == this.activeQuoteEscape)) {
            this.activeQuoteLazyEnd = this.activeQuoteEnd;
          }
        }
      }
      if (append) {
        if (AbstractCharStreamScanner.this.offset > this.start) {
          this.builder.append(AbstractCharStreamScanner.this.buffer, this.start, AbstractCharStreamScanner.this.offset);
        }
        this.start = AbstractCharStreamScanner.this.offset + 1;
      }
    }
  }

  private Appender newAppender(boolean trim) {

    if (trim) {
      return new TrimmingAppender();
    } else {
      return new PlainAppender();
    }
  }

  private abstract class Appender implements Runnable {

    protected StringBuilder builder;

    protected int start;

    protected int trimEnd;

    protected abstract void append(int end);

    protected abstract String getAppended(int end);

    protected abstract String getAppended();

    protected void foundNonSpace() {

      // nothing by default, can be overridden with custom handling...
    }

    @Override
    public String toString() {

      if (this.builder == null) {
        return "";
      }
      return this.builder.toString();
    }
  }

  private class PlainAppender extends Appender {

    @Override
    protected void append(int end) {

      this.builder = AbstractCharStreamScanner.this.append(this.builder, this.start, end);
    }

    @Override
    protected String getAppended(int end) {

      return AbstractCharStreamScanner.this.getAppended(this.builder, this.start, end);
    }

    @Override
    protected String getAppended() {

      return AbstractCharStreamScanner.this.getAppended(this.builder, this.start,
          AbstractCharStreamScanner.this.offset);
    }

    @Override
    public void run() {

      this.builder = AbstractCharStreamScanner.this.append(this.builder, this.start,
          AbstractCharStreamScanner.this.offset);
    }
  }

  private class TrimmingAppender extends Appender {

    private int spaceCount;

    @Override
    protected void foundNonSpace() {

      this.trimEnd = AbstractCharStreamScanner.this.offset + 1;
      if (this.spaceCount > 0) {
        this.builder = builder(this.builder);
        while (this.spaceCount > 0) {
          this.builder.append(' ');
          this.spaceCount--;
        }
      }
    }

    @Override
    protected void append(int end) {

      this.spaceCount += end - this.trimEnd;
      this.builder = AbstractCharStreamScanner.this.append(this.builder, this.start, this.trimEnd);
    }

    @Override
    protected String getAppended(int end) {

      return AbstractCharStreamScanner.this.getAppended(this.builder, this.start, this.trimEnd);
    }

    @Override
    protected String getAppended() {

      return AbstractCharStreamScanner.this.getAppended(this.builder, this.start, this.trimEnd);
    }

    @Override
    public void run() {

      this.builder = AbstractCharStreamScanner.this.append(this.builder, this.start, this.trimEnd);
    }
  }

}
