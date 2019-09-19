# Boost Maven Plugin Prototype

### What is the Boost Maven Plugin?

This is a prototype Maven plugin to package a fully configured Java EE or MicroProfile application with a target runtime.

When added to your pom.xml, the plugin will

1. Install the desired target runtime.
2. Create a server.
3. Install the application to the server.
4. Configure the server appropriately for the application.

### Using the Boost Maven Plugin

Boost is an end-to-end packaging and dependency management plugin for Jakarta EE and Microprofile applications. To use Boost in your project, define the plugin and add the appropriate BOM and Booster dependencies to manage various MicroProfile features. 

## Defining the plugin

Add the following to the `<plugins>` section of your project's pom.xml:
```xml
  <plugin>
        <groupId>org.microshed.boost</groupId>
        <artifactId>boost-maven-plugin</artifactId>
        <version>0.2.1</version>
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
## Execution goals

* Package - packages your project into an executable jar file

## Boost BOMs

The Boost BOMs define which version of Jakarta EE or MicroProfile is required. This will influence which features are loaded as compile and runtime dependencies. 

### EE7-BOM

Defining the EE7 BOM ensures that all EE features comply with the EE7 specification.

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.microshed.boost.boosters</groupId>
            <artifactId>ee7-bom</artifactId>
            <version>0.2.1</version>
	    <scope>import</scope>
	    <type>pom</type>
	 </dependency>
    </dependencies>
</dependencyManagement>
```

### EE8-BOM

Defining the EE8 BOM ensures that all EE features comply with the EE8 specification.

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.microshed.boost.boosters</groupId>
            <artifactId>ee8-bom</artifactId>
            <version>0.2.1</version>
	    <scope>import</scope>
	    <type>pom</type>
	 </dependency>
    </dependencies>
</dependencyManagement>
```

### MP20-BOM

Defining the MP20 BOM ensures that all MicroProfile features comply with the 2.0 specification.

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.microshed.boost.boosters</groupId>
            <artifactId>mp20-bom</artifactId>
            <version>0.2.1</version>
	    <scope>import</scope>
	    <type>pom</type>
	 </dependency>
    </dependencies>
</dependencyManagement>
```
### MP21-BOM

Defining the MP21 BOM ensures that all MicroProfile features comply with the 2.1 specification.

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.microshed.boost.boosters</groupId>
            <artifactId>mp21-bom</artifactId>
            <version>0.2.1</version>
	    <scope>import</scope>
	    <type>pom</type>
	 </dependency>
    </dependencies>
</dependencyManagement>
```
### MP22-BOM

Defining the MP22 BOM ensures that all MicroProfile features comply with the 2.2 specification.

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.microshed.boost.boosters</groupId>
            <artifactId>mp22-bom</artifactId>
            <version>0.2.1</version>
	    <scope>import</scope>
	    <type>pom</type>
	 </dependency>
    </dependencies>
</dependencyManagement>
```
### MP30-BOM

Defining the MP30 BOM ensures that all MicroProfile features comply with the 3.0 specification.

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.microshed.boost.boosters</groupId>
            <artifactId>mp30-bom</artifactId>
            <version>0.2.1</version>
	    <scope>import</scope>
	    <type>pom</type>
	 </dependency>
    </dependencies>
</dependencyManagement>
```

## Boosters

Boost provides a set of dependencies called boosters for various Jakarta EE and MicroProfile features. These Boosters will automatically pull in the appropriate compile and runtime dependencies needed for that feature. Depending on which BOM is defined, Boost will choose the appropriate versions for each feature.

For a full list of supported boosters and their usage, see [here](https://github.com/MicroShed/boost/wiki/Boosters).

## Build Your Boosted Application Project
Once your application project pom.xml is finalized with the Boost Plugin, Boost BOMs and booster dependencies, issue the following Maven command:

 `mvn clean package`