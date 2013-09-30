/*
 * Copyright (C) 2013 Ladislav Jech <archenroot@gmail.com>
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

import java.io.FileNotFoundException;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import static org.archenroot.fw.soatest.SoaTestingFrameworkComponentType.ComponentOperation.READ_NEW_MESSAGE_IN_QUEUE;
import static org.archenroot.fw.soatest.SoaTestingFrameworkComponentType.ComponentOperation.READ_ALL_MASSAGES_IN_QUEUE;
import static org.archenroot.fw.soatest.SoaTestingFrameworkComponentType.JMS;
import org.archenroot.fw.soatest.jms.JmsComponent;
import org.archenroot.fw.soatest.tool.ValidateTransferedValues;
import org.archenroot.fw.soatest.xml.XMLValidator;
import org.gibello.zql.ParseException;
import org.xml.sax.SAXException;


/**
 *
 * @author Ladislav Jech <archenroot@gmail.com>
 */
public class TestJmsComponent {

    public static void testJmsComponent() throws SAXException, ParserConfigurationException, IOException, FileNotFoundException, ParseException {
        //JmsComponent jmsComponent = (JmsComponent) SoaTestingFrameworkComponentFactory.buildSoaTestingFrameworkComponent(JMS);
        
        //jmsComponent.executeOperation(READ_ALL_MASSAGES_IN_QUEUE);
       // jmsComponent.executeOperation(READ_NEW_MESSAGE_IN_QUEUE);
        
        //XMLValidator xmlValidator = new XMLValidator();
        //boolean validateXMLFiles = xmlValidator.validateXMLFiles("test/jms", "test/jms/JMSMessage.xsd");
        
        
        //ValidateTransferedValues vtv = new ValidateTransferedValues();
        //vtv.validateValuesFromFile("test/db/insert.sql", "test/jms/message_1.xml");
        
        
    }
}
