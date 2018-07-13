
![Match](./docs/header/Match.png)

A lightweight, fast and extensible build system.

Match hides away the heavy lifting in Java, leaving the build files simple, intuitive, and easy to maintain.

    # Add the libraries
    Library(
        name = "checkstyle"
        file = "checkstyle-8.11-all.jar"
        location = "https://github.com/checkstyle/checkstyle/releases/download/checkstyle-8.11/"
    )

    Library(
        name = "junit"
        file = "junit-4.12.jar"
        location = "http://search.maven.org/remotecontent?filepath=junit/junit/4.12/"
    )

    # Build the code
    JavaJar(
        name = "Sample"
        source = Find("source")
        main-class = "match.Sample"
    )
    
    # Build the tests
    JavaJar(
        name = "SampleTest"
        source = Find("tests/source")
        main-class = "match.SampleTest"
        library = [
            "Sample"
            "junit"
        ]
    )
    
    # Run the tests
    JavaJUnit(
        name = "SampleTestResult"
        library = [
            "SampleTest"
            "Sample"
        ]
        main-class = "match.SampleTest"
    )

    # Check the code style
    CheckStyle(
        name = "SampleCheckStyleResult"
        config = "$HOME/match/checkstyle.xml"
        source = [
            Find("source")
            Find("tests/source")
        ]
    )

    # Release the package once style is checked and tests pass
    Release(
        source = "Sample"
        channel = "cp %s $HOME/match/libraries/Sample-0.5.jar"
        require = [
            "SampleCheckStyleResult"
            "SampleTestResult"
        ]
    )

## Running
- ./lite-em - builds all targets
- ./lite-em quiet - builds all targets quietly
- ./lite-em verbose - builds all targets verbosely

## Functions
- CheckStyle - enforces a style guide on the source code.
- Find - finds all files under the given directory, filtering files by an optional pattern.
- Get - looks up a build property given a key.
- GetFile - gets a reference to a file created by another function.
- Gradle - triggers a Gradle build of the given tasks.
- JavaJar - compiles java code into a jar.
- JavaJUnit - runs JUnit tests.
- Library - adds a library to the build, downloading if necessary.
- Platform - picks an option based on the build platform.
- Protobuf - generates Java from Protocol Buffers and then packages the classes into a jar.
- Release - builds a release bundle, and pushes it out the distribution channels.
- Set - sets a build property given a key/value pair.
- SetFile - sets a file property and reference.
- Zip - compresses the source files into a .zip file.

## Extensions
Match can easily be extended to include project-, language- or workspace-specific functions.

1. Create a new function in the "match.expression.function" package which extends "Function"
2. Compile and add to Match's classpath
3. Call by name in your match files

## Samples
- Echo - a simple Java application with tests. The match file compiles and jars the code and tests, and then runs the tests.
- HelloDroid - an Android Library and Application. The match file compiles the library and application, and then runs the tests.
