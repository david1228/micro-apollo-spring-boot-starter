package com.letv.micro.apollo;

import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import java.util.Set;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Dynamic configuration changed to log level ( logging.level. )
 *
 * Created by David.Liu on 2018/9/14.
 */
@Slf4j
public class LoggerLevelRefresher implements ApplicationContextAware{
  private ApplicationContext applicationContext;

  @Autowired
  private ContextRefresher contextRefresher;

  @PostConstruct
  public void initialize() {
    refreshLoggingLevels(ConfigService.getConfig("application").getPropertyNames());
  }

  @ApolloConfigChangeListener
  private void onChange(ConfigChangeEvent changeEvent) {
    refreshLoggingLevels(changeEvent.changedKeys());
  }

  private void refreshLoggingLevels(Set<String> changedKeys) {
    boolean loggingLevelChanged = false;
    for (String changedKey : changedKeys) {
      if (changedKey.startsWith("logging.level.")) {
        loggingLevelChanged = true;
        break;
      }
    }

    if (loggingLevelChanged) {
      log.info("Refreshing logging levels");

      /**
       * refresh logging levles
       * @see org.springframework.cloud.logging.LoggingRebinder#onApplicationEvent
       */
      this.applicationContext.publishEvent(new EnvironmentChangeEvent(changedKeys));
      log.info("Logging levels refreshed");
    }
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }
}
