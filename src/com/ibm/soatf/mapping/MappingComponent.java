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
package com.ibm.soatf.mapping;

import com.ibm.soatf.ComponentResult;
import com.ibm.soatf.SOATFComponent;
import com.ibm.soatf.SOATFCompType;
import com.ibm.soatf.CompOperType;
import com.ibm.soatf.SOATFCompFactory;
import com.ibm.soatf.UnsupportedComponentOperationException;
import com.ibm.soatf.config._interface.mapping.CustomMappings.Mapping;
import com.ibm.soatf.config._interface.mapping.MappingConfiguration;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author zANGETSu
 */
public class MappingComponent extends SOATFComponent {
    
    private static final Logger logger = LogManager.getLogger(MappingComponent.class.getName());
    
    public static Set<CompOperType> supportedOperations = CompOperType.MAPPING_OPERATIONS;   
    
    private final MappingConfiguration mappingConfiguration;
    
    private List<Mapping> customMappings;
    private SOATFComponent fromComponent;
    private SOATFComponent toComponent;
    private SOATFCompType fromType;
    private SOATFCompType toType;
    
    public MappingComponent(MappingConfiguration mappingConfiguration, ComponentResult componentOperationResult, String identificator) {
        super(SOATFCompType.JMS, componentOperationResult, identificator);
        this.mappingConfiguration = mappingConfiguration;
        
        constructComponent();
    }
    @Override
    protected void constructComponent() {
        /*
        fromType = SOATFCompType.valueOf(mappingConfiguration.getFromComponent().getComponentType().name());
        fromComponent = SOATFCompFactory.buildSOATFComponent(fromType, mappingConfiguration.getFromComponent().getIdentificator(), this.componentOperationResult);
        toType = SOATFCompType.valueOf(mappingConfiguration.getToComponent().getComponentType().name());
        toComponent = SOATFCompFactory.buildSOATFComponent(toType, mappingConfiguration.getToComponent().getIdentificator(), this.componentOperationResult);
        if (mappingConfiguration.getCustomMappings() != null) {
            customMappings = mappingConfiguration.getCustomMappings().getMapping();
        } 
                */
    }

    @Override
    public void executeOperation(CompOperType componentOperation) {
        this.getComponentOperationResult().setCompOperType(componentOperation);
        try {        
            if (supportedOperations.contains(componentOperation)) {
                    throw new com.ibm.soatf.UnsupportedComponentOperationException();
            }            
            switch (componentOperation){
                case MAPPING_VALIDATE_SCENARIO:
                        validateScenario();
                    break;
                default:
                    throw new UnsupportedComponentOperationException();
            }
        } catch (UnsupportedComponentOperationException ex) {
            logger.fatal("Component operation is not supported." + ex);
            this.getComponentOperationResult().addMsg("Component operation is not supported.");
            this.getComponentOperationResult().setOverallResultSuccess(false);            
        } catch (Exception ex) {
            logger.fatal(ex);
            this.getComponentOperationResult().setOverallResultSuccess(false); 
        }
    }

    private void validateScenario() throws Exception {
        Mapper mapper;
       /*
        if (fromType == SOATFCompType.DATABASE && toType == SOATFCompType.JMS) {
            mapper = new DBToXMLMapper(this.getComponentOperationResult(), customMappings);
        } else if (fromType == SOATFCompType.JMS && toType == SOATFCompType.DATABASE) {
            mapper = new XMLToDBMapper(this.getComponentOperationResult(), customMappings);
        } else if (fromType == SOATFCompType.JMS && toType == SOATFCompType.JMS) {
            mapper = new XMLToXMLMapper(this.getComponentOperationResult(), customMappings);
        } else {
            this.componentOperationResult.setOverallResultSuccess(false);
            this.componentOperationResult.addMsg("Cannot find proper mapper for this scenario.");
            logger.info("Cannot find proper mapper for this scenario.");
            return;
        }
               
        Iterator<File> sourceFilesIterator = ((IMappingEndpoint)fromComponent).getGeneratedFiles(mappingConfiguration.getFromComponent().getObject());
        int srcCount = 0;
        int destCount = 0;
        while(sourceFilesIterator.hasNext()) {
            ++srcCount;
            File src = sourceFilesIterator.next();
            Iterator<File> destinationFilesIterator = ((IMappingEndpoint)toComponent).getGeneratedFiles(mappingConfiguration.getToComponent().getObject());
            while(destinationFilesIterator.hasNext()) {
                ++destCount;
                mapper.validate(src, destinationFilesIterator.next());
                if (!this.getComponentOperationResult().isOverallResultSuccess()) {
                    return;
                }
            }
        }
        this.getComponentOperationResult().setOverallResultSuccess(true);
        this.getComponentOperationResult().addMsg("Mapper succesfully compared "+srcCount+" source object[s] against "+destCount+" destination object[s].");
        logger.debug("Mapper succesfully compared "+srcCount+" source object[s] against "+destCount+" destination object[s].");
               */
    }
    
    
}
