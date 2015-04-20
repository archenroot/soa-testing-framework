/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ibm.soatf.component.util;
import com.ibm.soatf.flow.FrameworkExecutionException;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.Date;
import java.util.Properties;

/**
 *
 *  MailAgent.java from.
 *  Class for sending email message using the JavaMail API trough SMTP server.
 *
 *  Usage:
 *
 *  MailAgent agent = new MailAgent(to, cc, bcc, from, subject, body, smtpHost);
 *  agent.sendMessage();
 *
 *  The fields "to", "cc", and "bcc" can be comma-separated sequences of addresses.
 *  @author zANGETSu
 *
 */
public class MailAgent
{
  private String to;
  private String cc;
  private String bcc;
  private String from;
  private String subject;
  private String content;
  private String smtpHost;
  private Message message;

  // Dummy constructor, shouldn't be used as soon as it will not work in current state!!!
  public MailAgent(){};
  
  // Prefered way of how it should be used.
  public MailAgent(String to,
                   String cc,
                   String bcc,
                   String from,
                   String subject,
                   String content,
                   String smtpHost)
  throws FrameworkExecutionException
  {
      try {
          this.to = to;
          this.cc = cc;
          this.bcc = bcc;
          this.from = from;
          this.subject = subject;
          this.content = content;
          this.smtpHost = smtpHost;
          
          message = createMessage();
          message.setFrom(new InternetAddress(from));
          setToCcBccRecipients();
          
          message.setSentDate(new Date());
          message.setSubject(subject);
          message.setText(content);
      } catch (AddressException ex) {
          throw new FrameworkExecutionException(ex);
      } catch (MessagingException ex) {
          throw new FrameworkExecutionException(ex);
      } finally {
          
      }
  }

  public void sendMessage() throws FrameworkExecutionException 
  {
    try
    {
      Transport.send(message);
    }
    catch (MessagingException me)
    {
      // do logging here
      throw new FrameworkExecutionException(me);
    }
  }

  private Message createMessage()
  {
    Properties properties = new Properties();
    properties.put("mail.smtp.host", smtpHost);
    Session session = Session.getDefaultInstance(properties, null);
    return new MimeMessage(session);
  }

  private void setToCcBccRecipients()
  throws FrameworkExecutionException
  {
    setMessageRecipients(to, Message.RecipientType.TO);
    setMessageRecipients(cc, Message.RecipientType.CC);
    setMessageRecipients(bcc, Message.RecipientType.BCC);
  } 

  private void setMessageRecipients(String recipient, Message.RecipientType recipientType)
  throws FrameworkExecutionException
  {
    InternetAddress[] addressArray = buildInternetAddressArray(recipient);

    if ((addressArray != null) && (addressArray.length > 0))
    {
        try {
            message.setRecipients(recipientType, addressArray);
        } catch (MessagingException ex) {
            throw new FrameworkExecutionException(ex);
        }
    }
  }

  /**
   * The address can be a comma-separated sequence of email addresses.
   * @see mail.internet.InternetAddress.parse() for details.
   *
   */
  private InternetAddress[] buildInternetAddressArray(String address)
  throws FrameworkExecutionException
  {
    // could test for a null or blank String but I'm letting parse just throw an exception
    InternetAddress[] internetAddressArray = null;
    try
    {
      internetAddressArray = InternetAddress.parse(address);
    }
    catch (AddressException ae)
    {
      // do logging here
      throw new FrameworkExecutionException(ae);
    }
    return internetAddressArray;
  }

}