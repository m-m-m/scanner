/* Copyright (c) The m-m-m Team, Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0 */
package io.github.mmm.scanner;

import java.util.Locale;
import java.util.Objects;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import io.github.mmm.base.filter.CharFilter;
import io.github.mmm.base.number.NumberType;
import io.github.mmm.scanner.number.CharScannerNumberParserLang;
import io.github.mmm.scanner.number.CharScannerRadixMode;

/**
 * This is the abstract test for implementations of {@link CharStreamScanner}.
 */
@SuppressWarnings("all")
public abstract class AbstractCharStreamScannerTest extends Assertions {

  protected static final SimpleTextFormatMessageHandler HANDLER = SimpleTextFormatMessageHandler
      .ofThrowErrorsNoLogging();

  protected CharStreamScanner scanner(String string) {

    return scanner(string, false);
  }

  protected CharStreamScanner scanner(String string, boolean lookahead) {

    int capacity = 1;
    if (lookahead) {
      capacity = 32;
    }
    return scanner(string, capacity);
  }

  protected abstract CharStreamScanner scanner(String string, int capacity);

  @Test
  void testSkipWhile() {

    // arrange
    String string = "abc def  ghi";
    // act
    CharStreamScanner scanner = scanner(string);
    int skipCount = scanner.skipWhile(' ');
    // assert
    assertThat(skipCount).isEqualTo(0);
    // act again
    String read = scanner.read(3);
    // assert
    assertThat(read).isEqualTo("abc");
    // act again
    skipCount = scanner.skipWhile(' ');
    // assert
    assertThat(skipCount).isEqualTo(1);
    // act again
    read = scanner.read(3);
    // assert
    assertThat(read).isEqualTo("def");
    // act again
    skipCount = scanner.skipWhile(' ');
    // assert
    assertThat(skipCount).isEqualTo(2);
    // act again
    read = scanner.read(3);
    // assert
    assertThat(read).isEqualTo("ghi");
    assertThat(scanner.hasNext()).isFalse();
  }

  @Test
  void testSkipWhileFilter() {

    // arrange
    String string = "abc def \tghi\t\t \t";
    CharFilter filter = (c) -> (c == ' ' || c == '\t');
    // act
    CharStreamScanner scanner = scanner(string);
    assertThat(scanner.getPosition()).isEqualTo(0);
    int skipCount = scanner.skipWhile(filter);
    // assert
    assertThat(skipCount).isEqualTo(0);
    // act again
    String read = scanner.read(3);
    // assert
    assertThat(read).isEqualTo("abc");
    assertThat(scanner.getPosition()).isEqualTo(3);
    // act again
    skipCount = scanner.skipWhile(filter);
    // assert
    assertThat(skipCount).isEqualTo(1);
    assertThat(scanner.getPosition()).isEqualTo(4);
    // act again
    read = scanner.read(3);
    // assert
    assertThat(read).isEqualTo("def");
    assertThat(scanner.getPosition()).isEqualTo(7);
    // act again
    skipCount = scanner.skipWhile(filter);
    // assert
    assertThat(skipCount).isEqualTo(2);
    assertThat(scanner.getPosition()).isEqualTo(9);
    // act again
    read = scanner.read(3);
    // assert
    assertThat(read).isEqualTo("ghi");
    assertThat(scanner.getPosition()).isEqualTo(12);
    // act again
    skipCount = scanner.skipWhile(filter, 3);
    // assert
    assertThat(skipCount).isEqualTo(3);
    assertThat(scanner.getPosition()).isEqualTo(15);
    // act again
    skipCount = scanner.skipWhile(filter, 5);
    // assert
    assertThat(skipCount).isEqualTo(1);
    assertThat(scanner.hasNext()).isFalse();
    assertThat(scanner.getPosition()).isEqualTo(16);
  }

  @Test
  void testSkipOver() {

    // arrange
    String substring = "xYz";
    String string = "xxYzFOOxYztheend";
    // act
    CharStreamScanner scanner = scanner(string, true);
    // assert
    checkSkipOver(scanner, substring, false);
    // act again
    scanner = scanner(string.toLowerCase(), true);
    // assert
    checkSkipOver(scanner, substring, true);
  }

  private void checkSkipOver(CharStreamScanner scanner, String substring, boolean ignoreCase) {

    assertThat(scanner.skipOver(substring, ignoreCase)).isTrue();
    assertThat(scanner.expectUnsafe("FOO", ignoreCase)).isTrue();
    assertThat(scanner.skipOver(substring, ignoreCase)).isTrue();
    String rest = scanner.read(Integer.MAX_VALUE);
    if (ignoreCase) {
      rest = rest.toLowerCase();
    }
    assertThat(rest).isEqualTo("theend");
  }

  @Test
  void testSkipUntil() {

    // unescaped
    CharStreamScanner scanner = scanner("string");
    assertThat(scanner.skipUntil('n')).isTrue();
    assertThat(scanner.next()).isEqualTo('g');
    assertThat(scanner.hasNext()).isFalse();

    // escaped
    String end = "12345";
    scanner = scanner("\"Quotet text with \\\" inside!\"" + end);
    assertThat(scanner.next()).isEqualTo('\"');
    assertThat(scanner.skipUntil('\"', '\\')).isTrue();
    assertThat(scanner.expectUnsafe(end, false)).isTrue();
  }

