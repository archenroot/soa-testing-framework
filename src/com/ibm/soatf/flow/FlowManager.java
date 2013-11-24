/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ibm.soatf.flow;



import com.ibm.soatf.config.master.Databases;
import com.ibm.soatf.config.master.Databases.Database.DatabaseInstance;
import com.ibm.soatf.config.master.Environment;
import com.ibm.soatf.config.master.EnvironmentType;
import static com.ibm.soatf.config.master.ExecuteOn.SOURCE;
import static com.ibm.soatf.config.master.ExecuteOn.TARGET;
import com.ibm.soatf.config.master.ExecutionBlock;
import com.ibm.soatf.config.master.FTPServers;
import com.ibm.soatf.config.master.FTPServers.FtpServer.FtpServerInstance;
import com.ibm.soatf.config.master.FileSystemProjectStructure;
import com.ibm.soatf.config.master.FlowPattern;
import com.ibm.soatf.config.master.Interface;
import com.ibm.soatf.config.master.Interface.Patterns.ReferencedFlowPattern;
import com.ibm.soatf.config.master.OSBReporting.OsbReportingInstance;
import com.ibm.soatf.config.master.Operation;
import com.ibm.soatf.config.master.OracleFusionMiddleware;
import com.ibm.soatf.config.master.OracleFusionMiddleware.OracleFusionMiddlewareInstance;
import com.ibm.soatf.config.master.TestScenario;
import com.ibm.soatf.component.database.DatabaseComponent;
import com.ibm.soatf.component.ftp.FTPComponent;
import com.ibm.soatf.component.jms.JMSComponent;
import com.ibm.soatf.component.osb.OSBComponent;
import com.ibm.soatf.component.soap.SOAPComponent;
import com.ibm.soatf.component.util.UtilityComponent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author zANGETSu
 */
public class FlowManager {

   
}
