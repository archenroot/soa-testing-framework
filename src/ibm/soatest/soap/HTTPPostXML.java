package ibm.soatest.soap;

 /*
 * $Header: 
 * $Revision$
 * $Date$
 * ====================================================================
 *
 *  Copyright 2002-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * [Additional notices, if required by prior licensing conditions]
 *
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;


/**
 *
 * This is a sample application that demonstrates
 * how to use the Jakarta HttpClient API.
 *
 * This application sends an XML document
 * to a remote web server using HTTP POST
 *
 * @author Sean C. Sullivan
 * @author Ortwin Glück
 * @author Oleg Kalnichevski
 */
public class HTTPPostXML {
    
    /**
     *
     * Usage:
     *          java PostXML http://mywebserver:80/ c:\foo.xml
     *
     *  @param args command line arguments
     *                 Argument 0 is a URL to a web server
     *                 Argument 1 is a local filename
     *
     */
    public static void main(String[] args) throws Exception {
       /* 
        if (args.length != 2) {
            System.out.println(
                "Usage: java -classpath <classpath> [-Dorg.apache.commons."+
                "logging.simplelog.defaultlog=<loglevel>]" +
                " PostXML <url> <filename>]");
            
            System.out.println("<classpath> - must contain the "+
                "commons-httpclient.jar and commons-logging.jar");
            
            System.out.println("<loglevel> - one of error, "+
                    "warn, info, debug, trace");
            
            System.out.println("<url> - the URL to post the file to");
            System.out.println("<filename> - file to post to the URL");
            System.out.println();
            System.exit(1);
        }
        
        // Get target URL
        String strURL = args[0];
        
        // Get file to be posted
        String strXMLFilename = args[1];
        File input = new File(strXMLFilename);
        
        // Prepare HTTP post
        PostMethod post = new PostMethod(strURL);
        
        // Request content will be retrieved directly
        // from the input stream
        // Per default, the request content needs to be buffered
        // in order to determine its length.
        // Request body buffering can be avoided when
        // content length is explicitly specified
        post.setRequestEntity(new InputStreamRequestEntity(
                new FileInputStream(input), input.length()));
        
        // Specify content type and encoding
        // If content encoding is not explicitly specified
        // ISO-8859-1 is assumed
        post.setRequestHeader(
                "Content-type", "text/xml; charset=ISO-8859-1");
        
        // Get HTTP client
        HttpClient httpclient = new HttpClient() {

            @Override
            public HttpParams getParams() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public ClientConnectionManager getConnectionManager() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public HttpResponse execute(HttpUriRequest request) throws IOException, ClientProtocolException {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public HttpResponse execute(HttpUriRequest request, HttpContext context) throws IOException, ClientProtocolException {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public HttpResponse execute(HttpHost target, HttpRequest request) throws IOException, ClientProtocolException {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context) throws IOException, ClientProtocolException {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler) throws IOException, ClientProtocolException {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) throws IOException, ClientProtocolException {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler) throws IOException, ClientProtocolException {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) throws IOException, ClientProtocolException {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
        
        // Execute request
        try {
            
            int result = httpclient.executeMethod(post);
            
            // Display status code
            System.out.println("Response status code: " + result);
            
            // Display response
            System.out.println("Response body: ");
            System.out.println(post.getResponseBodyAsString());
            
        } finally {
            // Release current connection to the connection pool 
            // once you are done
            post.releaseConnection();
        }
    }
               */
}
}