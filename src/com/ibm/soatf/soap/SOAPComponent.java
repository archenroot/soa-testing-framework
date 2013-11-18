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
package com.ibm.soatf.soap;

import com.ibm.soatf.ComponentResult;
import com.ibm.soatf.CompOperType;
import static com.ibm.soatf.CompOperType.SOAP_OPERATIONS;
import com.ibm.soatf.FlowPatternCompositeKey;
import com.ibm.soatf.SOATFComponent;
import com.ibm.soatf.SOATFCompType;
import com.ibm.soatf.config._interface.jms.JMSConfiguration;
import com.ibm.soatf.config._interface.soap.SOAPConfiguration;
import com.ibm.soatf.config.master.OracleFusionMiddleware;
import com.ibm.soatf.config.master.OracleFusionMiddleware.OracleFusionMiddlewareInstance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author zANGETSu
 */
public class SOAPComponent extends SOATFComponent {

    private static final Logger logger = LogManager.getLogger(SOAPComponent.class);
    private OracleFusionMiddlewareInstance soapMasterConfig;
    private SOAPConfiguration soapInterfaceConfig;
    
    private String identificator;
    private String serviceName;
    private String operationName;
    private String endPointURI;

    public SOAPComponent(
            OracleFusionMiddlewareInstance soapMasterConfig,
            SOAPConfiguration soapInterfaceConfig,
            ComponentResult componentOperationResult,
            FlowPatternCompositeKey ifaceFlowPatternCompositeKey) {
        super(SOATFCompType.SOAP, componentOperationResult);
        this.soapMasterConfig = soapMasterConfig;
        this.soapInterfaceConfig = soapInterfaceConfig;
        constructComponent();
    }

    @Override
    protected void constructComponent() {
        /*
        this.identificator = this.soapConfiguration.getIdentificator();
        this.serviceName = this.soapConfiguration.getServiceName();
        this.operationName = this.soapConfiguration.getOperationName();
        this.endPointURI = this.soapConfiguration.getEndPointUri();
                */
    }

    @Override
    protected void executeOperation(CompOperType componentOperationType) {
        if (!SOAP_OPERATIONS.contains(componentOperationType)) {
            final String msg = "Unsupported operation: " + componentOperationType + ". Valid operations are: " + SOAP_OPERATIONS;
            logger.error(msg);
            componentOperationResult.setResultMessage(msg);
            componentOperationResult.setOverallResultSuccess(false);
        } else {
            switch (componentOperationType) {
                case SOAP_GENERATE_DYNAMICALLY_SOAP_REQUEST_TO_FILE: generateDynamicallySOAPRequest();
                                                                     break;
                case SOAP_INVOKE_SERVICE_WITH_PROVIDED_ENVELOPE:     invokeServiceWithProvidedSOAPRequest();
                                                                     break;
                default:
                    logger.info("Operation execution not yet implemented: " + componentOperationType);
                    componentOperationResult.setResultMessage("Operation: " + componentOperationType + " is valid, but not yet implemented");
                    componentOperationResult.setOverallResultSuccess(false);
            }
        }
    }

    private void generateDynamicallySOAPRequest() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void invokeServiceWithProvidedSOAPRequest() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
