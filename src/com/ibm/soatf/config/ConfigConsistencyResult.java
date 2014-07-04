/*
 * Copyright (C) 2014 user
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

package com.ibm.soatf.config;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author user
 */
public class ConfigConsistencyResult {
    private final List<String> messages = new ArrayList<>();
    private final List<String> masterWarnings = new ArrayList<>();
    private final List<String> masterErrors = new ArrayList<>();
    private final List<String> interfaceWarnings = new ArrayList<>();
    private final List<String> interfaceErrors = new ArrayList<>();
    
    void addMsg(String msg) {
        messages.add(msg);
    }
    
    void addMasterWarning(String msg, Object... args) {
        masterWarnings.add(String.format(msg, args));
    }
    
    void addMasterError(String msg, Object... args) {
        masterErrors.add(String.format(msg, args));
    }
    
    void addInterfaceWarning(String msg, Object... args) {
        interfaceWarnings.add(String.format(msg, args));
    }
    
    void addInterfaceError(String msg, Object... args) {
        interfaceErrors.add(String.format(msg, args));
    }

    public List<String> getMessages() {
        return messages;
    }

    public List<String> getMasterWarnings() {
        return masterWarnings;
    }

    public List<String> getMasterErrors() {
        return masterErrors;
    }

    public List<String> getInterfaceWarnings() {
        return interfaceWarnings;
    }

    public List<String> getInterfaceErrors() {
        return interfaceErrors;
    }

    void addAllMessages(ConfigConsistencyResult tempCcr) {
        getMessages().addAll(tempCcr.getMessages());
        getMasterWarnings().addAll(tempCcr.getMasterWarnings());
        getMasterErrors().addAll(tempCcr.getMasterErrors());
        getInterfaceWarnings().addAll(tempCcr.getInterfaceWarnings());
        getInterfaceErrors().addAll(tempCcr.getInterfaceErrors());
    }
}
