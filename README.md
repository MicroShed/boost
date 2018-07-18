# Boost Maven Plugin Prototype

This is a prototype Maven plugin to package a Spring Boot application with Liberty

When added to your pom.xml, the plugin will

1. Install Open Liberty
2. Create an Open Liberty server
3. Thin the Spring Boot project application
4. Install the application to the server
5. Install and add the springBoot-1.5 feature to the server configuration
6. Package the server and application into a runnable jar



### Build Liberty Boost Plugin

1. git clone git@github.com:OpenLiberty/boost.git
2. cd boost
3. mvn clean install

### Use the Liberty Boost plugin in your Spring Boot Maven project 


1. Add the following to your project pom.xml

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

2. Run mvn clean package
2. Run the produced jar file: java -jar <application_name>.jar"
