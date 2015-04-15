//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.5-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.04.02 at 10:57:18 AM BST 
//


package com.ibm.soatf.config.iface;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.ibm.soatf.config.iface.util.UTILConfig;


/**
 * <p>Java class for SOATFIfaceConfig complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SOATFIfaceConfig">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ifaceEndPoints" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="ifaceEndPoint" maxOccurs="unbounded">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;extension base="{http://www.ibm.com/SOATF/Config/Iface}IfaceEndpoint">
 *                           &lt;attribute name="endPointLocalId" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                         &lt;/extension>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="utilConfig" type="{http://www.ibm.com/SOATF/Config/Iface/UTIL}UTILConfig" minOccurs="0"/>
 *         &lt;element name="ifaceFlowPatternConfig" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="ifaceFlowPattern" type="{http://www.ibm.com/SOATF/Config/Iface}IfaceFlowPattern" maxOccurs="unbounded"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SOATFIfaceConfig", propOrder = {
    "ifaceEndPoints",
    "utilConfig",
    "ifaceFlowPatternConfig"
})
public class SOATFIfaceConfig {

    protected SOATFIfaceConfig.IfaceEndPoints ifaceEndPoints;
    protected UTILConfig utilConfig;
    protected SOATFIfaceConfig.IfaceFlowPatternConfig ifaceFlowPatternConfig;

    /**
     * Gets the value of the ifaceEndPoints property.
     * 
     * @return
     *     possible object is
     *     {@link SOATFIfaceConfig.IfaceEndPoints }
     *     
     */
    public SOATFIfaceConfig.IfaceEndPoints getIfaceEndPoints() {
        return ifaceEndPoints;
    }

    /**
     * Sets the value of the ifaceEndPoints property.
     * 
     * @param value
     *     allowed object is
     *     {@link SOATFIfaceConfig.IfaceEndPoints }
     *     
     */
    public void setIfaceEndPoints(SOATFIfaceConfig.IfaceEndPoints value) {
        this.ifaceEndPoints = value;
    }

    /**
     * Gets the value of the utilConfig property.
     * 
     * @return
     *     possible object is
     *     {@link UTILConfig }
     *     
     */
    public UTILConfig getUtilConfig() {
        return utilConfig;
    }

    /**
     * Sets the value of the utilConfig property.
     * 
     * @param value
     *     allowed object is
     *     {@link UTILConfig }
     *     
     */
    public void setUtilConfig(UTILConfig value) {
        this.utilConfig = value;
    }

    /**
     * Gets the value of the ifaceFlowPatternConfig property.
     * 
     * @return
     *     possible object is
     *     {@link SOATFIfaceConfig.IfaceFlowPatternConfig }
     *     
     */
    public SOATFIfaceConfig.IfaceFlowPatternConfig getIfaceFlowPatternConfig() {
        return ifaceFlowPatternConfig;
    }

    /**
     * Sets the value of the ifaceFlowPatternConfig property.
     * 
     * @param value
     *     allowed object is
     *     {@link SOATFIfaceConfig.IfaceFlowPatternConfig }
     *     
     */
    public void setIfaceFlowPatternConfig(SOATFIfaceConfig.IfaceFlowPatternConfig value) {
        this.ifaceFlowPatternConfig = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="ifaceEndPoint" maxOccurs="unbounded">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;extension base="{http://www.ibm.com/SOATF/Config/Iface}IfaceEndpoint">
     *                 &lt;attribute name="endPointLocalId" type="{http://www.w3.org/2001/XMLSchema}string" />
     *               &lt;/extension>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "ifaceEndPoint"
    })
    public static class IfaceEndPoints {

        @XmlElement(required = true)
        protected List<SOATFIfaceConfig.IfaceEndPoints.IfaceEndPoint> ifaceEndPoint;

        /**
         * Gets the value of the ifaceEndPoint property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the ifaceEndPoint property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getIfaceEndPoint().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link SOATFIfaceConfig.IfaceEndPoints.IfaceEndPoint }
         * 
         * 
         */
        public List<SOATFIfaceConfig.IfaceEndPoints.IfaceEndPoint> getIfaceEndPoint() {
            if (ifaceEndPoint == null) {
                ifaceEndPoint = new ArrayList<SOATFIfaceConfig.IfaceEndPoints.IfaceEndPoint>();
            }
            return this.ifaceEndPoint;
        }


        /**
         * <p>Java class for anonymous complex type.
         * 
         * <p>The following schema fragment specifies the expected content contained within this class.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;extension base="{http://www.ibm.com/SOATF/Config/Iface}IfaceEndpoint">
         *       &lt;attribute name="endPointLocalId" type="{http://www.w3.org/2001/XMLSchema}string" />
         *     &lt;/extension>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class IfaceEndPoint
            extends IfaceEndpoint
        {

            @XmlAttribute(name = "endPointLocalId")
            protected String endPointLocalId;

            /**
             * Gets the value of the endPointLocalId property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getEndPointLocalId() {
                return endPointLocalId;
            }

            /**
             * Sets the value of the endPointLocalId property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setEndPointLocalId(String value) {
                this.endPointLocalId = value;
            }

        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="ifaceFlowPattern" type="{http://www.ibm.com/SOATF/Config/Iface}IfaceFlowPattern" maxOccurs="unbounded"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "ifaceFlowPattern"
    })
    public static class IfaceFlowPatternConfig {

        @XmlElement(required = true)
        protected List<IfaceFlowPattern> ifaceFlowPattern;

        /**
         * Gets the value of the ifaceFlowPattern property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the ifaceFlowPattern property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getIfaceFlowPattern().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link IfaceFlowPattern }
         * 
         * 
         */
        public List<IfaceFlowPattern> getIfaceFlowPattern() {
            if (ifaceFlowPattern == null) {
                ifaceFlowPattern = new ArrayList<IfaceFlowPattern>();
            }
            return this.ifaceFlowPattern;
        }

    }

}
