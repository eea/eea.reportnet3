package org.eea.dataset.io.kafka.commands;

import java.util.ArrayList;
import java.util.List;
import javax.transaction.Transactional;
import org.bson.types.ObjectId;
import org.eea.dataset.persistence.data.domain.DatasetValue;
import org.eea.dataset.persistence.data.domain.FieldValue;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.persistence.data.repository.FieldRepository;
import org.eea.dataset.persistence.data.repository.RecordRepository;
import org.eea.dataset.persistence.data.repository.TableRepository;
import org.eea.dataset.persistence.metabase.domain.DesignDataset;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.DesignDatasetRepository;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.exception.EEAException;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.multitenancy.TenantResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SpreadDataCommand extends AbstractEEAEventHandlerCommand {

  @Autowired
  private DataSetMetabaseRepository dataSetMetabaseRepository;

  @Autowired
  private DesignDatasetRepository designDatasetRepository;

  @Autowired
  private TableRepository tableRepository;

  @Autowired
  private RecordRepository recordRepository;

  @Autowired
  private FieldRepository fieldRepository;

  @Autowired
  private SchemasRepository schemasRepository;

  private static final String DATASET_ID = "dataset_%s";

  @Override
  public EventType getEventType() {
    return EventType.SPREAD_DATA_EVENT;
  }

  @Override
  public void execute(EEAEventVO eeaEventVO) throws EEAException {
    if (EventType.SPREAD_DATA_EVENT.equals(eeaEventVO.getEventType())) {
      String stringDataset = (String) eeaEventVO.getData().get("dataset_id");
      String idDatasetSchema = (String) eeaEventVO.getData().get("idDatasetSchema");
      Long dataset = Long.valueOf(stringDataset);
      Long dataflowId = dataSetMetabaseRepository.findDataflowIdById(dataset);
      List<DesignDataset> designs = designDatasetRepository.findByDataflowId(dataflowId);
      boolean isdesing = false;
      for (DesignDataset design : designs) {
        if (design.getId().equals(dataset)) {
          isdesing = true;
        }
      }
      if (!isdesing) {
        spreadData(designs, dataset, idDatasetSchema);
      }
    }
  }

  @Transactional
  private void spreadData(List<DesignDataset> designs, Long dataset, String idDatasetSchema) {
    for (DesignDataset design : designs) {
      // get tables from schema
      DataSetSchema schema = schemasRepository.findByIdDataSetSchema(new ObjectId(idDatasetSchema));
      List<TableSchema> listOfTables = schema.getTableSchemas();


      // get the data from designs datasets
      TenantResolver.setTenantName(String.format(DATASET_ID, design.getId().toString()));
      List<RecordValue> redordDesignValues = new ArrayList<>();

      for (TableSchema desingTable : listOfTables) {
        TenantResolver.getTenantName();
        redordDesignValues.addAll(
            recordRepository.findByTableValueAllRecords(desingTable.getIdTableSchema().toString()));

      }
      List<RecordValue> redordDesignValuesList = new ArrayList<>();

      DatasetValue ds = new DatasetValue();
      ds.setId(dataset);

      for (RecordValue record : redordDesignValues) {
        RecordValue recordAux = new RecordValue();
        recordAux.setTableValue(record.getTableValue());

        recordAux.setDatasetPartitionId(dataset);
        List<FieldValue> fieldValues = fieldRepository.findByRecord(record);
        List<FieldValue> fieldValuesOnlyValues = new ArrayList<>();
        for (FieldValue field : fieldValues) {
          FieldValue auxField = new FieldValue();
          auxField.setValue(field.getValue());
          auxField.setIdFieldSchema(field.getIdFieldSchema());
          auxField.setType(field.getType());
          auxField.setRecord(recordAux);
          fieldValuesOnlyValues.add(auxField);
        }
        recordAux.setFields(fieldValuesOnlyValues);
        redordDesignValuesList.add(recordAux);
      }
      if (!redordDesignValuesList.isEmpty()) {
        // save values
        TenantResolver.setTenantName(String.format(DATASET_ID, dataset));
        recordRepository.saveAll(redordDesignValuesList);
      }
    }
  }

}
