/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ibm.soatf.flow;

/**
 *
 * @author user
 */
public class FlowExecutionEvent {
    private String operationName;
    private OperationResult operationResult;

    public FlowExecutionEvent(String operationName, OperationResult operationResult) {
        this.operationName = operationName;
        this.operationResult = operationResult;
    }

    public FlowExecutionEvent(String operationName) {
        this(operationName, null);
    }

    public OperationResult getOperationResult() {
        return operationResult;
    }

    public String getOperationName() {
        return operationName;
    }
    
    
}
