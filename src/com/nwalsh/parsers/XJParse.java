// xjparse.java - A simple command-line XML parser

/* Copyright 2005 Norman Walsh.
 * Derived from org.apache.xml.resolver.apps.xread.
 * Portions copyright 2001-2004 The Apache Software Foundation or its
 * licensors, as applicable.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nwalsh.parsers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xml.resolver.tools.ResolvingXMLReader;
import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.helpers.Debug;
import org.apache.xml.resolver.apps.XParseError;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A simple command-line XML parsing application.
 *
 * <p>This class implements a simple command-line XML Parser. It's
 * just a little wrapper around the JAXP XMLReader with support for
 * catalogs.
 * </p>
 *
 * <p>Usage: com.nwalsh.parsers.xjparse [opts] xmlfile</p>
 *
 * <p>Where:</p>
 *
 * <dl>
 * <dt><code>-c</code> <em>catalogfile</em></dt>
 * <dd>Load a particular catalog file</dd>
 * <dt><code>-w</code></dt>
 * <dd>Perform a well-formed parse, not a validating parse</dd>
 * <dt><code>-v</code></dt>
 * <dd>Perform a validating parse (the default)</dd>
 * <dt><code>-s</code></dt>
 * <dd>Enable W3C XML Schema validation</dd>
 * <dt><code>-S</code> <em>schema.xsd</em></dt>
 * <dd>Use schema.xsd for validation (implies -s)</dd>
 * <dt><code>-f</code></dt>
 * <dd>Enable full schema checking (implies -s)</dd>
 * <dt><code>-n</code></dt>
 * <dd>Perform a namespace-ignorant parse</dd>
 * <dt><code>-N</code></dt>
 * <dd>Perform a namespace-aware parse (the default)</dd>
 * <dt><code>-d</code> <em>integer</em></dt>
 * <dd>Set the debug level (warnings are level 2)</dd>
 * <dt><code>-E</code> <em>integer</em></dt>
 * <dd>Set the maximum number of errors to display</dd>
 * </dl>
 *
 * <p>The process ends with error-level 1, if there are errors.</p>
 *
 * @see org.apache.xml.resolver.tools.ResolvingXMLReader
 *
 * @author Norman Walsh
 * <a href="mailto:ndw@nwalsh.com">ndw@nwalsh.com</a>
 *
 * @version 1.0
 */
public class XJParse {
    private static Debug debug = CatalogManager.getStaticManager().debug;

    protected static final String SCHEMA_VALIDATION_FEATURE_ID
            = "http://apache.org/xml/features/validation/schema";

    protected static final String SCHEMA_FULL_CHECKING_FEATURE_ID
            = "http://apache.org/xml/features/validation/schema-full-checking";

    protected static final String EXTERNAL_SCHEMA_LOCATION_PROPERTY_ID
            = "http://apache.org/xml/properties/schema/external-schemaLocation";

    protected static final String EXTERNAL_NONS_SCHEMA_LOCATION_PROPERTY_ID
            = "http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation";

    /** The main entry point */
    public static void main (String[] args) throws FileNotFoundException, IOException {
        XJParse parser = new XJParse();
        parser.run3(args);
    }

    public void run3(String[] args) throws FileNotFoundException, IOException {
        CmdLineParser argparser = new CmdLineParser(debug, args);

        if (argparser.xmlfile == null && !argparser.fullChecking) {
            // Hack
            System.out.println("Usage: com.nwalsh.parsers.xjparse [opts] xmlfile");
            System.out.println("");
            System.out.println("Where:");
            System.out.println("");
            System.out.println("-c catalogfile   Load a particular catalog file");
            System.out.println("-w               Perform a well-formed parse, not a validating parse");
            System.out.println("-v               Perform a validating parse (the default)");
            System.out.println("-s               Enable W3C XML Schema validation");
            System.out.println("-S schema.xsd    Use schema.xsd for validation (implies -s)");
            System.out.println("-f               Enable full schema checking (implies -s)");
            System.out.println("-n               Perform a namespace-ignorant parse");
            System.out.println("-N               Perform a namespace-aware parse (the default)");
            System.out.println("-d integer       Set the debug level (warnings are level 2)");
            System.out.println("-E integer       Set the maximum number of errors to display");
            System.out.println("");
            System.out.println("The process ends with error-level 1, if there are errors.");
            System.exit(1);
        }

        XJParser xjparser = new XJParser();

        Hashtable<String,String> schemaList = lookupSchemas(argparser.xsdFiles);

        ResolvingXMLReader reader = new ResolvingXMLReader();
        Catalog catalog = reader.getCatalog();
        for (int count = 0; count < argparser.catalogFiles.size(); count++) {
            String file = (String) argparser.catalogFiles.elementAt(count);
            catalog.parseCatalog(file);
        }

        xjparser.setDtdValidate(argparser.validating);
        xjparser.setNamespaceAware(argparser.nsAware);
        xjparser.setMaxMessages(argparser.maxErrs);
        xjparser.setXsdValidate(argparser.useSchema || (!argparser.xsdFiles.isEmpty()));
        xjparser.setSchemas(schemaList.values());

        if (argparser.xmlfile != null) {
            String parseType = argparser.validating ? "validating" : "well-formed";
            String nsType = argparser.nsAware ? "namespace-aware" : "namespace-ignorant";
            if (argparser.maxErrs > 0) {
                System.out.println("Attempting "
                        + parseType
                        + ", "
                        + nsType
                        + " parse");
            }
            xjparser.parse(argparser.xmlfile);
        } else {
            System.exit(0);
        }

        xjparser.printParseStats();

        if (xjparser.getErrorCount() > 0) {
            System.exit(1);
        }
    }

