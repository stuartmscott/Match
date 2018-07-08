
![Match](./docs/header/Match.png)

A lightweight, fast and extensible build system.

Match hides away the heavy lifting in Java, leaving the build files simple, intuitive, and easy to maintain.

    # Add the library
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

    # Release the package once tests pass
    Release(
        source = "Sample"
        channel = "cp %s $HOME/match/libraries/Sample-0.5.jar"
        await = "SampleTestResult"
    )

## Functions
- AndroidApk - builds an Android APK using the Android SDK.
- AndroidInstrumentation - installs and runs instrumentation tests on a connected Android device.
- CheckStyle - enforces a style guide on the source code.
- Find - finds all files under the given directory, filtering files by an optional pattern.
- Get - looks up a build property given a key.
- GetFile - gets a reference to a file created by another function.
- JavaJar - compiles java code into a jar.
- JavaJUnit - runs JUnit tests.
- Library - adds a library to the build, downloading if necessary.
- Release - builds a release bundle, and pushes it out the distribution channels.
- Set - sets a build property given a key/value pair.
- SetFile - sets a file property and reference.
- Zip - compresses the source files into a .zip file.

## Extensions
Match can easily be extended to include project-, language- or workspace-specific functions.

1. Create a new function in the "expression.function" package which extends "Function"
2. Compile and add to Match's classpath
3. Call by name in your match files

## Samples
- Echo - a simple Java application with tests. The match file compiles and jars the code and tests, and then runs the tests.
- HelloDroid - an Android Library and Application. The match file compiles the library and application, and then runs the tests.
