package oy.tol.chatclient.turhat;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import oy.tol.chat.Message;
import oy.tol.chatclient.ChatClientDataProvider;
import oy.tol.chatclient.ChatTCPClient;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;





public class MainFrame extends JFrame implements ActionListener, ChatClientDataProvider {

    private static final String SERVER = "localhost:10000";

    private String currentServer = SERVER; // URL of the server without paths.
	private int serverPort = 10000; // The server port listening to client connections.
	private String nick = null; // Nickname, user can change the name visible in chats.
	private String currentChannel = "main";
	private ChatTCPClient tcpClient = null; // Client handling the requests & responses.
	private boolean running = true;

    JPanel left_menu;
    JPanel message_panel;
    JButton button1;
    JButton button2;
    JButton button3;
    JButton send_button;

    JTextField message_field;



    MainFrame(String configuration){
        initialize_frame();
        read_config(configuration);
    }



    public void initialize_frame(){
		JFrame frame = new JFrame();
		frame.setTitle("Chat client :)");
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        

        left_menu = new JPanel();
        left_menu.setBackground(Color.LIGHT_GRAY);
        left_menu.setPreferredSize(new Dimension(100, 100));
        left_menu.setLayout(new GridLayout(10, 1));

        frame.add(left_menu, BorderLayout.WEST);



        message_panel = new JPanel();
        message_panel.setBackground(Color.DARK_GRAY);
        message_panel.setPreferredSize(new Dimension(100, 100));

        frame.add(message_panel, BorderLayout.SOUTH);

		

        button1 = new JButton("1");
        button2 = new JButton("2");
        button3 = new JButton("3");
        send_button = new JButton("send");

        button1.addActionListener(this);
        button2.addActionListener(this);
        button3.addActionListener(this);
        send_button.addActionListener(this);

        
        left_menu.add(button1);
        left_menu.add(button2);
        left_menu.add(button3);

        message_field = new JTextField();
        message_field.setPreferredSize(new Dimension(400, 50));

        message_panel.add(message_field);
        message_panel.add(send_button);

        frame.setVisible(true);
	
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == button1){
            System.out.println("button 1 pressed");
        }
        if(e.getSource() == button2){
            System.out.println("button 2 pressed");
        }
        if(e.getSource() == button3){
            System.out.println("button 3 pressed");
        }
        if(e.getSource() == send_button){
            String message = message_field.getText();
            if(message != null && !message.trim().isEmpty()){
                tcpClient.postChatMessage(message);
                System.out.println("message sent: " + message);
                message_field.setText("");
            }
        }
    }






    public void read_config(String configFile){
        try{
            readConfiguration(configFile);
            if (null == nick) {
                System.out.println("!! Provide a nick in settings");
            }
            tcpClient = new ChatTCPClient(this);
            new Thread(tcpClient).start();
        }catch (Exception e) {
            System.out.println(" *** ERROR : " + e.getMessage());
        }
    }

    private void readConfiguration(String configFileName) throws FileNotFoundException, IOException {
		System.out.println("Using configuration: " + configFileName);
		File configFile = new File(configFileName);
		Properties config = new Properties();
		FileInputStream istream;
		istream = new FileInputStream(configFile);
		config.load(istream);
		String serverStr = config.getProperty("server", "localhost:10000");
		String [] components = serverStr.split(":");
		if (components.length == 2) {
			serverPort = Integer.parseInt(components[1]);
			currentServer = components[0];
		} else {
			System.out.println("Invalid server address in settings");
		}
		nick = config.getProperty("nick", "");
		if (config.getProperty("usecolor", "false").equalsIgnoreCase("true")) {
			//useColorOutput = true;
		}
		istream.close();
	}



    @Override
    public String getServer() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getServer'");
    }



    @Override
    public int getPort() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPort'");
    }



    @Override
    public String getNick() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getNick'");
    }



    @Override
    public boolean handleReceived(Message message) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handleReceived'");
    }



    @Override
    public void connectionClosed() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'connectionClosed'");
    }

    
}
