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

package com.ibm.soatf.gui;

import com.ibm.soatf.component.AbstractSoaTFComponent;
import static com.ibm.soatf.gui.SOATestingFrameworkGUI.operationProgressBar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author user
 */
public class ProgressMonitor {
    private static final Logger logger = LogManager.getLogger(ProgressMonitor.class.getName());
    /**
     * Simple rule: set the maximum to the number of <code>increment</code> calls that will follow after this method + 1
     * for the <code>markDone</code> method which is always called at the end of the {@link AbstractSoaTFComponent}'s
     * method <code>execute(Operation operation)</code>
     * @param max what should be the maximum value of the progress bar
     */
    public static void init(int max) {
        operationProgressBar.setValue(0);
        operationProgressBar.setMinimum(0);
        operationProgressBar.setMaximum(max);
        operationProgressBar.setIndeterminate(false);
    }
    
    /**
     * Simple rule: set the maximum to the number of <code>increment</code> calls that will follow after this method + 1
     * for the <code>markDone</code> method which is always called at the end of the {@link AbstractSoaTFComponent}'s
     * method <code>execute(Operation operation)</code>
     * @param max what should be the maximum value of the progress bar
     * @param initialMsg what message should be displayed initially
     */
    public static void init(int max, String initialMsg) {
        setMsg(initialMsg);
        init(max);
    }
    
    public static void increment(String msg) {
        increment();
        setMsg(msg);
    }
    
    public static void increment() {
        operationProgressBar.setValue(operationProgressBar.getValue() + 1);
    }
    
    public static void setMsg(String msg) {
        operationProgressBar.setStringPainted(msg != null);
        operationProgressBar.setString(msg);
    }
    
    public static void clear() {
        operationProgressBar.setStringPainted(false);
        operationProgressBar.setValue(0);
    }
    
    public static void setIndeterminate() {
        clear();
        operationProgressBar.setIndeterminate(true);
    }
    
    public static void markDone() {
        operationProgressBar.setIndeterminate(false);
        int maximum = operationProgressBar.getMaximum();
        if(operationProgressBar.getValue() != maximum - 1) {
            logger.trace("Component operation either failed with exception (and you can ignore this message) or there is a mismatch between maximum number of ProgressMonitor events and actual number of calls of the increment methods during operation execution");
        }
        if (maximum < 1) {
            operationProgressBar.setMaximum(1);
            maximum = 1;
        }
        operationProgressBar.setValue(maximum);
        setMsg("Done");
    }
}
