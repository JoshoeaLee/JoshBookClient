package Client;

import java.io.BufferedReader;
import java.io.IOException;

import javafx.scene.control.ChoiceBox;

public class OnlinePopulationThread  extends Thread{

    ChoiceBox<String> onlineUsers; 
    BufferedReader inputReader;


    public OnlinePopulationThread(ChoiceBox<String> onlineUsers, BufferedReader inputReader){

        this.onlineUsers = onlineUsers;
        this.inputReader = inputReader;
    }


    public void run(){
        String otherUser="";
        try {
            otherUser = inputReader.readLine();
            System.out.println("Read first line");
            System.out.println(otherUser);
        } catch (IOException e1) {
            e1.printStackTrace();
        }



        while(!otherUser.equals("Online Users Populated")){
            try {
                onlineUsers.getItems().add(otherUser);
                System.out.println("Just added " + otherUser);
                otherUser = inputReader.readLine();
                System.out.println(otherUser);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

   

    
}
