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


package com.ibm.soatf.component;

import com.ibm.soatf.config.master.Operation;
import com.ibm.soatf.tool.UniqueIdGenerator;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author zANGETSu
 */
public final class ComponentResult {

    Logger logger = LogManager.getLogger(ComponentResult.class.getName());

    private String resultId;

    private boolean overallResultSuccess;
    private String resultMessage;
    
    private SOATFCompType soaTFCompType;
    private Operation operation;
    
    private List<String> messages = new ArrayList<>();
    private String interfaceName;
    
    public ComponentResult() {
        this(false, null, null, null);
    }
    
    private ComponentResult(boolean overallResultSuccess, String resultMessage, SOATFCompType soaTFCompType, Operation operation) {
        setResultId(UniqueIdGenerator.generateUniqueId());
        setSoaTFCompType(soaTFCompType);
        setOperation(operation);
        setOverallResultSuccess(overallResultSuccess);
        setResultMessage(resultMessage);
    }
    
    
    public String getResultId() {
        return resultId;
    }

    private void setResultId(String resultId) {
        this.resultId = resultId;
    }
    
    public boolean isOverallResultSuccess() {
        return overallResultSuccess;
    }

    public void setOverallResultSuccess(boolean overallResultSuccess) {
        this.overallResultSuccess = overallResultSuccess;
    }
    
    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
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
    
    public String getInterfaceName(){
        return this.interfaceName;
    }
    public void setInterfacename(String interfaceName){
        this.interfaceName = interfaceName;
    }
    @Override
    public String toString() {
        return "CompOperResult:\n"
                + "resultId=" + resultId + "\n"
                + "overallResultSuccess=" + overallResultSuccess + "\n"
                + "resultMessage=" + resultMessage + "\n"
                + "soaTFCompType=" + soaTFCompType + "\n"
                + "compOperType=" + operation + "\n"
                + "messages=" + messages;
    }

    void merge(ComponentResult cor) {
        this.messages.addAll(cor.getMessages());
        this.overallResultSuccess = cor.isOverallResultSuccess();
    }   
    
}
