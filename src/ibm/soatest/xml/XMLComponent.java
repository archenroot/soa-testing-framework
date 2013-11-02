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
package ibm.soatest.xml;

import ibm.soatest.CompOperResult;
import ibm.soatest.CompOperType;
import ibm.soatest.SOATFComponent;
import ibm.soatest.SOATFCompType;
import ibm.soatest.config.JMSConfiguration;
import ibm.soatest.UnsupportedComponentOperation;
import ibm.soatest.tool.FileSystem;
import java.io.File;
import java.util.Iterator;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author zANGETSu
 */
public class XMLComponent extends SOATFComponent {
    private static final Logger logger = LogManager.getLogger(XMLComponent.class.getName());
    
    public static Set<CompOperType> supportedOperations = CompOperType.xmlOperations;   
    
    private final JMSConfiguration jmsConfiguration;    
    
    public XMLComponent(JMSConfiguration jmsConfiguration, CompOperResult componentOperationResult, String identificator) {
        super(SOATFCompType.XML, componentOperationResult, identificator);
        this.jmsConfiguration = jmsConfiguration;
        
        constructComponent();
    }

    @Override
    protected void constructComponent() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void executeOperation(CompOperType componentOperation) {
        this.getComponentOperationResult().setCompOperType(componentOperation);
        if (supportedOperations.contains(componentOperation)) {
            try {
                throw new ibm.soatest.UnsupportedComponentOperation();
            } catch (ibm.soatest.UnsupportedComponentOperation ex) {
                logger.error("Component operation is not supported." + ex.getLocalizedMessage());
                this.getComponentOperationResult().setOverallResultSuccess(false);
                return; // this.getComponentOperationResult();
            }
        }
        
        switch (componentOperation){
            /*case XML_VALIDATE_FILE:
                try {
                    validateMessageFiles();
                } catch (Exception ex) {
                    logger.fatal(ex);
                }
                break;*/
            default:
                try {
                    throw new UnsupportedComponentOperation();
                } catch (UnsupportedComponentOperation ex) {
                    logger.fatal( ex);
                }                
                break;
        }
        logger.info("Operation " + componentOperation + " succesfully executed.");
        this.getComponentOperationResult().setOverallResultSuccess(true);
        this.getComponentOperationResult().setResultMessage("Opertaion succesfully executed.");
        
    }
    
    /*public Iterator getGeneratedFiles(String objectName) {
        String pattern = "*";
        if (objectName != null) pattern = objectName;
        String filemask = new StringBuilder(jmsConfiguration.getQueue().getName()).append(NAME_DELIMITER).append(pattern).append(MESSAGE_SUFFIX).toString();
        return FileUtils.iterateFiles(new File(FileSystem.JMS_MESSAGE_DIR), new WildcardFileFilter(filemask), TrueFileFilter.INSTANCE);
    }

    private void validateMessageFiles() {
        Iterator iterateFiles = getGeneratedFiles(null);
        while(iterateFiles.hasNext()) {
            ValidatorResult result = XMLValidator.validateXMLFile(iterateFiles.next().toString(), jmsConfiguration.getMessageSchema().getRelativeURI());
            if (!result.isValid()) {
                componentOperationResult.setOverallResultSuccess(false);
                componentOperationResult.setResultMessage("Message in queue " + jmsConfiguration.getQueue().getName() + " is not valid");
                logger.info("Message in queue " + jmsConfiguration.getQueue().getName() + " is not valid.");
                return;
            }
            logger.info("Message in queue " + jmsConfiguration.getQueue().getName() + " is valid.");
        }
    }*/
}
