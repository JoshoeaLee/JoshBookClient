package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import javafx.scene.control.ChoiceBox;

public class ClientThread extends Thread {

    Socket clientSocket;
    BufferedReader inputReader;
    PrintWriter outputPrinter;
    ClientController clientController;

    public ClientThread(Socket clientSocket, BufferedReader inputReader, PrintWriter outputPrinter, ClientController clientController){
        this.clientSocket = clientSocket;
        this.inputReader = inputReader;
        this.outputPrinter = outputPrinter;
        this.clientController = clientController;
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
                String message = inputReader.readLine();
                System.out.println(message + " at socket " + clientSocket);
            }


            this.listenForMore();
        }
            
        catch(IOException e){
            System.out.println("Socket's closed!");
        }
    }
    

    public void listenForMore(){
        System.out.println("ListenForMore");
        clientController.listenForMessages();
    }
}
