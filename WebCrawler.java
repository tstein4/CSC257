/**
 * Web Crawler. Written by TJ Stein
 */
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.LinkedList;
import java.util.ArrayList;

public class WebCrawler {

	/*
	Usage: java WebCrawler mode n url [url...]
	mode = pages or bytes per second (b for bytes, p for pages)
	n = mode per second (int)
	url = starting url
	[url...] = extra starting urls
	example: java WebCrawler p 5 www.cs.rochester.edu
	 */
	public static void main(String[] args) {
		if (args.length < 3){
			System.out.println("Not enough command line arguments. Usage: java WebCrawler mode n url [url...]");
			System.exit(1);
		}
		String mode = args[0];
		if (!mode.equals("p") && !mode.equals("b")){
			System.out.println("Error: First Argument must be 'p' or 'b' or pages/bytes per second");
			System.exit(1);
		}
		int n = -1;
		try{
			n = Integer.parseInt(args[1]);
			if (n <= 0){
				System.out.println("Error: WebCrawler can't run at 0 or negative bytes/page per second");
				System.exit(1);
			}
		} catch (NumberFormatException e){
			System.out.println("Error: 2nd argument must be an interger");
			System.exit(1);
		}

		LinkedList<String> q = new LinkedList<String>();
		for (int i = 2; i < args.length; i++){
			if (args[i].matches("(http://|https://)?(www.)?(\\w+\\.)+(\\w+)"))
				q.add(args[i]);
			else{
				System.out.println("Error: Not a valid URL");
				System.exit(1);
			}
		}

		ArrayList<String> set = new ArrayList<String>();

		while (!q.isEmpty()){
			String v = q.pop();
			System.out.println(v);

			// Get Website

			// String input = in.readAll();

			// String regexp = "http://(\\w+\\.)*(\\w+)";
			// Pattern pattern = Pattern.compile(regexp);
			// Matcher matcher = pattern.matcher(input);

			// while (matcher.find()){
				// String w = matcher.group();
				// if (!set.contains(w)){
					// q.enqueue(w);
					// set.add(w);
				// }
			// }
		}
	}

}