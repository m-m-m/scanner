/* Copyright (c) The m-m-m Team, Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0 */
package io.github.mmm.scanner;

/// This is the interface used to define the syntax to scan characters.
///
/// @see CharStreamScanner#readUntil(int, boolean, CharScannerSyntax)
public interface CharScannerSyntax {

  /// This method gets the character used to start a quotation that should be terminated by a [quote-end][#getQuoteEnd()] character. The text inside the quote is taken as is (without the quote characters).
  /// Common examples for quote characters are the single quotes (`'`) and double quotes (`"`).
  ///
  /// @return the character used to start a quotation or [-1][CharStreamScanner#EOS] to disable.
  int getQuoteStart();

  /// This method gets the character used to end a quotation.
  ///
  /// @see #getQuoteStart()
  ///
  /// @return the character used to end a quotation or [-1][CharStreamScanner#EOS] to disable.
  int getQuoteEnd();

  /// This method gets the character used as escape. It is used to mark special characters like [#getQuoteStart()]
  /// to allow these characters also in the payload. The escape itself is removed on decoding while the next character is
  /// taken as is without any special interpretation.
  /// The most common escape character is the backslash (`\`).
  /// Here are some examples for decoding:
  /// <table border="1">
  /// <tr>
  /// <th>[escape][#getEscape()]</th>
  /// <th>input</th>
  /// <th>output</th>
  /// </tr>
  /// <tr>
  /// <td>\</td>
  /// <td>a\b\\c</td>
  /// <td>ab\c</td>
  /// </tr>
  /// <tr>
  /// <td>~</td>
  /// <td>a~b~~~c</td>
  /// <td>ab~c</td>
  /// </tr>
  /// </table>
  ///
  /// This allows to encode special characters like a [stop-character][CharStreamScanner#readUntil(int, boolean, CharScannerSyntax)], [quote-start][#getQuoteStart()], [alt-quote-start][#getAltQuoteStart()], as well as the
  /// [escape][#getEscape()] itself.
  /// **ATTENTION:**
  /// The [escape][#getEscape()] is disabled within [quotations][#getQuoteStart()].
  ///
  /// @see #getEntityStart()
  ///
  /// @return the escape character or [-1][CharStreamScanner#EOS] for no escaping.
  int getEscape();

  /// This method gets the character used to escape the [quote-end][#getQuoteEnd()] character within a quotation.
  /// This may be the [quote-end][#getQuoteEnd()] itself so a duplicate [quote-end][#getQuoteEnd()] represents
  /// a single occurrence of that character within a quotation. Otherwise the escape may be any other character.
  /// Please note that this escaping is only active within a quotation opened by [quote-start][#getQuoteStart()] and
  /// only escapes the [quote-end][#getQuoteEnd()] character and nothing else so in any other case the
  /// [quote-escape][#getQuoteEscape()] is treated as a regular character.
  /// <table border="1">
  /// <tr>
  /// <th>[quote-start][#getQuoteStart()]</th>
  /// <th>[quote-end][#getQuoteEnd()]</th>
  /// <th>[quote-escape][#getQuoteEscape()]</th>
  /// <th>input</th>
  /// <th>output</th>
  /// </tr>
  /// <tr>
  /// <td>'</td>
  /// <td>'</td>
  /// <td>'</td>
  /// <td>a'bc'd</td>
  /// <td>abcd</td>
  /// </tr>
  /// <tr>
  /// <td>'</td>
  /// <td>'</td>
  /// <td>'</td>
  /// <td>a'b''c'd</td>
  /// <td>ab'cd</td>
  /// </tr>
  /// <tr>
  /// <td>'</td>
  /// <td>'</td>
  /// <td>\</td>
  /// <td>a'b\c\'d\\'e'f</td>
  /// <td>ab\c'd\'ef</td>
  /// </tr>
  /// </table>
  ///
  /// @return the character used to escape the [quote-end][#getQuoteEnd()] character or [-1][CharStreamScanner#EOS] to disable.
  int getQuoteEscape();

  /// If [quote-start][#getQuoteStart()], [quote-end][#getQuoteEnd()] and [quote-escape][#getQuoteEscape()] all point to the same character (which is NOT `'\0'`), then this method determines if
  /// [quotation escaping][#getQuoteEscape()] is _lazy_. This means that outside a quotation a double
  /// occurrence of the quote character is NOT treated as quotation but as escaped quote character. Otherwise if NOT
  /// lazy, the double quote character is treated as quotation representing the empty sequence.
  /// Here are some examples:
  /// <table border="1">
  /// <tr>
  /// <th>[quote-start][#getQuoteStart()]</th>
  /// <th>[quote-end][#getQuoteEnd()]</th>
  /// <th>[quote-escape][#getQuoteEscape()]</th>
  /// <th>[quote-escape-lazy][#isQuoteEscapeLazy()]</th>
  /// <th>input</th>
  /// <th>output</th>
  /// </tr>
  /// <tr>
  /// <td>'</td>
  /// <td>'</td>
  /// <td>'</td>
  /// <td>true</td>
  /// <td>''</td>
  /// <td>'</td>
  /// </tr>
  /// <tr>
  /// <td>'</td>
  /// <td>'</td>
  /// <td>'</td>
  /// <td>false</td>
  /// <td>''</td>
  /// <td> </td>
  /// </tr>
  /// <tr>
  /// <td>'</td>
  /// <td>'</td>
  /// <td>'</td>
  /// <td>true</td>
  /// <td>''''</td>
  /// <td>''</td>
  /// </tr>
  /// <tr>
  /// <td>'</td>
  /// <td>'</td>
  /// <td>'</td>
  /// <td>false</td>
  /// <td>''''</td>
  /// <td>'</td>
  /// </tr>
  /// <tr>
  /// <td>'</td>
  /// <td>'</td>
  /// <td>'</td>
  /// <td>true</td>
  /// <td>'''a'</td>
  /// <td>'a</td>
  /// </tr>
  /// <tr>
  /// <td>'</td>
  /// <td>'</td>
  /// <td>'</td>
  /// <td>false</td>
  /// <td>'''a'</td>
  /// <td>'a</td>
  /// </tr>
  /// </table>
  ///
  /// Please note that for `'''a'` the complete sequence is treated as quote if [quote-escape-lazy][#isQuoteEscapeLazy()] is `false` and otherwise just the trailing `'a'`.
  ///
  /// @return `true` if quote-escaping is lazy, `false` otherwise.
  boolean isQuoteEscapeLazy();

