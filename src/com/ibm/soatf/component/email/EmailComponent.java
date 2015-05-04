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
package com.ibm.soatf.component.email;

import com.ibm.soatf.component.AbstractSoaTFComponent;
import com.ibm.soatf.component.SOATFCompType;
import com.ibm.soatf.config.iface.IfaceExecBlock;
import com.ibm.soatf.config.iface.email.Attachment;
import com.ibm.soatf.config.iface.email.Email;
import com.ibm.soatf.config.iface.email.ReadProtocol;
import com.ibm.soatf.config.iface.email.Security;
import com.ibm.soatf.config.master.EmailServers.EmailServer.EmailServerInstance;
import com.ibm.soatf.config.master.EmailServers.EmailServer.Directories;
import com.ibm.soatf.config.master.Operation;
import com.ibm.soatf.flow.FlowExecutor;
import com.ibm.soatf.flow.FrameworkExecutionException;
import com.ibm.soatf.flow.OperationResult;
import com.ibm.soatf.gui.ProgressMonitor;
import com.ibm.soatf.tool.Utils;
import com.sun.mail.pop3.POP3SSLStore;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Vladimir Zarik
 */
public class EmailComponent extends AbstractSoaTFComponent {
    
    /**
     * Default suffix for JMS related artefacts.
     *
     *
     */
    public static final String MESSAGE_SUFFIX = ".xml";

    /**
     *
     */
    public static final String NAME_DELIMITER = "_";    

    private static final Logger logger = LogManager.getLogger(EmailComponent.class);
    public static String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

    private final EmailServerInstance emailMasterConfig;
    private final Email email;
    private final Directories directories;

    private String hostName;
    private int port;
    private String user;
    private String password;
    private Security security;
    private ReadProtocol protocol;
    private String stageDirectory;
    private String errorDirectory;
    private String archiveDirectory;
    private String fileContent;
    private String fileName;

    private static final Map<String, File> attachments = new HashMap<>();

    private final OperationResult cor;

    public EmailComponent(
            IfaceExecBlock ifaceExecBlock,
            EmailServerInstance emailMasterConfig,
            Email email,
            Directories directories,
            File workingDir) {
        super(SOATFCompType.EMAIL);
        this.emailMasterConfig = emailMasterConfig;
        //this.ftpConfiguration = ftpInterfaceConfig;
        this.email = email;
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

        this.hostName = this.emailMasterConfig.getHostName();
        this.port = this.emailMasterConfig.getPort();
        this.user = this.emailMasterConfig.getPrincipal();
        this.password = this.emailMasterConfig.getPassword();
        this.security = Security.fromValue(this.emailMasterConfig.getSecurity());
        if (this.emailMasterConfig.getReadProtocol() != null) {
            this.protocol = ReadProtocol.fromValue(this.emailMasterConfig.getReadProtocol());
        }
        if (directories != null) {
            this.stageDirectory = directories.getStageDirectory();
            this.errorDirectory = directories.getErrorDirectory();
            this.archiveDirectory = directories.getArchiveDirectory();
        }

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
        //actualFileUsed = generateFile(this.workingDir, this.fileName, this.fileContent).getName();//(this.workingDir, this.fileName, this.fileContent).getName();
        switch (operation.getName()) {
            case EMAIL_READ:
                emailRead();
                break;
            case EMAIL_SEND:
                //generateFile();
                emailSend();
                break;
            case EMAIL_CHECK_STRUCTURE:
                emailCheck();
                break;
            /*case FTP_CHECK_DELIVERED_FOLDER_FOR_FILE:
             checkFolderForFile(this.archiveDirectory);
             break;
             case FTP_CHECK_ERROR_FOLDER_FOR_FILE:
             checkFolderForFile(this.errorDirectory);
             break;
             case FTP_SEARCH_FOR_FILE:
             checkFolderForFile(this.stageDirectory);
             break;*/
            default:
                logger.info("Operation execution not yet implemented: " + operation.getName().value());
                cor.addMsg("Operation: " + operation.getName().value() + " is valid, but not yet implemented");
        }
    }

