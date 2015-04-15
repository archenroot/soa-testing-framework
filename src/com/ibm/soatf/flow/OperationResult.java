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
import com.ibm.soatf.config.master.ExecBlockOperation;
import com.ibm.soatf.config.master.Operation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * SOA Testing Framework component operation result class. Implementation of
 * singleton pattern able to keep all collection of execution results.
 *
 * @author Ladislav Jech <archenroot@gmail.com>
 */
public final class OperationResult {

    private static final Logger logger = LogManager.getLogger(OperationResult.class.getName());
    private static OperationResult instance;
    private static final Map<String, OperationResult> prevInstances = new HashMap<String, OperationResult>();

    private CommonResult commonResult = CommonResult.UNKNOWN;

    private SOATFCompType soaTFCompType;
    private Operation operation;

    //TODO: maybe use composite key ? 
    private String scenarioName;
    private String execBlockName;

    private final List<String> messages = new ArrayList<String>();
    private final List<String> shortMessages = new ArrayList<String>();

    private OperationResult() {
        this(null, null);
    }

    private OperationResult(
            final SOATFCompType soaTFCompType,
            final Operation operation) {
        setSoaTFCompType(soaTFCompType);
        setOperation(operation);
    }

    /**
     * Gets successful indicator.
     *
     * @return true if success, otherwise false
     */
    public boolean isSuccessful() {
        return commonResult  == CommonResult.SUCCESS;
    }

    /**
     * Gets failure indicator.
     *
     * @return true if failure, otherwise false
     */
    public boolean isFailure() {
        return commonResult.equals(CommonResult.FAILURE);
    }

    /**
     * Gets warning indicator.
     *
     * @return true if warning, otherwise false
     */
    public boolean isWarning() {
        return commonResult.equals(CommonResult.WARNING);
    }

    /**
     * Gets unknown indicator.
     *
     * @return true if unknown, otherwise false
     */
    public boolean isUnknown() {
        return commonResult.equals(CommonResult.UNKNOWN);
    }

    /**
     * Gets current common result instance, which is 4 logic object paired with
     * every instance of component operation result.
     *
     * @return 4 logic common result object
     */
    public CommonResult getCommmonResult() {
        return commonResult;
    }

    /**
     * Sets current common result instance, which is 4 logic object paired with
     * every instance of component operation result.
     *
     * @param commonResult 4 logic common result object
     */
    public void setCommmonResult(final CommonResult commonResult) {
        this.commonResult = commonResult;
    }

    /**
     * Marks result as success.
     */
    public void markSuccessful() {
        this.commonResult = CommonResult.SUCCESS;
    }

    /**
     * Marks result as warning.
     */
    public void markWarning() {
        this.commonResult = CommonResult.WARNING;
    }

    /**
     * Marks result as unknown.
     */
    public void markUnknown() {
        this.commonResult = CommonResult.UNKNOWN;
    }

    /**
     * Marks result as failure.
     */
    public void markFailure() {
        this.commonResult = CommonResult.FAILURE;
    }

    /**
     * Gets SOA Testing Framework component type related current component
     * operation result instance.
     *
     * @return SOATFCompType
     */
    public SOATFCompType getSoaTFCompType() {
        return soaTFCompType;
    }

    /**
     * Sets SOA Testing Framework component type related current component
     * operation result instance.
     *
     * @param soaTFCompType SOA Testing Framework component enumeration type
     * object
     */
    public void setSoaTFCompType(final SOATFCompType soaTFCompType) {
        this.soaTFCompType = soaTFCompType;
    }

    /**
     * Gets result's component operation instance.
     *
     * @return operation executed to which result is related
     */
    public Operation getOperation() {
        return operation;
    }

    /**
     * Sets result's component operation.
     *
     * @param operation component operation object
     */
    public void setOperation(final Operation operation) {
        this.operation = operation;
    }

    /**
     * Gets scenario name.
     *
     * @return test scenario name
     */
    public String getScenarioName() {
        return scenarioName;
    }

    /**
     * Sets scenario name.
     *
     * @param scenarioName scenario name
     */
    public void setScenarioName(final String scenarioName) {
        this.scenarioName = scenarioName;
    }

    /**
     * Gets current execution block name.
     *
     * @return execution block name
     */
    public String getExecBlockName() {
        return execBlockName;
    }

    /**
     * Sets current execution block name.
     *
     * @param blockName
     */
    public void setExecBlockName(final String blockName) {
        this.execBlockName = blockName;
    }

    /**
     * Adds another message to current result object.
     *
     * @param msg can be any String message describing point in time which
     * object survived
     */
    public void addMsg(final String msg) {
        if (msg != null) {
            Exception exception = new Exception();
            StackTraceElement[] stackTrace = exception.getStackTrace();
            String className = stackTrace[1].getClassName();
            int idx = className.lastIndexOf(".");
            className = idx != -1 ? className.substring(idx + 1) : className;
            messages.add("[" + className + "] " + msg);
            shortMessages.add(msg);
        }
    }
    
