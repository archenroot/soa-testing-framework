import static org.archenroot.fw.soatest.SoaTestingFrameworkComponentType.ComponentOperation.EXECUTE_INSERT_FROM_FILE;
import static org.archenroot.fw.soatest.SoaTestingFrameworkComponentType.ComponentOperation.GENERATE_INSERT_DYNAMICALLY_ONE_ROW;
import static org.archenroot.fw.soatest.SoaTestingFrameworkComponentType.DATABASE;
import org.archenroot.fw.soatest.database.DatabaseComponent;
import org.archenroot.fw.soatest.SoaTestingFrameworkComponentFactory;

DatabaseComponent databaseComponent =  (DatabaseComponent) SoaTestingFrameworkComponentFactory.buildSoaTestingFrameworkComponent(DATABASE);
databaseComponent.executeOperation(GENERATE_INSERT_DYNAMICALLY_ONE_ROW);
databaseComponent.executeOperation(EXECUTE_INSERT_FROM_FILE);


