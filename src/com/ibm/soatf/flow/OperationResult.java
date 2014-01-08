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

import com.ibm.soatf.component.SOATFCompType;
import com.ibm.soatf.config.master.Operation;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author zANGETSu
 */
public final class OperationResult {

    private static final Logger logger = LogManager.getLogger(OperationResult.class.getName());
    private static OperationResult instance;

    private boolean success = false;
    
    private SOATFCompType soaTFCompType;
    private Operation operation;
    
    private final List<String> messages = new ArrayList<>();
    
    private OperationResult() {
        this(null, null);
    }
    
    private OperationResult(SOATFCompType soaTFCompType, Operation operation) {
        setSoaTFCompType(soaTFCompType);
        setOperation(operation);
    }
    
    public boolean isSuccessful() {
        return success;
    }

    public void markSuccessful() {
        this.success = true;
    }

    public SOATFCompType getSoaTFCompType() {
        return soaTFCompType;
    }

    public void setSoaTFCompType(SOATFCompType soaTFCompType) {
        this.soaTFCompType = soaTFCompType;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }
    
    public void addMsg(String msg) {
        Exception exception = new Exception();
        StackTraceElement[] stackTrace = exception.getStackTrace();
        String className = stackTrace[1].getClassName();
        int idx = className.lastIndexOf(".");
        className = idx != -1 ? className.substring(idx +1) : className;
        messages.add("[" + className + "] " + msg);
    }
    
    public List<String> getMessages() {
        return messages;
    }
    
    @Override
    public String toString() {
        return "CompOperResult:\n"
                + "success=" + success + "\n"
                + "soaTFCompType=" + soaTFCompType + "\n"
                + "compOperType=" + operation + "\n"
                + "messages=" + messages;
    }
    
    public static OperationResult getInstance() {
        if (instance == null) {
            instance = new OperationResult();
        }
        return instance;
    }
    
    static void reset() {
        instance = new OperationResult();
    }
    
}
