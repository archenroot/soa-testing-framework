/*
 * Copyright (C) 2014 user
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

import java.io.File;
import java.io.IOException;

/**
 *
 * @author user
 */
public class Test {
    public static void main(String[] args) throws Exception {
        //execute(new File("C:\\Apps\\Notepad++\\notepad++.exe"), new File("C:\\WORK\\Irish Water\\projects\\trunk\\soa_test\\IW.600.EBS_M_PCM_SupplierDataVendors\\FlowPattern_-_DatabaseToQueueToQueueToDatabase\\TestScenarioForVendorSitesOfTypeEBSPCM002\\PositiveScenario1SourceToDestination\\db\\eis_ebs_DatabaseAdapter_XXXIW_MW_PO_VENDOR_SITES_EXT_insert.sql"));
        int a = 0x1E;
        int b = 0xE2;
        int c = a & ~b;
        System.out.println(Integer.toBinaryString(a));
        System.out.println(Integer.toBinaryString(~b));
        System.out.println(Integer.toBinaryString(c));
        System.out.println(c);
    }
    
    private static void execute(File executableFile, File file) {
        if (executableFile == null || !executableFile.exists() || file == null || !file.exists()) {
            return;
        }
        String shortExecutable = com.ibm.soatf.tool.Utils.getOSSafeParentPath(executableFile);
        String shortFile = com.ibm.soatf.tool.Utils.getOSSafeParentPath(file);
        try {
            if (!com.ibm.soatf.tool.Utils.isEmpty(shortExecutable) && !com.ibm.soatf.tool.Utils.isEmpty(shortFile)) {
                String command = "cmd /c start \"" + shortExecutable + "\" \"" + shortFile + "\"";
                //logger.trace("Running cmdline: " + command);
                Runtime.getRuntime().exec(command);
            }
        } catch (IOException e) {
            //logger.warn("Could not launch the associated application '" + executableFile.getAbsolutePath() + "' for the file '" + file.getAbsolutePath(), e);
        }
    }
}
