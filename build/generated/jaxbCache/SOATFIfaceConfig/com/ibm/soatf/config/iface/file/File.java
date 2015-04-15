//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.5-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.04.02 at 10:57:18 AM BST 
//


package com.ibm.soatf.config.iface.file;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for File complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="File">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="fileName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="directory" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="archiveDirectory" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="errorArchiveDirectory" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="fileContent" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "File", propOrder = {
    "fileName",
    "directory",
    "archiveDirectory",
    "errorArchiveDirectory",
    "fileContent"
})
public class File {

    @XmlElement(required = true)
    protected String fileName;
    @XmlElement(required = true)
    protected String directory;
    @XmlElement(required = true)
    protected String archiveDirectory;
    @XmlElement(required = true)
    protected String errorArchiveDirectory;
    protected String fileContent;

    /**
     * Gets the value of the fileName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Sets the value of the fileName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFileName(String value) {
        this.fileName = value;
    }

    /**
     * Gets the value of the directory property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDirectory() {
        return directory;
    }

    /**
     * Sets the value of the directory property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDirectory(String value) {
        this.directory = value;
    }

    /**
     * Gets the value of the archiveDirectory property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getArchiveDirectory() {
        return archiveDirectory;
    }

    /**
     * Sets the value of the archiveDirectory property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setArchiveDirectory(String value) {
        this.archiveDirectory = value;
    }

    /**
     * Gets the value of the errorArchiveDirectory property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getErrorArchiveDirectory() {
        return errorArchiveDirectory;
    }

    /**
     * Sets the value of the errorArchiveDirectory property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setErrorArchiveDirectory(String value) {
        this.errorArchiveDirectory = value;
    }

    /**
     * Gets the value of the fileContent property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFileContent() {
        return fileContent;
    }

    /**
     * Sets the value of the fileContent property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFileContent(String value) {
        this.fileContent = value;
    }

}