  /**
   * Tests {@link CharStreamScanner#readUntil(char, boolean)}.
   */
  @Test
  void testReadUntil() {

    // arrange
    String string = "string";
    CharStreamScanner scanner;
    // act (not escaped)
    scanner = scanner(string);
    // assert
    assertThat(scanner.readUntil('n', false)).isEqualTo("stri");
    assertThat(scanner.next()).isEqualTo('g');
    assertThat(scanner.hasNext()).isFalse();

    // act again (no EOF)
    scanner = scanner(string);
    // assert
    assertThat(scanner.readUntil('x', false)).isNull();
    assertThat(scanner.hasNext()).isFalse();

    // act again (EOF)
    scanner = scanner(string);
    // assert
    assertThat(scanner.readUntil('x', true)).isEqualTo(string);
    assertThat(scanner.hasNext()).isFalse();

  }

  /**
   * Tests {@link CharStreamScanner#readUntil(char, boolean, char)}.
   */
  @Test
  void testReadUntilWithEscape() {

    CharStreamScanner scanner;
    // act (test with different escape and stop char)
    scanner = scanner("bla\"Quotet text with \\\" and \\\\ inside!\"bla");
    // assert
    assertThat(scanner.readUntil('"', false)).isEqualTo("bla");
    assertThat(scanner.readUntil('"', false, '\\')).isEqualTo("Quotet text with \" and \\ inside!");
    assertThat(scanner.readUntil('\0', true)).isEqualTo("bla");

    // act again (test with same escape and stop char)
    scanner = scanner("bla\"Quotet text with \"\" and \\ inside!\"bla");
    // assert
    assertThat(scanner.readUntil('"', false)).isEqualTo("bla");
    assertThat(scanner.readUntil('"', false, '"')).isEqualTo("Quotet text with \" and \\ inside!");
    assertThat(scanner.readUntil('\0', true)).isEqualTo("bla");

  }

  /**
   * Tests {@link CharStreamScanner#readUntil(CharFilter, boolean, String, boolean, boolean)}.
   */
  @Test
  void testReadUntilWithStopString() {

    // arrange
    CharFilter filter = CharFilter.NEWLINE;
    String string = "/* comment */\n" + //
        "  /*\n" + //
        "   *   Line  1.    \n" + //
        "   * Line2  \n" + //
        "   */";
    // act
    CharStreamScanner scanner = scanner(string, true);
    // assert
    assertThat(scanner.expectUnsafe("/*")).isTrue();
    assertThat(scanner.readUntil(filter, false, "*/", false, true)).isEqualTo("comment");
    assertThat(scanner.expectUnsafe("*/")).isTrue();
    assertThat(scanner.readLine()).isEmpty();
    assertThat(scanner.readUntil(filter, false, "/*", false, true)).isEmpty();
    assertThat(scanner.expectUnsafe("/*")).isTrue();
    assertThat(scanner.skipUntil('*')).isTrue();
    assertThat(scanner.readUntil(filter, false, "*/", false, true)).isEqualTo("Line  1.");
    assertThat(scanner.skipUntil('*')).isTrue();
    assertThat(scanner.readUntil(filter, false, "*/", false, true)).isEqualTo("Line2");
    assertThat(scanner.readLine()).isEmpty();
    assertThat(scanner.readUntil(filter, false, "*/", false, false)).isEqualTo("   ");
  }

  /**
   * Tests {@link CharSequenceScanner#readUntil(char, boolean, CharScannerSyntax)} with quote and backslash as escape
   * char.
   */
  @Test
  void testReadUntilWithSyntaxBackslashEscaped() {

    // arrange
    String end = "12345";
    String string = "\"Quotet text with \\\" inside!\"" + end;
    CharScannerSyntaxBean syntax = new CharScannerSyntaxBean();
    syntax.setEscape('\\');
    // act
    CharStreamScanner scanner = scanner(string);
    // assert
    assertThat(scanner.next()).isEqualTo('\"');
    assertThat(scanner.getPosition()).isEqualTo(1);
    assertThat(scanner.getColumn()).isEqualTo(2);
    assertThat(scanner.getLine()).isEqualTo(1);
    String result = scanner.readUntil('\"', false, syntax);
    assertThat(result).isEqualTo("Quotet text with \" inside!");
    assertThat(scanner.getPosition()).isEqualTo(29);
    assertThat(scanner.getColumn()).isEqualTo(30);
    assertThat(scanner.getLine()).isEqualTo(1);
    assertThat(scanner.expectUnsafe(end, false)).isTrue();
    assertThat(scanner.hasNext()).isFalse();
    assertThat(scanner.getPosition()).isEqualTo(34);
    assertThat(scanner.getColumn()).isEqualTo(35);
    assertThat(scanner.getLine()).isEqualTo(1);
  }

  /**
   * Tests {@link CharSequenceScanner#readUntil(char, boolean, CharScannerSyntax)} with all quote settings to single
   * quote.
   */
  @Test
  void testReadUntilWithSyntaxSingleQuotes() {

    // arrange
    CharScannerSyntaxBean syntax = new CharScannerSyntaxBean();
    syntax.setEscape('\\');
    syntax.setQuote('\'');
    syntax.setQuoteEscape('\'');
    // act + assert
    check('x', true, syntax, "''a''''b'''c''d' x", "a'b'c'd ");
  }

  /**
   * Tests {@link CharSequenceScanner#readUntil(char, boolean, CharScannerSyntax)} with all quote settings to single
   * quote and {@link CharScannerSyntax#isQuoteEscapeLazy() lazy quote}.
   */
  @Test
  void testReadUntilWithSyntaxSingleQuotesLazy() {

    // arrange
    CharScannerSyntaxBean syntax = new CharScannerSyntaxBean();
    syntax.setEscape('\\');
    syntax.setQuote('\'');
    syntax.setQuoteEscape('\'');
    syntax.setQuoteEscapeLazy(true);
    // act + assert
    check('x', true, syntax, "''a''''b'''c'dx", "'a''b'cd");
  }

