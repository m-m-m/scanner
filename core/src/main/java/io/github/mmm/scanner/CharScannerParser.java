package io.github.mmm.scanner;

import java.io.Reader;

/// Interface of a parser the can [parse][#parse(String)] a Java object from a [string
 representation][Object#toString()].
///
/// @param <T> type of the java object to parse to.
/// @since 1.0.0
public interface CharScannerParser<T> {

  /// @param data the [string representation][Object#toString()] to parse.
  /// @return the parsed data as Java object.
  default T parse(String data) {

    return parse(new CharSequenceScanner(data));
  }

  /// @param reader the [Reader] with the [string representation][Object#toString()] to parse.
  /// @return the parsed data as Java object.
  default T parse(Reader reader) {

    return parse(new CharReaderScanner(reader));
  }

  /// @param scanner the [CharStreamScanner] with the [string representation][Object#toString()] to parse.
  /// @return the parsed data as Java object.
  public abstract T parse(CharStreamScanner scanner);

}
