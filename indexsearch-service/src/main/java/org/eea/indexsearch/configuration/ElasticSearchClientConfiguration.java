package org.eea.indexsearch.configuration;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;



/**
 * The Class ElasticSearchClientConfiguration.
 */
@Configuration
@EnableTransactionManagement
@EnableAspectJAutoProxy
public class ElasticSearchClientConfiguration {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(ElasticSearchClientConfiguration.class);


  /** The host. */
  @Value("${elasticsearch.host}")
  private String host;

  /** The port. */
  @Value("${elasticsearch.port}")
  private int port;


  /**
   * Client.
   *
   * @return the rest high level client
   */
  @Bean(destroyMethod = "close")
  public RestHighLevelClient client() {

    final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("", ""));

    RestClientBuilder builder = RestClient.builder(new HttpHost(host, port))
        .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
            .setDefaultCredentialsProvider(credentialsProvider));

    RestHighLevelClient client = new RestHighLevelClient(builder);


    LOG.info("ElasticSearch Client Conected. ");

    return client;
  }
}
