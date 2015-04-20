package com.ibm.soatf.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class ResultColumnRenderer extends DefaultTableCellRenderer {
    private boolean colorizeBackground;
    
    public ResultColumnRenderer() {
        this.colorizeBackground = false;
    }
    
    public ResultColumnRenderer(boolean colorizeBackground) {
        this.colorizeBackground = colorizeBackground;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {

        //Cells are by default rendered as a JLabel.
        JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
        TableModelResults model = (TableModelResults) table.getModel();
        Result result = model.getRow(row);
        if (result.isPrePostOperation()) {
            l.setForeground(Color.DARK_GRAY);
            Font newLabelFont=new Font(l.getFont().getName(),Font.ITALIC,l.getFont().getSize());  
            l.setFont(newLabelFont); 
        } else {
            l.setForeground(Color.BLACK);
            Font newLabelFont=new Font(l.getFont().getName(),Font.PLAIN,l.getFont().getSize());  
            l.setFont(newLabelFont); 
        }
        if (result.isInfoRow()) {
            l.setBackground(new Color(25,134,255));
        } else if (colorizeBackground) {
            switch (result.getCommonResult()) {
                case SUCCESS: 
                    l.setBackground(Color.GREEN);
                    break;
                case FAILURE:
                    l.setBackground(Color.RED);
                    break;
                case WARNING:
                    l.setBackground(Color.YELLOW);
                    break;
                default:
                    l.setBackground(Color.LIGHT_GRAY);
            }
        } else {
            l.setBackground(Color.WHITE);
        }
        //Return the JLabel which renders the cell.
        return l;
    }
}