    private void emailRead() throws EmailComponentException {
        ProgressMonitor.init(8, "Preparing for reading email...");
        Properties props = new Properties();
        Session session = null;
        Store store = null;
        try {
            switch (this.protocol) {
                case POP_3:
                    props.put("mail.pop3.host", this.hostName);
                    props.put("mail.pop3.port", this.port);
                    props.put("mail.pop3.auth", "true");
                    switch (this.security) {
                        case TLS:
                            props.setProperty("mail.pop3.ssl.enable", "false");
                            props.setProperty("mail.pop3.starttls.enable", "true"); 
                            props.setProperty("mail.pop3.starttls.required", "true");
                            ProgressMonitor.increment("Initiating Email POP3 TLS session");  
                            session = Session.getDefaultInstance(props);
                            ProgressMonitor.increment("Connecting to the email store...");
                            store = session.getStore("pop3");
                            store.connect(this.hostName, this.user, this.password);                      
                            break;
                        case SSL: 
                            props.put("mail.pop3.socketFactory.port", this.port);
                            props.put("mail.pop3.socketFactory.class", SSL_FACTORY);
                            props.put("mail.pop3.socketFactory.fallback", "false");
                            ProgressMonitor.increment("Initiating Email POP3 SSL session");   
                            URLName url = new URLName("pop3", this.hostName, this.port, "", this.user, this.password);        
                            session = Session.getInstance(props, null);
                            ProgressMonitor.increment("Connecting to the email store...");
                            store = new POP3SSLStore(session, url);
                            store.connect();
                            break;
                        default:
                            ProgressMonitor.increment("Initiating Email POP3 session");
                            session = Session.getDefaultInstance(props);
                            ProgressMonitor.increment("Connecting to the email store...");
                            store = session.getStore("pop3");
                            store.connect(this.hostName, this.user, this.password);
                            break;
                    }
                    break;
                case IMAP:
                    props.put("mail.store.protocol", "imap");
                    URLName url = new URLName("imap", this.hostName, this.port, "", this.user, this.password); 
                    switch (this.security) {
                       case SSL:
                            props.setProperty("mail.store.protocol", "imaps");
                            props.put("mail.imap.socketFactory.class", SSL_FACTORY);
                            props.put("mail.imap.socketFactory.fallback", "false");
                            props.put("mail.imap.socketFactory.port", Integer.toString(this.port));
                            ProgressMonitor.increment("Initiating Email IMAP SSL session");
                            session = Session.getInstance(props, null);
                            ProgressMonitor.increment("Connecting to the email store...");
                            store = session.getStore(url);
                            store.connect();
                            break;
                        case TLS:
                            props.setProperty("mail.imap.socketFactory.fallback", "false");
                            props.setProperty("mail.imap.starttls.enable", "true");
                            ProgressMonitor.increment("Initiating Email IMAP SSL session");
                            session = Session.getInstance(props, null);
                            ProgressMonitor.increment("Connecting to the email store...");
                            //store = session.getStore("imap");
                            store = session.getStore(url);
                            store.connect();
                            //store.connect(this.hostName, this.user, this.password);
                            break;
                        default:
                            
                            break;
                    }
                    break;
                default:
                    final String msg = "Email protocol type not supported: " + security;
                    logger.info(msg);
                    cor.addMsg(msg);
                    throw new EmailComponentException(msg);
            } 

            ProgressMonitor.increment("Opening email folder...");
            Folder inbox = store.getFolder(this.email.getInbound().getFolder());
            inbox.open(Folder.READ_ONLY);
            ProgressMonitor.increment("Reading the email message...");
            Message msg = inbox.getMessage(inbox.getMessageCount());
            
            ProgressMonitor.increment("Saving the email message to disk...");               
            storeAttachments(msg, workingDir, this.hostName);
            cor.addMsg("Email was successfully read.");
            cor.markSuccessful();
        } catch (MessagingException | IOException emailex) {
            final String msg = "Error while reading the email.";
            cor.addMsg(msg);
            throw new EmailComponentException(msg, emailex);
        }
    }

//    private void checkFolderForFile(String folderName) throws EmailComponentException {
//        FTPClient client = null;
//        if (actualFileUsed == null) {
//            final String msg = "File name was not set.";
//            cor.addMsg(msg);
//            throw new EmailComponentException(msg);
//        }
//        switch (security) {
//            case NONE:
//                try {
//                    ProgressMonitor.init(5, "Connecting to FTP server...");
//                    client = new FTPClient();
//                    client.connect(this.hostName, this.port);
//                    ProgressMonitor.increment("Logging in...");
//                    client.login(this.user, this.password);
//                    ProgressMonitor.increment("Changing directory...");
//                    client.changeDirectory(folderName);
//                    ProgressMonitor.increment("Listing files...");
//                    FTPFile[] fileArray = client.list();
//                    boolean found = false;
//                    for (FTPFile file : fileArray) {
//                        String name = file.getName();
//                        if (name != null && (name.equals(actualFileUsed) || name.endsWith("__" + actualFileUsed))) {
//                            found = true;
//                            break;
//                        }
//                    }
//                    if (found) {
//                        logger.info("File with name '" + actualFileUsed + "' was found in directory: " + folderName);
//                        cor.addMsg("File with name '" + actualFileUsed + "' was found in directory: " + folderName);
//                        cor.markSuccessful();
//                    } else {
//                        final String message = "File with name '" + actualFileUsed + "' was not found in directory: " + folderName;
//                        logger.info(message);
//                        cor.addMsg(message);
//                        String additionalMessage=null;
//                        client.changeDirectory(this.stageDirectory);
//                        fileArray = client.list();
//                        for (int i = 0; i < fileArray.length; i++) {
//                            FTPFile file = fileArray[i];
//                            String name = file.getName();
//                            if (name != null && (name.equals(actualFileUsed) || name.endsWith("__" + actualFileUsed))) {
//                                additionalMessage = "The file was found in the original directory, it seems not to be polled at all.\n";
//                                break;
//                            }
//                        }
//                        if (additionalMessage == null) {
//                            client.changeDirectory(this.errorDirectory);
//                            fileArray = client.list();
//                            for (int i = 0; i < fileArray.length; i++) {
//                                FTPFile file = fileArray[i];
//                                String name = file.getName();
//                                if (name != null && (name.equals(actualFileUsed) || name.endsWith("__" + actualFileUsed))) {
//                                    additionalMessage = "The file was found in the error directory. There was an error while processing the file.";
//                                    break;
//                                }
//                            }
//                        }
//                        if (additionalMessage == null) {
//                            client.changeDirectory(this.archiveDirectory);
//                            fileArray = client.list();
//                            for (int i = 0; i < fileArray.length; i++) {
//                                FTPFile file = fileArray[i];
//                                String name = file.getName();
//                                if (name != null && (name.equals(actualFileUsed) || name.endsWith("__" + actualFileUsed))) {
//                                    additionalMessage = "The file was found in the archive directory. File was processed without any errors.";
//                                    break;
//                                }
//                            }
//                        }
//                        cor.addMsg(additionalMessage);                        
//                        throw new EmailComponentException(message);
//                    }
//                    break;
//                } catch (FTPException | FTPIllegalReplyException | IllegalStateException | IOException | FTPDataTransferException | FTPAbortedException | FTPListParseException ftpex) {
//                    final String msg = "FTP error while trying to search in the remote folder";
//                    cor.addMsg(msg);
//                    throw new EmailComponentException(msg, ftpex);
//                } finally {
//                    ProgressMonitor.increment("Disconnecting...");
//                    if (client != null) {
//                        try {
//                            client.disconnect(true);
//                        } catch (IllegalStateException | IOException | FTPIllegalReplyException | FTPException ex) {;}
//                    }
//                }
//            case SSH:
//                Session session = null;
//                ChannelSftp channelSftp = null;
//
//                try {
//                    JSch jsch = new JSch();
//                    String sshMsg = "Creating SSH session...";
//                    ProgressMonitor.init(6, sshMsg);
//                    logger.info(sshMsg);
//                    cor.addMsg(sshMsg, null);
//                    session = jsch.getSession(this.user, this.hostName, this.port);
//                    session.setPassword(this.password);
//                    session.setConfig(FileComponent.CONFIG);
//                    sshMsg = "SSH session created.";
//                    logger.info(sshMsg);
//                    cor.addMsg(sshMsg, null);
//
//                    sshMsg = "Connecting to SSH session...";
//                    ProgressMonitor.increment(sshMsg);
//                    logger.info(sshMsg);
//                    cor.addMsg(sshMsg, null);
//                    session.connect();
//                    sshMsg = "Connected to SSH session.";
//                    logger.info(sshMsg);
//                    cor.addMsg(sshMsg, null);
//
//                    sshMsg = "Opening SSH channel...";
//                    ProgressMonitor.increment(sshMsg);
//                    logger.info(sshMsg);
//                    cor.addMsg(sshMsg, null);
//                    channelSftp = (ChannelSftp) session.openChannel("sftp");
//                    sshMsg = "SSH channel opened.";
//                    logger.info(sshMsg);
//                    cor.addMsg(sshMsg, null);
//
//                    sshMsg = "Connecting to SSH channel...";
//                    ProgressMonitor.increment(sshMsg);
//                    logger.info(sshMsg);
//                    cor.addMsg(sshMsg, null);
//                    channelSftp.connect();
//                    sshMsg = "Connected to SSH channel.";
//                    logger.info(sshMsg);
//                    cor.addMsg(sshMsg);
//
//                    Vector ls;
//
//                    sshMsg = "Listing '" + folderName + "' content...";
//                    ProgressMonitor.increment("Listing directory content...");
//                    logger.info(sshMsg);
//                    cor.addMsg(sshMsg);
//                    ls = channelSftp.ls(folderName);
//
//                    if(!Utils.isEmpty(ls)) {
//                        for (Object object : ls) {
//                            ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) object;
//                            final String name = entry.getFilename();
//                            if (name != null && (name.equals(actualFileUsed) || name.endsWith("__" + actualFileUsed))) {
//                            //if (entry.getFilename().matches(fileName)) {
//                                String msg = "File '" + actualFileUsed + "' found in '" + folderName + "'";
//                                logger.info(msg);
//                                cor.addMsg(msg);
//                                cor.markSuccessful();
//                                return;
//                            }
//                        }
//                    }
//                    sshMsg = "File '" + actualFileUsed + "' not found in '" + folderName + "'";
//                    cor.addMsg(sshMsg);
//
//                    String additionalMessage=null;
//                    ls = channelSftp.ls(this.stageDirectory);                        
//                    if(!Utils.isEmpty(ls)) {
//                        for (Object object : ls) {
//                            ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) object;
//                            final String name = entry.getFilename();
//                            if (name != null && (name.equals(actualFileUsed) || name.endsWith("__" + actualFileUsed))) {
//                                additionalMessage =  "The file was found in the original directory, it seems not to be polled at all.\n";
//                                break;
//                            }
//                        }
//                    }
//                    if (additionalMessage == null) {
//                        ls = channelSftp.ls(this.errorDirectory);     
//                        if(!Utils.isEmpty(ls)) {
//                            for (Object object : ls) {
//                                ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) object;
//                                final String name = entry.getFilename();
//                                if (name != null && (name.equals(actualFileUsed) || name.endsWith("__" + actualFileUsed))) {
//                                    additionalMessage =  "The file was found in the error directory. There was an error while processing the file.";
//                                    break;
//                                }
//                            }
//                        }
//                    }
//                    if (additionalMessage == null) {
//                        ls = channelSftp.ls(this.archiveDirectory);     
//                        if(!Utils.isEmpty(ls)) {
//                            for (Object object : ls) {
//                                ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) object;
//                                final String name = entry.getFilename();
//                                if (name != null && (name.equals(actualFileUsed) || name.endsWith("__" + actualFileUsed))) {
//                                    additionalMessage = "The file was found in the archive directory. File was processed without any errors.";
//                                    break;
//                                }
//                            }
//                        }                            
//                    }
//                    cor.addMsg(additionalMessage);                         
//                    throw new EmailComponentException(sshMsg);
//                } catch (JSchException | SftpException ftpex) {
//                    final String msg = "FTP error while trying to search in the remote folder";
//                    cor.addMsg(msg);
//                    throw new EmailComponentException(msg, ftpex);
//                } finally {
//                    ProgressMonitor.increment("Disconnecting...");
//                    FileComponent.disconnect(session, channelSftp);
//                }                                                                                                                                                                                                                                                                                                                                
//            default:
//                final String msg = "Security type not supported: " + security;
//                logger.info(msg);
//                cor.addMsg(msg);
//                throw new EmailComponentException(msg);
//        }
//    }
    
