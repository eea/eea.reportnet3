package org.eea.validation.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
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
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Component;

/**
 * The Class FKValidationUtils.
 */
@Component
public class FKValidationUtils {

  /** The data set controller zuul. */
  private static DataSetControllerZuul dataSetControllerZuul;

  /** The rules repository. */
  private static RulesRepository rulesRepository;

  /** The dataset metabase controller zuul. */
  private static DataSetMetabaseControllerZuul datasetMetabaseControllerZuul;

  /** The schemas repository. */
  private static SchemasRepository schemasRepository;

  /** The field repository. */
  private static FieldRepository fieldRepository;

  /*
   * we need to put synchronized void because drools need a static method to call in a java file, so
   * we should create a static and put this synchronized to put @autowired to convert the Object in
   * a bean
   */
  /**
   * Sets the dataset controller.
   *
   * @param dataSetControllerZuul the new dataset controller
   */
  @Autowired
  synchronized void setDatasetController(DataSetControllerZuul dataSetControllerZuul) {
    FKValidationUtils.dataSetControllerZuul = dataSetControllerZuul;
  }

  /**
   * Sets the rules repository.
   *
   * @param rulesRepository the new rules repository
   */
  @Autowired
  synchronized void setRulesRepository(RulesRepository rulesRepository) {
    FKValidationUtils.rulesRepository = rulesRepository;
  }

  /**
   * Sets the data set metabase controller zuul.
   *
   * @param datasetMetabaseControllerZuul the new data set metabase controller zuul
   */
  @Autowired
  synchronized void setDataSetMetabaseControllerZuul(
      DataSetMetabaseControllerZuul datasetMetabaseControllerZuul) {
    FKValidationUtils.datasetMetabaseControllerZuul = datasetMetabaseControllerZuul;
  }

  /**
   * Sets the schemas repository.
   *
   * @param schemasRepository the new schemas repository
   */
  @Autowired
  synchronized void setSchemasRepository(SchemasRepository schemasRepository) {
    FKValidationUtils.schemasRepository = schemasRepository;
  }

  /**
   * Sets the field repository.
   *
   * @param fieldRepository the new field repository
   */
  @Autowired
  synchronized void setFieldRepository(FieldRepository fieldRepository) {
    FKValidationUtils.fieldRepository = fieldRepository;
  }

  /** The Constant PK_VALUE_LIST: {@value}. */
  private static final String PK_VALUE_LIST =
      "select distinct field_value.VALUE from dataset_%s.field_value field_value where field_value.id_field_schema='%s'";

  /** The Constant COMPOSE_PK_LIST: {@value}. */
  private static final String COMPOSE_PK_LIST = "select fk_id from " + " (select "
      + " (SELECT fv.id FROM dataset_%s.field_value fv  WHERE fv.id_record = rv.id AND fv.id_field_schema = '%s') as fk_id,  "
      + " (SELECT fv.value  FROM dataset_%s.field_value fv  WHERE fv.id_record = rv.id AND fv.id_field_schema = '%s') fk,  "
      + " (SELECT fv.value FROM dataset_%s.field_value fv  WHERE fv.id_record = rv.id AND fv.id_field_schema = '%s') optionalfk  "
      + " FROM dataset_%s.record_value rv) as table1  " + " left join " + " (SELECT distinct "
      + " (SELECT fv.value  FROM dataset_%s.field_value fv  WHERE fv.id_record = rv.id AND fv.id_field_schema = '%s') pk,  "
      + " (SELECT fv.value FROM dataset_%s.field_value fv  WHERE fv.id_record = rv.id AND fv.id_field_schema = '%s') optionalpk  "
      + " FROM dataset_%s.record_value rv) as table2 "
      + " on table1.fk = table2.pk and table1.optionalfk = table2.optionalpk "
      + " where pk is null";

