package org.eea.dataset.persistence.schemas.repository;

import lombok.AllArgsConstructor;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.eea.dataset.persistence.schemas.domain.webform.WebformConfig;
import org.eea.dataset.persistence.schemas.domain.webform.WebformConfigHistory;
import org.eea.dataset.persistence.schemas.domain.webform.WebformConfigHistoryCounters;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Updates;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * The Class ExtendedWebformConfigRepositoryImpl.
 */
@Service
@AllArgsConstructor
public class ExtendedWebformConfigRepositoryImpl implements ExtendedWebformConfigRepository {

  /** The mongo database. */
  private final MongoDatabase mongoDatabase;
  private final MongoTemplate mongoTemplate;
  private final WebformConfigHistoryRepository webformConfigHistoryRepository;

  /**
   * Update web form config.
   *
   * @param webform the webform
   */
  @Override
  public void updateWebFormConfig(WebformConfig webform) {

    Bson updates = Updates.combine(Updates.set("file", webform.getFile()),
        Updates.set("name", webform.getName()));

    mongoDatabase.getCollection("WebformConfig")
        .updateOne(new Document("idReferenced", webform.getIdReferenced()), updates);

  }

  @Override
  public void saveWebFormConfigHistory(WebformConfig webFormMongo) {
    WebformConfigHistory history = new WebformConfigHistory();
    history.setIdWebformConfigSchema(webFormMongo.getId());
    history.setIdReferenced(webFormMongo.getIdReferenced());
    history.setName(webFormMongo.getName());
    history.setFile(webFormMongo.getFile());
    // Use document-specific version counter
    history.setVersion(getNextSequence(webFormMongo.getId()));
    history.setCreatedAt(Instant.now());

    webformConfigHistoryRepository.save(history);
  }

  @Override
  public WebformConfigHistory getWebFormConfigHistory(ObjectId webFormId, Long version) {
    if (version != null) {
      // Retrieve specific version
      return webformConfigHistoryRepository
          .findByIdWebformConfigSchemaAndVersion(webFormId, version)
          .orElse(null);
    } else {
      // Retrieve latest version
      return webformConfigHistoryRepository
          .findFirstByIdWebformConfigSchemaOrderByVersionDesc(webFormId)
          .orElse(null);
    }
  }

  private long getNextSequence(ObjectId webformConfigId) {
    String counterKey = "WebformConfigHistory_" + webformConfigId; // Unique counter for each document

    Query query = new Query(Criteria.where("_id").is(counterKey));
    Update update = new Update().inc("seq", 1);
    FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true).upsert(true);

    WebformConfigHistoryCounters webformConfigHistoryCounters = mongoTemplate.findAndModify(query, update, options, WebformConfigHistoryCounters.class);
    return webformConfigHistoryCounters != null ? webformConfigHistoryCounters.getSeq() : 1;
  }
}
