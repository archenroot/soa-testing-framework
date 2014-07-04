package com.ibm.soatf.component.soap;

import com.ibm.soatf.gui.ProgressMonitor;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 *
 * @author zANGETSu
 */
public class JAXWSDispatch{

    private final static Logger logger = LogManager.getLogger(JAXWSDispatch.class);
    private boolean basicAuthOn = false;
    
     public void invokeServiceWithProvidedSOAPRequest(String urlString, String requestFile, String responseFileName) throws SoapComponentException  {
        try {
            //final String filename = new StringBuilder("").toString();
            final URL url = new URL(urlString);
            final String requestEnvelope = FileUtils.readFileToString(new File(requestFile));
            
            final SOAPMessage response = invoke(url, requestEnvelope);
            final File responseFile = new File(responseFileName);
            if (responseFile.exists()) {
                FileUtils.forceDelete(responseFile);
            }
            try (FileOutputStream fos = new FileOutputStream(responseFile)) {
                response.writeTo(fos);
            }
        } catch (Throwable th) {
            String msg = "" + th.getMessage();
            logger.error(msg);
            throw new SoapComponentException(th);
        }
    }
    /**
     *
     * @param endpointUrl
     * @param xmlMsg
     * @return
     * @throws javax.xml.soap.SOAPException
     * @throws java.io.IOException
     */
    public SOAPMessage invoke(URL endpointUrl, String xmlMsg) throws SOAPException, IOException {
        /** Create a service and add at least one port to it. **/
        
        SOAPConnectionFactory sfc = SOAPConnectionFactory.newInstance();
        
        SOAPConnection connection = sfc.createConnection();
        
        InputStream is = new ByteArrayInputStream(xmlMsg.getBytes());
        SOAPMessage request = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL).createMessage(new MimeHeaders(), is);
        request.removeAllAttachments();
        ProgressMonitor.increment("Invoking service...");
        SOAPMessage response = connection.call(request, endpointUrl);
        connection.close();
        
        response.writeTo(System.out);
        //response.writeTo(new FileOutputStream(new File("res.xml")));
        
        return response;
    }
}