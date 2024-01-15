
package oy.tol.chatclient;


import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
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
import javax.swing.text.Utilities;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


import java.io.Console;
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

import com.diogonunes.jcolor.Ansi;
import com.diogonunes.jcolor.Attribute;

import oy.tol.chat.ChangeTopicMessage;
import oy.tol.chat.ChatMessage;
import oy.tol.chat.ErrorMessage;
import oy.tol.chat.ListChannelsMessage;
import oy.tol.chat.Message;
import oy.tol.chat.StatusMessage;


public class ChatClient extends JFrame implements ActionListener, ListSelectionListener, ChatClientDataProvider  {

    public static void main(String[] args) {
		System.out.println(args.length);
 
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

//private JTextArea message_area;
private JTextPane message_area2;
//private JPanel new_message_area;

private JTextField message_field;
private JList<String> channels_list;
private DefaultListModel<String> model;
private JScrollPane channel_list_scroll;

private JTextField change_nick_field;
private JMenuBar menubar;
private JMenu settingsMenu;
private JButton helpMenu;
private JMenuItem change_nick;

private JButton private_message;

private JPanel private_message_pane;
private JTextArea private_text;
private JTextField private_field;

private JPanel header_pane;
private JScrollPane header_scroll;
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

Icon welcome = new ImageIcon("kissa.jpg");


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
	//Icon update = new ImageIcon("update.PNG");
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
	//message_list_panel.setPreferredSize(new Dimension(100, 400));
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

	private_text = new JTextArea("  Yksityisviesti käyttäjälle:   ");
	private_field = new JTextField();
	private_message_pane.add(private_text, BorderLayout.WEST);
	private_message_pane.add(private_field, BorderLayout.CENTER);

	cancel_reply = new JButton("cancel");
	//cancel_reply.setPreferredSize(new Dimension(50, 20));
	cancel_reply.addActionListener(this);
	private_message_pane.add(cancel_reply, BorderLayout.EAST);
	cancel_reply.setVisible(false);

	message_field = new JTextField();
	message_field.setPreferredSize(new Dimension(630, 60));
	message_panel.add(message_field);

	//send button
	send_button = new JButton("send");
	send_button.addActionListener(this);
	message_panel.add(send_button);

	//list and panel and scroll to display channels
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
	
	//left menu text stuff, could be modified!!!!!??????
	JPanel update_channels_pane = new JPanel();
	JLabel update_channels_label = new JLabel("update channels:");
	//update_channels_pane.add(update_channels_label);
	//update_channels_pane.add(update_channels_button);
	//left_menu.add(update_channels_pane);
	
	JPanel new_channel_pane = new JPanel();
	JLabel new_channel_label = new JLabel ("new channel: ");
	//new_channel_pane.add(new_channel_label);
	//new_channel_pane.add(new_channel_button);
	//left_menu.add(new_channel_pane);
	left_menu.add(update_channels_button);
	left_menu.add(new_channel_button);

	//JPanel change_topic_pane = new JPanel();
	//JLabel change_topic_label = new JLabel ("Change topic: ");
	//change_topic_pane.add(change_topic_label);
	//change_topic_pane.add(change_topic_button);
	//left_menu.add(change_topic_pane);

	//reply = new JButton("reply");
	//reply.addActionListener(this);
	//left_menu.add(reply);
	//left_menu.add(private_message);
	//left_menu.add(new_channel_button);
	//left_menu.add(change_topic_button);

	//up header panel and stuff related to that
	header_pane = new JPanel();
	header_pane.setBackground(Color.LIGHT_GRAY);
	//header_pane.setPreferredSize(new Dimension(100, 40));
	header_pane.setLayout(new FlowLayout(FlowLayout.LEFT));
	//header_pane.setLayout(new GridLayout(1, 5));
	header_scroll = new JScrollPane();
	//header_scroll.add(header_pane);


	header_text1 = new JLabel("  Current channel: ");
	//header_text1.setForeground(Color.black);
	header_text2 = new JLabel("    Topic: ");
	//header_text2.setForeground(Color.black);

	Font font = new Font("Courier", Font.BOLD,12);

	header_text_topic = new JLabel("topic unknown");
	header_text_topic.setBackground(Color.WHITE);
	header_text_topic.setOpaque(true);
	header_text_topic.setFont(font);
	
	header_text_channel = new JLabel("main");
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

	//reply.addActionListener(this);
	//reply_private.addActionListener(this);

	frame.getRootPane().setDefaultButton(send_button);

	frame.setVisible(true);
}





private void new_client_joined(){
	JOptionPane.showOptionDialog(frame, 
		"Welcome to use chat\nPlease input your nickname after clicking 'ok'.", "Welcome",
		JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, welcome, null, null);
	change_nick();
	//change_nick();
	tcpClient.listChannels();

	//message_area.append("Welcome " + nick + "! Current channel is " + currentChannel + ". \n");
	//message_area.append() topic
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
	/* 
	if(e.getSource() == reply){
		//perus vastaus
		String replying_to = retrieveTextFromSameLine();

		System.out.println("normi reply");
		int selectionStart = message_area2.getSelectionStart();
        int selectionEnd = message_area2.getSelectionEnd();
		if (selectionStart != selectionEnd) {
			StyledDocument doc = message_area2.getStyledDocument();////tässä menossa?
            try {
                String selectedText = doc.getText(selectionStart, selectionEnd - selectionStart);
				System.out.println(selectedText);

			} catch (BadLocationException ex) {
                ex.printStackTrace();
            }
		}
	}
	if(e.getSource() == reply_private){
		//vastaus priva messageen
		System.out.println("priva reply");
	}*/
	if(e.getSource() == cancel_reply){
		reset_private_fields();
	}
}



private void reset_private_fields(){
	private_text.setText("Yksityisviesti käyttäjälle: ");
	private_field.setVisible(true);
	cancel_reply.setVisible(false);
	is_reply = false;
}



private void send_message(){
	
	String message = message_field.getText();
	if(message != null && !message.trim().isEmpty()){
		String to_user = private_field.getText();
		if(is_reply){


			message = "reply to:  " + message_to_reply.getNick() +"  ---   "+ message_to_reply.getMessage() + "\n ---  " + message;
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
	//JOptionPane.showMessageDialog(null, "Tähän tulee se ohje");
	JDialog help_dialog = new JDialog(frame);
	help_dialog.setSize(300, 400);
	help_dialog.setLocationRelativeTo(frame);
	help_dialog.setLayout(new FlowLayout());

	JTextArea text1 = new JTextArea("Help\n - Lähetä viesti painamalla 'send'\n " + 
		"- Vastaa viestiin painamalla 're'\n" +
		"- Lähetä yksityisviesti kirjoittamalla vastaanottajan nimimerkki. "+
		"Huom! Vastaanottajan tulee olla lähettänyt vähintään yhden viestin jtn.\n"+
		"- Liity kanavalle valitsemalla se vasemmassa reunassa olevasta listasta.\n"+
		"- Luo uusi kanava painamalla 'new channel' Kanavan aihe ei ole pakollinen. \n"+
		"- Voit vaihtaa minkä tahansa kanavan aiheen painamalla 'change'.\n"+
		"- Vaihda nimimerkki painamalla ensin 'Settings' ja sitten 'Change nick'."
		);
	text1.setLineWrap(true);
	text1.setWrapStyleWord(true);
	text1.setSize(new Dimension(280, 350));
	help_dialog.add(text1);
	
	JButton close = new JButton("close");
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
	//newChannel.setSize(new Dimension(20, 70));
	new_channel_dialog.add(newChannel);
	JLabel text2 = new JLabel("New channel topic:    ");
	new_channel_dialog.add(text2);
	JTextField newTopic = new JTextField("", 15);
	//newTopic.setSize(new Dimension(20, 70));
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




	//String newChannel = JOptionPane.showInputDialog(frame, "New channel name; ");
	
}




private void change_topic(){
	String newChannelTopic = JOptionPane.showInputDialog(frame, "New channel topic; ");
	if (newChannelTopic != null && !newChannelTopic.trim().isEmpty()) {
		tcpClient.changeTopicTo(newChannelTopic);

		//tcpClient.postChatMessage("Channel topic changed to " + newChannelTopic);
		//show_sent_message("Channel topic changed to '" + newChannelTopic  + "'");
	}else{
		tcpClient.changeTopicTo("no topic");
	}
}





private void change_nick(){
	//String newNick = JOptionPane.showInputDialog(frame, "New nickname; ", nick);
	//if (newNick != null && !newNick.trim().isEmpty()){
	//	nick = newNick;
	//}
	JDialog new_nick_dialog = new JDialog(frame);
	new_nick_dialog.setSize(300, 100);
	new_nick_dialog.setLocationRelativeTo(frame);
	new_nick_dialog.setLayout(new FlowLayout());

	JLabel text1 = new JLabel("New nickname:    ");
	new_nick_dialog.add(text1);
	JTextField newNick = new JTextField(nick, 15);
	//newChannel.setSize(new Dimension(20, 70));
	new_nick_dialog.add(newNick);

	JButton ok = new JButton("ok");
	//ok.setSize(new Dimension(20, 20));
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
	//message_area.append(now_str + "  " + nick + "(you)   ---    " + message + "\n");
	//JPanel new_message = new JPanel();
	//new_message.setLayout(new FlowLayout(FlowLayout.LEFT));
	//new_message.setPreferredSize(new Dimension(500, 20));
	//new_message.setBackground(Color.YELLOW);
	//JLabel text = new JLabel(now_str + "  " + nick + "(you)   ---    " + message + "\n");
	//text.setPreferredSize(new Dimension(1000, 20));
	//new_message.add(text);
	//new_message_area.add(new_message);

	Style style = message_area2.addStyle("SenderStyle", null);
    StyleConstants.setForeground(style, Color.BLUE);
    StyleConstants.setBold(style, true);

	reply = new JButton("re");
	reply.setPreferredSize(new Dimension(20, 20));
	//reply.addActionListener(this);
	reply.addActionListener(new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			replyToMessage(msg);
		}
	});

	try {
		doc.insertString(doc.getLength(), now_str + "  " + nick + " (you)   ---    " + message + "     ", style);
		//message_area2.insertComponent(new JButton("re"));
		StyleConstants.setComponent(style, reply);
		doc.insertString(doc.getLength(), "\n", style );
	} catch (BadLocationException e) {
		// TODO Auto-generated catch block
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
	if (config.getProperty("usecolor", "false").equalsIgnoreCase("true")) {
		//useColorOutput = true;
	}
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
	Style style = message_area2.addStyle("ReceivedStyle", null);
   	StyleConstants.setForeground(style, Color.BLACK);
    //StyleConstants.setBold(style, true);
	StyledDocument doc = message_area2.getStyledDocument();

	boolean continueReceiving = true;
		switch (message.getType()) {
			case Message.CHAT_MESSAGE: {
				if (message instanceof ChatMessage) {
					ChatMessage msg = (ChatMessage)message;
					if (msg.isDirectMessage()) {
						String message_got = msg.getMessage();
						String sender_nick = msg.getNick();
						String sent_time = timeFormatter.format(msg.getSent());
						reply_private = new JButton("re");
						reply_private.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								replyToMessage(msg);
							}
						});
						//privaatti message saatu
						
						//UUID msg.isReplyTo();

						try {
							doc.insertString(doc.getLength(), sent_time + "  " + sender_nick + " (yksityisviesti)   ---    " + message_got + "     ", style);
							StyleConstants.setComponent(style, reply_private);
							doc.insertString(doc.getLength(), "\n", style );
						} catch (BadLocationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						frame.setVisible(true);
						//message_area.append(sent_time + "  private message from:  " + sender_nick + "   ------   " + message_got + "\n");

					} else {
						//normi message saatu
						String message_got = msg.getMessage();
						String sender_nick = msg.getNick();
						String sent_time = timeFormatter.format(msg.getSent());
						reply = new JButton("re");
						reply.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								replyToMessage(msg);
							}
						});
						

						
						//message_area.append(sent_time + "  " + sender_nick + "   ------   " + message_got + "\n");

						try {
							doc.insertString(doc.getLength(), sent_time + "  " + sender_nick + "    ---    " + message_got + "    ", style);
							//message_area2.insertComponent(new JButton("reply"));
							StyleConstants.setComponent(style, reply);
							doc.insertString(doc.getLength(), "\n", style );
						
						} catch (BadLocationException e) {
							// TODO Auto-generated catch block
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
					System.out.println(channels);
					update_channels(channels);
					
				}
				break;
			}

