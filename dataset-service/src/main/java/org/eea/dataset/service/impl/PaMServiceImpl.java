package org.eea.dataset.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.eea.dataset.persistence.data.domain.FieldValue;
import org.eea.dataset.persistence.data.repository.FieldRepository;
import org.eea.dataset.service.DatasetService;
import org.eea.dataset.service.PaMService;
import org.eea.dataset.service.file.FileCommonUtils;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.pams.EntityPaMVO;
import org.eea.interfaces.vo.pams.SectorVO;
import org.eea.interfaces.vo.pams.SinglePaMVO;
import org.eea.multitenancy.DatasetId;
import org.eea.multitenancy.TenantResolver;
import org.eea.utils.LiteralConstants;
import org.eea.utils.PaMConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


// TODO: Auto-generated Javadoc
/**
 * The Class PaMServiceImpl.
 */
@Service
public class PaMServiceImpl implements PaMService {

  /** The field repository. */
  @Autowired
  private FieldRepository fieldRepository;

  /** The file common utils. */
  @Autowired
  FileCommonUtils fileCommonUtils;

  /** The dataset service. */
  @Autowired
  DatasetService datasetService;

  /**
   * Gets the list single paM.
   *
   * @param datasetId the dataset id
   * @param groupPaMId the group paM id
   * @return the list single paM
   * @throws EEAException the EEA exception
   */
  @Override
  public List<SinglePaMVO> getListSinglePaM(@DatasetId Long datasetId, String groupPaMId)
      throws EEAException {

    // get dataflowId
    Long dataflowId = datasetService.getDataFlowIdById(datasetId);
    // get datasetSchema
    Map<String, String> schemaIds = getPaMsSchemaIds(datasetId, dataflowId);

    // @DatasetId doesn't works
    TenantResolver.setTenantName(LiteralConstants.DATASET_PREFIX + datasetId);
    // --------

    // go through the list singlepam and set attributes
    FieldValue fieldPams = fieldRepository
        .findFirstByIdFieldSchemaAndValue(schemaIds.get(PaMConstants.ID), groupPaMId);
    String singlePamsValue =
        getValue(hasRecordsAndFields(fieldPams), schemaIds.get(PaMConstants.LIST_OF_SINGLE_PAMS));
    List<SinglePaMVO> listSinglePaM = new ArrayList<>();
    if (singlePamsValue != null) {
      String[] singlePams = singlePamsValue.split(PaMConstants.SEPARATOR);
      for (int i = 0; i < singlePams.length; i++) {
        SinglePaMVO singlePaMVO = new SinglePaMVO();
        singlePaMVO.setId(singlePams[i]);
        // set attributes of table1
        setAttributesTable1(schemaIds, singlePams[i], singlePaMVO);
        // set attributes entities
        setAttributesEntities(schemaIds, singlePams[i], singlePaMVO);
        // set attributes table2
        setAttributesTable2(schemaIds, singlePams[i], singlePaMVO);
        // set attributes sectors
        setAttributesSectors(schemaIds, singlePams[i], singlePaMVO);

        listSinglePaM.add(singlePaMVO);
      }
    }

    return listSinglePaM;
  }


