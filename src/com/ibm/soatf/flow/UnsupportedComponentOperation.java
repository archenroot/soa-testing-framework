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

import com.ibm.soatf.flow.FrameworkExecutionException;

/**
 * Exception raised when framework is asked to execute operation which is not
 * currently supported.
 *
 * @author Ladislav Jech <archenroot@gmail.com>
 */
public class UnsupportedComponentOperation extends FrameworkExecutionException {

    /**
     *
     */
    public UnsupportedComponentOperation() {
    }

    /**
     *
     * @param message
     */
    public UnsupportedComponentOperation(String message) {
        super(message);
    }

    /**
     *
     * @param cause
     */
    public UnsupportedComponentOperation(Throwable cause) {
        super(cause);
    }

    /**
     *
     * @param message
     * @param cause
     */
    public UnsupportedComponentOperation(String message, Throwable cause) {
        super(message, cause);
    }
}
