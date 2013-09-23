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
package org.archenroot.fw.soatest;

import java.io.File;
import java.io.IOException;
import org.archenroot.fw.soatest.database.DatabaseTestComponent;

/**
 *
 * @author zANGETSu
 */
public class Main {
    public static void main(String[] args) throws IOException {
        String path = new File(".").getCanonicalPath().toString() +
                "\\xml-resources\\jaxb\\SOATFConfiguration\\soa-testing-framework-config.xml";
        boolean fileExists = new File(path).exists();
        System.out.println(new File (new File(".").getCanonicalPath().toString() +
                "\\xml-resources\\jaxb\\SOATFConfiguration\\soa-testing-framework-config.xml").exists());
        SOATestingFramework soaTF = new SOATestingFramework("\\xml-resources\\jaxb\\SOATFConfiguration\\soa-testing-framework-config.xml");
        SOATestingFrameworkConfiguration soaTFConfig 
                = new SOATestingFrameworkConfiguration(
                new File(".").getCanonicalPath().toString() +
                "\\xml-resources\\jaxb\\SOATFConfiguration\\soa-testing-framework-config.xml");
        //soaTFConfig.getDatabaseType();
    DatabaseTestComponent dtc = new DatabaseTestComponent(soaTFConfig.getDatabaseType());
    dtc.generateInsertStatement();
    }
}
