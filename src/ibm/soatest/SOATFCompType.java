package ibm.soatest;

import ibm.soatest.database.DatabaseComponent;
import java.util.EnumSet;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public enum SOATFCompType {
       
    DATABASE("Database Testing Framework Type Domain"),
    FILE("File Testing Framework Type Domain"),
    FTP("FTP Testing Framework Type Domain"),
    JMS("JMS Testing Framework Type Domain"),
    OSB("OSB Testing Framework Type Domain"),
    REST("REST Testing Framework Type Domain"),
    SOAP("SOAP Testing Framework Type Domain"),
    TOOL("TOOL Testing Framework Type Domain"),
    XML("XML Testing Framework Type Domain"),
    MAPPING("Mapping Testing Framework Type Domain");

    private static final Logger logger = LogManager.getLogger(DatabaseComponent.class.getName());
    
    private final String displayName;

    SOATFCompType(final String displayDescription) {
        this.displayName = displayDescription;
    }

    public String getDisplayName() {
        return displayName;
    }

    
}
