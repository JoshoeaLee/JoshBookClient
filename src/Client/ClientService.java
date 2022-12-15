package Client;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

/*
 * Allows JavaFX to dynamically update while threads etc. are constantly listening
 * Service help I used - https://www.youtube.com/watch?v=Xb6j8VfHxJo
 */
public class ClientService extends Service<String> {

    ClientController clientController;
    ClientGUI clientGUI;

    /*
     * Constructor for ClientService class.
     * A ClientController for the user is made.
     */
    public ClientService( ClientGUI clientGUI){

        this.clientGUI = clientGUI;
        try {
            this.clientController = new ClientController(clientGUI);
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        //This gets finished when the client service is ended
        setOnSucceeded(new EventHandler<WorkerStateEvent>(){
            @Override
            public void handle(WorkerStateEvent e){
                System.out.println(e.getSource().getValue());
            }
        });

    }

    @Override
    protected Task<String> createTask() {
        return new Task<String>(){
            @Override
            protected String call() throws Exception{
                return "Client Controller Ended";
            }
        };
    }

    /*
     * Close client controller.
     */
    public void stopClient() throws Exception{
        if(clientController!=null){
            clientController.closeClient();
        }

    }

    public ClientController getController(){
        return clientController;
    }
    
}
