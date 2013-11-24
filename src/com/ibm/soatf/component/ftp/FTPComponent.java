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
package com.ibm.soatf.component.ftp;

import com.ibm.soatf.component.ComponentResult;
import com.ibm.soatf.component.CompOperType;
import static com.ibm.soatf.component.CompOperType.FTP_OPERATIONS;
import com.ibm.soatf.flow.FlowPatternCompositeKey;
import com.ibm.soatf.FrameworkConfiguration;

import com.ibm.soatf.component.SOATFComponent;
import com.ibm.soatf.component.SOATFCompType;
import com.ibm.soatf.config.iface.IfaceExecBlock;
import com.ibm.soatf.config.iface.ftp.FTPConfig;
import com.ibm.soatf.config.iface.ftp.Security;
import com.ibm.soatf.config.master.FTPServers.FtpServer.FtpServerInstance;
import com.ibm.soatf.config.master.Operation;
import it.sauronsoftware.ftp4j.FTPAbortedException;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferException;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPFile;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;
import it.sauronsoftware.ftp4j.FTPListParseException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author zANGETSu
 */
public class FTPComponent extends SOATFComponent {

    private static final Logger logger = LogManager.getLogger(FTPComponent.class);

    private FtpServerInstance ftpMasterConfig;
    private FTPConfig ftpConfiguration;
    private String workingDirectoryPath;
    private FlowPatternCompositeKey fpck;    
    private static final FrameworkConfiguration FCFG = FrameworkConfiguration.getInstance();
    
    private String hostName;
    private String port;
    private String user;
    private String password;
    private Security security;
    private String stageDirectory;
    private String errorDirectory;
    private String archiveDirectory;
    private String fileContent;
    private String fileName; 
    private String actualPrefix;

    public FTPComponent(
            IfaceExecBlock ifaceExecBlock,
            FtpServerInstance ftpMasterConfig, 
            FTPConfig ftpInterfaceConfig,
            ComponentResult componentOperationResult,
            FlowPatternCompositeKey ifaceFlowPatternCompositeKey) {
        super(SOATFCompType.FTP, componentOperationResult);
        this.ftpMasterConfig = ftpMasterConfig;
        this.ftpConfiguration = ftpInterfaceConfig;
        this.fpck = ifaceFlowPatternCompositeKey;
        this.actualPrefix="";
        
        constructComponent();
    }
    
    /*public FTPComponent(ComponentResult componentOperationResult) {
        super(SOATFCompType.FTP, componentOperationResult);
        this.hostName = "zarik";
        this.port = "21";
        this.user = "anonymous";
        this.password = "aaa";
        this.security = Security.NONE;
        this.stageDirectory = "salmon/out";
        this.errorDirectory = "salmon/error";
        this.archiveDirectory = "salmon/archive";
        //this.filePattern = this.ftpConfiguration.getFilePattern();
        this.fileName = "test.data";
        workingDirectoryPath = "C:\\test\\";
    }*/

    @Override
    protected void constructComponent() {

        this.hostName = this.ftpMasterConfig.getHostName();
        this.port = this.ftpMasterConfig.getPort();
        this.user = this.ftpMasterConfig.getUser();
        this.password = this.ftpMasterConfig.getPassword();
        this.security = Security.valueOf(this.ftpMasterConfig.getSecurity());
        this.stageDirectory = this.ftpMasterConfig.getStageDirectory();
        this.errorDirectory = this.ftpMasterConfig.getErrorDirectory();
        this.archiveDirectory = this.ftpMasterConfig.getArchiveDirectory();
        this.fileContent = this.ftpConfiguration.getFileContent();
        this.fileName = this.ftpConfiguration.getFileName();
        
        /*
        * Need to be refactorized!!!
        */
        workingDirectoryPath = FCFG.SOA_TEST_HOME + "\\" +
                fpck.getIfaceName() + "_" + FCFG.getValidFileSystemObjectName(fpck.getIfaceDesc()) + "\\" +
                FCFG.FLOW_PATTERN_DIR_NAME_PREFIX + FCFG.getValidFileSystemObjectName(fpck.getFlowPatternId()) + "\\" +
                FCFG.getValidFileSystemObjectName(fpck.getTestName()) + "\\" +
                FCFG.getValidFileSystemObjectName(fpck.getTestScenarioId()) + "\\ftp\\";

    }

    @Override
    public void executeOperation(Operation operation) {
        this.compOperResult.setOperation(operation);
        if (!FTP_OPERATIONS.contains(operation)) {
            final String msg = "Unsupported operation: " + operation.getName().value() + ". Valid operations are: " + FTP_OPERATIONS;
            logger.error(msg);
            this.compOperResult.setResultMessage(msg);
            this.compOperResult.setOverallResultSuccess(false);
        } else {
            switch (operation.getName()) {
            case FTP_DOWNLOAD_FILE:
                try {
                    ftpDownloadFile();
                } catch (    FTPException | FTPIllegalReplyException | IllegalStateException | IOException | FTPDataTransferException | FTPAbortedException ex) {
                    logger.fatal(ex);
                    compOperResult.setResultMessage("Error while downloading a file with name '" + fileName + "'");
                    compOperResult.setOverallResultSuccess(false);
                }
                break;
            case FTP_UPLOAD_FILE:
                try {
                    generateFile();
                    ftpUploadFile();
                } catch (    IllegalStateException | IOException | FTPIllegalReplyException | FTPException | FTPDataTransferException | FTPAbortedException ex) {
                    logger.fatal(ex);
                    compOperResult.setResultMessage("Error while uploading a file with name '" + fileName + "'");
                    compOperResult.setOverallResultSuccess(false);
                }
                break;
            case FTP_CHECK_DELIVERED_FOLDER_FOR_FILE:
                try {
                    checkFolderForFile(this.archiveDirectory);
                } catch (    IllegalStateException | IOException | FTPIllegalReplyException | FTPException | FTPDataTransferException | FTPAbortedException | FTPListParseException ex) {
                    logger.fatal(ex);
                }
                break;
            case FTP_CHECK_ERROR_FOLDER_FOR_FILE:
                try {
                    checkFolderForFile(this.errorDirectory);
                } catch (    IllegalStateException | IOException | FTPIllegalReplyException | FTPException | FTPDataTransferException | FTPAbortedException | FTPListParseException ex) {
                    logger.fatal(ex);
                }
                break;    
            case FTP_SEARCH_FOR_FILE:
                try {
                    checkFolderForFile(this.stageDirectory);
                } catch (    IllegalStateException | IOException | FTPIllegalReplyException | FTPException | FTPDataTransferException | FTPAbortedException | FTPListParseException ex) {
                    logger.fatal(ex);
                }
                break;                                  
            default:
                logger.info("Operation execution not yet implemented: " + operation.getName().value());
                this.compOperResult.setResultMessage("Operation: " + operation.getName().value() + " is valid, but not yet implemented");
                this.compOperResult.setOverallResultSuccess(false);
            }
        }
    }

