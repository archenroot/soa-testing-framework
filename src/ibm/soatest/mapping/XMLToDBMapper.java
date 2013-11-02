/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ibm.soatest.mapping;

import ibm.soatest.CompOperResult;
import ibm.soatest.config.CustomMappings;
import ibm.soatest.tool.ValidateTransferedValues;
import java.io.File;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author zANGETSu
 */
public class XMLToDBMapper extends Mapper {
    private static final Logger logger = LogManager.getLogger(XMLToDBMapper.class.getName());
    
    public XMLToDBMapper(CompOperResult componentOperationResult, List<CustomMappings.Mapping> mappings) {
        super(componentOperationResult, mappings);
    }
    
    @Override
    public void validate(File src, File dest) {
        logger.debug("comparing " + src.getName() + " and " + dest.getName());
        try {
            boolean result = ValidateTransferedValues.validateValuesFromFile(dest, src, this.mappings);
            componentOperationResult.setOverallResultSuccess(result);
            if (result) {
                componentOperationResult.addMsg("DB statement and JMS message file are equal");
            } else {
                componentOperationResult.addMsg("DB statement and JMS message file differ");
            }
        } catch (Exception e) {
            componentOperationResult.setOverallResultSuccess(false);
            componentOperationResult.addMsg("comparison error  " + e.getLocalizedMessage());
            logger.debug("comparison error  " + e.getLocalizedMessage());
        }        
    }
    
}
