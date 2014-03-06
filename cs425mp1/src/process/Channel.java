package process;

import java.util.LinkedList;
import java.util.Queue;

import message.Message;

public class Channel {

	int from, to;
	int id;
	public Queue<Message> messageQueue;
	boolean recordOn;
	boolean hasPrint;
	
	public Channel(int from, int to) {
		this.from = from;
		this.to = to;
		id = 10*from + to;
		messageQueue = new LinkedList<Message>();
		recordOn = false;
		hasPrint = false;
	}
	
	public void hasPrintOn()
	{
		hasPrint = true;
	}
	
	public void hasPrintOff()
	{
		hasPrint = false;
	}
	
	public boolean hasPrint()
	{
		boolean temp = hasPrint;
		return temp;
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
	
	public void recordChannelStateAsEmpty() {
		
	}
	
	public void recordChannelState() {
		printCurrState(); // TODO Modify this, should write state info into file, and widget & money should be recorded as well.
	}
	
	public boolean outputCurrState()
	{
		return recordOn;
	}
	
	public void printCurrState() {
		System.out.println(String.format("id=%d, isOn=", id, recordOn));
	}

}