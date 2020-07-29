package org.eea.validation.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.transaction.Transactional;
import org.bson.types.ObjectId;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.validation.persistence.data.domain.DatasetValue;
import org.eea.validation.persistence.data.domain.FieldValidation;
import org.eea.validation.persistence.data.domain.FieldValue;
import org.eea.validation.persistence.data.domain.Validation;
import org.eea.validation.persistence.data.repository.FieldRepository;
import org.eea.validation.persistence.repository.RulesRepository;
import org.eea.validation.persistence.repository.SchemasRepository;
import org.eea.validation.persistence.schemas.DataSetSchema;
import org.eea.validation.persistence.schemas.FieldSchema;
import org.eea.validation.persistence.schemas.TableSchema;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class PKValidation.
 */
@Component
public class FKValidationUtils {

  /**
   * The data set controller zuul.
   */
  @Autowired
  private static DataSetControllerZuul dataSetControllerZuul;

  /**
   * The rules repository.
   */
  @Autowired
  private static RulesRepository rulesRepository;

  /**
   * The dataset metabase controller zuul.
   */
  @Autowired
  private static DataSetMetabaseControllerZuul datasetMetabaseControllerZuul;

  /**
   * The schemas repository.
   */
  @Autowired
  private static SchemasRepository schemasRepository;

  /**
   * The field repository.
   */
  @Autowired
  private static FieldRepository fieldRepository;

  /**
   * The Constant PK_VALUE_LIST.
   */
  private static final String PK_VALUE_LIST =
      "select distinct field_value.VALUE from dataset_%s.field_value field_value where field_value.id_field_schema='%s'";


  /**
   * Isfield PK.
   *
   * @param datasetValue the dataset value
   * @param idFieldSchema the id field schema
   * @param idRule the id rule
   * @param pkMustBeUsed the pk must be used
   * @return the boolean
   */
  public static Boolean isfieldFK(DatasetValue datasetValue, String idFieldSchema, String idRule,
      Boolean pkMustBeUsed) {
    // Id dataset to Validate
    long datasetIdReference = datasetValue.getId();

    // Get FK Schema
    String fkSchemaId = datasetMetabaseControllerZuul.findDatasetSchemaIdById(datasetIdReference);
    DataSetSchema datasetSchemaFK =
        schemasRepository.findByIdDataSetSchema(new ObjectId(fkSchemaId));
    FieldSchema idFieldSchemaPk = getPKFieldFromFKField(datasetSchemaFK, idFieldSchema);

    String idFieldSchemaPKString = "";
    if (null != idFieldSchemaPk && null != idFieldSchemaPk.getReferencedField()
        && null != idFieldSchemaPk.getReferencedField().getIdPk()) {
      idFieldSchemaPKString = idFieldSchemaPk.getReferencedField().getIdPk().toString();
    }


    FieldSchema fkFieldSchema = getPKFieldSchemaFromSchema(datasetSchemaFK, idFieldSchema);

    // Id Dataset contains PK list
    Long datasetIdRefered =
        dataSetControllerZuul.getReferencedDatasetId(datasetIdReference, idFieldSchemaPKString);

    // Get PK Schema
    String pkSchemaId = datasetMetabaseControllerZuul.findDatasetSchemaIdById(datasetIdRefered);
    DataSetSchema datasetSchemaPK =
        schemasRepository.findByIdDataSetSchema(new ObjectId(pkSchemaId));

    // get Orig name
    TableSchema origname = getTableSchemaFromIdFieldSchema(datasetSchemaFK, idFieldSchema);

    // Retrieve PK List
    List<String> pkList = mountQuery(datasetSchemaPK, idFieldSchemaPKString, datasetIdRefered);

    // Get list of Fields to validate
    List<FieldValue> fkFields = fieldRepository.findByIdFieldSchema(idFieldSchema);

    // GetValidationData
    Validation pkValidation = createValidation(idRule, fkSchemaId, origname);
    List<FieldValue> errorFields = new ArrayList<>();

    if (!pkMustBeUsed) {

      createFieldValueValidation(fkFieldSchema, pkList, fkFields, pkValidation, errorFields);

      saveFieldValidations(errorFields);

      // Force true because we only need Field Validations
      return true;

    } else {
      if (null != fkFieldSchema && null != fkFieldSchema.getPkMustBeUsed()
          && fkFieldSchema.getPkMustBeUsed()) {

        return setValuesToValidate(fkFieldSchema, pkList, fkFields);
      }

    }
    return true;
  }


