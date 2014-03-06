package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import message.RegularMessage;

public class Client {
	Socket client = null;
	public ObjectOutputStream os = null;
	public ObjectInputStream is = null;

	public Client(String hostName, int port_num) {
		try {
			client = new Socket(hostName, port_num);
			os = new ObjectOutputStream(client.getOutputStream());
			is = new ObjectInputStream(client.getInputStream());
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host:" + hostName);
		} catch (IOException e) {
			System.err
					.println("Couldn't get I/O for the connection to: hostname");
		}

	}

	public int getID() throws ClassNotFoundException {
		System.out.println("client getting connection, getting process id \n");
		int id = 0;
		while (id == 0) {
			try {
				id = (Integer) is.readObject();

			} catch (IOException e) {
				System.out.println(e);
			}
		}
		System.out.println("This is the process's ID " + id);
		return id;
	}
}
