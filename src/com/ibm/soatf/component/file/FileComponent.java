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
package com.ibm.soatf.component.file;

import com.ibm.soatf.FrameworkException;
import com.ibm.soatf.component.AbstractSoaTFComponent;
import com.ibm.soatf.component.SOATFCompType;
import com.ibm.soatf.config.ConfigurationManager;
import com.ibm.soatf.config.MasterConfiguration;
import com.ibm.soatf.config.iface.file.File;
import com.ibm.soatf.config.master.Operation;
import com.ibm.soatf.config.master.OracleFusionMiddleware;
import com.ibm.soatf.config.master.OracleFusionMiddleware.OracleFusionMiddlewareInstance.Clusters.Cluster.ManagedServer;
import com.ibm.soatf.flow.FlowExecutor;
import com.ibm.soatf.flow.FrameworkExecutionException;
import com.ibm.soatf.flow.OperationResult;
import com.ibm.soatf.gui.ProgressMonitor;
import com.ibm.soatf.tool.FileSystem;
import com.ibm.soatf.tool.Utils;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.Vector;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Component that deals with manipulation of files over SSH protocol
 * @author kroky
 */
public class FileComponent extends AbstractSoaTFComponent {

    private static final Logger logger = LogManager.getLogger(FileComponent.class);
    private final OracleFusionMiddleware.OracleFusionMiddlewareInstance ofmInstance;
    private final File fileObjectFromXml;
    private final OperationResult cor;
    private String fileName;
    private String remoteDir;
    private String fileContent;
    private String sshUser;
    private String sshPassword;
    private int port;
    private String hostName;
    
    public static final Properties CONFIG = new java.util.Properties();
    static {
        CONFIG.put("userauth.password", "com.jcraft.jsch.UserAuthPassword");
        CONFIG.put("StrictHostKeyChecking", "no");
    }
    private String errorDir;
    private String archiveDir;

    /**
     * Constructor. Sets the component type to FILE and calls the
     * constructComponent method.
     * @param ofmInstance
     * @param fileObjectFromXml
     * @param workingDir
     */
    public FileComponent(OracleFusionMiddleware.OracleFusionMiddlewareInstance ofmInstance, File fileObjectFromXml, java.io.File workingDir) {
        super(SOATFCompType.FILE);
        this.ofmInstance = ofmInstance;
        this.fileObjectFromXml = fileObjectFromXml;
        this.workingDir = workingDir;
        cor = OperationResult.getInstance();
        constructComponent();
    }

    /**
     * Method that takes care of properly constructing the component and
     * initialize its variables
     */
    @Override
    protected final void constructComponent() {
        fileName = fileObjectFromXml.getFileName().replaceAll("//", "/");
        remoteDir = fileObjectFromXml.getDirectory().replaceAll("//", "/");
        errorDir = fileObjectFromXml.getErrorArchiveDirectory();
        archiveDir = fileObjectFromXml.getArchiveDirectory();
        fileContent = fileObjectFromXml.getFileContent();
        sshUser = ofmInstance.getSsh().getSshUser();
        sshPassword = ofmInstance.getSsh().getSshPassword();
        port = ofmInstance.getSsh().getPort();
    }

    /**
     * Executes the operation.
     * @param operation operation to execute
     * @throws FrameworkException when the operation execution fails
     */
    @Override
    public void executeOperation(Operation operation) throws FrameworkException {
        //cor.setOperation(operation); //nastavuje konstruktor abstractoperation
        MasterConfiguration masterConfig = ConfigurationManager.getInstance().getMasterConfig();
        ManagedServer managedServer = masterConfig.getFirstManagedServerInCluster(masterConfig.getOsbCluster(ofmInstance));
        hostName = managedServer.getHostName();
        java.io.File actualFileUsed = createLocalFile();//(this.workingDir, this.fileName, this.fileContent).getName();
        switch (operation.getName()) {
            case FILE_PUT:
                putFile(actualFileUsed, remoteDir);
                break;
            case FILE_GET:
                break;
            case FILE_EXISTS_IN_SOURCE_DIR:
                checkFileInDir(actualFileUsed.getName(), remoteDir);
                break;
            case FILE_EXISTS_IN_ERROR_DIR:
                checkFileInDir(actualFileUsed.getName(), errorDir);
                break;
            case FILE_EXISTS_IN_ARCHIVE_DIR:
                checkFileInDir(actualFileUsed.getName(), archiveDir);
                break;
            case FILE_NOT_EXISTS_IN_SOURCE_DIR:
                break;
            case FILE_NOT_EXISTS_IN_ERROR_DIR:
                break;
            case FILE_NOT_EXISTS_IN_ARCHIVE_DIR:
                break;
            default:
                logger.info("Operation execution not yet implemented: " + operation.getName().value());
                cor.addMsg("Operation: " + operation.getName().value() + " is valid, but not yet implemented");
        }
    }

