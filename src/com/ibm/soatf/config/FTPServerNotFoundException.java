/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ibm.soatf.config;

import com.ibm.soatf.FrameworkException;

/**
 *
 * @author zANGETSu
 */
public class FTPServerNotFoundException extends FrameworkException{
    public FTPServerNotFoundException() {}
        
        public FTPServerNotFoundException(String message) {
            super(message);
        }
        
        public FTPServerNotFoundException(Throwable cause){
            super(cause);
        }
        
        public FTPServerNotFoundException(String message, Throwable cause)
        {
            super(message, cause);
        }
}
