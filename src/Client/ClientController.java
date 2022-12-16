package Client;

import java.io.BufferedReader;
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

    //I Used the following links to help me with decoding and recovering keys from byte arrays
    //https://stackoverflow.com/questions/2411096/how-to-recover-a-rsa-public-key-from-a-byte-array
    //Decoder - https://stackoverflow.com/questions/67947209/java-lang-illegalargumentexception-illegal-base64-character-5b


/*
 * This class actually holds the business logic involving logging in, creating accounts, sending messages to the server
 */
public class ClientController {

    ClientGUI gui;
    BufferedReader inputReader;
    PrintWriter outputPrinter;
    Socket clientSocket;
    String userId;
    SecretKey sessionKey;
    EncryptionHandler encryptionHandler;
    HashMap<String, ObservableList<String>> messageBoxes = new HashMap<>();
    
    public ClientController(ClientGUI gui) throws Exception{
        this.gui = gui;
    }


    /*
     * Connects the client to the server.
     */
    public void connectToServer(String ip, int port) throws Exception{
        clientSocket = new Socket(ip, port);
        inputReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        outputPrinter = new PrintWriter(clientSocket.getOutputStream(), true);
    }

    /*
     * Sends a message to the server letting it know that this user is logging out.
     */
    public void logOut(){
        outputPrinter.println("LO357GGI1NG_O683UT_T)%#IME");
        outputPrinter.println(userId);
    }


