/*
 * Copyright (C) 2013 user
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
package com.ibm.soatf.util;

import java.io.File;

/**
 *
 * @author user
 */
public final class Utils {
    
    /**
     * Construct the file path from the given <code>pathElements</code>
     * 
     * @param pathElements individual parts of the path
     * @return string representation of the path constructed from the <code>pathElements</code>
     */
    public static String getFullFilePathStr(String ...pathElements) {
        if (pathElements == null || pathElements.length == 0) {
            throw new IllegalArgumentException("At least 1 path element must be specified");
        }
        File path = new File(pathElements[0]);
        for (int i = 1; i < pathElements.length; i++) {
            path = new File(path, pathElements[i]);
        }
        return path.getAbsolutePath();
    }
}
