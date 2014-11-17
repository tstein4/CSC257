import java.util.ArrayList;
import java.io.*;
import java.net.*;

public class Node{
	String dir;
	String hostname;
	int port;
	ArrayList<String> neighborIPs;
	int[] searchedFiles;
	ServerThread sThread;

	public Node(int port, String dir, ArrayList<String> neighborIPs){
		this.port = port;
		this.dir = dir;
		searchedFiles = new int[10];
		this.neighborIPs = neighborIPs;
		for (String ip : neighborIPs){
			sendToNeighbor(ip, "addneighbor|"+port);
		}
	}

	public void startServer(){
		sThread = new ServerThread(port);
		sThread.start();
	}

	public void stopServer(){
		sThread.terminate();
	}

	public void worker(){

	}

	public void startSeach(){

	}

	public void pingNeighbors(){
		for (String ip : neighborIPs){
			sendToNeighbor(ip, "ping");
		}
	}

	public void printNeighbors(){
		for (String ip : neighborIPs){
			System.out.println(ip);
		}
	}

	public void sendToNeighbor(String ip, String message){
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
			System.out.println(ip);
			e.printStackTrace();
			System.exit(1);
		}
	}

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
			System.exit(0);
		}
		Node node = new Node(port, args[1], ipList);
		node.startServer();

		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		String userInput;
		for(;;){
			userInput = stdIn.readLine();
			if (userInput.equals("quit")){
				System.out.println("Quitting.");
				System.exit(0);
			}
			else if (userInput.equals("print")){
				node.printNeighbors();
			}
			else if (userInput.equals("ping")){
				node.pingNeighbors();
			}
			else
				System.out.println(userInput);
		}
	}

	class WorkerThread extends Thread{
		private Socket socket = null;

		public WorkerThread(Socket socket){
			this.socket = socket;
		}

		public void run(){
			try(
				// PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			) {
				// System.out.println("ip: " +socket.getInetAddress().toString().substring(1));
				// System.out.println("port: " +socket.getPort());
				String inputLine, outputLine;
				while ((inputLine = in.readLine()) != null){
					if (inputLine.indexOf("addneighbor") >= 0){
						String port = inputLine.substring(inputLine.indexOf("|")+1);
						if (!neighborIPs.contains(socket.getInetAddress().toString().substring(1)+":"+port)){
							neighborIPs.add(socket.getInetAddress().toString().substring(1)+":"+port);
						}
					}
					else if(inputLine.indexOf("ping") >= 0){
						System.out.println("Got pinged by " + socket.getInetAddress().toString().substring(1));
					}
					// System.out.println(inputLine);
				}
			} catch (IOException e){
				e.printStackTrace();
			}
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
			try(
				ServerSocket serverSocket = new ServerSocket(portNumber);
			){
				while (running){
					// System.out.println("pls");
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