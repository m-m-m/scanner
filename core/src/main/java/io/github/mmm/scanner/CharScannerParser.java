package io.github.mmm.scanner;

import java.io.Reader;

/**
 * Interface of a parser the can {@link #parse(String) parse} a Java object from a {@link Object#toString() string
 * representation}.
 *
 * @param <T> type of the java object to parse to.
 * @since 1.0.0
 */
public interface CharScannerParser<T> {

  /**
   * @param data the {@link Object#toString() string representation} to parse.
   * @return the parsed data as Java object.
   */
  default T parse(String data) {

    return parse(new CharSequenceScanner(data));
  }

  /**
   * @param reader the {@link Reader} with the {@link Object#toString() string representation} to parse.
   * @return the parsed data as Java object.
   */
  default T parse(Reader reader) {

    return parse(new CharReaderScanner(reader));
  }

  /**
   * @param scanner the {@link CharStreamScanner} with the {@link Object#toString() string representation} to parse.
   * @return the parsed data as Java object.
   */
  public abstract T parse(CharStreamScanner scanner);

}