  private static Boolean setValuesToValidate(FieldSchema fkFieldSchema, List<String> pkList,
      List<FieldValue> fkFields) {
    // Values must be
    Set<String> pkSet = new HashSet<>();
    pkSet.addAll(pkList);
    if (Boolean.TRUE.equals(fkFieldSchema.getPkHasMultipleValues())) {
      // we look one by one to know if all values are avaliable
      checkAllValuesMulti(pkSet, fkFields);
    } else {
      // Values must check
      fkFields.stream().forEach(field -> pkSet.remove(field.getValue()));
    }
    return pkSet.isEmpty();
  }


  /**
   * Creates the field value validation.
   *
   * @param fkFieldSchema the fk field schema
   * @param pkList the pk list
   * @param fkFields the fk fields
   * @param pkValidation the pk validation
   * @param errorFields the error fields
   */
  private static void createFieldValueValidation(FieldSchema fkFieldSchema, List<String> pkList,
      List<FieldValue> fkFields, Validation pkValidation, List<FieldValue> errorFields) {
    for (FieldValue field : fkFields) {
      if (Boolean.FALSE.equals(checkPK(pkList, field,
          null != fkFieldSchema ? fkFieldSchema.getPkHasMultipleValues() : Boolean.FALSE))) {
        List<FieldValidation> fieldValidationList =
            field.getFieldValidations() != null ? field.getFieldValidations() : new ArrayList<>();
        FieldValidation fieldValidation = new FieldValidation();
        fieldValidation.setValidation(pkValidation);
        FieldValue fieldValue = new FieldValue();
        fieldValue.setId(field.getId());
        fieldValidation.setFieldValue(fieldValue);
        fieldValidationList.add(fieldValidation);
        field.setFieldValidations(fieldValidationList);
        errorFields.add(field);
      }
    }
  }


  /**
   * Check all values multi.
   *
   * @param pkSet the pk set
   * @param fkFields the fk fields
   */
  private static void checkAllValuesMulti(Set<String> pkSet, List<FieldValue> fkFields) {

    for (FieldValue fieldValue : fkFields) {
      final List<String> arrayValue = Arrays.asList(fieldValue.getValue().split(","));

      for (String valueArray : arrayValue) {
        pkSet.remove(valueArray.trim());
      }
    }
  }



  /**
   * Creates the validation.
   *
   * @param idRule the id rule
   * @param idDatasetSchema the id dataset schema
   * @param origname the origname
   * @return the validation
   */
  private static Validation createValidation(String idRule, String idDatasetSchema,
      TableSchema origname) {

    Rule rule = rulesRepository.findRule(new ObjectId(idDatasetSchema), new ObjectId(idRule));

    Validation validation = new Validation();
    if (rule != null) {
      validation.setIdRule(rule.getRuleId().toString());

      switch (rule.getThenCondition().get(1)) {
        case "WARNING":
          validation.setLevelError(ErrorTypeEnum.WARNING);
          break;
        case "ERROR":
          validation.setLevelError(ErrorTypeEnum.ERROR);
          break;
        case "INFO":
          validation.setLevelError(ErrorTypeEnum.INFO);
          break;
        case "BLOCKER":
          validation.setLevelError(ErrorTypeEnum.BLOCKER);
          break;
        default:
          validation.setLevelError(ErrorTypeEnum.BLOCKER);
          break;
      }

      validation.setMessage(rule.getThenCondition().get(0));
      validation.setTypeEntity(EntityTypeEnum.FIELD);
      validation.setValidationDate(new Date().toString());
      validation.setOriginName(origname.getNameTableSchema());
    }
    return validation;
  }


