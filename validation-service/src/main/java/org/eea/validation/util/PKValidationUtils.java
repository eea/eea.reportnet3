package org.eea.validation.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bson.types.ObjectId;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSchemaController;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.validation.persistence.data.domain.FieldValidation;
import org.eea.validation.persistence.data.domain.FieldValue;
import org.eea.validation.persistence.data.domain.Validation;
import org.eea.validation.persistence.data.repository.FieldRepository;
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
  private static DataSetControllerZuul dataSetControllerZuul;

  /** The dataset schema controller. */
  private static DatasetSchemaController datasetSchemaController;

  /** The dataset metabase controller zuul. */
  private static DataSetMetabaseControllerZuul datasetMetabaseControllerZuul;

  /** The schemas repository. */
  private static SchemasRepository schemasRepository;

  /** The field repository. */
  private static FieldRepository fieldRepository;


  /**
   * Sets the dataset controller 1.
   *
   * @param dataSetControllerZuul the new dataset controller 1
   */
  @Autowired
  private void setDatasetController1(DataSetControllerZuul dataSetControllerZuul) {
    PKValidationUtils.dataSetControllerZuul = dataSetControllerZuul;
  }

  /**
   * Sets the dataset schema controller.
   *
   * @param datasetSchemaController the new dataset schema controller
   */
  @Autowired
  private void setDatasetSchemaController(DatasetSchemaController datasetSchemaController) {
    PKValidationUtils.datasetSchemaController = datasetSchemaController;
  }

  /**
   * Sets the data set metabase controller zuul.
   *
   * @param datasetMetabaseControllerZuul the new data set metabase controller zuul
   */
  @Autowired
  private void setDataSetMetabaseControllerZuul(
      DataSetMetabaseControllerZuul datasetMetabaseControllerZuul) {
    PKValidationUtils.datasetMetabaseControllerZuul = datasetMetabaseControllerZuul;
  }

  /**
   * Sets the schemas repository.
   *
   * @param schemasRepository the new schemas repository
   */
  @Autowired
  private void setSchemasRepository(SchemasRepository schemasRepository) {
    PKValidationUtils.schemasRepository = schemasRepository;
  }

  /**
   * Sets the dataset repository.
   *
   * @param fieldRepository the new dataset repository
   */
  @Autowired
  private void setDatasetRepository(FieldRepository fieldRepository) {
    PKValidationUtils.fieldRepository = fieldRepository;
  }

  /** The Constant FK_VALUES. */
  private static final String FK_VALUES =
      "select field_value.id, field_value.VALUE from dataset_%s.field_value field_value where field_value.id_field_schema='%s'";

  /** The Constant PK_VALUE_LIST. */
  private static final String PK_VALUE_LIST =
      "select distinct field_value.VALUE from dataset_%s.field_value field_value where field_value.id_field_schema='%s'";


  /**
   * Isfield PK.
   *
   * @param datasetId the dataset id
   * @param idFieldSchema the id field schema
   * @return the boolean
   */
  public static Boolean isfieldPK(String datasetId, String idFieldSchema) {
    // Id dataset to Validate
    long datasetIdReference = Long.parseLong(datasetId);
    // Id Dataset contains PK list
    Long datasetIdRefered =
        dataSetControllerZuul.getDatasetIdReferenced(datasetIdReference, idFieldSchema);
    // Get FK Schema
    String fkSchemaId = datasetMetabaseControllerZuul.findDatasetSchemaIdById(datasetIdReference);
    DataSetSchema datasetSchemaFK =
        schemasRepository.findByIdDataSetSchema(new ObjectId(fkSchemaId));
    // Get PK Schema
    String pkSchemaId = datasetMetabaseControllerZuul.findDatasetSchemaIdById(datasetIdRefered);
    DataSetSchema datasetSchemaPK =
        schemasRepository.findByIdDataSetSchema(new ObjectId(pkSchemaId));

    // Retrieve PK List
    List<String> pkList = mountQuery(datasetSchemaPK,
        getPKFieldFromFKField(datasetSchemaFK, idFieldSchema), datasetIdRefered);

    // Get list of Fields to validate
    List<FieldValue> fkFields = fieldRepository.findByIdFieldSchema(idFieldSchema);

    Validation pkValidation = createValidation("", fkSchemaId);

    // for (TableSchema table : schema.getTableSchemas()) {
    List<FieldValue> errorFields = new ArrayList<>();
    for (FieldValue field : fkFields) {
      if (checkPK(pkList, field)) {
        List<FieldValidation> fieldValidationList = new ArrayList<>();
        FieldValidation fieldValidation = new FieldValidation();
        fieldValidation.setValidation(pkValidation);
        fieldValidationList.add(fieldValidation);
        field.setIdFieldSchema(idFieldSchema);
        errorFields.add(field);
      }
    }

    saveFieldValidations(errorFields);

    // Force true because we only need Field Validations
    return true;
  }


  private static Validation createValidation(String idRule, String idDatasetSchema) {

    Validation validation = new Validation();
    validation.setIdRule("");
    validation.setLevelError(ErrorTypeEnum.WARNING);
    validation.setMessage("");
    validation.setTypeEntity(EntityTypeEnum.FIELD);
    validation.setValidationDate("");
    validation.setOriginName("");


    return validation;
  }


  /**
   * Creates the field validations.
   *
   * @param fieldValue the field value
   */
  private static void saveFieldValidations(List<FieldValue> fieldValue) {

  }



  /**
   * Check PK.
   *
   * @param pkValues the pk values
   * @param value the FieldValue
   * @return the boolean
   */
  private static Boolean checkPK(List<String> pkValues, FieldValue value) {

    if (pkValues.contains(value.getValue())) {
      return true;
    } else {
      return false;
    }
  }



  /**
   * Mount query.
   *
   * @param datasetSchema the dataset schema
   * @param IdFieldScehma the id field scehma
   * @param datasetId the dataset id
   * @return the list
   */
  private static List<String> mountQuery(DataSetSchema datasetSchema, String IdFieldScehma,
      Long datasetId) {


    List<String> valueList = new ArrayList<String>();

    String query = createQuery(datasetSchema, IdFieldScehma, datasetId);
    List<String> objectReurned = fieldRepository.queryExecution(query);



    // objectReurned.stream().forEach(element -> {
    // FieldValue fieldValue = new FieldValue();
    // fieldValue.setId(field);


    return valueList;

  }



  /**
   * Creates the query.
   *
   * @param datasetSchema the dataset schema
   * @param idFieldSchema the id field schema
   * @param datasetId the dataset id
   * @return the string
   */
  private static String createQuery(DataSetSchema datasetSchema, String idFieldSchema,
      Long datasetId) {


    Map<String, String> fieldData = getFieldSchemaFromSchema(datasetSchema, idFieldSchema);
    StringBuilder query = new StringBuilder();

    for (Map.Entry<String, String> entry : fieldData.entrySet()) {
      String value = String.format(PK_VALUE_LIST, datasetId, entry.getKey());
      query.append(value);
    }
    return query.toString();
  }


  /**
   * Gets the field schema PK from schema.
   *
   * @param schema the schema
   * @param idFieldSchema the id field schema
   * @return the field schema from schema
   */
  private static Map<String, String> getFieldSchemaFromSchema(DataSetSchema schema,
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


  /**
   * Gets the PK field from FK field.
   *
   * @param schema the schema
   * @param idFieldSchema the id field schema
   * @return the PK field from FK field
   */
  private static String getPKFieldFromFKField(DataSetSchema schema, String idFieldSchema) {

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
