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
package ibm.soatest.jms;

import ibm.soatest.CompOperResult;
import java.util.logging.Level;
import java.util.logging.Logger;
import ibm.soatest.SOATFComponent;
import ibm.soatest.SOATFCompType;
import ibm.soatest.CompOperType;
import ibm.soatest.config.database.DatabaseConfiguration;
import ibm.soatest.config.jms.JMSConfiguration;

/**
 *
 * @author zANGETSu
 */
public class JMSComponent extends SOATFComponent {
    private JMSConfiguration jmsConfiguration;
    
    private CompOperResult componentOperationResult;
      
    private CompOperType componentOperation;
    public JMSComponent(){
        super(SOATFCompType.JMS);
        constructComponent();
    }
    public JMSComponent(JMSConfiguration jmsConfiguration) {
        super(SOATFCompType.JMS);
        this.jmsConfiguration = jmsConfiguration;
        
        constructComponent();
    }
    @Override
    protected void constructComponent() {
        
    }

    @Override
    public CompOperResult executeOperation(CompOperType componentOperation) {
        this.componentOperation = componentOperation;
        switch (componentOperation){
            case JMS_READ_ALL_MASSAGES_IN_QUEUE:
        try {
            readAllMessagesInQueue();
        } catch (Exception ex) {
            Logger.getLogger(JMSComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
                break;
            default:
                break;
                
        }
        return this.componentOperationResult;
    }

    private void readAllMessagesInQueue() throws Exception {
         
                 DistribuedQueueBrowser dqb = new DistribuedQueueBrowser("t3://prometheus:7001",
                    "t3://prometheus:11001",
                    "OSBWriteQueue", "weblogic", "Weblogic123");
            dqb.printQueueMessages();
    }
    
    
}
