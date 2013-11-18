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
package com.ibm.soatf.util;

import com.ibm.soatf.ComponentResult;
import com.ibm.soatf.CompOperType;
import static com.ibm.soatf.CompOperType.FTP_OPERATIONS;
import com.ibm.soatf.FlowPatternCompositeKey;
import com.ibm.soatf.SOATFComponent;
import com.ibm.soatf.SOATFCompType;
import com.ibm.soatf.config._interface.ftp.FTPConfiguration;
import com.ibm.soatf.config._interface.ftp.Security;
import com.ibm.soatf.config._interface.util.UtilConfiguration;
import com.ibm.soatf.config.master.FTPServers.FtpServer;
import com.ibm.soatf.config.master.FTPServers.FtpServer.FtpServerInstance;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author zANGETSu
 */
public class UtilityComponent extends SOATFComponent {

    private static final Logger logger = LogManager.getLogger(UtilityComponent.class);

    private UtilConfiguration utilInterfaceConfig;
    private long delay;

    public UtilityComponent(
            UtilConfiguration utilInterfaceConfig,
            ComponentResult componentOperationResult,
            FlowPatternCompositeKey ifaceFlowPatternCompositeKey) {
        super(SOATFCompType.UTIL, componentOperationResult);
        this.utilInterfaceConfig = utilInterfaceConfig;
        constructComponent();
    }

    @Override
    protected void constructComponent() {
        delay = utilInterfaceConfig.getDelay();
    }

    @Override
    public void executeOperation(CompOperType componentOperation) {
        this.componentOperationResult.setCompOperType(componentOperation);
        if (!FTP_OPERATIONS.contains(componentOperation)) {
            final String msg = "Unsupported operation: " + componentOperation + ". Valid operations are: " + FTP_OPERATIONS;
            logger.error(msg);
            this.componentOperationResult.setResultMessage(msg);
            this.componentOperationResult.setOverallResultSuccess(false);
        } else {
            switch (componentOperation) {
                case DB_CHECK_NUMBER_OF_ROWS:
                    break;
                default:
                    logger.info("Operation execution not yet implemented: " + componentOperation);
                    this.componentOperationResult.setResultMessage("Operation: " + componentOperation + " is valid, but not yet implemented");
                    this.componentOperationResult.setOverallResultSuccess(false);
            }
        }
    }
}
