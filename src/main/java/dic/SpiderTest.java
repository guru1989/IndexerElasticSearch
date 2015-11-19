package dic;
public class SpiderTest
{
    /**
     * This is our test. It creates a spider (which creates spider legs) and crawls the web.
     * 
     * @param args[0]   :    URL from where to start crawling
     * 		  args[1]	:	 number of URLs to crawl
     * 		  args[2]	:	search query	 
     *        
     *            
     */
    public static void main(String[] args)
    {
        Spider spider = new Spider(args[3]);
        spider.crawler(args[0], Integer.parseInt(args[1]));
        spider.querySearch(args[2]);
    }
}