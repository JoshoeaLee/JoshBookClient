package Client;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Base64;

import javax.crypto.SecretKey;

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
            System.out.println("Client Thread " + line); //Catches the 'Online Users Populated' line

            
            if(line.equals("NEW_USER_WARNING")){
                String user = inputReader.readLine();
                ChoiceBox<String> onlineUserList = clientController.gui.getOnlineUserList();
                if (!onlineUserList.getItems().contains(user)){
                    onlineUserList.getItems().add(user);
                }
            }

            if(line.equals("MESSAGE_INCOMING")){
                String encMessage = inputReader.readLine();
                byte[] decodedMessage = Base64.getDecoder().decode(encMessage);
                String decryptedMessage = aes.decrypt(decodedMessage);
                System.out.println(decryptedMessage + " at socket " + clientSocket);
            }

            this.listenForMore();
        }
            
        catch(Exception e){
            System.out.println("Socket's closed!");
        } 
    }
    

    public void listenForMore(){
        System.out.println("ListenForMore");
        clientController.listenForMessages();
    }
}
