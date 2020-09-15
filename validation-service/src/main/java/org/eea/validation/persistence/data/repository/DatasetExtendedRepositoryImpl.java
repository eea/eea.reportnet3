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
import org.eea.interfaces.vo.dataset.FieldVO;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.hibernate.Session;
import org.hibernate.jdbc.ReturningWork;

public class DatasetExtendedRepositoryImpl implements DatasetExtendedRepository {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public TableVO queryRSExecution(String query) {
    Session session = (Session) entityManager.getDelegate();
    return session.doReturningWork(new ReturningWork<TableVO>() {
      @Override
      public TableVO execute(Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
          ResultSet rs = stmt.executeQuery();
          ResultSetMetaData rsmd = rs.getMetaData();
          TableVO tableVO = new TableVO();
          List<RecordVO> records = new ArrayList<>();

          while (rs.next()) {
            RecordVO record = new RecordVO();
            tableVO.setId(Long.parseLong(rs.getString(3)));
            record.setId(rs.getString(1));
            record.setIdRecordSchema(rs.getString(2));
            record.setDatasetPartitionId(Long.parseLong(rs.getString(4)));
            record.setDataProviderCode(rs.getString(5));

            if (null != rs.getString(6)) {
              List<FieldVO> fields = new ArrayList<>();
              for (int fieldcolumninit = 6; fieldcolumninit < rsmd
                  .getColumnCount(); fieldcolumninit += 4) {
                FieldVO field = new FieldVO();
                field.setId(rs.getString(fieldcolumninit));
                field.setIdFieldSchema(rs.getString(fieldcolumninit + 1));
                field.setValue(rs.getString(fieldcolumninit + 2));
                switch (DataType.valueOf(rs.getString(fieldcolumninit + 3))) {
                  case ATTACHMENT:
                    field.setType(DataType.ATTACHMENT);
                    break;
                  case BOOLEAN:
                    field.setType(DataType.BOOLEAN);
                    break;
                  case CIRCLE:
                    field.setType(DataType.CIRCLE);
                    break;
                  case CODELIST:
                    field.setType(DataType.CODELIST);
                    break;
                  case COORDINATE_LAT:
                    field.setType(DataType.COORDINATE_LAT);
                    break;
                  case COORDINATE_LONG:
                    field.setType(DataType.COORDINATE_LONG);
                    break;
                  case DATE:
                    field.setType(DataType.DATE);
                    break;
                  case EMAIL:
                    field.setType(DataType.EMAIL);
                    break;
                  case LINK:
                    field.setType(DataType.LINK);
                    break;
                  case LINK_DATA:
                    field.setType(DataType.LINK_DATA);
                    break;
                  case LONG_TEXT:
                    field.setType(DataType.LONG_TEXT);
                    break;
                  case MULTISELECT_CODELIST:
                    field.setType(DataType.MULTISELECT_CODELIST);
                    break;
                  case NUMBER_DECIMAL:
                    field.setType(DataType.NUMBER_DECIMAL);
                    break;
                  case NUMBER_INTEGER:
                    field.setType(DataType.NUMBER_INTEGER);
                    break;
                  case PHONE:
                    field.setType(DataType.PHONE);
                    break;
                  case POINT:
                    field.setType(DataType.POINT);
                    break;
                  case POLYGON:
                    field.setType(DataType.POLYGON);
                    break;
                  case URL:
                    field.setType(DataType.URL);
                    break;
                  case TEXT:
                    field.setType(DataType.TEXT);
                    break;
                  default:
                    field.setType(DataType.TEXT);
                    break;
                }
                fields.add(field);
              }
              record.setFields(fields);
            }

            records.add(record);
            tableVO.setRecords(records);
          }
          return tableVO;
        }
      }
    });
  }

}
