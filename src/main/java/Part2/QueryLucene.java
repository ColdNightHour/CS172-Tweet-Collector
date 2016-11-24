package Part2;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.Version;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.nibor.autolink.LinkExtractor;
import org.nibor.autolink.LinkSpan;
import org.nibor.autolink.LinkType;

import java.io.*;
import java.util.EnumSet;
import java.util.NoSuchElementException;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Set;

public class QueryLucene {
  public static void main(String [] args) throws IOException{
    File indexDir = new File("./LuceneIndex");
    indexDir.mkdir();
    Directory d = FSDirectory.open(indexDir.toPath());
    StandardAnalyzer analyzer = new StandardAnalyzer();
    Map<String, Float> boosts = new HashMap<String, Float>();
    boosts.put("message", (float)(10.0));
    try{
      //System.out.println(args[0]);
      Query q = new MultiFieldQueryParser(new String[] {"name", "hashtags", "message", "url_titles", "timestamp", "location"}, analyzer, boosts).parse(args[0]);
      int hitsPerPage = 10;
      IndexReader reader = DirectoryReader.open(d);
      IndexSearcher searcher = new IndexSearcher(reader);
      TopDocs docs = searcher.search(q, hitsPerPage);
      ScoreDoc[] hits = docs.scoreDocs;
      System.out.println("~HITS~");
      System.out.println("~STRT~");
      for(int i = 0; i < hits.length; ++i) {
        int docId = hits[i].doc;
        Document de = searcher.doc(docId);
        System.out.println("DOC-BEG");
        System.out.print("{{\"score\" : " + hits[i].score + "}, ");
        System.out.print("{\"name\" : \"" + de.get("name") + "\"}, ");
        System.out.print("{\"message\" : \"" + de.get("message") + "\"}, ");
        System.out.print("{\"hashtags\" : " + de.get("hashtags") + "}, ");
        System.out.print("{\"location\" : \"" + de.get("location") + "\"}, ");
        System.out.print("{\"url_titles\" : " + de.get("url_titles") + "}, ");
        System.out.println("{\"timestamp\" : \"" + de.get("timestamp") + "\"}}");
      }
      System.out.println("~END~");
    } catch (ParseException e){

    }

  }
}
