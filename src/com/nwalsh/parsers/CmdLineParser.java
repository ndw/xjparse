/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.nwalsh.parsers;

import java.util.Vector;
import org.apache.xml.resolver.helpers.Debug;

/**
 *
 * @author ndw
 */
public class CmdLineParser {
    String  xmlfile    = null;
    int     debuglevel = 0;
    int     maxErrs    = 10;
    boolean nsAware    = true;
    boolean validating = true;
    boolean useSchema  = false;
    boolean showWarnings = (debuglevel > 2);
    boolean showErrors = true;
    boolean fullChecking = false; // true implies useSchema
    Vector<String> xsdFiles = new Vector<String> ();
    Vector<String> catalogFiles = new Vector<String> ();

    public CmdLineParser(Debug debug, String[] args) {
        for (int i=0; i<args.length; i++) {
            if (args[i].equals("-c")) {
                ++i;
                catalogFiles.add(args[i]);
                continue;
            }

            if (args[i].equals("-w")) {
                validating = false;
                continue;
            }

            if (args[i].equals("-v")) {
                validating = true;
                continue;
            }

            if (args[i].equals("-s")) {
                useSchema = true;
                continue;
            }

            if (args[i].equals("-S")) {
                ++i;
                xsdFiles.add(args[i]);
                useSchema = true;
                continue;
            }

            if (args[i].equals("-f")) {
                fullChecking = true;
                useSchema = true;
                continue;
            }

            if (args[i].equals("-n")) {
                nsAware = false;
                continue;
            }

            if (args[i].equals("-N")) {
                nsAware = true;
                continue;
            }

            if (args[i].equals("-d")) {
                ++i;
                String debugstr = args[i];
                try {
                    debuglevel = Integer.parseInt(debugstr);
                    if (debuglevel >= 0) {
                        if (debug != null) {
                            debug.setDebug(debuglevel);
                        }
                        showWarnings = (debuglevel > 2);
                    }
                } catch (Exception e) {
                    System.err.println("Not an integer: "+debugstr+" after -d");
                }
                continue;
            }

            if (args[i].equals("-E")) {
                ++i;
                String errstr = args[i];
                try {
                    int errs = Integer.parseInt(errstr);
                    if (errs >= 0) {
                        maxErrs = errs;
                    }
                } catch (Exception e) {
                    // nop
                }
                continue;
            }

            xmlfile = args[i];
        }
    }
}