     private void emailCheck() throws EmailComponentException {
        if (email.getEmailAttachment() != null) {
            ProgressMonitor.init(2+email.getEmailAttachment().size(), "Preparing to read email structure...");        
        } else {
            ProgressMonitor.init(2, "Preparing for reading email structure...");
        }
        boolean hasError = false;

        File file = new File(workingDir, this.hostName+MESSAGE_SUFFIX);
        if (!file.exists()) {
            hasError = true;
            cor.addMsg("Email body was not found");
        } else {
            String subject = null;
            try (
                InputStream fis = new FileInputStream(file);
                InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
                BufferedReader br = new BufferedReader(isr);
            ) {
                subject = br.readLine();
                Pattern pattern = Pattern.compile(this.email.getInbound().getSubject());
                Matcher matcher = pattern.matcher(subject); 
                if (subject == null || !matcher.find()) {
                    hasError = true;
                    cor.addMsg("Email subject don't match the expected string");
                }
            } catch (IOException e) {
                hasError = true;
                cor.addMsg("Error while reading the email subject");
            }            
        }
        ProgressMonitor.increment("Email body & subject checked");
        if (email.getEmailAttachment() != null) {
            for (Attachment a: email.getEmailAttachment()) {
                final String fileName = new StringBuilder(this.hostName).append(NAME_DELIMITER).append(a.getFileName()).toString();
                logger.trace("Looking for attachment with name "+fileName);
                if (attachments.size() > 0) {
                    if (attachments.get(fileName.toLowerCase()) == null) {
                        hasError = true;
                        cor.addMsg("Email attachment with name "+fileName+" was not found");                    
                    }
                } else {
                    file = new File(workingDir, fileName.toLowerCase());
                    if (!file.exists()) {
                        hasError = true;
                        cor.addMsg("Email attachment with name "+fileName+" was not found");                        
                    }
                }
                ProgressMonitor.increment("Email attachmend "+fileName+" was checked");
            }
        }
        if (!hasError) {
            cor.addMsg("Email structure is complete.");
            cor.markSuccessful();
        }
        ProgressMonitor.markDone();
    }
     
