import java.util.ArrayList;

public class Node{
	String dir;
	ArrayList<String> neighborIPs;

	public Node(String dir, ArrayList<String> neighborIPs){
		this.dir = dir;
		this.neighborIPs = neighborIPs;
	}

	public Node(String dir){
		this.dir = dir;
		neighborIPs = new ArrayList<String>();
	}

}