  /**
   * Sets the attributes sectors.
   *
   * @param schemaIds the schema ids
   * @param singlePamId the single pam id
   * @param singlePaMVO the single pa MVO
   */
  private void setAttributesSectors(Map<String, String> schemaIds, String singlePamId,
      SinglePaMVO singlePaMVO) {
    List<SectorVO> sectors = new ArrayList<>();
    List<FieldValue> fkSectorObjectives = fieldRepository.findByIdFieldSchemaAndValue(
        schemaIds.get(PaMConstants.FK_PAMS_SECTOR_OBJECTIVES), singlePamId);
    List<FieldValue> fkOtherObjectives = fieldRepository.findByIdFieldSchemaAndValue(
        schemaIds.get(PaMConstants.FK_PAMS_OTHER_OBJECTIVES), singlePamId);
    if (fkSectorObjectives != null) {
      for (FieldValue fieldValue : fkSectorObjectives) {
        SectorVO sectorPaMs = new SectorVO();
        List<FieldValue> fields = hasRecordsAndFields(fieldValue);
        // add attributes sectorObjectives
        sectorPaMs.setOtherSectors(getValue(fields, schemaIds.get(PaMConstants.OTHER_SECTORS)));
        sectorPaMs.setSectorAffected(getValue(fields, schemaIds.get(PaMConstants.SECTOR_AFFECTED)));
        String objectiveList = getValue(fields, schemaIds.get(PaMConstants.OBJECTIVE));
        sectorPaMs.setObjectives(
            objectiveList != null ? Arrays.asList(objectiveList.split(PaMConstants.SEPARATOR))
                : null);
        String pkSectorObjective = getValue(fields, schemaIds.get(PaMConstants.PK));
        // add attributes otherObjectives
        List<String> objectives =
            buildOtherObjective(schemaIds, fkOtherObjectives, pkSectorObjective);
        sectorPaMs.setOtherObjectives(objectives);
        sectors.add(sectorPaMs);
      }
    }
    singlePaMVO.setSectors(sectors);


  }


  /**
   * Builds the other objective.
   *
   * @param schemaIds the schema ids
   * @param fkOtherObjectives the fk other objectives
   * @param pkSectorObjective the pk sector objective
   * @return the list
   */
  private List<String> buildOtherObjective(Map<String, String> schemaIds,
      List<FieldValue> fkOtherObjectives, String pkSectorObjective) {
    List<String> objectives = new ArrayList<>();
    if (fkOtherObjectives != null) {
      for (FieldValue fieldValue2 : fkOtherObjectives) {
        List<FieldValue> fieldsOtherObjective = hasRecordsAndFields(fieldValue2);
        if (fieldsOtherObjective != null) {
          String otherObjective =
              getOtherObjectiveValue(schemaIds, pkSectorObjective, fieldsOtherObjective);
          if (otherObjective != null) {
            objectives.add(otherObjective);
          }
        }
      }
    }
    return objectives;
  }


  /**
   * Gets the other objective value.
   *
   * @param schemaIds the schema ids
   * @param pkSectorObjective the pk sector objective
   * @param fieldsOtherObjective the fields other objective
   * @return the other objective value
   */
  private String getOtherObjectiveValue(Map<String, String> schemaIds, String pkSectorObjective,
      List<FieldValue> fieldsOtherObjective) {
    for (FieldValue fieldValue3 : fieldsOtherObjective) {
      if (fieldValue3.getIdFieldSchema().equals(schemaIds.get(PaMConstants.FK_SECTOR_OBJECTIVES))
          && fieldValue3.getValue().equals(pkSectorObjective)) {
        return getValue(fieldValue3.getRecord().getFields(), schemaIds.get(PaMConstants.OTHER));
      }
    }
    return null;
  }


  /**
   * Sets the attributes table 2.
   *
   * @param schemaIds the schema ids
   * @param singlePamId the single pam id
   * @param singlePaMVO the single pa MVO
   */
  private void setAttributesTable2(Map<String, String> schemaIds, String singlePamId,
      SinglePaMVO singlePaMVO) {
    FieldValue fkPamsTable2 = fieldRepository
        .findFirstByIdFieldSchemaAndValue(schemaIds.get(PaMConstants.FK_PAMS_TABLE_2), singlePamId);
    if (fkPamsTable2 != null) {
      List<FieldValue> fields = hasRecordsAndFields(fkPamsTable2);
      singlePaMVO
          .setPolicyImpacting(getValue(fields, schemaIds.get(PaMConstants.POLICY_IMPACTING)));
    }
  }


  /**
   * Sets the attributes entities.
   *
   * @param schemaIds the schema ids
   * @param singlePamId the single pam id
   * @param singlePaMVO the single pa MVO
   */
  private void setAttributesEntities(Map<String, String> schemaIds, String singlePamId,
      SinglePaMVO singlePaMVO) {
    List<EntityPaMVO> entitiesPams = new ArrayList<>();
    List<FieldValue> fkPaMsEntities = fieldRepository
        .findByIdFieldSchemaAndValue(schemaIds.get(PaMConstants.FK_PAMS_ENTITIES), singlePamId);
    if (fkPaMsEntities != null) {
      for (FieldValue fieldValue : fkPaMsEntities) {
        List<FieldValue> fields = hasRecordsAndFields(fieldValue);
        EntityPaMVO entityPaMVO = new EntityPaMVO();
        entityPaMVO.setName(getValue(fields, schemaIds.get(PaMConstants.NAME)));
        entityPaMVO.setType(getValue(fields, schemaIds.get(PaMConstants.TYPE)));
        entitiesPams.add(entityPaMVO);
      }
    }
    singlePaMVO.setEntities(entitiesPams);
  }


