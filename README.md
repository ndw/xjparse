# xjparse

The `xjparse` library provides a convenient wrapper for running W3C
XML Schema validation with Xerces.

Usage: `java [java options] com.nwalsh.parsers.XJParse [parser options] doc.xml`

Where `[parser options]` are:

* `-c` *catalogFile* to load a particular XML Catalog
* `-w` to perform a well-formed (non-validating) parse
* `-v` to perform a validating parse (the default)
* `-s` to enable W3C XML Schema validation
* `-S` *schema.xsd* to use *schema.xsd* for validation, implies `-s`
* `-f` to enable full schema checking, implies `-s`
* `-n` to perform a namespace-ignorant parse
* `-N` to perform a namespace-aware parse (the default)
* `-d` *integer* to set the debug level
* `-E` *integer* to specify the maximum number of errors to display

The process ends with error-level 1 if there are errors, 0 otherwise.

# Testing

Running `ant test` will demonstrate the results.

# Xerces libraries

For convenience, libraries from Xerces 2.11.0 are included in the `lib` directory;
you can ignore those or replace them with any recent version of the Xerces instead.
