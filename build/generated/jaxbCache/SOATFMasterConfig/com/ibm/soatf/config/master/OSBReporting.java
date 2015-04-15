//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.5-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.04.02 at 10:57:17 AM BST 
//


package com.ibm.soatf.config.master;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for OSBReporting complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OSBReporting">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="osbReportingInstance" maxOccurs="unbounded">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;extension base="{http://www.ibm.com/SOATF/Config/Master}AbstractMasterConfigEnvironmentInstance">
 *                 &lt;sequence>
 *                   &lt;element name="hostName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="port" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *                   &lt;element name="userName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="password" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;choice>
 *                     &lt;element name="serviceId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                     &lt;element name="serviceName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;/choice>
 *                 &lt;/sequence>
 *                 &lt;attribute name="environment" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/extension>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="dbObjects">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="reportEventsTable">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" default="REPORTING_EVENTS" />
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="errorsTable">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" default="XXXIW_MWP_ERRORS" />
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="errorDetailsTable">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" default="XXXIW_MWP_ERROR_DETAILS" />
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="gatherOSBReportsProcedure">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" default="ERROR_HOSPITAL.gather_osb_data" />
 *                           &lt;attribute name="scheduledInterval" type="{http://www.w3.org/2001/XMLSchema}int" default="60" />
 *                           &lt;attribute name="forceProcedureExecution" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
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
@XmlType(name = "OSBReporting", propOrder = {
    "osbReportingInstance",
    "dbObjects"
})
public class OSBReporting {

    @XmlElement(required = true)
    protected List<OSBReporting.OsbReportingInstance> osbReportingInstance;
    @XmlElement(required = true)
    protected OSBReporting.DbObjects dbObjects;

    /**
     * Gets the value of the osbReportingInstance property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the osbReportingInstance property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOsbReportingInstance().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OSBReporting.OsbReportingInstance }
     * 
     * 
     */
    public List<OSBReporting.OsbReportingInstance> getOsbReportingInstance() {
        if (osbReportingInstance == null) {
            osbReportingInstance = new ArrayList<OSBReporting.OsbReportingInstance>();
        }
        return this.osbReportingInstance;
    }

    /**
     * Gets the value of the dbObjects property.
     * 
     * @return
     *     possible object is
     *     {@link OSBReporting.DbObjects }
     *     
     */
    public OSBReporting.DbObjects getDbObjects() {
        return dbObjects;
    }

    /**
     * Sets the value of the dbObjects property.
     * 
     * @param value
     *     allowed object is
     *     {@link OSBReporting.DbObjects }
     *     
     */
    public void setDbObjects(OSBReporting.DbObjects value) {
        this.dbObjects = value;
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
     *         &lt;element name="reportEventsTable">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" default="REPORTING_EVENTS" />
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="errorsTable">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" default="XXXIW_MWP_ERRORS" />
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="errorDetailsTable">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" default="XXXIW_MWP_ERROR_DETAILS" />
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="gatherOSBReportsProcedure">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" default="ERROR_HOSPITAL.gather_osb_data" />
     *                 &lt;attribute name="scheduledInterval" type="{http://www.w3.org/2001/XMLSchema}int" default="60" />
     *                 &lt;attribute name="forceProcedureExecution" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
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
    @XmlType(name = "", propOrder = {
        "reportEventsTable",
        "errorsTable",
        "errorDetailsTable",
        "gatherOSBReportsProcedure"
    })
    public static class DbObjects {

        @XmlElement(required = true)
        protected OSBReporting.DbObjects.ReportEventsTable reportEventsTable;
        @XmlElement(required = true)
        protected OSBReporting.DbObjects.ErrorsTable errorsTable;
        @XmlElement(required = true)
        protected OSBReporting.DbObjects.ErrorDetailsTable errorDetailsTable;
        @XmlElement(required = true)
        protected OSBReporting.DbObjects.GatherOSBReportsProcedure gatherOSBReportsProcedure;

        /**
         * Gets the value of the reportEventsTable property.
         * 
         * @return
         *     possible object is
         *     {@link OSBReporting.DbObjects.ReportEventsTable }
         *     
         */
        public OSBReporting.DbObjects.ReportEventsTable getReportEventsTable() {
            return reportEventsTable;
        }

        /**
         * Sets the value of the reportEventsTable property.
         * 
         * @param value
         *     allowed object is
         *     {@link OSBReporting.DbObjects.ReportEventsTable }
         *     
         */
        public void setReportEventsTable(OSBReporting.DbObjects.ReportEventsTable value) {
            this.reportEventsTable = value;
        }

        /**
         * Gets the value of the errorsTable property.
         * 
         * @return
         *     possible object is
         *     {@link OSBReporting.DbObjects.ErrorsTable }
         *     
         */
        public OSBReporting.DbObjects.ErrorsTable getErrorsTable() {
            return errorsTable;
        }

        /**
         * Sets the value of the errorsTable property.
         * 
         * @param value
         *     allowed object is
         *     {@link OSBReporting.DbObjects.ErrorsTable }
         *     
         */
        public void setErrorsTable(OSBReporting.DbObjects.ErrorsTable value) {
            this.errorsTable = value;
        }

        /**
         * Gets the value of the errorDetailsTable property.
         * 
         * @return
         *     possible object is
         *     {@link OSBReporting.DbObjects.ErrorDetailsTable }
         *     
         */
        public OSBReporting.DbObjects.ErrorDetailsTable getErrorDetailsTable() {
            return errorDetailsTable;
        }

        /**
         * Sets the value of the errorDetailsTable property.
         * 
         * @param value
         *     allowed object is
         *     {@link OSBReporting.DbObjects.ErrorDetailsTable }
         *     
         */
        public void setErrorDetailsTable(OSBReporting.DbObjects.ErrorDetailsTable value) {
            this.errorDetailsTable = value;
        }

        /**
         * Gets the value of the gatherOSBReportsProcedure property.
         * 
         * @return
         *     possible object is
         *     {@link OSBReporting.DbObjects.GatherOSBReportsProcedure }
         *     
         */
        public OSBReporting.DbObjects.GatherOSBReportsProcedure getGatherOSBReportsProcedure() {
            return gatherOSBReportsProcedure;
        }

        /**
         * Sets the value of the gatherOSBReportsProcedure property.
         * 
         * @param value
         *     allowed object is
         *     {@link OSBReporting.DbObjects.GatherOSBReportsProcedure }
         *     
         */
        public void setGatherOSBReportsProcedure(OSBReporting.DbObjects.GatherOSBReportsProcedure value) {
            this.gatherOSBReportsProcedure = value;
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
         *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" default="XXXIW_MWP_ERROR_DETAILS" />
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class ErrorDetailsTable {

            @XmlAttribute(name = "name")
            protected String name;

            /**
             * Gets the value of the name property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getName() {
                if (name == null) {
                    return "XXXIW_MWP_ERROR_DETAILS";
                } else {
                    return name;
                }
            }

            /**
             * Sets the value of the name property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setName(String value) {
                this.name = value;
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
         *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" default="XXXIW_MWP_ERRORS" />
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class ErrorsTable {

            @XmlAttribute(name = "name")
            protected String name;

            /**
             * Gets the value of the name property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getName() {
                if (name == null) {
                    return "XXXIW_MWP_ERRORS";
                } else {
                    return name;
                }
            }

            /**
             * Sets the value of the name property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setName(String value) {
                this.name = value;
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
         *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" default="ERROR_HOSPITAL.gather_osb_data" />
         *       &lt;attribute name="scheduledInterval" type="{http://www.w3.org/2001/XMLSchema}int" default="60" />
         *       &lt;attribute name="forceProcedureExecution" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class GatherOSBReportsProcedure {

            @XmlAttribute(name = "name")
            protected String name;
            @XmlAttribute(name = "scheduledInterval")
            protected Integer scheduledInterval;
            @XmlAttribute(name = "forceProcedureExecution")
            protected Boolean forceProcedureExecution;

            /**
             * Gets the value of the name property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getName() {
                if (name == null) {
                    return "ERROR_HOSPITAL.gather_osb_data";
                } else {
                    return name;
                }
            }

            /**
             * Sets the value of the name property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setName(String value) {
                this.name = value;
            }

            /**
             * Gets the value of the scheduledInterval property.
             * 
             * @return
             *     possible object is
             *     {@link Integer }
             *     
             */
            public int getScheduledInterval() {
                if (scheduledInterval == null) {
                    return  60;
                } else {
                    return scheduledInterval;
                }
            }

            /**
             * Sets the value of the scheduledInterval property.
             * 
             * @param value
             *     allowed object is
             *     {@link Integer }
             *     
             */
            public void setScheduledInterval(Integer value) {
                this.scheduledInterval = value;
            }

            /**
             * Gets the value of the forceProcedureExecution property.
             * 
             * @return
             *     possible object is
             *     {@link Boolean }
             *     
             */
            public boolean isForceProcedureExecution() {
                if (forceProcedureExecution == null) {
                    return false;
                } else {
                    return forceProcedureExecution;
                }
            }

            /**
             * Sets the value of the forceProcedureExecution property.
             * 
             * @param value
             *     allowed object is
             *     {@link Boolean }
             *     
             */
            public void setForceProcedureExecution(Boolean value) {
                this.forceProcedureExecution = value;
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
         *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" default="REPORTING_EVENTS" />
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class ReportEventsTable {

            @XmlAttribute(name = "name")
            protected String name;

            /**
             * Gets the value of the name property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getName() {
                if (name == null) {
                    return "REPORTING_EVENTS";
                } else {
                    return name;
                }
            }

            /**
             * Sets the value of the name property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setName(String value) {
                this.name = value;
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
     *     &lt;extension base="{http://www.ibm.com/SOATF/Config/Master}AbstractMasterConfigEnvironmentInstance">
     *       &lt;sequence>
     *         &lt;element name="hostName" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="port" type="{http://www.w3.org/2001/XMLSchema}int"/>
     *         &lt;element name="userName" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="password" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;choice>
     *           &lt;element name="serviceId" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *           &lt;element name="serviceName" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;/choice>
     *       &lt;/sequence>
     *       &lt;attribute name="environment" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/extension>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "hostName",
        "port",
        "userName",
        "password",
        "serviceId",
        "serviceName"
    })
    public static class OsbReportingInstance
        extends AbstractMasterConfigEnvironmentInstance
    {

        @XmlElement(required = true)
        protected String hostName;
        @XmlElement(defaultValue = "1521")
        protected int port;
        @XmlElement(required = true, defaultValue = "NWK_XXXIW")
        protected String userName;
        @XmlElement(required = true)
        protected String password;
        protected String serviceId;
        protected String serviceName;
        @XmlAttribute(name = "environment", required = true)
        protected String environment;

        /**
         * Gets the value of the hostName property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getHostName() {
            return hostName;
        }

        /**
         * Sets the value of the hostName property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setHostName(String value) {
            this.hostName = value;
        }

        /**
         * Gets the value of the port property.
         * 
         */
        public int getPort() {
            return port;
        }

        /**
         * Sets the value of the port property.
         * 
         */
        public void setPort(int value) {
            this.port = value;
        }

        /**
         * Gets the value of the userName property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getUserName() {
            return userName;
        }

        /**
         * Sets the value of the userName property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setUserName(String value) {
            this.userName = value;
        }

        /**
         * Gets the value of the password property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getPassword() {
            return password;
        }

        /**
         * Sets the value of the password property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setPassword(String value) {
            this.password = value;
        }

        /**
         * Gets the value of the serviceId property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getServiceId() {
            return serviceId;
        }

        /**
         * Sets the value of the serviceId property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setServiceId(String value) {
            this.serviceId = value;
        }

        /**
         * Gets the value of the serviceName property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getServiceName() {
            return serviceName;
        }

        /**
         * Sets the value of the serviceName property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setServiceName(String value) {
            this.serviceName = value;
        }

        /**
         * Gets the value of the environment property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getEnvironment() {
            return environment;
        }

        /**
         * Sets the value of the environment property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setEnvironment(String value) {
            this.environment = value;
        }

    }

}
