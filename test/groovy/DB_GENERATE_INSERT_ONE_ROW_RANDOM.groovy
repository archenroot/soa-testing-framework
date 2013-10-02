this.getClass().classLoader.rootLoader.addURL(new URL("file:///c:/Dev/proj/SOATestingFramework/soa-testing-framework/dist/SOATestingFramework.jar"));
//import static com.ibm.fm.soatest.SoaTestingFrameworkComponentType.DATABASE;
//import static com.ibm.fm.soatest.SoaTestingFrameworkComponentType.ComponentOperation.DB_GENERATE_INSERT_ONE_ROW_RANDOM;
//import com.ibm.fm.soatest.SoaTestingFrameworkComponentFactory;
//import com.ibm.fm.soatest.database.DatabaseComponent;

def SoaFactory = Class.forName("com.ibm.fm.soatest.SoaTestingFrameworkComponentFactory");
def databaseComponent = Class.forName("com.ibm.fm.soatest.database.DatabaseComponent").newInstance();
databaseComponent =  SoaFactory.buildSoaTestingFrameworkComponent(DATABASE);
//DatabaseComponent databaseComponent =  (DatabaseComponent) SoaTestingFrameworkComponentFactory.buildSoaTestingFrameworkComponent(DATABASE);
//databaseComponent.executeOperation(DB_GENERATE_INSERT_ONE_ROW_RANDOM);