/* Copyright (c) The m-m-m Team, Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0 */
package net.sf.mmm.scanner;

/**
 * This is the implementation of {@link CharScannerSyntax} as Java bean. <br>
 * The actual {@code char}s like {@link #getEscape() escape} are realized as simple bean-properties and initialized with
 * <code>'\0'</code> so they are disabled by default.
 *
 * @see CharStreamScanner#readUntil(char, boolean, CharScannerSyntax)
 *
 * @author Joerg Hohwiller (hohwille at users.sourceforge.net)
 * @since 1.0.0
 */
public class CharScannerSyntaxBean implements CharScannerSyntax {

  private char escape;

  private char quoteStart;

  private char quoteEnd;

  private char quoteEscape;

  private boolean quoteEscapeLazy;

  private char altQuoteStart;

  private char altQuoteEnd;

  private char altQuoteEscape;

  private boolean altQuoteEscapeLazy;

  private char entityStart;

  private char entityEnd;

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
  public char getEscape() {

    return this.escape;
  }

  /**
   * @param escape is the {@link #getEscape() escape} to set.
   */
  public void setEscape(char escape) {

    this.escape = escape;
  }

  @Override
  public char getQuoteStart() {

    return this.quoteStart;
  }

  /**
   * @param quoteStart is the {@link #getQuoteStart() quoteStart} to set.
   */
  public void setQuoteStart(char quoteStart) {

    this.quoteStart = quoteStart;
  }

  @Override
  public char getQuoteEnd() {

    return this.quoteEnd;
  }

  /**
   * @param quoteEnd is the {@link #getQuoteEnd() quoteEnd} to set.
   */
  public void setQuoteEnd(char quoteEnd) {

    this.quoteEnd = quoteEnd;
  }

  /**
   * This method sets both the {@link #getQuoteStart() quote-start} and {@link #getQuoteEnd() quote-end} character.
   *
   * @param quote the quote character to set.
   */
  public void setQuote(char quote) {

    this.quoteStart = quote;
    this.quoteEnd = quote;
  }

  @Override
  public char getQuoteEscape() {

    return this.quoteEscape;
  }

  /**
   * @param quoteEscape is the {@link #getQuoteEnd() quote-escape} to set.
   */
  public void setQuoteEscape(char quoteEscape) {

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

  /**
   * This method gets the alternative character used to start a quotation that should be terminated by a
   * {@link #getAltQuoteEnd() alt-quote-end} character. The text inside the quote is taken as is (without the quote
   * characters).
   *
   * @see #getQuoteStart()
   *
   * @return the alternative character used to start a quotation or {@code '\0'} for no quotation.
   */
  @Override
  public char getAltQuoteStart() {

    return this.altQuoteStart;
  }

  /**
   * @param alternativeQuoteStart is the {@link #getAltQuoteStart() alt-quote-start} character to set.
   */
  public void setAltQuoteStart(char alternativeQuoteStart) {

    this.altQuoteStart = alternativeQuoteStart;
  }

  /**
   * This method gets the alternative character used to end a quotation.
   *
   * @see #getAltQuoteStart()
   *
   * @return the alternative character used to end a quotation.
   */
  @Override
  public char getAltQuoteEnd() {

    return this.altQuoteEnd;
  }

  /**
   * This method sets the {@link #getAltQuoteEnd() alt-quote-end} character.
   *
   * @param alternativeQuoteEnd is the {@link #getAltQuoteEnd() alt-quote-end} character.
   */
  public void setAltQuoteEnd(char alternativeQuoteEnd) {

    this.altQuoteEnd = alternativeQuoteEnd;
  }

  /**
   * This method sets both the {@link #getAltQuoteStart() alt-quote-start} and {@link #getAltQuoteEnd() alt-quote-end}
   * character.
   *
   * @param altQuote the alt-quote character to set.
   */
  public void setAltQuote(char altQuote) {

    this.altQuoteStart = altQuote;
    this.altQuoteEnd = altQuote;
  }

  @Override
  public char getAltQuoteEscape() {

    return this.altQuoteEscape;
  }

  /**
   * @param altQuoteEscape is the {@link #getAltQuoteEscape() alt-quote-escape} to set.
   */
  public void setAltQuoteEscape(char altQuoteEscape) {

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
  public char getEntityStart() {

    return this.entityStart;
  }

  /**
   * @param entityStart the {@link #getEntityStart() entity-start} to set.
   */
  public void setEntityStart(char entityStart) {

    this.entityStart = entityStart;
  }

  @Override
  public char getEntityEnd() {

    return this.entityEnd;
  }

  /**
   * @param entityEnd the {@link #getEntityEnd() entity-end} to set.
   */
  public void setEntityEnd(char entityEnd) {

    this.entityEnd = entityEnd;
  }

  /**
   * {@inheritDoc}
   *
   * <b>ATTENTION:</b><br>
   * You need to override this method if you want to {@link #setEntityStart(char) use} entities.
   */
  @Override
  public String resolveEntity(String entity) {

    throw new IllegalArgumentException(entity);
  }
}
