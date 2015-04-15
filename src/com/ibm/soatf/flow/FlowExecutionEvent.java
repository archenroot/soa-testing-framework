/*
 * Copyright (C) 2013 zANGETSu
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.ibm.soatf.flow;

/**
 * Class representing any event within framework execution related to single
 * operation and keeping track of execution result.
 *
 * @author Ladislav Jech <archenroot@gmail.com>
 */
public class FlowExecutionEvent {

    private String operationName;
    private boolean prePostOperation;
    private OperationResult operationResult;

    /**
     * General constructor.
     *
     * @param operationName framework component operation name
     * @param prePostOperation
     * @param operationResult framework component operation execution result
     * instance
     */
    public FlowExecutionEvent(String operationName, boolean prePostOperation, OperationResult operationResult) {
        this.operationName = operationName;
        this.operationResult = operationResult;
        this.prePostOperation = prePostOperation;
    }

    /**
     * Constructor for simple scenario.
     *
     * @param operationName framework component operation name
     * @param prePostOperation
     */
    public FlowExecutionEvent(String operationName, boolean prePostOperation) {
        this(operationName, prePostOperation, null);
    }

    /**
     * Gets component operation result
     *
     * @return current operation result instance
     */
    public OperationResult getOperationResult() {
        return operationResult;
    }

    /**
     * Gets component operation name
     *
     * @return string representing operation name
     */
    public String getOperationName() {
        return operationName;
    }

    /**
     * Gets component operation prePost status
     *
     * @return boolean representing operation pre/post status
     */
    public boolean isPrePostOperation() {
        return prePostOperation;
    }
}
