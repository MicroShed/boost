# boost-gradle
Liberty Boost Gradle support

# Adding the plugin to a project

Build the plugin locally with:

```
gradle clean build
```

To use the installed version of the plugin add the following code snippet to your project's `build.gradle` file:

```
buildscript {
    repositories {
        mavenLocal()
    }
    dependencies {
        classpath 'io.openliberty.boost:boost-gradle-plugin:0.1-SNAPSHOT'
    }
}

apply plugin: 'boost'
```

# Plugin Tasks

| Task Name       | Description                                      |
|-----------------|--------------------------------------------------|
| boostStart      | Starts the Boost application.                    |
| boostStop       | Stops the Boost application.                     |
| boostRun        | Runs the Boost application in the foreground.    |
| boostDebug      | Runs the Boost application in debug mode.        |
| boostPackage    | Packages the application and server.             |
| boostDocker     | Creates a Docker file and image for the project. |
| boostDockerPush | Pushes Docker image to a Docker repository.      |