  /**
   * Tests {@link CharSequenceScanner#readUntil(char, boolean, CharScannerSyntax)} with all alt-quote settings to double
   * quote.
   */
  @Test
  void testReadUntilWithSyntaxAltDobuleQuotes() {

    // arrange
    CharScannerSyntaxBean syntax = new CharScannerSyntaxBean();
    syntax.setEscape('\\');
    syntax.setQuote('\'');
    syntax.setQuoteEscape('\'');
    syntax.setQuoteEscapeLazy(true);
    syntax.setAltQuote('"');
    syntax.setAltQuoteEscape('"');
    // act + assert
    check('x', true, syntax, "\"\"a\"\"\"\"b\"\"\"c\"dx", "a\"b\"cd");
  }

  /**
   * Tests {@link CharSequenceScanner#readUntil(char, boolean, CharScannerSyntax)} with all alt-quote settings to double
   * quote and {@link CharScannerSyntax#isAltQuoteEscapeLazy() lazy alt-quote}.
   */
  @Test
  void testReadUntilWithSyntaxAltDobuleQuotesLazy() {

    // arrange
    CharScannerSyntaxBean syntax = new CharScannerSyntaxBean();
    syntax.setEscape('\\');
    syntax.setQuote('\'');
    syntax.setQuoteEscape('\'');
    syntax.setQuoteEscapeLazy(true);
    syntax.setAltQuote('"');
    syntax.setAltQuoteEscape('"');
    syntax.setAltQuoteEscapeLazy(true);
    // act + assert
    check('x', true, syntax, "\"\"a\"\"\"\"b\"\"\"c\"dx", "\"a\"\"b\"cd");
  }

  /**
   * Test of {@link CharStreamScanner#readUntil(CharFilter, boolean)} reading until stop char was hit.
   */
  @Test
  void testReadUntilWithCharFilter() {

    // arrange
    String string = " blabla_$";
    CharStreamScanner scanner;
    scanner = scanner(string);
    // act
    String result = scanner.readUntil(cp -> cp == '$', true);
    // assert
    assertThat(result).isEqualTo(" blabla_");
  }

  /**
   * Test of {@link CharStreamScanner#readUntil(CharFilter, boolean)} reading until EOT if no stop char was hit but
   * {@code acceptEnd} was {@code true}.
   */
  @Test
  void testReadUntilWithCharFilterEot() {

    // arrange
    String string = " blabla_$";
    CharStreamScanner scanner;
    scanner = scanner(string);
    // act
    String result = scanner.readUntil(CharFilter.NEWLINE, true);
    // assert
    assertThat(result).isEqualTo(string);
  }

  /**
   * Test of {@link CharStreamScanner#readUntil(CharFilter, boolean)} returning {@code null} if no stop char was hit but
   * {@code acceptEnd} was {@code false}.
   */
  @Test
  void testReadUntilWithCharFilterNoEot() {

    // arrange
    String string = " blabla_$";
    CharStreamScanner scanner;
    scanner = scanner(string);
    // act
    String result = scanner.readUntil(CharFilter.NEWLINE, false);
    // assert
    assertThat(result).isNull();
  }

  /**
   * Test of {@link CharStreamScanner#readUntil(CharFilter, boolean, String, boolean ignoreCase, boolean)} with buffer
   * limit overflow.
   */
  @Test
  void testReadUntilWithCharFilterAndStopString() {

    // arrange
    String string = " blabla_$\n";
    CharStreamScanner scanner;
    // act
    scanner = scanner(string);
    // assert
    assertThat(scanner.readUntil(CharFilter.NEWLINE, true, "$", false, true)).isEqualTo("blabla_");
    // act again
    scanner = scanner(string, true);
    // assert
    assertThat(scanner.readUntil(CharFilter.NEWLINE, true, "_$", false, true)).isEqualTo("blabla");
  }

  @Test
  void testReadLong() {

    new NumberScannerLong(s -> scanner(s, 2)).test();
  }

  @Test
  void testReadInteger() {

    new NumberScannerInteger(s -> scanner(s, 2)).test();
  }

  @Test
  void testReadDouble() {

    new NumberScannerDouble(s -> scanner(s, 9)).test();
  }

  @Test
  void testReadDoubles() {

    // arrange
    String string = "123456789-987654321+0.123e-10xyz";
    // act
    CharStreamScanner scanner = scanner(string, 4);
    // assert
    assertThat(scanner.readDouble()).isEqualTo(123456789d);
    assertThat(scanner.readDouble()).isEqualTo(-987654321d);
    assertThat(scanner.readDouble()).isEqualTo(+0.123e-10);
    assertThat(scanner.readDouble()).isNull();
    // scanner.require("xyz");
    assertThat(scanner.read(Integer.MAX_VALUE)).isEqualTo("xyz");
  }

