
package oy.tol.chatclient;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import oy.tol.chat.ChangeTopicMessage;
import oy.tol.chat.ChatMessage;
import oy.tol.chat.ErrorMessage;
import oy.tol.chat.ListChannelsMessage;
import oy.tol.chat.Message;


public class ChatClient extends JFrame implements ActionListener, ListSelectionListener, ChatClientDataProvider  {

    public static void main(String[] args) {
 
    	if (args.length == 1) {								
        	try {
            	System.out.println("Launching ChatClient with config file " + args[0]);
				ChatClient client = new ChatClient();

				client.initialize_frame();
				client.read_config(args[0]);
				
				client.new_client_joined();
				

        	} catch (Exception e) {
            	System.out.println("Failed to run the ChatClient");
            	System.out.println("Reason: " + e.getLocalizedMessage());
            	//e.printStackTrace();
        	}
    	} else {
        	System.out.println("Usage: java -jar chat-client-jar-file chatclient.properties");
        	System.out.println("Where chatclient.properties is the client configuration file");
    	}

	}


private static final String SERVER = "localhost:10000";

private String currentServer = SERVER; // URL of the server without paths.
private int serverPort = 10000; // The server port listening to client connections.
private String nick = null; // Nickname, user can change the name visible in chats.
private String currentChannel = "main";
private ChatTCPClient tcpClient = null; // Client handling the requests & responses.
private boolean running = true;

private JFrame frame;
private JPanel left_menu;
private JPanel message_panel;
private JScrollPane message_list_panel;
private JButton update_channels_button;
private JButton new_channel_button;
private JButton change_topic_button;
private JButton send_button;

private JTextPane message_area2;

private JTextField message_field;
private JList<String> channels_list;
private DefaultListModel<String> model;
private JScrollPane channel_list_scroll;

private JMenuBar menubar;
private JMenu settingsMenu;
private JButton helpMenu;
private JMenuItem change_nick;


private JPanel private_message_pane;
private JTextArea private_text;
private JTextField private_field;

private JPanel header_pane;
private JLabel header_text1;
private JLabel header_text2;

private JLabel header_text_topic;
private JLabel header_text_channel;

private JPanel middle_pane;

private JButton reply= new JButton("reply");
private JButton reply_private= new JButton("reply");

private JButton cancel_reply;

private boolean is_reply = false;
private ChatMessage message_to_reply;

//Icons are from https://icons8.com/icons
Icon reply_icon = new ImageIcon("icons8-reply-50.png");
Icon send_icon = new ImageIcon("icons8-send-48.png");
Icon welcome = new ImageIcon("icons8-smiley-48.png");


public void initialize_frame(){
	//frame
	frame = new JFrame();
	frame.setTitle("Chat client :)");
	frame.setLayout(new BorderLayout());
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.setSize(800, 600);
	frame.setResizable(false);
	frame.setLocationRelativeTo(null);
	
	//left menu
	left_menu = new JPanel();
	left_menu.setBackground(Color.LIGHT_GRAY);
	left_menu.setPreferredSize(new Dimension(150, 100));
	//left_menu.setLayout(new GridLayout(3, 1));
	frame.add(left_menu, BorderLayout.WEST);

	//left menu buttons
	update_channels_button = new JButton("update channels");
	new_channel_button = new JButton("new channel");
	update_channels_button.addActionListener(this);
	new_channel_button.addActionListener(this);
	update_channels_button.setPreferredSize(new Dimension(140, 20));
	new_channel_button.setPreferredSize(new Dimension(140, 20));

	//up menu bar + settings + change nick + help
	menubar = new JMenuBar();
	frame.setJMenuBar(menubar);
	settingsMenu = new JMenu("Settings");
	menubar.add(settingsMenu);
	helpMenu = new JButton("Help");
	menubar.add(helpMenu);
	helpMenu.setPreferredSize(new Dimension(200, helpMenu.getPreferredSize().height));
	helpMenu.setOpaque(false);
	helpMenu.setContentAreaFilled(false);
	helpMenu.setBorderPainted(false);
	change_nick = new JMenuItem("Change nick");
	settingsMenu.add(change_nick);
	change_nick.addActionListener(this);
	helpMenu.addActionListener(this);

	//middle pane
	middle_pane = new JPanel();
	middle_pane.setBackground(Color.RED);
	middle_pane.setLayout(new BorderLayout());
	frame.add(middle_pane, BorderLayout.CENTER);

	//area to display messages
	message_area2 = new JTextPane();
	message_area2.setEditable(false);

	//scroll pane for messages
	message_list_panel = new JScrollPane(message_area2);
	message_list_panel.setBackground(Color.BLUE);
	message_list_panel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	middle_pane.add(message_list_panel, BorderLayout.CENTER);

	//panel down where you write messages
	message_panel = new JPanel();
	message_panel.setBackground(Color.DARK_GRAY);
	message_panel.setPreferredSize(new Dimension(100, 100));
	message_panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
	frame.add(message_panel, BorderLayout.SOUTH);

	//space for private message and reply stuff
	private_message_pane = new JPanel();
	private_message_pane.setBackground(Color.WHITE);
	private_message_pane.setPreferredSize(new Dimension(700, 20));
	private_message_pane.setLayout(new BorderLayout());
	message_panel.add(private_message_pane);

	private_text = new JTextArea("  Private message to user:   ");
	private_field = new JTextField();
	private_message_pane.add(private_text, BorderLayout.WEST);
	private_message_pane.add(private_field, BorderLayout.CENTER);

	cancel_reply = new JButton("cancel");
	cancel_reply.addActionListener(this);
	private_message_pane.add(cancel_reply, BorderLayout.EAST);
	cancel_reply.setVisible(false);

	//space where you write the message
	message_field = new JTextField();
	message_field.setPreferredSize(new Dimension(628, 60));
	message_panel.add(message_field);

	//send button
	send_button = new JButton(send_icon);
	send_button.setBackground(Color.WHITE);
	send_button.addActionListener(this);
	message_panel.add(send_button);

	//list, panel and scroll to display channels
	model = new DefaultListModel<String>();
	channels_list = new JList<String>(model);
	channels_list.addListSelectionListener(this);
	JPanel channel_list_pane = new JPanel();
	channel_list_pane.setLayout(new BorderLayout());
	channel_list_pane.add(channels_list, BorderLayout.NORTH);
	channel_list_scroll = new JScrollPane(channel_list_pane);
	channel_list_scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	channel_list_scroll.setPreferredSize(new Dimension(140, 300));
	left_menu.add(channel_list_scroll);

	//up header panel to display current channel and topic
	header_pane = new JPanel();
	header_pane.setBackground(Color.LIGHT_GRAY);
	header_pane.setLayout(new FlowLayout(FlowLayout.LEFT));

	header_text1 = new JLabel("  Current channel: ");
	header_text2 = new JLabel("    Topic: ");
	Font font = new Font("Courier", Font.BOLD,12);

	header_text_topic = new JLabel("topic unknown");
	header_text_topic.setBackground(Color.WHITE);
	header_text_topic.setOpaque(true);
	header_text_topic.setFont(font);
	
	header_text_channel = new JLabel(currentChannel);
	header_text_channel.setBackground(Color.white);
	header_text_channel.setOpaque(true);
	header_text_channel.setFont(font);

	header_pane.add(header_text1);
	header_pane.add(header_text_channel);
	header_pane.add(header_text2);
	header_pane.add(header_text_topic);

	change_topic_button = new JButton("change");
	change_topic_button.addActionListener(this);
	header_pane.add(change_topic_button);

	middle_pane.add(header_pane, BorderLayout.NORTH);

	left_menu.add(update_channels_button);
	left_menu.add(new_channel_button);

	//enter can be used to send messages
	frame.getRootPane().setDefaultButton(send_button);

	frame.setVisible(true);
}


@Override
public void actionPerformed(ActionEvent e) {
	
	if(e.getSource() == update_channels_button){
		tcpClient.listChannels();	
	}

	if(e.getSource() == new_channel_button){
		new_channel();
	}

	if(e.getSource() == change_topic_button){
		change_topic();
	}

	if(e.getSource() == send_button){
		send_message();
		reset_private_fields();
	}

	if(e.getSource() == change_nick){
		change_nick();
	}

	if(e.getSource() == helpMenu){
		open_help();
	}

	if(e.getSource() == cancel_reply){
		reset_private_fields();
	}
}


private void new_client_joined(){
	JOptionPane.showOptionDialog(frame, 
		"Welcome to chat\n Please input your nickname after clicking 'ok'.\n"+
		"If this is your first time, please read instructions by clicking 'Help'.", "Welcome",
		JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, welcome, null, null);
	change_nick();
	tcpClient.listChannels();
}




private void reset_private_fields(){
	private_text.setText("  Private message to user:   ");
	private_field.setVisible(true);
	cancel_reply.setVisible(false);
	is_reply = false;
}



private void send_message(){
	
	String message = message_field.getText();
	if(message != null && !message.trim().isEmpty()){
		String to_user = private_field.getText();
		if(is_reply){
			message = "Reply to:  " + message_to_reply.getNick() +" --- "+ message_to_reply.getMessage() + "\n-->  " + message;
			if (message_to_reply.isDirectMessage()){
				to_user = message_to_reply.directMessageRecipient();
			}
		}
		
		if(to_user!= null && !to_user.trim().isEmpty()){
			StringBuilder str = new StringBuilder();
			str.append("@");
			str.append(to_user);
			str.append(" ");
			str.append(message);

			message = str.toString();
			private_field.setText("");

		}

		tcpClient.postChatMessage(message);
		show_sent_message(message);
		message_field.setText("");
	}
}




private void open_help(){
	JDialog help_dialog = new JDialog(frame);
	help_dialog.setSize(300, 400);
	help_dialog.setLocationRelativeTo(frame);
	help_dialog.setLayout(new FlowLayout());

	JTextArea text1 = new JTextArea(" Instructions:  \n  - Send a message by clicking 'send'.\n" + 
		" - Reply to a message by clicking 're' next to the message.\n" +
		" - Send a private message by typing the recipient's nickname to the field after 'Private message to: '. "+
		" Note! The recipient must have sent at least one message before being able to receive a private message." + 
		" This applies also when a user changes their nickname.\n"+
		" - Join a channel by clikcing it's name from the list on the left side of the screen.\n"+
		" - Create a new channel by clicking 'new channel'. Only name of the channel is required. Topic field is optional and can be changed later. \n"+
		" - Change the topic of any channel by clicking 'change' next to the current topic.\n"+
		" - Change your nickname by clicking 'Change nick' from the settings. New nickanme can't be empty.\n"+
		" Remember to respect other users and have fun! :)"
		);
	text1.setLineWrap(true);
	text1.setWrapStyleWord(true);
	text1.setSize(new Dimension(280, 350));
	help_dialog.add(text1);
	
	JButton close = new JButton("Close");
	close.addActionListener(new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			help_dialog.dispose();
		}
	});

	help_dialog.add(close);
	help_dialog.setVisible(true);
}



