package process;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import message.Message;
import message.RegularMessage;

import org.omg.CORBA.Environment;

import server.Main;
import client.Client;

public class Process implements Runnable {

	public int id;
	public int widget;
	public int money;
	public int logicalTimestamp;
	public int[] vectorTimestamp;
	public boolean hasRecordedState;
	public boolean hasSendMarker;
	public Client client;
	public Thread sendThread;
	ProcessSendThread send;

	public Process(int widget, int money) {
		this.widget = widget;
		this.money = money;
		logicalTimestamp = 0;
		vectorTimestamp = new int[Main.proc_num];
		hasRecordedState = false;
		hasSendMarker = false;
	}

	public synchronized void recordProcessState() throws IOException {
		//String front = String.format("id %d : snapshot %d : logical %d : vector ", id, Main.sequence_num, logicalTimestamp);
		String front = String.format("id %d : snapshot %d ", id, Main.sequence_num, logicalTimestamp);
		String mid = "";
		String back = String.format(": money %d widgets %d", money, widget);
		String content = front + mid + back;

		String filePath = Main.txtDirectory + "process_" + id + ".txt";
		File file = new File(filePath);

		// if file doesnt exists, then create it
		if (!file.exists()) {
			file.createNewFile();
		}
		// true means append to existing file
		FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(content);
		bw.newLine();
		bw.close();

	}

	public void onReceivingMarker(Message m, Channel c) throws IOException {
		if (m.isMarker()) {
			if (!hasRecordedState) {
				recordProcessState();
				c.recordChannelState();
			}
		} else {
			System.out.println("Not a marker");
		}
	}

	public void updateStateOnReceiving(Message m) {
		if (m.isMarker()) {

		} else if (m.isRegular()) {

		} else {
			System.out.println("Who you are??");
		}
	}

	public void printCurrState() {
		System.out.println(String.format(
				"id=%d, widget=%d, money=%d, logical=%d", id, widget, money,
				logicalTimestamp));
	}

	public void receiveMessage() throws ClassNotFoundException {
		RegularMessage my_m;
		while (true) {
			try {
				my_m = (RegularMessage) client.is.readObject();
				synchronized (this) {
					money += my_m.money;
					widget += my_m.widget;
				}
			} catch (IOException e) {
				System.out.println(e);
			}
		}
	}

	@Override
	public void run() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ie) {
			// Handle exception
		}
		// use this client to communicate with server
		client = new Client("localhost", Main.port_num);
		try {
			id = client.getID();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println("This is ID " + id);

		send = new ProcessSendThread(client.os, id, Main.proc_num);
		sendThread = new Thread(send);
		sendThread.start();
		try {
			receiveMessage();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}

}
