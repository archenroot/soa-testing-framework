/*
+------------------------------------------------------------------------------+  
+--------/                                                          \----------+
|      //  ÛÛÛÛ       ÛÛÛÛ          Û         ÛÛÛÛÛÛÛÛÛÛÛ ÛÛÛÛÛÛÛÛÛ ||         |
|     //  ÛÛÛ  Û     ÛÛÛ  Û         Û            ÛÛÛÛÛ    ÛÛÛÛÛ     //         |
|    ||  ÛÛÛÛÛ      ÛÛÛÛ   Û       ÛÛÛ           ÛÛÛÛÛ    ÛÛÛÛÛ     \\         |
|    ||  ÛÛÛÛÛ     ÛÛÛÛÛ    Û     ÛÛÛÛÛ          ÛÛÛÛÛ    ÛÛÛÛÛÛÛÛÛ  ||        |
|    ||  ÛÛÛÛÛÛ    ÛÛÛÛÛ    Û     ÛÛÛÛÛ          ÛÛÛÛÛ    ÛÛÛÛÛ    __//        |
|    \\   ÛÛÛÛÛÛ   ÛÛÛÛÛ    Û    ÛÛÛÛÛÛÛ         ÛÛÛÛÛ    ÛÛÛÛÛ   //           |
|     \\   ÛÛÛÛÛÛ  ÛÛÛÛÛ    Û    Û ÛÛÛÛÛ         ÛÛÛÛÛ    ÛÛÛÛÛ   ||           |
|      \\   ÛÛÛÛÛ  ÛÛÛÛÛ    Û   Û  ÛÛÛÛÛÛ        ÛÛÛÛÛ    ÛÛÛÛÛ   ||           |
|      //   ÛÛÛÛÛ   ÛÛÛÛ   Û   Û    ÛÛÛÛÛÛ       ÛÛÛÛÛ    ÛÛÛÛÛ   ||           |
|     // Û   ÛÛÛ     ÛÛÛ  Û    ÛÛÛÛÛÛÛÛÛÛÛ       ÛÛÛÛÛ    ÛÛÛÛÛ   ||           |
|    //   ÛÛÛÛÛ       ÛÛÛÛ    Û      ÛÛÛÛÛÛ      ÛÛÛÛÛ    ÛÛÛÛÛ   ||           |
+------------------------------------------------------------------------------+   
+------------------------------------------------------------------------------+
| Component:   | OSB                                                           |
+------------------------------------------------------------------------------+
| Script name: | OSB_DISABLE_PROXY_NAME
+------------------------------------------------------------------------------+
| Description: | Groovy SIMPLE interface script is part of underlaying SOA     + 
|--------------+ Testing Framework written entirelly in Java. This script      +
| should provide atomic operation from integration testing perspective.        +
+------------------------------------------------------------------------------+
| Focus:       | This artifact creates Oracle Service Bus component and issues +
+--------------+ command trough JMX interface to disable specific proxy service+
| from operation within OSB. Typically we want to deliver message to some type +
| of end-point, but don't let the message to be consumed by the pooling adap-  +
| ter (JMS, DATABASE, FTP, FILE type of end-points currently suported.         +
+------------------------------------------------------------------------------+
| Usage:       | All the necessary stuff about related to OSB component is     +
+--------------+ defined in XML configuration file within "osbconf" namespace. +
| Most important atribute to define is attribute "identificator" within        +
| <osbconf:service> element. This need to be changed here according to confi-  +
| guration, so the framework knows which service to disable. No other changes  +
| are required.                                                                +
+------------------------------------------------------------------------------+
*/
// Package name
package com.ibm.fm.soatest.groovy.osb

//------------------------------------------------------------------------------
/* !DO NOT TOUCH HERE! - Import of SOA TF runtime artifacts and initialization
 of related objects
*/

// Generic component factory builder method
import static ibm.soatest.SOATFCompFactory.buildSOATFComponent
// Type of OSB component itself
import ibm.soatest.osb.OSBComponent
// Static enumeration of component to be created from the component factory
import static ibm.soatest.SOATFCompType.OSB
// Static enumeration of command which is going to be issued within created
// component
import static ibm.soatest.CompOperType.OSB_DISABLE_PROXY_SERVICE
// Component generic result object
import ibm.soatest.CompOperResult
									
// Creating concrete OSB component from abstract type using factory
OSBComponent osbComponent = (OBSComponent) buildSOATFComponent(OSB)

CompOperResult compOperResult
//------------------------------------------------------------------------------

//------------------------------------------------------------------------------
/* !!! IMPORTANT - NEED TO CHANGE ACCORDING TO XML CONFIGURATION */
// Change this value according to identificator from XML configuration file
def serviceIdentificator = "Service Identificator"
//------------------------------------------------------------------------------

//------------------------------------------------------------------------------
// Issue the command to the component with service identificator

assert true == true

log.info("adfsafadsf")