			case Message.CHANGE_TOPIC: {
				ChangeTopicMessage msg = (ChangeTopicMessage)message;
				//vaihda aihe
				//message_area.append("aihe vaihdettu: " + msg.getTopic() + "\n");
				header_text_topic.setText(msg.getTopic());

				break;
			}

			case Message.STATUS_MESSAGE: {
				StatusMessage msg = (StatusMessage)message;
				//message_area.append("status: " + msg.getStatus() + "\n");
				//näytä status
				break;
			}

			case Message.ERROR_MESSAGE: {
				ErrorMessage msg = (ErrorMessage)message;
				//message_area.append("error: " + msg.getError() + "\n");
				try {
					doc.insertString(doc.getLength(), "Error: " + msg.getError() + "\n", style);
				} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				frame.setVisible(true);
				//error
				System.out.println("joku error");
				if (msg.requiresClientShutdown()) {
					continueReceiving = false;
					running = false;
					System.out.println("Vissiin tapahtu jotain pahaa");
					JOptionPane.showMessageDialog(frame, "connection closed\nClose the window by clicking 'ok'");
					System.exit(0);
				}
				break;
			}

			default:
				try {
						doc.insertString(doc.getLength(), "Unknown message type from server\n", style);
				} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("Unknown message type from server.");
				break;
		}
		//printPrompt(LocalDateTime.now(), nick, "", colorMsg);
		return continueReceiving;
}


