/**
 * Copyright (c) 2012-2013 Reficio (TM) - Reestablish your software!. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ibm.soatf.component.soap.builder;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Tom Bujok
 * @since 1.0.0
 */
public class SoapOperationFinder {

    private final Binding binding;

    private String operationName;
    private String operationInputName;
    private String operationOutputName;
    private String soapAction;
    private SoapBuilder builder;

    public SoapOperationFinder(SoapBuilder builder, Binding binding) {
        this.binding = binding;
        this.builder = builder;
    }

    public SoapOperationFinder name(String operationName) {
        if (operationName == null) {
            throw new NullPointerException();
        }
        this.operationName = operationName;
        return this;
    }

    public SoapOperationFinder soapAction(String soapAction) {
        if (soapAction == null) {
            throw new NullPointerException();
        }
        this.soapAction = soapAction;
        return this;
    }

    public SoapOperationFinder inputName(String inputName) {
        if (inputName == null) {
            throw new NullPointerException();
        }
        this.operationInputName = inputName;
        return this;
    }

    public SoapOperationFinder outputName(String outputName) {
        if (outputName == null) {
            throw new NullPointerException();
        }
        this.operationOutputName = outputName;
        return this;
    }

    @SuppressWarnings("unchecked")
    public SoapOperationBuilder find() {
        validateInput();
        List<SoapOperationBuilder> found = new ArrayList<SoapOperationBuilder>();
        for (BindingOperation operation : (List<BindingOperation>) binding.getBindingOperations()) {
            boolean condition = true;
            condition &= checkOperationName(operation);
            condition &= checkSoapAction(operation);
            condition &= checkOperationInputName(operation);
            condition &= checkOperationOutputName(operation);
            if(condition) {
                found.add(SoapOperationImpl.create(builder, binding, operation));
                if(found.size() > 1) {
                    throw new SoapBuilderException("Operation not unique - found more than one operation");
                }
            }
        }
        if(found.isEmpty()) {
            throw new SoapBuilderException("Found no operations");
        }
        return found.iterator().next();
    }

    public SoapOperationBuilder find(SoapContext context) {
        SoapOperationBuilder builder = find();
        builder.setContext(context);
        return builder;
    }

    private void validateInput() {
        boolean failed = true;
        failed &= this.operationName == null;
        failed &= this.soapAction == null;
        failed &= this.operationInputName == null;
        failed &= this.operationOutputName == null;
        if(failed) {
            throw new IllegalArgumentException("All finder properties cannot be null");
        }
    }

    private boolean checkOperationName(BindingOperation op) {
        if (this.operationName != null) {
            return this.operationName.equals(op.getOperation().getName());
        }
        return true;
    }

    private boolean checkSoapAction(BindingOperation op) {
        if (this.soapAction != null) {
            return this.soapAction.equals(SoapUtils.getSOAPActionUri(op));
        }
        return true;
    }

    private boolean checkOperationInputName(BindingOperation op) {
        if (this.operationInputName != null) {
            return this.operationInputName.equals(op.getOperation().getInput().getName());
        }
        return true;
    }

    private boolean checkOperationOutputName(BindingOperation op) {
        if (this.operationOutputName != null) {
            return this.operationOutputName.equals(op.getOperation().getOutput().getName());
        }
        return true;
    }


}
