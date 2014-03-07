------------------Group members------------------

Meng Huang (mhuang14)

Chengkan Huang (chuang85)

------------------Algorithm------------------

Simply follow the snapshot algorithm given by text book.

Creates a server thread and multiple "processes" threads as per given.

Processes send messages to each other in a random manner, and process#1 initiates snapshots once in a while during the running of program.

Each process records its state after it has received a marker(if it has not yet recorded). States are recorded by writing into local disk.

Program terminates automatically when the given number of snapshots have been taken place.

------------------Usage------------------

Import the folder as java project into Eclipse.

Find "Main.java" in package named "server". 

Run ihe file as java application.

Enter port number, number of processes and number of snapshots as required in console.

Then the program starts, all the running details would be printed out in the console.

After it has terminated, the recorded states will be recorded as files in the folder named "snapshot_result" under default directory.

And the format of the files strictly follows the instruction.

By running the "SearchTool.java" under package "utility", entering a snapshot sequence number, one can acquire all the infomation related to that snapshot, along with counts for total money and total widgets.



PLEASE NOTE: Every time before running the program, the "snapshot_result" folder will be automatically cleared.