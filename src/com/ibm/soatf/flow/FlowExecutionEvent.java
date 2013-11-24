/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ibm.soatf.flow;

import com.ibm.soatf.component.ComponentResult;
import com.ibm.soatf.config.master.Operation;

/**
 *
 * @author user
 */
public class FlowExecutionEvent {
    private Operation operation;
    private ComponentResult componentResult;

    public FlowExecutionEvent(Operation operation, ComponentResult componentResult) {
        this.operation = operation;
        this.componentResult = componentResult;
    }

    public FlowExecutionEvent(Operation operation) {
        this(operation, null);
    }

    public ComponentResult getComponentResult() {
        return componentResult;
    }

    public Operation getOperation() {
        return operation;
    }
    
    
}
