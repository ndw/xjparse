package com.nwalsh.parsers;

import org.junit.Test;
import org.xmlresolver.Catalog;
import org.xmlresolver.Resolver;

import java.util.Vector;

public class BothTests {
    @Test
    public void bothValid() {
        Vector<String> schemas = new Vector<String>();
        schemas.add("src/test/resources/schema/xml.xsd");
        schemas.add("src/test/resources/schema/doc.xsd");

        XJParser parser = new XJParser();
        parser.setDtdValidate(true);
        parser.setNamespaceAware(true);
        parser.setXsdValidate(true);
        parser.setSchemas(schemas);
        assert(parser.parse("src/test/resources/docs/dtd-valid.xml"));
    }

    @Test
    public void bothDtdInvalidXsdValid() {
        Vector<String> schemas = new Vector<String>();
        schemas.add("src/test/resources/schema/xml.xsd");
        schemas.add("src/test/resources/schema/doc.xsd");

        XJParser parser = new XJParser();
        parser.setDtdValidate(true);
        parser.setNamespaceAware(true);
        parser.setXsdValidate(true);
        parser.setSchemas(schemas);
        assert(! parser.parse("src/test/resources/docs/dtd-xsd-valid-only.xml"));
    }

    @Test
    public void bothDtdValidXsdInvalid() {
        Vector<String> schemas = new Vector<String>();
        schemas.add("src/test/resources/schema/xml.xsd");
        schemas.add("src/test/resources/schema/doc.xsd");

        XJParser parser = new XJParser();
        parser.setDtdValidate(true);
        parser.setNamespaceAware(true);
        parser.setXsdValidate(true);
        parser.setSchemas(schemas);
        assert(! parser.parse("src/test/resources/docs/dtd-dtd-valid-only.xml"));
    }
}

