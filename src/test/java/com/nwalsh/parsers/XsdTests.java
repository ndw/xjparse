package com.nwalsh.parsers;

import org.junit.Test;
import org.xmlresolver.Catalog;
import org.xmlresolver.Resolver;

import java.util.Vector;

public class XsdTests {
    @Test
    public void xsdValid() {
        Vector<String> schemas = new Vector<String>();
        schemas.add("src/test/resources/schema/xml.xsd");
        schemas.add("src/test/resources/schema/doc.xsd");

        XJParser parser = new XJParser();
        parser.setDtdValidate(false);
        parser.setNamespaceAware(true);
        parser.setXsdValidate(true);
        parser.setSchemas(schemas);
        parser.setFullChecking(true);
        assert(parser.parse("src/test/resources/docs/xsd-valid.xml"));
    }

    @Test
    public void xsdInvalid() {
        Vector<String> schemas = new Vector<String>();
        schemas.add("src/test/resources/schema/xml.xsd");
        schemas.add("src/test/resources/schema/doc.xsd");

        XJParser parser = new XJParser();
        parser.setDtdValidate(false);
        parser.setNamespaceAware(true);
        parser.setXsdValidate(true);
        parser.setSchemas(schemas);
        assert(! parser.parse("src/test/resources/docs/xsd-invalid.xml"));
    }

    @Test
    public void xsdDtdInvalidXsdValid() {
        Vector<String> schemas = new Vector<String>();
        schemas.add("src/test/resources/schema/xml.xsd");
        schemas.add("src/test/resources/schema/doc.xsd");

        XJParser parser = new XJParser();
        parser.setDtdValidate(false);
        parser.setNamespaceAware(true);
        parser.setXsdValidate(true);
        parser.setSchemas(schemas);
        assert(parser.parse("src/test/resources/docs/dtd-xsd-valid-only.xml"));
    }

    @Test
    public void xsdHintsValid() {
        XJParser parser = new XJParser();
        parser.setDtdValidate(false);
        parser.setNamespaceAware(true);
        parser.setXsdValidate(true);
        assert(parser.parse("src/test/resources/docs/xsd-hints-valid.xml"));
    }

    @Test
    public void xsdHintsInvalid() {
        XJParser parser = new XJParser();
        parser.setDtdValidate(false);
        parser.setNamespaceAware(true);
        parser.setXsdValidate(true);
        assert(! parser.parse("src/test/resources/docs/xsd-hints-invalid.xml"));
    }

    @Test
    public void xsdHintsInvalidWithoutCatalog() {
        XJParser parser = new XJParser();
        parser.setDtdValidate(false);
        parser.setNamespaceAware(true);
        parser.setXsdValidate(true);
        assert(! parser.parse("src/test/resources/docs/xsd-hints-catalog-valid.xml"));
    }

    @Test
    public void xsdHintsValidWithCatalog() {
        Catalog catalog = new Catalog("src/test/resources/catalogs/catalog.xml");
        Resolver resolver = new Resolver(catalog);

        XJParser parser = new XJParser();
        parser.setDtdValidate(false);
        parser.setNamespaceAware(true);
        parser.setXsdValidate(true);
        parser.setResolver(resolver);
        assert(parser.parse("src/test/resources/docs/xsd-hints-catalog-valid.xml"));
    }

    @Test
    public void xsdInvalidWithoutCatalog() {
        Vector<String> schemas = new Vector<String>();
        schemas.add("src/test/resources/schema/xml.xsd");
        schemas.add("src/test/resources/schema/catalog-doc.xsd");

        XJParser parser = new XJParser();
        parser.setDtdValidate(false);
        parser.setNamespaceAware(true);
        parser.setXsdValidate(true);
        parser.setSchemas(schemas);
        parser.setFullChecking(true);
        assert(! parser.parse("src/test/resources/docs/xsd-valid.xml"));
    }

    @Test
    public void xsdValidWithCatalog() {
        Catalog catalog = new Catalog("src/test/resources/catalogs/catalog.xml");
        Resolver resolver = new Resolver(catalog);

        Vector<String> schemas = new Vector<String>();
        schemas.add("src/test/resources/schema/xml.xsd");
        schemas.add("src/test/resources/schema/catalog-doc.xsd");

        XJParser parser = new XJParser();
        parser.setDtdValidate(false);
        parser.setNamespaceAware(true);
        parser.setXsdValidate(true);
        parser.setSchemas(schemas);
        parser.setFullChecking(true);
        parser.setResolver(resolver);
        assert(! parser.parse("src/test/resources/docs/xsd-valid.xml"));
    }
}
