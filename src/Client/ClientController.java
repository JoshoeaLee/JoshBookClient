package Client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;

public class ClientController {

    ClientGUI gui;
    BufferedReader inputReader;
    PrintWriter outputPrinter;
    Socket clientSocket;
    String userId;
    SecretKey sessionKey;
    EncryptionHandler encryptionHandler;

    HashMap<String, ObservableList<String>> messageBoxes = new HashMap<>();
    
    //https://stackoverflow.com/questions/2411096/how-to-recover-a-rsa-public-key-from-a-byte-array
    //Decoder - https://stackoverflow.com/questions/67947209/java-lang-illegalargumentexception-illegal-base64-character-5b

    public ClientController(ClientGUI gui) throws Exception{
        this.gui = gui;

    }

    public void connectToServer(String ip, int port) throws Exception{

        clientSocket = new Socket(ip, port);
        inputReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        outputPrinter = new PrintWriter(clientSocket.getOutputStream(), true);

    }

    public void logOut(){
        outputPrinter.println("LO357GGI1NG_O683UT_T)%#IME");
        outputPrinter.println(userId);
    }


    public void sendMessage(String message, String recepient) {
        if(recepient==null){
            gui.openNotification("Please select a valid recepient");
        }
        else{
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            String timeStampString = timestamp.toString();
            this.updateYourMessages(recepient, message, timeStampString);
        outputPrinter.println("INCOMING_MESSAGE_X9%(*");
        outputPrinter.println(recepient);


        //SENDING ENCRYPTED MESSAGE - https://stackoverflow.com/questions/4860590/sending-and-receiving-byte-using-socket

        try {
            //Encrypt the message using the client's session key
            byte[] encMessage = encryptionHandler.encrypt(message);
            String encMessageString = Base64.getEncoder().encodeToString(encMessage);
            outputPrinter.println(encMessageString);

        } catch (Exception e1) {
            e1.printStackTrace();
        }
        
     }
    }

    public void listenForMessages(){
        new ClientThread(clientSocket, inputReader, outputPrinter, this, encryptionHandler, sessionKey).start();
    }

    public void closeClient() throws IOException{
        if(clientSocket!=null){
            outputPrinter.println("Client Closing");
            this.logOut();
            clientSocket.close();
            System.out.println("Successfully closing");
        }
       
    }

    //THESE HAPPEN BEFORE SENDING MESSAGES HAPPENS

    public void logIn(String firstName, String lastName, String passWord, Text portInstructions) throws IOException, InterruptedException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException{

        //Secure Login
        EncryptionHandler loginEncryptionHandler = new EncryptionHandler();
        byte[] EncFN = loginEncryptionHandler.encryptUsingServerPublic(firstName);
        byte[] EncLN = loginEncryptionHandler.encryptUsingServerPublic(lastName);
        byte[] EncPW = loginEncryptionHandler.encryptUsingServerPublic(passWord);

        String encodedFN = Base64.getEncoder().encodeToString(EncFN);
        String encodedLN = Base64.getEncoder().encodeToString(EncLN);
        String encodedPW = Base64.getEncoder().encodeToString(EncPW);
        
        outputPrinter.println("login");
        outputPrinter.println(encodedFN);
        outputPrinter.println(encodedLN);
        outputPrinter.println(encodedPW);

        String accountStatus = inputReader.readLine();
        if(accountStatus.equals("loginSuccess")){
            gui.extraStage.close();
            gui.openNotification("Successfully logged in");
            String welcome = inputReader.readLine();
            portInstructions.setText(welcome);
            userId = inputReader.readLine();


            sessionKey = generateSessionKey(userId);

             //Encode -> Encrypt -> Encode The Client's Session Key using the server key.
             String keyString = Base64.getEncoder().encodeToString(sessionKey.getEncoded());
             byte[] encKey = encryptionHandler.encryptUsingServerPublic(keyString);
             String encKeyString = Base64.getEncoder().encodeToString(encKey);
            outputPrinter.println(encKeyString);

            this.populateOnlineUsers();
            this.enterChatroom();

        }
        else if(accountStatus.equals("loginFail")){
            gui.openNotification("Login Failed!");
        }

    }

