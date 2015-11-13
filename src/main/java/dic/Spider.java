package dic;
import java.util.HashSet;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import java.util.Map;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.util.HashMap;




public class Spider
{
  public static final String CLUSTER_NAME = "DIC";
  public static final String NODE_NAME = "dicNode";
  public static final String INDEX_NAME = "dicindex";	// should always be in lowercase
  
  private Set<String> pagesVisited = new HashSet<String>();
  private List<String> pagesToVisit = new LinkedList<String>();
  
  

  /**
   * Our main launching point for the Spider's functionality. Internally it creates spider legs
   * that make an HTTP request and parse the response (the web page).
   * 
   * @param url
   *            - The starting point of the spider
   * @param searchWord
   *            - The word or string that you are searching for
   */
  public void crawler(String url, int maxUrlCount)
  {
	  Node node = nodeBuilder().clusterName(CLUSTER_NAME).settings(ImmutableSettings.settingsBuilder().put("node.name",NODE_NAME).build()).node();
      Client client = node.client();
      while(this.pagesVisited.size() < maxUrlCount)
      {
          String currentUrl;
          SpiderLeg leg = new SpiderLeg();
          if(this.pagesToVisit.isEmpty())
          {
              currentUrl = url;
              this.pagesVisited.add(url);
          }
          else
          {
              currentUrl = this.nextUrl();
          }
         
          //dynamoDB.insertItem(currentUrl, "DataIntensive,Data Intensive");
          if(leg.crawl(currentUrl, client, INDEX_NAME)) // Lots of stuff happening here. Look at the crawl method in
          {
        	  
        	  this.pagesToVisit.addAll(leg.getLinks());
          }
      }
      System.out.println("\n**Done** Visited " + this.pagesVisited.size() + " web page(s)");
      client.close();
  }
  
  void querySearch(String keyword)
  {
	  
	  Node node = nodeBuilder().clusterName(CLUSTER_NAME).settings(ImmutableSettings.settingsBuilder().put("node.name",NODE_NAME).build()).node();
      Client client = node.client();
      SearchResponse response = client.prepareSearch(INDEX_NAME)
    	        .setQuery(QueryBuilders.matchQuery("_all", keyword))             // Query
    	        .execute()
    	        .actionGet();
      SearchHits hits = response.getHits();
      for(SearchHit hit:hits)
      {
    	  System.out.println(hit.sourceAsMap().get("url").toString()+" rank="+hit.getScore());
      }
      client.close();
      System.out.println("Client close");
  }


  /**
   * Returns the next URL to visit (in the order that they were found). We also do a check to make
   * sure this method doesn't return a URL that has already been visited.
   * 
   * @return
   */
  private String nextUrl()
  {
      String nextUrl;
      do
      {
          nextUrl = this.pagesToVisit.remove(0);
      } while(this.pagesVisited.contains(nextUrl));
      this.pagesVisited.add(nextUrl);
      return nextUrl;
  }
}