  /**
   * Sets the attributes table 1.
   *
   * @param schemaIds the schema ids
   * @param singlePamId the single pam id
   * @param singlePaMVO the single pa MVO
   */
  private void setAttributesTable1(Map<String, String> schemaIds, String singlePamId,
      SinglePaMVO singlePaMVO) {
    FieldValue fkPamsTable1 = fieldRepository
        .findFirstByIdFieldSchemaAndValue(schemaIds.get(PaMConstants.FK_PAMS_TABLE_1), singlePamId);
    if (fkPamsTable1 != null) {
      List<FieldValue> fields = hasRecordsAndFields(fkPamsTable1);
      singlePaMVO.setImplementationPeriodStart(
          getValue(fields, schemaIds.get(PaMConstants.IMPLEMENTATION_PERIOD_START)));
      singlePaMVO.setImplementationPeriodFinish(
          getValue(fields, schemaIds.get(PaMConstants.IMPLEMENTATION_PERIOD_FINISH)));
      singlePaMVO.setImplementationPeriodComment(
          getValue(fields, schemaIds.get(PaMConstants.IMPLEMENTATION_PERIOD_COMMENT)));
      singlePaMVO.setStatusImplementation(
          getValue(fields, schemaIds.get(PaMConstants.STATUS_IMPLEMENTATION)));
      singlePaMVO.setIsPolicyMeasureEnvisaged(
          getValue(fields, schemaIds.get(PaMConstants.IS_POLICY_MEASURE_ENVISAGED)));
      singlePaMVO.setProjectionsScenario(
          getValue(fields, schemaIds.get(PaMConstants.PROJECTIONS_SCENARIO)));
      String unionPolicyList = getValue(fields, schemaIds.get(PaMConstants.UNION_POLICY_LIST));
      String typePolicyInstrumentList =
          getValue(fields, schemaIds.get(PaMConstants.TYPE_POLICY_INSTRUMENT));
      String ghgAffectedList = getValue(fields, schemaIds.get(PaMConstants.GHG_AFFECTED));
      singlePaMVO.setUnionPolicyList(
          unionPolicyList != null ? Arrays.asList(unionPolicyList.split(PaMConstants.SEPARATOR))
              : null);
      singlePaMVO.setTypePolicyInstrument(typePolicyInstrumentList != null
          ? Arrays.asList(typePolicyInstrumentList.split(PaMConstants.SEPARATOR))
          : null);
      singlePaMVO.setGhgAffected(
          ghgAffectedList != null ? Arrays.asList(ghgAffectedList.split(PaMConstants.SEPARATOR))
              : null);
    }
  }



