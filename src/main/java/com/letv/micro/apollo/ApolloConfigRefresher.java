package com.letv.micro.apollo;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import com.ctrip.framework.apollo.spring.config.PropertySourcesConstants;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.type.filter.AnnotationTypeFilter;

/**
 * Appolo config context refresher, support application config and public application config updates
 *
 * Created by David.Liu on 2018/8/24.
 */
@Slf4j
public class ApolloConfigRefresher {

  private ContextRefresher contextRefresher;

  private static final Splitter NAMESPACE_SPLITTER = Splitter.on(",").omitEmptyStrings().trimResults();
  private static final String APPLICATION = "application";
  private static final String BASE_PACKGE_FILE = "META-INF/package";
  private static final String BASE_PACKAGE = basePackage();
  private static final String APP_PATTERN = ".*\\.application{1}.*";
  private static final String RESOURCE_PATTERN = "/**/*.class";

  public ApolloConfigRefresher(ContextRefresher contextRefresher, ConfigurableEnvironment environment) {
    this.contextRefresher = contextRefresher;
    ConfigChangeListener configChangeListener = new ConfigChangeListener() {
      @Override
      public void onChange(ConfigChangeEvent changeEvent) {
        refreshConfigProperties(changeEvent);
      }
    };

    String namespaces = environment.getProperty(PropertySourcesConstants.APOLLO_BOOTSTRAP_NAMESPACES, ConfigConsts.NAMESPACE_APPLICATION);
    List<String> namespaceBootList = NAMESPACE_SPLITTER.splitToList(namespaces);
    List<String> namespaceList = getNamespaceAnnotations();
    namespaceList.addAll(namespaceBootList);
    namespaceList = ImmutableSet.copyOf(Iterables.filter(namespaceList, Predicates.not(Predicates.isNull()))).asList();

    for (String namespace : namespaceList) {
      if (APPLICATION.equals(namespace) || namespace.matches(APP_PATTERN)) {
        Config config = ConfigService.getConfig(namespace);
        log.info("Apollo bootstrap namespace: {} add to ChangeListener", namespace);
        config.addChangeListener(configChangeListener);
      }
    }
  }

  private List<String> getNamespaceAnnotations() {
    List<Class<?>> classList = loadAnnotationClasses(EnableApolloConfig.class);

    for (Class<?> clazz : classList) {
      EnableApolloConfig apolloAnnotation = clazz.getAnnotation(EnableApolloConfig.class);
      return Lists.newArrayList(apolloAnnotation.value());
    }
    return Lists.newArrayList();
  }

  private List<Class<?>> loadAnnotationClasses(Class annotation) {
    ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
    scanner.addIncludeFilter(new AnnotationTypeFilter(annotation));
    Set<BeanDefinition> beanDefinitions = scanner.findCandidateComponents(BASE_PACKAGE);
    List<Class<?>> classes = Lists.newArrayListWithCapacity(beanDefinitions.size());
    for (BeanDefinition beanDefinition : beanDefinitions) {
      try {
        Class<?> modelClass = Class.forName(beanDefinition.getBeanClassName());
        log.info("Scan basepackage:{} class:{}", annotation.getName(), modelClass);
        classes.add(modelClass);
      } catch (ClassNotFoundException e) {
        log.error("Scan basepackage:{} class failed", BASE_PACKAGE, e);
      }
    }
    return classes;
  }

  private void refreshConfigProperties(ConfigChangeEvent changeEvent) {
    log.info("Appolo config properties refreshing!");

    Set<String> refreshKeys = this.contextRefresher.refresh();

    log.info("Appolo config has been published. Keys refreshed [{}]", refreshKeys);
  }

  private static String basePackage() {
    StringBuffer result = new StringBuffer();
    try {
      InputStream in = ApolloConfigRefresher.class.getClassLoader().getResourceAsStream(BASE_PACKGE_FILE);
      byte[] bytes = new byte[256];
      int len = 0;
      while ((len = in.read(bytes)) != -1) {
        result.append(new String(bytes, 0, len));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return result.toString();
  }

}