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
package com.ibm.soatf;


import com.ibm.soatf.gui.SOATestingFrameworkGUI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.BooleanOptionHandler;

/**
 *
 * @author zANGETSu
 */
public class SOATestingFramework {
    
    
    @Option(name="-env",aliases = {"--environent"}, usage="system or user interface identificator to run flow test sets on", metaVar = "ENVIRONMENT")
    private String environment;

    // 
    @Argument(usage = "-gui", metaVar="GUI")
    private String soaInterface;

    // using 'handler=...' allows you to specify a custom OptionHandler
    // implementation class. This allows you to bind a standard Java type
    // with a non-standard option syntax
    @Option(name="-custom",handler=BooleanOptionHandler.class,usage="boolean value for checking the custom handler")
    private boolean data;

    // receives other command line parameters than options
    
    @Argument(required = false)
    private List<String> soaInterfaces = new ArrayList<String>();
    
    private Map<String, String> properties = new HashMap<String, String>();
    
    // Java input properties map
    @Option(name = "-D", metaVar = "<property>=<value>",
          usage = "use value for given property")
    
    private void setProperty(final String property) throws CmdLineException {
    String[] arr = property.split("=");
    if(arr.length != 2) {
        throw new CmdLineException("Properties must be specified in the form:"+
                                   "<property>=<value>");
    }
    properties.put(arr[0], arr[1]);
  }
    