  @Test
  void testReadDoubleLang() {

    checkDoubleString("-9.87654321098765432109876543210");
    checkDoubleString("-2.2250738585072012e+307");
    // TODO this one is currently causing rounding error
    // checkDoubleString("9.8765432109876543210e+307");
    checkDoubleString("2.2250738585072012e-308"); // CVE-2010-4476
    checkDoubleString("-2.2250738585072012e-308");
    checkDoubleString("1.23e-348"); // +0.0 (underflow)
    checkDoubleString("-1.23e-348"); // -0.0 (underflow)
    checkDoubleString("123456789012345678901234567890e-1");
    checkDoubleString("123456789012345678901234567890e+20");
    checkDoubleString("-Infinity");
    checkDoubleString("-NaN");
    checkDoubleString("0x1234567890ABCDEF1234567890ABCDEF.0P1");
    checkDoubleString("0x123456789ABCDEF0123456789ABCDEF01234567890ABCDEFP+2");
    checkDoubleString("0x0.00000000000001234567890ABCDEFP-900");
    checkDoubleString("0xAB.CDEF01234567890ABCDEFP+1");
    checkDoubleString("0xAB.CDEF01234567890000000P+1");
    checkDoubleString("0xAB.CDEF0123456789FF0000FP+1");
    checkDoubleString("0xABCDEF0123456789FF0000FP+1");
    checkDoubleString("0xABCDEF0123456789FF0000FP+932"); // very large number
    checkDoubleString("0xABCDEF0123456789FF0000FP+933"); // infinity
    checkDoubleString("0xAB.CDP+1");
    checkDoubleString("1.234567890e+1");
    checkDoubleString("1.23456789012e+1");
    checkDoubleString("12.3456789012e-1");
    checkDoubleString(".123456789012e+2");
    checkDoubleString("0.123456789012e+2");
    checkDoubleString("00.1234567890120000000000000000000000000000e+2");
  }

  private void checkDoubleString(String number) {

    // act
    double expected = Double.parseDouble(number);
    CharStreamScanner scanner = scanner(number, 9);
    CharScannerNumberParserLang numberParser = new CharScannerNumberParserLang(CharScannerRadixMode.NO_OCTAL,
        NumberType.DOUBLE, "_");
    scanner.readNumber(numberParser);
    Double result = numberParser.asDouble();

    // assert
    if (!Objects.equals(result, expected)) {
      System.out.println("Error for: " + number);
      System.out.println("expected : " + formatBinary(expected) + " | " + expected);
      System.out.println("actual   : " + formatBinary(result.doubleValue()) + " | " + result);
    }
    if (Double.isNaN(expected)) {
      assertThat(result).isNaN();
    } else {
      assertThat(result).isEqualTo(expected);
    }
  }

  private String formatBinary(double expected) {

    String binary = Long.toUnsignedString(Double.doubleToLongBits(expected), 2);
    int zeros = 64 - binary.length();
    if (zeros > 0) {
      StringBuilder sb = new StringBuilder(64);
      while (zeros > 0) {
        sb.append('0');
        zeros--;
      }
      sb.append(binary);
      binary = sb.toString();
    }
    return binary;
  }

  @Test
  void testReadDoubleNonNumbers() {

    checkDouble("NaN");
    checkDouble("+NaN");
    checkDouble("-NaN");
    checkDouble("Infinity");
    checkDouble("+Infinity");
    checkDouble("-Infinity");
    checkDouble("NAN");
  }

  private Double checkDouble(String number) {

    // act java.lang
    Double javaD = null;
    NumberFormatException javaE = null;
    try {
      javaD = Double.valueOf(number);
    } catch (NumberFormatException e) {
      javaE = e;
    }
    // act again mmm scanner
    Double scannerD = null;
    NumberFormatException scannerE = null;
    try {
      CharStreamScanner scanner = scanner(number, 9);
      scannerD = scanner.readDouble();
    } catch (NumberFormatException e) {
      scannerE = e;
    }
    // assert
    assertThat(scannerD).isEqualTo(javaD);
    if (javaD == null) {
      assertThat(javaE).isNotNull();
      if (scannerE != null) {
        assertThat(javaE).hasMessage(scannerE.getMessage());
      }
    }
    return scannerD;
  }

  @Test
  void testReadFloat() {

    new NumberScannerFloat(s -> scanner(s, 9)).test();
  }

  @Test
  void testReadFloats() {

    // arrange
    String string = "123456789-987654321+0.123e-10xyz";
    // act
    CharStreamScanner scanner = scanner(string, 4);
    // assert
    assertThat(scanner.readFloat()).isEqualTo(123456789f);
    assertThat(scanner.readFloat()).isEqualTo(-987654321f);
    assertThat(scanner.readFloat()).isEqualTo(+0.123e-10f);
    assertThat(scanner.readFloat()).isNull();
    // scanner.require("xyz");
    assertThat(scanner.read(Integer.MAX_VALUE)).isEqualTo("xyz");
  }

  /**
   * Tests {@link CharSequenceScanner#readUntil(char, boolean, CharScannerSyntax)} with a {@link CharScannerSyntax}
   * using all features in combination.
   */
  @Test
  void testReadUntilWithSyntaxFull() {

    // arrange (full syntax)
    CharScannerSyntaxBean syntax = new CharScannerSyntaxBean() {

      @Override
      public String resolveEntity(String entity) {

        if ("lt".equals(entity)) {
          return "<";
        } else if ("gt".equals(entity)) {
          return ">";
        }
        return super.resolveEntity(entity);
      }
    };
    syntax.setEscape('\\');
    syntax.setQuote('"');
    syntax.setQuoteEscape('$');
    syntax.setAltQuote('\'');
    syntax.setAltQuoteStart('\'');
    syntax.setAltQuoteEnd('\'');
    syntax.setAltQuoteEscape('\'');
    syntax.setEntityStart('&');
    syntax.setEntityEnd(';');

    // act
    CharStreamScanner scanner = scanner("Hi \"$\"quote$\"\", 'a''l\\t' and \\\"esc\\'&lt;&gt;&lt;x&gt;!");
    String result = scanner.readUntil('!', false, syntax);
    // assert
    assertThat(result).isEqualTo("Hi \"quote\", a'l\\t and \"esc'<><x>");
    assertThat(scanner.hasNext()).isFalse();
    assertThat(scanner.getPosition()).isEqualTo(54);
    assertThat(scanner.getColumn()).isEqualTo(55);
    assertThat(scanner.getLine()).isEqualTo(1);

    // act again (with acceptEof)
    scanner = scanner("Hi 'qu''ote'");
    result = scanner.readUntil('\0', true, syntax);
    // assert
    assertThat(result).isEqualTo("Hi qu'ote");
    assertThat(scanner.hasNext()).isFalse();
    assertThat(scanner.getPosition()).isEqualTo(12);
    assertThat(scanner.getColumn()).isEqualTo(13);
    assertThat(scanner.getLine()).isEqualTo(1);
  }

