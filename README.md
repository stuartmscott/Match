
![Match](./docs/header/Match.png)

A lightweight, fast and extensible build system.

Match hides away the heavy lifting in Java, leaving the build files simple, intuitive, and easy to maintain.

    # Add the library
    Library(
        name = "junit"
        version = "4.12"
        extension = "jar"
        location = "http://search.maven.org/remotecontent?filepath=junit/junit/"
    )

    # Build the code
    JavaJar(
        name = "Sample"
        source = Find("source")
        main_class = "match.Sample"
    )
    
    # Build the tests
    JavaJar(
        name = "SampleTest"
        source = Find("tests/source")
        main_class = "match.SampleTest"
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
        main_class = "match.SampleTest"
    )

    # Package the distribution
    Zip(
        name = "SampleDist"
        source = [
            Find("scripts")
            GetFile("Sample")
        ]
    )

## Functions
- Find - finds all files under the given directory, filtering files by an optional pattern.
- Get - looks up a build property given a key.
- GetFile - gets a reference to a file created by another function.
- JavaJar - compiles java code into a jar.
- JavaJUnit - runs JUnit tests.
- Library - adds a library to the build, downloading if necessary.
- Set - sets a build property given a key/value pair.
- SetFile - sets a file property and reference.
- Zip - compresses the source files into a .zip file.

## Extensions
Match can easily be extended to include project-, language- or workspace-specific functions.

1. Create a new function in the "expression.function" package which extends "Function"
2. Compile and add to Match's classpath
3. Call by name in your match files
