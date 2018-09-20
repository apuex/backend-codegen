package com.github.apuex.springbootsolution.codegen

import java.io.{File, PrintWriter}
import java.util.Locale

import com.github.apuex.springbootsolution.runtime.SymbolConverters._

import scala.xml.{Node, Text}

object App extends App {
  val xml = ModelLoader(args(0)).xml
  val modelName = xml.attribute("name").asInstanceOf[Some[Text]].get.data
  val modelPackage = xml.attribute("package").asInstanceOf[Some[Text]].get.data
  val projectRoot = s"${System.getProperty("project.root", "target/generated")}"
  val projectDir = s"${projectRoot}/${cToShell(modelName)}/${cToShell(modelName)}-app"
  val srcDir = s"${projectDir}/src/main/java/${modelPackage.replace('.', '/')}/app"
  val resourcesDir = s"${projectDir}/src/main/resources"
  val webappDir = s"${projectDir}/src/main/webapp"

  new File(srcDir).mkdirs()
  new File(resourcesDir).mkdirs()
  new File(s"${webappDir}/META-INF").mkdirs()

  project

  application
  applicationProperties
  tomcatContext
  appConfig

  private def applicationProperties = {
    val printWriter = new PrintWriter(s"${resourcesDir}/application.properties", "utf-8")

    val source =
      s"""# data source and mq configurations.
         |spring.datasource.url=jdbc:mysql://localhost:3306/example?useSSL=false
         |spring.datasource.username=example
         |spring.datasource.password=password
         |spring.datasource.hikari.connection-test-query=SELECT 1
         |
         |# disable jmx if deployed in standalone tomcat instance
         |spring.jmx.enabled=false
         |# disable datasource auto-configuration if using jndi datasouce
         |# and deployed in standalone tomcat instance
         |#spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
         |""".stripMargin

     printWriter.print(source)

     printWriter.close()
  }

  private def tomcatContext = {
    val printWriter = new PrintWriter(s"${webappDir}/META-INF/context.xml", "utf-8")

    val source =
      s"""<?xml version="1.0" encoding="UTF-8"?>
         |<Context antiJARLocking="true" path="" reloadable="true" crossContext="true">
         |
         |  <Resource name="jdbc/example"
         |            type="javax.sql.DataSource"
         |            auth="Container"
         |            driverClassName="net.sourceforge.jtds.jdbc.Driver"
         |            url="jdbc:jtds:sqlserver://192.168.0.38:1433/example"
         |            username="sa"
         |            password=""
         |            validationQuery="SELECT 1"
         |            maxTotal="100"
         |            maxIdle="30"
         |            maxWaitMillis="10000"
         |  />
         |
         |</Context>
       """.stripMargin
    printWriter.print(source)

    printWriter.close()
  }