  /** The Constant COMPOSE_PK_MUST_BE_USED_LIST: {@value}. */
  private static final String COMPOSE_PK_MUST_BE_USED_LIST = "select distinct pk from "
      + " (select "
      + " (SELECT fv.value  FROM dataset_%s.field_value fv  WHERE fv.id_record = rv.id AND fv.id_field_schema = '%s') fk,  "
      + " (SELECT fv.value FROM dataset_%s.field_value fv  WHERE fv.id_record = rv.id AND fv.id_field_schema = '%s') optionalfk  "
      + " FROM dataset_%s.record_value rv) as table1  " + " right join  " + " (SELECT distinct "
      + " (SELECT fv.value  FROM dataset_%s.field_value fv  WHERE fv.id_record = rv.id AND fv.id_field_schema = '%s') pk,  "
      + " (SELECT fv.value FROM dataset_%s.field_value fv  WHERE fv.id_record = rv.id AND fv.id_field_schema = '%s') optionalpk  "
      + " FROM dataset_%s.record_value rv) as table2 "
      + " on table1.fk = table2.pk and table1.optionalfk = table2.optionalpk "
      + " where fk is null";


  /** The Constant PK_QUERY_VALUES: {@value}. */
  private static final String PK_QUERY_VALUES =
      " select OptionalPKValue ,cast (array_agg(tableaux.PKValueAux) as TEXT) as PKValue from( SELECT distinct "
          + " (SELECT fv.value  FROM dataset_%s.field_value fv  WHERE fv.id_record = rv.id AND fv.id_field_schema = '%s') as PKValueAux, "
          + " (SELECT fv.value FROM dataset_%s.field_value fv  WHERE fv.id_record = rv.id AND fv.id_field_schema = '%s') as OptionalPKValue "
          + " FROM dataset_%s.record_value rv ) as tableaux group by (OptionalPKValue)";


  /** The Constant FK_QUERY_VALUES: {@value}. */
  private static final String FK_QUERY_VALUES = "select "
      + " (SELECT fv.id FROM dataset_%s.field_value fv  WHERE fv.id_record = rv.id AND fv.id_field_schema = '%s') as fk_id, "
      + " (SELECT fv.value  FROM dataset_%s.field_value fv  WHERE fv.id_record = rv.id AND fv.id_field_schema = '%s') fk, "
      + " (SELECT fv.value FROM dataset_%s.field_value fv  WHERE fv.id_record = rv.id AND fv.id_field_schema = '%s') optionalfk "
      + " FROM dataset_%s.record_value rv ";


  /** The Constant FK_COUNT_VALUES: {@value}. */
  private static final String FK_COUNT_VALUES =
      "select count(fv.id) from dataset_%s.field_value fv where ID_FIELD_SCHEMA = '%s' ";


  /** The Constant PK_MUST_BE_USED: {@value}. */
  private static final String PK_MUST_BE_USED =
      "with fktable as (select * from dataset_%s.field_value fv where ID_FIELD_SCHEMA = '%s'),\r\n"
          + " pktable as (select distinct field_value.VALUE pk_value from dataset_%s.field_value field_value where field_value.id_field_schema = '%s'),\r\n"
          + " fktable_aux as(select distinct unnest(string_to_array(fktable.value ,'; ')) fkas from fktable)\r\n"
          + " select count(pk_value) from fktable_aux\r\n" + " right join pktable\r\n"
          + " on pk_value = fktable_aux.fkas where fktable_aux.fkas is null ";

  /** The Constant FK_SINGLE_WRONG: {@value}. */
  private static final String FK_SINGLE_WRONG =
      "with fktable as (select * from dataset_%s.field_value fv where ID_FIELD_SCHEMA = '%s'),\r\n"
          + " pktable as (select string_agg(pk_value, '; ') pk_value  from (select distinct field_value.VALUE pk_value\r\n"
          + " from dataset_%s.field_value field_value\r\n"
          + " where field_value.id_field_schema = '%s') table_aux),\r\n"
          + " fkcrosspk as (select *, (pkas @> fkas) is_contained  from (\r\n"
          + " select fktable.id,string_to_array(case when fktable.value like %s then fktable.value  when fktable.value like %s then REPLACE(fktable.value, ';', '; ' ) else fktable.value end ,'; ') as fkas,\r\n"
          + " (select string_to_array(pk_value,'; ') from pktable ) as pkas\r\n"
          + " from fktable) table_aux2 limit %s offset %s )\r\n"
          + " select fktable.* from fktable inner join fkcrosspk on fkcrosspk.id = fktable.id where is_contained = false\r\n"
          + " limit %s offset %s ";

