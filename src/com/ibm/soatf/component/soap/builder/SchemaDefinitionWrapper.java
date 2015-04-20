/**
 * Copyright (c) 2012-2013 Reficio (TM) - Reestablish your software!. All Rights Reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package com.ibm.soatf.component.soap.builder;

import com.ibm.soatf.gui.ProgressMonitor;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.wsdl.Definition;
import javax.xml.namespace.QName;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;

/**
 * This class was extracted from the soapUI code base by centeractive ag in October 2011.
 * The main reason behind the extraction was to separate the code that is responsible
 * for the generation of the SOAP messages from the rest of the soapUI's code that is
 * tightly coupled with other modules, such as soapUI's graphical user interface, etc.
 * The goal was to create an open-source java project whose main responsibility is to
 * handle SOAP message generation and SOAP transmission purely on an XML level.
 * <br/>
 * centeractive ag would like to express strong appreciation to SmartBear Software and
 * to the whole team of soapUI's developers for creating soapUI and for releasing its
 * source code under a free and open-source licence. centeractive ag extracted and
 * modifies some parts of the soapUI's code in good faith, making every effort not
 * to impair any existing functionality and to supplement it according to our
 * requirements, applying best practices of software design.
 * <p/>
 * Changes done:
 * - changing location in the package structure
 * - removal of dependencies and code parts that are out of scope of SOAP message generation
 * - minor fixes to make the class compile out of soapUI's code base
 */
public class SchemaDefinitionWrapper {
    private final static Logger logger = LogManager.getLogger(SchemaDefinitionWrapper.class);
    private SchemaTypeSystem schemaTypes;
    private SchemaTypeLoader schemaTypeLoader;

    private Definition definition;

    public SchemaDefinitionWrapper(Definition definition, String schemaURL) {
        this.definition = definition;
        loadSchemaTypes(new UrlSchemaLoader(schemaURL));
    }

    public SchemaTypeLoader getSchemaTypeLoader() {
        return schemaTypeLoader;
    }

    public SchemaTypeSystem getSchemaTypeSystem() {
        return schemaTypes;
    }

    public boolean hasSchemaTypes() {
        return schemaTypes != null;
    }

    public Collection<String> getDefinedNamespaces() throws Exception {
        Set<String> namespaces = new HashSet<String>();

        SchemaTypeSystem schemaTypes = getSchemaTypeSystem();
        if (schemaTypes != null) {
            namespaces.addAll(SchemaUtils.extractNamespaces(getSchemaTypeSystem(), true));
        }

        namespaces.add(getTargetNamespace());

        return namespaces;
    }

    public String getTargetNamespace() {
        return WsdlUtils.getTargetNamespace(definition);
    }

    public SchemaType findType(QName typeName) {
        return getSchemaTypeLoader().findType(typeName);
    }

    private void loadSchemaTypes(DefinitionLoader loader) {
        String msg = "Loading schema types...";
        ProgressMonitor.increment(msg);
        logger.debug(msg);
        schemaTypes = SchemaUtils.loadSchemaTypes(loader.getBaseURI(), loader);
        msg = "Getting schema type loader...";
        ProgressMonitor.increment(msg);
        logger.debug(msg);
        schemaTypeLoader = XmlBeans.typeLoaderUnion(new SchemaTypeLoader[]{schemaTypes,
                XmlBeans.getBuiltinTypeSystem()});
    }

}
