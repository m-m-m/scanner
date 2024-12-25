/* Copyright (c) The m-m-m Team, Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0 */
package io.github.mmm.scanner;

/**
 * This is the implementation of {@link CharScannerSyntax} as Java bean. <br>
 * The actual {@code char}s like {@link #getEscape() escape} are realized as simple bean-properties and initialized with
 * <code>'\0'</code> so they are disabled by default.
 *
 * @see CharStreamScanner#readUntil(int, boolean, CharScannerSyntax)
 */
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

  /**
   * The constructor.
   */
  public CharScannerSyntaxBean() {

    super();
    this.escape = '\0';
    this.quoteStart = '\0';
    this.quoteEnd = '\0';
  }

  @Override
  public int getEscape() {

    return this.escape;
  }

  /**
   * @param escape is the {@link #getEscape() escape} to set.
   */
  public void setEscape(int escape) {

    this.escape = escape;
  }

  @Override
  public int getQuoteStart() {

    return this.quoteStart;
  }

  /**
   * @param quoteStart is the {@link #getQuoteStart() quoteStart} to set.
   */
  public void setQuoteStart(int quoteStart) {

    this.quoteStart = quoteStart;
  }

  @Override
  public int getQuoteEnd() {

    return this.quoteEnd;
  }

  /**
   * @param quoteEnd is the {@link #getQuoteEnd() quoteEnd} to set.
   */
  public void setQuoteEnd(int quoteEnd) {

    this.quoteEnd = quoteEnd;
  }

  /**
   * This method sets both the {@link #getQuoteStart() quote-start} and {@link #getQuoteEnd() quote-end} character.
   *
   * @param quote the quote character to set.
   */
  public void setQuote(int quote) {

    this.quoteStart = quote;
    this.quoteEnd = quote;
  }

  @Override
  public int getQuoteEscape() {

    return this.quoteEscape;
  }

  /**
   * @param quoteEscape is the {@link #getQuoteEnd() quote-escape} to set.
   */
  public void setQuoteEscape(int quoteEscape) {

    this.quoteEscape = quoteEscape;
  }

  @Override
  public boolean isQuoteEscapeLazy() {

    return this.quoteEscapeLazy;
  }

  /**
   * @param quoteEscapeLazy the {@link #isQuoteEscapeLazy() quote-escape-lazy} flag to set
   */
  public void setQuoteEscapeLazy(boolean quoteEscapeLazy) {

    this.quoteEscapeLazy = quoteEscapeLazy;
  }

  @Override
  public int getAltQuoteStart() {

    return this.altQuoteStart;
  }

  /**
   * @param alternativeQuoteStart is the {@link #getAltQuoteStart() alt-quote-start} character to set.
   */
  public void setAltQuoteStart(int alternativeQuoteStart) {

    this.altQuoteStart = alternativeQuoteStart;
  }

  @Override
  public int getAltQuoteEnd() {

    return this.altQuoteEnd;
  }

  /**
   * This method sets the {@link #getAltQuoteEnd() alt-quote-end} character.
   *
   * @param alternativeQuoteEnd is the {@link #getAltQuoteEnd() alt-quote-end} character.
   */
  public void setAltQuoteEnd(int alternativeQuoteEnd) {

    this.altQuoteEnd = alternativeQuoteEnd;
  }

  /**
   * This method sets both the {@link #getAltQuoteStart() alt-quote-start} and {@link #getAltQuoteEnd() alt-quote-end}
   * character.
   *
   * @param altQuote the alt-quote character to set.
   */
  public void setAltQuote(int altQuote) {

    this.altQuoteStart = altQuote;
    this.altQuoteEnd = altQuote;
  }

  @Override
  public int getAltQuoteEscape() {

    return this.altQuoteEscape;
  }

  /**
   * @param altQuoteEscape is the {@link #getAltQuoteEscape() alt-quote-escape} to set.
   */
  public void setAltQuoteEscape(int altQuoteEscape) {

    this.altQuoteEscape = altQuoteEscape;
  }

  @Override
  public boolean isAltQuoteEscapeLazy() {

    return this.altQuoteEscapeLazy;
  }

  /**
   * @param altQuoteEscapeLazy the {@link #isAltQuoteEscapeLazy() alt-quote-lazy} flag to set
   */
  public void setAltQuoteEscapeLazy(boolean altQuoteEscapeLazy) {

    this.altQuoteEscapeLazy = altQuoteEscapeLazy;
  }

  @Override
  public int getEntityStart() {

    return this.entityStart;
  }

  /**
   * @param entityStart the {@link #getEntityStart() entity-start} to set.
   */
  public void setEntityStart(int entityStart) {

    this.entityStart = entityStart;
  }

  @Override
  public int getEntityEnd() {

    return this.entityEnd;
  }

  /**
   * @param entityEnd the {@link #getEntityEnd() entity-end} to set.
   */
  public void setEntityEnd(int entityEnd) {

    this.entityEnd = entityEnd;
  }

  /**
   * {@inheritDoc} <br>
   * <b>ATTENTION:</b><br>
   * You need to override this method if you want to {@link #setEntityStart(int) use} entities.
   */
  @Override
  public String resolveEntity(String entity) {

    throw new IllegalArgumentException(entity);
  }
}
