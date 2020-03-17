package org.eea.validation.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bson.types.ObjectId;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSchemaController;
import org.eea.validation.persistence.data.domain.FieldValue;
import org.eea.validation.persistence.data.repository.DatasetRepository;
import org.eea.validation.persistence.repository.SchemasRepository;
import org.eea.validation.persistence.schemas.DataSetSchema;
import org.eea.validation.persistence.schemas.FieldSchema;
import org.eea.validation.persistence.schemas.TableSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class PKValidation.
 */
@Component
public class PKValidationUtils {

  /** The data set controller zuul. */
  @Autowired
  private static DataSetControllerZuul dataSetControllerZuul;

  @Autowired
  private static DatasetSchemaController datasetSchemaController;

  @Autowired
  private static DataSetMetabaseControllerZuul datasetMetabaseControllerZuul;

  @Autowired
  private static SchemasRepository schemasRepository;

  @Autowired
  private static DatasetRepository datasetRepository;

  private static final String FK_VALUES =
      "select field_value.id, field_value.VALUE from dataset_%s.field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='%s'";

  private static final String PK_VALUE_LIST =
      "select distinct field_value.VALUE from dataset_%s.field_value field_value where field_value.id_field_schema='%s';";

  private static final String TABLE_START = "AS ( SELECT ";

  private static final String TABLE_FINAL = "FROM dataset_%s.record_value rv )";

  private static final Integer PAGE_SIZE = 1000;

  public static Boolean isfieldPK(String datasetId, String idFieldSchema, String idFk) {

    long datasetIdReference = Long.parseLong(datasetId);

    Long datasetIdRefered =
        dataSetControllerZuul.getDatasetIdReferenced(datasetIdReference, idFieldSchema, idFk);

    String fkSchemaId = datasetMetabaseControllerZuul.findDatasetSchemaIdById(datasetIdReference);
    String pkSchemaId = datasetMetabaseControllerZuul.findDatasetSchemaIdById(datasetIdRefered);

    DataSetSchema datasetSchemaPK =
        schemasRepository.findByIdDataSetSchema(new ObjectId(pkSchemaId));

    DataSetSchema datasetSchemaFK =
        schemasRepository.findByIdDataSetSchema(new ObjectId(fkSchemaId));


    mountQuery(datasetSchemaPK, datasetSchemaFK,
        getFieldPKFromFKField(datasetSchemaFK, idFieldSchema), idFieldSchema, datasetIdRefered,
        datasetIdReference);



    return false;
  }


  private void createFieldValidations(List<FieldValue> fieldValue) {

  }



  private static void mountQuery(DataSetSchema datasetSchemaPK, DataSetSchema datasetSchemaFK,
      String pkId, String fkId, Long datasetIdRefered, Long datasetIdReference) {


    String queryPKPart = createQuery(datasetSchemaPK, pkId, datasetIdRefered, false);
    List<String> a = datasetRepository.queryExecution(queryPKPart);



    String queryFKPart = createQuery(datasetSchemaFK, fkId, datasetIdReference, true);
    List<String> b = datasetRepository.queryExecution(queryFKPart);


    System.out.println("yeah");
  }

  private static String createQuery(DataSetSchema datasetSchema, String idFieldSchema,
      Long datasetId, Boolean isFK) {

    Map<String, String> fieldData = getFieldSchemasFromSchema(datasetSchema, idFieldSchema);
    StringBuilder query = new StringBuilder();

    query.append(TABLE_START);

    for (Map.Entry<String, String> entry : fieldData.entrySet()) {
      if (isFK.equals(Boolean.TRUE)) {
        String value = String.format(FK_VALUES, datasetId, entry.getKey());
        query.append(value);
      } else {
        String value = String.format(PK_VALUE_LIST, datasetId, entry.getKey());
        query.append(value);
      }
    }
    return query.toString();
  }


  private static Map<String, String> getFieldSchemasFromSchema(DataSetSchema schema,
      String idFieldSchema) {

    TableSchema tableSchema = new TableSchema();
    Map<String, String> fieldData = new HashMap<>();
    Boolean locatedField = false;

    for (TableSchema table : schema.getTableSchemas()) {
      for (FieldSchema field : table.getRecordSchema().getFieldSchema()) {
        if (field.getIdFieldSchema().toString().equals(idFieldSchema)) {
          tableSchema = table;
          locatedField = Boolean.TRUE;
          break;
        }
      }
      if (locatedField.equals(Boolean.TRUE)) {
        break;
      }
    }

    tableSchema.getRecordSchema().getFieldSchema().stream().forEach(field -> {
      if (field.getIdFieldSchema().toString().equals(idFieldSchema)) {
        fieldData.put(field.getIdFieldSchema().toString(), field.getHeaderName());
      }
    });

    return fieldData;
  }


  private static String getFieldPKFromFKField(DataSetSchema schema, String idFieldSchema) {

    String pkField = null;
    Boolean locatedPK = false;

    for (TableSchema table : schema.getTableSchemas()) {
      for (FieldSchema field : table.getRecordSchema().getFieldSchema()) {
        if (field.getIdFieldSchema().toString().equals(idFieldSchema)) {
          pkField = field.getReferencedField().getIdPk().toString();
          locatedPK = Boolean.TRUE;
          break;
        }
      }
      if (locatedPK.equals(Boolean.TRUE)) {
        break;
      }
    }
    return pkField;
  }


}