  /**
   * Creates the field validations.
   *
   * @param fieldValues the field values
   */
  @Transactional
  private static void saveFieldValidations(List<FieldValue> fieldValues) {
    fieldRepository.saveAll(fieldValues);
  }


  /**
   * Check PK.
   *
   * @param pkValues the pk values
   * @param value the FieldValue
   * @param pkHasMultipleValues the pk has multiple values
   * @return the boolean
   */
  private static Boolean checkPK(List<String> pkValues, FieldValue value,
      Boolean pkHasMultipleValues) {
    Boolean returnChecked = Boolean.TRUE;
    if (Boolean.TRUE.equals(pkHasMultipleValues)) {
      final List<String> arrayValue = Arrays.asList(value.getValue().split(","));

      for (String valueArray : arrayValue) {
        if (!pkValues.contains(valueArray.trim())) {
          returnChecked = Boolean.FALSE;
          break;
        }
      }
      return returnChecked;
    } else {
      returnChecked = pkValues.contains(value.getValue());
      return returnChecked;

    }
  }


  /**
   * Mount query.
   *
   * @param datasetSchema the dataset schema
   * @param idFieldSchema the id field schema
   * @param datasetId the dataset id
   * @return the list
   */
  private static List<String> mountQuery(DataSetSchema datasetSchema, String idFieldSchema,
      Long datasetId) {

    List<String> valueList = new ArrayList<>();

    String query = createQuery(datasetSchema, idFieldSchema, datasetId);
    List<String> objectReurned = fieldRepository.queryExecution(query);
    for (int i = 0; i < objectReurned.size(); i++) {
      valueList.add(objectReurned.get(i));
    }

    return valueList;

  }


  /**
   * Creates the query.
   *
   * @param datasetSchema the dataset schema
   * @param idFieldSchema the id field schema
   * @param datasetId the dataset id
   *
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
   *
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
   * Gets the table schema from id field schema.
   *
   * @param schema the schema
   * @param idFieldSchema the id field schema
   *
   * @return the table schema from id field schema
   */
  private static TableSchema getTableSchemaFromIdFieldSchema(DataSetSchema schema,
      String idFieldSchema) {

    TableSchema tableSchema = new TableSchema();
    Boolean locatedTable = false;

    for (TableSchema table : schema.getTableSchemas()) {
      for (FieldSchema field : table.getRecordSchema().getFieldSchema()) {
        if (field.getIdFieldSchema().toString().equals(idFieldSchema)) {
          tableSchema = table;
          locatedTable = Boolean.TRUE;
          break;
        }
      }
      if (locatedTable.equals(Boolean.TRUE)) {
        break;
      }
    }
    return tableSchema;
  }

  /**
   * Gets the PK field from FK field.
   *
   * @param schema the schema
   * @param idFieldSchema the id field schema
   *
   * @return the PK field from FK field
   */
  private static FieldSchema getPKFieldFromFKField(DataSetSchema schema, String idFieldSchema) {

    FieldSchema pkField = null;
    Boolean locatedPK = false;

    for (TableSchema table : schema.getTableSchemas()) {
      for (FieldSchema field : table.getRecordSchema().getFieldSchema()) {
        if (field.getIdFieldSchema().toString().equals(idFieldSchema)) {
          pkField = field;
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


  /**
   * Gets the PK field schema from schema.
   *
   * @param schema the schema
   * @param idFieldSchema the id field schema
   * @return the PK field schema from schema
   */
  private static FieldSchema getPKFieldSchemaFromSchema(DataSetSchema schema,
      String idFieldSchema) {

    FieldSchema field = null;
    Boolean locatedPK = false;

    for (TableSchema table : schema.getTableSchemas()) {
      for (FieldSchema fieldAux : table.getRecordSchema().getFieldSchema()) {
        if (fieldAux.getIdFieldSchema().toString().equals(idFieldSchema)) {
          field = fieldAux;
          locatedPK = Boolean.TRUE;
          break;
        }
      }
      if (locatedPK.equals(Boolean.TRUE)) {
        break;
      }
    }
    return field;
  }


}
