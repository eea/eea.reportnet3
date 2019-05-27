package org.eea.inspire;

import java.net.URL;
import org.eea.swagger.EnableEEASwagger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;


/**
 * The type Data set application.
 */

@SpringBootApplication
@EnableDiscoveryClient
@EnableCircuitBreaker
@EnableEEASwagger
public class InspireHarvesterApplication {


  /**
   * The entry point of application.
   *
   * @param args the input arguments
   */
  public static void main(final String[] args) {
    SpringApplication.run(InspireHarvesterApplication.class, args);
    testAtom();
  }


  /**
   * Test atom.
   */
  private static void testAtom() {
    boolean ok = false;

    try {
      final URL feedUrl = new URL("https://www.hoy.es/rss/atom/?section=ultima-hora");

      final SyndFeedInput input = new SyndFeedInput();
      final SyndFeed feed = input.build(new XmlReader(feedUrl));

      System.out.println(feed);

      ok = true;
    } catch (final Exception ex) {
      ex.printStackTrace();
      System.out.println("ERROR: " + ex.getMessage());
    }
    if (!ok) {
      System.out.println();
      System.out.println("FeedReader reads and prints any RSS/Atom feed type.");
      System.out.println("The first parameter must be the URL of the feed to read.");
      System.out.println();
    }
  }

}
