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
package com.ibm.soatf.tool;

import com.ibm.soatf.component.database.DatabaseComponent;
import com.ibm.soatf.component.jms.JmsComponent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gibello.zql.ParseException;
import org.gibello.zql.ZInsert;
import org.gibello.zql.ZQuery;
import org.gibello.zql.ZStatement;
import org.gibello.zql.ZqlParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;

/**
 *
 * @author zANGETSu
 */
public class ValidateTransferedValues {
    
    private static final Logger logger = LogManager.getLogger(ValidateTransferedValues.class.getName());

    public ValidateTransferedValues() {
    }
    
    public static boolean validateValuesFromFile(File sqlScriptFile, File messageFile, Map<String,String> mappings) throws FileNotFoundException, ParseException, java.text.ParseException {
        if (sqlScriptFile == null || messageFile == null || !sqlScriptFile.exists() || !messageFile.exists()) {
            throw new FileNotFoundException();
        }

        ZqlParser zqlParser = new ZqlParser();   
        zqlParser.addCustomFunction("TO_DATE", 2);

        zqlParser.initParser(new FileInputStream(sqlScriptFile));

        ZStatement zs = zqlParser.readStatement();
        System.out.println("Input statement: " + zs.toString());

        boolean valid = true;
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
            String messageValue = null;

            Pattern pattern =  Pattern.compile("'([^']+)'");
            
            while (colEnum.hasMoreElements()) {
                
                boolean dateCompare = false;
                dbColumnName = colEnum.nextElement().toString();
                if(mappings != null && mappings.containsKey(dbColumnName)) {
                    messageElementName = mappings.get(dbColumnName);
                    if (messageElementName == null || "".equals(messageElementName)) {
                        //null v toColumnName - neporovnavam
                        logger.debug("Skipping column "+dbColumnName);
                        valEnum.nextElement();
                        continue;
                    }
                } else {
                    messageElementName = constructXMLElementNameFromDBColumn(dbColumnName);
                }
                dbValue = valEnum.nextElement().toString();
                if (dbValue != null && dbValue.startsWith("TO_DATE")) dateCompare = true;
                Matcher matcher = pattern.matcher(dbValue);
                if (matcher.find()) {
                     dbValue = matcher.group(1);
                }
                logger.debug("Comparing values for column "+dbColumnName+" and coresponding element "+messageElementName);
                messageValue = getElementFromFile(messageElementName, messageFile, false);
                boolean differ = false;
                if (dateCompare) {
                    final Date dbDate = DatabaseComponent.DATE_FORMAT.parse(dbValue);
                    //TODO: timezone //Date xmlDate = DatatypeConverter.parseDate(messageValue).getTime();
                    Date xmlDate = (messageValue == null || messageValue.length() < 19) ? null : JmsComponent.DATE_FORMAT.parse(messageValue.substring(0,19));
                    differ ^= dbDate.equals(xmlDate);
                } else {
                    if (dbValue == null) {
                        if (messageValue != null) {
                            differ = true;
                        } else {
                            differ = false;
                        }
                    } else {
                        differ = !dbValue.equals(messageValue);
                    }
                }
                if (differ) {
                    logger.debug("values are different: " + dbValue + " <> " + messageValue);
                    valid = false;
                } else {
                    logger.debug("values are equal");
                }
            }

        }
        return valid;
    }
    
    public static boolean validateElementValuesFromFile(File srcMessageFile, File destMessageFile, Map<String,String> mappings) throws FileNotFoundException, ParseException, java.text.ParseException {
        if (srcMessageFile == null || destMessageFile == null || !srcMessageFile.exists() || !destMessageFile.exists()) {
            throw new FileNotFoundException();
        }


        boolean valid = true;

        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(srcMessageFile);

            DocumentTraversal traversal = (DocumentTraversal) doc;
            
            String srcElementName;
            String destElementName;
            String srcValue;
            String destValue;

            NodeIterator iterator = traversal.createNodeIterator(doc.getDocumentElement(), NodeFilter.SHOW_ELEMENT, null, true);
            for (Node n = iterator.nextNode(); n != null; n = iterator.nextNode()) {
                Element e = (Element)n;
                if (isLeafElement(e)) {
                    srcElementName = e.getTagName();
                    if (srcElementName.indexOf(":") >= 0) {
                        srcElementName = srcElementName.substring(srcElementName.indexOf(":")+1);
                    }
                    if(mappings != null && mappings.containsKey(srcElementName)) {
                        destElementName = mappings.get(srcElementName);
                        if (destElementName == null || "".equals(destElementName)) {
                            //null v toColumnName - neporovnavam
                            logger.debug("Skipping column "+srcElementName);
                            continue;
                        }
                    } else {
                        destElementName = srcElementName;
                    }
                    logger.debug("Comparing values for element "+srcElementName+" and coresponding element "+destElementName);
                    srcValue = getElementFromFile(srcElementName, srcMessageFile, false);
                    destValue = getElementFromFile(destElementName, destMessageFile, true);
                    boolean differ = ((srcValue == null && destValue != null) || !srcValue.equals(destValue));
                    if (differ) {
                        logger.debug("values are different: " + srcValue + " <> " + destValue);
                        valid = false;
                    } else {
                        logger.debug("values are equal");
                    }                    
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            valid = false;
        }
        
        return valid;
    }
    
    private static boolean isLeafElement(Element e) {
        boolean leaf = true;
        if(e.hasChildNodes()) {
            NodeList list =e.getChildNodes();
            for(int i=0; i < list.getLength(); i++) {
                if (list.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    leaf = false;
                    break;
                }
            }
        }
        return leaf;
    }

    private static String constructXMLElementNameFromDBColumn(String dbColumnName) {
        String elementName = null;

        final String[] parts = dbColumnName.split("_");
        boolean first = true;
        for (String part : parts) {
            if (first) {
                elementName = part.toLowerCase();
                first = false;
            } else {
                final String firstSymbol = part.substring(0, 1).toUpperCase();
                final String restSymbols = part.substring(1, part.length()).toLowerCase();
                StringBuffer sb = new StringBuffer();
                sb.append(firstSymbol);
                sb.append(restSymbols);
                elementName = sb.toString();
            }

        }
        return elementName;
    }

    private static String getElementFromFile(String messageElementName, File file, boolean ignoreCase) {
        BufferedReader br = null;
        String searchFor = ":" + messageElementName + ">";
        String elementValue = null;
        String sCurrentLine;
        try {
            br = new BufferedReader(new FileReader(file));
            while ((sCurrentLine = br.readLine()) != null) {
                if (ignoreCase) {
                     if (StringUtils.containsIgnoreCase(sCurrentLine, searchFor)) {
                         int indexBegin = sCurrentLine.toLowerCase().indexOf(searchFor.toLowerCase());
                         int indexEnd = sCurrentLine.substring(indexBegin).indexOf("<");
                         elementValue = sCurrentLine.substring(indexBegin + searchFor.length(), indexBegin + indexEnd);
                         break;
                     }
                } else {
                     if (sCurrentLine.contains(searchFor)) {
                        int indexBegin = sCurrentLine.indexOf(searchFor);
                        int indexEnd = sCurrentLine.substring(indexBegin).indexOf("<");
                        elementValue = sCurrentLine.substring(indexBegin + searchFor.length(), indexBegin + indexEnd);
                        break;
                        //System.out.println(elementValue);
                    }
                    // System.out.println(sCurrentLine);                   
                }
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
