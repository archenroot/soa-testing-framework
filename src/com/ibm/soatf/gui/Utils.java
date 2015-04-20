/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ibm.soatf.gui;

import java.awt.Component;
import java.awt.Dimension;

/**
 *
 * @author Kroky
 */
public class Utils {
    
    public static void centerOnParent(Component parent, Component child) {
        //p = parent
        //c = child
        int pX = parent.getLocation().x;
        int pY = parent.getLocation().y;
        
        Dimension pSize = parent.getSize();
        Dimension cSize = child.getSize();
        
        int cX = pX + (pSize.width - cSize.width) / 2;
        int cY = pY + (pSize.height - cSize.height) / 2;
        child.setLocation(cX, cY);
    }
}
