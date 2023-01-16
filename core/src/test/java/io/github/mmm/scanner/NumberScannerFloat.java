package io.github.mmm.scanner;

import java.util.function.Function;

import io.github.mmm.base.number.NumberType;
import io.github.mmm.scanner.number.CharScannerRadixHandler;

class NumberScannerFloat extends NumberScanner<Float> {

  public NumberScannerFloat(Function<String, CharStreamScanner> factory) {

    super(NumberType.FLOAT, factory);
  }

  @Override
  Float scan(CharStreamScanner scanner, CharScannerRadixHandler radixMode) {

    return scanner.readFloat(radixMode);
  }

}
