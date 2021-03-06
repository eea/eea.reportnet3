package org.eea.dataset.persistence.data.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.apache.commons.lang3.StringUtils;
import org.eea.dataset.mapper.FieldNoValidationMapper;
import org.eea.dataset.persistence.data.domain.FieldValue;
import org.eea.dataset.service.model.FieldValueWithLabel;
import org.eea.interfaces.vo.dataset.FieldVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.multitenancy.TenantResolver;
import org.eea.utils.LiteralConstants;
import org.hibernate.Session;
import org.hibernate.jdbc.ReturningWork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
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

  /** The entity manager. */
  @PersistenceContext(name = "dataSetsEntityManagerFactory")
  private EntityManager entityManager;

  /**
   * The field no validation mapper.
   */
  @Autowired
  private FieldNoValidationMapper fieldNoValidationMapper;


  /** The Constant QUERY_FIELD_SCHEMA_AND_VALUE: {@value}. */
  private static final String QUERY_FIELD_SCHEMA_AND_VALUE =
      "select * from dataset_%s.field_value where id_field_schema='%s' and value IN (%s)";

  /** The Constant QUERY_FIND_VALUE: {@value}. */
  private static final String QUERY_FIND_VALUE =
      "select table2.valuet2 as id,fv.value as value from dataset_%s.field_value fv "
          + "inner join (select fv2.id_record as idrecordt2,fv2.value as valuet2  from dataset_%s.field_value fv2 where "
          + "fv2.id_field_schema ='%s' and fv2.value IN(%s)) as table2 "
          + "on table2.idrecordt2 = fv.id_record where fv.id_field_schema ='%s'";

  /**
   * The Constant SORT_NUMBER_QUERY.
   */
  private static final String SORT_NUMBER_QUERY =
      ",CASE WHEN is_numeric(fv.value)= true THEN CAST(fv.value as java.math.BigDecimal) END as orden ";

  /**
   * The Constant SORT_DATE_QUERY.
   */
  private static final String SORT_DATE_QUERY = ", CASE WHEN is_date(fv.value)= true THEN CAST(fv.value as java.sql.Date) END as orden ";

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
          + " AND (:searchText IS NULL or LOWER(fv.value) like LOWER(CONCAT('%',:searchText,'%')) or LOWER(tag.value) like LOWER(CONCAT('%',:searchText,'%')) ) ";

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
   * The enum SortQueryType
   */
  private enum SortQueryType {
    NUMBER, DATE, STRING
  }


  /**
   * Find by id field schema with tag ordered.
   *
   * @param idPk the id pk
   * @param labelSchemaId the label schema id
   * @param searchValue the search value
   * @param conditionalSchemaId the conditional schema id
   * @param conditionalValue the conditional value
   * @param dataTypePk the data type pk
   * @param resultsNumber the results number
   * @return the list
   */
  @Override
  public List<FieldVO> findByIdFieldSchemaWithTagOrdered(String idPk, String labelSchemaId,
      String searchValue, String conditionalSchemaId, String conditionalValue, DataType dataTypePk,
      Integer resultsNumber) throws DataIntegrityViolationException {

    List<FieldVO> fieldsVO = new ArrayList<>();
    List<FieldValueWithLabel> fields = new ArrayList<>();

    StringBuilder queryBuilder = new StringBuilder();
    SortQueryType sortQueryType = SortQueryType.STRING;
    queryBuilder.append(QUERY_1);
    if (dataTypePk != null && (DataType.NUMBER_DECIMAL.equals(dataTypePk)
        || DataType.NUMBER_INTEGER.equals(dataTypePk))) {
      queryBuilder.append(SORT_NUMBER_QUERY);
      sortQueryType = SortQueryType.NUMBER;
    } else if (dataTypePk != null && DataType.DATE.equals(dataTypePk)) {
      queryBuilder.append(SORT_DATE_QUERY);
      sortQueryType = SortQueryType.DATE;
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

    // Remove all values that are not of the correct type
    if (sortQueryType == SortQueryType.NUMBER) {
      fieldsVO.removeIf(e -> !stringIsNumeric(e.getValue()));
    } else if (sortQueryType == SortQueryType.DATE) {
      fieldsVO.removeIf(e -> !stringIsDate(e.getValue()));
    }

    return fieldsVO;
  }


  /**
   * Query execution record.
   *
   * @param fieldSchemaId the id field schema
   * @param idsList the ids list
   * @param datasetId the dataset id
   * @return the list
   */
  @Override
  public List<FieldValue> findByFieldSchemaAndValue(String fieldSchemaId, List<String> idsList,
      Long datasetId) {
    String result = listToString(datasetId.toString(), idsList);
    String finalQuery =
        String.format(QUERY_FIELD_SCHEMA_AND_VALUE, datasetId.toString(), fieldSchemaId, result);
    Query query = entityManager.createNativeQuery(finalQuery, FieldValue.class);
    return query.getResultList();
  }


  /**
   * Query find value by other field.
   *
   * @param fieldSchemaId1 the id field schema 1
   * @param fieldSchemaId2 the id field schema 2
   * @param datasetId the dataset id
   * @param idsList the ids list
   * @return the list
   */
  @Override
  public List<FieldVO> findValue(String fieldSchemaId1, String fieldSchemaId2, String datasetId,
      List<String> idsList) {
    String result = listToString(datasetId, idsList);
    String finalQuery = String.format(QUERY_FIND_VALUE, datasetId, datasetId, fieldSchemaId1,
        result, fieldSchemaId2);
    Session session = (Session) entityManager.getDelegate();
    return session.doReturningWork(new ReturningWork<List<FieldVO>>() {
      @Override
      public List<FieldVO> execute(Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(finalQuery);
            ResultSet rs = stmt.executeQuery();) {
          List<FieldVO> fields = new ArrayList<>();
          while (rs.next()) {
            FieldVO field = new FieldVO();
            field.setId(rs.getString(1));
            field.setValue(rs.getString(2));
            fields.add(field);
          }
          return fields;
        }
      }
    });
  }

  /**
   * List to string.
   *
   * @param datasetId the dataset id
   * @param idsList the ids list
   * @return the string
   */
  private String listToString(String datasetId, List<String> idsList) {
    TenantResolver.setTenantName(LiteralConstants.DATASET_PREFIX + datasetId);
    UnaryOperator<String> addQuotes = s -> "'" + s + "'";
    return idsList.stream().map(addQuotes).collect(Collectors.joining(", "));
  }

  /**
   * Check if a String is Numeric
   * @param strNum String to check
   * @return true if is numeric
   */
  private boolean stringIsNumeric(String strNum) {
    if (strNum == null) {
      return false;
    }
    try {
      Double.parseDouble(strNum);
    } catch (NumberFormatException nfe) {
      return false;
    }
    return true;
  }

  /**
   * Check if a String is Date
   * @param dateStr String to check
   * @return true if is Date
   */
  private boolean stringIsDate(String dateStr) {
    DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    boolean isDate = true;
    try {
      sdf.parse(dateStr);
    } catch (ParseException e) {
      isDate = false;
    }
    return isDate;
  }


}
