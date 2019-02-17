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
import java.util.Hashtable;
import java.util.Vector;

import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlresolver.Catalog;
import org.xmlresolver.Resolver;

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
 * @author Norman Walsh
 * <a href="mailto:ndw@nwalsh.com">ndw@nwalsh.com</a>
 *
 * @version 1.0
 */
public class XJParse {
    private static final String VERSION = BuildConfig.VERSION;

    public static void main (String[] args) throws FileNotFoundException, IOException {
        XJParse parser = new XJParse();
        parser.run(args);
    }

    private void run(String[] args) throws FileNotFoundException, IOException {
        CmdLineParser argparser = new CmdLineParser(args);

        if (argparser.xmlfile == null && !argparser.fullChecking) {
            System.out.println("XJParse version " + VERSION);
            System.out.println("Usage: com.nwalsh.parsers.xjparse [opts] xmlfile");
            System.out.println("");
            System.out.println("Where:");
            System.out.println("");
            System.out.println("-c catalogfile   Load a particular catalog file");
            System.out.println("-v               Enable DTD validation");
            System.out.println("-s               Enable W3C XML Schema validation");
            System.out.println("-S schema.xsd    Use schema.xsd for validation (implies -s)");
            System.out.println("-f               Enable full schema checking (implies -s)");
            System.out.println("-n               Perform a namespace-ignorant parse (namespace aware by default)");
            System.out.println("-E integer       Set the maximum number of errors to display");
            System.out.println("-d               Debug (dump stack trace on exception)");
            System.out.println("-q               Quiet mode; don't print errors and warnings");
            System.out.println("");
            System.out.println("The process ends with error-level 1, if there are errors.");
            System.exit(1);
        }

        XJParser xjparser = new XJParser();

        Hashtable<String,String> schemaList = lookupSchemas(argparser.xsdFiles);

        StringBuilder catalogList = new StringBuilder();
        int catCount = 0;
        for (String catalogFile : argparser.catalogFiles) {
            if (catCount > 0) {
                catalogList.append(";");
            }
            catCount += 1;
            catalogList.append(catalogFile);
        }

        Resolver resolver = null;
        
        if (catCount > 0) {
            Catalog catalog = new Catalog(catalogList.toString());
            resolver = new Resolver(catalog);
        } else {
            resolver = new Resolver();
        }

        xjparser.setDtdValidate(argparser.validating);
        xjparser.setNamespaceAware(argparser.nsAware);
        xjparser.setMaxMessages(argparser.maxErrs);
        xjparser.setXsdValidate(argparser.useSchema || (!argparser.xsdFiles.isEmpty()));
        xjparser.setFullChecking(argparser.fullChecking);
        xjparser.setDebug(argparser.debug);
        xjparser.setResolver(resolver);

        if (schemaList.size() > 0) {
            xjparser.setSchemas(schemaList.values());
        }

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
                xsd = xsd.replaceAll(" ", "%20");

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
        }  catch (FileNotFoundException fnf) {
            System.err.println(fnf.toString());
        } catch (ParserConfigurationException | SAXException | IOException pce) {
            pce.printStackTrace();
        }

        return mapping;
    }
}