    public static void main(String[] args) {
        try {
            //FrameworkConfiguration.getInstance().checkConfiguration();
            if(args.length > 0 && args[0].equalsIgnoreCase("-gui") || true) {
                /* Set the Nimbus look and feel */
                //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
                /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
                 * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
                 */
                try {
                    for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                        if ("Nimbus".equals(info.getName())) {
                            javax.swing.UIManager.setLookAndFeel(info.getClassName());
                            break;
                        }
                    }
                } catch (ClassNotFoundException ex) {
                    java.util.logging.Logger.getLogger(SOATestingFrameworkGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                } catch (InstantiationException ex) {
                    java.util.logging.Logger.getLogger(SOATestingFrameworkGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    java.util.logging.Logger.getLogger(SOATestingFrameworkGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                } catch (javax.swing.UnsupportedLookAndFeelException ex) {
                    java.util.logging.Logger.getLogger(SOATestingFrameworkGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                }
                //</editor-fold>

                /* Create and display the form */
            
                final SOATestingFrameworkGUI soatfgui = new SOATestingFrameworkGUI();
                //setup the JTextAreLogger
                java.awt.EventQueue.invokeLater(new Runnable() {

                    public void run() {
                        soatfgui.setVisible(true);
                    }
                });
                while (!soatfgui.isShowing()) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException ex) {
                        //nothing to do
                    }
                }
                FrameworkConfiguration.getInstance().checkConfiguration();
            } else {
                //FlowManager.executeInterfaceTest("BG.RINT.047", "Ladislav Jech LocalHost");
            }
           
            /*  final String[] argv = { "-D", "key=value", "-env", "custom",
            "-D", "key2=value2", "-gui", "install" };
            final SOATestingFramework options = new SOATestingFramework();
            final CmdLineParser parser = new CmdLineParser(options);
            parser.parseArgument(argv);
            
            // print usage
            parser.setUsageWidth(Integer.MAX_VALUE);
            parser.printUsage(System.err);
            
            // check the options have been set correctly
            assertEquals("custom", options.environment);
            assertEquals(2, options.soaInterfaces.size());
            assertEquals(2, options.properties.size());
            
            init();
            */
            //FileSystem.initializeFileSystemStructure(new File(".").getCanonicalPath());
            //TestDatabaseComponent.testDatabaseComponent();
            //TestOSBComponent.testOSBComponent();
            //TestJMSComponent.testJmsComponent();
            //TestSoapComponent.testDatabaseComponent();
            
            /*
            
            String path = new File(".").getCanonicalPath().toString()
            + "\\xml-resources\\jaxb\\SOATFConfiguration\\soa-testing-framework-config.xml";
            boolean fileExists = new File(path).exists();
            System.out.println(new File(new File(".").getCanonicalPath().toString()
            + "\\xml-resources\\jaxb\\SOATFConfiguration\\soa-testing-framework-config.xml").exists());
            //SoaTestingFramework soaTF = new SoaTestingFramework("\\xml-resources\\jaxb\\SOATFConfiguration\\soa-testing-framework-config.xml");
            SoaTestingFrameworkConfiguration soaTFConfig
            = new SoaTestingFrameworkConfiguration(
            new File(".").getCanonicalPath().toString()
            + "\\xml-resources\\jaxb\\SOATFConfiguration\\soa-testing-framework-config.xml");
            
            */
            //soaTFConfig.getDatabaseType();
            //DatabaseTestComponent dtc = new DatabaseTestComponent(soaTFConfig.getDatabaseType());
            //dtc.generateSQLStatement(CRUDType.INSERT);
            /*
            WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
            
            WSDLReaderImpl r = new WSDLReaderImpl();
            //Description mydesc = r.read(new URL("file:///c:/Dev/svn-rep/trunk/osb/ContractsMaintainFromEBS/Resources/WSDLs/ContractMaintainReceiveFromEBSForMaximo.wsdl"));
            Description desc = r.read(new URL("http://prometheus:11001/HudsonDemo/proxy/SendJMSQueue"));
            
            SchemaReader sreader = r.getSchemaReader();
            //Listmydesc.getImports()
            Schema schema = sreader.read(new URL("file:///c:/Dev/svn-rep/trunk/osb/ContractsMaintainFromEBS/Resources/WSDLs/ContractMaintainReceiveFromEBSForMaximo.wsdl"));
            NamespaceMapperImpl impl = schema.getAllNamespaces();
            URI uri = schema.getDocumentURI();
            ListIterator types = schema.getTypes().listIterator();
            while (types.hasNext()){
            Object type = types.next();
            System.out.println(((Type) type).toString());
            }
            */
            /*
            
            ListIterator elements = schema.getElements().listIterator();
            int i = 0;
            List mylist = schema.getElements();
            while (elements.hasNext()) {
            Object element = elements.next();
            System.out.println(((Element) element).toString());
            }
            */
            //Description desc = reader.read(new URL("file:///c:/Dev/svn-rep/trunk/osb/ContractsMaintainFromEBS/Resources/WSDLs/ContractMaintainReceiveFromEBSForMaximo.wsdl"));
            // Write a WSDL 1.1 or 2.0 (depend of desc version)
            //Document doc = WSDLFactory.newInstance().newWSDLWriter().getDocument(desc);
            //String s= doc.getTextContent();
            /*SoapComponent stc = new SoapComponent("SendJMSQueue",
            // "http://prometheus:11001/HudsonDemo/proxy/SendJMSQueue",
            "http://prometheus:11001/HudsonDemo/proxy/SendJMSQueue",
            "sendJMSMessage",
            "soapRequest.xml",
            "soapResponse.xml");
            //stc.getAndSaveXmlSchemaFromWsdl();
            //stc.getAndSaveSoapEnvelopeRequest();
            //stc.invokeService();
            JAXPProcessor.getSoapBodyContent("soapRequest.xml");
            
            
            stc.isSoapRequestEnvelopeValid();
            stc.validateMessage(SoapComponent.FlowDirectionType.INBOUND);
            stc.validateMessage(SoapComponent.FlowDirectionType.OUTBOUND);
            System.out.println("completed.");
            UrlSchemaLoader sl = new UrlSchemaLoader(stc.getSoapEndPointUri());
            XmlObject xo = sl.loadXmlObject(stc.getSoapEndPointUri() + "?wsdl", null);
            xo.save(new File("testXMLOject.xml"));
            CachedWsdlLoader cwl = new CachedWsdlLoader(stc.getWsdlInterface());
            cwl.saveDefinition(".");
            System.out.println("Latest import: " + cwl.getLatestImportURI());
            ;        System.out.println( stc.getWsdlContext().hasSchemaTypes());
            SchemaTypeSystem sts = stc.getWsdlContext().getSchemaTypeSystem();
            SchemaType st[] = sts.documentTypes();
            /*
            List allSeenTypes = new ArrayList();
            allSeenTypes.addAll(Arrays.asList(sts.documentTypes()));
            //allSeenTypes.addAll(Arrays.asList(sts.attributeTypes()));
            //allSeenTypes.addAll(Arrays.asList(sts.globalTypes()));
            for (int i = 0; i < allSeenTypes.size(); i++)
            {
            SchemaType sType = (SchemaType)allSeenTypes.get(i);
            System.out.println("Visiting " + sType.toString());
            
            allSeenTypes.addAll(Arrays.asList(sType.getAnonymousTypes()));
            }
            
            
            
            System.exit(0);
            */
        } catch (FrameworkConfigurationException ex) {
            Logger.getLogger(SOATestingFramework.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
