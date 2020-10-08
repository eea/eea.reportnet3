package org.eea.validation.persistence.data.repository;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.validation.persistence.data.domain.FieldValidation;
import org.eea.validation.persistence.data.domain.FieldValue;
import org.eea.validation.persistence.data.domain.RecordValidation;
import org.eea.validation.persistence.data.domain.RecordValue;
import org.eea.validation.persistence.data.domain.TableValue;
import org.eea.validation.persistence.data.domain.Validation;
import org.hibernate.Session;
import org.hibernate.jdbc.ReturningWork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class DatasetExtendedRepositoryImpl.
 */
public class DatasetExtendedRepositoryImpl implements DatasetExtendedRepository {

  private static final Logger LOG = LoggerFactory.getLogger(DatasetExtendedRepositoryImpl.class);


  /** The entity manager. */
  @PersistenceContext(unitName = "dataSetsEntityManagerFactory")
  private EntityManager entityManager;


  @Override
  public Long getTableId(String idTableSchema, Long datasetId) {
    String stringQuery = "select id from dataset_" + datasetId
        + ".table_value where id_table_schema = '" + idTableSchema + "'";
    Query query = entityManager.createNativeQuery(stringQuery);
    BigInteger result = (BigInteger) query.getSingleResult();
    return result.longValue();
  }



  /**
   * Query RS execution.
   *
   * @param query the query
   * @return the table value
   */
  @Override
  @Transactional
  public TableValue queryRSExecution(String query, EntityTypeEnum entityTypeEnum, String entityName,
      Long datasetId, Long idTable) throws SQLException {
    Session session = (Session) entityManager.getDelegate();
    return session.doReturningWork(new ReturningWork<TableValue>() {
      @Override
      public TableValue execute(Connection conn) throws SQLException {
        conn.setSchema("dataset_" + datasetId);
        try (PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();) {
          LOG.info("Query: {}", query);
          TableValue tableValue = new TableValue();
          List<RecordValue> records = new ArrayList<>();

          while (rs.next()) {
            RecordValue record = new RecordValue();
            tableValue.setId(idTable);
            List<FieldValue> fields = new ArrayList<>();
            switch (entityTypeEnum) {
              case RECORD:
                record.setId(rs.getString("record_id"));
                record.setFields(fields);
                records.add(record);
                tableValue.setRecords(records);
                break;
              case FIELD:
                record.setId(rs.getString("record_id"));
                FieldValue field = new FieldValue();
                field.setId(rs.getString(entityName + "_id"));
                fields.add(field);
                record.setFields(fields);
                records.add(record);
                tableValue.setRecords(records);
                break;
              case TABLE:
                break;
              case DATASET:
                break;
            }

          }

          return tableValue;
        }
      }
    });

  }


  /**
   * Query unique result execution.
   *
   * @param stringQuery the string query
   * @return the list
   */
  @Override
  public List<Object> queryUniqueResultExecution(String stringQuery) {
    Query query = entityManager.createNativeQuery(stringQuery.toLowerCase());
    return query.getResultList();
  }


  /**
   * Query record validation execution.
   *
   * @param query the query
   * @return the list
   */
  @Override
  public List<RecordValidation> queryRecordValidationExecution(String query) {

    Session session = (Session) entityManager.getDelegate();
    return session.doReturningWork(new ReturningWork<List<RecordValidation>>() {
      @Override
      public List<RecordValidation> execute(Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();) {
          List<RecordValidation> recordValidations = new ArrayList<>();
          while (rs.next()) {
            RecordValidation recordValidation = new RecordValidation();
            recordValidation.setId(Long.parseLong(rs.getString(1)));

            Validation validation = new Validation();
            validation.setId(Long.parseLong(rs.getString(2)));
            validation.setIdRule(rs.getString(6));
            validation.setLevelError(ErrorTypeEnum.valueOf(rs.getString(7)));
            validation.setMessage(rs.getString(8));
            validation.setOriginName(rs.getString(9));
            validation.setTypeEntity(EntityTypeEnum.valueOf(rs.getString(10)));
            validation.setValidationDate(rs.getString(11));

            RecordValue record = new RecordValue();
            record.setId(rs.getString(3));
            record.setDataProviderCode(rs.getString(12));
            record.setDatasetPartitionId(Long.parseLong(rs.getString(13)));
            record.setIdRecordSchema(rs.getString(14));

            TableValue table = new TableValue();
            table.setId(Long.parseLong(rs.getString(15)));

            record.setTableValue(table);

            recordValidation.setRecordValue(record);
            recordValidation.setValidation(validation);

            recordValidations.add(recordValidation);
          }
          return recordValidations;
        }
      }
    });
  }

  /**
   * Query field validation execution.
   *
   * @param query the query
   * @return the list
   */
  @Override
  public List<FieldValidation> queryFieldValidationExecution(String query) {

    Session session = (Session) entityManager.getDelegate();
    return session.doReturningWork(new ReturningWork<List<FieldValidation>>() {
      @Override
      public List<FieldValidation> execute(Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();) {

          List<FieldValidation> fieldValidations = new ArrayList<>();
          while (rs.next()) {
            FieldValidation fieldValidation = new FieldValidation();
            fieldValidation.setId(Long.parseLong(rs.getString(1)));

            Validation validation = new Validation();
            validation.setId(Long.parseLong(rs.getString(2)));
            validation.setIdRule(rs.getString(6));
            validation.setLevelError(ErrorTypeEnum.valueOf(rs.getString(7)));
            validation.setMessage(rs.getString(8));
            validation.setOriginName(rs.getString(9));
            validation.setTypeEntity(EntityTypeEnum.valueOf(rs.getString(10)));
            validation.setValidationDate(rs.getString(11));


            FieldValue field = new FieldValue();
            field.setId(rs.getString(3));
            field.setIdFieldSchema(rs.getString(12));
            field.setType(rs.getString(14));
            field.setValue(rs.getString(15));

            RecordValue record = new RecordValue();
            record.setId(rs.getString(13));

            field.setRecord(record);

            fieldValidation.setValidation(validation);
            fieldValidation.setFieldValue(field);

            fieldValidations.add(fieldValidation);
          }
          return fieldValidations;
        }
      }
    });
  }



}