    /**
     * Adds another message to current result object.
     *
     * @param msg can be any String message describing point in time which
     * object survived
     * @param shortMsg is shorter message for the purposes of report
     */
    public void addMsg(final String msg, final String shortMsg) {
        if (msg != null) {
            Exception exception = new Exception();
            StackTraceElement[] stackTrace = exception.getStackTrace();
            String className = stackTrace[1].getClassName();
            int idx = className.lastIndexOf(".");
            className = idx != -1 ? className.substring(idx + 1) : className;
            messages.add("[" + className + "] " + msg);
        }
        if (shortMsg != null) {
            shortMessages.add(shortMsg);
        }
    }
    
    /**
     * Adds another message to current result object.
     *
     * @param format format of the both messages
     * @param msg can be any String message describing point in time which
     * object survived
     * @param shortMsg is shorter message for the purposes of report
     */
    public void addMsg(final String format, final String msg, final String shortMsg) {
        if (format != null) {
            Exception exception = new Exception();
            StackTraceElement[] stackTrace = exception.getStackTrace();
            String className = stackTrace[1].getClassName();
            int idx = className.lastIndexOf(".");
            className = idx != -1 ? className.substring(idx + 1) : className;
            messages.add(String.format("[" + className + "] " + format, msg));
            shortMessages.add(String.format(format, shortMsg != null?shortMsg:msg));
        }
    }    

    /**
     * Gets collection of object messages.
     *
     * @return collection of all messages related to current object
     */
    public List<String> getMessages() {
        return messages;
    }

    @Override
    public String toString() {
        return "CompOperResult:\n"
                + "success=" + commonResult + "\n"
                + "soaTFCompType=" + soaTFCompType + "\n"
                + "compOperType=" + operation + "\n"
                + "messages=" + messages;
    }

    /**
     * Singleton pattern applied on this object creates or returns existing
     * instance of class.
     *
     * @return OperationResult instance
     */
    public static OperationResult getInstance() {
        if (instance == null) {
            instance = new OperationResult();

        }
        return instance;
    }

    /**
     * Put current instance in collection of previous instances and creates
     * clean new one.
     */
    public static void nextInstance() {
        if (instance != null && instance.getOperation() != null && instance.getOperation() instanceof ExecBlockOperation) {
            prevInstances.put(instance.getSearchKey(), instance);
        }
        instance = new OperationResult();
    }

    /**
     * Resets whole singleton by creating new instance and clearing all existing
     * previous instances within collection.
     */
    public static void reset() {
        prevInstances.clear();
        instance = new OperationResult();
    }

    /**
     * UNKNOWN - TODO - PROBABLY DEPRECATED, I can see duplication of
     * shortMessages in relation to messages field.
     *
     * @return formated short messages
     */
    @Deprecated
    public String getShortMessages() {
        StringBuilder sb = new StringBuilder();
        String delim = "";
        for (String s : shortMessages) {
            sb.append(delim);
            sb.append(s);
            delim = "\n";
        }
        return sb.toString();
    }

    /**
     * Gets unique key used to search within collection of previous result
     * instances.
     *
     * @return current search key
     */
    public String getSearchKey() {
        return scenarioName + "_~_" + execBlockName + "_~_" + operation.getName() + "_~_" + operation.getExecuteOn().name();
    }

    /**
     *
     * @param scenarioName
     * @param blockName
     * @param operationName
     * @param executedOn
     * @return
     */
    public static String searchResultShortMessages(String scenarioName, String blockName, String operationName, String executedOn) {
        OperationResult found = findResult(scenarioName, blockName, operationName, executedOn);
        if (found == null) {
            //return scenarioName + "_~_" + execBlockName + "_~_" + operationName + "_~_" + executedOn;
            return null;
        }
        return found.getShortMessages();
    }

    /**
     *
     * @param scenarioName
     * @param blockName
     * @param operationName
     * @param executedOn
     * @return
     */
    public static String searchCommonResult(String scenarioName, String blockName, String operationName, String executedOn) {
        OperationResult found = findResult(scenarioName, blockName, operationName, executedOn);
        if (found == null) {
            //return scenarioName + "_~_" + execBlockName + "_~_" + operationName + "_~_" + executedOn;
            return null;
        }
        return found.getCommmonResult().name();
    }

    private static OperationResult findResult(String scenarioName, String blockName, String operationName, String executedOn) {
        if (scenarioName == null || blockName == null || operationName == null || executedOn == null) {
            return null;
        }
        String key = scenarioName + "_~_" + blockName + "_~_" + operationName + "_~_" + executedOn.toUpperCase();
        OperationResult found = prevInstances.get(key);
        if (found == null) {
            if (instance != null && instance.getOperation() instanceof ExecBlockOperation && key.equals(instance.getSearchKey())) {
                return instance;
            }
        } else {
            return found;
        }
        return null;
    }

    /**
     * 4 logic common result class.
     *
     * @author Ladislav Jech <archenroot@gmail.com>
     */
    public enum CommonResult {

        /**
         * Success state.
         */
        SUCCESS,
        /**
         * Failure state.
         */
        FAILURE,
        /**
         * Warning state.
         */
        WARNING,
        /**
         * Unknown state.
         */
        UNKNOWN;
    }
}
