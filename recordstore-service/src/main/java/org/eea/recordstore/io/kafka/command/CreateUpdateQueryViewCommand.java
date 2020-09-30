package org.eea.recordstore.io.kafka.command;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eea.interfaces.controller.dataset.DatasetSchemaController.DatasetSchemaControllerZuul;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.recordstore.exception.RecordStoreAccessException;
import org.eea.recordstore.service.RecordStoreService;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * The Class CreateUpdateQueryViewCommand.
 */
@Component
public class CreateUpdateQueryViewCommand extends AbstractEEAEventHandlerCommand {

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(CreateUpdateQueryViewCommand.class);

  /** The Constant QUERY_FILTER_BY_ID_RECORD: {@value}. */
  private static final String QUERY_FILTER_BY_ID_RECORD =
      ".field_value fv where fv.id_record=rv.id and fv.id_field_schema = '";


  /** The Constant AS: {@value}. */
  private static final String AS = "') AS ";


  /** The Constant AS: {@value}. */
  private static final String COMMA = ", ";


  /** The record store service. */
  @Autowired
  private RecordStoreService recordStoreService;

  /** The dataset schema controller. */
  @Autowired
  private DatasetSchemaControllerZuul datasetSchemaController;


  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.CREATE_UPDATE_RULE_EVENT;
  }

  /**
   * Execute.
   *
   * @param eeaEventVO the eea event VO
   */
  @Override
  public void execute(EEAEventVO eeaEventVO) {
    String stringDataset = (String) eeaEventVO.getData().get(LiteralConstants.DATASET_ID);
    Long datasetId = Long.valueOf(stringDataset);

    DataSetSchemaVO datasetSchema = datasetSchemaController.findDataSchemaByDatasetId(datasetId);

    datasetSchema.getTableSchemas().stream()
        .filter(table -> !CollectionUtils.isEmpty(table.getRecordSchema().getFieldSchema()))
        .forEach(table -> {
          List<FieldSchemaVO> columns = table.getRecordSchema().getFieldSchema();
          try {
            // create materialiced view of all tableSchemas
            executeViewQuery(columns, table.getNameTableSchema(), table.getIdTableSchema(),
                datasetId);
            // execute view permission
            executeViewPermissions(table.getNameTableSchema(), datasetId);
          } catch (RecordStoreAccessException e) {
            LOG_ERROR.error("Error creating Query view: {}", e.getMessage(), e);
          }
        });
  }

  /**
   * Execute view permissions.
   *
   * @param queryViewName the query view name
   * @param datasetId the dataset id
   * @throws RecordStoreAccessException the record store access exception
   */
  private void executeViewPermissions(String queryViewName, Long datasetId)
      throws RecordStoreAccessException {
    String querySelectPermission =
        "GRANT SELECT ON dataset_" + datasetId + "." + queryViewName + " TO validation";
    recordStoreService.executeQueryViewCommands(querySelectPermission);

    String queryDeletePermission =
        "GRANT DELETE ON dataset_" + datasetId + "." + queryViewName + " TO recordstore";
    recordStoreService.executeQueryViewCommands(queryDeletePermission);

  }

  /**
   * Query view query.
   *
   * @param columns the columns
   * @param queryViewName the query view name
   * @param idTableSchema the id table schema
   * @param datasetId the dataset id
   * @throws RecordStoreAccessException the record store access exception
   */
  private void executeViewQuery(List<FieldSchemaVO> columns, String queryViewName,
      String idTableSchema, Long datasetId) throws RecordStoreAccessException {

    List<String> stringColumns = new ArrayList<>();
    for (FieldSchemaVO column : columns) {
      stringColumns.add(column.getId());
    }

    StringBuilder stringQuery = new StringBuilder("CREATE OR REPLACE VIEW dataset_" + datasetId
        + "." + "\"" + queryViewName + "\"" + " as (select rv.id as record_id, ");
    Iterator<String> iterator = stringColumns.iterator();
    int i = 0;
    while (iterator.hasNext()) {
      String schemaId = iterator.next();
      // id
      stringQuery.append("(select fv.id from dataset_" + datasetId + QUERY_FILTER_BY_ID_RECORD)
          .append(schemaId).append(AS).append("\"").append(columns.get(i).getName()).append("_id")
          .append("\" ");
      stringQuery.append(COMMA);
      // value
      DataType type = DataType.TEXT;
      for (FieldSchemaVO column : columns) {
        if (column.getId().equals(schemaId)) {
          type = column.getType();
        }
      }
      switch (type) {
        case DATE:
          stringQuery
              .append("(select CAST(fv.value as date) from dataset_" + datasetId
                  + QUERY_FILTER_BY_ID_RECORD)
              .append(schemaId).append(AS).append("\"").append(columns.get(i).getName())
              .append("\" ");
          break;
        case NUMBER_DECIMAL:
        case NUMBER_INTEGER:
          stringQuery
              .append("(select CAST(fv.value as numeric) from dataset_" + datasetId
                  + QUERY_FILTER_BY_ID_RECORD)
              .append(schemaId).append(AS).append("\"").append(columns.get(i).getName())
              .append("\" ");
          break;
        default:
          stringQuery
              .append("(select fv.value from dataset_" + datasetId + QUERY_FILTER_BY_ID_RECORD)
              .append(schemaId).append(AS).append("\"").append(columns.get(i).getName())
              .append("\" ");
          break;
      }
      if (iterator.hasNext()) {
        stringQuery.append(COMMA);
      }
      i++;
    }
    stringQuery.append(" from dataset_" + datasetId + ".record_value rv");
    stringQuery.append(" inner join dataset_" + datasetId
        + ".table_value tv on rv.id_table = tv.id where tv.id_table_schema = '" + idTableSchema
        + "')");

    recordStoreService.executeQueryViewCommands(stringQuery.toString());
  }
}


