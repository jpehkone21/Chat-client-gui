package oy.tol.chatclient.turhat;

public class Launcher {
    

    public static void main(String[] args) {
     //   MainFrame mainframe = new MainFrame();
    
    //initialize_frame();
 
    if (args.length == 1) {								
        try {
            System.out.println("Launching ChatClient with config file " + args[0]);
            MainFrame mainframe = new MainFrame(args[0]);
            //ChatClient client = new ChatClient();
            //client.run(args[0]);
        } catch (Exception e) {
            System.out.println("Failed to run the ChatClient");
            System.out.println("Reason: " + e.getLocalizedMessage());
            e.printStackTrace();
        }
    } else {
        System.out.println("Usage: java -jar chat-client-jar-file chatclient.properties");
        System.out.println("Where chatclient.properties is the client configuration file");
    }

}

}
