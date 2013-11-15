/*
 * Copyright (C) 2013 user
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
package com.ibm.soatf.mapping;

import com.ibm.soatf.ComponentResult;
import com.ibm.soatf.config._interface.mapping.CustomMappings;
import com.ibm.soatf.tool.ValidateTransferedValues;
import java.io.File;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author user
 */
public class XMLToXMLMapper extends Mapper {
    private static final Logger logger = LogManager.getLogger(XMLToXMLMapper.class.getName());
    
    public XMLToXMLMapper(ComponentResult componentOperationResult, List<CustomMappings.Mapping> mappings) {
        super(componentOperationResult, mappings);
    }
    
    @Override
    public void validate(File src, File dest) {
        logger.debug("comparing " + src.getName() + " and " + dest.getName());
        try {
            boolean result = ValidateTransferedValues.validateElementValuesFromFile(src, dest, this.mappings);
            componentOperationResult.setOverallResultSuccess(result);
            if (result) {
                componentOperationResult.addMsg("JMS message files are equal");
                logger.info("JMS message files are equal");
            } else {
                componentOperationResult.addMsg("JMS message files differ");
                logger.info("JMS message files differ");
            }
        } catch (Exception e) {
            componentOperationResult.setOverallResultSuccess(false);
            componentOperationResult.addMsg("comparison error  " + e.getLocalizedMessage());
            logger.debug("comparison error  " + e.getLocalizedMessage());
        }         
    }
    
}
