package org.eea.validation.persistence.data.repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.eea.multitenancy.TenantResolver;

/**
 * The Interface RecordRepository.
 */
public class TableRepositoryExtendedImpl implements TableRepositoryExtended {

  /** The Constant TABLE_SCHEMA_BY_ID_FIELD. */
  private static final String TABLE_SCHEMA_BY_ID_FIELD =
      "select distinct id_table_schema from dataset_%s.table_value tv "
          + " inner join dataset_%s.record_value rv " + " on tv.id = rv.id_table "
          + " inner join dataset_%s.field_value fv " + " on fv.id_record = rv.id "
          + " where fv.id_field_schema='%s' ";

  /** The entity manager. */
  @PersistenceContext
  private EntityManager entityManager;

  /**
   * Find table value by field schema id. With that method we find the tableschema by a
   * idFieldSchema
   *
   * @param datasetId the dataset id
   * @param idFieldSchema the id field schema
   * @return the string
   */
  @Override
  public String findTableValueByFieldSchemaId(Long datasetId, String idFieldSchema) {
    TenantResolver.setTenantName("dataset_" + datasetId);
    Query query = entityManager.createNativeQuery(
        String.format(TABLE_SCHEMA_BY_ID_FIELD, datasetId, datasetId, datasetId, idFieldSchema));
    return query.getResultList().get(0).toString();
  }



}
