/*
 * Copyright (C) 2013 zANGETSu
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.archenroot.fw.soatest.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Enumeration;
import java.util.Vector;
import org.gibello.zql.ParseException;
import org.gibello.zql.ZInsert;
import org.gibello.zql.ZQuery;
import org.gibello.zql.ZStatement;
import org.gibello.zql.ZqlParser;

/**
 *
 * @author zANGETSu
 */
public class ValidateTransferedValues {

    public ValidateTransferedValues() {
    }

    public void validateValuesFromFile(String sqlScriptFileName, String messageFileName) throws FileNotFoundException, ParseException {
        String sqlScriptFileContent = null;
        String messageScriptFileContent = null;

        ZqlParser zqlParser = new ZqlParser();
        boolean sqlfileexists = new File(sqlScriptFileName).exists();
        boolean messagefileexists = new File(messageFileName).exists();

        zqlParser.initParser(new FileInputStream(new File(sqlScriptFileName)));

        ZStatement zs = zqlParser.readStatement();
        System.out.println("Input statement: " + zs.toString());

        if (zs instanceof ZQuery) {

            System.out.println("Je to select statement");
        } else if (zs instanceof ZInsert) {
            System.out.println("Je to insert statement");
            ZInsert zi = (ZInsert) zs;
            int i = 0;
            Vector columns = zi.getColumns();
            Vector values = zi.getValues();

            Enumeration colEnum = columns.elements();
            Enumeration valEnum = values.elements();
            String dbColumnName = null;
            String dbValue = null;
            String messageElementName = null;
            String messageElementValue = null;
            String messageValue = null;

            while (colEnum.hasMoreElements()) {

                dbColumnName = colEnum.nextElement().toString();
                if (!dbColumnName.equals("REC_POOLED")) {
                    dbValue = valEnum.nextElement().toString();
                    System.out.println("DB column: " + dbColumnName);
                    System.out.println("DB value: " + dbValue);
                    messageElementName = constructXMLElementNameFromDBColumn(dbColumnName);
                    messageValue = getElementFromFile(messageElementName, messageFileName);
                    System.out.println("MS column: " + messageElementName);
                    System.out.println("MS value: " + messageValue);
                }
            }

        }
    }

    private String constructXMLElementNameFromDBColumn(String dbColumnName) {
        String elementName = null;

        String[] parts = dbColumnName.split("_");
        boolean first = true;
        for (String part : parts) {
            if (first) {
                elementName = part.toLowerCase();
                first = false;
            } else {
                String firstSymbol = part.substring(0, 1).toUpperCase();
                String restSymbols = part.substring(2, part.length()).toLowerCase();
                elementName += firstSymbol + restSymbols;
            }

        }
        return elementName;
    }

    private String getElementFromFile(String messageElementName, String file) {
        BufferedReader br = null;
        String searchFor = ":" + messageElementName + ">";
        String elementValue = null;
        String sCurrentLine;
        try {
            br = new BufferedReader(
                    new FileReader(file));
            while ((sCurrentLine = br.readLine()) != null) {
                if (sCurrentLine.contains(searchFor)) {
                    int indexBegin = sCurrentLine.indexOf(searchFor);
                    int indexEnd = sCurrentLine.substring(indexBegin).indexOf("<");
                    elementValue = sCurrentLine.substring(indexBegin + searchFor.length(), indexBegin + indexEnd);
                    //System.out.println(elementValue);
                }
                // System.out.println(sCurrentLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return elementValue;
    }
}
