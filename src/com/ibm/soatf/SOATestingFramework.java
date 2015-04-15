/*
 * Copyright (C) 2013 Ladislav Jech
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

import com.ibm.soatf.config.ConfigurationManager;
import com.ibm.soatf.config.DirectoryStructureManager;
import com.ibm.soatf.config.FrameworkConfigurationException;
import com.ibm.soatf.config.MasterConfiguration;
import com.ibm.soatf.config.master.Interface;
import com.ibm.soatf.config.master.Project;
import com.ibm.soatf.config.master.SOATestingFrameworkMasterConfiguration;
import com.ibm.soatf.flow.FlowExecutor;
import com.ibm.soatf.flow.FrameworkExecutionException;
import com.ibm.soatf.gui.SOATestingFrameworkGUI;
import com.ibm.soatf.gui.logging.JTextAreaAppender;
import java.awt.Color;
import java.util.List;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * SOA Testing Framework main class. Used to parse input parameters and start
 * GUI based interface or process command-line execution.
 *
 * @author Ladislav Jech <archenroot@gmail.com>
 */
public class SOATestingFramework {

    private static final Logger logger
            = LogManager.getLogger(SOATestingFramework.class.getName());

    /**
     * SOA Testing Framework main static method.
     *
     * @param args Main input parameters.
     */
    public static void main(String[] args) {
        Options options = new Options();
        options.addOption(new Option("gui", "Display a GUI"));

        options.addOption(OptionBuilder.withArgName("environment")
                .hasArg()
                .withDescription("Environment to run the tests on")
                .create("env")); // has a value
        options.addOption(OptionBuilder.withArgName("project")
                .hasArg()
                .withDescription("Project to run the tests on")
                .create("p")); // has a value
        options.addOption(OptionBuilder
                .withArgName("interface")
                .hasArg()
                .withDescription("Interface to run the tests on")
                .create("i")); // has a value
        CommandLineParser parser = new BasicParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            validate(cmd);

            if (cmd.hasOption("gui")) {

                /* Set the Nimbus look and feel */
                //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
                /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
                 * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
                 */
                try {
                    if(false) { //disabled the OS Look'n'Feel
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());                        
                    } else {
                        for (UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                            if ("Nimbus".equals(info.getName())) {
                                javax.swing.UIManager.setLookAndFeel(info.getClassName());
                                break;
                            }
                        }
                        UIManager.getLookAndFeelDefaults().put("nimbusOrange", (new Color(0,128,255)));                  
                    }
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
                    logger.error("Cannot set look and feel", ex);
                }
                //</editor-fold>

                final SOATestingFrameworkGUI soatfgui = new SOATestingFrameworkGUI();
                java.awt.EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        soatfgui.setVisible(true);
                    }
                });
            } else {
                //<editor-fold defaultstate="collapsed" desc="Command line mode">
                try {
                    // Initialization of configuration manager.
                    ConfigurationManager.getInstance().init();
                    
                    String env = cmd.getOptionValue("env", null);
                    String ifaceName;
                    boolean inboundOnly = false;
                    if (cmd.hasOption("p")) {
                        String projectName = cmd.getOptionValue("p");
                        MasterConfiguration masterConfig = ConfigurationManager.getInstance().getMasterConfig();
                        List<SOATestingFrameworkMasterConfiguration.Interfaces.Interface> interfaces = masterConfig.getInterfaces();
                        all:
                        for (Interface iface : interfaces) {
                            List<Project> projects = iface.getProjects().getProject();
                            for (Project project : projects) {
                                if (project.getName().equals(projectName)) {
                                    inboundOnly = "INBOUND".equalsIgnoreCase(project.getDirection());
                                    ifaceName = iface.getName();
                                    break all;
                                }
                            }
                        }
                        throw new FrameworkExecutionException("No such project found in master configuration: " + projectName);
                    } else {
                        ifaceName = cmd.getOptionValue("i");
                        inboundOnly = false;
                    }
                    DirectoryStructureManager.checkFrameworkDirectoryStructure(ifaceName);
                    FlowExecutor flowExecutor = new FlowExecutor(inboundOnly, env, ifaceName);
                    flowExecutor.execute();
                } catch (FrameworkConfigurationException ex) {
                    logger.fatal("Configuration corrupted. See the exception stack trace for details.", ex);
                } catch (FrameworkException ex) {
                    logger.fatal(ex);
                }
//</editor-fold>
            }
        } catch (ParseException ex) {
            logger.fatal("Could not parse the command line arguments. Reason: " + ex);
            printUsage();
        } catch (Throwable ex) {
            logger.fatal("Unexpected error occured: ", ex);
            printUsage();
            System.exit(-1);
        }
    }

    /**
     * throws an exception if the provided command line doesn't contain enough arguments to run the framework
     * @param cmd
     * @throws FrameworkExecutionException 
     */
    private static void validate(CommandLine cmd) throws FrameworkExecutionException {
        if (cmd.hasOption("gui")) {
            //ok
        } else {
            //dummy JTextArea object for the GUI logger to avoid stupid errors from it
            //proper way would be to programmatically remove this appender from the logging framework when there's no GUI
            JTextAreaAppender.setJTextArea(new JTextArea());
            if (cmd.hasOption("env")) {
                if (cmd.hasOption("p") && cmd.hasOption("i")) {
                    throw new FrameworkExecutionException("Specify either -p or -i parameter, not both");
                } else {
                    if (!cmd.hasOption("p") && !cmd.hasOption("i")) {
                        throw new FrameworkExecutionException("You must specify either -p or -i parameter");
                    }
                }
            } else {
                throw new FrameworkExecutionException("Parameter -gui or -env expected");
            }
        }
    }
    
<<<<<<< HEAD
    /**
     * prints list of command line arguments identified by framework
     */
=======
>>>>>>> 7c2802d5d20e30d5191a0f8f327cacd09e189422
    private static void printUsage() {
        logger.info("USAGE: java -jar SOATestingFramework.jar {[-gui|{-env environment|{-p project|-i interface}}]} ");
        logger.info("Example1: java -jar SOATestingFramework.jar -gui\tRuns the framework in the gui mode");
        logger.info("Example2: java -jar SOATestingFramework.jar -env \"dev3\" -p \"PaymentRequestsFromPCM\"\tRuns the framework without the gui on the specified environment and specified project");
        logger.info("Example3: java -jar SOATestingFramework.jar -env \"dev3\" -i \"IW.701.M_PCM_EBS\"\tRuns the framework without the gui on the specified environment and specified interface (i.e. set of projects that comprise that interface)");
    }
}