  private void check(char stop, boolean acceptEot, CharScannerSyntax syntax, String input, String expected) {

    // pass 1: test with readUntil giving stop as char
    CharStreamScanner scanner = scanner(input);
    String output = scanner.readUntil(stop, acceptEot, syntax);
    assertThat(output).isEqualTo(expected);
    if (!scanner.hasNext()) {
      int length = input.length();
      assertThat(scanner.getPosition()).isEqualTo(length);
      assertThat(scanner.getColumn()).isEqualTo(length + 1);
      assertThat(scanner.getLine()).isEqualTo(1);
    }

    // pass 2: test with readUntil giving stop as char
    scanner = scanner(input);
    output = scanner.readUntil(c -> c == stop, acceptEot, syntax);
    assertThat(output).isEqualTo(expected);
    assertThat(scanner.hasNext()).isTrue();
    assertThat(scanner.next()).isEqualTo(stop);
    if (!scanner.hasNext()) {
      int length = input.length();
      assertThat(scanner.getPosition()).isEqualTo(length);
      assertThat(scanner.getColumn()).isEqualTo(length + 1);
      assertThat(scanner.getLine()).isEqualTo(1);
    }
  }

  @Test
  void testExpectPositive() {

    // arrange
    String start = "hello ";
    String middle = "world";
    String end = " this is cool!";
    // act
    CharStreamScanner scanner = scanner(start + middle + end);
    // assert
    assertThat(scanner.expectUnsafe(start, false)).isTrue();
    assertThat(scanner.expectUnsafe(middle.toUpperCase(Locale.ENGLISH), true)).isTrue();
    assertThat(scanner.expectUnsafe(end.toLowerCase(Locale.ENGLISH), true)).isTrue();
    assertThat(scanner.hasNext()).isFalse();
    assertThat(scanner.getPosition()).isEqualTo(25);
    assertThat(scanner.getColumn()).isEqualTo(26);
    assertThat(scanner.getLine()).isEqualTo(1);
  }

  @Test
  void testExpectStrict() {

    // arrange
    String string = "Hello World!";
    // act
    CharStreamScanner scanner = scanner(string, true);
    // assert
    assertThat(scanner.expect("Hello WorlD", false)).isFalse();
    assertThat(scanner.getPosition()).isEqualTo(0);
    assertThat(scanner.getColumn()).isEqualTo(1);
    assertThat(scanner.getLine()).isEqualTo(1);
    assertThat(scanner.expect("Hello ", false)).isTrue();
    assertThat(scanner.getPosition()).isEqualTo(6);
    assertThat(scanner.getColumn()).isEqualTo(7);
    assertThat(scanner.getLine()).isEqualTo(1);
    assertThat(scanner.expect("WorlD!", true)).isTrue();
    assertThat(scanner.getPosition()).isEqualTo(12);
    assertThat(scanner.getColumn()).isEqualTo(13);
    assertThat(scanner.getLine()).isEqualTo(1);
    assertThat(scanner.hasNext()).isFalse();
  }

  @Test
  void testExpectNegative() {

    // arrange
    String string = "string";
    // act
    CharStreamScanner scanner = scanner(string);
    // assert
    assertThat(scanner.expectUnsafe("strign", false)).isFalse();
    assertThat(scanner.read(2)).isEqualTo("ng");
    assertThat(scanner.hasNext()).isFalse();
    assertThat(scanner.getPosition()).isEqualTo(6);
    assertThat(scanner.getColumn()).isEqualTo(7);
    assertThat(scanner.getLine()).isEqualTo(1);
  }

  @Test
  void testNext() {

    // arrange
    String string = "0123456789";
    // act
    CharStreamScanner scanner = scanner(string);
    for (int i = 0; i < 10; i++) {
      assertThat(scanner.hasNext()).isTrue();
      int cp = scanner.next();
      char expected = (char) ('0' + i);
      // assert
      assertThat(cp).isEqualTo(expected);
    }
    assertThat(scanner.hasNext()).isFalse();
    assertThat(scanner.next()).isEqualTo(CharStreamScanner.EOS);
    assertThat(scanner.getPosition()).isEqualTo(10);
    assertThat(scanner.getColumn()).isEqualTo(11);
    assertThat(scanner.getLine()).isEqualTo(1);
  }

  @Test
  void testReadWhile() {

    // arrange
    String string = "abc def  ghi";
    CharFilter textFilter = CharFilter.LATIN_LETTER;
    CharFilter spaceFilter = CharFilter.WHITESPACE;
    // act
    CharStreamScanner scanner = scanner(string);
    // assert
    assertThat(scanner.readWhile(textFilter)).isEqualTo("abc");
    assertThat(scanner.readWhile(textFilter)).isEmpty();
    assertThat(scanner.readWhile(textFilter, 0, 0)).isEmpty();
    assertThat(scanner.readWhile(spaceFilter)).isEqualTo(" ");
    assertThat(scanner.readWhile(textFilter)).isEqualTo("def");
    assertThat(scanner.readWhile(spaceFilter)).isEqualTo("  ");
    assertThat(scanner.readWhile(textFilter, 0, 2)).isEqualTo("gh");
    assertThat(scanner.readWhile(textFilter, 0, 2)).isEqualTo("i");
    assertThat(scanner.hasNext()).isFalse();
    assertThat(scanner.getPosition()).isEqualTo(12);
    assertThat(scanner.getColumn()).isEqualTo(13);
    assertThat(scanner.getLine()).isEqualTo(1);
  }

