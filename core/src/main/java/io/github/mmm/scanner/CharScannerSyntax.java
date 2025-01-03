/* Copyright (c) The m-m-m Team, Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0 */
package io.github.mmm.scanner;

/**
 * This is the interface used to define the syntax to scan characters.
 *
 * @see CharStreamScanner#readUntil(int, boolean, CharScannerSyntax)
 */
public interface CharScannerSyntax {

  /**
   * This method gets the character used to start a quotation that should be terminated by a {@link #getQuoteEnd()
   * quote-end} character. The text inside the quote is taken as is (without the quote characters). <br>
   * Common examples for quote characters are the single quotes ({@code '}) and double quotes ({@code "}).
   *
   * @return the character used to start a quotation or {@code '\0'} to disable.
   */
  int getQuoteStart();

  /**
   * This method gets the character used to end a quotation.
   *
   * @see #getQuoteStart()
   *
   * @return the character used to end a quotation or {@code '\0'} to disable.
   */
  int getQuoteEnd();

  /**
   * This method gets the character used as escape. It is used to mark special characters like {@link #getQuoteStart()}
   * to allow these characters also in the payload. The escape itself is removed on decoding while the next character is
   * taken as is without any special interpretation. <br>
   * The most common escape character is the backslash ({@code \}). <br>
   * Here are some examples for decoding:
   * <table border="1">
   * <tr>
   * <th>{@link #getEscape() escape}</th>
   * <th>input</th>
   * <th>output</th>
   * </tr>
   * <tr>
   * <td>\</td>
   * <td>a\b\\c</td>
   * <td>ab\c</td>
   * </tr>
   * <tr>
   * <td>~</td>
   * <td>a~b~~~c</td>
   * <td>ab~c</td>
   * </tr>
   * </table>
   *
   * This allows to encode special characters like a {@link CharStreamScanner#readUntil(int, boolean, CharScannerSyntax)
   * stop-character}, {@link #getQuoteStart() quote-start}, {@link #getAltQuoteStart() alt-quote-start}, as well as the
   * {@link #getEscape() escape} itself. <br>
   * <b>ATTENTION:</b><br>
   * The {@link #getEscape() escape} is disabled within {@link #getQuoteStart() quotations}.
   *
   * @see #getEntityStart()
   *
   * @return the escape character or {@code '\0'} for no escaping.
   */
  int getEscape();

  /**
   * This method gets the character used to escape the {@link #getQuoteEnd() quote-end} character within a quotation.
   * This may be the {@link #getQuoteEnd() quote-end} itself so a duplicate {@link #getQuoteEnd() quote-end} represents
   * a single occurrence of that character within a quotation. Otherwise the escape may be any other character. <br>
   * Please note that this escaping is only active within a quotation opened by {@link #getQuoteStart() quote-start} and
   * only escapes the {@link #getQuoteEnd() quote-end} character and nothing else so in any other case the
   * {@link #getQuoteEscape() quote-escape} is treated as a regular character. <br>
   * <table border="1">
   * <tr>
   * <th>{@link #getQuoteStart() quote-start}</th>
   * <th>{@link #getQuoteEnd() quote-end}</th>
   * <th>{@link #getQuoteEscape() quote-escape}</th>
   * <th>input</th>
   * <th>output</th>
   * </tr>
   * <tr>
   * <td>'</td>
   * <td>'</td>
   * <td>'</td>
   * <td>a'bc'd</td>
   * <td>abcd</td>
   * </tr>
   * <tr>
   * <td>'</td>
   * <td>'</td>
   * <td>'</td>
   * <td>a'b''c'd</td>
   * <td>ab'cd</td>
   * </tr>
   * <tr>
   * <td>'</td>
   * <td>'</td>
   * <td>\</td>
   * <td>a'b\c\'d\\'e'f</td>
   * <td>ab\c'd\'ef</td>
   * </tr>
   * </table>
   *
   * @return the character used to escape the {@link #getQuoteEnd() quote-end} character or {@code '\0'} to disable.
   */
  int getQuoteEscape();

