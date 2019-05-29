package org.eea.dataset.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.eea.dataset.mapper.DataSchemaMapper;
import org.eea.dataset.persistence.metabase.domain.TableCollection;
import org.eea.dataset.persistence.metabase.domain.TableHeadersCollection;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseTableRepository;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.dataset.persistence.schemas.domain.RecordSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.google.common.collect.Lists;

/**
 * The type Dataschema service.
 */
@Service("datachemaService")
public class DataschemaServiceImpl implements DatasetSchemaService {

  /** The schemas repository. */
  @Autowired
  private SchemasRepository schemasRepository;

  /** The data set metabase table collection. */
  @Autowired
  private DataSetMetabaseTableRepository dataSetMetabaseTableCollection;

  /** The dataschema mapper. */
  @Autowired
  private DataSchemaMapper dataSchemaMapper;


  /**
   * Creates the data schema.
   *
   * @param datasetId the dataset id
   */
  @Override
  public void createDataSchema(Long datasetId) {

    DataSetSchema dataSetSchema = new DataSetSchema();
    Iterable<TableCollection> tables = dataSetMetabaseTableCollection.findAllByDataSetId(datasetId);
    ArrayList<TableCollection> values = Lists.newArrayList(tables);

    List<TableSchema> tableSchemas = new ArrayList<>();

    dataSetSchema.setNameDataSetSchema("dataSet_" + datasetId);
    dataSetSchema.setIdDataFlow(1L);

    for (int i = 1; i <= values.size(); i++) {
      TableCollection table = values.get(i - 1);
      TableSchema tableSchema = new TableSchema();
      tableSchema.setIdTableSchema(new ObjectId());

      tableSchema.setNameTableSchema(table.getTableName());

      RecordSchema recordSchema = new RecordSchema();
      recordSchema.setIdRecordSchema(new ObjectId());
      recordSchema.setIdTableSchema(tableSchema.getIdTableSchema());

      List<FieldSchema> fieldSchemas = new ArrayList<>();

      int headersSize = table.getTableHeadersCollections().size();
      for (int j = 1; j <= headersSize; j++) {
        TableHeadersCollection header = table.getTableHeadersCollections().get(j - 1);
        FieldSchema fieldSchema = new FieldSchema();
        fieldSchema.setIdFieldSchema(new ObjectId());
        fieldSchema.setIdRecord(recordSchema.getIdRecordSchema());
        fieldSchema.setHeaderName(header.getHeaderName());
        fieldSchema.setType(header.getHeaderType());
        fieldSchemas.add(fieldSchema);
      }
      recordSchema.setFieldSchema(fieldSchemas);
      tableSchema.setRecordSchema(recordSchema);
      tableSchemas.add(tableSchema);
    }
    dataSetSchema.setTableSchemas(tableSchemas);
    schemasRepository.save(dataSetSchema);

  }


  /**
   * Find the dataschema per id
   * 
   * @param dataschemaId the idDataschema
   */
  @Override
  public DataSetSchemaVO getDataSchemaById(String dataschemaId) {

    Optional<DataSetSchema> dataschema = schemasRepository.findById(new ObjectId(dataschemaId));

    DataSetSchemaVO dataSchemaVO = new DataSetSchemaVO();
    if (dataschema.isPresent()) {
      DataSetSchema datasetSchema = dataschema.get();

      dataSchemaVO = dataSchemaMapper.entityToClass(datasetSchema);
    }

    return dataSchemaVO;

  }

  /**
   * Find the dataschema per idDataFlow
   * 
   * @param idFlow the idDataFlow to look for
   */
  @Override
  public DataSetSchemaVO getDataSchemaByIdFlow(Long idFlow) {

    DataSetSchema dataschema = schemasRepository.findSchemaByIdFlow(idFlow);

    return dataSchemaMapper.entityToClass(dataschema);

  }


}