  @Test
  void testPeekWhile() {

    // arrange
    String string = "abc def  ghi";
    CharFilter textFilter = CharFilter.LATIN_LETTER;
    CharFilter spaceFilter = CharFilter.WHITESPACE;
    // act
    CharStreamScanner scanner = scanner(string, 3);
    // assert
    assertThat(scanner.peekWhile(textFilter, 3)).isEqualTo("abc");
    scanner.skip(3);
    assertThat(scanner.peekWhile(textFilter, 3)).isEmpty();
    scanner.skip(1);
    assertThat(scanner.peekWhile(textFilter, 3)).isEqualTo("def");
    scanner.skip(3);
    assertThat(scanner.peekWhile(textFilter, 3)).isEmpty();
    scanner.skip(2);
    assertThat(scanner.peekWhile(textFilter, 2)).isEqualTo("gh");
    assertThat(scanner.peekWhile(textFilter, 3)).isEqualTo("ghi");
  }

  @Test
  void testReadLine() {

    // arrange
    String string = "abc\ndef\rghi\r\njkl\n\rend";
    // act
    CharStreamScanner scanner = scanner(string);
    // assert
    assertThat(scanner.readLine()).isEqualTo("abc");
    assertThat(scanner.readLine()).isEqualTo("def");
    assertThat(scanner.readLine()).isEqualTo("ghi");
    assertThat(scanner.readLine()).isEqualTo("jkl");
    assertThat(scanner.readLine()).isEmpty();
    assertThat(scanner.readLine()).isEqualTo("end");
    // arrange
    string = "abc\ndef\nghi\r\njkl\n\nend";
    // act
    scanner = scanner(string);
    // assert
    assertThat(scanner.readLine()).isEqualTo("abc");
    assertThat(scanner.getPosition()).isEqualTo(4);
    assertThat(scanner.getColumn()).isEqualTo(1);
    assertThat(scanner.getLine()).isEqualTo(2);
    assertThat(scanner.readLine()).isEqualTo("def");
    assertThat(scanner.getPosition()).isEqualTo(8);
    assertThat(scanner.getColumn()).isEqualTo(1);
    assertThat(scanner.getLine()).isEqualTo(3);
    assertThat(scanner.readLine()).isEqualTo("ghi");
    assertThat(scanner.getPosition()).isEqualTo(13);
    assertThat(scanner.getColumn()).isEqualTo(1);
    assertThat(scanner.getLine()).isEqualTo(4);
    assertThat(scanner.readLine()).isEqualTo("jkl");
    assertThat(scanner.getPosition()).isEqualTo(17);
    assertThat(scanner.getColumn()).isEqualTo(1);
    assertThat(scanner.getLine()).isEqualTo(5);
    assertThat(scanner.readLine()).isEmpty();
    assertThat(scanner.getPosition()).isEqualTo(18);
    assertThat(scanner.getColumn()).isEqualTo(1);
    assertThat(scanner.getLine()).isEqualTo(6);
    assertThat(scanner.readLine()).isEqualTo("end");
    assertThat(scanner.getPosition()).isEqualTo(21);
    assertThat(scanner.getColumn()).isEqualTo(4);
    assertThat(scanner.getLine()).isEqualTo(6);
  }

  @Test
  void testReadLineWithTrim() {

    // arrange
    String string = "  ab c \ndef\r ghi\r\nj k l\n \r \n  \r\n   end";
    // act
    CharStreamScanner scanner = scanner(string);
    // assert
    assertThat(scanner.readLine(true)).isEqualTo("ab c");
    assertThat(scanner.readLine(true)).isEqualTo("def");
    assertThat(scanner.readLine(true)).isEqualTo("ghi");
    assertThat(scanner.readLine(true)).isEqualTo("j k l");
    assertThat(scanner.readLine(true)).isEmpty();
    assertThat(scanner.readLine(true)).isEmpty();
    assertThat(scanner.readLine(true)).isEmpty();
    assertThat(scanner.readLine(true)).isEqualTo("end");
  }

  @Test
  void testReadDigit() {

    // arrange
    String string = "01234567890a ";
    // act
    CharStreamScanner scanner = scanner(string);
    // assert
    for (int i = 0; i < 10; i++) {
      assertThat(scanner.readDigit()).isEqualTo(i);
    }
    assertThat(scanner.readDigit()).isEqualTo(0);
    assertThat(scanner.readDigit()).isEqualTo(-1);
    assertThat(scanner.next()).isEqualTo('a');
    assertThat(scanner.readDigit()).isEqualTo(-1);
    assertThat(scanner.next()).isEqualTo(' ');
    assertThat(scanner.getPosition()).isEqualTo(13);
    assertThat(scanner.getColumn()).isEqualTo(14);
    assertThat(scanner.getLine()).isEqualTo(1);
  }

  @Test
  void testPeek() {

    // arrange
    String string = "abc";
    // act
    CharStreamScanner scanner = scanner(string);
    // assert
    assertThat(scanner.peek()).isEqualTo('a');
    assertThat(scanner.peek()).isEqualTo('a');
    assertThat(scanner.next()).isEqualTo('a');

    assertThat(scanner.peek()).isEqualTo('b');
    assertThat(scanner.next()).isEqualTo('b');

    assertThat(scanner.peek()).isEqualTo('c');
    assertThat(scanner.next()).isEqualTo('c');

    assertThat(scanner.peek()).isEqualTo(CharStreamScanner.EOS);
    assertThat(scanner.next()).isEqualTo(CharStreamScanner.EOS);
  }

