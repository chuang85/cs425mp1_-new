package server;

import java.io.File;
import java.util.Scanner;

import process.Channel;
import process.Process;

public class Main {

	public static int port_num;
	public static int proc_num = 0;
	public static int snapshot_num = 0;
	public static boolean snapshot_on = false;
	public static int sequence_num = 1;
	public static Process[] p;
	public static final String txtDirectory = System.getProperty("user.dir")
			+ "\\snapshot_result\\";

	public static Channel[][] channel;

	public static void main(String args[]) {
		clearDirectory();

		Server server = new Server();

		// input the port number
		System.out.println("Enter the port number : ");
		Scanner scanner = new Scanner(System.in);
		port_num = scanner.nextInt();

		// input for the number of processes need
		System.out.println("Enter the process number : ");
		proc_num = scanner.nextInt();
		// proc_num = proc_num;

		// input number of snapshots
		System.out.println("Enter the snapshot number : ");
		snapshot_num = scanner.nextInt();
		// snapshot_num = snapshot_num;

		// the process array, starting from index 1 !!!!!!!!!!!!!!!!!!!!!!
		p = new Process[proc_num + 1];
		for (int i = 1; i < proc_num + 1; i++) {
			// give each process 100 money and 100 wigets to start
			p[i] = new Process(10, 10);
		}
		new Thread(server).start();

		for (int i = 1; i < proc_num + 1; i++) {
			new Thread(p[i]).start();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ie) {
				// Handle exception
			}
		}

	}

	private static void clearDirectory() {
		File dir = new File(txtDirectory);
		for (File file : dir.listFiles())
			file.delete();
	}
}
