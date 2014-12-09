/**
 * Web Crawler. Written by TJ Stein
 */
import java.util.LinkedList;
import java.util.ArrayList;
import java.io.*;

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
	public static void main(String[] args) throws IOException{
		String mode;
		int n = 0;
		ArrayList<String> urls = new ArrayList<String>();
		ArrayList<String> searchTerms = new ArrayList<String>();
		boolean verbose = false;
		if (args.length < 6){
			System.out.println("Not enough command line arguments. Usage: java WebCrawler <mode> <n> -url <urls...> -search <terms...>");
			System.exit(1);
		}
		mode = args[0];
		if (!mode.equals("pages") && !mode.equals("bytes")){
			System.out.println("Error: First Argument must be 'pages' or 'bytes' for pages/bytes per second");
			System.exit(1);
		}
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
		if (!args[2].equals("-url")){
			System.out.println("Error: no URLs entered. Must preface URLs with '-url' flag\nUsage: java WebCrawler <mode> <n> -url <urls...> -search <terms...>");
			System.exit(1);
		}

		int i = 3;
		while (i < args.length && !args[i].equals("-search")){

			if (args[i].matches("((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)"))
				urls.add(args[i]);
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
		i++;
		while (i < args.length && !args[i].equals("-v")){
			searchTerms.add(args[i].toLowerCase());
			i++;
		}

		if (i != args.length && args[i].equals("-v")){
			verbose = true;
		}

		CrawlerThread crawler = new CrawlerThread(mode, n, urls, searchTerms, verbose);
		crawler.start();

		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		String userInput;
		System.out.print("$ ");
		for(;;){
			userInput = stdIn.readLine();
			if (userInput.equals("quit")){
				System.out.println("Quitting.");
				crawler.terminate();
				System.exit(0);
			}
		}
	}
}