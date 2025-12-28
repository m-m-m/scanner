/* Copyright (c) The m-m-m Team, Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0 */
package io.github.mmm.scanner;

import java.io.StringReader;

/**
 * Test of {@link CharReaderScanner}.
 */
class CharReaderScannerTest extends AbstractCharStreamScannerTest {

  private static final CharReaderScanner SCANNER = new CharReaderScanner(1, HANDLER);

  @Override
  protected CharStreamScanner scanner(String string, int capacity) {

    StringReader reader = new StringReader(string);
    if (capacity != 1) {
      return new CharReaderScanner(capacity, HANDLER, reader);
    }
    SCANNER.setReader(reader);
    return SCANNER;
  }

}
