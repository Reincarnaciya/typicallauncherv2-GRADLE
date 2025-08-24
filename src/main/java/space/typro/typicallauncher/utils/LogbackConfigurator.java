package space.typro.typicallauncher.utils;

import space.typro.directorymanager.DirectoryManager;
import ch.qos.logback.core.Context;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
public class LogbackConfigurator {


    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private static final String LOG_PATTERN = "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n";

    public static void configure() {
        try {
            Path logDir = DirectoryManager.logDir.getDir().toPath();
            Path logFile = logDir.resolve("launcher_" +
                    LocalDateTime.now(ZoneId.of("Europe/Moscow")).format(FILE_DATE_FORMAT) + ".log");

            System.setProperty("logback.configurationFile", "");

            ch.qos.logback.classic.Logger rootLogger =
                    (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);

            rootLogger.detachAndStopAllAppenders();

            ch.qos.logback.core.ConsoleAppender<ch.qos.logback.classic.spi.ILoggingEvent> consoleAppender =
                    new ch.qos.logback.core.ConsoleAppender<>();
            consoleAppender.setContext(rootLogger.getLoggerContext());
            consoleAppender.setName("CONSOLE");
            consoleAppender.setEncoder(getPatternLayoutEncoder(rootLogger.getLoggerContext()));
            consoleAppender.start();

            ch.qos.logback.core.FileAppender<ch.qos.logback.classic.spi.ILoggingEvent> fileAppender =
                    new ch.qos.logback.core.FileAppender<>();
            fileAppender.setContext(rootLogger.getLoggerContext());
            fileAppender.setName("FILE");
            fileAppender.setFile(logFile.toString());
            fileAppender.setEncoder(getPatternLayoutEncoder(rootLogger.getLoggerContext()));
            fileAppender.start();

            rootLogger.addAppender(consoleAppender);
            rootLogger.addAppender(fileAppender);
            rootLogger.setLevel(ch.qos.logback.classic.Level.INFO);

        } catch (Exception e) {
            System.err.println("Failed to configure logger: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static ch.qos.logback.core.encoder.LayoutWrappingEncoder<ch.qos.logback.classic.spi.ILoggingEvent>
    getPatternLayoutEncoder(Context context) {

        ch.qos.logback.classic.PatternLayout layout = new ch.qos.logback.classic.PatternLayout();
        layout.setPattern(LogbackConfigurator.LOG_PATTERN);
        layout.setContext(context);
        layout.start();

        ch.qos.logback.core.encoder.LayoutWrappingEncoder<ch.qos.logback.classic.spi.ILoggingEvent> encoder =
                new ch.qos.logback.core.encoder.LayoutWrappingEncoder<>();
        encoder.setLayout(layout);
        return encoder;
    }
}