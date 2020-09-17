package org.eea.validation.persistence.data.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.eea.interfaces.vo.dataset.enums.DataType;
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

/**
 * The Class DatasetExtendedRepositoryImpl.
 */
public class DatasetExtendedRepositoryImpl implements DatasetExtendedRepository {

  /** The entity manager. */
  @PersistenceContext
  private EntityManager entityManager;

  /**
   * Query RS execution.
   *
   * @param query the query
   * @return the table value
   */
  @Override
  public TableValue queryRSExecution(String query) {
    Session session = (Session) entityManager.getDelegate();
    return session.doReturningWork(new ReturningWork<TableValue>() {
      @Override
      public TableValue execute(Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
          ResultSet rs = stmt.executeQuery();
          ResultSetMetaData rsmd = rs.getMetaData();
          TableValue tableValue = new TableValue();
          List<RecordValue> records = new ArrayList<>();

          while (rs.next()) {
            RecordValue record = new RecordValue();
            tableValue.setId(Long.parseLong(rs.getString(3)));
            record.setId(rs.getString(1));
            record.setIdRecordSchema(rs.getString(2));
            record.setDatasetPartitionId(Long.parseLong(rs.getString(4)));
            record.setDataProviderCode(rs.getString(5));

            if (null != rs.getString(6)) {
              List<FieldValue> fields = new ArrayList<>();
              for (int fieldcolumninit = 6; fieldcolumninit < rsmd
                  .getColumnCount(); fieldcolumninit += 4) {
                FieldValue field = new FieldValue();
                field.setId(rs.getString(fieldcolumninit));
                field.setIdFieldSchema(rs.getString(fieldcolumninit + 1));
                field.setValue(rs.getString(fieldcolumninit + 2));
                switch (DataType.valueOf(rs.getString(fieldcolumninit + 3))) {
                  case ATTACHMENT:
                    field.setType(DataType.ATTACHMENT.toString());
                    break;
                  case BOOLEAN:
                    field.setType(DataType.BOOLEAN.toString());
                    break;
                  case CODELIST:
                    field.setType(DataType.CODELIST.toString());
                    break;
                  case DATE:
                    field.setType(DataType.DATE.toString());
                    break;
                  case EMAIL:
                    field.setType(DataType.EMAIL.toString());
                    break;
                  case LINK:
                    field.setType(DataType.LINK.toString());
                    break;
                  case LINK_DATA:
                    field.setType(DataType.LINK_DATA.toString());
                    break;
                  case LONG_TEXT:
                    field.setType(DataType.LONG_TEXT.toString());
                    break;
                  case MULTISELECT_CODELIST:
                    field.setType(DataType.MULTISELECT_CODELIST.toString());
                    break;
                  case NUMBER_DECIMAL:
                    field.setType(DataType.NUMBER_DECIMAL.toString());
                    break;
                  case NUMBER_INTEGER:
                    field.setType(DataType.NUMBER_INTEGER.toString());
                    break;
                  case PHONE:
                    field.setType(DataType.PHONE.toString());
                    break;
                  case URL:
                    field.setType(DataType.URL.toString());
                    break;
                  case TEXT:
                    field.setType(DataType.TEXT.toString());
                    break;
                  default:
                    field.setType(DataType.TEXT.toString());
                    break;
                }
                fields.add(field);
              }
              record.setFields(fields);
            }

            records.add(record);
            tableValue.setRecords(records);
          }
          return tableValue;
        }
      }
    });
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
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
          ResultSet rs = stmt.executeQuery();
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
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
          ResultSet rs = stmt.executeQuery();

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
