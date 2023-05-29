package io.github.mmm.scanner;

import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.event.Level;

import io.github.mmm.base.text.AbstractTextFormatMessageHandler;
import io.github.mmm.base.text.TextFormatMessage;
import io.github.mmm.base.text.TextFormatMessageHandler;
import io.github.mmm.base.text.TextFormatMessageType;

/**
 * Default implementation of {@link TextFormatMessageHandler}.
 */
@SuppressWarnings("exports")
public class SimpleTextFormatMessageHandler extends AbstractTextFormatMessageHandler {

  /** Maps each {@link TextFormatMessageType} to its analog {@link Level}. */
  public static final Function<TextFormatMessageType, Level> LOG_MAPPER_DEFAULT = new LogLevelMapper(false);

  /**
   * Like {@link #LOG_MAPPER_DEFAULT} but maps {@link TextFormatMessageType#INFO info messages} to log-level
   * {@link Level#DEBUG debug}.
   */
  public static final Function<TextFormatMessageType, Level> LOG_MAPPER_INFO_AS_DEBUG = new LogLevelMapper(true);

  private static final SimpleTextFormatMessageHandler INSTANCE = new SimpleTextFormatMessageHandler(false,
      AbstractCharStreamScanner.LOG, LOG_MAPPER_INFO_AS_DEBUG, false);

  private final Logger logger;

  private final Function<TextFormatMessageType, Level> logLevelMapper;

  /**
   * The constructor.
   *
   * @param throwOnError - {@code true} to throw a {@link RuntimeException} when an {@link TextFormatMessageType#ERROR
   *        error} {@link TextFormatMessage message} is {@link #handle(TextFormatMessage) handled}.
   * @param logger the {@link Logger} used to log {@link TextFormatMessage}s. May be {@code null} to omit logging.
   * @param logLevelMapper the {@link Function} to map {@link TextFormatMessageType} to the according log {@link Level}.
   * @param collectMessages the flag for {@link #isCollectMessages()}.
   */
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
      this.logger.atLevel(level).log("At line {} in column {}: {}", message.getLine(), message.getColumn(),
          message.getText());
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

  /**
   * @return the singleton instance.
   */
  public static SimpleTextFormatMessageHandler get() {

    return INSTANCE;
  }

  /**
   * @return a {@link SimpleTextFormatMessageHandler} that throws an exception in case of a
   *         {@link TextFormatMessageType#ERROR error} {@link TextFormatMessage message} and collects and logs other
   *         messages.
   */
  public static SimpleTextFormatMessageHandler ofThrowErrors() {

    return new SimpleTextFormatMessageHandler(true, AbstractCharStreamScanner.LOG, LOG_MAPPER_INFO_AS_DEBUG, true);
  }

  /**
   * @return a {@link SimpleTextFormatMessageHandler} that throws an exception in case of a
   *         {@link TextFormatMessageType#ERROR error} {@link TextFormatMessage message} and collects other messages
   *         without any logging.
   */
  public static SimpleTextFormatMessageHandler ofThrowErrorsNoLogging() {

    return new SimpleTextFormatMessageHandler(true, null, null, true);
  }
}
