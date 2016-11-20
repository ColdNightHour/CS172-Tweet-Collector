package Part2;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.nibor.autolink.LinkExtractor;
import org.nibor.autolink.LinkSpan;
import org.nibor.autolink.LinkType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.EnumSet;

/**
 * Created by jman0_000 on 11/19/2016.
 */
public class WriteToLucene {

    public static String dir = ""; //DIRECTORY WHERE OUR TWEETS ARE STORED

    public static void addDoc(IndexWriter w, String line) {
        try {
            Document doc = new Document();
            JSONObject tweet = new JSONObject(doc);

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
            for (LinkSpan link = links.iterator().next();
                 link != null; link = links.iterator().next()) {
                String url = message.substring(link.getBeginIndex(), link.getEndIndex());

                try {
                    doc.add(new TextField("url_titles", Jsoup.connect(url).get().title(), Field.Store.YES));
                } catch (IOException e) {}
            }

            JSONArray hashtagArr = tweet.getJSONArray("hashTags");
            for (int i = 0; i < hashtagArr.length(); ++i) {
                doc.add(new StringField("hashtags", hashtagArr.getString(i), Field.Store.YES));
            }
        } catch (JSONException e) {}
    }

    public static void main(String [] args) throws IOException{
        StandardAnalyzer sA = new StandardAnalyzer();
        Directory index = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(sA);
        IndexWriter w = new IndexWriter(index, config);

        File[] files = new File(dir).listFiles();
        for (File f : files) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(f));

                for (String line = br.readLine(); line != null; line = br.readLine()) {
                    addDoc(w, line);
                }

                w.close();
            } catch (IOException e1) {}
        }
    }
}
