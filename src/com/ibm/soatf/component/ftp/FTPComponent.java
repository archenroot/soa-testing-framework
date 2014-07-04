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

import com.ibm.soatf.component.AbstractSoaTFComponent;
import com.ibm.soatf.component.SOATFCompType;
import com.ibm.soatf.component.file.FileComponent;
import com.ibm.soatf.config.iface.IfaceExecBlock;
import com.ibm.soatf.config.iface.ftp.FTPConfig;
import com.ibm.soatf.config.iface.ftp.Security;
import com.ibm.soatf.config.master.FTPServers.FtpServer.Directories;
import com.ibm.soatf.config.master.FTPServers.FtpServer.FtpServerInstance;
import com.ibm.soatf.config.master.Operation;
import com.ibm.soatf.flow.FlowExecutor;
import com.ibm.soatf.flow.FrameworkExecutionException;
import com.ibm.soatf.flow.OperationResult;
import com.ibm.soatf.gui.ProgressMonitor;
import com.ibm.soatf.tool.FileSystem;
import com.ibm.soatf.tool.Utils;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import it.sauronsoftware.ftp4j.FTPAbortedException;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferException;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPFile;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;
import it.sauronsoftware.ftp4j.FTPListParseException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.Vector;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Ladislav Jech <archenroot@gmail.com>
 */
public class FTPComponent extends AbstractSoaTFComponent {

    private static final Logger logger = LogManager.getLogger(FTPComponent.class);