private void new_channel(){

	JDialog new_channel_dialog = new JDialog(frame);
	new_channel_dialog.setSize(300, 200);
	new_channel_dialog.setLocationRelativeTo(frame);
	new_channel_dialog.setLayout(new FlowLayout());

	JLabel text1 = new JLabel("New channel name:    ");
	new_channel_dialog.add(text1);
	JTextField newChannel = new JTextField("", 15);

	new_channel_dialog.add(newChannel);
	JLabel text2 = new JLabel("New channel topic:    ");
	new_channel_dialog.add(text2);
	JTextField newTopic = new JTextField("", 15);

	new_channel_dialog.add(newTopic);

	JButton ok = new JButton("ok");
	ok.setSize(new Dimension(20, 20));
	ok.addActionListener(new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (newChannel.getText() == null || newChannel.getText().trim().isEmpty()){
				newChannel.setBorder(BorderFactory.createLineBorder(Color.red));
			}else{
				changeChannel(newChannel.getText());
				tcpClient.listChannels();
				if (newTopic.getText() != null && !newTopic.getText().trim().isEmpty()) {
					tcpClient.changeTopicTo(newTopic.getText());
				}
				new_channel_dialog.dispose();
			}
		}
	});
	JButton cancel = new JButton("cancel");
	cancel.addActionListener(new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			new_channel_dialog.dispose();
		}
	});

	new_channel_dialog.add(ok);
	new_channel_dialog.add(cancel);

	new_channel_dialog.setVisible(true);
}




