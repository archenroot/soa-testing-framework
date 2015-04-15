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
package com.ibm.soatf.xml;

import com.ibm.soatf.FrameworkException;
import com.ibm.soatf.flow.FrameworkExecutionException;
import com.ibm.soatf.component.AbstractSoaTFComponent;
import com.ibm.soatf.component.CompOperType;
import com.ibm.soatf.component.SOATFCompType;
import com.ibm.soatf.config.iface.jms.JMSConfig;
import com.ibm.soatf.config.master.Operation;
import com.ibm.soatf.flow.OperationResult;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author zANGETSu
 */
@Deprecated
public class XMLComponent extends AbstractSoaTFComponent {
    private static final Logger logger = LogManager.getLogger(XMLComponent.class.getName());
    
    public static Set<CompOperType> supportedOperations = CompOperType.XML_OPERATIONS;   
    
    private final JMSConfig jmsConfiguration; 
    
    private final OperationResult cor;
    
    public XMLComponent(JMSConfig jmsConfiguration) {
        super(SOATFCompType.XML);
        this.jmsConfiguration = jmsConfiguration;
        cor = OperationResult.getInstance();
        constructComponent();
    }

    @Override
    protected final void constructComponent() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void executeOperation(Operation operation) throws FrameworkException {
        cor.setOperation(operation);
//        if (supportedOperations.contains(operation)) {
//            try {
//                throw new com.ibm.soatf.UnsupportedComponentOperationException();
//            } catch (    com.ibm.soatf.UnsupportedComponentOperationException ex) {
//                logger.error("Component operation is not supported." + ex.getLocalizedMessage());
//                return; // cor;
//            }
//        }
        
        switch (operation.getName()){
            /*case XML_VALIDATE_FILE:
                try {
                    validateMessageFiles();
                } catch (Exception ex) {
                    logger.fatal(ex);
                }
                break;*/
            default:
                String msg = "Invalid operation name: " + operation.getName();
                logger.error(msg);
                cor.addMsg(msg);
                throw new FrameworkExecutionException(msg);
        }
//        logger.info("Operation " + operation.getName() + " succesfully executed.");
//        cor.markSuccessful();
//        cor.addMsg("Opertaion succesfully executed.");
        
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
                componentOperationResult.addMsg("Message in queue " + jmsConfiguration.getQueue().getName() + " is not valid");
                logger.info("Message in queue " + jmsConfiguration.getQueue().getName() + " is not valid.");
                return;
            }
            logger.info("Message in queue " + jmsConfiguration.getQueue().getName() + " is valid.");
        }
    }*/

    @Override
    protected void destructComponent() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
