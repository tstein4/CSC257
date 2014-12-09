import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.*;
import java.net.*;

public class CrawlerThread extends Thread{
	private ArrayList<String> visitedSites;
	private LinkedList<String> q;
	private PriorityQueue<SiteScore> pq;
	private String mode;
	private int limit;
	private ArrayList<String> terms;
	private boolean running;
	private boolean verbose;

	private long timestamp;
	private int counter;
	private int interval = 1000;

	public CrawlerThread(String mode, int limit, ArrayList<String> urls, ArrayList<String> terms, boolean verbose){
		this.mode = mode;
		this.limit = limit;
		this.terms = terms;
		this.verbose = verbose;
		visitedSites = new ArrayList<String>();
		q = new LinkedList<String>();
		pq = new PriorityQueue<SiteScore>();
		for (String url : urls){
			q.add(url);
		}
		running = true;
		counter = 0;
	}

	public void run(){
		timestamp = System.currentTimeMillis();
		String nextSite;
		String source;
		int score, i;
		ArrayList<String> pageURLs;
		SiteScore sc;
		File f;
		while (running && !q.isEmpty()){
			nextSite = q.pop();
			if (visitedSites.contains(nextSite))
				continue;
			visitedSites.add(nextSite);
			if (!allowsCrawlers(nextSite)){
				if (verbose) System.out.println("Crawler not allowed via Robots.txt");
				continue;
			}
			try{
				if (verbose) System.out.println("Visiting " + nextSite);
				if (mode.equals("pages"))
					source = getSiteSourcePages(nextSite);
				else
					source = getSiteSourceBytes(nextSite);
				score = 0;
				i = 0;
				for (String term : terms) {
					i = source.indexOf(term);
					while(i >= 0){
						score++;
						i = source.indexOf(term, i+1);
					}
				}
				Pattern pattern = Pattern.compile("((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)");
				Matcher pageMatcher = pattern.matcher(source);
				pageURLs = new ArrayList<String>();
				while(pageMatcher.find()){
					pageURLs.add(pageMatcher.group());
				}
				for (String s : pageURLs) {
					if (!visitedSites.contains(s)){
						if (verbose) System.out.println("Adding " + s + " to url queue");
						q.add(s);
					}
				}
				String fileName = nextSite.replace("/", "");
				PrintWriter writer = new PrintWriter("/tmp/" + fileName, "UTF-8");
				writer.print(source);
				writer.close();
				sc = new SiteScore(nextSite, score);
				pq.add(sc);
			}catch(MalformedURLException e){
				if (verbose) System.out.println("Error: Malformed URL. Skipping.");
				continue;
			}catch(IOException e){
				if (verbose) System.out.println("Error: Unable to connect to site: " + nextSite);
				continue;
			}
		}
		terminate();
		System.exit(0);
	}

	public void terminate(){
		running = false;
		System.out.println("Done running.\nBest Sites:");
		while (!pq.isEmpty()){
			SiteScore score = pq.poll();
			if (score.getScore() > 0)
				System.out.println(score.getSite() + " -- " + score.getScore() + " hit" + (score.getScore() == 1 ? "." : "s."));
		}
	}

	private boolean allowsCrawlers(String stringURL){
		String newURL = stringURL + "/robots.txt";
		try{
			URL url = new URL(newURL);
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null){
				if (inputLine.equals("User-agent: *") && (inputLine=in.readLine()) != null){
					if (inputLine.equals("Disallow: /"))
						return false;
				}
			}
			in.close();
			return true;
		}catch(MalformedURLException e){
			return true;
		}catch(IOException e){
			return true;
		}
	}

	private String getSiteSourcePages(String urlString) throws MalformedURLException, IOException{
		if (counter > limit){
			long now = System.currentTimeMillis();
			if (timestamp + interval >= now ){
				try{
					if (verbose) System.out.println("Hit page per second limit. Sleeping.");
					sleep(timestamp+interval-now);
				}
				catch(InterruptedException e){}
			}
			timestamp = now;
			counter = 0;
		}
		URL url = new URL(urlString);
		URLConnection urlcon = url.openConnection();
		urlcon.setConnectTimeout(5000);
		urlcon.setReadTimeout(5000);
		BufferedReader in = new BufferedReader(new InputStreamReader(urlcon.getInputStream()));
		StringBuilder a = new StringBuilder();
		String inputLine;
		while ((inputLine = in.readLine()) != null){
			a.append(inputLine.toLowerCase());
		}
		in.close();
		counter++;
		return a.toString();
	}

	private String getSiteSourceBytes(String urlString) throws MalformedURLException, IOException{
		URL url = new URL(urlString);
		URLConnection urlcon = url.openConnection();
		urlcon.setConnectTimeout(5000);
		urlcon.setReadTimeout(5000);
		InputStream in = urlcon.getInputStream();
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		byte[] byteChunk = new byte[limit];
		int n;
		while ((n = in.read(byteChunk)) >= 0){
			byteStream.write(byteChunk, 0, n);
			try{
				if (verbose) System.out.println("Hit bytes per second limit. Sleeping.");
				sleep(interval);
			}
			catch(InterruptedException e){}
		}
		in.close();
		byte[] bytes = byteStream.toByteArray();
		return new String(bytes);
	}

	class SiteScore implements Comparable<SiteScore>{
		private String site;
		private int score;
		public SiteScore(String site, int score){
			this.site = site;
			this.score = score;
		}
		public int getScore(){
			return score;
		}
		public String getSite(){
			return site;
		}
		public void setSite(String site){
			this.site = site;
		}
		public void setScore(int score){
			this.score = score;
		}
		public int compareTo(SiteScore that){
			return that.getScore()-this.getScore();
		}
	}
}