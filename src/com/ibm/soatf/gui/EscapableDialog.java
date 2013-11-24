/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ibm.soatf.gui;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;

/**
 *
 * @author user
 */
public class EscapableDialog extends JDialog {

    public EscapableDialog(Frame owner, boolean modal) {
        super(owner, modal);
        registerEscape();
    }
    
    public EscapableDialog(Dialog owner, boolean modal) {
        super(owner, modal);
        registerEscape();
    }
    
    private void registerEscape() {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escapePressed");
        getRootPane().getActionMap().put("escapePressed", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                closeWindow();
            }
        });
    }
    
    protected void closeWindow() {
        dispose();
    }
}
