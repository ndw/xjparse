# xjparse

The `xjparse` library provides a convenient wrapper for running W3C
XML Schema validation with Xerces.

Usage: `java [java options] com.nwalsh.parsers.XJParse [parser options] doc.xml`

Where `[parser options]` are:

* `-c` *catalogFile* to load a particular XML Catalog
* `-v` to perform a (DTD) validating parse
* `-s` to enable W3C XML Schema validation
* `-S` *schema.xsd* to use *schema.xsd* for validation, implies `-s`
* `-f` to enable full schema checking, implies `-s`
* `-n` to perform a namespace-ignorant parse
* `-E` *integer* to specify the maximum number of errors to display
* `-d` to enable debug mode; show stack trace on errors
* `-q` to use quiet mode; validation warning and error messages suppressed

The process ends with error-level 1 if there are errors, 0 otherwise.

# Distributions

There are two jar files:

* `xjparse-app-X.Y.Z.jar` contains the application classes and all of the dependencies. You
can run the application directly out of this jar file.
* `xjparse-X.Y.Z.jar` contains only the application classes; to use it, you’ll need to put the
dependencies on your class path. (Look in `build.gradle` for the current set of dependencies.)

# Testing

Running the following command:

    java -jar xjparse-app-3.0.0.jar -f -S src/test/resources/schema/doc.xsd src/test/resources/docs/sample.xml

will demonstrate the results.

# Contact

If you have questions or comments about `xjparse`, please visit
<http://github.com/ndw/xjparse> for contact details or to report issues.
