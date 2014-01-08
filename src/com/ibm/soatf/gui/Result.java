/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ibm.soatf.gui;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author user
 */
class Result {
    private String operationName;
    private String message;
    private Boolean success;
    private List<String> messages = new ArrayList<>();
    
    public static final String RESULT_PASSED = "PASSED";
    public static final String RESULT_FAILED = "FAILED";
    public static final String RESULT_UNKNOWN = "";

    public Result(String operationName) {
        this.operationName = operationName;
        this.message = "";
        this.success = null;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean isSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    public String getSuccessStr() {
        return isSuccess() == null ? RESULT_UNKNOWN : (isSuccess() ? RESULT_PASSED : RESULT_FAILED);
    }
}
