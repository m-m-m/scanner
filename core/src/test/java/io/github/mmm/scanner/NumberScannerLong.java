package io.github.mmm.scanner;

import java.util.function.Function;

import io.github.mmm.base.number.NumberType;
import io.github.mmm.scanner.number.CharScannerRadixHandler;

class NumberScannerLong extends NumberScanner<Long> {

  public NumberScannerLong(Function<String, CharStreamScanner> factory) {

    super(NumberType.LONG, factory);
  }

  @Override
  Long scan(CharStreamScanner scanner, CharScannerRadixHandler radixMode) {

    return scanner.readLong(radixMode);
  }

}
