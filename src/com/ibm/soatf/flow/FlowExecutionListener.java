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
public interface FlowExecutionListener {
    public void operationStarted(FlowExecutionEvent evt);
    
    public void operationFinished(FlowExecutionEvent evt);
}