  private def appConfig = {
    val printWriter = new PrintWriter(s"${resourcesDir}/app-config.xml", "utf-8")

    val source =
      s"""<?xml version="1.0" encoding="UTF-8"?>
         |<beans xmlns="http://www.springframework.org/schema/beans"
         |  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:jee="http://www.springframework.org/schema/jee"
         |  xmlns:integration="http://www.springframework.org/schema/integration"
         |  xmlns:websocket="http://www.springframework.org/schema/websocket"
         |  xsi:schemaLocation="http://www.springframework.org/schema/beans
         |    http://www.springframework.org/schema/beans/spring-beans.xsd
         |    http://www.springframework.org/schema/jee
         |    http://www.springframework.org/schema/jee/spring-jee.xsd
         |    http://www.springframework.org/schema/integration
         |    http://www.springframework.org/schema/integration/spring-integration.xsd
         |    http://www.springframework.org/schema/websocket
         |    http://www.springframework.org/schema/websocket/spring-websocket.xsd">
         |
         |  <bean id="protobufHttpMessageConverter" class="org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter"/>
         |
         |  <jee:jndi-lookup id="dbDataSource" jndi-name="jdbc/example"
         |    expected-type="javax.sql.DataSource" />
         |
         |  <bean id="jdbcTemplate" class="org.springframework.jdbc.core.JdbcTemplate">
         |    <property name="dataSource" ref="dbDataSource"/>
         |  </bean>
         |
         |  <bean id="jmsConnectionFactory" class="org.springframework.jms.connection.CachingConnectionFactory">
         |    <property name="targetConnectionFactory">
         |      <bean class="com.github.apuex.jms.SunConnectionFactory">
         |        <property name="configuration">
         |          <props>
         |            <prop key="imqConfiguredClientID">${cToShell(modelName)}-app</prop>
         |            <prop key="imqBrokerHostName">192.168.0.166</prop>
         |            <prop key="imqBrokerHostPort">7676</prop>
         |            <prop key="imqDefaultUsername">admin</prop>
         |            <prop key="imqDefaultPassword">admin</prop>
         |            <prop key="imqReconnectEnabled">true</prop>
         |            <prop key="imqReconnectInterval">3000</prop>
         |            <prop key="imqReconnectAttempts">1000000</prop>
         |          </props>
         |        </property>
         |      </bean>
         |    </property>
         |    <property name="sessionCacheSize" value="10"/>
         |    <property name="cacheConsumers" value="false"/>
         |  </bean>
         |
         |  <bean id="${cToCamel(modelName)}EventNotifyTopic" class="com.sun.messaging.BasicTopic">
         |    <constructor-arg value="${modelName.toUpperCase(Locale.ENGLISH)}_EVENT_NOTIFY_TOPIC"/>
         |  </bean>
         |
         |  <bean id="${cToCamel("other_system")}EventNotifyTopic" class="com.sun.messaging.BasicTopic">
         |    <constructor-arg value="${"other_system".toUpperCase(Locale.ENGLISH)}_EVENT_NOTIFY_TOPIC"/>
         |  </bean>
         |
         |  <bean id="jmsProtobufConverter" class="com.github.apuex.protobuf.jms.ProtoJmsMessageConverter">
         |    <property name="protobufDescriptors">
         |      <list>
         |        <value>/protobuf/descriptor-sets/${cToShell(modelName)}-message-1.0-SNAPSHOT.protobin</value>
         |      </list>
         |    </property>
         |  </bean>
         |
         |  <bean id="eventNotifyTemplate" class="org.springframework.jms.core.JmsTemplate">
         |    <property name="connectionFactory" ref="jmsConnectionFactory"/>
         |    <property name="defaultDestination" ref="${cToCamel(modelName)}EventNotifyTopic" />
         |    <property name="messageConverter" ref="jmsProtobufConverter" />
         |  </bean>
         |
         |  <bean id="eventSourceAdapter" class="com.github.apuex.eventsource.jms.EventSourceJmsAdapter">
         |    <constructor-arg ref="eventNotifyTemplate"/>
         |  </bean>
         |
         |  <bean id="messageListenerDelegate" class="com.wincom.mstar.pe.integration.ProtobufMessageListenerDelegate"/>
         |
         |  <bean id="messageListenerAdapter" class="org.springframework.jms.listener.adapter.MessageListenerAdapter">
         |    <constructor-arg ref="messageListenerDelegate"/>
         |    <property name="messageConverter" ref="jmsProtobufConverter"/>
         |  </bean>
         |
         |  <bean id="taskScheduler" class="org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler">
         |    <property name="poolSize" value="8"/>
         |    <property name="threadNamePrefix" value="message-listener"/>
         |  </bean>
         |
         |  <bean id="jmsMessageListenerContainer" class="org.springframework.jms.listener.DefaultMessageListenerContainer">
         |    <property name="connectionFactory" ref="jmsConnectionFactory"/>
         |    <property name="destination" ref="${cToCamel("other_system")}EventNotifyTopic"/>
         |    <property name="messageListener" ref="messageListenerAdapter"/>
         |    <property name="taskExecutor" ref="taskScheduler"/>
         |    <property name="subscriptionName" value="data-integration-subscription"/>
         |    <property name="clientId" value="${cToShell(modelName)}-app"/>
         |    <property name="subscriptionDurable" value="true"/>
         |    <property name="subscriptionShared" value="true"/>
         |  </bean>
         |</beans>
         |""".stripMargin

    printWriter.print(source)

    printWriter.close()
  }

