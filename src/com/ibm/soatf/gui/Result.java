/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ibm.soatf.gui;


import com.ibm.soatf.flow.OperationResult.CommonResult;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author user
 */
class Result {
    private String operationName;
    private String message;
    private CommonResult commonResult;
    private List<String> messages = new ArrayList<>();
    
    public static final String RESULT_PASSED = "PASSED";
    public static final String RESULT_FAILED = "FAILED";
    public static final String RESULT_WARNING = "WARNING";
    public static final String RESULT_UNKNOWN = "";

    public Result(String operationName) {
        this.operationName = operationName;
        this.message = "";
        this.commonResult = CommonResult.UNKNOWN;
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

    public CommonResult getCommonResult() {
        return commonResult;
    }

    public void setCommonResult(CommonResult commonResult) {
        this.commonResult = commonResult;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    public String getSuccessStr() {
        switch (commonResult) {
            case SUCCESS: return RESULT_PASSED;
            case FAILURE: return RESULT_FAILED;
            case WARNING: return RESULT_WARNING;
            default: return RESULT_UNKNOWN;
        }
    }
}
