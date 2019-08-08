package org.eea.validation.persistence.data.repository;

import java.math.BigInteger;
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
   * @param value the value
   * @param fieldSchema the field schema
   * @param idDataset the id dataset
   * @return the list
   */
  @SuppressWarnings("unchecked")
  @Override
  public Integer findAllFieldValuesByFieldSchemAndNameDataSet(String value, String fieldSchema,
      Long idDataset) {
    String QUERY = "SELECT count(fv.value) FROM dataset_" + idDataset
        + ".field_value fv where fv.id_Field_Schema = '" + fieldSchema
        + "' and LOWER(fv.value)=LOWER('" + value + "')";
    Query query = entityManager.createNativeQuery(QUERY);
    List<BigInteger> countSameValue = query.getResultList();
    return countSameValue.get(0).intValue();
  }


}

