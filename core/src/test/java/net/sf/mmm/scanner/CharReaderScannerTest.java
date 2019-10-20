/* Copyright (c) The m-m-m Team, Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0 */
package net.sf.mmm.scanner;

import java.io.StringReader;

/**
 * Test of {@link CharReaderScanner}.
 */
public class CharReaderScannerTest extends AbstractCharStreamScannerTest {

  private static final CharReaderScanner SCANNER = new CharReaderScanner(1);

  @Override
  protected CharStreamScanner scanner(String string, boolean lookahead) {

    StringReader reader = new StringReader(string);
    if (lookahead) {
      return new CharReaderScanner(1024, reader);
    }
    SCANNER.setReader(reader);
    return SCANNER;
  }

}
