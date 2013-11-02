/*
 * Copyright (C) 2013 Ladislav Jech <archenroot@gmail.com>
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
package ibm.soatest;

import static ibm.soatest.CompOperType.OSB_DISABLE_SERVICE;
import static ibm.soatest.CompOperType.OSB_ENABLE_SERVICE;
import static ibm.soatest.SOATFCompType.OSB;
import ibm.soatest.osb.OSBComponent;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ladislav Jech <archenroot@gmail.com>
 */
public class TestOSBComponent {

    public static void testOSBComponent() {
        CompOperResult cor = new CompOperResult();
        boolean overallResult = false;
        OSBComponent osbComponent = (OSBComponent) SOATFCompFactory.buildSOATFComponent(OSB, "test_service", cor);
        //OSBComponent osbComponent2 = (OSBComponent) SOATFCompFactory.buildSOATFComponent(OSB,"test_service2");
        osbComponent.executeOperation(OSB_DISABLE_SERVICE);
        overallResult = osbComponent.componentOperationResult.isOverallResultSuccess();
        try {
            Thread.sleep(40000);
        } catch (InterruptedException ex) {
            Logger.getLogger(TestOSBComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
        osbComponent.executeOperation(OSB_ENABLE_SERVICE);
    }
}
