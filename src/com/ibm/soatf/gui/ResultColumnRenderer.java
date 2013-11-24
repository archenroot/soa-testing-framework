package com.ibm.soatf.gui;

import static com.ibm.soatf.gui.TableModelResults.RESULT_FAILED;
import static com.ibm.soatf.gui.TableModelResults.RESULT_PASSED;
import static com.ibm.soatf.gui.TableModelResults.RESULT_UNKNOWN;
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

        //Get the status for the current row.
        TableModelResults tableModel = (TableModelResults) table.getModel();
        
        switch (tableModel.getSuccessLabel(row)){
            case RESULT_PASSED:
                l.setBackground(Color.GREEN);
                break;
            case RESULT_FAILED:
                l.setBackground(Color.RED);
                break;
            case RESULT_UNKNOWN:
                l.setBackground(Color.LIGHT_GRAY);
                break;
        }
        //Return the JLabel which renders the cell.
        return l;
    }
}
