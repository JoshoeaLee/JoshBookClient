package Client;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

public class ClientService extends Service<String> {

    ClientController clientController;
    ClientGUI clientGUI;

    public ClientService( ClientGUI clientGUI){

        this.clientGUI = clientGUI;
        this.clientController = new ClientController(clientGUI);

        
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
                return "Client Controller Created";
            }



        };
    }

    public void stopClient() throws Exception{
        if(clientController!=null){
            clientController.closeClient();
        }

    }

    public ClientController getController(){
        return clientController;
    }
    
}