  /**
   * Gets the pa ms schema ids.
   *
   * @param datasetId the dataset id
   * @param dataflowId the dataflow id
   * @return the pa ms schema ids
   * @throws EEAException the EEA exception
   */
  private Map<String, String> getPaMsSchemaIds(Long datasetId, Long dataflowId)
      throws EEAException {
    Map<String, String> schemaIds = new HashMap<>();
    // GET ID'S tables
    DataSetSchemaVO schema = fileCommonUtils.getDataSetSchema(dataflowId, datasetId);
    String tablePamsId = fileCommonUtils.getIdTableSchema(PaMConstants.PAMS, schema);
    String table1Id = fileCommonUtils.getIdTableSchema(PaMConstants.TABLE_1, schema);
    String entitiesId = fileCommonUtils.getIdTableSchema(PaMConstants.ENTITIES, schema);
    String table2Id = fileCommonUtils.getIdTableSchema(PaMConstants.TABLE_2, schema);
    String sectorObjectivesId =
        fileCommonUtils.getIdTableSchema(PaMConstants.SECTOR_OBJECTIVES, schema);
    String otherObjectivesId =
        fileCommonUtils.getIdTableSchema(PaMConstants.OTHER_OBJECTIVES, schema);

    // PAMS
    schemaIds.put(PaMConstants.LIST_OF_SINGLE_PAMS, isFieldSchemaNull(
        fileCommonUtils.findIdFieldSchema(PaMConstants.LIST_OF_SINGLE_PAMS, tablePamsId, schema)));
    schemaIds.put(PaMConstants.ID,
        isFieldSchemaNull(fileCommonUtils.findIdFieldSchema(PaMConstants.ID, tablePamsId, schema)));

    // TABLE1
    schemaIds.put(PaMConstants.FK_PAMS_TABLE_1, isFieldSchemaNull(
        fileCommonUtils.findIdFieldSchema(PaMConstants.FK_PAMS, table1Id, schema)));
    schemaIds.put(PaMConstants.IMPLEMENTATION_PERIOD_START, isFieldSchemaNull(fileCommonUtils
        .findIdFieldSchema(PaMConstants.IMPLEMENTATION_PERIOD_START, table1Id, schema)));
    schemaIds.put(PaMConstants.IMPLEMENTATION_PERIOD_FINISH, isFieldSchemaNull(fileCommonUtils
        .findIdFieldSchema(PaMConstants.IMPLEMENTATION_PERIOD_FINISH, table1Id, schema)));
    schemaIds.put(PaMConstants.IMPLEMENTATION_PERIOD_COMMENT, isFieldSchemaNull(fileCommonUtils
        .findIdFieldSchema(PaMConstants.IMPLEMENTATION_PERIOD_COMMENT, table1Id, schema)));
    schemaIds.put(PaMConstants.STATUS_IMPLEMENTATION, isFieldSchemaNull(
        fileCommonUtils.findIdFieldSchema(PaMConstants.STATUS_IMPLEMENTATION, table1Id, schema)));
    schemaIds.put(PaMConstants.IS_POLICY_MEASURE_ENVISAGED, isFieldSchemaNull(fileCommonUtils
        .findIdFieldSchema(PaMConstants.IS_POLICY_MEASURE_ENVISAGED, table1Id, schema)));
    schemaIds.put(PaMConstants.PROJECTIONS_SCENARIO, isFieldSchemaNull(
        fileCommonUtils.findIdFieldSchema(PaMConstants.PROJECTIONS_SCENARIO, table1Id, schema)));
    schemaIds.put(PaMConstants.UNION_POLICY_LIST, isFieldSchemaNull(
        fileCommonUtils.findIdFieldSchema(PaMConstants.UNION_POLICY_LIST, table1Id, schema)));
    schemaIds.put(PaMConstants.TYPE_POLICY_INSTRUMENT, isFieldSchemaNull(
        fileCommonUtils.findIdFieldSchema(PaMConstants.TYPE_POLICY_INSTRUMENT, table1Id, schema)));
    schemaIds.put(PaMConstants.GHG_AFFECTED, isFieldSchemaNull(
        fileCommonUtils.findIdFieldSchema(PaMConstants.GHG_AFFECTED, table1Id, schema)));

    // ENTITIES
    schemaIds.put(PaMConstants.FK_PAMS_ENTITIES, isFieldSchemaNull(
        fileCommonUtils.findIdFieldSchema(PaMConstants.FK_PAMS, entitiesId, schema)));
    schemaIds.put(PaMConstants.TYPE, isFieldSchemaNull(
        fileCommonUtils.findIdFieldSchema(PaMConstants.TYPE, entitiesId, schema)));
    schemaIds.put(PaMConstants.NAME, isFieldSchemaNull(
        fileCommonUtils.findIdFieldSchema(PaMConstants.NAME, entitiesId, schema)));

    // TABLE2
    schemaIds.put(PaMConstants.FK_PAMS_TABLE_2, isFieldSchemaNull(
        fileCommonUtils.findIdFieldSchema(PaMConstants.FK_PAMS, table2Id, schema)));
    schemaIds.put(PaMConstants.POLICY_IMPACTING, isFieldSchemaNull(
        fileCommonUtils.findIdFieldSchema(PaMConstants.POLICY_IMPACTING, table2Id, schema)));

    // SECTOR OBJECTIVES
    schemaIds.put(PaMConstants.FK_PAMS_SECTOR_OBJECTIVES, isFieldSchemaNull(
        fileCommonUtils.findIdFieldSchema(PaMConstants.FK_PAMS, sectorObjectivesId, schema)));
    schemaIds.put(PaMConstants.SECTOR_AFFECTED, isFieldSchemaNull(fileCommonUtils
        .findIdFieldSchema(PaMConstants.SECTOR_AFFECTED, sectorObjectivesId, schema)));
    schemaIds.put(PaMConstants.PK, isFieldSchemaNull(
        fileCommonUtils.findIdFieldSchema(PaMConstants.PK, sectorObjectivesId, schema)));
    schemaIds.put(PaMConstants.OTHER_SECTORS, isFieldSchemaNull(
        fileCommonUtils.findIdFieldSchema(PaMConstants.OTHER_SECTORS, sectorObjectivesId, schema)));
    schemaIds.put(PaMConstants.OBJECTIVE, isFieldSchemaNull(
        fileCommonUtils.findIdFieldSchema(PaMConstants.OBJECTIVE, sectorObjectivesId, schema)));

    // OTHER OBJECTIVES
    schemaIds.put(PaMConstants.FK_PAMS_OTHER_OBJECTIVES, isFieldSchemaNull(
        fileCommonUtils.findIdFieldSchema(PaMConstants.FK_PAMS, otherObjectivesId, schema)));
    schemaIds.put(PaMConstants.FK_SECTOR_OBJECTIVES, isFieldSchemaNull(fileCommonUtils
        .findIdFieldSchema(PaMConstants.FK_SECTOR_OBJECTIVES, otherObjectivesId, schema)));
    schemaIds.put(PaMConstants.OTHER, isFieldSchemaNull(
        fileCommonUtils.findIdFieldSchema(PaMConstants.OTHER, otherObjectivesId, schema)));

    return schemaIds;
  }