  @Test
  void testExpect() {

    // arrange
    String string = "public static final String foo;";
    // act
    CharStreamScanner scanner = scanner(string);
    // assert
    assertThat(scanner.expectUnsafe("public", false)).isTrue();
    assertThat(scanner.expectOne('$')).isFalse();
    assertThat(scanner.expectOne(' ')).isTrue();
    assertThat(scanner.expectUnsafe("StATiC", true)).isTrue();
    assertThat(scanner.expectOne(' ')).isTrue();
    assertThat(scanner.expectUnsafe("FINAL", false)).isFalse();
    assertThat(scanner.expectUnsafe("FINAL", true)).isTrue();
    assertThat(scanner.expectOne(' ')).isTrue();
    assertThat(scanner.expectUnsafe("string", false)).isFalse();
    assertThat(scanner.expectUnsafe("String", false)).isTrue();
    assertThat(scanner.next()).isEqualTo(' ');
    assertThat(scanner.expectUnsafe("banana", true)).isFalse();
    assertThat(scanner.expectUnsafe("foo", false)).isTrue();
    assertThat(scanner.next()).isEqualTo(';');
    assertThat(scanner.hasNext()).isFalse();
    assertThat(scanner.getPosition()).isEqualTo(31);
    assertThat(scanner.getColumn()).isEqualTo(32);
    assertThat(scanner.getLine()).isEqualTo(1);
  }

  @Test
  void testEmpty() {

    // arrange
    String string = "";
    CharScannerSyntaxBean syntax = new CharScannerSyntaxBean();
    CharFilter filter = CharFilter.ANY;
    // act
    CharStreamScanner scanner = scanner(string);
    // assert
    assertThat(scanner.peek()).isEqualTo(CharStreamScanner.EOS);
    assertThat(scanner.next()).isEqualTo(CharStreamScanner.EOS);
    assertThat(scanner.readDigit()).isEqualTo(-1);
    assertThat(scanner.read(1)).isEmpty();
    assertThat(scanner.readLine()).isNull();
    assertThat(scanner.readUntil(' ', true)).isEmpty();
    assertThat(scanner.readUntil(' ', false)).isNull();
    assertThat(scanner.readUntil(' ', true, syntax)).isEmpty();
    assertThat(scanner.readUntil(' ', false, syntax)).isNull();
    assertThat(scanner.readUntil(' ', true, '\\')).isEmpty();
    assertThat(scanner.readUntil(' ', false, '\\')).isNull();
    assertThat(scanner.readUntil(filter, true)).isEmpty();
    assertThat(scanner.readUntil(filter, false)).isNull();
    assertThat(scanner.readWhile(filter)).isEmpty();
    assertThat(scanner.skipUntil(' ')).isFalse();
    assertThat(scanner.skipUntil(' ', '\\')).isFalse();
    assertThat(scanner.skipWhile(' ')).isEqualTo(0);
    assertThat(scanner.skipWhile(filter)).isEqualTo(0);
    assertThat(scanner.skipWhileAndPeek(filter)).isEqualTo(CharStreamScanner.EOS);
    assertThat(scanner.skipWhileAndPeek(filter, 10)).isEqualTo(CharStreamScanner.EOS);
    assertThat(scanner.expectOne(' ')).isFalse();
    assertThat(scanner.expectUnsafe("Text", true)).isFalse();
    assertThat(scanner.getPosition()).isEqualTo(0);
    assertThat(scanner.getColumn()).isEqualTo(1);
    assertThat(scanner.getLine()).isEqualTo(1);
  }

  /**
   * Tests {@link CharStreamScanner#readJavaStringLiteral()}.
   */
  @Test
  void testReadJavaStringLiteral() {

    // arrange
    String string = "\"Hi \\\"\\176\\477\\579\\u2022\\uuuuu2211\\\"\\n\"";
    // act
    CharStreamScanner scanner = scanner(string);
    String result = scanner.readJavaStringLiteral();
    // assert
    assertThat(result).isEqualTo("Hi \"\176\477\579\u2022\uuuuu2211\"\n").isEqualTo("Hi \"~'7/9•∑\"\n");
    assertThat(scanner.hasNext()).isFalse();
    assertThat(scanner.getPosition()).isEqualTo(39);
    assertThat(scanner.getColumn()).isEqualTo(40);
    assertThat(scanner.getLine()).isEqualTo(1);
  }

  /**
   * Tests {@link CharStreamScanner#readJavaStringLiteral()} with invald data.
   */
  @Test
  void testReadJavaStringLiteralErrors() {

    readJavaStringLiteralInvalid("\"", null, "");
    readJavaStringLiteralInvalid("\"a", null, "a");
    readJavaStringLiteralInvalid("\"ab", null, "ab");
    readJavaStringLiteralInvalid("\"ab\\\"", null, "ab\"");
    readJavaStringLiteralInvalid("\"ab\\\"\\8", "\\8", "ab\"8");
    readJavaStringLiteralInvalid("\"\\u1\"$", "\\u1", "?");
    readJavaStringLiteralInvalid("\"a\\u123x\"", "\\u123", "a?x");
  }

