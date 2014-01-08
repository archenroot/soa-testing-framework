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

import com.ibm.soatf.component.SOATFCompType;
import com.ibm.soatf.component.AbstractSOATFComponent;
import com.ibm.soatf.config.iface.IfaceExecBlock;
import com.ibm.soatf.config.iface.ftp.FTPConfig;
import com.ibm.soatf.config.iface.ftp.Security;
import com.ibm.soatf.config.master.FTPServers.FtpServer.Directories;
import com.ibm.soatf.config.master.FTPServers.FtpServer.FtpServerInstance;
import com.ibm.soatf.config.master.Operation;
import com.ibm.soatf.flow.OperationResult;
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
import java.util.Iterator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author zANGETSu
 */
public class FTPComponent extends AbstractSOATFComponent {

    private static final Logger logger = LogManager.getLogger(FTPComponent.class);

    private final FtpServerInstance ftpMasterConfig;
    private final FTPConfig ftpConfiguration;
    private final Directories directories;
    private File workingDir;
    
    private String hostName;
    private int port;
    private String user;
    private String password;
    private Security security;
    private String stageDirectory;
    private String errorDirectory;
    private String archiveDirectory;
    private String fileContent;
    private String fileName; 
    private String actualFileUsed;
    
    private final OperationResult cor;

    public FTPComponent(
            IfaceExecBlock ifaceExecBlock,
            FtpServerInstance ftpMasterConfig, 
            FTPConfig ftpInterfaceConfig,
            Directories directories,
            File workingDir) {
        super(SOATFCompType.FTP);
        this.ftpMasterConfig = ftpMasterConfig;
        this.ftpConfiguration = ftpInterfaceConfig;
        this.actualFileUsed="";
        this.directories = directories;
        this.workingDir = workingDir;
        cor = OperationResult.getInstance();
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
        this.stageDirectory = directories.getStageDirectory();
        this.errorDirectory = directories.getErrorDirectory();
        this.archiveDirectory = directories.getArchiveDirectory();
        this.fileContent = this.ftpConfiguration.getFileContent();
        this.fileName = this.ftpConfiguration.getFileName();

    }

    @Override
    public void executeOperation(Operation operation) {
        cor.setOperation(operation);
        /*if (!FTP_OPERATIONS.contains(operation)) {
            final String msg = "Unsupported operation: " + operation.getName().value() + ". Valid operations are: " + FTP_OPERATIONS;
            logger.error(msg);
            cor.addMsg(msg);
            cor.setOverallResultSuccess(false);
        } else {*/
            actualFileUsed = getFile(this.workingDir, this.fileName, this.fileContent).getName();
            switch (operation.getName()) {
            case FTP_DOWNLOAD_FILE:
                try {
                    ftpDownloadFile();
                } catch (    FTPException | FTPIllegalReplyException | IllegalStateException | IOException | FTPDataTransferException | FTPAbortedException ex) {
                    logger.fatal(ex);
                    cor.addMsg("Error while downloading a file with name '" + fileName + "'");
                }
                break;
            case FTP_UPLOAD_FILE:
                try {
                    //generateFile();
                    ftpUploadFile();
                } catch (    IllegalStateException | IOException | FTPIllegalReplyException | FTPException | FTPDataTransferException | FTPAbortedException ex) {
                    logger.fatal(ex);
                    cor.addMsg("Error while uploading a file with name '" + fileName + "'");
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
                cor.addMsg("Operation: " + operation.getName().value() + " is valid, but not yet implemented");
            }
        //}
    }

    private void ftpDownloadFile() throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException, FileNotFoundException, FTPDataTransferException, FTPAbortedException {
        switch (security) {
            case NONE:
                /*
                 * This implementation is just for testing purposes, there are currently 2 libraries for FTP, SFTP, FTPS and FTPES.
                 * Once I will wrap them into one client object, the usage will change.
                 */
                FTPClient client = new FTPClient();
                client.connect(this.hostName, this.port);
                client.login(this.user, this.password);
                client.changeDirectory(this.stageDirectory);
                File localFile = new File(workingDir, fileName);
                client.download(fileName, localFile);
                client.disconnect(true);
                logger.info("File with name '" + fileName + "' was downloaded from: " + this.stageDirectory);
                cor.addMsg("File with name '" + fileName + "' was downloaded from: " + this.stageDirectory);
                cor.markSuccessful();                 
                break;
            default:
                logger.info("Security type not supported: " + security);
                cor.addMsg("Security type " + security + " is valid, but not implemented yet.");
        }
    }
    
    private void checkFolderForFile(String folderName) throws FTPException, FTPIllegalReplyException, IllegalStateException, IOException, FTPDataTransferException, FTPAbortedException, FTPListParseException {
        if (fileName == null) {
            logger.error("File name was not set.");
            cor.addMsg("File name was not set.");
            return;
        }
        switch (security) {
            case NONE:
                FTPClient client = new FTPClient();
                client.connect(this.hostName, this.port);
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
                    cor.addMsg("File with name '" + fileName + "' was found in directory: " + folderName);
                    cor.markSuccessful();
                } else {
                    logger.info("File with name '" + fileName + "' was not found in directory: " + folderName);
                    cor.addMsg("File with name '" + fileName + "' was not found in directory: " + folderName);
                }
                client.disconnect(true);
                break;
            default:
                logger.info("Security type not supported: " + security);
                cor.addMsg("Security type " + security + " is valid, but not implemented yet.");
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
                client.connect(this.hostName, this.port);
                client.login(this.user, this.password);
                client.changeDirectory(this.stageDirectory);
                File localFile = new File(workingDir, actualFileUsed);
                client.upload(localFile);
                client.disconnect(true);
                logger.info("File with name '" + fileName + "' was uploaded: " + this.stageDirectory);
                cor.addMsg("File with name '" + fileName + "' was uploaded to: " + this.stageDirectory);
                cor.markSuccessful();                
                break;
            default:
                logger.info("Security type not supported: " + security);
                cor.addMsg("Security type " + security + " is valid, but not implemented yet.");
        }
    }

    private static File generateFile(File path, String fileName, String fileContent) {
        String actualPrefix = new SimpleDateFormat("yyyyMMdd_hhmmss_").format(new Date());
        File localFile = new File(path, actualPrefix+fileName);
        if (localFile.exists()) {
            return localFile;
        }
        Writer writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                  new FileOutputStream(localFile), "utf-8"));
            writer.write(fileContent);
            writer.flush();
        } catch (IOException ex) {
          //TODO
        } finally {
           try {
               if (writer != null) {
                writer.close();
               }
           } catch (Exception ex) {}
        }
        return localFile;
    }
    
    public static File getFile(File workingDirectory, String fileName, String fileContent) {
        String pattern = "*_" + fileName;
        Iterator<File> it = FileUtils.iterateFiles(workingDirectory, new WildcardFileFilter(pattern), TrueFileFilter.INSTANCE);
        int count = 0;
        File f = null;
        while(it.hasNext()) {
            ++count;
            f = it.next();
        }
        if (count == 0) {
            f = generateFile(workingDirectory, fileName, fileContent);
        }
        if (count > 1) {
            //TODO
        }
        return f;
    }

    @Override
    protected void destructComponent() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