private void change_topic(){
	String newChannelTopic = JOptionPane.showInputDialog(frame, "New channel topic; ");
	if (newChannelTopic != null && !newChannelTopic.trim().isEmpty()) {
		tcpClient.changeTopicTo(newChannelTopic);
	} else {
		tcpClient.changeTopicTo("no topic");
	}
}




private void change_nick(){

	JDialog new_nick_dialog = new JDialog(frame);
	new_nick_dialog.setSize(300, 100);
	new_nick_dialog.setLocationRelativeTo(frame);
	new_nick_dialog.setLayout(new FlowLayout());

	JLabel text1 = new JLabel("New nickname:    ");
	new_nick_dialog.add(text1);
	JTextField newNick = new JTextField(nick, 15);
	new_nick_dialog.add(newNick);

	JButton ok = new JButton("ok");
	ok.addActionListener(new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (newNick.getText() == null || newNick.getText().trim().isEmpty()){
				newNick.setBorder(BorderFactory.createLineBorder(Color.red));
			}else{
				nick = newNick.getText();
				tcpClient.listChannels();
				new_nick_dialog.dispose();
			}
		}
	});
	JButton cancel = new JButton("cancel");
	cancel.addActionListener(new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			new_nick_dialog.dispose();
		}
	});

	new_nick_dialog.add(ok);
	new_nick_dialog.add(cancel);

	new_nick_dialog.setVisible(true);
}




