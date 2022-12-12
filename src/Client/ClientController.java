package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javafx.scene.text.Text;

public class ClientController {

    ClientGUI gui;
    BufferedReader inputReader;
    PrintWriter outputPrinter;
    Socket clientSocket;
    String userId;

    
    public ClientController(ClientGUI gui){
        this.gui = gui;

    }

    public void connectToServer(String ip, int port) throws Exception{

        clientSocket = new Socket(ip, port);
        inputReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        outputPrinter = new PrintWriter(clientSocket.getOutputStream(), true);

    }

    public void sendMessage(String message, String recepient){

        outputPrinter.println("INCOMING_MESSAGE_X9%(*");
        outputPrinter.println(recepient);
        outputPrinter.println(message);
    }

    public void listenForMessages(){
        System.out.println("Starting a client thread");
        new ClientThread(clientSocket, inputReader, outputPrinter, this).start();
    }

    public void closeClient() throws IOException{
        if(clientSocket!=null){
            outputPrinter.println("Client Closing");
            clientSocket.close();
            System.out.println("Successfully closing");
        }
       
    }

    //THESE HAPPEN BEFORE SENDING MESSAGES HAPPENS

    public void logIn(String firstName, String lastName, String passWord, Text portInstructions) throws IOException, InterruptedException{
        outputPrinter.println("login");
        outputPrinter.println(firstName);
        outputPrinter.println(lastName);
        outputPrinter.println(passWord);

        String accountStatus = inputReader.readLine();
        if(accountStatus.equals("loginSuccess")){
            gui.openNotification("Successfully logged in");
            String welcome = inputReader.readLine();
            portInstructions.setText(welcome);
            userId = inputReader.readLine();

            this.populateOnlineUsers();
            this.enterChatroom();

        }
        else if(accountStatus.equals("loginFail")){
            gui.openNotification("Login Failed!");
        }

    }

    public void createAccount(String fName, String lName, String pWord, Text portInstructions) throws IOException{
        outputPrinter.println("createAccount");
        outputPrinter.println(fName);
        outputPrinter.println(lName);
        outputPrinter.println(pWord);

        String accountStatus =  inputReader.readLine();
        if(accountStatus.equals("AccountCreated")){
            gui.openNotification("Account Successfully Created");
            String welcome = inputReader.readLine();
            portInstructions.setText(welcome);
            userId = inputReader.readLine();


            this.populateOnlineUsers();
            this.enterChatroom();

        }
        else if(accountStatus.equals("AccountFailure")){
            gui.openNotification("That account already exists!");
        }
        
    }

    public void enterChatroom(){
        outputPrinter.println("NEW_USER_ENTRANCE");
        System.out.println("Sent NEW USER");
        this.listenForMessages(); //LISTEN

    }

    public void populateOnlineUsers() throws IOException{
        int userNum = 1;
        try {
             userNum = Integer.parseInt(inputReader.readLine());
            System.out.println(userNum + "users coming in!");
        } catch (IOException e1) {
            e1.printStackTrace();
        }

     
            for(int i = 0; i<userNum; i++){
                String otherUser = inputReader.readLine();

                if(!otherUser.equals("Online Users Populated")){
                    gui.getOnlineUserList().getItems().add(otherUser);
                    System.out.println("Just added " + otherUser);
                }
               

            }
       
        }
    }


    

