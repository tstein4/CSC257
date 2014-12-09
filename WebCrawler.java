/**
 * Web Crawler. Written by TJ Stein
 */
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.LinkedList;
import java.util.ArrayList;

public class WebCrawler {

	/*
	Usage: ava WebCrawler <mode> <n> -url <urls...> -search <terms...>
	mode = pages or bytes per second (b for bytes, p for pages)
	n = mode per second (int)
	-url = start URLs
	[url...] = starting urls
	-search = start search terms
	[terms...] search terms
	example: java WebCrawler pages 5 -url www.cs.rochester.edu -search words stuff things
	 */
	public static void main(String[] args) {
		if (args.length < 6){
			System.out.println("Not enough command line arguments. Usage: java WebCrawler <mode> <n> -url <urls...> -search <terms...>");
			System.exit(1);
		}
		String mode = args[0];
		if (!mode.equals("pages") && !mode.equals("bytes")){
			System.out.println("Error: First Argument must be 'pages' or 'bytes' for pages/bytes per second");
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
		if (!args[2].equals("-url")){
			System.out.println("Error: no URLs entered. Must preface URLs with '-url' flag\nUsage: java WebCrawler <mode> <n> -url <urls...> -search <terms...>");
			System.exit(1);
		}
		int i = 3;
		while (i < args.length && !args[i].equals("-search")){
			if (args[i].matches("(http://|https://)?(www.)?(\\w+\\.)+(\\w+)"))
				q.add(args[i]);
			else{
				System.out.println("Error: Not a valid URL");
				System.exit(1);
			}
			i++;
		}
		if (i == args.length){
			System.out.println("Error: No search terms. Must preface terms with '-search' flag\nUsage: java WebCrawler <mode> <n> -url <urls...> -search <terms...>");
			System.exit(1);
		}
		ArrayList<String> searchTerms = new ArrayList<String>();
		i++;
		while (i < args.length){
			searchTerms.add(args[i]);
			i++;
		}


		while (!q.isEmpty()){
			String v = q.pop();

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