  /**
   * Update groups in lsitofsingle pams and transform when we update a single pams id in a group of
   * pams.
   *
   * @param idListOfSinglePamsField the id list of single pams field
   * @param fieldValueToUpdate the field value to update
   * @param fieldValueInRecord the field value in record
   */
  @Override
  public void updateGroups(String idListOfSinglePamsField, FieldValue fieldValueToUpdate,
      FieldValue fieldValueInRecord) {
    // we update id of the group in the webform
    List<FieldValue> fieldValuesWithData = fieldRepository
        .findAllCascadeListOfSinglePams(idListOfSinglePamsField, fieldValueToUpdate.getValue());

    // if we find atleast one listofsingle filled we transform it
    if (null != fieldValuesWithData && !fieldValuesWithData.isEmpty()) {


      for (FieldValue fieldValue : fieldValuesWithData) {
        // we split and separate any , value
        List<String> items = Arrays.asList(fieldValue.getValue().split("\\s*,\\s*"));

        List<Long> itemsLong = new ArrayList();
        // we split and separate any value and transfrom to a long list
        for (int i = 0; i < items.size(); i++) {
          if (items.get(i).trim().equalsIgnoreCase(fieldValueToUpdate.getValue())) {
            items.set(i, fieldValueInRecord.getValue());
          }
          itemsLong.add(Long.valueOf(items.get(i)));
        }
        String composeListSinglesPams = "";
        composeListSinglesPams = cleanAndComposeString(itemsLong, composeListSinglesPams);
        fieldValue.setValue(composeListSinglesPams);
        fieldRepository.save(fieldValue);
      }
    }
  }

