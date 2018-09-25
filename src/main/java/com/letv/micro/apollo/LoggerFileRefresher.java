package com.letv.micro.apollo;

import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import java.util.Set;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogFile;
import org.springframework.boot.logging.LoggingInitializationContext;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.ResourceUtils;

/**
 *
 * Dynamic configuration changed to log paths ( logging.file or logging.path )
 *
 * Created by David.Liu on 2018/9/14.
 */
@Slf4j
public class LoggerFileRefresher {

  @Autowired
  private ConfigurableEnvironment environment;

  @Autowired
  private LoggingSystem loggingSystem;

  @PostConstruct
  public void initialize() {
    refreshLoggerFile(ConfigService.getConfig("application").getPropertyNames());
  }

  @ApolloConfigChangeListener
  private void onChange(ConfigChangeEvent changeEvent) {
    refreshLoggerFile(changeEvent.changedKeys());
  }

  private void refreshLoggerFile(Set<String> changedKeys) {
    boolean loggerFileChanged = false;
    for (String changedKey : changedKeys) {
      if (changedKey.startsWith("logging.path") || changedKey.startsWith("logging.file")) {
        loggerFileChanged = true;
        break;
      }
    }

    if (loggerFileChanged) {
      log.info("Refreshing logging file or path");
      reinitializeLogging();
      log.info("Logging file or path refreshed");
    }
  }

  private void reinitializeLogging() {
    LogFile logFile = LogFile.get(environment);
    if (logFile != null) {
      logFile.applyToSystemProperties();
    }
    String logConfig = environment.resolvePlaceholders("${logging.config:}");
    try {
      ResourceUtils.getURL(logConfig).openStream().close();
      // Three step initialization that accounts for the clean up of the logging
      // context before initialization. Spring Boot doesn't initialize a logging
      // system that hasn't had this sequence applied (since 1.4.1).
      loggingSystem.cleanUp();
      loggingSystem.beforeInitialize();
      loggingSystem.initialize(new LoggingInitializationContext(environment), logConfig, logFile);
    } catch (Exception ex) {
      log.warn("Logging config file location '{}' cannot be opened and will be ignored", logConfig, ex);
    }
  }

}
