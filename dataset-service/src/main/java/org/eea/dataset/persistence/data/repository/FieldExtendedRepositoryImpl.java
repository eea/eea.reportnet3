package org.eea.dataset.persistence.data.repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.apache.commons.lang3.StringUtils;
import org.eea.dataset.mapper.FieldNoValidationMapper;
import org.eea.dataset.persistence.data.domain.FieldValue;
import org.eea.dataset.service.model.FieldValueWithLabel;
import org.eea.interfaces.vo.dataset.FieldVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;


/**
 * The Class FieldExtendedRepositoryImpl.
 */
@Repository
public class FieldExtendedRepositoryImpl implements FieldExtendedRepository {


  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(FieldExtendedRepositoryImpl.class);

  /**
   * The entity manager.
   */
  @PersistenceContext
  private EntityManager entityManager;

  /**
   * The field no validation mapper.
   */
  @Autowired
  private FieldNoValidationMapper fieldNoValidationMapper;

  /**
   * The field repository.
   */
  @Autowired
  private FieldRepository fieldRepository;

  /**
   * The Constant SORT_NUMBER_QUERY.
   */
  private static final String SORT_NUMBER_QUERY =
      ",CAST(fv.value as java.math.BigDecimal) as orden ";

  /**
   * The Constant SORT_DATE_QUERY.
   */
  private static final String SORT_DATE_QUERY = ",CAST(fv.value as java.sql.Date) as orden ";

  /**
   * The Constant SORT_STRING_QUERY.
   */
  private static final String SORT_STRING_QUERY = ",CAST(fv.value as java.lang.String) as orden ";

  /**
   * The Constant QUERY_1.
   */
  private static final String QUERY_1 = "SELECT DISTINCT fv as fieldValue, tag as label ";

  /**
   * The Constant QUERY_2_WITHOUT_CONDITIONAL.
   */
  private static final String QUERY_2_WITHOUT_CONDITIONAL =
      "FROM FieldValue fv, FieldValue tag  WHERE fv.idFieldSchema = :fieldSchemaId ";

  /**
   * The Constant QUERY_2_WITH_CONDITIONAL.
   */
  private static final String QUERY_2_WITH_CONDITIONAL =
      "FROM FieldValue fv, FieldValue tag, FieldValue cond  WHERE fv.idFieldSchema = :fieldSchemaId ";

  /**
   * The Constant QUERY_3.
   */
  private static final String QUERY_3 =
      "AND tag.idFieldSchema = :labelId AND fv.record.id = tag.record.id " + " AND fv.value <> '' "
          + " AND (:searchText IS NULL or fv.value like CONCAT('%',:searchText,'%') or tag.value like CONCAT('%',:searchText,'%') ) ";

  /**
   * The Constant QUERY_3_CONDITIONAL.
   */
  private static final String QUERY_3_CONDITIONAL =
      "AND (cond.idFieldSchema = :conditionalId AND cond.value = :conditionalValue AND cond.record.id = fv.record.id or :conditionalId IS NULL) ";

  /**
   * The Constant QUERY_ORDER.
   */
  private static final String QUERY_ORDER = "ORDER BY orden";


  /**
   * Find by id field schema with tag ordered.
   *
   * @param idPk the id pk
   * @param labelSchemaId the label schema id
   * @param searchValue the search value
   * @param conditionalSchemaId the conditional schema id
   * @param conditionalValue the conditional value
   * @param resultsNumber the results number
   *
   * @return the list
   */
  @Override
  public List<FieldVO> findByIdFieldSchemaWithTagOrdered(String idPk, String labelSchemaId,
      String searchValue, String conditionalSchemaId, String conditionalValue,
      Integer resultsNumber) {

    List<FieldVO> fieldsVO = new ArrayList<>();
    List<FieldValueWithLabel> fields = new ArrayList<>();

    StringBuilder queryBuilder = new StringBuilder();
    queryBuilder.append(QUERY_1);
    FieldValue typeField = fieldRepository.findFirstTypeByIdFieldSchema(idPk);
    if (typeField != null && (DataType.NUMBER_DECIMAL.equals(typeField.getType())
        || DataType.NUMBER_INTEGER.equals(typeField.getType()))) {
      queryBuilder.append(SORT_NUMBER_QUERY);
    } else if (typeField != null && DataType.DATE.equals(typeField.getType())) {
      queryBuilder.append(SORT_DATE_QUERY);
    } else {
      queryBuilder.append(SORT_STRING_QUERY);
    }
    if (StringUtils.isNotBlank(conditionalSchemaId)) {
      queryBuilder.append(QUERY_2_WITH_CONDITIONAL).append(QUERY_3).append(QUERY_3_CONDITIONAL);
    } else {
      queryBuilder.append(QUERY_2_WITHOUT_CONDITIONAL).append(QUERY_3);
    }
    queryBuilder.append(QUERY_ORDER);

    Query query = entityManager.createQuery(queryBuilder.toString());
    query.setParameter("fieldSchemaId", idPk);
    query.setParameter("labelId", labelSchemaId);
    query.setParameter("searchText", searchValue);
    if (StringUtils.isNotBlank(conditionalSchemaId)) {
      query.setParameter("conditionalId", conditionalSchemaId);
      query.setParameter("conditionalValue", conditionalValue);
    }
    query.setMaxResults(resultsNumber);

    List<Object[]> sqlResults = query.getResultList();

    // Map the results of the query
    for (Object[] row : sqlResults) {
      FieldValueWithLabel fv = new FieldValueWithLabel();
      fv.setFieldValue((FieldValue) row[0]);
      fv.setLabel((FieldValue) row[1]);
      fields.add(fv);
    }
    fields.stream().forEach(fExtended -> {
      if (fExtended != null) {
        FieldVO fieldVO = fieldNoValidationMapper.entityToClass(fExtended.getFieldValue());
        fieldVO.setLabel(fExtended.getLabel().getValue());
        fieldsVO.add(fieldVO);
      }
    });

    // Remove the duplicate values
    HashSet<String> seen = new HashSet<>();
    fieldsVO.removeIf(e -> !seen.add(e.getValue()));

    return fieldsVO;
  }
}
