package org.eea.dataset.persistence.data.repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.springframework.data.repository.query.Param;

/**
 * The Class DatasetExtendedRepositoryImpl.
 */
public class DatasetExtendedRepositoryImpl implements DatasetExtendedRepository {

  /** The entity manager. */
  @PersistenceContext
  private EntityManager entityManager;

  /** The Constant QUERY_DROP_SCHEMA. */
  private static final String QUERY_DROP_SCHEMA = "DROP SCHEMA ?1";

  /**
   * Delete schema.
   *
   * @param schema the schema
   */
  @Override
  public void deleteSchema(@Param("schema") String schema) {
    Query query = entityManager.createNativeQuery(QUERY_DROP_SCHEMA);
    query.setParameter(1, schema);
    query.executeUpdate();
  }

}