private void show_sent_message(String message){

	ChatMessage msg = new ChatMessage(nick, message);

	StyledDocument doc = message_area2.getStyledDocument();
	LocalDateTime now = LocalDateTime.now();
	String now_str = timeFormatter.format(now);

	Style style = message_area2.addStyle("SenderStyle", null);
    StyleConstants.setForeground(style, Color.BLUE);
    StyleConstants.setBold(style, true);

	reply = new JButton(reply_icon);
	reply.setBackground(Color.WHITE);
	reply.setPreferredSize(new Dimension(20, 20));
	reply.addActionListener(new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			replyToMessage(msg);
		}
	});

	try {
		doc.insertString(doc.getLength(), now_str + "  " + nick + " (you)   ---    " + message + "     ", style);
		StyleConstants.setComponent(style, reply);
		doc.insertString(doc.getLength(), "\n", style );
	} catch (BadLocationException e) {
		e.printStackTrace();
	}

	frame.setVisible(true);
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
	istream.close();
}



@Override
public String getServer() {
	return currentServer;
}



@Override
public int getPort() {
	return serverPort;
}



@Override
public String getNick() {
	return nick;
}

private static final DateTimeFormatter timeFormatter = new DateTimeFormatterBuilder()
			.appendValue(HOUR_OF_DAY, 2)
			.appendLiteral(':')
			.appendValue(MINUTE_OF_HOUR, 2)
			.optionalStart()
			.appendLiteral(':')
			.appendValue(SECOND_OF_MINUTE, 2)
			.toFormatter();




@Override
public boolean handleReceived(Message message) {
	
	StyledDocument doc = message_area2.getStyledDocument();

	boolean continueReceiving = true;
		switch (message.getType()) {
			case Message.CHAT_MESSAGE: {
				if (message instanceof ChatMessage) {
					ChatMessage msg = (ChatMessage)message;

					if (msg.isDirectMessage()) {
						//private message

						Style style = message_area2.addStyle("PrivateStyle", null);
   						StyleConstants.setForeground(style, Color.ORANGE);
    					StyleConstants.setBold(style, true);

						String message_got = msg.getMessage();
						String sender_nick = msg.getNick();
						String sent_time = timeFormatter.format(msg.getSent());
						reply_private = new JButton(reply_icon);
						reply_private.setBackground(Color.WHITE);
						reply_private.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								replyToMessage(msg);
							}
						});


						try {
							doc.insertString(doc.getLength(), sent_time + "  " + sender_nick + " (yksityisviesti) --- " + message_got + "     ", style);
							StyleConstants.setComponent(style, reply_private);
							doc.insertString(doc.getLength(), "\n", style );
						} catch (BadLocationException e) {
							e.printStackTrace();
						}

						frame.setVisible(true);

					} else {
						//normal message

						Style style = message_area2.addStyle("ReceivedStyle", null);
   						StyleConstants.setForeground(style, Color.BLACK);
    					StyleConstants.setBold(style, true);

						String message_got = msg.getMessage();
						String sender_nick = msg.getNick();
						String sent_time = timeFormatter.format(msg.getSent());
						reply = new JButton(reply_icon);
						reply.setBackground(Color.WHITE);
						reply.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								replyToMessage(msg);
							}
						});

						try {
							doc.insertString(doc.getLength(), sent_time + "  " + sender_nick + " --- " + message_got + "    ", style);
							StyleConstants.setComponent(style, reply);
							doc.insertString(doc.getLength(), "\n", style );
						} catch (BadLocationException e) {
							e.printStackTrace();
						}

						frame.setVisible(true);
					}
				}
				break;
			} 

			case Message.LIST_CHANNELS: {
				ListChannelsMessage msg = (ListChannelsMessage)message;
				List<String> channels = msg.getChannels();
				if (null != channels) {
					update_channels(channels);
				}
				break;
			}

			case Message.CHANGE_TOPIC: {
				ChangeTopicMessage msg = (ChangeTopicMessage)message;
				header_text_topic.setText(msg.getTopic());

				break;
			}

			case Message.STATUS_MESSAGE: {
				//StatusMessage msg = (StatusMessage)message;
				break;
			}

			case Message.ERROR_MESSAGE: {
				Style style = message_area2.addStyle("ErrorStyle", null);
   				StyleConstants.setForeground(style, Color.RED);
   				StyleConstants.setBold(style, true);
				ErrorMessage msg = (ErrorMessage)message;
				try {
					doc.insertString(doc.getLength(), "Error: " + msg.getError() + "\n", style);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}

				frame.setVisible(true);
				if (msg.requiresClientShutdown()) {
					continueReceiving = false;
					running = false;
					JOptionPane.showMessageDialog(frame, "connection closed\nClose the window by clicking 'ok'");
					System.exit(0);
				}
				break;
			}

			default:
				try {
					Style style = message_area2.addStyle("ReceivedStyle", null);
   					StyleConstants.setForeground(style, Color.GRAY);
    				StyleConstants.setBold(style, true);
					doc.insertString(doc.getLength(), "Unknown message type from server\n", style);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
				break;
		}

		return continueReceiving;
}


