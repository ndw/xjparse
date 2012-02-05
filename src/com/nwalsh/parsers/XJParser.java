package com.nwalsh.parsers;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
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

    protected static final String SCHEMA_VALIDATION_FEATURE_ID
            = "http://apache.org/xml/features/validation/schema";

    protected static final String SCHEMA_FULL_CHECKING_FEATURE_ID
            = "http://apache.org/xml/features/validation/schema-full-checking";

    protected static final String EXTERNAL_SCHEMA_LOCATION_PROPERTY_ID
            = "http://apache.org/xml/properties/schema/external-schemaLocation";

    protected static final String EXTERNAL_NONS_SCHEMA_LOCATION_PROPERTY_ID
            = "http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation";

    private boolean valid = true;
    private boolean nsAware = true;
    private boolean dtdValidate = true;
    private boolean xsdValidate = true;
    private boolean xsd11 = false;
    private boolean fullChecking = false;
    private Collection<String> schemas = null;
    private XParseError errhandler = new XParseError();
    private Date startTime = null;
    private Date endTime = null;


    public XJParser() {
        // whatever
    }
    
    public void setDtdValidate(boolean validate) { dtdValidate = validate; }
    public void setXsdValidate(boolean validate) { xsdValidate = validate; }
    public void setNamespaceAware(boolean aware) { nsAware = aware; }
    public void setFullChecking(boolean full) { fullChecking = full; }
    public void setSchemas(Collection<String> schemas) { this.schemas = schemas; }
    public void setXSD11(boolean use11) { xsd11 = use11; }
    public void setMaxMessages(int errors) { errhandler.setMaxMessages(errors); }
    public int getErrorCount() { return errhandler.getErrorCount(); }
    
    public boolean parse(String doc) {
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
            }
        } else {
            sources = new StreamSource[0];
        }

        try {
            SchemaFactory factory = SchemaFactory.newInstance(xsd11 ? XSD11 : XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(sources);

            /** Setup SAX parser for schema validation. */
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(nsAware);
            spf.setValidating(dtdValidate);
            spf.setSchema(schema);

            spf.setFeature(SCHEMA_FULL_CHECKING_FEATURE_ID, fullChecking);

            valid = true;
            SAXParser parser = spf.newSAXParser();
            startTime = new Date();
            parser.parse(doc, new ParseHandler());
            endTime = new Date();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return valid;
    }

    public boolean dtdParse(String doc) {
        try {
            /** Setup SAX parser for DTD validation. */
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(nsAware);
            spf.setValidating(dtdValidate);

            valid = true;
            SAXParser parser = spf.newSAXParser();
            startTime = new Date();
            parser.parse(doc, new ParseHandler());
            endTime = new Date();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return valid;
    }

    public void printParseStats() {
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
            errhandler.fatalError(spe);
        }

        public void error(SAXParseException spe) throws SAXParseException {
            errhandler.error(spe);
        }

        public void warning(SAXParseException spe) throws SAXParseException {
            errhandler.warning(spe);
        }
    }
}
