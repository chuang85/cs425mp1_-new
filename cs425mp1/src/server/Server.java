package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

import message.Marker;
import message.Message;
import message.RegularMessage;
import process.Channel;

public class Server implements Runnable {

	ServerSocket myServer = null;
	Socket[] clientSocket;
	ObjectInputStream[] is;
	ObjectOutputStream[] os;
	int total_marker;
	// Message input. ConcurrentLinkedQueue is thread safe
	ConcurrentLinkedQueue<Message> message_queue = new ConcurrentLinkedQueue<Message>();

	public void reset_process() {
		for (int j = 1; j < Main.proc_num + 1; j++) {
			Main.p[j].hasRecordedState = false;
			Main.p[j].hasSendMarker = false;
		}
	}

	public void resetChannel() {
		for (int j = 1; j < Main.proc_num + 1; j++) {
			for (int k = 1; k < Main.proc_num + 1; k++) {
				if (j != k) {
					Main.channel[j][k].turnOffRecord();
				}
			}
		}
	}

	public void sendMarker(int sequenceNum, int from) {
		for (int i = 1; i < Main.proc_num + 1; i++) {
			if (i != from) {
				Marker m = new Marker(sequenceNum, from, i);
				try {
					Main.p[from].client.os.writeObject((Message) m);
					System.out.println(String.format(
							"P%d is sending marker to P%d", from, i));
					Main.p[from].client.os.flush();
				} catch (IOException e) {
					System.out.println(e);
				}
			}
		}
	}

	public void init_stamp() {
		for (int j = 1; j < Main.proc_num + 1; j++) {
			Main.logical[j] = 0;
			for (int k = 1; k < Main.proc_num + 1; k++) {
				Main.vector[j][k] = 0;
			}
		}
	}

	public void updateTimeStamp(Message m) {
		Main.logical[m.to] = Math.max(m.logicalM + 1, Main.logical[m.to] + 1);
		for (int j = 1; j < Main.proc_num + 1; j++) {
			if (j != m.to) {
				Main.vector[m.to][j] = Math.max(m.vectorM[j],
						Main.vector[m.to][j]);
			} else {
				Main.vector[m.to][j]++;
			}
		}
	}

	@Override
	public void run() {
		init_stamp();

		// init the client socket array and is os array
		clientSocket = new Socket[Main.proc_num + 1];
		is = new ObjectInputStream[Main.proc_num + 1];
		os = new ObjectOutputStream[Main.proc_num + 1];
		// create the server
		try {
			myServer = new ServerSocket(Main.port_num);
		} catch (IOException e) {
			System.out.println(e);
		}

		// wait for all the clients to connect
		System.out.println("Waiting for all the clients to connect... \n");
		int i = 1;
		while (true) {
			try {
				clientSocket[i] = myServer.accept();
				is[i] = new ObjectInputStream(clientSocket[i].getInputStream());
				os[i] = new ObjectOutputStream(
						clientSocket[i].getOutputStream());
				// send the process id to the connecting process
				os[i].writeObject((Integer) i);
				os[i].flush();
				i++;
			} catch (IOException e) {
				System.out.println(e);
			}
			if (i == Main.proc_num + 1)
				break;
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ie) {
			// Handle exception
		}
		System.out.println("Connection completed \n");

		// channel format channel[from][to] starting from 1,1
		Main.channel = new Channel[Main.proc_num + 1][Main.proc_num + 1];
		for (int j = 1; j < Main.proc_num + 1; j++) {
			for (int k = 1; k < Main.proc_num + 1; k++) {
				Main.channel[j][k] = new Channel(j, k);
			}
		}

		total_marker = Main.proc_num * (Main.proc_num - 1) + 1;
		// listen on clients
		Message agent;
		while (true) {
			// enqueue all the messages and markers, and put marker into channel
			for (int j = 1; j < Main.proc_num + 1; j++) {
				try {
					agent = (Message) is[j].readObject();
					message_queue.add(agent);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			while (!message_queue.isEmpty()) {
				agent = message_queue.poll();

				// message is a marker
				if (agent.isMarker()) {
					System.out.println(String.format(
							"P%d receiving marker from P%d", agent.to,
							agent.from));

					// update timestamp
					updateTimeStamp(agent);

					// stop send messages while recording state
					// p has not recorded its state yet
					if (Main.p[agent.to].hasRecordedState == false) {
						synchronized (this) {
							try {
								Main.p[agent.to].recordProcessState();
								Main.p[agent.to].hasRecordedState = true;
								System.out.println(String.format(
										"P%d has recorded state", agent.to));
								for (int j = 2; j < Main.proc_num + 1; j++) {
									if ((j != agent.to) && (j != agent.from)) {
										Main.channel[j][agent.to]
												.turnOnRecord();
										System.out.println(String.format(
												"C%d%d is turned on", j,
												agent.to));
									}
								}
								sendMarker(Main.sequence_num, agent.to);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						// turns on recording of messages arrving over other
						// incoming channels
					} else {
						Main.channel[agent.from][agent.to].turnOffRecord();
						Main.channel[agent.from][agent.to].recordChannelState();
					}
					total_marker--;
					// done with one snapshot
					if (total_marker == 0) {
						System.out.println("This snapshot is done\n\n");
						Main.snapshot_num--;
						Main.sequence_num++;
						reset_process();
						resetChannel();
						total_marker = Main.proc_num * (Main.proc_num - 1);
						Main.snapshot_on = false;
						if (Main.snapshot_num == 0)
							System.exit(1);
					}
				}
				// message is a regular message
				else {
					// if the channel's recording is on, store the message in
					// the channel
					if (Main.channel[agent.from][agent.to].outputCurrState())
						Main.channel[agent.from][agent.to].addMessage(agent);
					try {
						os[(int) agent.to].writeObject((RegularMessage) agent);
						os[(int) agent.to].flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
