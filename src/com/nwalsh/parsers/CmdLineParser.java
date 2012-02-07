/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.nwalsh.parsers;

import java.util.Vector;

/**
 *
 * @author ndw
 */
public class CmdLineParser {
    String  xmlfile    = null;
    int     maxErrs    = 10;
    boolean nsAware    = true;
    boolean validating = false;
    boolean useSchema  = false;
    boolean fullChecking = false; // true implies useSchema
    Vector<String> xsdFiles = new Vector<String> ();
    Vector<String> catalogFiles = new Vector<String> ();

    public CmdLineParser(String[] args) {
        for (int i=0; i<args.length; i++) {
            if (args[i].equals("-c")) {
                ++i;
                catalogFiles.add(args[i]);
                continue;
            }

            // Ignored for legacy reasons
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
