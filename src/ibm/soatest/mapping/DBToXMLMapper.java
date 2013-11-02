
package ibm.soatest.mapping;

import ibm.soatest.CompOperResult;
import ibm.soatest.config.CustomMappings.Mapping;
import ibm.soatest.tool.ValidateTransferedValues;
import java.io.File;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author zANGETSu
 */
public class DBToXMLMapper extends Mapper {
    private static final Logger logger = LogManager.getLogger(DBToXMLMapper.class.getName());
    
    public DBToXMLMapper(CompOperResult componentOperationResult, List<Mapping> mappings){
        super(componentOperationResult, mappings);
    }

    @Override
    public void validate(File src, File dest) {
        logger.debug("comparing " + src.getName() + " and " + dest.getName());
        try {
            boolean result = ValidateTransferedValues.validateValuesFromFile(src, dest, this.mappings);
            componentOperationResult.setOverallResultSuccess(result);
            if (result) {
                componentOperationResult.addMsg("DB statement file equals JMS message file");
                logger.info("DB statement file equals JMS message file");
            } else {
                componentOperationResult.addMsg("DB statement file and JMS message file differ");
                logger.info("DB statement file and JMS message file differ");
            }
        } catch (Exception e) {
            componentOperationResult.setOverallResultSuccess(false);
            componentOperationResult.addMsg("comparison error  " + e.getLocalizedMessage());
            logger.error("comparison error  " + e.getLocalizedMessage());
        }
    }
    
}
