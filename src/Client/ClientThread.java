package Client;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Base64;

import javax.crypto.SecretKey;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ChoiceBox;


public class ClientThread extends Thread {

    Socket clientSocket;
    BufferedReader inputReader;
    PrintWriter outputPrinter;
    ClientController clientController;
    SecretKey sessionKey;
    AES aes;

    public ClientThread(Socket clientSocket, BufferedReader inputReader, PrintWriter outputPrinter, ClientController clientController, AES aes, SecretKey sessionKey){
        this.clientSocket = clientSocket;
        this.inputReader = inputReader;
        this.outputPrinter = outputPrinter;
        this.clientController = clientController;
        this.aes = aes;
        this.sessionKey = sessionKey;
        
    }

    public void run(){
        try{
            String line = inputReader.readLine();

            
            if(line.equals("NEW_USER_WARNING")){
                String user = inputReader.readLine();
                ChoiceBox<String> onlineUserList = clientController.gui.getOnlineUserList();

                //ChoiceBok
                if (!onlineUserList.getItems().contains(user)){
                    onlineUserList.getItems().add(user);


                    ArrayList<String> messageList = new ArrayList<>();
                    ObservableList<String> observableMessageList = FXCollections.observableArrayList(messageList);
                    clientController.getMessageBoxes().put(user, observableMessageList);
                    clientController.getMessageBoxes().get(user).add(user);

                }
            }

            else if(line.equals("MESSAGE_INCOMING")){
                String  user = inputReader.readLine();
                String encMessage = inputReader.readLine();
                byte[] decodedMessage = Base64.getDecoder().decode(encMessage);
                String decryptedMessage = aes.decrypt(decodedMessage);
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                String timeStampString = timestamp.toString();
                clientController.updateMessages(user, decryptedMessage, timeStampString);
                System.out.println(decryptedMessage + " at socket " + clientSocket);
            }

            else if(line.equals("USER_LOGGING_OUT")){
                String logOutUser = inputReader.readLine();
                System.out.println("Logout Has been triggered");
                clientController.gui.getOnlineUserList().getItems().remove(logOutUser);
            }
            
            this.listenForMore();

        }
            
        catch(Exception e){
            System.out.println("Socket's closed!");
        } 
    }
    

    public void listenForMore(){
        clientController.listenForMessages();
    }
}