  /** The Constant FK_SINGLE_WRONG_IGNORE_CASE_LINK: {@value}. */
  private static final String FK_SINGLE_WRONG_IGNORE_CASE_LINK =
      "with fktable as (select * from dataset_%s.field_value fv where ID_FIELD_SCHEMA = '%s'),\r\n"
          + " pktable as (select string_agg(pk_value, '; ') pk_value  from (select distinct LOWER(field_value.VALUE) pk_value\r\n"
          + " from dataset_%s.field_value field_value\r\n"
          + " where field_value.id_field_schema = '%s') table_aux),\r\n"
          + " fkcrosspk as (select *, (pkas @> fkas) is_contained  from (\r\n"
          + " select fktable.id,string_to_array(case when fktable.value like %s then fktable.value  when fktable.value like %s then REPLACE(fktable.value, ';', '; ' ) else LOWER(fktable.value) end ,'; ') as fkas,\r\n"
          + " (select string_to_array(pk_value,'; ') from pktable ) as pkas\r\n"
          + " from fktable) table_aux2 limit %s offset %s )\r\n"
          + " select fktable.* from fktable inner join fkcrosspk on fkcrosspk.id = fktable.id where is_contained = false\r\n"
          + " limit %s offset %s ";

  /**
   * Isfield FK.
   *
   * @param datasetValue the dataset value
   * @param idFieldSchema the id field schema
   * @param idRule the id rule
   * @param pkMustBeUsed the pk must be used
   * @return the boolean
   */
  public static Boolean isfieldFK(DatasetValue datasetValue, String idFieldSchema, String idRule,
      boolean pkMustBeUsed) {
    boolean result = true;
    // Id dataset to Validate
    long datasetIdReference = datasetValue.getId();

    // Get FK Schema
    String fkSchemaId = datasetMetabaseControllerZuul.findDatasetSchemaIdById(datasetIdReference);
    DataSetSchema datasetSchemaFK =
        schemasRepository.findByIdDataSetSchema(new ObjectId(fkSchemaId));
    String idFieldSchemaPKString = getIdFieldSchemaPK(idFieldSchema, datasetSchemaFK);
    FieldSchema fkFieldSchema = getPKFieldFromFKField(datasetSchemaFK, idFieldSchema);

    // Id Dataset contains PK list
    Long datasetIdRefered =
        dataSetControllerZuul.getReferencedDatasetId(datasetIdReference, idFieldSchemaPKString);

    // get Orig name
    TableSchema tableName = getTableSchemaFromIdFieldSchema(datasetSchemaFK, idFieldSchema);

    // GetValidationData
    Validation pkValidation = createValidation(idRule, fkSchemaId, tableName, fkFieldSchema);

    // Optionals FK fields
    if (null != fkFieldSchema && null != fkFieldSchema.getReferencedField()
        && null != fkFieldSchema.getReferencedField().getLinkedConditionalFieldId()
        && null != fkFieldSchema.getReferencedField().getMasterConditionalFieldId()) {
      result = calculateFKCompose(pkMustBeUsed, datasetIdReference, idFieldSchemaPKString,
          fkFieldSchema, datasetIdRefered, pkValidation);
    } else {
      result = calculateFKsimple(idFieldSchema, pkMustBeUsed, datasetIdReference,
          idFieldSchemaPKString, fkFieldSchema, datasetIdRefered, pkValidation);
    }
    return result;
  }