    /**
     * Creates file on your local filesystem with name configured in the config.xml and sufixes it with current date and time
     * in following format: yyyyMMdd_hhmmss_
     * @return returns the reference to the created file
     * @throws FrameworkExecutionException when <code>IOException</code> occurs
     */
    private java.io.File createLocalFile() throws FrameworkExecutionException {
        java.io.File localFile = new java.io.File(workingDir, Utils.insertTimestampToFilename(fileName, FlowExecutor.getActualRunDate()));
        if (localFile.exists()) {
            String msg = "Local file %s already exists, returning it instead of creating a new one.";
            logger.info(String.format(msg, localFile.getAbsolutePath()));
            cor.addMsg(msg, "<a href='file://"+localFile.getAbsolutePath()+"'>"+localFile.getAbsolutePath()+"</a>", FileSystem.getRelativePath(localFile));
            return localFile;
        }
        try {
            FileUtils.writeStringToFile(localFile, fileContent, "UTF-8");
            String msg = "Created local file: %s";
            logger.info(String.format(msg, localFile.getAbsolutePath()));
            cor.addMsg(msg, "<a href='file://"+localFile.getAbsolutePath()+"'>"+localFile.getAbsolutePath()+"</a>", FileSystem.getRelativePath(localFile));
        } catch (IOException ex) {
            String msg = String.format("Failed to create local file. Reason: %s", ex.getMessage());
            cor.addMsg(msg);
            throw new FrameworkExecutionException(msg, ex);
        }
        return localFile;
    }

    /**
     * Returns the first file that matches the *_&lt;fileName&gt; pattern.
     * If no such file exists, it is created by calling the <code>createLocalFile()</code> method
     * @return the existing or newly created file
     * @throws FrameworkExecutionException when <code>IOException</code> occurs
     */
    private java.io.File getLocalFile() throws FrameworkExecutionException {
        String pattern = "*_" + fileName;
        java.io.File[] files = FileUtils.convertFileCollectionToFileArray(FileUtils.listFiles(workingDir, new WildcardFileFilter(pattern), TrueFileFilter.INSTANCE));
        if (files.length == 0) {
            return createLocalFile();
        }
        if (files.length > 1) {
            //TODO
        }
        return files[0];
    }