  /// This method gets the alternative character used to start a quotation that should be terminated by a
  /// [alt-quote-end][#getAltQuoteEnd()] character. The text inside the quote is taken as is (without the quote
  /// characters).
  ///
  /// @see #getQuoteStart()
  ///
  /// @return the alternative character used to start a quotation or [-1][CharStreamScanner#EOS] to disable.
  int getAltQuoteStart();

  /// This method gets the alternative character used to end a quotation.
  ///
  /// @see #getAltQuoteStart()
  ///
  /// @return the alternative character used to end a quotation.
  int getAltQuoteEnd();

  /// This method gets the character used to escape the [alt-quote-end][#getAltQuoteEnd()] character within an
  /// quotation opened by [alt-quote-start][#getAltQuoteStart()].
  ///
  /// @see #getQuoteEscape()
  ///
  /// @return the character used to escape the [quote-end][#getQuoteEnd()] character or [-1][CharStreamScanner#EOS] to disable.
  int getAltQuoteEscape();

  /// If [alt-quote-start][#getAltQuoteStart()], [alt-quote-end][#getAltQuoteEnd()] and
  /// [alt-quote-escape][#getAltQuoteEscape()] all point to the same character (which is NOT `'\0'`), then
  /// this method determines if [alt-quotation escaping][#getAltQuoteEscape()] is _lazy_.
  ///
  /// @see #isQuoteEscapeLazy()
  ///
  /// @return `true` if alt-quote-escaping is lazy, `false` otherwise.
  boolean isAltQuoteEscapeLazy();

  /// This method gets the character used to start an entity. An entity is a specific encoded string surrounded with
  /// [entity-start][#getEntityStart()] and [entity-end][#getEntityEnd()]. It will be decoded by
  /// [#resolveEntity(String)].
  ///
  /// @return the character used to start an entity or [-1][CharStreamScanner#EOS] to disable.
  int getEntityStart();

  /// This method gets the character used to end an entity.
  ///
  /// @see #getEntityStart()
  ///
  /// @return the character used to end an entity.
  int getEntityEnd();

  /// @param codePoint the [code-point][String#codePointAt(int)] that was escaped.
  /// @return the mapped [code-point][String#codePointAt(int)]. E.g. if "\\n" was parsed and escape was '\\' then
  ///         this method is called with 'n' as argument and may return '\n'.
  default int mapEscaped(int codePoint) {

    return switch (codePoint) {
      case 'b' -> '\b';
      case 'f' -> '\f';
      case 'n' -> '\n';
      case 'r' -> '\r';
      case 's' -> '\s';
      case 't' -> '\t';
      case '0' -> '\0';
      default -> codePoint;
    };
  }

  /// This method resolves the given `entity`.
  /// E.g. if [entity-start][#getEntityStart()] is `'&'` and [#getEntityEnd()] is `';'` then if
  /// the string `"<"` is scanned, this method is called with `"lt"` as `entity` argument and may
  /// return `"<"`.
  /// **ATTENTION:**
  /// You need to override this method if you want to use entities.
  ///
  /// @param entity is the entity string that was found surrounded by [entity-start][#getEntityStart()] and
  ///        [entity-end][#getEntityEnd()] excluding these characters.
  /// @return the decoded entity.
  default String resolveEntity(String entity) {

    throw new IllegalArgumentException(entity);
  }

  /// @return the [CharScannerSyntax] using single quotes ('\'') with backslash to [escape][#getEscape()].
  static CharScannerSyntax getSingleQuotes() {

    return CharScannerSyntaxType.SINGLE_QUOTE;
  }

  /// @return the [CharScannerSyntax] using double quotes ('"') with backslash to [escape][#getEscape()].
  static CharScannerSyntax getDoubleQuotes() {

    return CharScannerSyntaxType.SINGLE_QUOTE;
  }

  /// @return the [CharScannerSyntax] using both single and double quotes with backslash to [escape][#getEscape()].
  static CharScannerSyntax getBothQuotes() {

    return CharScannerSyntaxType.BOTH_QUOTES;
  }

}
