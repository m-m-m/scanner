package io.github.mmm.scanner;

import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.event.Level;

import io.github.mmm.base.text.AbstractTextFormatMessageHandler;
import io.github.mmm.base.text.TextFormatMessage;
import io.github.mmm.base.text.TextFormatMessageHandler;
import io.github.mmm.base.text.TextFormatMessageType;

/// Default implementation of [TextFormatMessageHandler].
@SuppressWarnings("exports")
public class SimpleTextFormatMessageHandler extends AbstractTextFormatMessageHandler {

  /// Maps each [TextFormatMessageType] to its analog [Level].
  public static final Function<TextFormatMessageType, Level> LOG_MAPPER_DEFAULT = new LogLevelMapper(false);

  /// Like [#LOG_MAPPER_DEFAULT] but maps [info messages][TextFormatMessageType#INFO] to log-level
  /// [debug][Level#DEBUG].
  public static final Function<TextFormatMessageType, Level> LOG_MAPPER_INFO_AS_DEBUG = new LogLevelMapper(true);

  private static final SimpleTextFormatMessageHandler INSTANCE = new SimpleTextFormatMessageHandler(false,
      AbstractCharStreamScanner.LOG, LOG_MAPPER_INFO_AS_DEBUG, false);

  private final Logger logger;

  private final Function<TextFormatMessageType, Level> logLevelMapper;

  /// The constructor.
  ///
  /// @param throwOnError - `true` to throw a [RuntimeException] when an [error][TextFormatMessageType#ERROR] [message][TextFormatMessage] is [handled][#handle(TextFormatMessage)].
  /// @param logger the [Logger] used to log [TextFormatMessage]s. May be `null` to omit logging.
  /// @param logLevelMapper the [Function] to map [TextFormatMessageType] to the according log [Level].
  /// @param collectMessages the flag for [#isCollectMessages()].
  public SimpleTextFormatMessageHandler(boolean throwOnError, Logger logger,
      Function<TextFormatMessageType, Level> logLevelMapper, boolean collectMessages) {

    super(throwOnError, collectMessages);
    this.logger = logger;
    this.logLevelMapper = logLevelMapper;
  }

  private void log(TextFormatMessage message) {

    if (this.logger == null) {
      return;
    }
    Level level = this.logLevelMapper.apply(message.getType());
    if (level != null) {
      String logMsg = "At line {} in column {}: {}";
      // Workaround for https://github.com/konsoletyper/teavm/issues/918
      // this.logger.atLevel(level).log(logMsg, message.getLine(), message.getColumn(), message.getText());
      switch (level) {
        case INFO:
          this.logger.info(logMsg, message.getLine(), message.getColumn(), message.getText());
          break;
        case WARN:
          this.logger.warn(logMsg, message.getLine(), message.getColumn(), message.getText());
          break;
        case ERROR:
          this.logger.error(logMsg, message.getLine(), message.getColumn(), message.getText());
          break;
        default:
          this.logger.warn(logMsg, message.getLine(), message.getColumn(), message.getText());
          break;
      }
    }
  }

  @Override
  public TextFormatMessage handle(TextFormatMessage message) {

    log(message);
    return super.handle(message);
  }

  private static final class LogLevelMapper implements Function<TextFormatMessageType, Level> {

    private final boolean logInfoOnDebug;

    private LogLevelMapper(boolean logInfoOnDebug) {

      super();
      this.logInfoOnDebug = logInfoOnDebug;
    }

    @Override
    public Level apply(TextFormatMessageType t) {

      switch (t) {
        case ERROR:
          return Level.ERROR;
        case WARNING:
          return Level.WARN;
        case INFO:
          if (this.logInfoOnDebug) {
            return Level.DEBUG;
          }
          return Level.INFO;
      }
      return null;
    }
  }

  /// @return the singleton instance.
  public static SimpleTextFormatMessageHandler get() {

    return INSTANCE;
  }

  /// @return a [SimpleTextFormatMessageHandler] that throws an exception in case of a
  ///         [error][TextFormatMessageType#ERROR] [message][TextFormatMessage] and collects and logs other
  ///         messages.
  public static SimpleTextFormatMessageHandler ofThrowErrors() {

    return new SimpleTextFormatMessageHandler(true, AbstractCharStreamScanner.LOG, LOG_MAPPER_INFO_AS_DEBUG, true);
  }

  /// @return a [SimpleTextFormatMessageHandler] that throws an exception in case of a
  ///         [error][TextFormatMessageType#ERROR] [message][TextFormatMessage] and collects other messages
  ///         without any logging.
  public static SimpleTextFormatMessageHandler ofThrowErrorsNoLogging() {

    return new SimpleTextFormatMessageHandler(true, null, null, true);
  }
}
