package com.nwalsh.parsers;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import org.xmlresolver.Resolver;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: ndw
 * Date: 2/5/12
 * Time: 1:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class XJParser {
    private static final String XSD11 = "http://www.w3.org/XML/XMLSchema/v1.1";

    protected static final String SCHEMA_FULL_CHECKING_FEATURE_ID
            = "http://apache.org/xml/features/validation/schema-full-checking";

    private boolean valid = true;
    private boolean nsAware = true;
    private boolean dtdValidate = true;
    private boolean xsdValidate = true;
    private boolean xsd11 = false;
    private boolean fullChecking = false;
    private int debug = 0;
    private int maxMessages = 0;
    private Collection<String> schemas = null;
    private XParseError errhandler = null;
    private Date startTime = null;
    private Date endTime = null;
    private Resolver resolver = null;


    public XJParser() {
        // whatever
    }
    
    public void setDtdValidate(boolean validate) { dtdValidate = validate; }
    public void setXsdValidate(boolean validate) { xsdValidate = validate; }
    public void setNamespaceAware(boolean aware) { nsAware = aware; }
    public void setFullChecking(boolean full) { fullChecking = full; }
    public void setDebug(int dbg) { debug = dbg; }
    public void setSchemas(Collection<String> schemas) { this.schemas = schemas; }
    public void setXSD11(boolean use11) { xsd11 = use11; }
    public void setMaxMessages(int errors) { maxMessages = errors; }
    public void setResolver(Resolver resolver) { this.resolver = resolver; }
    public int getErrorCount() { return errhandler.getErrorCount(); }
    
    public boolean parse(String doc) {
        errhandler = new XParseError(debug > 0);
        errhandler.setMaxMessages(maxMessages);

        if (xsdValidate) {
            return xsdParse(doc);
        } else {
            return dtdParse(doc);
        }
    }

    private boolean xsdParse(String doc) {
        StreamSource[] sources = null;
        
        if (schemas != null) {
            sources = new StreamSource[schemas.size()];
            int pos = 0;
            for (String suri : schemas) {
                StreamSource stream = new StreamSource(suri);
                sources[pos] = stream;
                pos += 1;
            }
        }

        try {
            SchemaFactory factory = SchemaFactory.newInstance(xsd11 ? XSD11 : XMLConstants.W3C_XML_SCHEMA_NS_URI);
            factory.setResourceResolver(resolver);
            Schema schema = null;

            if (sources == null) {
                schema = factory.newSchema();
            } else {
                schema = factory.newSchema(sources);
            }

            /* Setup SAX parser for schema validation. */
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(nsAware);
            spf.setValidating(dtdValidate);
            spf.setSchema(schema);

            spf.setFeature(SCHEMA_FULL_CHECKING_FEATURE_ID, fullChecking);
            doParse(doc, spf);
        } catch (Exception e) {
            valid = false;
            if (debug > 1) {
                e.printStackTrace();
            } else if (debug > 0) {
                System.err.println(e.toString());
            }
        }

        return valid;
    }

    public boolean dtdParse(String doc) {
        /* Setup SAX parser for DTD validation. */
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(nsAware);
        spf.setValidating(dtdValidate);
        doParse(doc, spf);
        return valid;
    }

    private void doParse(String doc, SAXParserFactory spf) {
        try {
            valid = true;
            SAXParser parser = spf.newSAXParser();
            startTime = new Date();
            parser.parse(doc, new ParseHandler());
            endTime = new Date();
        } catch (Exception e) {
            valid = false;
            if (debug > 1) {
                e.printStackTrace();
            } else if (debug > 0) {
                System.err.println(e.toString());
            }
        }
    }

    public void printParseStats() {
        if (startTime == null || endTime == null) {
            return;
        }

        long millisec = endTime.getTime() - startTime.getTime();
        long secs = 0;
        long mins = 0;
        long hours = 0;

        if (millisec > 1000) {
            secs = millisec / 1000;
            millisec = millisec % 1000;
        }

        if (secs > 60) {
            mins = secs / 60;
            secs = secs % 60;
        }

        if (mins > 60) {
            hours = mins / 60;
            mins = mins % 60;
        }

        if (errhandler.getMaxMessages() > 0) {
            System.out.print("Parse ");
            if (errhandler.getFatalCount() > 0) {
                System.out.print("failed ");
            } else {
                System.out.print("succeeded ");
                System.out.print("(");
                if (hours > 0) {
                    System.out.print(hours + ":");
                }
                if (hours > 0 || mins > 0) {
                    System.out.print(mins + ":");
                }
                System.out.print(secs + "." + millisec);
                System.out.print(") ");
            }
            System.out.print("with ");

            int errCount = errhandler.getErrorCount();
            int warnCount = errhandler.getWarningCount();

            if (errCount > 0) {
                System.out.print(errCount + " error");
                System.out.print(errCount > 1 ? "s" : "");
                System.out.print(" and ");
            } else {
                System.out.print("no errors and ");
            }

            if (warnCount > 0) {
                System.out.print(warnCount + " warning");
                System.out.print(warnCount > 1 ? "s" : "");
                System.out.print(".");
            } else {
                System.out.print("no warnings.");
            }

            System.out.println("");
        }
    }
    
    private class ParseHandler extends DefaultHandler {
        public void fatalError(SAXParseException spe) throws SAXParseException {
            valid = false;
            errhandler.fatalError(spe);
        }

        public void error(SAXParseException spe) throws SAXParseException {
            valid = false;
            errhandler.error(spe);
        }

        public void warning(SAXParseException spe) throws SAXParseException {
            errhandler.warning(spe);
        }

        public InputSource resolveEntity(String publicId, String systemId) throws IOException, SAXException {
            if (resolver != null) {
                return resolver.resolveEntity(publicId, systemId);
            } else {
                return null;
            }
        }
    }
}