    /**
     * Transfers the <code>file</code> to the specified remote directory via SSH
     * @param file file to transfer
     * @param where the directory to transfer to
     * @throws FrameworkExecutionException when error occurs while connecting via SSH or transfer fails
     */
    private void putFile(java.io.File file, String where) throws FrameworkExecutionException {
        Session session = null;
        ChannelSftp channelSftp = null;
        
        try {
            JSch jsch = new JSch();
            try {
                String msg = "Creating SSH session...";
                ProgressMonitor.init(8, msg);
                logger.info(msg);
                cor.addMsg(msg, null);
                session = jsch.getSession(sshUser, hostName, port);
                session.setPassword(sshPassword);
                session.setConfig(CONFIG);
                msg = "SSH session created.";
                logger.info(msg);
                cor.addMsg(msg, null);
            } catch (JSchException ex) {
                String msg = String.format("Failed to create SSH session. Reason: %s", ex.getMessage());
                cor.addMsg(msg);
                throw new FrameworkExecutionException(msg, ex);
            }
            
            try {
                String msg = "Connecting to SSH session...";
                ProgressMonitor.increment(msg);
                logger.info(msg);
                cor.addMsg(msg, null);
                session.connect();
                msg = "Connected to SSH session.";
                logger.info(msg);
                cor.addMsg(msg, null);
            } catch (JSchException ex) {
                String msg = String.format("Failed to connect to the SSH session. Reason: %s", ex.getMessage());
                cor.addMsg(msg);
                throw new FrameworkExecutionException(msg, ex);
            }
            
            try {
                String msg = "Opening SSH channel...";
                ProgressMonitor.increment(msg);
                logger.info(msg);
                cor.addMsg(msg, null);
                channelSftp = (ChannelSftp) session.openChannel("sftp");
                msg = "SSH channel opened.";
                logger.info(msg);
                cor.addMsg(msg, null);
            } catch (JSchException ex) {
                String msg = String.format("Failed to open the SFTP channel. Reason: %s", ex.getMessage());
                cor.addMsg(msg);
                throw new FrameworkExecutionException(msg, ex);
            }
            
            try {
                String msg = "Connecting to SSH channel...";
                ProgressMonitor.increment(msg);
                logger.info(msg);
                cor.addMsg(msg);
                channelSftp.connect();
                msg = "Connected to SSH channel.";
                logger.info(msg);
                cor.addMsg(msg);
            } catch (JSchException ex) {
                String msg = String.format("Failed to connect to the SFTP channel. Reason: %s", ex.getMessage());
                cor.addMsg(msg);
                throw new FrameworkExecutionException(msg, ex);
            }
            
            try {
                String msg = "Changing to '" + where + "' directory...";
                ProgressMonitor.increment("Changing directory...");
                logger.info(msg);
                cor.addMsg(msg);
                channelSftp.cd(where);
                msg = "Directory changed.";
                logger.info(msg);
                cor.addMsg(msg);
            } catch (SftpException ex) {
                String msg = String.format("Failed to execute 'cd '" + where + "' command. Reason: %s", ex.getMessage());
                cor.addMsg(msg);
                throw new FrameworkExecutionException(msg, ex);
            }
            
            final FileInputStream fis;
            try {
                String destFile = (where + "/" + fileName).replaceAll("//", "/"); //ensure that variable doesn't contain double slash
                final String msg = "Opening file '%s' for transfer. Destination file is: " + destFile;
                ProgressMonitor.increment("Opening file for transfer...");
                cor.addMsg(msg, "<a href='file://"+file.getAbsolutePath()+"'>"+file.getAbsolutePath()+"</a>", FileSystem.getRelativePath(file));
                fis = new FileInputStream(file);
            } catch (FileNotFoundException ex) {
                String msg = String.format("Could not open source file. Reason: %s", ex.getMessage());
                cor.addMsg(msg);
                throw new FrameworkExecutionException(msg, ex);
            }
            
            try {
                String msg = "Transferring file...";
                ProgressMonitor.increment(msg);
                logger.info(msg);
                cor.addMsg(msg, null);
                channelSftp.put(fis, fileName);
                msg = "File transfer finished successfully.";
                logger.info(msg);
                cor.addMsg(msg);
                cor.markSuccessful();
            } catch (SftpException ex) {
                String msg = String.format("Could not transfer source file. Reason: %s", ex.getMessage());
                cor.addMsg(msg);
                throw new FrameworkExecutionException(msg, ex);
            }
        } finally {
            ProgressMonitor.increment("Disconnecting...");
            disconnect(session, channelSftp);
        }
    }
    
    /**
     * Releases the resources allocated. If either of the objects is null, it is silently ignored and nothing is done
     * @param session session to release
     * @param channel channel to release
     */
    public static void disconnect(Session session, Channel channel) {
        if (channel != null) {
            try {
                String msg = "Disconnecting from SSH channel...";
                logger.debug(msg);
                channel.disconnect();
                msg = "Disconnected from SSH channel.";
                logger.debug(msg);
            } catch (Throwable e) {
                logger.warn("Could not disconnect from SSH channel", e.getMessage());
            }
        }
        if (session != null) {
            try {
                String msg = "Disconnecting from SSH session...";
                logger.trace(msg);
                session.disconnect();
                msg = "Disconnected from SSH session.";
                logger.trace(msg);
            } catch (Throwable e) {
                logger.warn("Could not disconnect from SSH session", e.getMessage());
            }
        }
    }

