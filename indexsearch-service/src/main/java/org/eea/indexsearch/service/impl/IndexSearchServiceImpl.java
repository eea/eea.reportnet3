package org.eea.indexsearch.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.eea.indexsearch.io.kafka.domain.ElasticSearchData;
import org.eea.indexsearch.service.IndexSearchService;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * The Class IndexSearchServiceImpl.
 */
@Service("indexSearchService")

/** The Constant log. */
@Slf4j
public class IndexSearchServiceImpl implements IndexSearchService {

  /** The Constant INDEX. */
  private static final String INDEX = "lead";

  /** The Constant TYPE. */
  private static final String TYPE = "lead";


  /** The client. */
  @Autowired
  private RestHighLevelClient client;

  /** The object mapper. */
  @Autowired
  private ObjectMapper objectMapper;


  // @Override
  // public String createProfile(EventHandler eventHandler) throws Exception {
  //
  // UUID uuid = UUID.randomUUID();
  // eventHandler.setId(uuid.toString());
  //
  // Map<String, Object> EventHandlerMapper = objectMapper.convertValue(eventHandler, Map.class);
  //
  // IndexRequest indexRequest =
  // new IndexRequest(INDEX, TYPE, eventHandler.getId()).source(EventHandlerMapper);
  //
  // IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
  //
  // return indexResponse.getResult().name();
  // }
  //
  // @Override
  // public ElasticSearchData findById(String id) throws Exception {
  //
  // GetRequest getRequest = new GetRequest(INDEX, TYPE, id);
  //
  // GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);
  // Map<String, Object> resultMap = getResponse.getSource();
  //
  // return objectMapper.convertValue(resultMap, Employee.class);
  //
  //
  // }
  //
  // @Override
  // public String updateProfile(Employee employee) throws Exception {
  //
  // Employee resultDocument = findById(employee.getId());
  //
  // UpdateRequest updateRequest = new UpdateRequest(INDEX, TYPE, resultDocument.getId());
  //
  // updateRequest.doc(convertProfileDocumentToMap(employee));
  // UpdateResponse updateResponse = client.update(updateRequest, RequestOptions.DEFAULT);
  //
  // return updateResponse.getResult().name();
  //
  // }
  /**
   * Find all.
   *
   * @return the list
   * @throws Exception the exception
   */
  //
  @Override
  public List<ElasticSearchData> findAll() throws Exception {


    SearchRequest searchRequest = buildSearchRequest(INDEX, TYPE);
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder.query(QueryBuilders.matchAllQuery());
    searchRequest.source(searchSourceBuilder);

    SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

    return getSearchResult(searchResponse);
  }

  // @Override
  // public List<Employee> findProfileByName(String name) throws Exception {
  //
  //
  // SearchRequest searchRequest = new SearchRequest();
  // searchRequest.indices(INDEX);
  // searchRequest.types(TYPE);
  //
  // SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
  //
  // MatchQueryBuilder matchQueryBuilder =
  // QueryBuilders.matchQuery("name", name).operator(Operator.AND);
  //
  // searchSourceBuilder.query(matchQueryBuilder);
  //
  // searchRequest.source(searchSourceBuilder);
  //
  // SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
  //
  // return getSearchResult(searchResponse);
  //
  // }
  /**
   * Delete profile document.
   *
   * @param id the id
   * @return the string
   * @throws Exception the exception
   */
  //
  @Override
  public String deleteProfileDocument(String id) throws Exception {

    DeleteRequest deleteRequest = new DeleteRequest(INDEX, TYPE, id);
    DeleteResponse response = client.delete(deleteRequest, RequestOptions.DEFAULT);

    return response.getResult().name();

  }

  //
  //
  // private Map<String, Object> convertProfileDocumentToMap(Employee employee) {
  // return objectMapper.convertValue(employee, Map.class);
  // }
  //
  //
  // private Employee convertMapToProfileDocument(Map<String, Object> map) {
  // return objectMapper.convertValue(map, Employee.class);
  // }
  //
  // @Override
  // public List<Employee> searchByTechnology(String technology) throws Exception {
  //
  // SearchRequest searchRequest = buildSearchRequest(INDEX, TYPE);
  // SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
  //
  // QueryBuilder queryBuilder =
  // QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("technologies.name", technology));
  //
  // searchSourceBuilder
  // .query(QueryBuilders.nestedQuery("technologies", queryBuilder, ScoreMode.Avg));
  //
  // searchRequest.source(searchSourceBuilder);
  //
  // SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
  //
  // return getSearchResult(response);
  // }
  //
  /**
   * Gets the search result.
   *
   * @param response the response
   * @return the search result
   */
  //
  private List<ElasticSearchData> getSearchResult(SearchResponse response) {

    SearchHit[] searchHit = response.getHits().getHits();

    List<ElasticSearchData> profileDocuments = new ArrayList<>();

    for (SearchHit hit : searchHit) {
      profileDocuments
          .add(objectMapper.convertValue(hit.getSourceAsMap(), ElasticSearchData.class));
    }

    return profileDocuments;
  }


  /**
   * Builds the search request.
   *
   * @param index the index
   * @param type the type
   * @return the search request
   */
  private SearchRequest buildSearchRequest(String index, String type) {

    SearchRequest searchRequest = new SearchRequest();
    searchRequest.indices(index);
    searchRequest.types(type);

    return searchRequest;
  }


  /**
   * Execute macros.
   */
  @Override
  public void executeMacros() {

    // EventHandlerExecutor executor = new EventHandlerExecutor(); // Invoker
    // IndexSearchEvents receiver = new IndexSearchEvents(); // receiver 1
    //
    // executor.setCommand(new IndexSearchEventCloseCommand(receiver));
    // executor.start();

  }
}


