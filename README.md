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

Running the following command:

    java -cp xjparse.jar com.nwalsh.parsers.XJParse -f -S tests/schema/doc.xsd tests/sample.xml

will demonstrate the results. If checked out the sources, you can
run this command with `ant test`.

# Xerces libraries

For convenience, several libraries are included in the `lib`
directory; you can ignore those or replace them with corresponding,
perhaps more recent, libraries if you wish.

# Contact

If you have questions or comments about `xjparse`, please visit
<http://github.com/ndw/xjparse> for contact details or to report issues.
