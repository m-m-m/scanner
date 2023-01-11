package io.github.mmm.scanner;

import java.util.function.Function;

import io.github.mmm.base.number.NumberType;

class NumberScannerInteger extends NumberScanner<Integer> {

  public NumberScannerInteger(Function<String, CharStreamScanner> factory) {

    super(NumberType.INTEGER, factory);
  }

  @Override
  Integer doScan(CharStreamScanner scanner, NumericRadixMode radixMode, boolean noSign, Integer max) {

    return scanner.readInteger(radixMode, noSign, max.intValue());
  }

}
