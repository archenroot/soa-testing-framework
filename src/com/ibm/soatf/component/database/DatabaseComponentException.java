 /*
 * Copyright (C) 2013 Ladislav Jech
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

package com.ibm.soatf.component.database;

import com.ibm.soatf.flow.FrameworkExecutionException;


/**
 * Database component exception class is thrown within database component
 * operation execution. Every checked and some unchecked exceptions are wrapped
 * by this class.
 *
 * @author Ladislav Jech <archenroot@gmail.com>
 */
public class DatabaseComponentException  extends FrameworkExecutionException {

    /**
     * General constructor.
     */
    public DatabaseComponentException() {
        super();
    }

    /**
     * Exception constructor which passes error message.
     *
     * @param message Exception description.
     */
    public DatabaseComponentException(final String message) {
        super(message);
    }

    /**
     * Exception constructor which passes error cause.
     *
     * @param cause Exception cause.
     */
    public DatabaseComponentException(final Throwable cause) {
        super(cause);
    }

    /**
     * Exception constructor which passes both error description and cause as
     * well.
     *
     * @param message Exception cause.
     * @param cause Exception cause.
     */
    public DatabaseComponentException(
            final String message,
            final Throwable cause) {
        super(message, cause);
    }
}
