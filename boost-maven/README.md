# Boost Maven Plugin Prototype

### What is Boost?

This is a prototype Maven plugin to package a fully configured Java EE or MicroProfile application with a target runtime.

When added to your pom.xml, the plugin will

1. Install the desired target runtime.
2. Create a server.
3. Install the application to the server.
4. Configure the server appropriately for the application.
5. Package the server and application into a runnable jar.

### Build the Boost Maven Plugin

1. `git clone git@github.com:OpenLiberty/boost.git`
2. `boost-maven.sh`

### Tutorial

For more detailed instructions, see [here](https://github.com/awisniew90/boosted-microprofile-rest-client/blob/master/README.md).

### Building and Developing Boost

See [here](https://github.com/OpenLiberty/boost/wiki/Home) 
