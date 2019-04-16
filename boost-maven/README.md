# Boost Maven Plugin Prototype

### What is Boost?

This is a prototype Maven plugin to package a fully configured application with a target runtime.

When added to your pom.xml, the plugin will

1. Install the desired target runtime.
2. Create a server.
3. Install the application to the server.
4. Configure the server appropriatly for the application.
5. Package the server and application into a runnable jar.

### Build the Boost Maven Plugin

1. `git clone git@github.com:OpenLiberty/boost.git`
2. `boost-maven.sh`
3. `mvn clean install`  (To run integration tests, add the -Pit parameter)

### Use the Boost Maven plugin in your Application Maven project 

#### Try it!

Kick the tires of Boost with zero configuration:

* Produce a runnable uber jar for your app:
    * `mvn clean package io.openliberty.boost:boost-maven-plugin:0.1:package`
    * `java -jar target/<application name>.jar`

#### Quick start - uber jar

1. Add the following to your project pom.xml
    ```xml
      <plugin>
            <groupId>io.openliberty.boost</groupId>
            <artifactId>boost-maven-plugin</artifactId>
            <version>0.1</version>
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
1. Run `mvn clean package`
1. Run the produced jar file: `java -jar <application_name>.jar`

#### Tutorial

For a more detailed tutorial, see [here](BoostTutorial.md).

### Building and Developing Boost

See [here](https://github.com/OpenLiberty/boost/wiki/Home) 