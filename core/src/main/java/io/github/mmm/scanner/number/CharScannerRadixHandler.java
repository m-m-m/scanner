package io.github.mmm.scanner.number;

/// Interface for [radix handling][#radix(int, char)].
public interface CharScannerRadixHandler {

  /// This method will be called if the first character is '0' and will also pass a lookahead of the next character as
  /// parameter `symbol`.
  ///
  /// @param radix the radix. Will be `16` for "0x", `2` for "0b", `8` for "0" followed by an octal
  ///        digit (0-7), and `0` in case an unexpected character was found after the first zero.
  /// @param symbol the character followed by the leading zero. E.g. 'x' or 'X' for radix 16. You could even implement
  ///        custom radix mode like "0o" for octal instead of "0" by returning `8` if `symbol` is 'o' or 'O'.
  /// @return the actual radix to use for further processing. If `0` (or less) is returned the radix is not
  ///         accepted and "0" is treated as a leading zero rather than a prefix of the radix that will remain to be
  ///         `10`. Typically implementations will return the given `radix`, but to prevent octal parsing due
  ///         to a leading zero (Java/C legacy) you can return `10` if `8` was given. If `8` is
  ///         returned, the symbol will be consumed and skipped if it is not a digit while otherwise the digit will be
  ///         consumed as part of the number even if it is greater than 7 (finally leading to a
  ///         [NumberFormatException]).
  int radix(int radix, char symbol);

}
