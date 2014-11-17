import java.util.ArrayList;
import java.util.Random;
import java.io.*;
import java.net.*;

public class Node{
	File[] files;
	int port;
	String hostAddress;
	ArrayList<String> neighborIPs;
	ArrayList<Integer> searchIDs;
	ServerThread sThread;

	public Node(int port, File[] files, ArrayList<String> neighborIPs){
		this.files = files;
		searchIDs = new ArrayList<Integer>();
		this.neighborIPs = neighborIPs;
		this.port = port;
		try{
			hostAddress = InetAddress.getLocalHost().getHostAddress();
		}
		catch(IOException e){
			hostAddress = "127.0.0.1";
			System.out.println("Error: can't find host address");
		}
		for (String ip : neighborIPs){
			sendToIP(ip, "add|"+hostAddress+":"+port);
		}
	}

	public void startServer(){
		sThread = new ServerThread(port);
		sThread.start();
	}

	public void stopServer(){
		sThread.terminate();
	}

	public void quit(){
		stopServer();
		for (String ip: neighborIPs){
			sendToIP(ip, "remove|"+hostAddress+":"+port);
		}
	}

	public void pingNeighbors(){
		for (String ip : neighborIPs){
			sendToIP(ip, "ping");
		}
	}

	public void printNeighbors(){
		for (String ip : neighborIPs){
			System.out.println(ip);
		}
	}

	public void printFiles(){
		for (File file : files){
			System.out.println(file.getName());
		}
	}

	public String getIP(){
		return hostAddress+":"+port;
	}

	public int searchFiles(String filePrefix){
		for (int i = 0; i < files.length; i++){
			if (files[i].getName().indexOf(filePrefix) == 0){
				return i;
			}
		}
		return -1;
	}

	public void search(String filePrefix, int ttl, int id, String sourceIP){
		if (searchIDs.contains(id)){
			return;
		}
		searchIDs.add(id);
		int newTTL = ttl-1;
		System.out.println("\nSearching for files with prefix: " + filePrefix);
		System.out.println("Query ID: " + id);
		System.out.println("TTL Value: " + ttl);
		int index = searchFiles(filePrefix);
		if (index != -1){
			System.out.println("\n* Found file " + files[index].getName());
			sendToIP(sourceIP, "found|"+files[index].getName()+"|"+hostAddress+":"+port);
		}
		else{
			System.out.println("\nFile not found.");
		}
		if (newTTL >= 0){
			System.out.println("\nTTL is greater than 0, propegating search.");
			for (String ip : neighborIPs){
				sendToIP(ip, "search|"+filePrefix+"|"+newTTL+"|"+id+"|"+sourceIP);
			}
		}
		System.out.print("\n$ ");
	}

	public void sendToIP(String ip, String message){
		String hostname = ip.substring(0, ip.indexOf(":"));
		int portNumber = Integer.parseInt(ip.substring(ip.indexOf(":")+1));
		try (
			Socket sock = new Socket(hostname, portNumber);
			PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
		){
			out.println(message);
		} catch(UnknownHostException e){
			System.out.println("UnknownHostException");
			System.exit(1);
		} catch (IOException e){
			e.printStackTrace();
			System.exit(1);
		}
	}

	class WorkerThread extends Thread{
		private Socket socket = null;

		public WorkerThread(Socket socket){
			this.socket = socket;
		}

		public void run(){
			try(
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			) {
				String inputLine, outputLine;
				while ((inputLine = in.readLine()) != null){
					if (inputLine.indexOf("add|") == 0){
						// Add syntax: add|<neighbor ip>
						String addr = inputLine.substring(inputLine.indexOf("|")+1);
						if (!neighborIPs.contains(addr)){
							System.out.println("\nAdding new neighbor at " + addr+"\n");
							System.out.print("$ ");
							neighborIPs.add(addr);
						}
					}
					else if (inputLine.indexOf("remove|")==0){
						// Remove syntax: remove|<neighbor ip>
						String addr=inputLine.substring(inputLine.indexOf("|")+1);
						if (neighborIPs.contains(addr)){
							System.out.println("\nRemoving neighbor at " + addr+"\n");
							System.out.print("$ ");
							neighborIPs.remove(addr);
						}
					}
					else if(inputLine.equals("ping")){
						System.out.println("\nGot pinged by " + socket.getInetAddress().toString().substring(1)+"\n");
						System.out.print("$ ");
					}
					else if (inputLine.indexOf("found|") == 0){
						// Found syntax: found|<filename>|<finding ip>
						String[] vals = inputLine.split("\\|");
						System.out.println("\n* Found file " + vals[1] + " at IP " + vals[2] + "\n");
						System.out.print("$ ");
					}
					else if (inputLine.indexOf("search|") == 0){
						// Search query syntax: search|<prefix>|<ttl>|<id>|<source ip>
						sleep(getRandomSleepTime()); // This is to prevent a node with 2 equal paths from reporting twice.
						String[] vals = inputLine.split("\\|");
						String prefix = vals[1];
						int ttl = Integer.parseInt(vals[2]);
						int id = Integer.parseInt(vals[3]);
						String sourceIP = vals[4];
						search(prefix, ttl, id, sourceIP);
					}
				}
			} catch (IOException e){
				e.printStackTrace();
			} catch (InterruptedException e){
				System.out.println("Interrupted.");
			}
		}

		public int getRandomSleepTime(){
			Random rand = new Random();
			return rand.nextInt(1000);
		}
	}

	class ServerThread extends Thread{
		private int portNumber;
		private boolean running;

		public ServerThread(int portNumber){
			this.portNumber = portNumber;
			running = true;
		}

		public void run(){
			try (
				ServerSocket serverSocket = new ServerSocket(portNumber);
			)
			{
				while (running){
					new WorkerThread(serverSocket.accept()).start();
				}
			} catch(IOException e){
				e.printStackTrace();
				System.exit(1);
			}
		}

		public void terminate(){
			running = false;
		}
	}
}