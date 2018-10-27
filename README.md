# micro-apollo-spring-boot-starter

> Spring Boot版本使用1.5.10.RELEASE <br/>
Spring Cloud版本使用Edgware.SR3

1)下载该代码
2)META-INF/package文件修改扫码工程包，默认是com.letv
3)通过maven命令打成jar包，建议上传(deploy)到自己公司的私服

##### 使用方式：<br/>
Spring Cloud微服务项目在pom.xml中引入如下依赖：
```
<dependency>
 <groupId>com.letv.micro.apollo</groupId>
 <artifactId>micro-apollo-spring-boot-starter</artifactId>
 <version>1.0-SNAPSHOT</version>
</dependency>
```

##### 功能实现：<br/>
对Spring Cloud配置刷新机制集成Apollo客户端做了进一步封装. <br/>
1、在Apollo配置中心发布配置后，微服务应用客户端监听配置变更，包括默认的配置和公共的配置，通过ContextRefresher中的refresh()方法完成应用环境上下文的配置刷新。<br/>
2、支持对日志级别和日志路径文件的动态配置变更。[Apollo Client无法很好的支持日志级别和日志路径文件的变更，因日志的LoggingApplicationListener加载优先级高，Apollo配置加载滞后。<br/>

引入micro-apollo-spring-boot-starter之后，如果你的项目使用了spring cloud config，那么可以将spring-cloud-stater-config依赖从pom.xml中去掉了。


##### Apollo配置中心公共配置命名规范: <br/>
公共配置建议统一放到新创建的项目中，该项目中存放Spring Cloud相关的公共组件的配置，比如Eureka、Zipkin、Stream等配置。
创建项目时，选择的部门如「微服务平台(dpms)」<br/>
###### 公共配置命名规则：<br/>
{部门前缀}.application  或者 {部门前缀}.application-{具体的细分配置}
当Apollo配置发布后，若需让Spring Cloud配置实现动态加载，公共配置命名必须以application关键字开头，在上述依赖的jar包中会对这类命名的Namespace做配置变更监听。
例如：<br/>
dpms.application-eureka存放eureka相关配置 <br/>
或 dpms.application-zipkin 存放zipkin相关配置 <br/>
或 dpms.application  存放Spring Cloud所有的公共相关配置 <br/>
其他微服务应用关联公共配置后，默认使用的公共配置项。<br/>
你也可以对公共配置所有参数做覆盖，覆盖后优先获取本项目中的配置，这个特性在Apolo的公共配置界面能够直观的展示出来。

Apollo配置中心文档可以参考：https://github.com/ctripcorp/apollo/wiki
