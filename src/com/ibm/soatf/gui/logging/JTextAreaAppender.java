/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ibm.soatf.gui.logging;

import java.io.Serializable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

/**
 *
 * @author user
 */
@Plugin(name = "JTextArea", category = "Core", elementType = "appender", printObject = true)
public class JTextAreaAppender extends AbstractAppender {

    private static JTextArea jTextArea = null;
    
    private JTextAreaAppender(final String name, final Layout<? extends Serializable> layout, final Filter filter) {
        super(name, filter, layout, true); 
    }
    
    public static void setJTextArea(JTextArea jTextArea) {
        JTextAreaAppender.jTextArea = jTextArea;
    }
 
    @PluginFactory
    public static JTextAreaAppender createAppender(@PluginAttribute("name") String name,
                                              @PluginElement("Layout") Layout layout,
                                              @PluginElement("Filters") Filter filter) {
 
        if (name == null) {
            LOGGER.error("No name provided for JTextAreaAppender");
            return null;
        }
        if (layout == null) {
            layout = PatternLayout.createLayout(null, null, null, null, null);
        }
        return new JTextAreaAppender(name, layout, filter);
    }

    @Override
    public void append(LogEvent event) {
        if(jTextArea == null) {
            LOGGER.warn("No JTextArea was set for the JTextAreaAppender. Please use the static setter JTextAreaAppender.setJTextArea()");
            jTextArea = new JTextArea();
            return;
        }
        final String msg = new String(getLayout().toByteArray(event));
        
        // Append formatted message to textarea using the Swing Thread.
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                jTextArea.append(msg);
            }
        });
    }
}