    private void ftpDownloadFile() throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException, FileNotFoundException, FTPDataTransferException, FTPAbortedException {
        switch (security) {
            case NONE:
                /*
                 * This implementation is just for testing purposes, there are currently 2 libraries for FTP, SFTP, FTPS and FTPES.
                 * Once I will wrap them into one client object, the usage will change.
                 */
                FTPClient client = new FTPClient();
                client.connect(this.hostName, Integer.parseInt(this.port));
                client.login(this.user, this.password);
                client.changeDirectory(this.stageDirectory);
                File localFile = new File(workingDirectoryPath + fileName);
                client.download(fileName, localFile);
                client.disconnect(true);
                logger.info("File with name '" + fileName + "' was downloaded from: " + this.stageDirectory);
                compOperResult.setResultMessage("File with name '" + fileName + "' was downloaded from: " + this.stageDirectory);
                compOperResult.setOverallResultSuccess(true);                 
                break;
            default:
                logger.info("Security type not supported: " + security);
                compOperResult.setResultMessage("Security type " + security + " is valid, but not implemented yet.");
                compOperResult.setOverallResultSuccess(false);
        }
    }
    
    private void checkFolderForFile(String folderName) throws FTPException, FTPIllegalReplyException, IllegalStateException, IOException, FTPDataTransferException, FTPAbortedException, FTPListParseException {
        if (fileName == null) {
            logger.error("File name was not set.");
            compOperResult.setResultMessage("File name was not set.");
            compOperResult.setOverallResultSuccess(false);
            return;
        }
        switch (security) {
            case NONE:
                FTPClient client = new FTPClient();
                client.connect(this.hostName, Integer.parseInt(this.port));
                client.login(this.user, this.password);
                client.changeDirectory(folderName);
                FTPFile[] fileArray = client.list();
                boolean found = false;
                for (int i = 0; i < fileArray.length; i++) {
                    FTPFile file = fileArray[i];
                    String name = file.getName();
                    if(name != null && (name.equals(fileName) || name.endsWith("__"+fileName))) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    logger.info("File with name '" + fileName + "' was found in directory: " + folderName);
                    compOperResult.setResultMessage("File with name '" + fileName + "' was found in directory: " + folderName);
                    compOperResult.setOverallResultSuccess(true);
                } else {
                    logger.info("File with name '" + fileName + "' was not found in directory: " + folderName);
                    compOperResult.setResultMessage("File with name '" + fileName + "' was not found in directory: " + folderName);
                    compOperResult.setOverallResultSuccess(false);                    
                }
                client.disconnect(true);
                break;
            default:
                logger.info("Security type not supported: " + security);
                compOperResult.setResultMessage("Security type " + security + " is valid, but not implemented yet.");
                compOperResult.setOverallResultSuccess(false);
        }
    }    

    private void ftpUploadFile() throws IllegalStateException, IOException, FileNotFoundException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException {
        switch (this.security) {
            case NONE:
                /*
                 * This implementation is just for testing purposes, there are currently 2 libraries for FTP, SFTP, FTPS and FTPES.
                 * Once I will wrap them into one client object, the usage will change.
                 */
                FTPClient client = new FTPClient();
                client.connect(this.hostName, Integer.parseInt(this.port));
                client.login(this.user, this.password);
                client.changeDirectory(this.stageDirectory);
                File localFile = new File(workingDirectoryPath, actualPrefix+fileName);
                client.upload(localFile);
                client.disconnect(true);
                logger.info("File with name '" + fileName + "' was uploaded: " + this.stageDirectory);
                compOperResult.setResultMessage("File with name '" + fileName + "' was uploaded to: " + this.stageDirectory);
                compOperResult.setOverallResultSuccess(true);                
                break;
            default:
                logger.info("Security type not supported: " + security);
                compOperResult.setResultMessage("Security type " + security + " is valid, but not implemented yet.");
                compOperResult.setOverallResultSuccess(false);
        }
    }

    private void generateFile() {
        this.actualPrefix = new SimpleDateFormat("yyyyMMdd_hhmmss_").format(new Date());
        File localFile = new File(workingDirectoryPath, actualPrefix+fileName);
        if (localFile.exists()) {
            return;
        }
        
        Writer writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                  new FileOutputStream(localFile), "utf-8"));
            writer.write(this.fileContent);
        } catch (IOException ex) {
          ;
        } finally {
           try {writer.close();} catch (Exception ex) {}
        }
    }
}
