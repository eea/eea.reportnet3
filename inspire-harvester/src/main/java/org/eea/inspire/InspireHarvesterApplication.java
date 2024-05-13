package org.eea.inspire;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import java.net.URL;
import org.eea.security.jwt.configuration.EeaEnableSecurity;
import org.eea.swagger.EnableEEASwagger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


/**
 * The type Inspire harvester application.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableCircuitBreaker
@EnableEEASwagger
@EeaEnableSecurity
@EnableCaching
public class InspireHarvesterApplication {

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(InspireHarvesterApplication.class);


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

      LOG.info(feed.toString());

      ok = true;
    } catch (final Exception ex) {
      LOG.error("ERROR: {}", ex.getMessage(), ex);
    }
    if (!ok) {

      LOG.info("FeedReader reads and prints any RSS/Atom feed type.");
      LOG.info("The first parameter must be the URL of the feed to read.");

    }
  }

}
