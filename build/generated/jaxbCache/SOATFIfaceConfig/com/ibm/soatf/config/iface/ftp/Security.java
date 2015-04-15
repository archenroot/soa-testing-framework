//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.5-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.04.02 at 10:57:18 AM BST 
//


package com.ibm.soatf.config.iface.ftp;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Security.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="Security">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}normalizedString">
 *     &lt;enumeration value="None"/>
 *     &lt;enumeration value="SSL"/>
 *     &lt;enumeration value="SSH"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "Security")
@XmlEnum
public enum Security {

    @XmlEnumValue("None")
    NONE("None"),
    SSL("SSL"),
    SSH("SSH");
    private final String value;

    Security(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static Security fromValue(String v) {
        for (Security c: Security.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
