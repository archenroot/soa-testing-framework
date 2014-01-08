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
        if (result.isSuccess() == null) {
            l.setBackground(Color.LIGHT_GRAY);
        } else {
            if (result.isSuccess()) {
                l.setBackground(Color.GREEN);
            } else {
                l.setBackground(Color.RED);
            }
        }
        //Return the JLabel which renders the cell.
        return l;
    }
}
