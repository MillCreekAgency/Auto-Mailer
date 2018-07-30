import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
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

    final static int TITLE_ROW = 0;
    final static int COMPANY_SELECT = 2;
    final static int CHOOSE_POLICY = 3;
    final static int OPTION_SELECTION = 5;


    public static void main(String[] args) {
        launch(args);
        //System.out.println(javafx.scene.text.Font.getFamilies().toString());
    }

    public void setPolicyFile(File policyFile, Stage primaryStage) {
        this.policyFile = policyFile;
        root.getChildren().remove(this.fileSelect);
        filePath = new Text(policyFile.getName());
        root.add(filePath, 0,CHOOSE_POLICY, 2, 1);

        cancelButton = new Button("Cancel");
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                setUpFileSelector(primaryStage);
            }
        });
        root.add(cancelButton, 2,CHOOSE_POLICY);
    }

    public void createCompanySelector(GridPane root) {
        companies = new ToggleGroup();

        oceanHarbor = new RadioButton("Ocean Harbor");
        oceanHarbor.setToggleGroup(companies);
        oceanHarbor.setSelected(true);

        Text companyTitle = new Text("Select Company");
        root.add(companyTitle, 1, COMPANY_SELECT - 1);
        root.add(oceanHarbor, 0, COMPANY_SELECT);

    }
    public void setOptionMenu(){
        Text optionTitle = new Text("Options");
        root.add(optionTitle, 1, OPTION_SELECTION - 1);

        updateInQQ = new CheckBox("Update in QQ");
        updateInQQ.setSelected(true);
        root.add(updateInQQ, 0, OPTION_SELECTION);

        sendToInsured = new CheckBox("Send to Insured");
        sendToInsured.setSelected(true);
        root.add(sendToInsured, 1, OPTION_SELECTION);

        printForMortgage = new CheckBox("Print for Mortgagee");
        printForMortgage.setSelected(true);
        root.add(printForMortgage, 2, OPTION_SELECTION);
    }

    public void setText() {
        title = new Text("Automatic Insurance Mailer");
        title.setFont(new Font("Helvetica Neue", 16));
        this.root.add(this.title, 1, TITLE_ROW);
        title.setTextAlignment(TextAlignment.CENTER);
    }

    public void setUpFileSelector(Stage primaryStage) {
        root.getChildren().remove(cancelButton);
        root.getChildren().remove(filePath);

        final FileChooser fileChooser = new FileChooser();
        this.fileSelect = new Button("Choose policy");
        this.fileSelect.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        File file = fileChooser.showOpenDialog(primaryStage);
                        if(file != null) {
                            setPolicyFile(file, primaryStage);
                        }
                    }
                }
        );
        root.add(fileSelect, 1, CHOOSE_POLICY);
    }

    public void setUpGridPane() {
        this.root = new GridPane();
        root.setAlignment(Pos.TOP_CENTER);
        root.setHgap(20);
        root.setVgap(20);
        root.setPadding(new Insets(25,25,25,25));

    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Mill Creek Agency Auto Insurance Mailer");

        this.setUpGridPane();
        this.setUpFileSelector(primaryStage);
        this.setText();
        this.createCompanySelector(root);
        this.setOptionMenu();
        primaryStage.setScene(new Scene(root, 800,550));
        primaryStage.show();
    }
}
