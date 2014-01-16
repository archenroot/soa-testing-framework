package com.ibm.soatf.gui;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class ResultColumnRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {

        //Cells are by default rendered as a JLabel.
        JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
        TableModelResults model = (TableModelResults) table.getModel();
        Result result = model.getRow(row);
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
        //Return the JLabel which renders the cell.
        return l;
    }
}
