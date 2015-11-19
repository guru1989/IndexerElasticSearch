# IndexerElasticSearch
Indexing web pages using ElasticSearch in a distributed environment.

## Prerequisites before running the project
Install Elasticsearch on the system: 
[https://www.elastic.co/guide/en/elasticsearch/guide/current/_installing_elasticsearch.html]

## Running the code
Run the jar file in target/dic-0.0.1-SNAPSHOT.jar:
> java -cp dic-0.0.1-SNAPSHOT.jar dic.SpiderTest "https://www.youtube.com/results?search_query=data+intensive+computing" 100 "hadoop" "C:\elasticsearch-2.0.0\elasticsearch-2.0.0\config\elasticsearch.yml"

param1 : URL<br>
param2 : urlCount<br>
param3 : searchQuery <br>
param4 : path to elasticsearch.yml file


## Workflow

 1. **Crawling** : A seed URL is given as a parameter and we have a limit on the max number of URLs also given as a parameter. For each URL crawled, we extract the **title**, **description** and **keywords** section from html content. There is no persistent storage involved in crawling. 
 
 2. **Indexing** : We use ElasticSearch for indexing. An index is built using the 3 sections extracted from crawling along with the URL.  All storage related stuff is taken care by ElasticSearch. We run the Indexer code in all individual nodes (systems) where crawling is taking place with a common cluster name **DIC** so that ElasticSearch forms a cluster with all these nodes. The index data will be stored locally on individual nodes . This approach is better than having a single server storing all the index data in which case it becomes a bottleneck. The index name specified is **dicindex**. All indexed documents can be viewed with this name.
 3. **Querying** : Currently we use the simple "Match Query" supported by ElasticSearch and display the top 10 websites ranked according to the searchRank provided by ElasticSearch.

## Distributed Indexing Test

 1. Setup two Windows 7 machines on VCL which are on the same LAN.<br>
 2. Setup ElasticSearch on both the machines.<br>
 3. Run the jar file on both the machines with different seed URLs:<br>
     **machine 1:**<br>
      *java -cp dic-0.0.1-SNAPSHOT.jar dic.SpiderTest "https://www.youtube.com/results?search_query=data+intensive+computing" 100 "hadoop" "C:\elasticsearch-2.0.0\elasticsearch-2.0.0\config\elasticsearch.yml"*<br>
**machine 2:**<br>
 *java -cp dic-0.0.1-SNAPSHOT.jar dic.SpiderTest "https://www.youtube.com/results?search_query=hadoop" 100 "hadoop" "C:\elasticsearch-2.0.0\elasticsearch-2.0.0\config\elasticsearch.yml"*
 4. Monitoring the cluster: Install **Sense** plugin for chrome browser on one of the systems  from:<br>
 https://chrome.google.com/webstore/detail/sense-beta/lhjgkmllcaadmopgmanpapmpjgmfcfig?hl=en <br>
 This tool helps in ElasticSearch cluster monitoring.<br> 
 Some of the commands of use are:<br>
**View indices across all clusters:**<br>
     *GET /_cat/indices?v*<br>
     
     **View cluster information:**<br>
     *GET /_cluster/state*
     
     
     **Simple Match Query:** <br>
GET _search
{
  "query" :{
    "match" : {
        "_all" : "hadoop"
    }
    }
    }

By running the above commands, it was verified how indexed data stored across different machines can be queried from a single node.

## ToDo

 - Integration of Redis BloomFilter. 
 
