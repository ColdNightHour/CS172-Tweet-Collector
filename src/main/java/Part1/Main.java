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

public class Main{
    public static int byteCount = 0;
    public static final String baseFileName = "TWEET_FILE";
    public static int fileCnt = 0; //CHANGE THIS VALUE WHENEVER THIS PROGRAM STOPS
    public static int tweetCnt = 0; //COUNTING # OF TWEETS SEEN
    public static int tweetByteCnt = 0;
    public static final int MAX_TWEET_CNT = 250; //CAP FOR DETECTING DUPES
    public static final int MAX_BYTE_CNT = 10 * 1024^2;

    HashSet tweetIdHash = new HashSet();

    public static File currentFile; //CURRENT FILE BEING I/O'd TO
    public static OutputStreamWriter stream;
    public static void main(String [] args) {
        ConfigurationBuilder config = new ConfigurationBuilder();
        config.setOAuthConsumerKey("K8P38gSDQUTk1Et7QAxDg5a1B")
            .setOAuthConsumerSecret("GcRmcdSe6CbLT1BvGNZREso8jrcNRENsYDc4crHWr4UOvaA44s")
            .setOAuthAccessToken("1853291412-qwBGaZr2S64KfmIU0hB1aYi4Kc0goxOQWzyeA20")
            .setOAuthAccessTokenSecret("CV17evpidxeGGTkP6mn7OwOOMIflt6DZSljwO0jbq6wZM");

        TwitterStream twitterStream = new TwitterStreamFactory(config.build()).getInstance();
        StatusListener statusListener = new StatusListener() {
            public void onStatus(Status status) {
              /*  try {
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
                        tweetCnt = 1;
                        tweetIdHash.clear();
                        tweetIdHash.add(id);
                    }

                    org.json.JSONObject object = new JSONObject();
                    object.put("name", status.getUser().getScreenName());

                    String msg = status.getText();
                    object.put("message", msg);
                    object.put("timestamp", status.getCreatedAt().toString());
                    object.put("location", status.getGeoLocation().toString());

                    //TODO: All this parse-y URL stuff should be done in a runnable.
                    //Prolly pass in the incomplete JSON object to runnable so that runnable finishes the job
                    LinkExtractor linkExtractor = LinkExtractor.builder().build();
                    Iterable<LinkSpan> links = linkExtractor.extractLinks(msg);
                    LinkSpan link;

                    org.json.JSONArray urlArray = new JSONArray();
                    while ((link = links.iterator().next()) != null) {
                        JSONObject urlBundle = new JSONObject();
                        String url = msg.substring(link.getBeginIndex(), link.getEndIndex());
                        urlBundle.put("url", url);

                        try {
                            urlBundle.put("url_title", Jsoup.connect("http://example.com/").get().title()); //TODO: FUCKING DO THIS
                        } catch (IOException e) {
                            System.out.println("Unable to parse title.");
                        }

                        urlArray.put(urlBundle);
                    }

                    if (urlArray.length() > 0) {
                        object.put("url_list", urlArray);
                    }

                    String jsonString = object.toString() + "\n";
                    if (tweetByteCnt < MAX_BYTE_CNT) {
                        tweetByteCnt += jsonString.getBytes().length;
                        stream.write(object.toString());
                    } else {
                        tweetByteCnt = jsonString.getBytes().length;
                        fileCnt++;
                        stream.close();

                        currentFile = new File(baseFileName + Integer.toString(fileCnt));
                        try {
                            currentFile.createNewFile();
                            stream = new OutputStreamWriter(new FileOutputStream(currentFile), "UTF8");
                            stream.write(jsonString);
                        } catch (Exception e) {
                            System.out.println("CREATING FILE WENT WRONG");
                            System.exit(-1);
                        }
                    }

                } catch (org.json.JSONException e) {
                    System.out.println("YOUR JSON CODE SUCKS");
                } catch (IOException e) {
                    System.out.println("UNABLE TO WRITE JSON TO FILE");
                }*/
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
                System.out.println("Got stall warning:" + warning);
            }

            public void onException(Exception ex) {
                ex.printStackTrace();
            }
        };

        twitterStream.addListener(statusListener);
        /*
        currentFile = new File(baseFileName + Integer.toString(fileCnt));
        try {
            currentFile.createNewFile();
            stream = new OutputStreamWriter(new FileOutputStream(currentFile), "UTF8");
        } catch (Exception e) {
            System.out.println("CREATING FILE WENT WRONG");
            return;
        }
        */
        twitterStream.sample();
    }
}
