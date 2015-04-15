/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ibm.soatf.component.file;

import com.ibm.soatf.flow.FrameworkExecutionException;
import com.ibm.soatf.flow.OperationResult;
import com.ibm.soatf.tool.Utils;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;
import java.util.Vector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author zANGETSu
 */
public class FileTester {
    
    
    
    //VM
//    private static String remoteDir   = "/opt";
//    private static String fileName    = "testfile.txt";
//    private static String sshUser     = "root";
//    private static String sshPassword = "password";
//    private static String hostName    = "192.168.89.3";
//    private static int port           = 22;
  
    //FREE SSHD
    private static String remoteDir   = "/";
    private static String errorDir    = "/error";
    private static String archiveDir  = "/archive";
    private static String fileName    = "testfile.txt";
    private static String sshUser     = "krokyk";
    private static String sshPassword = "Password1";
    private static String hostName    = "localdev";
    private static int    port        = 2222;
    
    
    private static final Properties CONFIG = new java.util.Properties();
    static {
        CONFIG.put("userauth.password", "com.jcraft.jsch.UserAuthPassword");
        CONFIG.put("StrictHostKeyChecking", "no");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        
//        ConfigurationManager.getInstance().init();
//        InterfaceConfiguration icfg = ConfigurationManager.getInstance().getInterfaceConfig("IW.600.EBS_M_PCM");
//        IfaceFlowPattern ifp = icfg.getIfaceFlowPattern("FTP To Queue To SOAP Service");
        
        checkFileInDir(fileName, errorDir);
        
    }
    private static final Logger logger = LogManager.getLogger(FileTester.class);
    private static final OperationResult cor = OperationResult.getInstance();
    private static  void putFile() throws FrameworkExecutionException {
        Session session = null;
        ChannelSftp channelSftp = null;
        
        try {
            JSch jsch = new JSch();
            try {
                String msg = "Creating SSH session...";
                logger.info(msg);
                cor.addMsg(msg);
                session = jsch.getSession(sshUser, hostName, port);
                session.setPassword(sshPassword);
                session.setConfig(CONFIG);
                msg = "SSH session created.";
                logger.info(msg);
                cor.addMsg(msg);
            } catch (JSchException ex) {
                String msg = String.format("Failed to create SSH session. Reason: %s", ex.getMessage());
                cor.addMsg(msg);
                throw new FrameworkExecutionException(msg, ex);
            }
            
            try {
                String msg = "Connecting to SSH session...";
                logger.info(msg);
                cor.addMsg(msg);
                session.connect();
                msg = "Connected to SSH session.";
                logger.info(msg);
                cor.addMsg(msg);
            } catch (JSchException ex) {
                String msg = String.format("Failed to connect to the SSH session. Reason: %s", ex.getMessage());
                cor.addMsg(msg);
                throw new FrameworkExecutionException(msg, ex);
            }
            
            try {
                String msg = "Opening SSH channel...";
                logger.info(msg);
                cor.addMsg(msg);
                final Channel channel = session.openChannel("sftp");
                channelSftp = (ChannelSftp) channel;
                msg = "SSH channel opened.";
                logger.info(msg);
                cor.addMsg(msg);
            } catch (JSchException ex) {
                String msg = String.format("Failed to open the SFTP channel. Reason: %s", ex.getMessage());
                cor.addMsg(msg);
                throw new FrameworkExecutionException(msg, ex);
            }
            
            try {
                String msg = "Connecting to SSH channel...";
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
                String msg = "Changing to '" + remoteDir + "' directory...";
                logger.info(msg);
                cor.addMsg(msg);
                channelSftp.cd(remoteDir);
                msg = "Directory changed.";
                logger.info(msg);
                cor.addMsg(msg);
            } catch (SftpException ex) {
                String msg = String.format("Failed to execute 'cd '" + remoteDir + "' command. Reason: %s", ex.getMessage());
                cor.addMsg(msg);
                throw new FrameworkExecutionException(msg, ex);
            }
            
            java.io.File file = getLocalFile();
            final FileInputStream fis;
            try {
                String destFile = (remoteDir + "/" + fileName).replaceAll("//", "/"); //ensure that variable doesn't contain double slash
                cor.addMsg("Opening file '" + file.getAbsolutePath() + "' for transfer. Destination file is: " + destFile);
                fis = new FileInputStream(file);
            } catch (FileNotFoundException ex) {
                String msg = String.format("Could not open source file. Reason: %s", ex.getMessage());
                cor.addMsg(msg);
                throw new FrameworkExecutionException(msg, ex);
            }
            
            try {
                String msg = "File transfer started.";
                logger.info(msg);
                cor.addMsg(msg);
                channelSftp.put(fis, fileName);
                msg = "File transfer finished successfully.";
                logger.info(msg);
                cor.addMsg(msg);
            } catch (SftpException ex) {
                String msg = String.format("Could not transfer source file. Reason: %s", ex.getMessage());
                cor.addMsg(msg);
                throw new FrameworkExecutionException(msg, ex);
            }
        } finally {
            disconnect(session, channelSftp);
        }
    }

    public static File getLocalFile() {
        return new File("C:\\SOATestingFramework\\README.md");
    }
    
    private static void disconnect(Session session, Channel channel) {
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
    
    private static void checkFileInDir(String fileName, String dir) throws FrameworkExecutionException {
        Session session = null;
        ChannelSftp channelSftp = null;
        
        try {
            JSch jsch = new JSch();
            try {
                String msg = "Creating SSH session...";
                logger.info(msg);
                cor.addMsg(msg);
                session = jsch.getSession(sshUser, hostName, port);
                session.setPassword(sshPassword);
                session.setConfig(CONFIG);
                msg = "SSH session created.";
                logger.info(msg);
                cor.addMsg(msg);
            } catch (JSchException ex) {
                String msg = String.format("Failed to create SSH session. Reason: %s", ex.getMessage());
                cor.addMsg(msg);
                throw new FrameworkExecutionException(msg, ex);
            }
            
            try {
                String msg = "Connecting to SSH session...";
                logger.info(msg);
                cor.addMsg(msg);
                session.connect();
                msg = "Connected to SSH session.";
                logger.info(msg);
                cor.addMsg(msg);
            } catch (JSchException ex) {
                String msg = String.format("Failed to connect to the SSH session. Reason: %s", ex.getMessage());
                cor.addMsg(msg);
                throw new FrameworkExecutionException(msg, ex);
            }
            
            try {
                String msg = "Opening SSH channel...";
                logger.info(msg);
                cor.addMsg(msg);
                channelSftp = (ChannelSftp) session.openChannel("sftp");
                msg = "SSH channel opened.";
                logger.info(msg);
                cor.addMsg(msg);
            } catch (JSchException ex) {
                String msg = String.format("Failed to open the SFTP channel. Reason: %s", ex.getMessage());
                cor.addMsg(msg);
                throw new FrameworkExecutionException(msg, ex);
            }
            
            try {
                String msg = "Connecting to SSH channel...";
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
            
            Vector ls;
            try {
                String msg = "Listing '" + dir + "' content...";
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
                        System.out.println("File found!");
                        return;
                    }
                }
                System.out.println("File not found!");
            }
        } finally {
            disconnect(session, channelSftp);
        }
    }
}
