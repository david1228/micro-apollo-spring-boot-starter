package com.letv.micro.apollo;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 *
 * Microservice apollo auto configuration for spring boot application
 *
 * Created by David.Liu on 2018/8/27.
 */
@Configuration
@ConditionalOnProperty(value = {"micro.apollo.config.enabled"}, havingValue = "true", matchIfMissing = true)
public class MicroApolloAutoConfiguration {

  @Configuration
  @ConditionalOnClass(ContextRefresher.class)
  @ConditionalOnBean(ContextRefresher.class)
  protected static class MicroAppoloRefresherConfiguration {
    @Bean
    public ApolloConfigRefresher apolloConfigRefresher(ContextRefresher contextRefresher, ConfigurableEnvironment environment) {
      return new ApolloConfigRefresher(contextRefresher, environment);
    }

    @Bean
    public LoggerLevelRefresher loggerLevelRefresher() {
      return new LoggerLevelRefresher();
    }

    @Bean
    public LoggerFileRefresher loggerFileRefresher() {
      return new LoggerFileRefresher();
    }
  }
}
