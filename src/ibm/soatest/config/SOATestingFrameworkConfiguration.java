//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.5-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.10.10 at 12:11:36 PM BST 
//


package ibm.soatest.config;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SOATestingFrameworkConfiguration complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SOATestingFrameworkConfiguration">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="database" type="{http://www.archenroot.org/SOATestingFramework/Config/Database}DatabaseConfiguration" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="jms" type="{http://www.archenroot.org/SOATestingFramework/Config/JMS}JMSConfiguration" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="osb" type="{http://www.archenroot.org/SOATestingFramework/Config/OSB}OSBConfiguration"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SOATestingFrameworkConfiguration", namespace = "http://www.archenroot.org/SOATestingFramework/Config", propOrder = {
    "database",
    "jms",
    "osb",
    "mapping"
})
@XmlRootElement(name = "soaTestingFrameworkConfiguration", namespace = "http://www.archenroot.org/SOATestingFramework/Config")
public class SOATestingFrameworkConfiguration {

    protected List<DatabaseConfiguration> database;
    protected List<JMSConfiguration> jms;
    @XmlElement(required = true)
    protected OSBConfiguration osb;
    protected List<MappingConfiguration> mapping;

    /**
     * Gets the value of the database property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the database property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDatabase().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DatabaseConfiguration }
     * 
     * 
     */
    public List<DatabaseConfiguration> getDatabase() {
        if (database == null) {
            database = new ArrayList<DatabaseConfiguration>();
        }
        return this.database;
    }

    /**
     * Gets the value of the jms property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the jms property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getJms().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JMSConfiguration }
     * 
     * 
     */
    public List<JMSConfiguration> getJms() {
        if (jms == null) {
            jms = new ArrayList<JMSConfiguration>();
        }
        return this.jms;
    }

    /**
     * Gets the value of the osb property.
     * 
     * @return
     *     possible object is
     *     {@link OSBConfiguration }
     *     
     */
    public OSBConfiguration getOsb() {
        return osb;
    }

    /**
     * Sets the value of the osb property.
     * 
     * @param value
     *     allowed object is
     *     {@link OSBConfiguration }
     *     
     */
    public void setOsb(OSBConfiguration value) {
        this.osb = value;
    }

    /**
     * Gets the value of the mapping property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the mapping property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMapping().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MappingConfiguration }
     * 
     * 
     */
    public List<MappingConfiguration> getMapping() {
        if (mapping == null) {
            mapping = new ArrayList<MappingConfiguration>();
        }
        return this.mapping;
    }
}
