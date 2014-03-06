package utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import server.Main;

public class SearchTool {
	final File folder;
	Set<String> fileSet;
	static int totalMoney;
	static int totalWidget;

	public SearchTool(String folderPath) {
		folder = new File(folderPath);
		fileSet = new HashSet<String>();
	}

	public void searchAll(int n) throws IOException {
		String query = "snapshot " + String.valueOf(n);
		String fullPath;
		String currLine;
		BufferedReader br = null;
		String[] strArr = null;
		totalMoney = 0; 
		totalWidget = 0;
		for (String currFile : fileSet) {
			fullPath = Main.txtDirectory + currFile;
			// System.out.println(fullPath);
			br = new BufferedReader(new FileReader(fullPath));
			while ((currLine = br.readLine()) != null) {
				if (currLine.contains(query)) {
					System.out.println(currLine);
					strArr = currLine.split(" ");
					if (currLine.contains("message")) {
						totalMoney += Integer.valueOf(strArr[12]);
						totalWidget += Integer.valueOf(strArr[14]);
					} else {
					totalMoney += Integer.valueOf(strArr[7]);
					totalWidget += Integer.valueOf(strArr[9]);
					}
				}
			}
		}
		br.close();
	}

	private void listFilesForFolder() {
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				listFilesForFolder();
			} else {
				// System.out.println(fileEntry.getName());
				fileSet.add(fileEntry.getName());
			}
		}
	}

	public static void main(String[] args) throws IOException {
		SearchTool st = new SearchTool(Main.txtDirectory);
		st.listFilesForFolder();
		while (true) {
			System.out.println("\nEnter the snapshot id, enter '0' to quit ");
			Scanner scanner = new Scanner(System.in);
			int snapshotId = scanner.nextInt();
			if (snapshotId == 0) {
				break;
			}
			st.searchAll(snapshotId);
			System.out.println(String.format("total money = %d, total widgets = %d", totalMoney, totalWidget));
		}
	}
}