  /**
   * If {@link #getQuoteStart() quote-start}, {@link #getQuoteEnd() quote-end} and {@link #getQuoteEscape()
   * quote-escape} all point to the same character (which is NOT {@code '\0'}), then this method determines if
   * {@link #getQuoteEscape() quotation escaping} is <em>lazy</em>. This means that outside a quotation a double
   * occurrence of the quote character is NOT treated as quotation but as escaped quote character. Otherwise if NOT
   * lazy, the double quote character is treated as quotation representing the empty sequence. <br>
   * Here are some examples:
   * <table border="1">
   * <tr>
   * <th>{@link #getQuoteStart() quote-start}</th>
   * <th>{@link #getQuoteEnd() quote-end}</th>
   * <th>{@link #getQuoteEscape() quote-escape}</th>
   * <th>{@link #isQuoteEscapeLazy() quote-escape-lazy}</th>
   * <th>input</th>
   * <th>output</th>
   * </tr>
   * <tr>
   * <td>'</td>
   * <td>'</td>
   * <td>'</td>
   * <td>true</td>
   * <td>''</td>
   * <td>'</td>
   * </tr>
   * <tr>
   * <td>'</td>
   * <td>'</td>
   * <td>'</td>
   * <td>false</td>
   * <td>''</td>
   * <td>&nbsp;</td>
   * </tr>
   * <tr>
   * <td>'</td>
   * <td>'</td>
   * <td>'</td>
   * <td>true</td>
   * <td>''''</td>
   * <td>''</td>
   * </tr>
   * <tr>
   * <td>'</td>
   * <td>'</td>
   * <td>'</td>
   * <td>false</td>
   * <td>''''</td>
   * <td>'</td>
   * </tr>
   * <tr>
   * <td>'</td>
   * <td>'</td>
   * <td>'</td>
   * <td>true</td>
   * <td>'''a'</td>
   * <td>'a</td>
   * </tr>
   * <tr>
   * <td>'</td>
   * <td>'</td>
   * <td>'</td>
   * <td>false</td>
   * <td>'''a'</td>
   * <td>'a</td>
   * </tr>
   * </table>
   * <br>
   * Please note that for {@code '''a'} the complete sequence is treated as quote if {@link #isQuoteEscapeLazy()
   * quote-escape-lazy} is {@code false} and otherwise just the trailing {@code 'a'}.
   *
   * @return {@code true} if quote-escaping is lazy, {@code false} otherwise.
   */
  boolean isQuoteEscapeLazy();

  /**
   * This method gets the alternative character used to start a quotation that should be terminated by a
   * {@link #getAltQuoteEnd() alt-quote-end} character. The text inside the quote is taken as is (without the quote
   * characters).
   *
   * @see #getQuoteStart()
   *
   * @return the alternative character used to start a quotation or {@code '\0'} to disable.
   */
  int getAltQuoteStart();

  /**
   * This method gets the alternative character used to end a quotation.
   *
   * @see #getAltQuoteStart()
   *
   * @return the alternative character used to end a quotation.
   */
  int getAltQuoteEnd();

  /**
   * This method gets the character used to escape the {@link #getAltQuoteEnd() alt-quote-end} character within an
   * quotation opened by {@link #getAltQuoteStart() alt-quote-start}.
   *
   * @see #getQuoteEscape()
   *
   * @return the character used to escape the {@link #getQuoteEnd() quote-end} character or {@code '\0'} to disable.
   */
  int getAltQuoteEscape();

  /**
   * If {@link #getAltQuoteStart() alt-quote-start}, {@link #getAltQuoteEnd() alt-quote-end} and
   * {@link #getAltQuoteEscape() alt-quote-escape} all point to the same character (which is NOT {@code '\0'}), then
   * this method determines if {@link #getAltQuoteEscape() alt-quotation escaping} is <em>lazy</em>.
   *
   * @see #isQuoteEscapeLazy()
   *
   * @return {@code true} if alt-quote-escaping is lazy, {@code false} otherwise.
   */
  boolean isAltQuoteEscapeLazy();

  /**
   * This method gets the character used to start an entity. An entity is a specific encoded string surrounded with
   * {@link #getEntityStart() entity-start} and {@link #getEntityEnd() entity-end}. It will be decoded by
   * {@link #resolveEntity(String)}.
   *
   * @return the character used to start an entity or {@code '\0'} to disable.
   */
  int getEntityStart();

  /**
   * This method gets the character used to end an entity.
   *
   * @see #getEntityStart()
   *
   * @return the character used to end an entity.
   */
  int getEntityEnd();

  /**
   * This method resolves the given {@code entity}. <br>
   * E.g. if {@link #getEntityStart() entity-start} is {@code '&'} and {@link #getEntityEnd()} is {@code ';'} then if
   * the string {@code "&lt;"} is scanned, this method is called with {@code "lt"} as {@code entity} argument and may
   * return {@code "<"}.
   *
   * @param entity is the entity string that was found surrounded by {@link #getEntityStart() entity-start} and
   *        {@link #getEntityEnd() entity-end} excluding these characters.
   * @return the decoded entity.
   */
  String resolveEntity(String entity);

}
