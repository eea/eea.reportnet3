package org.eea.recordstore.io.kafka.command;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.transaction.Transactional;
import org.eea.interfaces.controller.dataset.DatasetSchemaController.DatasetSchemaControllerZuul;
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

@Component
public class CreateUpdateQueryViewCommand extends AbstractEEAEventHandlerCommand {

  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  private static final Logger LOG = LoggerFactory.getLogger(CreateUpdateQueryViewCommand.class);

  private static final String QUERY_FILTER_BY_ID_RECORD =
      ".field_value fv where fv.id_record=rv.id and fv.id_field_schema = '";


  /** The Constant AS: {@value}. */
  private static final String AS = "') AS ";


  /** The Constant AS: {@value}. */
  private static final String COMMA = ", ";


  @Autowired
  private RecordStoreService recordStoreService;

  @Autowired
  private DatasetSchemaControllerZuul datasetSchemaController;


  @Override
  public EventType getEventType() {
    return EventType.COMMAND_EXECUTE_VALIDATION;
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
            queryViewQuery(columns, table.getNameTableSchema(), table.getIdTableSchema(),
                datasetId);
          } catch (RecordStoreAccessException e) {
            e.printStackTrace();
          }
        });
  }

  @Transactional
  private void queryViewQuery(List<FieldSchemaVO> columns, String queryViewName,
      String idTableSchema, Long datasetId) throws RecordStoreAccessException {

    List<String> stringColumns = new ArrayList<>();
    for (FieldSchemaVO column : columns) {
      stringColumns.add(column.getId());
    }

    StringBuilder stringQuery = new StringBuilder("CREATE OR REPLACE VIEW dataset_" + datasetId
        + "." + queryViewName + " as (select rv.id, ");
    stringQuery.append("rv.id_record_schema,");
    stringQuery.append("rv.id_table,");
    stringQuery.append("rv.dataset_partition_id,");
    stringQuery.append("rv.data_provider_code,");
    Iterator<String> iterator = stringColumns.iterator();
    int i = 0;
    while (iterator.hasNext()) {
      String schemaId = iterator.next();
      stringQuery.append("(select fv.id from dataset_" + datasetId + QUERY_FILTER_BY_ID_RECORD)
          .append(schemaId).append(AS).append(columns.get(i).getName()).append("_id");
      stringQuery.append(COMMA);
      stringQuery
          .append(
              "(select fv.id_field_schema from dataset_" + datasetId + QUERY_FILTER_BY_ID_RECORD)
          .append(schemaId).append(AS).append(columns.get(i).getName()).append("_id_field_schema");
      stringQuery.append(COMMA);
      stringQuery.append("(select fv.value from dataset_" + datasetId + QUERY_FILTER_BY_ID_RECORD)
          .append(schemaId).append(AS).append(columns.get(i).getName());
      stringQuery.append(COMMA);
      stringQuery.append("(select fv.type from dataset_" + datasetId + QUERY_FILTER_BY_ID_RECORD)
          .append(schemaId).append(AS).append(columns.get(i).getName()).append("_type");
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