    /*
     * Sends a message to the server letting it know that this client is sending a message,
     * It sends a message and the message's recepient.
     */
    public void sendMessage(String message, String recepient) {
        //Make sure a recepient is selected
        if(recepient==null){
            gui.openNotification("Please select a valid recepient");
        }
        //Updates your own screen to show the message you've sent with a timestamp
        else{
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            String timeStampString = timestamp.toString();
            this.updateYourMessages(recepient, message, timeStampString);

        //Tell the server you're sending a message.
        outputPrinter.println("INCOMING_MESSAGE_X9%(*");
        outputPrinter.println(recepient);  //Send the recepient


        //SENDING ENCRYPTED MESSAGE - https://stackoverflow.com/questions/4860590/sending-and-receiving-byte-using-socket
        try {
            //Encrypt the message using the client's session key
            byte[] encMessage = encryptionHandler.encrypt(message);
            //ENCODE
            String encMessageString = Base64.getEncoder().encodeToString(encMessage);
            //Send the encrypted message
            outputPrinter.println(encMessageString);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
     }
    }

  
    /*
     * Creates a thread which listens for messages from the server
     */
    public void listenForMessages(){
        new ClientThread(clientSocket, inputReader, outputPrinter, this, encryptionHandler, sessionKey).start();
    }

    /*
     * Closes the client socket and initiates proper logout procedures.
     */
    public void closeClient() throws IOException{
        if(clientSocket!=null){
            outputPrinter.println("Client Closing");
            this.logOut();
            clientSocket.close();
            System.out.println("Successfully closing");
        }
       
    }


    /*
     * Secure Login. Encrypts entered details using the server public key and then sends it to the server.
     * Listens to server to see how login process is going.
     */
    public void logIn(String firstName, String lastName, String passWord, Text portInstructions) throws IOException, InterruptedException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException{

        //Secure Login
        EncryptionHandler loginEncryptionHandler = new EncryptionHandler();
        //Encrypt details
        byte[] EncFN = loginEncryptionHandler.encryptUsingServerPublic(firstName);
        byte[] EncLN = loginEncryptionHandler.encryptUsingServerPublic(lastName);
        byte[] EncPW = loginEncryptionHandler.encryptUsingServerPublic(passWord);

        //Encode
        String encodedFN = Base64.getEncoder().encodeToString(EncFN);
        String encodedLN = Base64.getEncoder().encodeToString(EncLN);
        String encodedPW = Base64.getEncoder().encodeToString(EncPW);
        
        //Notify server of login and then send
        outputPrinter.println("login");
        outputPrinter.println(encodedFN);
        outputPrinter.println(encodedLN);
        outputPrinter.println(encodedPW);

        //Check whether login has been successful or not.
        String accountStatus = inputReader.readLine();
        if(accountStatus.equals("loginSuccess")){
            gui.extraStage.close();
            gui.openNotification("Successfully logged in");
            String welcome = inputReader.readLine();
            portInstructions.setText(welcome);
            userId = inputReader.readLine();
            //Generate session key
            sessionKey = generateSessionKey(userId);

            //Encode -> Encrypt -> Encode The Client's Session Key using the server key.
             String keyString = Base64.getEncoder().encodeToString(sessionKey.getEncoded());
             byte[] encKey = encryptionHandler.encryptUsingServerPublic(keyString);
             String encKeyString = Base64.getEncoder().encodeToString(encKey);
            //Send the server the session key being used.
            outputPrinter.println(encKeyString);

            this.populateOnlineUsers();
            this.enterChatroom();
        }
        else if(accountStatus.equals("loginFail")){
            gui.openNotification("Login Failed!");
        }
    }

    /*
     * Secure account creation. Encrypts entered details using the server public key and then sends it to the server.
     * Listens to server to see how process is going
     */
    public void createAccount(String fName, String lName, String pWord, Text portInstructions) throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException{
       //Secure Account Creation
        EncryptionHandler createAccountEncryptionHandler = new EncryptionHandler();
        //ENCRYPT
        byte[] EncFN = createAccountEncryptionHandler.encryptUsingServerPublic(fName);
        byte[] EncLN = createAccountEncryptionHandler.encryptUsingServerPublic(lName);
        byte[] EncPW = createAccountEncryptionHandler.encryptUsingServerPublic(pWord);

        //ENCODE
        String encodedFN = Base64.getEncoder().encodeToString(EncFN);
        String encodedLN = Base64.getEncoder().encodeToString(EncLN);
        String encodedPW = Base64.getEncoder().encodeToString(EncPW);
        
        //NOTIFY SERVER OF ACCOUNT CREATION AND THEN SEND
        outputPrinter.println("createAccount");
        outputPrinter.println(encodedFN);
        outputPrinter.println(encodedLN);
        outputPrinter.println(encodedPW);

        //CHECK TO SEE IF ACCOUNT CREATION IS SUCCESSFUL OR NOT
        String accountStatus =  inputReader.readLine();
        if(accountStatus.equals("AccountCreated")){
            gui.extraStage.close();
            gui.openNotification("Account Successfully Created");
            String welcome = inputReader.readLine();
            portInstructions.setText(welcome);
            userId = inputReader.readLine();
            //GENERATE A SESSION KEY FOR THE USER
            sessionKey = generateSessionKey(userId);

            //ENCODE->ENCRYPT->ENCODE SESSION KEY USING SERVER PUBLIC KEY AND THEN SEND IT TO THE SERVER
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

   /*
    * Notifies server that a new user has entered and then listens for messages.
    */
    public void enterChatroom(){
        outputPrinter.println("NEW_USER_ENTRANCE");
        this.listenForMessages(); 

    }

    /**
     * Generates a session key
     * @return Session key for the client while they're connected to the server
     */
    public SecretKey generateSessionKey(String user) throws FileNotFoundException{
        try {
            encryptionHandler = new EncryptionHandler();
            SecretKey sessionKey = encryptionHandler.generateKey();
            return sessionKey;
            
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }


    /*
     * FINDS OUT HOW MANY PEOPLE ARE ONLINE FROM THE SERVER AND THEN POPULATES THE 
     * 'ONLINE USER LIST'.
     */
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

    
        /*
         * Updates the message display with messages received from the server.
         */
    public void updateMessages(String user, String decryptedMessage, String timeString){
            Platform.runLater(new Runnable() {
                @Override
                public void run(){
                    ClientController.this.getMessageBoxes().get(user).add(user + " said : " + decryptedMessage + " |" + timeString);
                }
            });
        }

        /*
         * Updates the message display with messages you have sent.
         */
    public void updateYourMessages(String user, String decryptedMessage, String timeString){
            Platform.runLater(new Runnable() {
                @Override
                public void run(){
                    ClientController.this.getMessageBoxes().get(user).add("You said : " + decryptedMessage + " |" + timeString);
                }
            });
        }

        //GETTER
        public HashMap<String, ObservableList<String>> getMessageBoxes(){
            return messageBoxes;
        }
    }


    