    public void createAccount(String fName, String lName, String pWord, Text portInstructions) throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException{
       //Secure Account Creation
        EncryptionHandler createAccountEncryptionHandler = new EncryptionHandler();
        byte[] EncFN = createAccountEncryptionHandler.encryptUsingServerPublic(fName);
        byte[] EncLN = createAccountEncryptionHandler.encryptUsingServerPublic(lName);
        byte[] EncPW = createAccountEncryptionHandler.encryptUsingServerPublic(pWord);

        String encodedFN = Base64.getEncoder().encodeToString(EncFN);
        String encodedLN = Base64.getEncoder().encodeToString(EncLN);
        String encodedPW = Base64.getEncoder().encodeToString(EncPW);
        
        outputPrinter.println("createAccount");
        outputPrinter.println(encodedFN);
        outputPrinter.println(encodedLN);
        outputPrinter.println(encodedPW);

        String accountStatus =  inputReader.readLine();
        if(accountStatus.equals("AccountCreated")){
            gui.extraStage.close();
            gui.openNotification("Account Successfully Created");
            String welcome = inputReader.readLine();
            portInstructions.setText(welcome);
            userId = inputReader.readLine();


            sessionKey = generateSessionKey(userId);

            String keyString = Base64.getEncoder().encodeToString(sessionKey.getEncoded());
            byte[] encKey = encryptionHandler.encryptUsingServerPublic(keyString);
            String encKeyString = Base64.getEncoder().encodeToString(encKey);
            outputPrinter.println(encKeyString);

            
            this.populateOnlineUsers();
            this.enterChatroom();

        }
        else if(accountStatus.equals("AccountFailure")){
            gui.openNotification("That account already exists!");
        }
        
    }

    public void enterChatroom(){
        outputPrinter.println("NEW_USER_ENTRANCE");
        this.listenForMessages(); //LISTEN

    }

    /**
     * Generates a session key
     * @return Session key for the client while they're connected to the server
     * @throws FileNotFoundException
     */
    public SecretKey generateSessionKey(String user) throws FileNotFoundException{
        try {
            encryptionHandler = new EncryptionHandler();
            SecretKey sessionKey = encryptionHandler.generateKey();
            String encodedSessionKey = Base64.getEncoder().encodeToString(sessionKey.getEncoded());
            PrintWriter saveKeyPrint = new PrintWriter(new File("./lib/" + user + "SessionKey.txt"));
            saveKeyPrint.println(encodedSessionKey);
            saveKeyPrint.close();

            return sessionKey;
            
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void populateOnlineUsers() throws IOException{
        int userNum = 1;
        try {
             userNum = Integer.parseInt(inputReader.readLine());
        } catch (IOException e1) {
            e1.printStackTrace();
        }

     
            for(int i = 0; i<userNum; i++){
                String otherUser = inputReader.readLine();

                if(!otherUser.equals("Online Users Populated")){
                    gui.getOnlineUserList().getItems().add(otherUser);
                    ArrayList<String> messageList = new ArrayList<>();
                    ObservableList<String> observableMessageList = FXCollections.observableArrayList(messageList);
                    messageBoxes.put(otherUser, observableMessageList);
                    messageBoxes.get(otherUser).add(otherUser);
                }
               

            }
       
        }

        public void updateMessages(String user, String decryptedMessage, String timeString){
            Platform.runLater(new Runnable() {
                @Override
                public void run(){
                    ClientController.this.getMessageBoxes().get(user).add(user + " said : " + decryptedMessage + " |" + timeString);
                }
            });
        }

        public void updateYourMessages(String user, String decryptedMessage, String timeString){
            Platform.runLater(new Runnable() {
                @Override
                public void run(){
                    ClientController.this.getMessageBoxes().get(user).add("You said : " + decryptedMessage + " |" + timeString);
                }
            });
        }

        public HashMap<String, ObservableList<String>> getMessageBoxes(){
            return messageBoxes;
        }
    }


    

