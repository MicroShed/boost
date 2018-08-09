# Boost Maven Plugin Prototype

### NOTE: Dependency on unreleased snapshots

Do not try these instructions yet since they require not-yet-merged personal branches of the [ci.maven](https://github.com/WASdev/ci.maven) plugin.

The current **master** depends on [this branch](https://github.com/anjumfatima90/ci.maven/tree/spring-boot-thin-plugin-ext) at commit [a24b04c](https://github.com/anjumfatima90/ci.maven/commit/a24b04cd668f2d2e31fc6c1762028ae938e66ac9) of the ci.maven plugin, which you must install somehow (e.g. clone and build/install with a local Maven build).

### What is Boost?

This is a prototype Maven plugin to package a Spring Boot application with Liberty

When added to your pom.xml, the plugin will

1. Install Open Liberty
2. Create an Open Liberty server
3. Thin the Spring Boot project application
4. Install the application to the server
5. Install and add appropriate Liberty feature to the server configuration
6. Package the server and application into a runnable jar



### Build Liberty Boost Plugin

1. git clone git@github.com:OpenLiberty/boost.git
2. cd boost
3. mvn clean install  (To run integration tests, add the -Pit parameter)

### Use the Liberty Boost plugin in your Spring Boot Maven project 


1. Add the following to your project pom.xml
```xml
  <plugin>
        <groupId>boost.project</groupId>
        <artifactId>boost-maven-plugin</artifactId>
        <version>1.0-SNAPSHOT</version>
        <executions>
          <execution>
                <phase>package</phase>
                <goals>
                      <goal>package-app</goal>
                </goals>
          </execution>
       </executions>
  </plugin>
```

2. Run mvn clean package
2. Run the produced jar file: java -jar <application_name>.jar

### Building and Developing Boost

See [here](https://github.com/OpenLiberty/boost/wiki/Home) 
