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
      "SELECT id_table_schema from dataset_%d.table_value tv where tv.id = "
          + "(select rv.id_table from dataset_%d.record_value rv where rv.id = "
          + "(select fv.id_record from dataset_%d.field_value fv where fv.id_field_schema='%s'))";

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
