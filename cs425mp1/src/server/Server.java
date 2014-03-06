package server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

import process.Channel;
import message.Marker;
import message.Message;
import message.RegularMessage;

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

					while (Main.channel[j][k].messageQueue.poll() != null) {

					}
					Main.channel[j][k].turnOffRecord();
					Main.channel[j][k].hasPrintOff();
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

	@Override
	public void run() {
		// TODO Auto-generated method stub
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

		// send the snapshot number to process 1
		try {
			os[1].writeObject((Integer) Main.snapshot_num);
			os[1].flush();
		} catch (IOException e) {
			System.out.println(e);
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
			//	if (j != k) {
					Main.channel[j][k] = new Channel(j, k);
			//	}
			}
		}

		total_marker = Main.proc_num * (Main.proc_num - 1)+1;
		// listen on clients
		Message agent;
		while (true) {
			// enqueue all the messages and markers, and put marker into channel
			for (int j = 1; j < Main.proc_num + 1; j++) {
				try {
					// agent = (RegularMessage) is[j].readObject();
					// message_queue.add((RegularMessage) agent);
					agent = (Message) is[j].readObject();
					message_queue.add(agent);
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
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
					
					//////////////////////////////////////////////////stop send messages while recording state
					
					
					
					// p has not recorded its state yet
					if (Main.p[agent.to].hasRecordedState == false) {
						synchronized (this) {
						try {
							Main.p[agent.to].sendThread.suspend();
							Main.p[agent.to].recordProcessState();
							// TODO record it process state now
							Main.p[agent.to].hasRecordedState = true;
							System.out.println(String.format(
									"P%d has recorded state", agent.to))
									;
							for (int j = 2; j < Main.proc_num + 1; j++) {
								if ((j != agent.to) && (j != agent.from)) {
									Main.channel[j][agent.to].turnOnRecord();
									System.out.println(String.format(
											"C%d%d is turned on", j, agent.to));
								}
							}
							sendMarker(Main.sequence_num,agent.to);
							Main.p[agent.to].sendThread.resume();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						}
						
						// turns on recording of messages arrving over other
						// incoming channels
						
						

					} else {
						Main.channel[agent.from][agent.to].turnOffRecord();
						Main.channel[agent.from][agent.to].hasPrintOn();
						while (Main.channel[agent.from][agent.to].messageQueue
								.peek() != null) {
							RegularMessage rm = (RegularMessage) Main.channel[agent.from][agent.to].messageQueue
									.poll();
							System.out.println(String.format("C%d%d has recorded state", agent.from, agent.to));
							synchronized (this) {
								String content = String
										.format("id %d : snapshot %d : message %d to %d : money %d widgets %d",
												agent.to, Main.sequence_num,
												agent.from, agent.to, rm.money,
												rm.widget); // TODO ADD
															// TIMESTAMP

								String filePath = Main.txtDirectory
										+ "channel_" + agent.from + agent.to
										+ ".txt";
								File file = new File(filePath);
								try {
									if (!file.exists()) {
										file.createNewFile();
									}
									FileWriter fw = new FileWriter(
											file.getAbsoluteFile(), true);
									BufferedWriter bw = new BufferedWriter(fw);
									bw.write(content);
									bw.newLine();
									bw.close();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
					}
					total_marker--;
					// done with one snapshot
					if (total_marker == 0) {
						System.out.println("This snapshot is done");
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
						// System.out.println(String.format("Sending msg from %d to %d",
						// agent.from, agent.to));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

	}

}