  /**
   * Calculate FK compose.
   *
   * @param pkMustBeUsed the pk must be used
   * @param datasetIdReference the dataset id reference
   * @param idFieldSchemaPKString the id field schema PK string
   * @param fkFieldSchema the fk field schema
   * @param datasetIdRefered the dataset id refered
   * @param pkValidation the pk validation
   * @return true, if successful
   */
  private static boolean calculateFKCompose(boolean pkMustBeUsed, long datasetIdReference,
      String idFieldSchemaPKString, FieldSchema fkFieldSchema, Long datasetIdRefered,
      Validation pkValidation) {
    boolean result;
    String fkConditionalLinkedFieldSchemaId =
        fkFieldSchema.getReferencedField().getLinkedConditionalFieldId().toString();
    String fkConditionalMasterFieldSchemaId =
        fkFieldSchema.getReferencedField().getMasterConditionalFieldId().toString();

    String datasetIdFK = Long.toString(datasetIdReference);
    String fkFieldSchemaId = fkFieldSchema.getIdFieldSchema().toString();
    String datasetIdPK = Long.toString(datasetIdRefered);

    String query = String.format(COMPOSE_PK_LIST, datasetIdFK, fkFieldSchemaId, datasetIdFK,
        fkFieldSchemaId, datasetIdFK, fkConditionalMasterFieldSchemaId, datasetIdFK, datasetIdPK,
        idFieldSchemaPKString, datasetIdPK, fkConditionalLinkedFieldSchemaId, datasetIdPK);


    List<FieldValue> errorFields = new ArrayList<>();
    if (!pkMustBeUsed && Boolean.FALSE.equals(fkFieldSchema.getPkHasMultipleValues())) {
      List<String> ifFKs = createAndExecuteQuery(query);
      while (!ifFKs.isEmpty()) {
        int pkCount = ifFKs.size();
        int batchSize = pkCount < 5000 ? pkCount : 5000;
        List<String> fkAuxLsit = ifFKs.subList(0, batchSize);
        createFieldValueValidationQuery(fieldRepository.findByIds(fkAuxLsit), pkValidation,
            errorFields);
        saveFieldValidations(errorFields);
        errorFields.clear();
        ifFKs = ifFKs.subList(batchSize, pkCount);
      }
      // Force true because we only need Field Validations
      result = true;
    } else {
      result = setValuesToValidateQuery(fkFieldSchema, datasetIdFK,
          fkConditionalMasterFieldSchemaId, datasetIdPK, idFieldSchemaPKString,
          fkConditionalLinkedFieldSchemaId, pkValidation, pkMustBeUsed);
    }
    return result;
  }

  /**
   * Calculate Fk simple.
   *
   * @param idFieldSchema the id field schema
   * @param pkMustBeUsed the pk must be used
   * @param datasetIdReference the dataset id reference
   * @param idFieldSchemaPKString the id field schema PK string
   * @param fkFieldSchema the fk field schema
   * @param datasetIdRefered the dataset id refered
   * @param pkValidation the pk validation
   * @return the boolean
   */
  private static Boolean calculateFKsimple(String idFieldSchema, boolean pkMustBeUsed,
      long datasetIdReference, String idFieldSchemaPKString, FieldSchema fkFieldSchema,
      Long datasetIdRefered, Validation pkValidation) {
    boolean result = true;
    if (!pkMustBeUsed) {
      // Counts fks
      List<FieldValue> errorFields = new ArrayList<>();
      Integer totalRecords = getSinglesFKs(Long.valueOf(datasetIdReference), idFieldSchema);
      int batchSize = 50000;
      int pkBatchSize = batchSize / 2;
      for (int fkindex = 0; fkindex < totalRecords; fkindex += batchSize) {
        for (int pkindex = 0; pkindex < totalRecords; pkindex += pkBatchSize) {
          List<FieldValue> fkFields;
          if (fkFieldSchema.getIgnoreCaseInLinks() != null && fkFieldSchema.getIgnoreCaseInLinks()) {
            fkFields = fieldRepository.queryPKNativeFieldValue(
                String.format(FK_SINGLE_WRONG_IGNORE_CASE_LINK, datasetIdReference, idFieldSchema, datasetIdRefered,
                    idFieldSchemaPKString, "'%; %'", "'%;%'", pkBatchSize, pkindex, batchSize,
                    fkindex));
          } else {
            fkFields = fieldRepository.queryPKNativeFieldValue(
                String.format(FK_SINGLE_WRONG, datasetIdReference, idFieldSchema, datasetIdRefered,
                    idFieldSchemaPKString, "'%; %'", "'%;%'", pkBatchSize, pkindex, batchSize,
                    fkindex));
          }
          if (null != fkFields && !fkFields.isEmpty()) {
            createFieldValueValidationV2(fkFields, pkValidation, errorFields);
            saveFieldValidations(errorFields);
            fkFields.clear();
            errorFields.clear();
          }
        }
      }
      // Force true because we only need Field Validations
      result = true;
    } else {
      // only count < 0 pk are not used
      if (null != fkFieldSchema && null != fkFieldSchema.getPkMustBeUsed()) {
        result = getSinglesPKsMustBeUsed(datasetIdReference, idFieldSchema, datasetIdRefered,
            idFieldSchemaPKString);
      }
    }
    return result;
  }

