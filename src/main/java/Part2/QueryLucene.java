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
/**
 * Created by coldnighthour on 11/20/16.
 */
public class QueryLucene {
  public static void main(String [] args) throws IOException{
    File indexDir = new File("./LuceneIndex");
    indexDir.mkdir();
    Directory d = FSDirectory.open(indexDir.toPath());
    StandardAnalyzer analyzer = new StandardAnalyzer();
    //String querystr = /*args.length > 0 ? args[0] : "lucene"*/messga "bomb";

    try{
      Query q = new QueryParser("message", analyzer).parse("message");
      int hitsPerPage = 10;
      IndexReader reader = DirectoryReader.open(d);
      IndexSearcher searcher = new IndexSearcher(reader);
      TopDocs docs = searcher.search(q, hitsPerPage);
      ScoreDoc[] hits = docs.scoreDocs;
      System.out.println("Found " + hits.length + " hits.");
    } catch (ParseException e){

    }

  }
}