  private def application = {
    val printWriter = new PrintWriter(s"${srcDir}/Application.java", "utf-8")

    val source =
      s"""package ${modelPackage}.app;
         |
         |import com.github.apuex.springbootsolution.runtime.*;
         |import com.google.protobuf.*;
         |import com.google.protobuf.util.*;
         |import ${modelPackage}.message.*;
         |
         |import org.springframework.boot.*;
         |import org.springframework.boot.autoconfigure.*;
         |import org.springframework.boot.builder.*;
         |import org.springframework.boot.web.servlet.support.*;
         |import org.springframework.context.annotation.*;
         |import org.springframework.http.converter.protobuf.*;
         |
         |import java.util.*;
         |
         |@Configuration
         |@ComponentScan({"${modelPackage}.*"})
         |@ImportResource("classpath:app-config.xml")
         |@SpringBootApplication
         |public class Application extends SpringBootServletInitializer {
         |
         |  public static void main(String[] args) {
         |    SpringApplication.run(Application.class, args);
         |  }
         |
         |  @Bean
         |  ProtobufHttpMessageConverter protobufHttpMessageConverter() throws Exception {
         |    JsonFormat.TypeRegistry registry = JsonFormat.TypeRegistry.newBuilder()
         |        .add(${cToPascal{modelName}}.getDescriptor().getMessageTypes())
         |        .add(Messages.getDescriptor().getMessageTypes())
         |        .build();
         |    Set<Descriptors.FieldDescriptor> fieldsToAlwaysOutput = new HashSet<>();
         |    ${cToPascal{modelName}}.getDescriptor().getMessageTypes()
         |        .forEach(t -> fieldsToAlwaysOutput.addAll(t.getFields()));
         |    JsonFormat.Printer printer = JsonFormat.printer()
         |        .usingTypeRegistry(registry)
         |        .includingDefaultValueFields(fieldsToAlwaysOutput);
         |
         |    JsonFormat.Parser parser = JsonFormat.parser().usingTypeRegistry(registry);
         |    return new ProtobufJsonFormatHttpMessageConverter(parser, printer);
         |  }
         |
         |  @Override
         |  protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
         |    return application.sources(Application.class);
         |  }
         |}
    """.stripMargin
    printWriter.print(source)

    printWriter.close()
  }

  private def project = {
    val printWriter = new PrintWriter(s"${projectDir}/pom.xml", "utf-8")

    val source =
      s"""<?xml version="1.0" encoding="UTF-8"?>
         |<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         |         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
         |  <modelVersion>4.0.0</modelVersion>
         |
         |  <groupId>${modelPackage}</groupId>
         |  <artifactId>${cToShell(modelName)}-app</artifactId>
         |  <version>1.0-SNAPSHOT</version>
         |  <packaging>war</packaging>
         |
         |  <parent>
         |    <groupId>${modelPackage}</groupId>
         |    <artifactId>${cToShell(modelName)}</artifactId>
         |    <version>1.0-SNAPSHOT</version>
         |  </parent>
         |
         |  <dependencies>
         |    <dependency>
         |      <groupId>${modelPackage}</groupId>
         |      <artifactId>${cToShell(modelName)}-controller</artifactId>
         |      <version>1.0-SNAPSHOT</version>
         |    </dependency>
         |    <dependency>
         |      <groupId>${modelPackage}</groupId>
         |      <artifactId>${cToShell(modelName)}-integration</artifactId>
         |      <version>1.0-SNAPSHOT</version>
         |    </dependency>
         |    <dependency>
         |      <groupId>org.springframework.boot</groupId>
         |      <artifactId>spring-boot-starter-web</artifactId>
         |      <version>2.0.3.RELEASE</version>
         |    </dependency>
         |    <dependency>
         |      <groupId>org.springframework.boot</groupId>
         |      <artifactId>spring-boot-starter-tomcat</artifactId>
         |      <version>2.0.3.RELEASE</version>
         |      <scope>provided</scope>
         |    </dependency>
         |    <dependency>
         |      <groupId>com.github.apuex.jms</groupId>
         |      <artifactId>imq-patch</artifactId>
         |      <version>1.0.1</version>
         |    </dependency>
         |    <dependency>
         |      <groupId>org.glassfish.mq</groupId>
         |      <artifactId>imq</artifactId>
         |      <version>5.1</version>
         |    </dependency>
         |    <dependency>
         |      <groupId>com.github.apuex.event-source</groupId>
         |      <artifactId>event-source-jms</artifactId>
         |      <version>1.0.0</version>
         |    </dependency>
         |    <dependency>
         |      <groupId>org.springframework.boot</groupId>
         |      <artifactId>spring-boot-starter-test</artifactId>
         |      <version>2.0.3.RELEASE</version>
         |      <scope>test</scope>
         |    </dependency>
         |    <dependency>
         |      <groupId>com.jayway.jsonpath</groupId>
         |      <artifactId>json-path</artifactId>
         |      <version>2.4.0</version>
         |      <scope>test</scope>
         |    </dependency>
         |
         |  </dependencies>
         |
         |  <build>
         |    <plugins>
         |      <plugin>
         |        <groupId>org.springframework.boot</groupId>
         |        <artifactId>spring-boot-maven-plugin</artifactId>
         |        <version>2.0.3.RELEASE</version>
         |      </plugin>
         |    </plugins>
         |  </build>
         |
         |</project>
         |
       """.stripMargin

    printWriter.print(source)

    printWriter.close()
  }
}
