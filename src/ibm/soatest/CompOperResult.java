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
package ibm.soatest;

import ibm.soatest.tool.UniqueIdGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author zANGETSu
 */
public class CompOperResult {

    Logger logger = LogManager.getLogger(CompOperResult.class.getName());

    private String resultId;
    private boolean overallResultSuccess;
    private String resultMessage;
//private List<String> messages = null;

    public CompOperResult() {
        this.resultId = UniqueIdGenerator.generateUniqueId();
    }

    public CompOperResult(boolean overallResult, String resultMessage) {
        this.overallResultSuccess = overallResult;
        this.resultMessage = resultMessage;
    }

   

    public boolean getOverallResult() {
        return this.overallResultSuccess;
    }

    public String getResultId() {
        return this.resultId;
    }
    public String getResultMessage(){
        return this.resultMessage;
    }

    public void setOverallResult(boolean overallResultSuccess) {
        this.overallResultSuccess = overallResultSuccess;
    }
    public void setResultMessage(String resultMessage){
        this.resultMessage = resultMessage;
    }

}
