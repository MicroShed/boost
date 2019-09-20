# Boost

[![Build Status](https://travis-ci.org/dev-tools-for-enterprise-java/boost.svg?branch=master)](https://travis-ci.org/dev-tools-for-enterprise-java/boost)
[![License](https://img.shields.io/badge/License-EPL%201.0-green.svg)](http://www.eclipse.org/legal/epl-v10.html)

Boost includes a Maven and Gradle plugin to make it easier to build your MicroProfile applications.

There are two, separate active Boost projects. 

- Boost Maven Plugin, BOMs, and Boosters (`boost-maven`)
- Boost Common Resources (`boost-common`)

with a Boost Gradle project under development.

### Using the Boost Maven Plugin
Please see the [boost-maven plugin documentation](https://github.com/MicroShed/boost/tree/master/boost-maven#boost-maven-plugin-prototype) for details on using boost in your maven application project.

To see an example of Boost in use in a maven application project, please see our sample application project [here](https://github.com/OpenLiberty/boosted-microprofile-rest-client)

### Developing Boost

If you are interested in contributing to Boost, read the [wiki](https://github.com/MicroShed/boost/wiki) for more information.

If you are interested in the Boost runtime adapter SPI, it is described in greater detail in the [Boost Runtimes Adapter SPI](https://github.com/MicroShed/boost/wiki/Boost-Runtime-Adapter-SPI) page of the wiki.
git 
### Building Boost

You will need to build the `boost-common` project before building the `boost-maven` project. We provide some scripts below to simplify this process. 

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