  private void readJavaStringLiteralInvalid(String string, String illegalEscape, String tolerantResult) {

    CharStreamScanner scanner = scanner(string);
    assertThat(scanner.readJavaStringLiteral(null)).isEqualTo(tolerantResult);
    int length = string.length();
    // ensure that we read to the end...
    while (scanner.hasNext()) {
      scanner.next();
    }
    assertThat(scanner.getPosition()).isEqualTo(length);
    assertThat(scanner.getColumn()).isEqualTo(length + 1);
    assertThat(scanner.getLine()).isEqualTo(1);
    Class<IllegalStateException> exception = IllegalStateException.class;
    try {
      scanner(string).readJavaStringLiteral();
      failBecauseExceptionWasNotThrown(exception);
    } catch (Exception e) {
      String message;
      if (illegalEscape != null) {
        message = "Illegal escape sequence " + illegalEscape;
      } else {
        message = "Java string literal not terminated";
      }
      assertThat(e).isInstanceOf(exception).hasMessage(message);
    }
  }

  /**
   * Tests {@link CharStreamScanner#readJavaCharLiteral()}.
   */
  @Test
  void testReadJavaCharLiteral() {

    // arrange
    String string = "'a'$'\\''$'\\\\'$'\\0'$'\\47'$'\\176'$'\\u2022'$";
    // act
    CharStreamScanner scanner = scanner(string);
    // assert
    assertThat(scanner.readJavaCharLiteral()).isEqualTo('a');
    assertThat(scanner.expectOne('$')).isTrue();
    assertThat(scanner.readJavaCharLiteral()).isEqualTo('\'');
    assertThat(scanner.expectOne('$')).isTrue();
    assertThat(scanner.readJavaCharLiteral()).isEqualTo('\\');
    assertThat(scanner.expectOne('$')).isTrue();
    assertThat(scanner.readJavaCharLiteral()).isEqualTo('\0');
    assertThat(scanner.expectOne('$')).isTrue();
    assertThat(scanner.readJavaCharLiteral()).isEqualTo('\'');
    assertThat(scanner.expectOne('$')).isTrue();
    assertThat(scanner.readJavaCharLiteral()).isEqualTo('~');
    assertThat(scanner.expectOne('$')).isTrue();
    assertThat(scanner.readJavaCharLiteral()).isEqualTo('•');
    assertThat(scanner.expectOne('$')).isTrue();
    assertThat(scanner.hasNext()).isFalse();
    assertThat(scanner.getPosition()).isEqualTo(41);
    assertThat(scanner.getColumn()).isEqualTo(42);
    assertThat(scanner.getLine()).isEqualTo(1);

    // and given
    string = "'a''\\'''\\\\''\\0''\\47''\\176''\\u2022'";
    // act
    scanner = scanner(string);
    // assert
    assertThat(scanner.readJavaCharLiteral()).isEqualTo('a');
    assertThat(scanner.readJavaCharLiteral()).isEqualTo('\'');
    assertThat(scanner.readJavaCharLiteral()).isEqualTo('\\');
    assertThat(scanner.readJavaCharLiteral()).isEqualTo('\0');
    assertThat(scanner.readJavaCharLiteral()).isEqualTo('\'');
    assertThat(scanner.readJavaCharLiteral()).isEqualTo('~');
    assertThat(scanner.readJavaCharLiteral()).isEqualTo('•');
    assertThat(scanner.hasNext()).isFalse();
    assertThat(scanner.getPosition()).isEqualTo(34);
    assertThat(scanner.getColumn()).isEqualTo(35);
    assertThat(scanner.getLine()).isEqualTo(1);
  }

  /**
   * Tests {@link CharStreamScanner#readJavaCharLiteral()} with invalid data.
   */
  @Test
  void testReadJavaCharLiteralErrors() {

    readJavaCharLiteralInvalid("'", "'");
    readJavaCharLiteralInvalid("'a", "'a");
    readJavaCharLiteralInvalid("'ab'", "'ab'");
    readJavaCharLiteralInvalid("'ab'$", "'ab'");
    readJavaCharLiteralInvalid("'\\u12345'$", "'\\u12345'");
    readJavaCharLiteralInvalid("'\\8'$", "'\\8'");
    readJavaCharLiteralInvalid("'\\78'$", "'\\78'");
    readJavaCharLiteralInvalid("'\\477'$", "'\\477'");
  }

  private void readJavaCharLiteralInvalid(String string, String expectedErrorValue) {

    CharStreamScanner scanner = scanner(string);
    assertThat(scanner.readJavaCharLiteral(null)).isEqualTo('?');
    int length = string.length();
    // ensure that we read to the end...
    while (scanner.hasNext()) {
      scanner.next();
    }
    assertThat(scanner.getPosition()).isEqualTo(length);
    assertThat(scanner.getColumn()).isEqualTo(length + 1);
    assertThat(scanner.getLine()).isEqualTo(1);
    Class<IllegalStateException> exception = IllegalStateException.class;
    try {
      scanner(string).readJavaCharLiteral();
      failBecauseExceptionWasNotThrown(exception);
    } catch (Exception e) {
      assertThat(e).isInstanceOf(exception).hasMessageContaining(expectedErrorValue);
    }
  }

  @Test
  void testUnicode() {

    // arrange
    String gClev = "\uD834\uDD1E";
    String dBar = "\uD834\uDD01";
    String x = "x";
    String note1_8 = "\uD834\uDD60";
    String text = gClev + dBar + x + note1_8;
    CharStreamScanner scanner = scanner(text);
    // act + assert
    assertThat(scanner.next()).isEqualTo(gClev.codePointAt(0));
    assertThat(scanner.next()).isEqualTo(dBar.codePointAt(0));
    assertThat(scanner.next()).isEqualTo(x.codePointAt(0));
    assertThat(scanner.next()).isEqualTo(note1_8.codePointAt(0));
    assertThat(scanner.hasNext()).isFalse();
  }
}
