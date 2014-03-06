package message;

import java.io.Serializable;

public abstract class Message implements Serializable {
	/**
	 * Auto-generated serial number.
	 */
	private static final long serialVersionUID = -6851812906831068726L;
	public int from;
	public int to;

	public Message(int from, int to) {
		this.from = from;
		this.to = to;
	}

	public boolean isMarker() {
		return this instanceof Marker;
	}

	public boolean isRegular() {
		return this instanceof RegularMessage;
	}
}
