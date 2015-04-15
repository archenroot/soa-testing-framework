/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ibm.soatf.gui;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author user
 * @param <T>
 */
class TableModelResults extends AbstractTableModel  {
    
    List<Result> results = new ArrayList<>();
    
    private static final String[] COLUMN_NAMES = new String[] {
        "Operation", "Message", "Result"
    };
    
    private static final Class<?>[] COLUMN_CLASSES = new Class<?>[] {
        String.class, String.class, String.class
    };
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return COLUMN_CLASSES[columnIndex];
    }

    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }
    
    @Override
    public int getRowCount() {
        return results.size();
    }
    
    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Result result = results.get(rowIndex);
        switch(columnIndex) {
            case 0: return result.getOperationName() == null ? "" : result.getOperationName();
            case 1: return buildMessage(result.getMessages());
            case 2: return result.getSuccessStr();
            default: throw new IndexOutOfBoundsException("Invalid column index: " + columnIndex);
        }
    }

    public void addRow(Result result) {
        results.add(result);
        fireTableRowsInserted(getLastRowIdx(), getLastRowIdx());
    }
    
    public void updateLastRow(Result result) {
        //last row
        Result resultToUpdate = results.get(getLastRowIdx());
        resultToUpdate.setMessages(result.getMessages());
        resultToUpdate.setOperationName(result.getOperationName());
        resultToUpdate.setCommonResult(result.getCommonResult());
        resultToUpdate.setPrePostOperation(result.isPrePostOperation());
        fireTableRowsUpdated(getLastRowIdx(), getLastRowIdx());
    }
    
    private int getLastRowIdx() {
        return results.size() - 1;
    }
    
    public void clear() {
        results.clear();
        fireTableDataChanged();
    }

    public Result getRow(int rowIdx) {
        return results.get(rowIdx);
    }

    private String buildMessage(List<String> messages) {
        StringBuilder sb = new StringBuilder();
        for (String msg : messages) {
            sb.append(msg).append("\n");
        }
        return sb.toString();
    }
}
