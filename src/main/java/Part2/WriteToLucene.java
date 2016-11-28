package Part2;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.nibor.autolink.LinkExtractor;
import org.nibor.autolink.LinkSpan;
import org.nibor.autolink.LinkType;

import java.io.*;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Created by jman0_000 on 11/19/2016.
 */
public class WriteToLucene {

    public static String dir = "C:\\Users\\jman0_000\\Documents\\TweetSample"; //DIRECTORY WHERE OUR TWEETS ARE STORED

    public static void addDoc(IndexWriter w, String line) {
        try {
            //System.out.println(line);
            Document doc = new Document();
            JSONObject tweet = new JSONObject(line);

            doc.add(new TextField("name", tweet.getString("name"), Field.Store.YES));

            String location = tweet.getString("location");
            doc.add(new StoredField("location", location.equals("Null") ? "" : location));

            doc.add(new StoredField("timestamp", tweet.getString("timestamp")));

            String message = tweet.getString("message");
            doc.add(new TextField("message", message, Field.Store.YES));
            LinkExtractor linkExtractor = LinkExtractor.builder()
                .linkTypes(EnumSet.of(LinkType.URL)) // limit to URLs
                .build();
            Iterable<LinkSpan> links = linkExtractor.extractLinks(message);

            List<String> titleArr = new ArrayList<>();
            try {
                for (LinkSpan link : links) {
                    String url = message.substring(link.getBeginIndex(), link.getEndIndex());
                    System.out.println("Getting title of: " + url);
                    try {
                        titleArr.add(Jsoup.connect(url).get().title());
                    } catch (Exception e) {
                        System.out.println("Unable to parse URL " + url);
                    }
                }
                doc.add(new TextField("url_titles", titleArr.toString(), Field.Store.YES));
            } catch (NoSuchElementException e) {
                System.out.println("No urls in this tweet!");
            }

            JSONArray hashtagArr = tweet.getJSONArray("hashTags");
            List<String> arr = new ArrayList<String>();
            for (int i = 0; i < hashtagArr.length(); ++i) {
                //System.out.println("Hashtag: " + hashtagArr.getString(i));
                arr.add(hashtagArr.getString(i));
            }

            doc.add(new TextField("hashtags", arr.toString(), Field.Store.YES));

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
        IndexWriterConfig config = new IndexWriterConfig(sA);
        File indexDir = new File("./LuceneIndex");
        indexDir.mkdir();
        Directory d = FSDirectory.open(indexDir.toPath());
        IndexWriter w = new IndexWriter(d, config);
        //w.deleteAll();

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

                } catch (IOException e1) {
                    System.out.println(getStackTrace(e1));
                }
            }

            w.close();

        } catch (NullPointerException e) {
            System.out.println(getStackTrace(e));
        }
    }
}
