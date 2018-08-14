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

public class ApplicationWindow extends Application {

    private File policyFile;
    private RadioButton oceanHarbor;
    private ToggleGroup companies;
    private GridPane root;
    private Button fileSelect;
    private Text filePath;
    private Text title;
    private Button cancelButton;
    private CheckBox updateInQQ;
    private CheckBox sendToInsured;
    private CheckBox printForMortgage;
    private Stage primaryStage;

    private String password;
    private String username = "dean@millcreekagency.com";

    private final static int TITLE_ROW = 1;
    private final static int COMPANY_SELECT = 4;
    private final static int CHOOSE_POLICY = 5;
    private final static int OPTION_SELECTION = 8;
    private final static int UPDATE_BUTTON = 9;


    public static void main(String[] args) {
        //OceanHarbor oc = new OceanHarbor("/Users/bryce/policies/", false, false);
        launch(args);
        //System.out.println(javafx.scene.text.Font.getFamilies().toString());
    }

    public void login(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

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

    public void setUpUpdater() {
        this.setUpFileSelector();
        this.setText();
        this.createCompanySelector(root);
        this.setOptionMenu();
        this.updateButton(this);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
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

    public void createCompanySelector(GridPane root) {
        companies = new ToggleGroup();

        oceanHarbor = new RadioButton("Ocean Harbor");
        oceanHarbor.setToggleGroup(companies);
        oceanHarbor.setSelected(true);

        Text companyTitle = new Text("Select Company");
        root.add(companyTitle, 1, COMPANY_SELECT - 1);
        GridPane.setHalignment(companyTitle, HPos.CENTER);
        root.add(oceanHarbor, 0, COMPANY_SELECT);
        GridPane.setHalignment(oceanHarbor, HPos.CENTER);

    }

    public void getEmailPassword(String to, Policy policy){
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        Text title = new Text("Please enter email password for " + Email.FROM);
        title.setFont(Font.font("Helvetica Neue", FontWeight.NORMAL,20));
        PasswordField emailPassword = new PasswordField();
        grid.add(title, 0, 0);
        grid.add(emailPassword, 0, 1);
        Button ok = new Button("Submit");
        ok.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                dialog.close();
                policy.sendEmail(to, emailPassword.getText());
            }
        });
        grid.add(ok, 0, 2);
        Scene dialogScene = new Scene(grid, 600, 500);
        dialog.setScene(dialogScene);
        dialog.show();
    }

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
        Scene dialogScene = new Scene(grid, 400, 300);
        dialog.setScene(dialogScene);
        dialog.show();
    }

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

    private Text createText(String str, int size, FontWeight fontWeight) {
        Text text = new Text(str);
        text.setFont(Font.font("Helventica Neue", fontWeight, size));
        GridPane.setHalignment(text, HPos.CENTER);
        return text;
    }

    public void setText() {
        title = this.createText("Mill Creek Insurance Mailer", 16, FontWeight.BOLD);
        this.root.add(title, 1, TITLE_ROW);
        GridPane.setHalignment(title, HPos.CENTER);
    }

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

    public void setUpGridPane() {
        this.root = new GridPane();
        root.setAlignment(Pos.TOP_CENTER);
        root.setHgap(20);
        root.setVgap(30);
        root.setPadding(new Insets(25,25,25,25));

    }

    private void pleaseSelectPolicyPopup() {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        Scene dialogScene = new Scene(grid, 400, 300);
        dialog.setScene(dialogScene);
        dialog.show();

        grid.add(this.createText("ERROR", 20, FontWeight.BOLD), 0, 0);
        grid.add(this.createText("Please select a policy to update", 12, FontWeight.NORMAL), 0, 1);
        Button ok = new Button("OK");
        ok.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                dialog.close();
            }
        });
        grid.add(ok, 0, 2);
    }

    public void sendToInsured(String email, Policy policy, ApplicationWindow app) {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        // email Option
        this.addToGrid(grid, this.createText("Would you like to send an email", 20, FontWeight.NORMAL), 1, 0);
        // Letter option
        this.addToGrid(grid, this.createText("Or send a physical Letter", 20, FontWeight.NORMAL), 1, 2);

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
                policy.makeLetter();
            }
        });

        this.addToGrid(grid, emailButton, 0, 4);
        this.addToGrid(grid, letterButton, 2, 4);

        Scene dialogScene = new Scene(grid, 500, 300);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    private void addToGrid(GridPane grid, Node node, int columnIndex, int rowIndex) {
        grid.add(node, columnIndex, rowIndex);
        GridPane.setHalignment(node, HPos.CENTER);
    }


    private void updateButton(ApplicationWindow application){
        Button update = new Button("Update Policy");
        update.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(policyFile == null) {
                   pleaseSelectPolicyPopup();
                }else {
                    OceanHarbor oc = new OceanHarbor(policyFile, updateInQQ.isSelected(), printForMortgage.isSelected(), sendToInsured.isSelected(), application);
                }
            }
        });
        root.add(update, 1, UPDATE_BUTTON);
        GridPane.setHalignment(update, HPos.CENTER);
    }
}
