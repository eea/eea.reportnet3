package org.eea.dataset.persistence.data.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.eea.dataset.persistence.data.domain.FieldValue;
import org.eea.interfaces.vo.dataset.FieldVO;
import org.eea.multitenancy.TenantResolver;
import org.eea.utils.LiteralConstants;
import org.hibernate.Session;
import org.hibernate.jdbc.ReturningWork;

/**
 * The Class FieldExtendedRepositoryImpl.
 */
public class FieldExtendedRepositoryImpl implements FieldExtendedRepository {

  /** The entity manager. */
  @PersistenceContext(name = "dataSetsEntityManagerFactory")
  private EntityManager entityManager;

  private static final String QUERY_FIELD_SCHEMA_AND_VALUE =
      "select * from dataset_%s.field_value where id_field_schema='%s' and value IN (%s)";

  private static final String QUERY_FIND_VALUE =
      "select table2.valuet2 as id,fv.value as value from dataset_%s.field_value fv "
          + "inner join (select fv2.id_record as idrecordt2,fv2.value as valuet2  from dataset_%s.field_value fv2 where "
          + "fv2.id_field_schema ='%s' and fv2.value IN(%s)) as table2 "
          + "on table2.idrecordt2 = fv.id_record where fv.id_field_schema ='%s'";


  /**
   * Query execution record.
   *
   * @param queryString the query string
   * @return the list
   */
  @Override
  public List<FieldValue> queryFindByFieldSchemaAndValue(String idFieldSchema, List<String> idsList,
      Long datasetId) {
    String result = listToString(datasetId.toString(), idsList);
    String finalQuery =
        String.format(QUERY_FIELD_SCHEMA_AND_VALUE, datasetId.toString(), idFieldSchema, result);
    Query query = entityManager.createNativeQuery(finalQuery, FieldValue.class);
    return query.getResultList();
  }


  @Override
  public List<FieldVO> queryFindValue(String idFieldSchema1, String idFieldSchema2,
      String datasetId, List<String> idsList) {
    String result = listToString(datasetId, idsList);
    String finalQuery = String.format(QUERY_FIND_VALUE, datasetId, datasetId, idFieldSchema1,
        result, idFieldSchema2);
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

  private String listToString(String datasetId, List<String> idsList) {
    TenantResolver.setTenantName(LiteralConstants.DATASET_PREFIX + datasetId);
    UnaryOperator<String> addQuotes = s -> "'" + s + "'";
    return idsList.stream().map(addQuotes).collect(Collectors.joining(", "));
  }


}
