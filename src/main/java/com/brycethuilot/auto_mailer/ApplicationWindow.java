package com.brycethuilot.auto_mailer;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * Main class for Application. Creates all windows and pop ups for asking user what to do with policy
 *
 * @author Bryce Thuilot
 * @version %I%, %G%
 * @since 1.0
 */
public class ApplicationWindow extends Application {

    private File policyFile;
    private GridPane root;
    private Button fileSelect;
    private Text filePath;
    private Text title;
    private Button cancelButton;
    private CheckBox updateInQQ;
    private CheckBox sendToInsured;
    private CheckBox printForMortgage;
    private Stage primaryStage;
    private RadioButton oceanHarbor;
    private RadioButton nBay;

    private static Config config;

    private String password;
    public static String username = "dean@millcreekagency.com";

    private final static int TITLE_ROW = 1;
    private final static int COMPANY_SELECT = 4;
    private final static int CHOOSE_POLICY = 5;
    private final static int OPTION_SELECTION = 8;
    private final static int UPDATE_BUTTON = 9;


    /**
     * Start point of application. Launches GUI
     * @param args commandline arguments
     */
    public static void main(String[] args) {
        try {
            config = new Config();
        }catch (IOException io) {
            System.out.println(io.getLocalizedMessage());
            System.out.println("Cannot read config");
            System.exit(1);
        }

        launch(args);
    }

    /**
     * Reads the setting for the default username from the setting hash created by {@link Config}
     * @param setting the settings hash created by {@link Config}
     */
    static void setSetting(HashMap<String, String> setting) {
        username = setting.get("Default_QQ_Username");
    }

    /**
     * Sets the username and password fields to later be used to login into QQ Catalyst
     * @param username Username of QQ Catalyst
     * @param password Password of QQ Catalyst
     */
    public void login(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Sets up login view, once moved past calls {@link #login(String, String)}
     */
    public void setUpLogin() {
        Button login = new Button("Login");
        Text scenetitle = new Text("Welcome");
        scenetitle.setFont(Font.font("Helvetica Neue", FontWeight.NORMAL, 20));
        root.add(scenetitle, 0, COMPANY_SELECT - 1, 2, 1);

        Label userName = new Label("User Name:");
        root.add(userName, 0, COMPANY_SELECT);

        TextField userTextField = new TextField(username);
        root.add(userTextField, 1, COMPANY_SELECT);

        Label pw = new Label("Password:");
        root.add(pw, 0, COMPANY_SELECT + 1);

        PasswordField pwBox = new PasswordField();
        root.add(pwBox, 1, COMPANY_SELECT + 1);

        login.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                login(userTextField.getText(), pwBox.getText());
                root.getChildren().remove(0,root.getChildren().size());
                setUpUpdater();
            }
        });