  /**
   * Creates the field value validation V 2.
   *
   * @param fkFields the fk fields
   * @param pkValidation the pk validation
   * @param errorFields the error fields
   */
  private static void createFieldValueValidationV2(List<FieldValue> fkFields,
      Validation pkValidation, List<FieldValue> errorFields) {
    for (FieldValue field : fkFields) {
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

  /**
   * Distinct by key.
   *
   * @param <T> the generic type
   * @param keyExtractor the key extractor
   * @return the predicate
   */
  public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
    Set<Object> seen = ConcurrentHashMap.newKeySet();
    return t -> seen.add(keyExtractor.apply(t));
  }



  /**
   * Gets the singles F ks.
   *
   * @param datasetIdFK the dataset id FK
   * @param fkFieldSchema the fk field schema
   * @return the singles F ks
   */
  private static Integer getSinglesFKs(Long datasetIdFK, String fkFieldSchema) {
    String queryPks = String.format(FK_COUNT_VALUES, datasetIdFK, fkFieldSchema);
    return fieldRepository.getCount(queryPks).intValue();
  }


  /**
   * Gets the singles P ks must be used.
   *
   * @param datasetIdFK the dataset id FK
   * @param fkFieldSchema the fk field schema
   * @param datasetIdRefered the dataset id refered
   * @param idFieldSchemaPKString the id field schema PK string
   * @return the singles P ks must be used
   */
  private static Boolean getSinglesPKsMustBeUsed(Long datasetIdFK, String fkFieldSchema,
      Long datasetIdRefered, String idFieldSchemaPKString) {
    String query = String.format(PK_MUST_BE_USED, datasetIdFK, fkFieldSchema, datasetIdRefered,
        idFieldSchemaPKString);
    return (fieldRepository.getCount(query).intValue() > 0) ? false : true;
  }

  /**
   * Gets the id field schema PK.
   *
   * @param idFieldSchema the id field schema
   * @param datasetSchemaFK the dataset schema FK
   * @return the id field schema PK
   */
  private static String getIdFieldSchemaPK(String idFieldSchema, DataSetSchema datasetSchemaFK) {
    FieldSchema idFieldSchemaPk = getPKFieldFromFKField(datasetSchemaFK, idFieldSchema);

    String idFieldSchemaPKString = "";
    if (null != idFieldSchemaPk && null != idFieldSchemaPk.getReferencedField()
        && null != idFieldSchemaPk.getReferencedField().getIdPk()) {
      idFieldSchemaPKString = idFieldSchemaPk.getReferencedField().getIdPk().toString();
    }
    return idFieldSchemaPKString;
  }

  /**
   * Sets the values to validate query.
   *
   * @param fkFieldSchema the fk field schema
   * @param datasetIdFK the dataset id FK
   * @param fkConditionalMasterFieldSchemaId the fk conditional master field schema id
   * @param datasetIdPK the dataset id PK
   * @param idFieldSchemaPKString the id field schema PK string
   * @param fkConditionalLinkedFieldSchemaId the fk conditional linked field schema id
   * @param pkValidation the pk validation
   * @param pkMustBeUsed the pk must be used
   * @return the boolean
   */
  private static Boolean setValuesToValidateQuery(FieldSchema fkFieldSchema, String datasetIdFK,
      String fkConditionalMasterFieldSchemaId, String datasetIdPK, String idFieldSchemaPKString,
      String fkConditionalLinkedFieldSchemaId, Validation pkValidation, Boolean pkMustBeUsed) {
    boolean error = true;
    List<FieldValue> errorFields = new ArrayList<>();
    if (Boolean.TRUE.equals(fkFieldSchema.getPkHasMultipleValues())) {
      String queryPks = String.format(PK_QUERY_VALUES, datasetIdPK, idFieldSchemaPKString,
          datasetIdPK, fkConditionalLinkedFieldSchemaId, datasetIdPK);

      List<Object[]> pkList = fieldRepository.queryPKExecution(queryPks);
      Map<String, String> pkMap = new HashMap<>();
      Map<String, String> pkMapAux = new HashMap<>();
      if (null != pkList) {
        for (int i = 0; i < pkList.size(); i++) {
          if (null != pkList.get(i) && null != pkList.get(i)[0] && null != pkList.get(i)[1]) {
            pkMap.put(pkList.get(i)[0].toString(),
                pkList.get(i)[1].toString().replace("{", "").replace("}", ""));
            pkMapAux.put(pkList.get(i)[0].toString(),
                pkList.get(i)[1].toString().replace("{", "").replace("}", ""));
          }
        }
      }
      Set<String> ifFKs =
          findFKs(fkFieldSchema, datasetIdFK, fkConditionalMasterFieldSchemaId, pkMap, pkMapAux);
      if (!ifFKs.isEmpty()) {
        List<FieldValue> fieldsToValidate = fieldRepository.findByIds(new ArrayList<>(ifFKs));
        FieldValue auxField = new FieldValue();
        auxField.setValue("");
        fieldsToValidate.add(auxField);
        createFieldValueValidationQuery(fieldsToValidate, pkValidation, errorFields);
        if (pkMustBeUsed.equals(Boolean.FALSE)) {
          saveFieldValidations(errorFields);
        }
      }
      if (Boolean.TRUE.equals(pkMustBeUsed)) {
        error = !pkMapAux.entrySet().isEmpty();
      }
    } else {
      String queryPks = String.format(COMPOSE_PK_MUST_BE_USED_LIST, datasetIdFK,
          fkFieldSchema.getIdFieldSchema().toString(), datasetIdFK,
          fkConditionalMasterFieldSchemaId, datasetIdFK, datasetIdPK, idFieldSchemaPKString,
          datasetIdPK, fkConditionalLinkedFieldSchemaId, datasetIdPK);
      List<String> pkUnusedList = createAndExecuteQuery(queryPks);
      if (!pkUnusedList.isEmpty()) {
        error = false;
      }
    }
    return error;
  }

  /**
   * Find F ks.
   *
   * @param fkFieldSchema the fk field schema
   * @param datasetIdFK the dataset id FK
   * @param fkConditionalMasterFieldSchemaId the fk conditional master field schema id
   * @param pkMap the pk map
   * @param pkMapAux the pk map aux
   * @return the sets the
   */
  private static Set<String> findFKs(FieldSchema fkFieldSchema, String datasetIdFK,
      String fkConditionalMasterFieldSchemaId, Map<String, String> pkMap,
      Map<String, String> pkMapAux) {
    Set<String> ifFKs = new HashSet<>();
    String queryFks =
        String.format(FK_QUERY_VALUES, datasetIdFK, fkFieldSchema.getIdFieldSchema().toString(),
            datasetIdFK, fkFieldSchema.getIdFieldSchema().toString(), datasetIdFK,
            fkConditionalMasterFieldSchemaId, datasetIdFK);
    List<Object[]> fkList = fieldRepository.queryPKExecution(queryFks);
    for (int i = 0; i < fkList.size(); i++) {
      if (null != pkMap.get(fkList.get(i)[2])) {
        List<String> pksByOptionalValue = Arrays.asList(pkMap.get(fkList.get(i)[2]).split(","));
        List<String> fksByOptionalValue = Arrays.asList(fkList.get(i)[1].toString().split(";"));
        pksByOptionalValue.replaceAll(String::trim);
        fksByOptionalValue.replaceAll(String::trim);

        for (String value : fksByOptionalValue) {

          List<String> pksByOptionalValueAux =
              new ArrayList<>(Arrays.asList(pkMapAux.get(fkList.get(i)[2]).split(",")));
          pksByOptionalValueAux.replaceAll(String::trim);

          if (!pksByOptionalValue.contains("\"" + value + "\"")
              && !pksByOptionalValue.contains(value)) {
            ifFKs.add(fkList.get(i)[0].toString());
          }
          if (pksByOptionalValue.contains(value)
              || pksByOptionalValue.contains("\"" + value + "\"")) {
            pksByOptionalValueAux.remove(value);
            pksByOptionalValueAux.remove("\"" + value + "\"");
          }
          pkMapAux.put((fkList.get(i)[2]).toString(),
              pksByOptionalValueAux.toString().replace("]", "").replace("[", "").trim());
        }
      }
    }
    return ifFKs;
  }



  /**
   * Creates the field value validation query.
   *
   * @param fieldsToValidate the fields to validate
   * @param pkValidation the pk validation
   * @param errorFields the error fields
   */
  private static void createFieldValueValidationQuery(List<FieldValue> fieldsToValidate,
      Validation pkValidation, List<FieldValue> errorFields) {
    for (FieldValue field : fieldsToValidate) {
      List<FieldValidation> fieldValidationList =
          field.getFieldValidations() != null ? field.getFieldValidations() : new ArrayList<>();
      FieldValidation fieldValidation = new FieldValidation();
      fieldValidation.setValidation(pkValidation);
      FieldValue fieldValue = new FieldValue();
      fieldValue.setId(field.getId());
      fieldValidation.setFieldValue(fieldValue);
      fieldValidationList.add(fieldValidation);
      field.setFieldValidations(fieldValidationList);
      if (!field.getValue().equals("")) {
        errorFields.add(field);
      }
    }
  }

  /**
   * Creates the and execute query.
   *
   * @param query the query
   * @return the list
   */
  private static List<String> createAndExecuteQuery(String query) {
    return fieldRepository.queryExecution(query);
  }

  /**
   * Sets the values to validate.
   *
   * @param fkFieldSchema the fk field schema
   * @param pkList the pk list
   * @param fkFields the fk fields
   * @return the boolean
   */
  private static Boolean setValuesToValidate(FieldSchema fkFieldSchema, List<String> pkList,
      List<FieldValue> fkFields) {
    // Values must be
    Set<String> pkSet = new HashSet<>();
    pkSet.addAll(pkList);
    if (Boolean.TRUE.equals(fkFieldSchema.getPkHasMultipleValues())) {
      // we look one by one to know if all values are avaliable
      checkAllValuesMulti(pkSet, fkFields);
    } else {
      // remove the empty char from the list to avoid false error positives
      pkSet.remove("");

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
      final List<String> arrayValue = Arrays.asList(fieldValue.getValue().split(";"));

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
   * @param tableName the tableName
   * @param fkFieldSchema the fk field schema
   * @return the validation
   */
  private static Validation createValidation(String idRule, String idDatasetSchema,
      TableSchema tableName, FieldSchema fkFieldSchema) {
    Validation validation = new Validation();

    Rule rule = rulesRepository.findRule(new ObjectId(idDatasetSchema), new ObjectId(idRule));

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
      validation.setTableName(tableName.getNameTableSchema());
      validation.setShortCode(rule.getShortCode());
      if (null != fkFieldSchema) {
        validation.setFieldName(fkFieldSchema.getHeaderName());
      }
    }
    return validation;
  }

  /**
   * Save field validations.
   *
   * @param fieldValues the field values
   */
  @Modifying
  private static void saveFieldValidations(List<FieldValue> fieldValues) {
    fieldRepository.saveAll(fieldValues);
    fieldRepository.flush();
  }

  /**
   * Check PK.
   *
   * @param pkValues the pk values
   * @param value the value
   * @param pkHasMultipleValues the pk has multiple values
   * @return the boolean
   */
  private static Boolean checkPK(List<String> pkValues, FieldValue value,
      Boolean pkHasMultipleValues) {
    Boolean returnChecked = Boolean.TRUE;
    if (Boolean.TRUE.equals(pkHasMultipleValues)) {
      final List<String> arrayValue = Arrays.asList(value.getValue().split(";"));

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
   * Gets the field schema from schema.
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
   * Gets the table schema from id field schema.
   *
   * @param schema the schema
   * @param idFieldSchema the id field schema
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

}
