package org.eea.validation.persistence.data.repository;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.springframework.stereotype.Repository;


/**
 * The Interface RecordRepository.
 */
@Repository
public class FieldRepositoryImpl implements FieldExtendedQueriesRepository {
  /**
   * The entity manager.
   */
  @PersistenceContext
  private EntityManager entityManager;


  /**
   * Find all field values by field schem and name data set.
   *
   * @param idFieldSchema the id field schema
   * @param idDataset the id dataset
   * @param recordCoordinate the record coordinate
   * @param columnCoordinate the column coordinate
   * @return the string
   */
  @SuppressWarnings("unchecked")
  @Override
  public String findAllFieldValuesByFieldSchemAndNameDataSet(String idFieldSchema, Long idDataset,
      Long recordCoordinate, Long columnCoordinate) {

    String QUERY = "SELECT fv.value FROM dataset_" + idDataset + ".field_value fv"
        + " where fv.id_Field_Schema = '" + idFieldSchema + "' and fv.record_coordinate = "
        + recordCoordinate + " and fv.column_coordinate = " + columnCoordinate;

    Query query = entityManager.createNativeQuery(QUERY);
    List<String> value = query.getResultList();
    if (null != value && !value.isEmpty()) {
      return value.get(0);
    } else {
      return null;
    }
  }
}