    private final FtpServerInstance ftpMasterConfig;
    //private final FTPConfig ftpConfiguration;
    private final FTPConfig.File ftpFile;
    private final Directories directories;

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
            FTPConfig.File ftpFile,
            Directories directories,
            File workingDir) {
        super(SOATFCompType.FTP);
        this.ftpMasterConfig = ftpMasterConfig;
        //this.ftpConfiguration = ftpInterfaceConfig;
        this.ftpFile = ftpFile;
        this.actualFileUsed = "";
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
    protected final void constructComponent() {

        this.hostName = this.ftpMasterConfig.getHostName();
        this.port = this.ftpMasterConfig.getPort();
        this.user = this.ftpMasterConfig.getUser();
        this.password = this.ftpMasterConfig.getPassword();
        this.security = Security.valueOf(this.ftpMasterConfig.getSecurity());
        this.stageDirectory = directories.getStageDirectory();
        this.errorDirectory = directories.getErrorDirectory();
        this.archiveDirectory = directories.getArchiveDirectory();
        /*
         * Fast workaround related to multiple data sources
         */
        //this.fileContent = this.ftpConfiguration.getFileContent();
        //this.fileName = this.ftpConfiguration.getFileName();
        this.fileContent = this.ftpFile.getFileContent();
        //if (this.fileContent != null) fileContent = fileContent.trim();
        this.fileName = this.ftpFile.getFileName();

    }

    @Override
    public void executeOperation(Operation operation) throws FrameworkExecutionException {
        //cor.setOperation(operation); //nastavuje konstruktor abstractoperation
        /*if (!FTP_OPERATIONS.contains(operation)) {
         final String msg = "Unsupported operation: " + operation.getName().value() + ". Valid operations are: " + FTP_OPERATIONS;
         logger.error(msg);
         cor.addMsg(msg);
         cor.setOverallResultSuccess(false);
         } else {*/
        actualFileUsed = generateFile(this.workingDir, this.fileName, this.fileContent).getName();//(this.workingDir, this.fileName, this.fileContent).getName();
        switch (operation.getName()) {
            case FTP_DOWNLOAD_FILE:
                ftpDownloadFile();
                break;
            case FTP_UPLOAD_FILE:
                //generateFile();
                ftpUploadFile();
                break;
            case FTP_CHECK_DELIVERED_FOLDER_FOR_FILE:
                checkFolderForFile(this.archiveDirectory);
                break;
            case FTP_CHECK_ERROR_FOLDER_FOR_FILE:
                checkFolderForFile(this.errorDirectory);
                break;
            case FTP_SEARCH_FOR_FILE:
                checkFolderForFile(this.stageDirectory);
                break;
            default:
                logger.info("Operation execution not yet implemented: " + operation.getName().value());
                cor.addMsg("Operation: " + operation.getName().value() + " is valid, but not yet implemented");
        }
    }

    private void ftpDownloadFile() throws FtpComponentException {
        FTPClient client = null;
        try {
            switch (security) {
                case NONE:
                    /*
                     * This implementation is just for testing purposes, there are currently 2 libraries for FTP, SFTP, FTPS and FTPES.
                     * Once I will wrap them into one client object, the usage will change.
                     */
                    client = new FTPClient();
                    client.connect(this.hostName, this.port);
                    client.login(this.user, this.password);
                    client.changeDirectory(this.stageDirectory);
                    File localFile = new File(workingDir, actualFileUsed);
                    client.download(fileName, localFile);
                    logger.info("File with name '" + fileName + "' was downloaded from: " + this.stageDirectory);
                    cor.addMsg("File with name '" + fileName + "' was downloaded from: " + this.stageDirectory);
                    cor.markSuccessful();
                    break;
                default:
                    final String msg = "Security type not supported: " + security;
                    logger.info(msg);
                    cor.addMsg(msg);
                    throw new FtpComponentException(msg);
            }
        } catch (IllegalStateException | FTPException | IOException | FTPIllegalReplyException | FTPDataTransferException | FTPAbortedException ftpex) {
            final String msg = "Error while downloading the file.";
            cor.addMsg(msg);
            throw new FtpComponentException(msg, ftpex);
        } finally {
            if (client != null) {
                try {
                    client.disconnect(true);
                } catch (IllegalStateException | IOException | FTPIllegalReplyException | FTPException ex) {;}
            }
        }
    }

    private void checkFolderForFile(String folderName) throws FtpComponentException {
        FTPClient client = null;
        if (actualFileUsed == null) {
            final String msg = "File name was not set.";
            cor.addMsg(msg);
            throw new FtpComponentException(msg);
        }
        switch (security) {
            case NONE:
                try {
                    ProgressMonitor.init(5, "Connecting to FTP server...");
                    client = new FTPClient();
                    client.connect(this.hostName, this.port);
                    ProgressMonitor.increment("Logging in...");
                    client.login(this.user, this.password);
                    ProgressMonitor.increment("Changing directory...");
                    client.changeDirectory(folderName);
                    ProgressMonitor.increment("Listing files...");
                    FTPFile[] fileArray = client.list();
                    boolean found = false;
                    for (FTPFile file : fileArray) {
                        String name = file.getName();
                        if (name != null && (name.equals(actualFileUsed) || name.endsWith("__" + actualFileUsed))) {
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        logger.info("File with name '" + actualFileUsed + "' was found in directory: " + folderName);
                        cor.addMsg("File with name '" + actualFileUsed + "' was found in directory: " + folderName);
                        cor.markSuccessful();
                    } else {
                        final String message = "File with name '" + actualFileUsed + "' was not found in directory: " + folderName;
                        logger.info(message);
                        cor.addMsg(message);
                        String additionalMessage=null;
                        client.changeDirectory(this.stageDirectory);
                        fileArray = client.list();
                        for (int i = 0; i < fileArray.length; i++) {
                            FTPFile file = fileArray[i];
                            String name = file.getName();
                            if (name != null && (name.equals(actualFileUsed) || name.endsWith("__" + actualFileUsed))) {
                                additionalMessage = "The file was found in the original directory, it seems not to be polled at all.\n";
                                break;
                            }
                        }
                        if (additionalMessage == null) {
                            client.changeDirectory(this.errorDirectory);
                            fileArray = client.list();
                            for (int i = 0; i < fileArray.length; i++) {
                                FTPFile file = fileArray[i];
                                String name = file.getName();
                                if (name != null && (name.equals(actualFileUsed) || name.endsWith("__" + actualFileUsed))) {
                                    additionalMessage = "The file was found in the error directory. There was an error while processing the file.";
                                    break;
                                }
                            }
                        }
                        if (additionalMessage == null) {
                            client.changeDirectory(this.archiveDirectory);
                            fileArray = client.list();
                            for (int i = 0; i < fileArray.length; i++) {
                                FTPFile file = fileArray[i];
                                String name = file.getName();
                                if (name != null && (name.equals(actualFileUsed) || name.endsWith("__" + actualFileUsed))) {
                                    additionalMessage = "The file was found in the archive directory. File was processed without any errors.";
                                    break;
                                }
                            }
                        }
                        cor.addMsg(additionalMessage);                        
                        throw new FtpComponentException(message);
                    }
                    break;
                } catch (FTPException | FTPIllegalReplyException | IllegalStateException | IOException | FTPDataTransferException | FTPAbortedException | FTPListParseException ftpex) {
                    final String msg = "FTP error while trying to search in the remote folder";
                    cor.addMsg(msg);
                    throw new FtpComponentException(msg, ftpex);
                } finally {
                    ProgressMonitor.increment("Disconnecting...");
                    if (client != null) {
                        try {
                            client.disconnect(true);
                        } catch (IllegalStateException | IOException | FTPIllegalReplyException | FTPException ex) {;}
                    }
                }
            case SSH:
                Session session = null;
                ChannelSftp channelSftp = null;

                try {
                    JSch jsch = new JSch();
                    String sshMsg = "Creating SSH session...";
                    ProgressMonitor.init(6, sshMsg);
                    logger.info(sshMsg);
                    cor.addMsg(sshMsg, null);
                    session = jsch.getSession(this.user, this.hostName, this.port);
                    session.setPassword(this.password);
                    session.setConfig(FileComponent.CONFIG);
                    sshMsg = "SSH session created.";
                    logger.info(sshMsg);
                    cor.addMsg(sshMsg, null);

                    sshMsg = "Connecting to SSH session...";
                    ProgressMonitor.increment(sshMsg);
                    logger.info(sshMsg);
                    cor.addMsg(sshMsg, null);
                    session.connect();
                    sshMsg = "Connected to SSH session.";
                    logger.info(sshMsg);
                    cor.addMsg(sshMsg, null);

                    sshMsg = "Opening SSH channel...";
                    ProgressMonitor.increment(sshMsg);
                    logger.info(sshMsg);
                    cor.addMsg(sshMsg, null);
                    channelSftp = (ChannelSftp) session.openChannel("sftp");
                    sshMsg = "SSH channel opened.";
                    logger.info(sshMsg);
                    cor.addMsg(sshMsg, null);

                    sshMsg = "Connecting to SSH channel...";
                    ProgressMonitor.increment(sshMsg);
                    logger.info(sshMsg);
                    cor.addMsg(sshMsg, null);
                    channelSftp.connect();
                    sshMsg = "Connected to SSH channel.";
                    logger.info(sshMsg);
                    cor.addMsg(sshMsg);

                    Vector ls;

                    sshMsg = "Listing '" + folderName + "' content...";
                    ProgressMonitor.increment("Listing directory content...");
                    logger.info(sshMsg);
                    cor.addMsg(sshMsg);
                    ls = channelSftp.ls(folderName);

                    if(!Utils.isEmpty(ls)) {
                        for (Object object : ls) {
                            ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) object;
                            final String name = entry.getFilename();
                            if (name != null && (name.equals(actualFileUsed) || name.endsWith("__" + actualFileUsed))) {
                            //if (entry.getFilename().matches(fileName)) {
                                String msg = "File '" + actualFileUsed + "' found in '" + folderName + "'";
                                logger.info(msg);
                                cor.addMsg(msg);
                                cor.markSuccessful();
                                return;
                            }
                        }
                    }
                    sshMsg = "File '" + actualFileUsed + "' not found in '" + folderName + "'";
                    cor.addMsg(sshMsg);

                    String additionalMessage=null;
                    ls = channelSftp.ls(this.stageDirectory);                        
                    if(!Utils.isEmpty(ls)) {
                        for (Object object : ls) {
                            ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) object;
                            final String name = entry.getFilename();
                            if (name != null && (name.equals(actualFileUsed) || name.endsWith("__" + actualFileUsed))) {
                                additionalMessage =  "The file was found in the original directory, it seems not to be polled at all.\n";
                                break;
                            }
                        }
                    }
                    if (additionalMessage == null) {
                        ls = channelSftp.ls(this.errorDirectory);     
                        if(!Utils.isEmpty(ls)) {
                            for (Object object : ls) {
                                ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) object;
                                final String name = entry.getFilename();
                                if (name != null && (name.equals(actualFileUsed) || name.endsWith("__" + actualFileUsed))) {
                                    additionalMessage =  "The file was found in the error directory. There was an error while processing the file.";
                                    break;
                                }
                            }
                        }
                    }
                    if (additionalMessage == null) {
                        ls = channelSftp.ls(this.archiveDirectory);     
                        if(!Utils.isEmpty(ls)) {
                            for (Object object : ls) {
                                ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) object;
                                final String name = entry.getFilename();
                                if (name != null && (name.equals(actualFileUsed) || name.endsWith("__" + actualFileUsed))) {
                                    additionalMessage = "The file was found in the archive directory. File was processed without any errors.";
                                    break;
                                }
                            }
                        }                            
                    }
                    cor.addMsg(additionalMessage);                         
                    throw new FtpComponentException(sshMsg);
                } catch (JSchException | SftpException ftpex) {
                    final String msg = "FTP error while trying to search in the remote folder";
                    cor.addMsg(msg);
                    throw new FtpComponentException(msg, ftpex);
                } finally {
                    ProgressMonitor.increment("Disconnecting...");
                    FileComponent.disconnect(session, channelSftp);
                }                                                                                                                                                                                                                                                                                                                                
            default:
                final String msg = "Security type not supported: " + security;
                logger.info(msg);
                cor.addMsg(msg);
                throw new FtpComponentException(msg);
        }
    }

    private void ftpUploadFile() throws FtpComponentException {
        FTPClient client = null;
        
        File localFile = new File(workingDir, actualFileUsed);            
        switch (this.security) {
            case NONE:
                try {
                    /*
                     * This implementation is just for testing purposes, there are currently 2 libraries for FTP, SFTP, FTPS and FTPES.
                     * Once I will wrap them into one client object, the usage will change.
                     */
                    ProgressMonitor.init(5, "Connecting to FTP server...");
                    client = new FTPClient();
                    client.connect(this.hostName, this.port);
                    ProgressMonitor.increment("Logging in...");
                    client.login(this.user, this.password);
                    ProgressMonitor.increment("Changing directory...");
                    client.changeDirectory(this.stageDirectory);
                    ProgressMonitor.increment("Uploading file...");
                    client.upload(localFile);
                    logger.info("File with name '" + actualFileUsed + "' was uploaded: " + this.stageDirectory);
                    cor.addMsg("File with name '" + actualFileUsed + "' was uploaded to: " + this.stageDirectory);
                    cor.markSuccessful();
                } catch (IllegalStateException | IOException | FTPIllegalReplyException | FTPException | FTPDataTransferException | FTPAbortedException ftpex) {
                    final String msg = "FTP error while uploading the file.";
                    cor.addMsg(msg);
                    throw new FtpComponentException(msg, ftpex);
                } finally {
                    ProgressMonitor.increment("Disconnecting...");
                    if (client != null) {
                        try {
                            client.disconnect(true);
                        } catch (IllegalStateException | IOException | FTPIllegalReplyException | FTPException ex) {;}
                    }
                }
                break;
            case SSH:
                String sshMsg;
                Session session = null;
                ChannelSftp channelSftp = null;
                try {
                    JSch jsch = new JSch();
                    sshMsg = "Creating SSH session...";
                    ProgressMonitor.init(8, sshMsg);
                    logger.info(sshMsg);
                    cor.addMsg(sshMsg, null);
                    session = jsch.getSession(this.user, this.hostName, this.port);
                    session.setPassword(this.password);
                    session.setConfig(FileComponent.CONFIG);
                    sshMsg = "SSH session created.";
                    logger.info(sshMsg);
                    cor.addMsg(sshMsg, null);

                    sshMsg = "Connecting to SSH session...";
                    ProgressMonitor.increment(sshMsg);
                    logger.info(sshMsg);
                    cor.addMsg(sshMsg, null);
                    session.connect();
                    sshMsg = "Connected to SSH session.";
                    logger.info(sshMsg);
                    cor.addMsg(sshMsg, null);

                    sshMsg = "Opening SSH channel...";
                    ProgressMonitor.increment(sshMsg);
                    logger.info(sshMsg);
                    cor.addMsg(sshMsg, null);
                    channelSftp = (ChannelSftp) session.openChannel("sftp");
                    sshMsg = "SSH channel opened.";
                    logger.info(sshMsg);
                    cor.addMsg(sshMsg, null);

                    sshMsg = "Connecting to SSH channel...";
                    ProgressMonitor.increment(sshMsg);
                    logger.info(sshMsg);
                    cor.addMsg(sshMsg);
                    channelSftp.connect();
                    sshMsg = "Connected to SSH channel.";
                    logger.info(sshMsg);
                    cor.addMsg(sshMsg);

                    sshMsg = "Changing to '" + this.stageDirectory + "' directory...";
                    ProgressMonitor.increment("Changing directory...");
                    logger.info(sshMsg);
                    cor.addMsg(sshMsg);
                    channelSftp.cd(this.stageDirectory);
                    sshMsg = "Directory changed.";
                    logger.info(sshMsg);
                    cor.addMsg(sshMsg);

                    final FileInputStream fis;
                    String destFile = (this.stageDirectory + "/" + actualFileUsed).replaceAll("//", "/"); //ensure that variable doesn't contain double slash
                    sshMsg = "Opening file '%s' for transfer. Destination file is: " + destFile;
                    ProgressMonitor.increment("Opening file for transfer...");
                    cor.addMsg(sshMsg, "<a href='file://"+localFile.getAbsolutePath()+"'>"+localFile.getAbsolutePath()+"</a>", FileSystem.getRelativePath(localFile));
                    fis = new FileInputStream(localFile);

                    sshMsg = "Transferring file...";
                    ProgressMonitor.increment(sshMsg);
                    logger.info(sshMsg);
                    cor.addMsg(sshMsg, null);
                    channelSftp.put(fis, actualFileUsed);
                    sshMsg = "File transfer finished successfully.";
                    logger.info(sshMsg);
                    cor.addMsg(sshMsg);
                    cor.markSuccessful();
                } catch (JSchException | SftpException | FileNotFoundException ftpex) {
                    final String msg = "FTP error while uploading the file.";
                    cor.addMsg(msg);
                    throw new FtpComponentException(msg, ftpex);
                } finally {
                    ProgressMonitor.increment("Disconnecting...");
                    FileComponent.disconnect(session, channelSftp);
                }
                break;                                                                                                                                                                
            default:
                logger.info("Security type not supported: " + security);
                cor.addMsg("Security type " + security + " is valid, but not implemented yet.");
        }
    }

    private File generateFile(File path, String fileName, String fileContent) throws FtpComponentException {
        File localFile = new File(path, Utils.insertTimestampToFilename(fileName, FlowExecutor.getActualRunDate()));
        
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
            if (writer != null) {            
                try {
                    writer.close();
                } catch (IOException ex) {;}
            }                
        }
        return localFile;
    }

    public File getFile(File workingDirectory, String fileName, String fileContent) throws FtpComponentException {
        String pattern = "*_" + fileName;
        Iterator<File> it = FileUtils.iterateFiles(workingDirectory, new WildcardFileFilter(pattern), TrueFileFilter.INSTANCE);
        int count = 0;
        File f = null;
        while (it.hasNext()) {
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