private void replyToMessage(ChatMessage message) {
	is_reply = true;
	//String originalMessage = message.getMessage();
	message_to_reply = message;
	if(message.isDirectMessage()){
		message_to_reply.setRecipient(message.getNick());
	}

	//System.out.println(originalMessage);
 
	private_text.setText("  reply to:   " + message.getNick() + "  --  " + message.getMessage());
	private_field.setVisible(false);
	cancel_reply.setVisible(true);
	


	//String replyContent = JOptionPane.showInputDialog("Reply to: " + originalMessage);
	//String sender = "User";
	//Message replyMessage = new Message(sender, replyContent);

	//messages.add(replyMessage);

	// Update chat panel
	//addMessageToChatPanel(sender + ": Reply to " + originalMessage + ": " + replyContent);
	//updateReplyToComboBox();
}

/* 
private String retrieveTextFromSameLine() {
        int caretPosition = message_area2.getCaretPosition();

        try {
            // Find the start and end offsets of the current line
            int startOffset = Utilities.getRowStart(message_area2, caretPosition);
            int endOffset = Utilities.getRowEnd(message_area2, caretPosition);

            // Extract the text from the current line
            String lineText = message_area2.getText(startOffset, endOffset - startOffset);
			return lineText;
            // Process the extracted text (in this example, simply display it)
            //JOptionPane.showMessageDialog(this, "Text on the same line: \n" + lineText);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
		return "ei onnistunu";
    }
*/





