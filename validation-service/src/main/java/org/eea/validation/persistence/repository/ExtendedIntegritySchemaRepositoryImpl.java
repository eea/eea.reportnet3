package org.eea.validation.persistence.repository;

import java.util.List;
import org.eea.validation.persistence.schemas.IntegritySchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

/**
 * The Class ExtendedIntegritySchemaRepositoryImpl.
 */
public class ExtendedIntegritySchemaRepositoryImpl implements ExtendedIntegritySchemaRepository {


  /** The mongo template. */
  @Autowired
  private MongoTemplate mongoTemplate;

  /**
   * Find byid field schema orig or dest.
   *
   * @param idFieldSchema the id field schema
   * @return the list
   */
  @Override
  public List<IntegritySchema> findByidFieldSchemaOrigOrDest(String idFieldSchema) {
    Query query = new Query(Criteria.where("originFields.$").is(idFieldSchema));
    List<IntegritySchema> integrity = mongoTemplate.find(query, IntegritySchema.class);
    return integrity;
  }

}
