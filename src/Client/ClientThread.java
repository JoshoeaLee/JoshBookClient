package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Base64;
import javax.crypto.SecretKey;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ChoiceBox;


/*
 * This thread gets created by the client controller when a message is received by the client. 
 * This thread listens for a message. On completion it sets the client controller to listen for more messages.
 */
public class ClientThread extends Thread {

    Socket clientSocket;
    BufferedReader inputReader;
    PrintWriter outputPrinter;
    ClientController clientController;
    SecretKey sessionKey;
    EncryptionHandler encryptionHandler;

    public ClientThread(Socket clientSocket, BufferedReader inputReader, PrintWriter outputPrinter, ClientController clientController, EncryptionHandler encryptionHandler, SecretKey sessionKey){
        this.clientSocket = clientSocket;
        this.inputReader = inputReader;
        this.outputPrinter = outputPrinter;
        this.clientController = clientController;
        this.encryptionHandler = encryptionHandler;
        this.sessionKey = sessionKey;
        
    }

    /*
     * Depending on the message the client receives, different actions must be taken.
     */
    public void run(){
        try{
            String line = inputReader.readLine();

            if(line.equals("NEW_USER_WARNING")){
                this.updateOnlineUsers();
            }

            else if(line.equals("MESSAGE_INCOMING")){
                this.handleMessage();
            }

            else if(line.equals("USER_LOGGING_OUT")){
               this.logUserOut();
            }
            
        this.listenForMore();

        }
            
        catch(Exception e){
            System.out.println("Socket's closed!");
        } 
    }

    /*
     * Adds the new user into the client's 'ONLINE USER LIST' and then creates an ObservableArray which 
     * holds the messages between the new user and this client.
     */
    public void updateOnlineUsers() throws IOException{

                String user = inputReader.readLine();
                ChoiceBox<String> onlineUserList = clientController.gui.getOnlineUserList();

                //Adds new user to the client's ONLINE USER LIST
                if (!onlineUserList.getItems().contains(user)){
                    onlineUserList.getItems().add(user);

                    //Creates an observable list of messages between the client and the new user
                    ArrayList<String> messageList = new ArrayList<>();
                    ObservableList<String> observableMessageList = FXCollections.observableArrayList(messageList);
                    clientController.getMessageBoxes().put(user, observableMessageList);
                    clientController.getMessageBoxes().get(user).add(user);

                }
    }
    
    /*
     * When the client hears a message from the server (which originally came from another client) coming,
     *  It receives it, decodes and decrypts it. Then it updates the client message display.
     */
    public void handleMessage() throws Exception{
        String  user = inputReader.readLine();  //Sender
                
                //Encoded Message received. Decode -> Decrypt using session key.
                String encMessage = inputReader.readLine();   
                byte[] decodedMessage = Base64.getDecoder().decode(encMessage);
                String decryptedMessage = encryptionHandler.decrypt(decodedMessage);

                //Printing out message with timestamp
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                String timeStampString = timestamp.toString();
                clientController.updateMessages(user, decryptedMessage, timeStampString);
                
    }
   
    /*
     * Removes the logged out user from the client's 'ONLINE USER LIST'
     */
    public void logUserOut() throws IOException{
        String logOutUser = inputReader.readLine();
        clientController.gui.getOnlineUserList().getItems().remove(logOutUser);
    }

    /*
     * Calls the client to listen for more messages.
     */
    public void listenForMore(){
        clientController.listenForMessages();
    }
}
