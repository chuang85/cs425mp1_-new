package process;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import server.Main;

import message.Message;
import message.RegularMessage;

public class Channel {

	int from, to;
	public Queue<Message> messageQueue;
	boolean recordOn;

	public Channel(int from, int to) {
		this.from = from;
		this.to = to;
		messageQueue = new LinkedList<Message>();
		recordOn = false;
	}

	public void addMessage(Message m) {
		messageQueue.offer(m);
	}

	public void removeMessage() {
		messageQueue.poll();
	}

	public void turnOnRecord() {
		recordOn = true;
	}

	public void turnOffRecord() {
		recordOn = false;
	}

	public synchronized void recordChannelState() {
		int totalMoney = 0;
		int totalWidget = 0;
		while (!messageQueue.isEmpty()) {
			RegularMessage rm = (RegularMessage) messageQueue.poll();
			totalMoney += rm.money;
			totalWidget += rm.widget;
		}
		String content = String.format(
				"id %d : snapshot %d : message %d to %d : money %d widgets %d",
				to, Main.sequence_num, from, to, totalMoney, totalWidget);
		String filePath = Main.txtDirectory + "channel_" + from + to + ".txt";
		File file = new File(filePath);
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.newLine();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(String.format("C%d%d has recorded state", from, to));
	}

	public boolean outputCurrState() {
		return recordOn;
	}

}