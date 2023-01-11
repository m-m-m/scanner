package io.github.mmm.scanner;

import java.util.function.Function;

import io.github.mmm.base.number.NumberType;

class NumberScannerLong extends NumberScanner<Long> {

  public NumberScannerLong(Function<String, CharStreamScanner> factory) {

    super(NumberType.LONG, factory);
  }

  @Override
  Long doScan(CharStreamScanner scanner, NumericRadixMode radixMode, boolean noSign, Long max) {

    return scanner.readLong(radixMode, noSign, max.longValue());
  }

}