private void replyToMessage(ChatMessage message) {
	is_reply = true;
	message_to_reply = message;
	if(message.isDirectMessage()){
		message_to_reply.setRecipient(message.getNick());
	}
 
	private_text.setText("  reply to:   " + message.getNick() + "  --  " + message.getMessage());
	private_field.setVisible(false);
	cancel_reply.setVisible(true);
}



private void update_channels(List<String> channels){
	Style style = message_area2.addStyle("ReceivedStyle", null);
   	StyleConstants.setForeground(style, Color.MAGENTA);
    StyleConstants.setBold(style, true);
	StyledDocument doc = message_area2.getStyledDocument();
	List<String> temp = new ArrayList<>();
	
	for (int i = 0; i<channels.size(); i++){
		//checking if new channels have been created

		String[] splitted = channels.get(i).split(" ");
		String channelName = splitted[0];
		temp.add(channelName);

		if(!model.contains(channelName)){
			try {
				doc.insertString(doc.getLength(), "New channel " + channelName + " added.\n" , style);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}

			frame.setVisible(true);
			model.add(0, channelName);
		}
	}
	 
	if(model.size() > temp.size()){
		for (int i = 0; i<model.size();i++){
			//checking if channels need to be deleted from the list

			if(!temp.contains(model.get(i))){
				
				try {
					doc.insertString(doc.getLength(),  "Channel " + model.get(i) + " has 0 users so it is deleted. \n", style);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
				frame.setVisible(true);
				
				model.remove(i);
			}
		}
	} 
}

	


@Override
public void connectionClosed() {
	if (running){
		JOptionPane.showMessageDialog(frame, "connection closed");
		System.out.println("Connection closed");
	} 
	running = false;
}




@Override
public void valueChanged(ListSelectionEvent e) {
	if (!e.getValueIsAdjusting()) {
		if (channels_list.getSelectedValue() != null){
			changeChannel(channels_list.getSelectedValue());
			channels_list.clearSelection();
		}
	}
}



private void changeChannel(String selected) {
	Style style = message_area2.addStyle("ReceivedStyle", null);
   	StyleConstants.setForeground(style, Color.MAGENTA);
    StyleConstants.setBold(style, true);
	StyledDocument doc = message_area2.getStyledDocument();
	String[] splitted = selected.split(" ");
	String newChannel = splitted[0];
	tcpClient.changeChannelTo(newChannel);	
	currentChannel = newChannel;

	try {
		doc.insertString(doc.getLength(), "You changed channel to " + newChannel + "\n", style);
	} catch (BadLocationException e) {
		e.printStackTrace();
	}

	frame.setVisible(true);

	header_text_channel.setText(newChannel);
}

}