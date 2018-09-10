# Boost Maven Plugin Prototype

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

1. `git clone git@github.com:OpenLiberty/boost-maven.git`
2. `cd boost-maven`
3. `mvn clean install`  (To run integration tests, add the -Pit parameter)

### Use the Liberty Boost plugin in your Spring Boot Maven project 


#### Quick start

1. Add the following to your project pom.xml
```xml
  <plugin>
        <groupId>io.openliberty.boost</groupId>
        <artifactId>boost-maven-plugin</artifactId>
        <version>0.1-SNAPSHOT</version>
        <executions>
          <execution>
                <phase>package</phase>
                <goals>
                      <goal>package</goal>
                </goals>
          </execution>
       </executions>
  </plugin>
```

2. Run `mvn clean package`
2. Run the produced jar file: `java -jar <application_name>.jar`

#### Tutorial

For a more detailed tutorial, see [here](Tutorial.md).
### Building and Developing Boost

See [here](https://github.com/OpenLiberty/boost/wiki/Home) 
