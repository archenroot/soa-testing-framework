//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.5-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.04.02 at 10:57:17 AM BST 
//


package com.ibm.soatf.config.master;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PreOrPostExecBlockOperation complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PreOrPostExecBlockOperation">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.ibm.com/SOATF/Config/Master}Operation">
 *       &lt;attribute name="executionBlockRef" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PreOrPostExecBlockOperation")
public class PreOrPostExecBlockOperation
    extends Operation
{

    @XmlAttribute(name = "executionBlockRef", required = true)
    protected String executionBlockRef;

    /**
     * Gets the value of the executionBlockRef property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExecutionBlockRef() {
        return executionBlockRef;
    }

    /**
     * Sets the value of the executionBlockRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExecutionBlockRef(String value) {
        this.executionBlockRef = value;
    }

}
