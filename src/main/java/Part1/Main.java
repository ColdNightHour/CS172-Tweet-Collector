package Part1;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.nibor.autolink.LinkExtractor;
import org.nibor.autolink.LinkSpan;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;
import java.util.HashSet;
import java.io.*;
import java.util.HashSet;
import java.util.Iterator;
import java.net.URL;
import java.net.MalformedURLException;
import java.lang.*;

public class Main{
    public static int byteCount = 0;
    public static final String baseFileName = "TWEET_FILE";
    public static int tweetCnt = 0; //COUNTING # OF TWEETS SEEN
    public static int tweetByteCnt = 0;
    public static final int MAX_TWEET_CNT = 15000000; //CAP FOR DETECTING DUPES
    public static final int MAX_BYTE_CNT = 10485760/10; //NUMBER OF BYTES FOR 10MB FILE 10485760      1048576
    /*Files of size 150000000*/
    public static HashSet<Long> tweetIdHash = new HashSet<Long>();
    public static File currentFile; //CURRENT FILE BEING I/O'd TO
    public static OutputStreamWriter stream;
    public static String location = "NULL";
    public static Boolean run = true;
    public static StringBuilder buffer = new StringBuilder("");
    public static int fileCnt = 25;
    public static int filePassCnt = 7;
    public static PrintWriter writer;
    public static void main(String [] args) {
        ConfigurationBuilder config = new ConfigurationBuilder();
        config.setOAuthConsumerKey("K8P38gSDQUTk1Et7QAxDg5a1B")
            .setOAuthConsumerSecret("GcRmcdSe6CbLT1BvGNZREso8jrcNRENsYDc4crHWr4UOvaA44s")
            .setOAuthAccessToken("1853291412-qwBGaZr2S64KfmIU0hB1aYi4Kc0goxOQWzyeA20")
            .setOAuthAccessTokenSecret("CV17evpidxeGGTkP6mn7OwOOMIflt6DZSljwO0jbq6wZM");
        /*try{
           writer = new PrintWriter("./tweets/file" + fileCnt + ".txt", "UTF-8");
        } catch(FileNotFoundException e) {
          System.out.println("NO FILE");
        }
        catch(UnsupportedEncodingException e) {
          System.out.println("WRONG ENCODING");
        }*/
        TwitterStream twitterStream = new TwitterStreamFactory(config.build()).getInstance();
        StatusListener statusListener = new StatusListener() {
            public void onStatus(Status status) {
                try {

                  //DUPLICATE DETECTION
                  long id = status.getId();
                  if (tweetCnt < MAX_TWEET_CNT) {
                      ++tweetCnt;
                      if (tweetIdHash.contains(id)) {
                          return;
                      } else {
                          tweetIdHash.add(id);
                      }
                  } else {
                      System.out.println("Cap Reset for duplicates");
                      tweetCnt = 1;
                      tweetIdHash.clear();
                      tweetIdHash.add(id);
                  }
                  String msg = status.getText();
                  org.json.JSONObject object = new JSONObject();
                  object.put("name", status.getUser().getScreenName());
                  object.put("message", msg);
                  object.put("timestamp", status.getCreatedAt());
                  if((status.getGeoLocation()) == null)
                    location = "Null";
                  else
                    location = status.getGeoLocation().toString();
                  object.put("location", location);
                  //TODO: All this parse-y URL stuff should be done in a runnable.
                  //Prolly pass in the incomplete JSON object to runnable so that runnable finishes the job
                  org.json.JSONArray urlArray = new JSONArray();
                  String [] parts = msg.split("\\s+");
                  // Attempt to convert each item into an URL.
                  for( String item : parts ) try {
                      URL url = new URL(item);
                      org.json.JSONObject urlBundle = new JSONObject();
                      urlBundle.put("url", url);
                      urlArray.put(urlBundle);
                  } catch (MalformedURLException e) {
                      // If there was an URL that was not it!...
                  }

                  if (urlArray.length() > 0) {
                      object.put("url_list", urlArray);
                  }
                  else
                    object.put("url_list", "NULL");
                  String jsonString = object.toString() + "\n";
                  try{
                    tweetByteCnt+=jsonString.getBytes("UTF-8").length;
                  } catch(UnsupportedEncodingException e) {
                    System.out.println("WRONG ENCODING");
                  }
                  buffer.append(jsonString);
                  if(tweetByteCnt >= MAX_BYTE_CNT) {
                    tweetByteCnt = 0;
                    try{
                      System.out.println("FILE " + fileCnt + " WRITTEN OF PASS " + filePassCnt);
                      filePassCnt++;
                      if(filePassCnt == 1) {
                        fileCnt++;
                        writer = new PrintWriter("./tweets/file" + fileCnt + ".txt", "UTF-8");
                        writer.println(buffer);
                      }
                      else if(filePassCnt != 1) {
                        writer= new PrintWriter(new FileOutputStream("./tweets/file" + fileCnt + ".txt", true));
                        writer.append(buffer);
                      }
                      if(filePassCnt == 11)
                        filePassCnt = 0;
                      writer.close();
                      buffer = new StringBuilder();
                      buffer.trimToSize();
                      System.gc();
                    }
                    catch(FileNotFoundException e) {
                      System.out.println("NO FILE");
                    }
                    catch(UnsupportedEncodingException e) {

                    }
                  }

                } catch (IndexOutOfBoundsException e) {
                    System.out.println("ERROR");
                    System.err.println("IndexOutOfBoundsException: " + e.getMessage());
                }
                catch (org.json.JSONException e) {
                    System.out.println("YOUR JSON CODE SUCKS");
                }

            }

            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
                //System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
            }

            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
            }

            public void onScrubGeo(long userId, long upToStatusId) {
                //System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
            }

            public void onStallWarning(StallWarning warning) {
                System.out.println("Got stall warning");
            }

            public void onException(Exception ex) {
                ex.printStackTrace();
            }
        };

        twitterStream.addListener(statusListener);

        twitterStream.sample();
    }
}
/*URL PROCESS TITLE CODE
// If possible then replace with anchor...
/*try {
    urlBundle.put("url_title", Jsoup.connect(url.toString()).get().title()); //TODO: FUCKING DO THIS
} catch (IOException e) {
    urlBundle.put("url_title", "Could not parse title");
} catch(IllegalArgumentException e) {
  urlBundle.put("url_title", "Exception for illegal argument");
}
*/