    public void run(String[] args) throws FileNotFoundException, IOException {
        CmdLineParser argparser = new CmdLineParser(debug, args);

        if (argparser.xmlfile == null && !argparser.fullChecking) {
            // Hack
            System.out.println("Usage: com.nwalsh.parsers.xjparse [opts] xmlfile");
            System.out.println("");
            System.out.println("Where:");
            System.out.println("");
            System.out.println("-c catalogfile   Load a particular catalog file");
            System.out.println("-w               Perform a well-formed parse, not a validating parse");
            System.out.println("-v               Perform a validating parse (the default)");
            System.out.println("-s               Enable W3C XML Schema validation");
            System.out.println("-S schema.xsd    Use schema.xsd for validation (implies -s)");
            System.out.println("-f               Enable full schema checking (implies -s)");
            System.out.println("-n               Perform a namespace-ignorant parse");
            System.out.println("-N               Perform a namespace-aware parse (the default)");
            System.out.println("-d integer       Set the debug level (warnings are level 2)");
            System.out.println("-E integer       Set the maximum number of errors to display");
            System.out.println("");
            System.out.println("The process ends with error-level 1, if there are errors.");
            System.exit(1);
        }

        Hashtable<String,String> schemaList = lookupSchemas(argparser.xsdFiles);

        ResolvingXMLReader reader = new ResolvingXMLReader();

        try {
            argparser.nsAware = true;
            reader.setFeature("http://xml.org/sax/features/namespaces", argparser.nsAware);
            reader.setFeature("http://xml.org/sax/features/validation", argparser.validating);

            if (argparser.useSchema) {
                reader.setFeature(SCHEMA_VALIDATION_FEATURE_ID, argparser.useSchema);
                reader.setFeature(SCHEMA_FULL_CHECKING_FEATURE_ID, argparser.fullChecking);
                if (schemaList != null) {
                    String slh = "";
                    String nons_slh = "";

                    for (String ns : schemaList.keySet()) {
                        String xsd = schemaList.get(ns);
                        if ("".equals(ns)) {
                            nons_slh = xsd;
                            if (argparser.debuglevel > 0) {
                                System.err.println("Hint: ''=" + xsd);
                            }
                        } else {
                            if (!"".equals(slh)) {
                                slh = slh + " ";
                            }
                            slh = slh + ns + " " + xsd;
                            if (argparser.debuglevel > 0) {
                                System.err.println("Hint: " + ns + "=" + xsd);
                            }
                        }
                    }

                    if (!"".equals(slh)) {
                        reader.setProperty(EXTERNAL_SCHEMA_LOCATION_PROPERTY_ID, slh);
                    }

                    if (!"".equals(nons_slh)) {
                        reader.setProperty(EXTERNAL_NONS_SCHEMA_LOCATION_PROPERTY_ID, nons_slh);
                    }
                }
            }
        } catch (SAXException e) {
            // nop;
        }

        Catalog catalog = reader.getCatalog();

        for (int count = 0; count < argparser.catalogFiles.size(); count++) {
            String file = (String) argparser.catalogFiles.elementAt(count);
            catalog.parseCatalog(file);
        }

        XParseError xpe = new XParseError(argparser.showErrors, argparser.showWarnings);
        xpe.setMaxMessages(argparser.maxErrs);
        reader.setErrorHandler(xpe);

        Date startTime = null;

        try {
            if (argparser.xmlfile != null) {
                String parseType = argparser.validating ? "validating" : "well-formed";
                String nsType = argparser.nsAware ? "namespace-aware" : "namespace-ignorant";
                if (argparser.maxErrs > 0) {
                    System.out.println("Attempting "
                            + parseType
                            + ", "
                            + nsType
                            + " parse");
                }
                startTime = new Date();
                reader.parse(argparser.xmlfile);
            } else {
                System.exit(0);
            }

        } catch (SAXException sx) {
            System.out.println("SAX Exception: " + sx);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Date endTime = new Date();

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

        if (argparser.maxErrs > 0) {
            System.out.print("Parse ");
            if (xpe.getFatalCount() > 0) {
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

            int errCount = xpe.getErrorCount();
            int warnCount = xpe.getWarningCount();

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

        if (xpe.getErrorCount() > 0) {
            System.exit(1);
        }
    }

    private static Hashtable<String,String> lookupSchemas(Vector<String> xsdFiles) {
        Hashtable<String,String> mapping = new Hashtable<String,String> ();

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setIgnoringComments(true);
            dbf.setNamespaceAware(true);
            dbf.setValidating(false);
            DocumentBuilder db = dbf.newDocumentBuilder();

            for (String xsd : xsdFiles) {
                // Hack. Spaces will cause Xerces to fall over.
                xsd = xsd.replaceAll(" ","%20");

                Document doc = db.parse(xsd);
                Element s = doc.getDocumentElement();

                // Record the absolute URI so that path names relative to the current working
                // directory won't fail to resolve when considered relative to the base URI
                // of the document being validated.
                //
                // Would use doc.getBaseURI(), but it doesn't have one!?
                xsd = s.getBaseURI();
                
                String targetNS = s.getAttribute("targetNamespace");
                if (targetNS == null || "".equals(targetNS)) {
                    mapping.put("", xsd);
                } else {
                    mapping.put(targetNS, xsd);
                }
            }
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (SAXException se) {
            se.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return mapping;
    }
}