private void update_channels(List<String> channels){
	Style style = message_area2.addStyle("ReceivedStyle", null);
   	StyleConstants.setForeground(style, Color.MAGENTA);
    StyleConstants.setBold(style, true);
	StyledDocument doc = message_area2.getStyledDocument();
	List<String> temp = new ArrayList<>();
	
	for (int i = 0; i<channels.size(); i++){

		String[] splitted = channels.get(i).split(" ");
		String channelName = splitted[0];
		temp.add(channelName);

		if(!model.contains(channelName)){
			//message_area.append("New channel " + channelName + " added.\n");
			try {
				doc.insertString(doc.getLength(), "New channel " + channelName + " added.\n" , style);
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			frame.setVisible(true);
			model.add(0, channelName);
		}
	}
	 
	if(model.size() > temp.size()){
		for (int i = 0; i<model.size();i++){
			if(!temp.contains(model.get(i))){
				//message_area.append("Channel " + model.get(i) + " has 0 users so it is deleted. \n");
				
				try {
					doc.insertString(doc.getLength(),  "Channel " + model.get(i) + " has 0 users so it is deleted. \n", style);
				} catch (BadLocationException e) {
					// TODO Auto-generated catch block
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
	if (!e.getValueIsAdjusting()) {//This line prevents double events
		//channel listasta valittu jotain
		System.out.println("listasta " + channels_list.getSelectedValuesList());
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
	//message_area.append("You changed channel to " + newChannel + "\n");
	try {
		doc.insertString(doc.getLength(), "You changed channel to " + newChannel + "\n", style);
	} catch (BadLocationException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

	frame.setVisible(true);


	header_text_channel.setText(newChannel);
}

}