    @Override
    protected void destructComponent() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Checks if the file with name <code>fileName</code> exists in the directory specified by <code>dir</code>.
     * It is done by matching the filename against each file residing in the <code>dir</code>
     * @param fileName name of the file
     * @param dir directory to check
     * @throws FrameworkExecutionException when error occurs while connecting via SSH or 'ls' command fails
     */
    private void checkFileInDir(String fileName, String dir) throws FrameworkExecutionException {
        Session session = null;
        ChannelSftp channelSftp = null;
        
        try {
            JSch jsch = new JSch();
            try {
                String msg = "Creating SSH session...";
                ProgressMonitor.init(6, msg);
                logger.info(msg);
                cor.addMsg(msg, null);
                session = jsch.getSession(sshUser, hostName, port);
                session.setPassword(sshPassword);
                session.setConfig(CONFIG);
                msg = "SSH session created.";
                logger.info(msg);
                cor.addMsg(msg, null);
            } catch (JSchException ex) {
                String msg = String.format("Failed to create SSH session. Reason: %s", ex.getMessage());
                cor.addMsg(msg);
                throw new FrameworkExecutionException(msg, ex);
            }
            
            try {
                String msg = "Connecting to SSH session...";
                ProgressMonitor.increment(msg);
                logger.info(msg);
                cor.addMsg(msg, null);
                session.connect();
                msg = "Connected to SSH session.";
                logger.info(msg);
                cor.addMsg(msg, null);
            } catch (JSchException ex) {
                String msg = String.format("Failed to connect to the SSH session. Reason: %s", ex.getMessage());
                cor.addMsg(msg);
                throw new FrameworkExecutionException(msg, ex);
            }
            
            try {
                String msg = "Opening SSH channel...";
                ProgressMonitor.increment(msg);
                logger.info(msg);
                cor.addMsg(msg, null);
                channelSftp = (ChannelSftp) session.openChannel("sftp");
                msg = "SSH channel opened.";
                logger.info(msg);
                cor.addMsg(msg, null);
            } catch (JSchException ex) {
                String msg = String.format("Failed to open the SFTP channel. Reason: %s", ex.getMessage());
                cor.addMsg(msg);
                throw new FrameworkExecutionException(msg, ex);
            }
            
            try {
                String msg = "Connecting to SSH channel...";
                ProgressMonitor.increment(msg);
                logger.info(msg);
                cor.addMsg(msg, null);
                channelSftp.connect();
                msg = "Connected to SSH channel.";
                logger.info(msg);
                cor.addMsg(msg);
            } catch (JSchException ex) {
                String msg = String.format("Failed to connect to the SFTP channel. Reason: %s", ex.getMessage());
                cor.addMsg(msg);
                throw new FrameworkExecutionException(msg, ex);
            }
            
            Vector ls;
            try {
                String msg = "Listing '" + dir + "' content...";
                ProgressMonitor.increment("Listing directory content...");
                logger.info(msg);
                cor.addMsg(msg);
                ls = channelSftp.ls(dir);
            } catch (SftpException ex) {
                String msg = String.format("Failed to execute 'ls '" + dir + "' command. Reason: %s", ex.getMessage());
                cor.addMsg(msg);
                throw new FrameworkExecutionException(msg, ex);
            }
            if(!Utils.isEmpty(ls)) {
                for (Object object : ls) {
                    ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) object;
                    if (entry.getFilename().matches(fileName)) {
                        String msg = "File '" + fileName + "' found in '" + dir + "'";
                        logger.info(msg);
                        cor.addMsg(msg);
                        cor.markSuccessful();
                        return;
                    }
                }
            }
            String msg = "File '" + fileName + "' not found in '" + dir + "'";
            cor.addMsg(msg);
            throw new FrameworkExecutionException(msg);
        } finally {
            ProgressMonitor.increment("Disconnecting...");
            disconnect(session, channelSftp);
        }
    }

}
