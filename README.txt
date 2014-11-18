Name: TJ Stein
Date: 11/18/2014
Assignment: Peer to Peer Gnutella Style


-------
 USAGE
-------

- COMPILATION -

Provided is a basic Makefile, so you can compile the project via

 $ make

Or, if you'd rather not use the Makefile:

 $ javac *.java

To clean the project, you can run

 $ make clean

Or, again, if you'd not use the Makefile,

 $ rm -rf *.class

To run the program, the syntax is ([] denotes optional):

 $ java PTP <port> <file directory> [<neighbor_ip_1:port> ... <neighbor_ip_n:port>]

Note that <neighbor_ip_x:port> are nodes already running in the network. My program automatically 
adds new neighbors to existing nodes, so there is no worrying about not having 2 way neighbor relations.

While the program is running, the PTP class will simulate a terminal for the user to use. 
The list of possible commands are:
- help 					prints a help dialog
- quit 					closes the node gracefully
- ping 					prints a 'ping' message on all neighbors
- print neighbors 		prints a list of the node's neighbor's ip and port
- print files 			prints a list of files in the node's file directory
- find <file_prefix> 	starts the Gnutella search algorithm
- set ttl <new_ttl> 	sets a new ttl value, to resize how far the query goes

All these functions print something, both on the current node and other nodes. When something prints on 
another node, the terminal simulation returns to normal after printing, so the terminal aspect of the 
program is not broken by unexpected printing.

The main function of the project is, obviously, the searching function. To search, the synatx is:

 $ find <file_prefix>

At each node, the search function prints that it is searching, what for, the query and ttl values, and 
the status of finding files. If a node finds a file, it prints this text:

 * Found file <file_name>

And on the source node, it will print:

 * Found file <file_name> at IP <found_ip:port>

So, a full example of this would be:

												(contains file node2.txt)
[NODE 1]										[NODE 2]
 realterminal:$ java PTP 9991 node1files		realterminal:$ java PTP 9992 node2files
 $												$
 Adding new neighbor at 127.0.1.1:9992			Searching for files with prefix: node2
 												Query ID: 49438
 $ find node2 									TTL Value: 1

 Searching for file with prefix: node2  		* Found file node2.txt
 Query ID: 49438
 TTL Value: 2 									TTL is greater than 0, propegating search.

 File not found. 								$

 TTL is greater than 0, propegating search.

 $
 * Found file node2.txt at IP 127.0.1.1:9992

 $ 

--------
 DESIGN
--------

My project is separated into 2 Java files: the Node class and the PTP 'driver' class. 

 - PTP class

This class is simply the driver of the program. It parses out the command line arguments and creates 
the Node object. It then enters an infinite loop where it takes in user input. This is where the user 
can control the searching of nodes. From this input, we can ping neighbors, list neighbors, list files, 
change the ttl value, and start searching for files. This doesn't actually contain anything regarding 
the Peer to Peer Gnutella program, it is just the controller for the Node.

 - Node class

This class is the actual implementation of the Gnutella node. The class is actually made up of 3 classes: 
the Node class itself, a Server thread, and a Worker thread. The Node has references to an array of Files, 
its port, its host address, a list of neighboring IP address, a list of previous query IDs, and a reference 
to a server thread.

On construction, the Node sends a message to each of its neighbors so that the neighboring relationship 
becomes 2 ways (as the neighbor doesn't have a reference to the newly created node when it is created). 
In the Node class are various little helper methods for the user controlled methods callable from the PTP 
class, like ping, print neighbors, and print files.

After the node is created, the server it has refernce to is started. This server thread accepts the messages 
from other nodes and splits off a new worker thread so that the Node can accept multiple messages at the same 
time.

All messages sent from a Node are simply String delimited with pipes 
(ex: "search|<prefix>|<ttl>|<id>|<sourceip>"). The Worker thread checks the first value in the String and 
appropriately handles the information in the message.

The actual search is a method that takes in a file prefix, ttl value, query id, and the source node IP. 
If the query ID is already in the list, it just returns and ignores the search. If not, it adds it to the 
list of query IDs. It then decrements the ttl value by 1. The method then looks through all the files and 
sees if the prefix is in the list. If it finds the file, it sends a message to the source node with the 
full file name and the finding node's ip and port. If the new TTL value is greater than or equal to 0, 
the node then sends a message to all neighbors with the proper information.

Some things about my implementation: 
-In the worker thread, when it sees a search method, it sleeps a random amount of time from 0 to 1000 
milliseconds. This is to avoid issues where if a node with a valid file for a search has 2 different 
paths that are equidistant from the source node would send the 'found' message twice, as by the time 
the second message reached the node, it hadn't technically found the file yet. This can still happen, 
but the odds that both paths would reach the node now are much slimmer.
- Query IDs are simply a random number from 0 to 100,000. The Query IDs are to prevent repeat searches, 
and this range provides a very large chance that no two searches would have the same Query ID. This better 
way to do this, I feel, would be to somehow create an id from hashing stuff like the file prefix name, 
node IP and port, and search time, however, I did not have a lot of extra time to create a good function 
to create a number for that.
- When a node leaves the network via the "quit" command (as opposed to just ctrl-c or ctrl-d), it actually 
sends a message to the neighbors telling them to remove it from their neighbors list. This supports a more 
responsive network, so nodes can join and leave the network without having to reset the entire network.
- The previous point brings up something I wanted to implement, but didn't have the extra time to. I wanted 
to add functionality from the command line interface of the PTP class that supported adding and removing 
specific neighbors to a Node. This wouldn't have been tough, but as I said, I did not have a lot of extra 
time to add extra features for this project.