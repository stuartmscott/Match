
![Match](./docs/header/Match.png)

A lightweight, fast and extensible build system.

Match hides away the heavy lifting in Java, leaving the build files simple, intuitive, and easy to maintain.

    # Build the code
    JavaJar(
        name = "Sample"
        source = Find("source")
        main_class = "main.Sample"
    )
    
    # Build the tests
    JavaJar(
        name = "SampleTest"
        source = Find("tests/source")
        main_class = "main.SampleTest"
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
        main_class = "main.SampleTest"
    )

## Functions
- Find - finds all files under the given directory, filtering files by an optional pattern.
- Get - looks up a build property given a key.
- JavaJar - compiles java code into a jar.
- JavaJUnit - runs JUnit tests.
- Set - sets a build property given a key/value pair.

## Extensions
Match can easily be extended to include project-, language- or workspace-specific functions.

1. Create a new function in the "expression.function" package which extends "Function"
2. Compile and add to Match's classpath
3. Call by name in your match files
