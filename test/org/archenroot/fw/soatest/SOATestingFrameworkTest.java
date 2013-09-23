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
package org.archenroot.fw.soatest;

import java.io.File;
import org.archenroot.fw.soatest.database.DatabaseTestComponent;
import org.archenroot.fw.soatest.file.FileTestComponent;
import org.archenroot.fw.soatest.ftp.FTPTestComponent;
import org.archenroot.fw.soatest.jms.JMSTestComponent;
import org.archenroot.fw.soatest.osbservicemanager.OSBServiceManagerTestComponent;
import org.archenroot.fw.soatest.soap.SOAPTestComponent;
import org.archenroot.fw.soatest.xml.SOATestingFrameworkConfiguration;
import org.archenroot.fw.soatest.xml.XMLTestComponent;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author zANGETSu
 */
public class SOATestingFrameworkTest {
    
    public SOATestingFrameworkTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of readConfiguration method, of class SOATestingFramework.
     */
    @Test
    public void testReadConfiguration() {
        System.out.println("readConfiguration");
        SOATestingFramework instance = new SOATestingFramework();
        instance.readConfiguration();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setSOATFConfigFile method, of class SOATestingFramework.
     */
    @Test
    public void testSetSOATFConfigFile_String() {
        System.out.println("setSOATFConfigFile");
        String soaTFConfigFilePath = "";
        SOATestingFramework instance = new SOATestingFramework();
        instance.setSOATFConfigFile(soaTFConfigFilePath);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setSOATFConfigFile method, of class SOATestingFramework.
     */
    @Test
    public void testSetSOATFConfigFile_File() {
        System.out.println("setSOATFConfigFile");
        File soaTFConfigFilePath = null;
        SOATestingFramework instance = new SOATestingFramework();
        instance.setSOATFConfigFile(soaTFConfigFilePath);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of initSOATF method, of class SOATestingFramework.
     */
    @Test
    public void testInitSOATF() {
        System.out.println("initSOATF");
        SOATestingFramework instance = new SOATestingFramework();
        instance.initSOATF();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDatabaseTestComponent method, of class SOATestingFramework.
     */
    @Test
    public void testGetDatabaseTestComponent() {
        System.out.println("getDatabaseTestComponent");
        SOATestingFramework instance = new SOATestingFramework();
        DatabaseTestComponent expResult = null;
        DatabaseTestComponent result = instance.getDatabaseTestComponent();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getFileTestComponent method, of class SOATestingFramework.
     */
    @Test
    public void testGetFileTestComponent() {
        System.out.println("getFileTestComponent");
        SOATestingFramework instance = new SOATestingFramework();
        FileTestComponent expResult = null;
        FileTestComponent result = instance.getFileTestComponent();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getFTPTestComponent method, of class SOATestingFramework.
     */
    @Test
    public void testGetFTPTestComponent() {
        System.out.println("getFTPTestComponent");
        SOATestingFramework instance = new SOATestingFramework();
        FTPTestComponent expResult = null;
        FTPTestComponent result = instance.getFTPTestComponent();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getJMSTestComponent method, of class SOATestingFramework.
     */
    @Test
    public void testGetJMSTestComponent() {
        System.out.println("getJMSTestComponent");
        SOATestingFramework instance = new SOATestingFramework();
        JMSTestComponent expResult = null;
        JMSTestComponent result = instance.getJMSTestComponent();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getOSBServiceManagerTestComponent method, of class SOATestingFramework.
     */
    @Test
    public void testGetOSBServiceManagerTestComponent() {
        System.out.println("getOSBServiceManagerTestComponent");
        SOATestingFramework instance = new SOATestingFramework();
        OSBServiceManagerTestComponent expResult = null;
        OSBServiceManagerTestComponent result = instance.getOSBServiceManagerTestComponent();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSOAPTestComponent method, of class SOATestingFramework.
     */
    @Test
    public void testGetSOAPTestComponent() {
        System.out.println("getSOAPTestComponent");
        SOATestingFramework instance = new SOATestingFramework();
        SOAPTestComponent expResult = null;
        SOAPTestComponent result = instance.getSOAPTestComponent();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getXMLTestComponent method, of class SOATestingFramework.
     */
    @Test
    public void testGetXMLTestComponent() {
        System.out.println("getXMLTestComponent");
        SOATestingFramework instance = new SOATestingFramework();
        XMLTestComponent expResult = null;
        XMLTestComponent result = instance.getXMLTestComponent();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSoaTFConfigFile method, of class SOATestingFramework.
     */
    @Test
    public void testGetSoaTFConfigFile() {
        System.out.println("getSoaTFConfigFile");
        SOATestingFramework instance = new SOATestingFramework();
        File expResult = null;
        File result = instance.getSoaTFConfigFile();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSoaTFConfig method, of class SOATestingFramework.
     */
    @Test
    public void testGetSoaTFConfig() {
        System.out.println("getSoaTFConfig");
        SOATestingFramework instance = new SOATestingFramework();
        SOATestingFrameworkConfiguration expResult = null;
        SOATestingFrameworkConfiguration result = instance.getSoaTFConfig();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setSoaTFConfigFile method, of class SOATestingFramework.
     */
    @Test
    public void testSetSoaTFConfigFile() {
        System.out.println("setSoaTFConfigFile");
        File soaTFConfigFile = null;
        SOATestingFramework instance = new SOATestingFramework();
        instance.setSoaTFConfigFile(soaTFConfigFile);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setSoaTFConfig method, of class SOATestingFramework.
     */
    @Test
    public void testSetSoaTFConfig() {
        System.out.println("setSoaTFConfig");
        SOATestingFrameworkConfiguration soaTFConfig = null;
        SOATestingFramework instance = new SOATestingFramework();
        instance.setSoaTFConfig(soaTFConfig);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
