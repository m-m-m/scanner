package io.github.mmm.scanner;

import java.util.function.Function;

import io.github.mmm.base.number.NumberType;

class NumberScannerDouble extends NumberScanner<Double> {

  public NumberScannerDouble(Function<String, CharStreamScanner> factory) {

    super(NumberType.DOUBLE, factory);
  }

  @Override
  Double doScan(CharStreamScanner scanner, NumericRadixMode radixMode, boolean noSign, Double max) {

    boolean customRadix = radixMode != NumericRadixMode.ONLY_10;
    return scanner.readDouble(customRadix);
  }

}
