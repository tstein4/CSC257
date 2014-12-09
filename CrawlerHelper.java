import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;

public class CrawlerHelper {

	class CrawlerThread extends Thread{
		private ArrayList<String> visitedSites = new ArrayList<String>();
		private LinkedList<String> q = new LinkedList<String>();
		private PriorityQueue<SiteScore> pq = new PriorityQueue<SiteScore>();

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
		public int compareTo(SiteScore other){
			return this.getScore()-other.getScore();
		}
	}
}