package io.github.mmm.scanner;

import java.util.function.Function;

import io.github.mmm.base.number.NumberType;
import io.github.mmm.scanner.number.CharScannerRadixHandler;

class NumberScannerInteger extends NumberScanner<Integer> {

  public NumberScannerInteger(Function<String, CharStreamScanner> factory) {

    super(NumberType.INTEGER, factory);
  }

  @Override
  Integer scan(CharStreamScanner scanner, CharScannerRadixHandler radixMode) {

    return scanner.readInteger(radixMode);
  }

}
