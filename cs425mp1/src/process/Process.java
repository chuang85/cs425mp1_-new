package process;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import message.RegularMessage;
import server.Main;
import client.Client;

public class Process implements Runnable {

	public int id;
	public int widget;
	public int money;
	public boolean hasRecordedState;
	public boolean hasSendMarker;
	public Client client;

	public Process(int widget, int money) {
		this.widget = widget;
		this.money = money;
		hasRecordedState = false;
		hasSendMarker = false;
	}

	public synchronized void recordProcessState() throws IOException {
		String front = String.format(
				"id %d : snapshot %d : logical %d : vector ", id,
				Main.sequence_num, Main.logical[id]);
		String mid = "";
		for (int i = 1; i < Main.proc_num + 1; i++) {
			mid += String.valueOf(Main.vector[id][i]) + " ";
		}
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

	public void receiveMessage() throws ClassNotFoundException {
		RegularMessage my_m;
		while (true) {
			try {
				my_m = (RegularMessage) client.is.readObject();
				// update timestamp
				Main.logical[my_m.to] = Math.max(my_m.logicalM + 1,
						Main.logical[my_m.to] + 1);
				for (int j = 1; j < Main.proc_num + 1; j++) {
					if (j != my_m.to) {
						Main.vector[my_m.to][j] = Math.max(my_m.vectorM[j],
								Main.vector[my_m.to][j]);
					} else {
						Main.vector[my_m.to][j]++;
					}
				}
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

		// Start sanding thread
		new Thread(new ProcessSendThread(client.os, id, Main.proc_num)).start();

		try {
			receiveMessage();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
