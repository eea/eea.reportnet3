package org.eea.validation.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.bson.types.ObjectId;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.FieldVO;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.thread.ThreadPropertiesManager;
import org.eea.validation.persistence.repository.RulesRepository;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.hibernate.Session;
import org.hibernate.jdbc.ReturningWork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * The Class SQLValitaionUtils.
 */
@Component
public class SQLValitaionUtils {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(KieBaseManager.class);

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant AS: {@value}. */
  private static final String AS = "') AS ";


  private static final String DATASET_QUERY = "dataset_";

  private static final String UNDERSCORE = "_";

  private static final String DOT = ".";


  /** The entity manager. */
  @PersistenceContext
  private static EntityManager entityManager;

  /** The rules repository. */
  @Autowired
  private RulesRepository rulesRepository;

  /** The kafka sender utils. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /**
   * Validate SQL rule.
   *
   * @param datasetSchemaId the dataset schema id
   * @param rule the rule
   */
  @Async
  @SuppressWarnings("unchecked")
  public void validateSQLRule(String datasetSchemaId, Rule rule) {

    EventType notificationEventType = null;
    NotificationVO notificationVO = NotificationVO.builder()
        .user((String) ThreadPropertiesManager.getVariable("user")).datasetSchemaId(datasetSchemaId)
        .shortCode(rule.getShortCode()).error("The QC Rule is disabled").build();

    if (null != null) {
      notificationEventType = EventType.VALIDATED_QC_RULE_EVENT;
      rule.setVerified(true);
      LOG.info("Rule validation passed: {}", rule);
    } else {
      notificationEventType = EventType.INVALIDATED_QC_RULE_EVENT;
      rule.setVerified(false);
      rule.setEnabled(false);
      LOG.info("Rule validation not passed: {}", rule);
    }

    rulesRepository.updateRule(new ObjectId(datasetSchemaId), rule);
    releaseNotification(notificationEventType, notificationVO);
  }


  /**
   * Release notification.
   *
   * @param eventType the event type
   * @param notificationVO the notification VO
   */
  private void releaseNotification(EventType eventType, NotificationVO notificationVO) {
    try {
      kafkaSenderUtils.releaseNotificableKafkaEvent(eventType, null, notificationVO);
    } catch (EEAException e) {
      LOG_ERROR.error("Unable to release notification: {}, {}", eventType, notificationVO);
    }
  }


  private static void executeSQL(String ruleId) {
    String sqlExample =
        "select t25.campo_de_contaminacion from dataset_256.table_25 t25 inner join dataset_125.table_del_aire ta on t25.id as ta.id";



    getListOfDatasets(sqlExample);

  }

  private static List<Integer> getListOfDatasets(String query) {
    List<Integer> datasetIdList = new ArrayList<>();

    String cadenaDondeBuscar = "sql";
    String loQueQuieroBuscar = "lenguaje sql";
    String[] palabras = loQueQuieroBuscar.split("\\s+");
    for (String palabra : palabras) {
      if (query.contains(palabra)) {
        System.out.println(palabra);
      }
    }


    datasetIdList.add(
        Integer.valueOf(query.substring(1 + query.indexOf(UNDERSCORE, query.indexOf(DATASET_QUERY)),
            query.indexOf(DOT, query.indexOf(DATASET_QUERY)))));

    return datasetIdList;
  }


  private static String createWithClause(List<FieldSchemaVO> columns, String tableName,
      String idTableSchema, Long datasetId) {

    List<String> stringColumns = new ArrayList<>();
    for (FieldSchemaVO column : columns) {
      stringColumns.add(column.getId());
    }

    StringBuilder stringQuery = new StringBuilder(tableName + " as (select rv.id, ");
    stringQuery.append("rv.id_record_schema,");
    stringQuery.append("rv.id_table,");
    stringQuery.append("rv.dataset_partition_id,");
    stringQuery.append("rv.data_provider_code,");
    Iterator<String> iterator = stringColumns.iterator();
    int i = 0;
    while (iterator.hasNext()) {
      String schemaId = iterator.next();
      stringQuery
          .append("(select fv.id from dataset_" + datasetId
              + ".field_value fv where fv.id_record=rv.id and fv.id_field_schema = '")
          .append(schemaId).append(AS).append(columns.get(i).getName()).append("_id");
      stringQuery.append(",");
      stringQuery
          .append("(select fv.id_field_schema from dataset_" + datasetId
              + ".field_value fv where fv.id_record=rv.id and fv.id_field_schema = '")
          .append(schemaId).append(AS).append(columns.get(i).getName()).append("_id_field_schema");
      stringQuery.append(",");
      stringQuery
          .append("(select fv.value from dataset_" + datasetId
              + ".field_value fv where fv.id_record=rv.id and fv.id_field_schema = '")
          .append(schemaId).append(AS).append(columns.get(i).getName());
      stringQuery.append(",");
      stringQuery
          .append("(select fv.type from dataset_" + datasetId
              + ".field_value fv where fv.id_record=rv.id and fv.id_field_schema = '")
          .append(schemaId).append(AS).append(columns.get(i).getName()).append("_type");
      if (iterator.hasNext()) {
        stringQuery.append(",");
      }
      i++;
    }
    stringQuery.append(" from dataset_" + datasetId + ".record_value rv");
    stringQuery.append(" inner join dataset_" + datasetId
        + ".table_value tv on rv.id_table = tv.id where tv.id_table_schema = '" + idTableSchema
        + "')");

    return stringQuery.toString();
  }

  private static TableVO queryRSExecution(String stringQuery) {
    Session session = (Session) entityManager.getDelegate();
    return session.doReturningWork(new ReturningWork<TableVO>() {
      @Override
      public TableVO execute(Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(stringQuery)) {
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
