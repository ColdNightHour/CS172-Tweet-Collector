package Part2;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
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
 * Created by jman0_000 on 11/19/2016.
 */
public class WriteToLucene {

    public static String dir = "C:\\Users\\jman0_000\\Documents\\Tweets"; //DIRECTORY WHERE OUR TWEETS ARE STORED

    public static void addDoc(IndexWriter w, String line) {
        try {
            //System.out.println(line);
            Document doc = new Document();
            JSONObject tweet = new JSONObject(line);

            doc.add(new StringField("name", tweet.getString("name"), Field.Store.YES));

            String location = tweet.getString("location");
            doc.add(new StringField("location",
                location.equals("Null") ? "" : location, Field.Store.NO));

            doc.add(new StringField("timestamp", tweet.getString("timestamp"), Field.Store.NO));

            String message = tweet.getString("message");
            doc.add(new TextField("message", message, Field.Store.YES));
            LinkExtractor linkExtractor = LinkExtractor.builder()
                .linkTypes(EnumSet.of(LinkType.URL)) // limit to URLs
                .build();
            Iterable<LinkSpan> links = linkExtractor.extractLinks(message);

            try {
                for (LinkSpan link : links) {
                    String url = message.substring(link.getBeginIndex(), link.getEndIndex());
                    System.out.println("Getting title of: " + url);
                    try {
                        doc.add(new TextField("url_titles", Jsoup.connect(url).get().title(), Field.Store.YES));
                    } catch (Exception e) {
                        System.out.println("Unable to parse URL " + url);
                    }
                }
            } catch (NoSuchElementException e) {
                System.out.println("No urls in this tweet!");
            }

            JSONArray hashtagArr = tweet.getJSONArray("hashTags");
            for (int i = 0; i < hashtagArr.length(); ++i) {
                doc.add(new StringField("hashtags", hashtagArr.getString(i), Field.Store.YES));
            }

            w.addDocument(doc);
            System.out.println("Indexed a tweet!");
        } catch (JSONException | IOException e) {
            System.out.println(getStackTrace(e));
        }
    }

    public static String getStackTrace(Throwable aThrowable) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        aThrowable.printStackTrace(printWriter);
        return result.toString();
    }

    public static void main(String [] args) throws IOException{
        StandardAnalyzer sA = new StandardAnalyzer();
        Directory index = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(sA);
        File indexDir = new File("./LuceneIndex");
        indexDir.mkdir();
        Directory d = FSDirectory.open(indexDir.toPath());
        IndexWriter w = new IndexWriter(index, config);
        w.deleteAll();

        File[] files = new File(dir).listFiles();
        System.out.println("Go through files in " + dir);
        try {
            for (File f : files) {
                try {
                    System.out.println("Parsing file " + f.getName());
                    BufferedReader br = new BufferedReader(new FileReader(f));

                    for (String line = br.readLine(); line != null; line = br.readLine()) {
                        addDoc(w, line);
                    }

                    w.close();
                } catch (IOException e1) {
                    System.out.println(getStackTrace(e1));
                }
            }
        } catch (NullPointerException e) {
            System.out.println(getStackTrace(e));
        }
    }
}
