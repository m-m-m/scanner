/* Copyright (c) The m-m-m Team, Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0 */
package io.github.mmm.scanner;

import java.util.HashMap;
import java.util.Map;

/// This is the implementation of [CharScannerSyntax] as Java bean.
/// The actual `char`s like [escape][#getEscape()] are realized as simple bean-properties and initialized with
/// `'\0'` so they are disabled by default.
///
/// @see CharStreamScanner#readUntil(int, boolean, CharScannerSyntax)
public class CharScannerSyntaxBean implements CharScannerSyntax {

  private int escape;

  private int quoteStart;

  private int quoteEnd;

  private int quoteEscape;

  private boolean quoteEscapeLazy;

  private int altQuoteStart;

  private int altQuoteEnd;

  private int altQuoteEscape;

  private boolean altQuoteEscapeLazy;

  private int entityStart;

  private int entityEnd;

  private Map<Integer, Integer> escapeMap;

  /// The constructor.
  public CharScannerSyntaxBean() {

    super();
    this.escape = CharStreamScanner.EOS;
    this.quoteStart = CharStreamScanner.EOS;
    this.quoteEnd = CharStreamScanner.EOS;
  }

  @Override
  public int getEscape() {

    return this.escape;
  }

  /// @param escape is the [escape][#getEscape()] to set.
  public void setEscape(int escape) {

    this.escape = escape;
  }

  @Override
  public int getQuoteStart() {

    return this.quoteStart;
  }

  /// @param quoteStart is the [quoteStart][#getQuoteStart()] to set.
  public void setQuoteStart(int quoteStart) {

    this.quoteStart = quoteStart;
  }

  @Override
  public int getQuoteEnd() {

    return this.quoteEnd;
  }

  /// @param quoteEnd is the [quoteEnd][#getQuoteEnd()] to set.
  public void setQuoteEnd(int quoteEnd) {

    this.quoteEnd = quoteEnd;
  }

  /// This method sets both the [quote-start][#getQuoteStart()] and [quote-end][#getQuoteEnd()] character.
  ///
  /// @param quote the quote character to set.
  public void setQuote(int quote) {

    this.quoteStart = quote;
    this.quoteEnd = quote;
  }

  @Override
  public int getQuoteEscape() {

    return this.quoteEscape;
  }

  /// @param quoteEscape is the [quote-escape][#getQuoteEnd()] to set.
  public void setQuoteEscape(int quoteEscape) {

    this.quoteEscape = quoteEscape;
  }

  @Override
  public boolean isQuoteEscapeLazy() {

    return this.quoteEscapeLazy;
  }

  /// @param quoteEscapeLazy the [quote-escape-lazy][#isQuoteEscapeLazy()] flag to set
  public void setQuoteEscapeLazy(boolean quoteEscapeLazy) {

    this.quoteEscapeLazy = quoteEscapeLazy;
  }

  @Override
  public int getAltQuoteStart() {

    return this.altQuoteStart;
  }

  /// @param alternativeQuoteStart is the [alt-quote-start][#getAltQuoteStart()] character to set.
  public void setAltQuoteStart(int alternativeQuoteStart) {

    this.altQuoteStart = alternativeQuoteStart;
  }

  @Override
  public int getAltQuoteEnd() {

    return this.altQuoteEnd;
  }

  /// This method sets the [alt-quote-end][#getAltQuoteEnd()] character.
  ///
  /// @param alternativeQuoteEnd is the [alt-quote-end][#getAltQuoteEnd()] character.
  public void setAltQuoteEnd(int alternativeQuoteEnd) {

    this.altQuoteEnd = alternativeQuoteEnd;
  }

  /// This method sets both the [alt-quote-start][#getAltQuoteStart()] and [alt-quote-end][#getAltQuoteEnd()]
  /// character.
  ///
  /// @param altQuote the alt-quote character to set.
  public void setAltQuote(int altQuote) {

    this.altQuoteStart = altQuote;
    this.altQuoteEnd = altQuote;
  }

  @Override
  public int getAltQuoteEscape() {

    return this.altQuoteEscape;
  }

  /// @param altQuoteEscape is the [alt-quote-escape][#getAltQuoteEscape()] to set.
  public void setAltQuoteEscape(int altQuoteEscape) {

    this.altQuoteEscape = altQuoteEscape;
  }

  @Override
  public boolean isAltQuoteEscapeLazy() {

    return this.altQuoteEscapeLazy;
  }

  /// @param altQuoteEscapeLazy the [alt-quote-lazy][#isAltQuoteEscapeLazy()] flag to set
  public void setAltQuoteEscapeLazy(boolean altQuoteEscapeLazy) {

    this.altQuoteEscapeLazy = altQuoteEscapeLazy;
  }

  @Override
  public int getEntityStart() {

    return this.entityStart;
  }

  /// @param entityStart the [entity-start][#getEntityStart()] to set.
  public void setEntityStart(int entityStart) {

    this.entityStart = entityStart;
  }

  @Override
  public int getEntityEnd() {

    return this.entityEnd;
  }

  /// @param entityEnd the [entity-end][#getEntityEnd()] to set.
  public void setEntityEnd(int entityEnd) {

    this.entityEnd = entityEnd;
  }

  @Override
  public int mapEscaped(int codePoint) {

    if (this.escapeMap != null) {
      Integer result = this.escapeMap.get(codePoint);
      if (result != null) {
        return result.intValue();
      }
    }
    return CharScannerSyntax.super.mapEscaped(codePoint);
  }

  /// Adds a custom mapping to [#mapEscaped(int)].
  ///
  /// @param sourceCp the source [code-point][String#codePointAt(int)].
  /// @param targetCp the mapped target [code-point][String#codePointAt(int)].
  public void addEscapeMapping(int sourceCp, int targetCp) {

    if (this.escapeMap == null) {
      this.escapeMap = new HashMap<>();
    }
    this.escapeMap.put(sourceCp, targetCp);
  }

}
