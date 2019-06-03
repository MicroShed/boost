## Liberty Boost

Boost is a Maven and Gradle plugin that enhances the builds for your Java EE, MicroProfile, and Spring Boot applications. 

There are three separate Boost projects. Click the links for more information.

- [Boost Maven Plugin](https://github.com/OpenLiberty/boost/tree/master/boost-maven)
- [Boost Gradle Plugin](https://github.com/OpenLiberty/boost/tree/master/boost-gradle)
- [Boost Common Resources](https://github.com/OpenLiberty/boost/tree/master/boost-common)

### Developing Liberty Boost

If you are interested in contributing to Liberty Boost, read the [wiki](https://github.com/OpenLiberty/boost-maven/wiki) for more information.

### Build Dependencies

The Boost plugin builds on the Liberty plugin.

For Maven, you will need to clone and `mvn install` the following repositories: [ci.common](https://github.com/WASdev/ci.common) and [ci.maven](https://github.com/WASdev/ci.maven).

### Building Liberty Boost

You will need to build the `boost-common` project before building either the Boost Maven Plugin or Boost Gradle Plugin. We provide some scripts below to simplify this process. 

#### Boost Maven Plugin

To build the Boost Maven Plugin:

##### Windows:

```
./boost-maven.bat
```

##### Mac/Linux:

```
./boost-maven.sh
```

#### Boost Gradle Plugin

To build the Boost Gradle Plugin:

##### Windows:

```
./boost-gradle.bat
```

##### Mac/Linux:

```
./boost-gradle.sh
```
