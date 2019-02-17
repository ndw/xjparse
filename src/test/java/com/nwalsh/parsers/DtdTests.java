package com.nwalsh.parsers;

import org.junit.Test;
import org.xmlresolver.Catalog;
import org.xmlresolver.Resolver;

import java.util.Vector;

public class DtdTests {
    @Test
    public void dtdValid() {
        XJParser parser = new XJParser();
        parser.setDtdValidate(true);
        parser.setNamespaceAware(true);
        parser.setXsdValidate(false);
        assert(parser.parse("src/test/resources/docs/dtd-valid.xml"));
    }

    @Test
    public void dtdInvalid() {
        XJParser parser = new XJParser();
        parser.setDtdValidate(true);
        parser.setNamespaceAware(true);
        parser.setXsdValidate(false);
        assert(! parser.parse("src/test/resources/docs/dtd-invalid.xml"));
    }

    @Test
    public void bothDtdValidXsdInvalid() {
        XJParser parser = new XJParser();
        parser.setDtdValidate(true);
        parser.setNamespaceAware(true);
        parser.setXsdValidate(false);
        assert(parser.parse("src/test/resources/docs/dtd-dtd-valid-only.xml"));
    }

    @Test
    public void dtdInvalidWithoutCatalog() {
        XJParser parser = new XJParser();
        parser.setDtdValidate(true);
        parser.setNamespaceAware(true);
        parser.setXsdValidate(false);
        assert(! parser.parse("src/test/resources/docs/dtd-catalog-valid.xml"));
    }

    @Test
    public void dtdValidWithCatalog() {
        Catalog catalog = new Catalog("src/test/resources/catalogs/catalog.xml");
        Resolver resolver = new Resolver(catalog);

        XJParser parser = new XJParser();
        parser.setDtdValidate(true);
        parser.setNamespaceAware(true);
        parser.setXsdValidate(false);
        parser.setResolver(resolver);
        assert(! parser.parse("src/test/resources/docs/dtd-catalog-valid.xml"));
    }
}
