package org.eea.validation.persistence.data.repository;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import org.eea.interfaces.vo.dataset.ValueVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.validation.exception.EEAInvalidSQLException;
import org.eea.validation.persistence.data.domain.FieldValidation;
import org.eea.validation.persistence.data.domain.FieldValue;
import org.eea.validation.persistence.data.domain.RecordValidation;
import org.eea.validation.persistence.data.domain.RecordValue;
import org.eea.validation.persistence.data.domain.TableValue;
import org.eea.validation.persistence.data.domain.Validation;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.jdbc.ReturningWork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;

/**
 * The Class DatasetExtendedRepositoryImpl.
 */
public class DatasetExtendedRepositoryImpl implements DatasetExtendedRepository {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(DatasetExtendedRepositoryImpl.class);

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant RECORD_ID: {@value}. */
  private static final String RECORD_ID = "record_id";

  /** The geometry error message. */
  @Value("${query.message.geometry.error}")
  private String geometryErrorMessage;

  /** The field batch size. */
  @Value("${validation.sqlFetchSize}")
  private int sqlFetchSize;

  /** The entity manager. */
  @PersistenceContext(unitName = "dataSetsEntityManagerFactory")
  private EntityManager entityManager;

  /**
   * Gets the table id.
   *
   * @param idTableSchema the id table schema
   * @param datasetId the dataset id
   * @return the table id
   */
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
   * @param entityTypeEnum the entity type enum
   * @param entityName the entity name
   * @param datasetId the dataset id
   * @param idTable the id table
   * @return the table value
   * @throws EEAInvalidSQLException the EEA invalid SQL exception
   */
  @Override
  @Transactional
  public TableValue queryRSExecution(String query, EntityTypeEnum entityTypeEnum, String entityName,
      Long datasetId, Long idTable) throws EEAInvalidSQLException {
    try {
      Session session = (Session) entityManager.getDelegate();
      return session.doReturningWork(
          conn -> executeQuery(conn, entityName, query, entityTypeEnum, datasetId, idTable));
    } catch (HibernateException e) {
      throw new EEAInvalidSQLException("SQL can't be executed: " + query, e);
    } finally {
      System.gc();
    }
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
            validation.setTableName(rs.getString(9));
            validation.setFieldName(rs.getString(10));
            validation.setShortCode(rs.getString(11));
            validation.setTypeEntity(EntityTypeEnum.valueOf(rs.getString(12)));
            validation.setValidationDate(rs.getString(13));

            RecordValue record = new RecordValue();
            record.setId(rs.getString(3));
            record.setDataProviderCode(rs.getString(14));
            record.setDatasetPartitionId(Long.parseLong(rs.getString(15)));
            record.setIdRecordSchema(rs.getString(16));

            TableValue table = new TableValue();
            table.setId(Long.parseLong(rs.getString(17)));

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
            validation.setTableName(rs.getString(9));
            validation.setFieldName(rs.getString(10));
            validation.setShortCode(rs.getString(11));
            validation.setTypeEntity(EntityTypeEnum.valueOf(rs.getString(12)));
            validation.setValidationDate(rs.getString(13));

            FieldValue field = new FieldValue();
            field.setId(rs.getString(3));
            field.setIdFieldSchema(rs.getString(14));
            field.setType(DataType.fromValue(rs.getString(16)));
            field.setValue(rs.getString(17));

            RecordValue record = new RecordValue();
            record.setId(rs.getString(15));

            field.setRecord(record);

            fieldValidation.setValidation(validation);
            fieldValidation.setFieldValue(field);

            fieldValidations.add(fieldValidation);
          }
          return fieldValidations;
        } finally {
          System.gc();
        }
      }
    });
  }

  /**
   * Execute query.
   *
   * @param conn the conn
   * @param entityName the entity name
   * @param query the query
   * @param entityTypeEnum the entity type enum
   * @param datasetId the dataset id
   * @param idTable the id table
   * @return the table value
   * @throws SQLException the SQL exception
   */
  private TableValue executeQuery(Connection conn, String entityName, String query,
      EntityTypeEnum entityTypeEnum, Long datasetId, Long idTable) throws SQLException {
    conn.setSchema("dataset_" + datasetId);
    TableValue tableValue;
    conn.setAutoCommit(false);
    Statement stmt = conn.createStatement();
    stmt.setFetchSize(sqlFetchSize);
    try (ResultSet rs = stmt.executeQuery(query)) {
      ResultSetMetaData rsm = rs.getMetaData();
      LOG.info("Query executed: {}", query);
      tableValue = new TableValue();
      List<RecordValue> records = new ArrayList<>();
      boolean continueLoop = true;
      while (rs.next() && continueLoop) {
        RecordValue record = new RecordValue();
        tableValue.setId(idTable);
        List<FieldValue> fields = new ArrayList<>();
        switch (entityTypeEnum) {
          case RECORD:
            getTableValue(tableValue, rs, rsm, records, record);
            break;
          case FIELD:
            record.setId(rs.getString(RECORD_ID));
            FieldValue field = new FieldValue();
            field.setId(rs.getString(entityName + "_id"));
            fields.add(field);
            for (int indexCol = 1; indexCol <= rsm.getColumnCount(); indexCol++) {
              FieldValue auxField2 = new FieldValue();
              auxField2.setColumnName(rsm.getColumnName(indexCol));
              // ColumnType = 1111 -> is Postgres Geometry type
              if (rsm.getColumnType(indexCol) == 1111) {
                try {
                  // we try to convert Extended Well-Known Binary (EWKB) into String
                  // Example : 0101000020E610000095B9F94674CF37C09CBF0985083B5040 -> Point (1,2)
                  // RSID : 4236
                  final GeometryFactory gm = new GeometryFactory(new PrecisionModel(), 4326);
                  final WKBReader wkbr = new WKBReader(gm);
                  byte[] wkbBytes = wkbr.hexToBytes(rs.getString(indexCol));
                  Geometry geom = wkbr.read(wkbBytes);
                  auxField2.setValue(geom.toText());
                } catch (ParseException | NullPointerException e) {
                  auxField2.setValue(geometryErrorMessage);
                }
              } else {
                auxField2.setValue(rs.getString(indexCol));
              }
              auxField2.setRecord(record);
              fields.add(auxField2);
            }
            record.setFields(fields);
            records.add(record);
            tableValue.setRecords(records);
            break;
          case TABLE:
            continueLoop = false;
            getTableValue(tableValue, rs, rsm, records, record);
            break;
          default:
            break;
        }
      }
      stmt.setFetchSize(0);
    } finally {
      stmt.close();
      conn.setAutoCommit(true);
    }
    System.gc();
    return tableValue;
  }

  /**
   * Gets the table value.
   *
   * @param tableValue the table value
   * @param rs the rs
   * @param rsm the rsm
   * @param records the records
   * @param record the record
   * @return the table value
   * @throws SQLException the SQL exception
   */
  private void getTableValue(TableValue tableValue, ResultSet rs, ResultSetMetaData rsm,
      List<RecordValue> records, RecordValue record) throws SQLException {
    record.setId(rs.getString(RECORD_ID));
    ArrayList<FieldValue> auxFields = new ArrayList<>();
    for (int indexCol = 1; indexCol <= rsm.getColumnCount(); indexCol++) {
      FieldValue auxField = new FieldValue();
      auxField.setColumnName(rsm.getColumnName(indexCol));
      // ColumnType = 1111 -> is Postgres Geometry type
      if (rsm.getColumnType(indexCol) == 1111) {
        try {
          // we try to convert Extended Well-Known Binary (EWKB) into String
          // Example : 0101000020E610000095B9F94674CF37C09CBF0985083B5040 -> Point (1,2)
          // RSID : 4236
          final GeometryFactory gm = new GeometryFactory(new PrecisionModel(), 4326);
          final WKBReader wkbr = new WKBReader(gm);
          byte[] wkbBytes = wkbr.hexToBytes(rs.getString(indexCol));
          Geometry geom = wkbr.read(wkbBytes);
          auxField.setValue(geom.toText());
        } catch (ParseException | NullPointerException e) {
          auxField.setValue(geometryErrorMessage);
        }
      } else {
        auxField.setValue(rs.getString(indexCol));
      }
      auxField.setRecord(record);
      auxFields.add(auxField);
    }
    record.setFields(auxFields);
    records.add(record);
    tableValue.setRecords(records);
  }

  /**
   * Validate query.
   *
   * @param query the query
   * @param datasetId the dataset id
   * @throws EEAInvalidSQLException the EEA invalid SQL exception
   */
  @Override
  @Transactional
  public void validateQuery(String query, Long datasetId) throws EEAInvalidSQLException {
    try {
      Session session = (Session) entityManager.getDelegate();
      session.doReturningWork(new ReturningWork<ResultSet>() {
        @Override
        public ResultSet execute(Connection conn) throws SQLException {
          conn.setSchema("dataset_" + datasetId);
          try (PreparedStatement stmt = conn.prepareStatement(query);
              ResultSet rs = stmt.executeQuery();) {
            LOG.info("executing query: {}", query);
            return rs;
          }
        }
      });
    } catch (HibernateException e) {
      throw new EEAInvalidSQLException("SQL not valid: " + query, e);
    }

  }

  /**
   * Run SQL rule with limited results.
   *
   * @param datasetId the dataset id
   * @param sqlRule the sql rule about to be run
   * @return the string formatted as JSON
   * @throws EEAInvalidSQLException the EEA invalid SQL exception
   */
  @Override
  @Transactional
  public List<List<ValueVO>> runSqlRule(Long datasetId, String sqlRule)
      throws EEAInvalidSQLException {
    List<List<ValueVO>> tableValues = new ArrayList<>();

    try {
      Session session = (Session) entityManager.getDelegate();
      tableValues = session.doReturningWork(new ReturningWork<List<List<ValueVO>>>() {
        @Override
        public List<List<ValueVO>> execute(Connection conn) throws SQLException {
          List<List<ValueVO>> tableRows = new ArrayList<>();
          conn.setReadOnly(true);
          conn.setSchema("dataset_" + datasetId);
          try (PreparedStatement stmt = conn.prepareStatement(sqlRule);
              ResultSet rs = stmt.executeQuery();) {
            ResultSetMetaData rsmt = rs.getMetaData();
            int index = 1;
            while (rs.next()) {
              List<ValueVO> values = new ArrayList<>();
              for (int i = 1; i <= rsmt.getColumnCount(); i++) {
                ValueVO valueToAdd = new ValueVO();
                valueToAdd.setValue(rs.getString(i));
                valueToAdd.setLabel(rsmt.getColumnLabel(i));
                valueToAdd.setTable(rsmt.getTableName(i));
                valueToAdd.setRow(index);
                values.add(valueToAdd);
              }
              tableRows.add(values);
              index++;

            }
            LOG.info("executing query: {}", sqlRule);
            return tableRows;
          }
        }
      });
    } catch (HibernateException e) {
      throw new EEAInvalidSQLException("SQL not valid: " + sqlRule, e);
    }
    return tableValues;
  }

  /**
   * Evaluates the SQL Rule and returns its total cost
   *
   * @param datasetId the dataset id
   * @param sqlRule the sql rule about to be evaluated
   * @return the string formatted as JSON
   * @throws EEAInvalidSQLException the EEA invalid SQL exception
   */
  @Override
  @Transactional
  public String evaluateSqlRule(Long datasetId, String sqlRule) throws EEAInvalidSQLException {
    String result = "";

    try {
      Session session = (Session) entityManager.getDelegate();
      result = session.doReturningWork(new ReturningWork<String>() {
        @Override
        public String execute(Connection conn) throws SQLException {
          String resultObject = "";
          conn.setReadOnly(true);
          conn.setSchema("dataset_" + datasetId);
          try (PreparedStatement stmt = conn.prepareStatement(sqlRule);
              ResultSet rs = stmt.executeQuery();) {
            while (rs.next()) {
              resultObject = rs.getString(1);
            }
            LOG.info("executing query: {}", sqlRule);
            return resultObject;
          }
        }
      });
    } catch (HibernateException e) {
      throw new EEAInvalidSQLException("SQL not valid: " + sqlRule, e);
    }
    return result;
  }

}