        root.add(login, 2, COMPANY_SELECT + 3);

    }

    /**
     * Adds a setting with an input field to the display
     * @param inputs the list of setting names matched with an TextField input
     * @param settingName the name of the setting to add
     * @param value its current value
     * @param row the row to put the setting on
     */
    private void addSetting(HashMap<String, TextField> inputs, String settingName, String value, int row) {
        TextField input = new TextField(value);
        this.addToGrid(root, this.createText(settingName.replace('_', ' '), 16, FontWeight.NORMAL), 0, row);
        this.addToGrid(root, input, 2, row);
        inputs.put(settingName, input);
    }


    /**
     * Creates the view to edit settings
     * @throws IOException if the settings file could not be read
     */
    private void setUpSettings() throws IOException{
        HashMap<String, String> settings = config.readConfig();
        this.addToGrid(root, this.createText("Settings", 20, FontWeight.NORMAL), 1 ,0);

        HashMap<String, TextField> inputs = new HashMap<String, TextField>();

        int i = 2;
        for(String setting : settings.keySet()) {
            this.addSetting(inputs, setting, settings.get(setting), i);
            i++;
        }

        int buttonRow = settings.size() + 2;
        Button exit = new Button("Exit");
        exit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                root.getChildren().remove(0, root.getChildren().size());
                setUpUpdater();
            }
        });
        this.addToGrid(root, exit, 2, buttonRow);

        Button save = new Button("Save");
        save.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                saveSettings(inputs);
            }
        });
        this.addToGrid(root, save, 0, buttonRow);
    }

    /**
     * Saves the settings to the config
     * @param settings the name of the settings mapped to the input
     */
    private void saveSettings(HashMap<String, TextField> settings){
        HashMap<String, String> saveSettings = new HashMap<>();
        for(String setting : settings.keySet()) {
            saveSettings.put(setting, settings.get(setting).getText());
        }

        try {
            config.setConfig(saveSettings);
        }catch (IOException io) {
            errorPopup("Unable to save settings");
        }
    }


    /**
     * Creates the button to get settings page
     */
    private void createSettingsButton() {
        Button settings = new Button("Settings");
        settings.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                root.getChildren().remove(0, root.getChildren().size());
                try {
                    setUpSettings();
                }catch (IOException io) {
                    errorPopup("Unable to open settings file");
                }
            }
        });
        root.add(settings, 2, TITLE_ROW);
    }

    /**
     * Sets up the updater view
     */
    public void setUpUpdater() {
        this.setUpFileSelector();
        this.createTitle();
        this.createSettingsButton();
        this.createCompanySelector(root);
        this.setOptionMenu();
        this.updateButton(this, this.username, this.password);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Stage primaryStage){
        primaryStage.setTitle("Mill Creek Agency Auto Insurance Mailer");

        this.primaryStage = primaryStage;
        this.setUpGridPane();

        if (password == null || username == null) {
            this.setUpLogin();
        }else {
            this.setUpUpdater();
        }

        primaryStage.setScene(new Scene(root, 800,550));
        primaryStage.show();
    }

    /**
     * Sets the policy file once selected from file view
     * @param policyFile the PDF file object
     */
    public void setPolicyFile(File policyFile) {
        this.policyFile = policyFile;
        root.getChildren().remove(this.fileSelect);
        filePath = new Text(policyFile.getName());
        root.add(filePath, 0,CHOOSE_POLICY, 2, 1);
        GridPane.setHalignment(filePath, HPos.CENTER);

        cancelButton = new Button("Cancel");
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                setUpFileSelector();
            }
        });
        root.add(cancelButton, 2,CHOOSE_POLICY);
        GridPane.setHalignment(cancelButton, HPos.CENTER);
    }

    /**
     * Creates the radio buttons for selecting a policy
     * @param root the GridPane view
     */
    public void createCompanySelector(GridPane root) {
        ToggleGroup companies = new ToggleGroup();

        oceanHarbor = new RadioButton("Ocean Harbor");
        oceanHarbor.setToggleGroup(companies);
        oceanHarbor.setSelected(true);

        nBay = new RadioButton("Narragansett Bay");
        nBay.setToggleGroup(companies);

        this.addToGrid(root, this.createText("Select Company", 16, FontWeight.NORMAL), 1, COMPANY_SELECT - 1);
        this.addToGrid(root, oceanHarbor, 0, COMPANY_SELECT);
        this.addToGrid(root, nBay, 2, COMPANY_SELECT);

    }

    /**
     * Popup to say whether or not sending email was successful
     * @param success if the email was successfully sent
     */
    private void sentEmailDialog(boolean success) {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        if (success) {
            grid.add(this.createText("Email successfully sent", 20, FontWeight.NORMAL), 0, 0);
        }else {
            grid.add(this.createText("Email could not be sent (most likely email address is wrong)", 20, FontWeight.NORMAL), 0, 0);
        }
        Button ok = new Button("OK");
        ok.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                dialog.close();
            }
        });
        grid.add(ok, 0, 1);
        Scene dialogScene = new Scene(grid, 600, 400);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    /**
     * Asks the user for the email password to the email in config
     * @param to the address sending to
     * @param policy the policy file
     */
    public void getEmailPassword(String to, Policy policy){
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        PasswordField emailPassword = new PasswordField();
        grid.add(this.createText("Please enter email password for " + Email.FROM, 20, FontWeight.NORMAL), 0, 0);
        grid.add(emailPassword, 0, 1);
        Button ok = new Button("Submit");
        ok.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                dialog.close();
                try {
                    policy.sendEmail(to, emailPassword.getText());
                    sentEmailDialog(true);
                }catch(Exception ex) {
                    sentEmailDialog(false);
                    System.out.println(ex.getMessage());
                }
            }
        });
        grid.add(ok, 0, 2);
        Scene dialogScene = new Scene(grid, 600, 500);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    /**
     * Pop up to ask user if they want to change emails from the one found on QQ
     * @param to The address found on QQ
     * @param policy Policy PDF File object
     */
    public void changeEmail(String to, Policy policy) {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.add(this.createText("The email will be sent to the address below",  16, FontWeight.NORMAL), 1 , 0);
        grid.add(this.createText("If you would like to change the address it is sent to, change the value in the box", 12, FontWeight.NORMAL), 1, 2);
        TextField emailInput = new TextField(to);
        GridPane.setHalignment(emailInput, HPos.CENTER);
        grid.add(emailInput, 0, 3, 3, 1);
        Button ok = new Button("OK");
        ok.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                getEmailPassword(emailInput.getText(), policy);
                dialog.close();
            }
        });
        grid.add(ok, 1, 5);
        GridPane.setHalignment(ok, HPos.CENTER);
        Scene dialogScene = new Scene(grid, 550, 300);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    /**
     * Creates the menu for options: Update in QQ, Send to insured, and print for mortgagee
     */
    public void setOptionMenu(){
        root.add(this.createText("Options", 16, FontWeight.NORMAL), 1, OPTION_SELECTION - 1);

        updateInQQ = new CheckBox("Update in QQ");
        updateInQQ.setSelected(true);
        root.add(updateInQQ, 0, OPTION_SELECTION);
        GridPane.setHalignment(updateInQQ, HPos.CENTER);

        sendToInsured = new CheckBox("Send to Insured");
        sendToInsured.setSelected(true);
        root.add(sendToInsured, 1, OPTION_SELECTION);
        GridPane.setHalignment(sendToInsured, HPos.CENTER);

        printForMortgage = new CheckBox("Print for Mortgagee");
        printForMortgage.setSelected(true);
        root.add(printForMortgage, 2, OPTION_SELECTION);
        GridPane.setHalignment(printForMortgage, HPos.CENTER);
    }

    /**
     * Creates a text object with Helventica Neufont and given attributes
     * @param str The string of the text object
     * @param size the font size
     * @param fontWeight the font weight
     * @return Text object
     */
    private Text createText(String str, int size, FontWeight fontWeight) {
        Text text = new Text(str);
        text.setFont(Font.font("Helventica Neue", fontWeight, size));
        GridPane.setHalignment(text, HPos.CENTER);
        return text;
    }

    /**
     * Creates title on update menu
     */
    public void createTitle() {
        title = this.createText("Mill Creek Insurance Mailer", 16, FontWeight.BOLD);
        this.root.add(title, 1, TITLE_ROW);
        GridPane.setHalignment(title, HPos.CENTER);
    }

    /**
     * Creates the file selector button and viewer
     */
    public void setUpFileSelector() {
        policyFile = null;
        root.getChildren().remove(cancelButton);
        root.getChildren().remove(filePath);

        VBox dragTarget = new VBox();

        dragTarget.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();

                boolean success = false;
                if(db.hasString()) {
                    success = true;
                    File policy = new File(db.getString());
                    setPolicyFile(policy);
                }

                event.setDropCompleted(success);
                event.consume();
            }
        });

        root.add(dragTarget, 1, CHOOSE_POLICY);

        final FileChooser fileChooser = new FileChooser();
        this.fileSelect = new Button("Choose policy");
        this.fileSelect.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        File file = fileChooser.showOpenDialog(primaryStage);
                        if(file != null) {
                            setPolicyFile(file);
                        }
                    }
                }
        );
        root.add(fileSelect, 1, CHOOSE_POLICY);
        GridPane.setHalignment(fileSelect, HPos.CENTER);
    }

    /**
     * Sets up the grip pane
     */
    public void setUpGridPane() {
        this.root = new GridPane();
        root.setAlignment(Pos.TOP_CENTER);
        root.setHgap(20);
        root.setVgap(30);
        root.setPadding(new Insets(25,25,25,25));

    }

    /**
     * Creates a Pop up entitled error with a given description
     * @param desc the description for the pop up to have
     */
    public void errorPopup(String desc) {
        final Stage dialog = new Stage();
        dialog.setTitle("ERROR");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        Scene dialogScene = new Scene(grid, 400, 300);
        dialog.setScene(dialogScene);
        dialog.show();

        grid.add(this.createText("ERROR", 20, FontWeight.BOLD), 0, 0);
        grid.add(this.createText(desc, 12, FontWeight.NORMAL), 0, 1);


        Button ok = new Button("OK");
        ok.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                dialog.close();
            }
        });
        grid.add(ok, 0, 2);
    }

    /**
     * Opens a dialog to ask whether to send the insured an email or letter
     * @param email email found on QQ
     * @param policy Policy file
     * @param app this, for policy object to callback
     */
    public void sendToInsured(String email, Policy policy, ApplicationWindow app) {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        // email option
        this.addToGrid(grid, this.createText("Would you like to send an email", 20, FontWeight.NORMAL), 1, 0);
        // letter option
        this.addToGrid(grid, this.createText("Or send a physical letter", 20, FontWeight.NORMAL), 1, 2);

        Text emailFound;
        if (email == null) {
            emailFound = this.createText("No email found on QQ", 12, FontWeight.NORMAL);
        }else {
            emailFound = this.createText("Email found on QQ: " + email, 12, FontWeight.NORMAL);
        }
        this.addToGrid(grid, emailFound, 1, 4);

        Button emailButton = new Button("Email");
        emailButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                dialog.close();
                policy.getEmailInfo(email, app);
            }
        });

        Button letterButton = new Button("Letter");
        letterButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                dialog.close();
                try {
                    policy.sendLetter();
                }catch (IOException io) {
                    errorPopup("Unable to send letter");
                }
            }
        });

        this.addToGrid(grid, emailButton, 0, 6);
        this.addToGrid(grid, letterButton, 2, 6);

        Scene dialogScene = new Scene(grid, 500, 300);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    /**
     *  Adds a given node to a given grid at given column and row index
     * @param grid Grid pane to add to
     * @param node node to add to grid
     * @param columnIndex colmun index to add at
     * @param rowIndex row index to add at
     */
    private void addToGrid(GridPane grid, Node node, int columnIndex, int rowIndex) {
        grid.add(node, columnIndex, rowIndex);
        GridPane.setHalignment(node, HPos.CENTER);
    }


    /**
     * Creates the button to being updating the policy on QQ, send to insured, and print for mortgagee
     * @param application This, for policy callback
     * @param username username for QQ
     * @param password password for QQ
     */
    private void updateButton(ApplicationWindow application, String username, String password){
        Button update = new Button("Update Policy");
        update.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(policyFile == null) {
                   errorPopup("Error please select a policy");
                   return;
                }
                Policy policy;
                try {
                    if (oceanHarbor.isSelected()) {
                        policy = new OceanHarbor(policyFile);
                    } else if (nBay.isSelected()){
                        policy = new NarragansettBay(policyFile);
                    } else {
                        return;
                    }
                }catch (IOException io) {
                    errorPopup("Unable to read PDF");
                    return;
                }

                if(updateInQQ.isSelected() || sendToInsured.isSelected()) {
                    policy.qqSignIn(username, password);
                }

                try {
                    if (updateInQQ.isSelected()) {
                        policy.updateOnQQ();
                    }

                    if (sendToInsured.isSelected() && policy.sendToInsrured()) {
                        policy.mailToInsured(application);
                    }
                }catch (NotSignedInException signIn) {
                    errorPopup("Unable to sign into qq");
                }

                if(printForMortgage.isSelected() && policy.sendToMortgagee()) {
                    try {
                        policy.printMortgagee();
                    } catch (IOException io) {
                        errorPopup("Unable to print mortgage pages");
                    }
                }

                policy.closeQQ();


            }
        });
        root.add(update, 1, UPDATE_BUTTON);
        GridPane.setHalignment(update, HPos.CENTER);
    }
}
