/* Copyright (c) The m-m-m Team, Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0 */
package io.github.mmm.scanner;

/**
 * Simplified implementation of {@link CharScannerSyntax} as immutable type.
 */
final class CharScannerSyntaxType implements CharScannerSyntax {

  static final CharScannerSyntaxType SINGLE_QUOTE = new CharScannerSyntaxType('\\', '\'');

  static final CharScannerSyntaxType DOUBLE_QUOTE = new CharScannerSyntaxType('\\', '"');

  static final CharScannerSyntaxType BOTH_QUOTES = new CharScannerSyntaxType('\\', '"', '\'');

  private final int escape;

  private final int quote;

  private final int altQuote;

  private CharScannerSyntaxType(int escape, int quote) {

    this(escape, quote, CharStreamScanner.EOS);
  }

  private CharScannerSyntaxType(int escape, int quote, int altQuote) {

    super();
    this.escape = escape;
    this.quote = quote;
    this.altQuote = altQuote;
  }

  @Override
  public int getEscape() {

    return this.escape;
  }

  @Override
  public int getQuoteStart() {

    return this.quote;
  }

  @Override
  public int getQuoteEnd() {

    return this.quote;
  }

  @Override
  public int getQuoteEscape() {

    return this.escape;
  }

  @Override
  public boolean isQuoteEscapeLazy() {

    return false;
  }

  @Override
  public int getAltQuoteStart() {

    return this.altQuote;
  }

  @Override
  public int getAltQuoteEnd() {

    return this.altQuote;
  }

  @Override
  public int getAltQuoteEscape() {

    return this.escape;
  }

  @Override
  public boolean isAltQuoteEscapeLazy() {

    return false;
  }

  @Override
  public int getEntityStart() {

    return CharStreamScanner.EOS;
  }

  @Override
  public int getEntityEnd() {

    return CharStreamScanner.EOS;
  }

}
