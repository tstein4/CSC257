import java.util.ArrayList;
import java.util.Random;
import java.io.*;

public class PTP{
	static int TTL = 2;

	public static void main(String[] args) throws IOException{
		if (args.length < 2){
			System.out.println("Usage: java Node <port> <file_directory> <list_of_node_ips>");
			System.exit(0);
		}
		ArrayList<String> ipList = new ArrayList<String>();
		if (args.length > 2){
			for (int i = 2; i < args.length; i++){
				if (args[i].indexOf(':') < 0)
					ipList.add(args[i]+":8765");
				else
					ipList.add(args[i]);
			}
		}
		int port=8765;
		try{
			port = Integer.parseInt(args[0]);
		}
		catch (NumberFormatException e){
			System.out.println("Error: port number not parseable.");
			System.exit(1);
		}
		File dir = new File(args[1]);
		File[] files = new File[0];
		if (dir.isDirectory()){
			files = dir.listFiles();
		}
		else{
			System.out.println("Error: not a valid directory.");
			System.exit(1);
		}
		Node node = new Node(port, files, ipList);
		node.startServer();

		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		String userInput;
		System.out.print("$ ");
		for(;;){
			userInput = stdIn.readLine();
			if (userInput.equals("quit")){
				System.out.println("Quitting.");
				node.quit();
				System.exit(0);
			}
			else if (userInput.equals("print neighbors")){
				node.printNeighbors();
				System.out.print("\n$ ");
			}
			else if (userInput.equals("print files")){
				node.printFiles();
				System.out.print("\n$ ");
			}
			else if (userInput.equals("ping")){
				node.pingNeighbors();
				System.out.print("\n$ ");
			}
			else if (userInput.indexOf("find ") == 0){
				String prefix = userInput.substring(userInput.indexOf(" ") + 1);
				int id = getRandomInt();
				node.search(prefix, TTL, id, node.getIP());
			}
			else if (userInput.equals("help") || userInput.equals("?")){
				System.out.println("Welcome to the help output.");
				System.out.println("Commdands:");
				System.out.println("quit            - This closes the node, and informs all neighbors that the node is no longer in the network.");
				System.out.println("print neighbors - This prints the IP Addresses and ports of the node's neighbors");
				System.out.println("print files     - This prints the files in the node's directory.");
				System.out.println("ping            - This sends a message to all neighbors and prints a \"ping\" in their output.");
				System.out.println("find <prefix>   - This command initiates the Gnutella flooding peer-to-peer search.");
				System.out.println("                  All nodes print their progress, and include TTL value, Query ID, and file prefix.");
				System.out.println("                  Hits are reported both in the finding node and the initial node.");
				System.out.print("\n$ ");
			}
			else if (userInput.indexOf("set ttl ") == 0){
				String[] vals = userInput.split(" ");
				try{
					TTL = Integer.parseInt(vals[2]);
				}
				catch(NumberFormatException e){
					System.out.println("Error: can't parse new ttl value.");
					System.out.print("\n$ ");
				}
			}
		}
	}
	public static int getRandomInt(){
		Random rand = new Random();
		return rand.nextInt(100000);
	}
}