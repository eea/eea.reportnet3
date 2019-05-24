package org.eea.dataset.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.eea.dataset.mapper.DataSchemaMapper;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.dataset.persistence.schemas.domain.RecordSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.interfaces.vo.dataset.enums.TypeData;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("datachemaService")
public class DataschemaServiceImpl implements DatasetSchemaService {

  /** The schemas repository. */
  @Autowired
  private SchemasRepository schemasRepository;

  /** The dataschema mapper. */
  @Autowired
  private DataSchemaMapper dataSchemaMapper;


  /**
   * Creates the data schema.
   *
   * @param datasetName the dataset name
   */
  @Override
  public void createDataSchema(String datasetName) {

    // This is a dummy to create a dataschema
    TypeData headerType = TypeData.BOOLEAN;

    DataSetSchema dataSetSchema = new DataSetSchema();

    dataSetSchema.setNameDataSetSchema("dataSet_1");
    dataSetSchema.setIdDataFlow(1L);


    long numeroRegistros = schemasRepository.count();
    dataSetSchema.setIdDataSetSchema(new ObjectId());
    List<TableSchema> tableSchemas = new ArrayList<>();
    Long dssID = 0L;
    Long fsID = 0L;

    for (int dss = 1; dss <= 3; dss++) {
      TableSchema tableSchema = new TableSchema();
      tableSchema.setIdTableSchema(new ObjectId());
      tableSchema.setNameTableSchema("tabla" + dss);
      RecordSchema recordSchema = new RecordSchema();
      recordSchema.setIdRecordSchema(new ObjectId());
      recordSchema.setIdTableSchema(tableSchema.getIdTableSchema());
      List<FieldSchema> fieldSchemas = new ArrayList<>();

      for (int fs = 1; fs <= 20; fs++) {
        FieldSchema fieldSchema = new FieldSchema();
        fieldSchema = new FieldSchema();
        fieldSchema.setIdFieldSchema(new ObjectId());
        fieldSchema.setIdRecord(recordSchema.getIdRecordSchema());
        if (dss / 2 == 1) {
          int dato = fs + 10;
          fieldSchema.setHeaderName("campo_" + dato);
          fieldSchema.setType(TypeData.FLOAT);
        } else {
          fieldSchema.setHeaderName("campo_" + fs);
          fieldSchema.setType(headerType);
        }

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
   * Find the dataschema per id.
   *
   * @param dataschemaId the idDataschema
   * @return the data schema by id
   */
  @Override
  public DataSetSchemaVO getDataSchemaById(String dataschemaId) {

    // The search using the direct method of MongoDB returns an Optional object
    Optional<DataSetSchema> dataschema = schemasRepository.findById(new ObjectId(dataschemaId));

    DataSetSchemaVO dataSchemaVO = new DataSetSchemaVO();

    // isPresent to check that certainly the search has returned result
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

    DataSetSchemaVO dataSchemaVo = new DataSetSchemaVO();
    DataSetSchema dataSchema = schemasRepository.findSchemaByIdFlow(idFlow);
    if (dataSchema != null) {
      // mapeo de entidad a VO
      dataSchemaVo = dataSchemaMapper.entityToClass(dataSchema);
    }
    return dataSchemaVo;

  }


}
