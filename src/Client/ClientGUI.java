package Client;

import java.io.IOException;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.text.Text;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class ClientGUI extends Application {

    ClientService clientService = new ClientService(this);
    ClientController clientController = clientService.getController();
    boolean loggedin = false;
    Text portInstructions;
    ChoiceBox<String> friendList;
    ListView<String> messageView;
    Stage extraStage;




    @Override
    public void start(Stage primaryStage) throws Exception{

        //Root Pane
        BorderPane root = new BorderPane();

        //Grab SideBar
        VBox clientSidebar = this.makeSidebar();
        root.setLeft(clientSidebar);

        //Message Pane
        VBox messagePane = this.makeMessagePane();
        root.setCenter(messagePane);


        //Scene setup
        Scene scene = new Scene(root,1000,400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("JoshBook Client");
        primaryStage.show();

        //On Client Close
        primaryStage.setOnCloseRequest(e->{
            System.out.println("Closing the client");
            try {
                clientController.closeClient();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
    }

    private VBox makeSidebar(){

        //Port and IP Selection Box////////////////////////////////////////////////////////////////////////////////////////////////////////
         portInstructions = new Text("Connect to a server");
        Text ipLabel = new Text("Select an IP Address: ");
        TextField ipField = new TextField();    
        
        Text portLabel = new Text("Select a Port Number: ");
        TextField portField = new TextField();       
        
        Button connectBtn = new Button("Connect");
        Button closeClient = new Button("Close");

        connectBtn.setOnAction(e->{
            try{
                String ip = ipField.getText();
                int port = Integer.valueOf(portField.getText());
                clientController.connectToServer(ip, port);
                if(!loggedin){
                    this.openLoginScreen();
                }
                //SWITCH TO LOGGEDIN
            }
            catch(Exception error){
                error.printStackTrace();
            }
        });

        closeClient.setOnAction(e->{
            try {
                clientController.closeClient();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });


        VBox portBox = new VBox(10);
        portBox.getChildren().addAll(portInstructions, ipLabel, ipField, portLabel, portField, connectBtn, closeClient);
        portBox.setMinHeight(160);
        portBox.setAlignment(Pos.CENTER);
        portBox.setBackground(new Background(new BackgroundFill(Color.BEIGE, CornerRadii.EMPTY, Insets.EMPTY)));

        //Choose a friend////////////////////////////////////////////////////////////////////////////////////////////////////////
        Text chooseFriend = new Text("Choose an online person to talk to");

        friendList = new ChoiceBox<String>();

        Button chooseFriendBtn = new Button("Select");


        VBox friendBox = new VBox(10);
        friendBox.getChildren().addAll(chooseFriend, friendList, chooseFriendBtn);
        friendBox.setMinHeight(160);
        friendBox.setAlignment(Pos.CENTER);
        friendBox.setBackground(new Background(new BackgroundFill(Color.PAPAYAWHIP, CornerRadii.EMPTY, Insets.EMPTY)));



        chooseFriendBtn.setOnAction(e->{
            if(clientController.getMessageBoxes().get(friendList.getSelectionModel().getSelectedItem())==null){
                this.openNotification("Choose a valid user");
            }
            else{
                ObservableList<String> userMessageList = this.clientController.getMessageBoxes().get(friendList.getSelectionModel().getSelectedItem());
                this.messageView.setItems(userMessageList);
            }
            
        });

        


        //SideBar
        VBox sideBar = new VBox(10);
        sideBar.getChildren().addAll(portBox,  friendBox);

        return sideBar;
    }

   

    private VBox makeMessagePane(){

        //Message Box Title
        Text boxTitle = new Text("JoshBook Chat");
        VBox titleBox = new VBox(10);
        titleBox.getChildren().add(boxTitle);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        titleBox.setMinHeight(20);
        titleBox.setBackground(new Background(new BackgroundFill(Color.GRAY, CornerRadii.EMPTY, Insets.EMPTY)));

        //Message Table

        messageView = new ListView<String>();
        messageView.setPlaceholder(new Label("Choose someone and start chatting!"));

        //Chat Box Input
        TextArea chatInput = new TextArea();
        chatInput.setText("Enter Message");
        chatInput.setMinSize(600, 40);
        chatInput.setPadding(new Insets(10));
        Button sendBtn = new Button("Send");
        HBox inputBox = new HBox(10);
        inputBox.getChildren().addAll(chatInput, sendBtn);
        inputBox.setAlignment(Pos.CENTER);
        inputBox.setMinHeight(80);
        inputBox.setBackground(new Background(new BackgroundFill(Color.GRAY, CornerRadii.EMPTY, Insets.EMPTY)));


        //SendButton Functionality

        sendBtn.setOnAction(e->{
            String receiver = ClientGUI.this.getOnlineUserList().getValue();
            clientController.sendMessage(chatInput.getText(), receiver);
        });
        //Full Message Pane
        VBox fullMessagePane = new VBox();
        fullMessagePane.getChildren().addAll(titleBox, messageView, inputBox);

        
        return fullMessagePane;

    }

    public void openLoginScreen(){

        //Making Login Screen

        Text loginInstructions = new Text("Log In!");

        Text firstNameLabel = new Text("First Name");
        TextField firstNameField = new TextField();

        Text lastNameLabel = new Text("Last Name");
        TextField lastNameField = new TextField();

        Text passwordText = new Text("Password");
        PasswordField passField = new PasswordField();
        Button loginBtn = new Button("Log in");
        Button createAccountBtn = new Button("Create New Account");

        VBox loginBox = new VBox(10);
        loginBox.getChildren().addAll(loginInstructions, firstNameLabel, firstNameField, lastNameLabel, lastNameField, passwordText, passField, loginBtn, createAccountBtn);

        Scene loginScene = new Scene(loginBox, 600, 600);
        Stage loginStage = new Stage();
        extraStage = loginStage;
        loginStage.setScene(loginScene);
        loginStage.setTitle("Please Log in");
        loginStage.show();

        createAccountBtn.setOnAction(click->{
            loginStage.close();
            this.openCreateAccountScreen();
        });

        loginBtn.setOnAction(e->{
            String firstName = firstNameField.getText();
            String lastName = lastNameField.getText();
            String pWord = passField.getText();

            try {
                clientController.logIn(firstName, lastName, pWord, portInstructions);
            } catch (Exception e1) {
                e1.printStackTrace();
            }

        });
    }


    public void openCreateAccountScreen(){

        //Making Create Account Screen

        Text createAccountInstructions = new Text("Create an Accoung!");

        Text firstName = new Text("First Name");
        TextField firstNameField = new TextField();

        Text lastName = new Text("Last Name");
        TextField lastNameField = new TextField();

        Text passwordText = new Text("Password");
        PasswordField passField = new PasswordField();

        Button createButton = new Button("Create");

        Button loginSwapButton = new Button("I already have an account");

        VBox createBox = new VBox(10);
        createBox.getChildren().addAll(createAccountInstructions, firstName, firstNameField,
         lastName, lastNameField, passwordText, passField, createButton, loginSwapButton);

        Scene createScene = new Scene(createBox, 600, 600);
        Stage createStage = new Stage();
        extraStage = createStage;
        createStage.setScene(createScene);
        createStage.setTitle("Create Account");
        createStage.show();

        loginSwapButton.setOnAction(click->{
            createStage.close();
            this.openLoginScreen();
        });

        createButton.setOnAction(e->{
            String newFirstName = firstNameField.getText();
            String newLastName = lastNameField.getText();
            String newPassword = passField.getText();
            try {
                clientController.createAccount(newFirstName, newLastName, newPassword, portInstructions);
            } catch (Exception e1) {
                e1.printStackTrace();
            }


        });


    }

    public void openNotification(String message){
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Alert");
        alert.setContentText(message);
        alert.showAndWait();
    }



    public ChoiceBox<String> getOnlineUserList(){
        return friendList;
    }

    public ListView<String> getCurrentMessages(){
        return messageView;
    }


    public static void main(String[] args) {
        launch(args);
    }
}