  /**
   * Delete groups if we delete a pk in cascade.
   *
   * @param fieldSchemasList the field schemas list
   * @param fieldValuePk the field value pk
   */
  @Override
  public void deleteGroups(List<Document> fieldSchemasList, String fieldValuePk) {

    // we find the id of list of single pams to delte the part
    String idListOfSinglePamsField = null;
    for (Object documentFieldList : fieldSchemasList) {
      if (null != ((Document) documentFieldList).get("headerName")
          && "ListOfSinglePams".equals(((Document) documentFieldList).getString("headerName"))) {
        idListOfSinglePamsField = ((Document) documentFieldList).get("_id").toString();
      }
    }

    // we update id of the group in the webform
    List<FieldValue> fieldValuesWithData =
        fieldRepository.findAllCascadeListOfSinglePams(idListOfSinglePamsField, fieldValuePk);

    // if we find atleast one listofsingle filled we transform it
    if (null != fieldValuesWithData && !fieldValuesWithData.isEmpty()) {

      for (FieldValue fieldValue : fieldValuesWithData) {
        // we split and separate any , value
        List<String> items = Arrays.asList(fieldValue.getValue().split("\\s*,\\s*"));

        // we split and separate any value and transfrom to a long list
        List<Long> itemsLong = new ArrayList();
        for (int i = 0; i < items.size(); i++) {
          if (!items.get(i).trim().equalsIgnoreCase(fieldValuePk)) {
            itemsLong.add(Long.valueOf(items.get(i)));
          }
        }
        String composeListSinglesPams = "";
        if (!itemsLong.isEmpty()) {
          composeListSinglesPams = cleanAndComposeString(itemsLong, composeListSinglesPams);
        }
        fieldValue.setValue(composeListSinglesPams);
        fieldRepository.save(fieldValue);
      }
    }

  }

  /**
   * Clean and compose string. we create the same string with comas, and clean it
   *
   * @param itemsLong the items long
   * @param composeListSinglesPams the compose list singles pams
   * @return the string
   */
  private String cleanAndComposeString(List<Long> itemsLong, String composeListSinglesPams) {
    Collections.sort(itemsLong);
    StringBuilder valueCompose = new StringBuilder("");
    // we compose the new string sorting it
    for (int i = 0; i < itemsLong.size(); i++) {
      if (i == itemsLong.size() - 1) {
        valueCompose.append(itemsLong.get(i));
      } else {
        valueCompose.append(itemsLong.get(i)).append(", ");
      }
    }
    // we delete the espaces and commas in the 1 or 2 char in the string to clean it
    if (',' == valueCompose.charAt(0) || ' ' == valueCompose.charAt(0)) {
      valueCompose.substring(1, valueCompose.length() - 1);
      if (',' == valueCompose.charAt(0) || ' ' == valueCompose.charAt(0)) {
        valueCompose.substring(1, valueCompose.length() - 1);
      }
    }
    // we delete the espaces and commas in the last or penultimate char in the string to clean it
    if (',' == valueCompose.charAt(valueCompose.length() - 1)
        || ' ' == valueCompose.charAt(valueCompose.length() - 1)) {
      valueCompose.substring(0, valueCompose.length() - 1);
      if (',' == valueCompose.charAt(valueCompose.length() - 1)
          || ' ' == valueCompose.charAt(valueCompose.length() - 1)) {
        valueCompose.substring(0, valueCompose.length() - 1);
      }
    }
    return valueCompose.toString();
  }


  /**
   * Gets the value.
   *
   * @param fields the fields
   * @param schemaId the schema id
   * @return the value
   */
  public String getValue(List<FieldValue> fields, String schemaId) {
    if (fields != null) {
      for (FieldValue fieldValue : fields) {
        if (fieldValue.getIdFieldSchema().equals(schemaId)) {
          return fieldValue.getValue();
        }
      }
    }
    return null;
  }

  /**
   * Checks if is field schema null.
   *
   * @param fieldSchema the field schema
   * @return the string
   */
  public String isFieldSchemaNull(FieldSchemaVO fieldSchema) {
    return fieldSchema != null ? fieldSchema.getId() : null;
  }

  /**
   * Checks for records and fields.
   *
   * @param fieldValue the field value
   * @return the list
   */
  private List<FieldValue> hasRecordsAndFields(FieldValue fieldValue) {
    return fieldValue.getRecord() != null || fieldValue.getRecord().getFields() != null
        ? fieldValue.getRecord().getFields()
        : null;
  }
}
