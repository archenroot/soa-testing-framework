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
package com.ibm.soatf.component;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Enumeration of all component type supported by framework, useful on some places in the framework, where program flow is based on type of the component, or description of component type is needed
 * @author Ladislav Jech <archenroot@gmail.com>
 */
public enum SOATFCompType {
       
    /**
     *
     */
    DATABASE("Database Testing Framework Type Domain"),

    /**
     *
     */
    FILE("File Testing Framework Type Domain"),

    /**
     *
     */
    FTP("FTP Testing Framework Type Domain"),

    /**
     *
     */
    JMS("JMS Testing Framework Type Domain"),

    /**
     *
     */
    OSB("OSB Testing Framework Type Domain"),

    /**
     *
     */
    REST("REST Testing Framework Type Domain"),

    /**
     *
     */
    SOAP("SOAP Testing Framework Type Domain"),

    /**
     *
     */
    TOOL("TOOL Testing Framework Type Domain"),

    /**
     *
     */
    UTIL("UTIL Testing Framework Type Domain"),

    /**
     *
     */
    REPORT("REPORT Testing Framework Type Domain"),

    /**
     *
     */
    XML("XML Testing Framework Type Domain"),

    /**
     *
     */
    MAPPING("Mapping Testing Framework Type Domain");

    private static final Logger logger = LogManager.getLogger(SOATFCompType.class.getName());
    
    private final String displayName;

    SOATFCompType(final String displayDescription) {
        this.displayName = displayDescription;
    }

    /**
     * returns description of selected component type
     * @return
     */
    public String getDisplayName() {
        return displayName;
    }
}
