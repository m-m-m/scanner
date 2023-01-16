package io.github.mmm.scanner;

import java.util.function.Function;

import io.github.mmm.base.number.NumberType;
import io.github.mmm.scanner.number.CharScannerRadixHandler;

class NumberScannerDouble extends NumberScanner<Double> {

  public NumberScannerDouble(Function<String, CharStreamScanner> factory) {

    super(NumberType.DOUBLE, factory);
  }

  @Override
  Double scan(CharStreamScanner scanner, CharScannerRadixHandler radixMode) {

    return scanner.readDouble(radixMode);
  }

}
