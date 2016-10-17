package Part1;

import twitter4j.*;

public class Main{
    public static void main(String [] args) {
        TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
        StatusListener statusListener = new StatusListener() {
            public void onStatus(Status status) {

            }

            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {

            }

            public void onTrackLimitationNotice(int i) {

            }

            public void onScrubGeo(long l, long l1) {

            }

            public void onStallWarning(StallWarning stallWarning) {
                System.out.println("Got stall warning:" + stallWarning);
            }

            public void onException(Exception e) {
                e.printStackTrace();
            }
        };
        twitterStream.addListener(statusListener);
        twitterStream.sample();
    }
}
