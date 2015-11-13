package dic;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.fasterxml.jackson.databind.*;
import static org.elasticsearch.common.xcontent.XContentFactory.*;


public class SpiderLeg
{
    // We'll use a fake USER_AGENT so the web server thinks the robot is a normal web browser.
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1";
    private List<String> links = new LinkedList<String>();
    private Document htmlDocument;
    private Map<String, Object> json = new HashMap<String, Object>();
    private ObjectMapper mapper = new ObjectMapper();
   
    

    /**
     * This performs all the work. It makes an HTTP request, checks the response, and then gathers
     * up all the links on the page. Perform a searchForWord after the successful crawl
     * 
     * @param url
     *            - The URL to visit
     * @return whether or not the crawl was successful
     */
    public boolean crawl(String url, Client client, String INDEX_NAME)
    {
    	try
        {
            Connection connection = Jsoup.connect(url).userAgent(USER_AGENT);
            Document htmlDocument = connection.get();
            this.htmlDocument = htmlDocument;
            if(connection.response().statusCode() == 200) // 200 is the HTTP OK status code
                                                          // indicating that everything is great.
            {
                System.out.println("\n**Visiting** Received web page at " + url);
                
                
            }
            if(!connection.response().contentType().contains("text/html"))
            {
                System.out.println("**Failure** Retrieved something other than HTML");
                return false;
            }
            Elements linksOnPage = htmlDocument.select("#results a[href]");
            Elements moreLinks = htmlDocument.select("#watch-related a[href]");
            Elements title = htmlDocument.select("meta[name=title]");
            Elements description = htmlDocument.select("meta[name=description]");
            Elements keywords = htmlDocument.select("meta[name=keywords]");
            json.put("url", url);
            if(!title.isEmpty())
            	json.put(title.first().attr("name").toString(), title.first().attr("content").toString());
            if(!description.isEmpty())
            	json.put(description.first().attr("name").toString(), description.first().attr("content").toString());
            if(!keywords.isEmpty())
            	json.put(keywords.first().attr("name").toString(), keywords.first().attr("content").toString());
            	
            byte[] jsonbyte = mapper.writeValueAsBytes(json);
            
            
			IndexResponse response = client.prepareIndex(INDEX_NAME, "sample")
                    .setSource(jsonbyte)
                    .execute()
                    .actionGet();
			
			
			
            //dynamoDB.insertItem(url, DBValue);
	        System.out.println("Found (" +  moreLinks.size() + ") links");
            for(Element link : linksOnPage)
            {
                this.links.add(link.absUrl("href"));
            }
            for(Element link : moreLinks)
            {
                this.links.add(link.absUrl("href"));
            }
            return true;
        }
        catch(IOException ioe)
        {
            // We were not successful in our HTTP request
            return false;
        }
    }


    /**
     * Performs a search on the body of on the HTML document that is retrieved. This method should
     * only be called after a successful crawl.
     * 
     * @param searchWord
     *            - The word or string to look for
     * @return whether or not the word was found
     */
    public boolean searchForWord(String searchWord)
    {
        // Defensive coding. This method should only be used after a successful crawl.
        if(this.htmlDocument == null)
        {
            System.out.println("ERROR! Call crawl() before performing analysis on the document");
            return false;
        }
        System.out.println("Searching for the word " + searchWord + "...");
        String bodyText = this.htmlDocument.body().text();
        return bodyText.toLowerCase().contains(searchWord.toLowerCase());
    }
   
    public List<String> getLinks()
    {
        return this.links;
    }
    
    public Document getDocument()
    {
    	return this.htmlDocument;
    }

}