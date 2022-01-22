/*
 * Copyright (c) The m-m-m Team, Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
/**
 * Provides scanners that help to parse character sequences efficient and easily.<br>
 * <h2>Scanner API</h2>
 *
 * For efficient parsers of complex grammars it is best practice to use a parser generator like {@code javacc} or
 * {@code antlr}. <br>
 * However in some situations it is more suitable to write a handwritten parser. The tradeoff is that this may result in
 * ugly monolithic code that is hard to maintain. <br>
 * The {@link io.github.mmm.scanner.CharStreamScanner} is an interface that covers typical tasks when paring strings or
 * streams and therefore makes your life a lot easier. You can concentrate on the syntax you want to parse and do NOT
 * need to repeat checks if the end is already reached all the time. For parsing enitre streams (e.g. from a
 * {@link java.io.Reader}) there is the implementation {@link io.github.mmm.scanner.CharReaderScanner} while for simple
 * {@link java.lang.String}s there is the implementation {@link io.github.mmm.scanner.CharSequenceScanner}. In any case
 * the entire data and state (parsing position) is encapsulated so you can easily delegate a step to another method or
 * class. Otherwise you would need to pass the current position to that method and return the new one from there. This
 * is tricky if the method should already return something else. <br>
 * As a motivation and anti-pattern, here is a little example of an entirely handwritten parser:
 *
 * <pre>
 * String input = getInputString();
 * int i = 0;
 * boolean colonFound = false;
 * while (i {@literal <} input.length()) {
 *   char c = input.charAt(i++);
 *   if (c == ':') {
 *     colonFound = true;
 *     break;
 *   }
 * }
 * if (!colonFound) {
 *   throw new IllegalArgumentException("Expected character ':' not found!");
 * }
 * String key = input.substring(0, i - 1);
 * String value = null;
 * if (i {@literal <} input.length()) {
 *   while ((i {@literal <} input.length()) {@literal &&} (input.charAt(i) == ' ')) {
 *     i++;
 *   }
 *   int start = i;
 *   while (i {@literal <} input.length()) {
 *     char c = input.charAt(i);
 *     if ((c {@literal <} '0') || (c {@literal >} '9')) {
 *       break;
 *     }
 *     i++;
 *   }
 *   value = input.substring(start, i);
 * }
 * </pre>
 *
 * Here is the same thing when using {@link io.github.mmm.scanner.CharSequenceScanner}:
 *
 * <pre>
 * String input = getInputString();
 * {@link io.github.mmm.scanner.CharStreamScanner} scanner = new {@link io.github.mmm.scanner.CharSequenceScanner}(input);
 * String key = scanner.{@link io.github.mmm.scanner.CharStreamScanner#readUntil(char, boolean) readUntil}(':', false);
 * if (key == null) {
 *   throw new IllegalArgumentException("Expected character ':' not found!");
 * }
 * scanner.{@link io.github.mmm.scanner.CharStreamScanner#skipWhile(char) skipWhile}(' ');
 * String value = scanner.{@link io.github.mmm.scanner.CharStreamScanner#readWhile(io.github.mmm.base.filter.CharFilter)
 * readWhile}({@link io.github.mmm.base.filter.CharFilter#LATIN_DIGIT});
 * </pre>
 *
 * This is just a simple example. The API offers all real-live scenarios you will need to parse your data. The
 * implementations are highly efficient and internally directly operate on {@code char[]}. Streaming implementations use
 * optimized lookahead buffers that can even be configured at construction time.
 */
module io.github.mmm.scanner {

  requires transitive io.github.mmm.base;

  requires org.slf4j;

  exports io.github.mmm.scanner;
}
