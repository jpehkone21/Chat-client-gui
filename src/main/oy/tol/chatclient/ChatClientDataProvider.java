package oy.tol.chatclient;

import oy.tol.chat.Message;

public interface ChatClientDataProvider {
	String getServer();
	int getPort();
	String getNick();
	boolean handleReceived(Message message);
	void connectionClosed();
}
