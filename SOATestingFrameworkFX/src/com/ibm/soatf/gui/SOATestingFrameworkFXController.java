/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ibm.soatf.gui;

import static com.ibm.soatf.gui.MasterConfigurationUnmarshaller.getInterfaceNames;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

/**
 *
 * @author zANGETSu
 */
public class SOATestingFrameworkFXController implements Initializable {
    
    @FXML
    public TextArea loggerTextArea;

    @FXML
    public ListView<String> interfacesView;

    @FXML
    public ListView<String> flowPatternsView;
    
    @FXML
    private ListView<String> projectsView;
    
    @FXML
    public ListView<String> testScenarioExecutionBlocksView;
    
    @FXML
    public ListView<String> executionBlocksView;
    
    @FXML
    public ComboBox<String> environmentsComboBox;
    
    @FXML
    public Label label;
    
    @FXML
    public Button buttonRunTest;
    
    public TextAreaOutputStream taOutputStream;
    
    public void executeRun(ActionEvent event){
        try {
            
            //loggerTextArea.appendText("asfadsfadsfadsf");
            System.setOut(new PrintStream(taOutputStream));
            String env = environmentsComboBox.getValue();
            String _interface = interfacesView.getSelectionModel().getSelectedItem();
            
            List<String> command = new ArrayList<String>();
            command.add("cmd.exe");
            command.add("/C");
            command.add(System.getenv().get("JAVA_HOME") + "\\bin\\java");
            command.add("-jar");
            command.add("c:\\SOATestingFramework\\dist\\SOATestingFramework.jar");
            
            // execute my command
            SystemCommandExecutor commandExecutor = new SystemCommandExecutor(command);
            
            int result = commandExecutor.executeCommand();
            
            // get the output from the command
            StringBuilder stdout = commandExecutor.getStandardOutputFromCommand();
            StringBuilder stderr = commandExecutor.getStandardErrorFromCommand();
            
            // print the output from the command
            System.out.println("STDOUT");
            System.out.println(stdout);
            //loggerTextArea.appendText(stdout.toString());
            System.out.println("STDERR");
            System.out.println(stderr);
            String s = null;
        } catch (IOException ex) {
            Logger.getLogger(SOATestingFrameworkFXController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(SOATestingFrameworkFXController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    @FXML
    public void handleButtonAction(ActionEvent event) {
        System.out.println("You clicked me!");
        label.setText("Hello World!");
    }
    
    

    public SimpleCompositeKey simpleCompKey = new SimpleCompositeKey();
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
        taOutputStream = new TextAreaOutputStream(loggerTextArea, "shell ");
            ObservableList<String> values =  FXCollections.observableArrayList(getInterfaceNames());
            System.out.println(values.size());
            interfacesView.setItems(FXCollections.observableList(values));
            
            environmentsComboBox.setItems(FXCollections.observableArrayList(MasterConfigurationUnmarshaller.getAllEnvironments()));
            environmentsComboBox.setValue("Select Environment");
       
            
            
            
            //Environment combobox - need to be dynamically updated based on current interface selection !! but just for future
 
   
            // Listener over item selection
            interfacesView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

                @Override
                public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue) {
                    try {
                        simpleCompKey.setInterfaceName(newValue);
                        flowPatternsView.setItems(FXCollections.observableArrayList(MasterConfigurationUnmarshaller.getReferencedFlowPatternNames(newValue)));
                        projectsView.setItems(FXCollections.observableArrayList(MasterConfigurationUnmarshaller.getProjectNames(newValue)));
                        System.out.println(newValue);
                    } catch (FrameworkConfigurationException ex) { Logger.getLogger(SOATestingFrameworkFXController.class.getName()).log(Level.SEVERE, null, ex); }
                }
            });
            

            flowPatternsView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    try {
                        simpleCompKey.setFlowPatternName(newValue);
                        testScenarioExecutionBlocksView.setItems(FXCollections.observableArrayList(MasterConfigurationUnmarshaller.getTestScenariosAndExecutionBlocks(newValue)));
                    } catch (FrameworkConfigurationException ex) { Logger.getLogger(SOATestingFrameworkFXController.class.getName()).log(Level.SEVERE, null, ex); }
                }
            });
            
            testScenarioExecutionBlocksView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>(){

                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    try {
                        simpleCompKey.setTestScenarioName(newValue.substring(0, newValue.indexOf(";")));
                        executionBlocksView.setItems(FXCollections.observableArrayList(MasterConfigurationUnmarshaller.getExecutionBlockNames(simpleCompKey)));
                    } catch (FrameworkConfigurationException ex) {
                        Logger.getLogger(SOATestingFrameworkFXController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            
            });
            
        } catch (FrameworkConfigurationException ex) {
            Logger.getLogger(SOATestingFrameworkFXController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
