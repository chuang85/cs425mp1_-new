package message;

import java.io.Serializable;

import server.Main;

public abstract class Message implements Serializable {
	/**
	 * Auto-generated serial number.
	 */
	private static final long serialVersionUID = -6851812906831068726L;
	public int from;
	public int to;
	public int lamboM;
	public int[] vectorM;
	
	public Message(int from, int to) {
		this.from = from;
		this.to = to;
		vectorM = new int[Main.proc_num+1];
	}

	public boolean isMarker() {
		return this instanceof Marker;
	}

	public boolean isRegular() {
		return this instanceof RegularMessage;
	}
}