    private static void storeAttachments(Message message, File path, String fileNamePrefix) throws MessagingException, IOException {
        if (!(message.getContent() instanceof Multipart)) {
                final File file = new File(path, fileNamePrefix+MESSAGE_SUFFIX);
                final StringBuilder bodyContent = new StringBuilder(message.getSubject());
                bodyContent.append("\n");
                bodyContent.append(message.getContent().toString());                       
                FileUtils.writeStringToFile(file, bodyContent.toString());
                return;
        }
        Multipart multipart = (Multipart) message.getContent();
        attachments.clear();
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);
            if(!Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition()) && (bodyPart.getFileName() == null || "".equals(bodyPart.getFileName()))) {
                final File file = new File(path, fileNamePrefix+MESSAGE_SUFFIX);
                final StringBuilder bodyContent = new StringBuilder(message.getSubject());
                bodyContent.append("\n");
                bodyContent.append(bodyPart.getContent().toString());                       
                FileUtils.writeStringToFile(file, bodyContent.toString());
                continue;
            }
            final String filename = new StringBuilder(fileNamePrefix).append(NAME_DELIMITER).append(bodyPart.getFileName()).toString();
            final File file = new File(path, filename);
            FileUtils.writeStringToFile(file, bodyPart.getContent().toString());
            //attachments.add(file);
            attachments.put(filename.toLowerCase(), file);
        }
    }
    
    private void emailSend() throws EmailComponentException {

        //File localFile = new File(workingDir, actualFileUsed);
        
        ProgressMonitor.init(5, "Preparing for sending email...");
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", this.hostName);
        props.put("mail.smtp.port", Integer.toString(this.port));                
        switch (this.security) {
            case TLS:
                props.put("mail.smtp.starttls.enable", "true");                        
                ProgressMonitor.increment("Initiating Email TLS session");               
                break;
            case SSL:
                props.put("mail.smtp.socketFactory.port", Integer.toString(this.port));
		props.put("mail.smtp.socketFactory.class", SSL_FACTORY);
                ProgressMonitor.increment("Initiating Email SSL session");
                break;
            default:
                ProgressMonitor.increment("Initiating Email session");
                break;
        }
        Session session = Session.getInstance(props,
          new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(user, password);
                }
          }); 
        session.setDebug(true);
        session.setDebugOut(System.out);

        try {
            ProgressMonitor.increment("Building the email message...");
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(this.user));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(this.email.getOutbound().getRecipient()));
            msg.setSubject(Utils.insertTimestampToFilename(this.email.getOutbound().getSubject(), FlowExecutor.getActualRunDate()));
            if (this.email.getEmailAttachment() != null && this.email.getEmailAttachment().size() > 0) {
                Multipart mp = new MimeMultipart();

                MimeBodyPart textPart = new MimeBodyPart();
                textPart.setContent(this.email.getOutbound().getContent(), "text/plain");
                mp.addBodyPart(textPart);

                for (Attachment a: this.email.getEmailAttachment()) {
                    final MimeBodyPart attachment = new MimeBodyPart();
                    attachment.setFileName(a.getFileName());
                    attachment.setContent(a.getFileContent(), a.getFileEncoding());
                    mp.addBodyPart(attachment);
                }
                msg.setContent(mp);
            } else {
                msg.setText(this.email.getOutbound().getContent());
            }
            ProgressMonitor.increment("Sending the email message...");
            Transport transport = session.getTransport("smtp");
            transport.connect(this.hostName, this.user, this.password);
            transport.sendMessage(msg, msg.getAllRecipients());
            transport.close();
            //Transport.send(msg); 
            cor.addMsg("Email was successfully sent.");
            cor.markSuccessful();
        } catch (MessagingException emailex) {
            final String msg = "Error while sending the email.";
            cor.addMsg(msg);
            throw new EmailComponentException(msg, emailex);
        }
    }

    private File generateFile(File path, String fileName, String fileContent) throws EmailComponentException {
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
                } catch (IOException ex) {;
                }
            }
        }
        return localFile;
    }
    
    public File[] getStoredFiles(String fileNamePrefix, String objectName) {
        String pattern = "*";
        if (objectName != null) {
            pattern = objectName;
        }
        final String filemask = new StringBuilder(fileNamePrefix).append(NAME_DELIMITER).append(pattern).toString();
        return FileUtils.convertFileCollectionToFileArray(FileUtils.listFiles(workingDir, new WildcardFileFilter(filemask), TrueFileFilter.INSTANCE));
    }

    public File getFile(File workingDirectory, String fileName, String fileContent) throws EmailComponentException {
        String pattern = "*-" + fileName;
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
