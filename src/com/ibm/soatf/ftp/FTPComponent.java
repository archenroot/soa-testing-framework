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
package com.ibm.soatf.ftp;

import com.ibm.soatf.ComponentResult;
import com.ibm.soatf.CompOperType;
import static com.ibm.soatf.CompOperType.FTP_OPERATIONS;
import com.ibm.soatf.FlowPatternCompositeKey;
import com.ibm.soatf.SOATFComponent;
import com.ibm.soatf.SOATFCompType;
import com.ibm.soatf.config._interface.ftp.FTPConfiguration;
import com.ibm.soatf.config._interface.ftp.Security;
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
public class FTPComponent extends SOATFComponent {

    private static final Logger logger = LogManager.getLogger(FTPComponent.class);

    private FTPConfiguration ftpConfiguration;
    private String hostName;
    private String port;
    private String user;
    private String password;
    private Security security;
    private String serverDirectory;
    private String errorDirectory;
    private String filePattern;
    private String file;

    public FTPComponent(
            FtpServerInstance ftpMasterConfig, 
            FTPConfiguration ftpInterfaceConfig,
            ComponentResult componentOperationResult,
            FlowPatternCompositeKey ifaceFlowPatternCompositeKey) {
        super(SOATFCompType.FTP, componentOperationResult);
        this.ftpConfiguration = ftpConfiguration;
        constructComponent();
    }

    @Override
    protected void constructComponent() {
/*
        this.hostName = this.ftpConfiguration.getHostName();
        this.port = this.ftpConfiguration.getPort();
        this.user = this.ftpConfiguration.getUser();
        this.password = this.ftpConfiguration.getPassword();
        this.security = this.ftpConfiguration.getSecurity();
        this.serverDirectory = this.ftpConfiguration.getServerDirectory();
        this.errorDirectory = this.ftpConfiguration.getErrorDirectory();
        this.filePattern = this.ftpConfiguration.getFilePattern();
        this.file = this.ftpConfiguration.getFileName();
*/
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
                case FTP_DOWNLOAD_FILE:
            try {
                ftpDownloadFile();
            } catch (FTPException ex) {
                logger.fatal(ex);
            } catch (FTPIllegalReplyException ex) {
                logger.fatal(ex);
            } catch (IllegalStateException ex) {
                logger.fatal(ex);
            } catch (IOException ex) {
                logger.fatal(ex);
            }
                    break;
                case FTP_SEND_FILE:
            try {
                ftpUploadFile();
            } catch (IllegalStateException ex) {
                logger.fatal(ex);
            } catch (IOException ex) {
                logger.fatal(ex);
            } catch (FTPIllegalReplyException ex) {
                logger.fatal(ex);
            } catch (FTPException ex) {
                logger.fatal(ex);
            }
                    break;
                default:
                    logger.info("Operation execution not yet implemented: " + componentOperation);
                    this.componentOperationResult.setResultMessage("Operation: " + componentOperation + " is valid, but not yet implemented");
                    this.componentOperationResult.setOverallResultSuccess(false);
            }
        }
    }

    private void ftpDownloadFile() throws FTPException, FTPIllegalReplyException, IllegalStateException, IOException {
        switch (security) {
            case NONE:
                /*
                 * This implementation is just for testing purposes, there are currently 2 libraries for FTP, SFTP, FTPS and FTPES.
                 * Once I will wrap them into one client object, the usage will change.
                 */
                FTPClient client = new FTPClient();
                client.connect(this.hostName, Integer.getInteger(this.port));
                client.login(this.user, this.password);
                
                client.disconnect(true);
                break;
            case SSH:
                //

                break;
            case SSL:
                //

                break;
            default:
                logger.info("Security type not supported: " + security);
                componentOperationResult.setResultMessage("Security type " + security + " is valid, but not implemented yet.");
                componentOperationResult.setOverallResultSuccess(false);
        }
    }

    private void ftpUploadFile() throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        switch (this.security) {
            case NONE:
                /*
                 * This implementation is just for testing purposes, there are currently 2 libraries for FTP, SFTP, FTPS and FTPES.
                 * Once I will wrap them into one client object, the usage will change.
                 */
                FTPClient client = new FTPClient();
                client.connect(this.hostName, Integer.getInteger(this.port));
                client.login(this.user, this.password);
                

                client.disconnect(true);
                break;
            case SSH:
                //

                break;
            case SSL:
                //

                break;
            default:
                logger.info("Security type not supported: " + security);
                componentOperationResult.setResultMessage("Security type " + security + " is valid, but not implemented yet.");
                componentOperationResult.setOverallResultSuccess(false);
        }
    }
}
