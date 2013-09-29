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

package org.archenroot.fw.soatest;

import static org.archenroot.fw.soatest.SoaTestingFrameworkComponentType.*;
import static org.archenroot.fw.soatest.SoaTestingFrameworkComponentType.ComponentOperation.*;
import org.archenroot.fw.soatest.database.*;


/**
 *
 * @author Ladislav Jech <archenroot@gmail.com>
 */
public class TestDatabaseComponent {
    
    public static void testDatabaseComponent(){
        DatabaseComponent databaseComponent = 
                (DatabaseComponent) SoaTestingFrameworkComponentFactory.buildSoaTestingFrameworkComponent(DATABASE);
        databaseComponent.executeOperation(GENERATE_INSERT_DYNAMICALLY_ONE_ROW);
        databaseComponent.executeOperation(EXECUTE_INSERT_FROM_FILE);
        
    }
    
}
