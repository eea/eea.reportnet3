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
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.dataset.service.PaMService;
import org.eea.dataset.service.file.FileCommonUtils;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.FieldVO;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.interfaces.vo.pams.EntityPaMVO;
import org.eea.interfaces.vo.pams.SectorVO;
import org.eea.interfaces.vo.pams.SinglePaMVO;
import org.eea.multitenancy.TenantResolver;
import org.eea.utils.LiteralConstants;
import org.eea.utils.PaMConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;


/**
 * The Class PaMServiceImpl.
 */
@Service
public class PaMServiceImpl implements PaMService {

  private static final String HEADER_NAME = "headerName";
  /**
   * The field repository.
   */
  @Autowired
  private FieldRepository fieldRepository;

  /**
   * The file common utils.
   */
  @Autowired
  private FileCommonUtils fileCommonUtils;

  /** The dataset metabase service. */
  @Autowired
  private DatasetMetabaseService datasetMetabaseService;

  /** The dataset schema service. */
  @Lazy
  @Autowired
  private DatasetSchemaService datasetSchemaService;

  /**
   * Gets the list single paM.
   *
   * @param datasetId the dataset id
   * @param groupPaMId the group paM id
   *
   * @return the list single paM
   *
   * @throws EEAException the EEA exception
   */
  @Override
  public List<SinglePaMVO> getListSinglePaM(Long datasetId, String groupPaMId) throws EEAException {

    // get dataflowId
    Long dataflowId = datasetMetabaseService.findDatasetMetabase(datasetId).getDataflowId();
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
      String[] singlePamsIds = singlePamsValue.split(PaMConstants.SEPARATOR);
      List<String> singlePamsIdsList = Arrays.asList(singlePamsIds);
      Map<String, List<FieldValue>> paMsFields =
          getPaMsFields(datasetId, schemaIds, singlePamsIdsList);
      Map<String, List<FieldVO>> otherDatasetFields = getOtherDatasetFields(schemaIds, paMsFields);

      for (int i = 0; i < singlePamsIds.length; i++) {
        SinglePaMVO singlePaMVO = new SinglePaMVO();
        singlePaMVO.setId(singlePamsIds[i]);
        singlePaMVO.setPaMName(
            getPaMNameValue(schemaIds, singlePamsIds[i], paMsFields.get(PaMConstants.SINGLES)));
        // set attributes of table1
        setAttributesTable1(schemaIds, singlePamsIds[i], singlePaMVO,
            paMsFields.get(PaMConstants.TABLE_1), otherDatasetFields);
        // set attributes entities
        setAttributesEntities(schemaIds, singlePamsIds[i], singlePaMVO,
            paMsFields.get(PaMConstants.ENTITIES));
        // set attributes table2
        setAttributesTable2(schemaIds, singlePamsIds[i], singlePaMVO,
            paMsFields.get(PaMConstants.TABLE_2));
        // set attributes sectors
        setAttributesSectors(schemaIds, singlePamsIds[i], singlePaMVO, paMsFields,
            otherDatasetFields);
        // set union policy other
        setAttributesUnionPolicyOther(schemaIds, singlePamsIds[i], singlePaMVO,
            paMsFields.get(PaMConstants.UNION_POLICY_OTHER));

        listSinglePaM.add(singlePaMVO);
      }
    }

    return listSinglePaM;
  }


  /**
   * Gets the pa ms fields.
   *
   * @param datasetId the dataset id
   * @param schemaIds the schema ids
   * @param singlePamsIdsList the single pams ids list
   * @return the pa ms fields
   */
  private Map<String, List<FieldValue>> getPaMsFields(Long datasetId, Map<String, String> schemaIds,
      List<String> singlePamsIdsList) {
    Map<String, List<FieldValue>> tablesFields = new HashMap<>();
    tablesFields.put(PaMConstants.SINGLES, fieldRepository
        .findByFieldSchemaAndValue(schemaIds.get(PaMConstants.ID), singlePamsIdsList, datasetId));
    tablesFields.put(PaMConstants.TABLE_1, fieldRepository.findByFieldSchemaAndValue(
        schemaIds.get(PaMConstants.FK_PAMS_TABLE_1), singlePamsIdsList, datasetId));
    tablesFields.put(PaMConstants.ENTITIES, fieldRepository.findByFieldSchemaAndValue(
        schemaIds.get(PaMConstants.FK_PAMS_ENTITIES), singlePamsIdsList, datasetId));
    tablesFields.put(PaMConstants.TABLE_2, fieldRepository.findByFieldSchemaAndValue(
        schemaIds.get(PaMConstants.FK_PAMS_TABLE_2), singlePamsIdsList, datasetId));
    tablesFields.put(PaMConstants.SECTOR_OBJECTIVES, fieldRepository.findByFieldSchemaAndValue(
        schemaIds.get(PaMConstants.FK_PAMS_SECTOR_OBJECTIVES), singlePamsIdsList, datasetId));
    tablesFields.put(PaMConstants.OTHER_OBJECTIVES, fieldRepository.findByFieldSchemaAndValue(
        schemaIds.get(PaMConstants.FK_PAMS_OTHER_OBJECTIVES), singlePamsIdsList, datasetId));
    tablesFields.put(PaMConstants.UNION_POLICY_OTHER, fieldRepository.findByFieldSchemaAndValue(
        schemaIds.get(PaMConstants.FK_PAMS_UNION_POLICY_OTHER), singlePamsIdsList, datasetId));

    return tablesFields;
  }


  /**
   * Gets the other dataset fields.
   *
   * @param schemaIds the schema ids
   * @param paMsFields the pa ms fields
   * @return the other dataset fields
   */
  private Map<String, List<FieldVO>> getOtherDatasetFields(Map<String, String> schemaIds,
      Map<String, List<FieldValue>> paMsFields) {
    Map<String, List<FieldVO>> tablesFields = new HashMap<>();
    List<String> sectorAffectedList = new ArrayList<>();
    List<String> objectiveList = new ArrayList<>();
    List<String> unionPolicyList = new ArrayList<>();
    if (paMsFields.get(PaMConstants.SECTOR_OBJECTIVES) != null) {
      for (FieldValue fieldValue : paMsFields.get(PaMConstants.SECTOR_OBJECTIVES)) {
        List<FieldValue> fields = hasRecordsAndFields(fieldValue);
        sectorAffectedList.add(getValue(fields, schemaIds.get(PaMConstants.SECTOR_AFFECTED)));
        String objectiveToList = getValue(fields, schemaIds.get(PaMConstants.OBJECTIVE));
        objectiveList.addAll(getListSplit(objectiveToList));
      }
      for (FieldValue fieldValue : paMsFields.get(PaMConstants.TABLE_1)) {
        List<FieldValue> fields = hasRecordsAndFields(fieldValue);
        String unionPolicyToList = getValue(fields, schemaIds.get(PaMConstants.UNION_POLICY_LIST));
        unionPolicyList.addAll(getListSplit(unionPolicyToList));
      }
      if (!sectorAffectedList.isEmpty()) {
        tablesFields.put(PaMConstants.SECTOR,
            fieldRepository.findValue(schemaIds.get(PaMConstants.SECTOR_PK),
                schemaIds.get(PaMConstants.SECTOR), schemaIds.get(PaMConstants.DATASET_ID_PK),
                sectorAffectedList));
      }
      if (!objectiveList.isEmpty()) {
        tablesFields.put(PaMConstants.OTHER_OBJECTIVES,
            fieldRepository.findValue(schemaIds.get(PaMConstants.OBJECTIVE_PK),
                schemaIds.get(PaMConstants.OBJECTIVE_OBJECTIVES),
                schemaIds.get(PaMConstants.DATASET_ID_PK), objectiveList));
      }
      if (!unionPolicyList.isEmpty()) {
        tablesFields.put(PaMConstants.UNION_POLICY,
            fieldRepository.findValue(schemaIds.get(PaMConstants.UNION_POLICIES_PK),
                schemaIds.get(PaMConstants.UNION_POLICY), schemaIds.get(PaMConstants.DATASET_ID_PK),
                unionPolicyList));
      }
    }
    return tablesFields;
  }

  /**
   * Gets the pa M name value.
   *
   * @param schemaIds the schema ids
   * @param singlePamId the single pam id
   * @param singleFields the single fields
   * @return the pa M name value
   */
  private String getPaMNameValue(Map<String, String> schemaIds, String singlePamId,
      List<FieldValue> singleFields) {
    String value = null;
    for (FieldValue fieldValue : singleFields) {
      if (singlePamId.equals(fieldValue.getValue())) {
        value = getValue(hasRecordsAndFields(fieldValue), schemaIds.get(PaMConstants.TITLE));
        break;
      }
    }
    return value;
  }


  /**
   * Sets the attributes sectors.
   *
   * @param schemaIds the schema ids
   * @param singlePamId the single pam id
   * @param singlePaMVO the single pa MVO
   * @param paMsFields the pa ms fields
   * @param otherDatasetFields the other dataset fields
   */
  private void setAttributesSectors(Map<String, String> schemaIds, String singlePamId,
      SinglePaMVO singlePaMVO, Map<String, List<FieldValue>> paMsFields,
      Map<String, List<FieldVO>> otherDatasetFields) {
    List<SectorVO> sectors = new ArrayList<>();
    List<FieldValue> sectorObjectivesFields = paMsFields.get(PaMConstants.SECTOR_OBJECTIVES);
    if (sectorObjectivesFields != null) {
      for (FieldValue fieldValue : sectorObjectivesFields) {
        if (fieldValue != null && singlePamId.equals(fieldValue.getValue())) {
          SectorVO sectorPaMs = new SectorVO();
          List<FieldValue> fields = hasRecordsAndFields(fieldValue);
          // add attributes sectorObjectives
          sectorPaMs.setOtherSectors(getValue(fields, schemaIds.get(PaMConstants.OTHER_SECTORS)));
          sectorPaMs
              .setSectorAffected(getValueOtherTable(otherDatasetFields.get(PaMConstants.SECTOR),
                  getValue(fields, schemaIds.get(PaMConstants.SECTOR_AFFECTED))));
          String objectiveList = getValue(fields, schemaIds.get(PaMConstants.OBJECTIVE));

          sectorPaMs.setObjectives(getObjectivesValue(getListSplit(objectiveList), schemaIds,
              otherDatasetFields.get(PaMConstants.OTHER_OBJECTIVES)));
          String pkSectorObjective = getValue(fields, schemaIds.get(PaMConstants.PK));
          // add attributes otherObjectives
          List<String> objectives =
              buildOtherObjective(schemaIds, paMsFields.get(PaMConstants.OTHER_OBJECTIVES),
                  pkSectorObjective, fieldValue.getValue());
          sectorPaMs.setOtherObjectives(objectives);
          sectors.add(sectorPaMs);
        }
      }
    }
    singlePaMVO.setSectors(sectors);


  }

  /**
   * Gets the value other table.
   *
   * @param valueList the objective list
   * @param id the id
   * @return the value other table
   */
  private String getValueOtherTable(List<FieldVO> valueList, String id) {
    String value = null;
    if (valueList != null) {
      for (FieldVO valueId : valueList) {
        if (id.equals(valueId.getId())) {
          value = valueId.getValue();
          break;
        }
      }
    }
    return value;

  }


  /**
   * Gets the objectives value.
   *
   * @param objectiveIds the objective ids
   * @param schemaIds the schema ids
   * @param objectiveValues the objective values
   * @return the objectives value
   */
  private List<String> getObjectivesValue(List<String> objectiveIds, Map<String, String> schemaIds,
      List<FieldVO> objectiveValues) {
    List<String> objectivesValue = new ArrayList<>();
    if (null != objectiveIds) {
      for (String objective : objectiveIds) {
        for (FieldVO field : objectiveValues) {
          if (objective.equals(field.getId())) {
            objectivesValue.add(field.getValue());
            break;
          }
        }
      }
    }
    return objectivesValue;
  }


  /**
   * Builds the other objective.
   *
   * @param schemaIds the schema ids
   * @param fkOtherObjectives the fk other objectives
   * @param pkSectorObjective the pk sector objective
   * @param SingleId the single id
   * @return the list
   */
  private List<String> buildOtherObjective(Map<String, String> schemaIds,
      List<FieldValue> fkOtherObjectives, String pkSectorObjective, String SingleId) {
    List<String> objectives = new ArrayList<>();
    if (fkOtherObjectives != null) {
      for (FieldValue fieldValue2 : fkOtherObjectives) {
        if (SingleId.equals(fieldValue2.getValue())) {
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
    }
    return objectives;
  }


  /**
   * Gets the other objective value.
   *
   * @param schemaIds the schema ids
   * @param pkSectorObjective the pk sector objective
   * @param fieldsOtherObjective the fields other objective
   *
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
   * @param table2Fields the table 2 fields
   */
  private void setAttributesTable2(Map<String, String> schemaIds, String singlePamId,
      SinglePaMVO singlePaMVO, List<FieldValue> table2Fields) {
    for (FieldValue fkPamsTable2 : table2Fields) {
      if (fkPamsTable2 != null && singlePamId.equals(fkPamsTable2.getValue())) {
        List<FieldValue> fields = hasRecordsAndFields(fkPamsTable2);
        singlePaMVO
            .setPolicyImpacting(getValue(fields, schemaIds.get(PaMConstants.POLICY_IMPACTING)));
      }
    }

  }

  /**
   * Sets the attributes union policy other.
   *
   * @param schemaIds the schema ids
   * @param singlePamId the single pam id
   * @param singlePaMVO the single pa MVO
   * @param otherUnionPolicyFields the other union policy fields
   */
  private void setAttributesUnionPolicyOther(Map<String, String> schemaIds, String singlePamId,
      SinglePaMVO singlePaMVO, List<FieldValue> otherUnionPolicyFields) {
    List<String> otherUnionPolicy = new ArrayList<>();
    for (FieldValue fkPamsTable2 : otherUnionPolicyFields) {
      if (fkPamsTable2 != null && singlePamId.equals(fkPamsTable2.getValue())) {
        List<FieldValue> fields = hasRecordsAndFields(fkPamsTable2);
        otherUnionPolicy.add(getValue(fields, schemaIds.get(PaMConstants.OTHER_UNION_POLICY)));
      }
    }
    singlePaMVO.setOtherUnionPolicy(otherUnionPolicy);
  }

  /**
   * Sets the attributes entities.
   *
   * @param schemaIds the schema ids
   * @param singlePamId the single pam id
   * @param singlePaMVO the single pa MVO
   * @param entitiesFields the entities fields
   */
  private void setAttributesEntities(Map<String, String> schemaIds, String singlePamId,
      SinglePaMVO singlePaMVO, List<FieldValue> entitiesFields) {
    List<EntityPaMVO> entitiesPams = new ArrayList<>();
    if (entitiesFields != null) {
      for (FieldValue fieldValue : entitiesFields) {
        if (fieldValue != null && singlePamId.equals(fieldValue.getValue())) {
          List<FieldValue> fields = hasRecordsAndFields(fieldValue);
          EntityPaMVO entityPaMVO = new EntityPaMVO();
          entityPaMVO.setName(getValue(fields, schemaIds.get(PaMConstants.NAME)));
          entityPaMVO.setType(getValue(fields, schemaIds.get(PaMConstants.TYPE)));
          entitiesPams.add(entityPaMVO);
        }
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
   * @param table1Fields the table 1 fields
   * @param otherDatasetFields the other dataset fields
   */
  private void setAttributesTable1(Map<String, String> schemaIds, String singlePamId,
      SinglePaMVO singlePaMVO, List<FieldValue> table1Fields,
      Map<String, List<FieldVO>> otherDatasetFields) {
    if (table1Fields != null) {
      for (FieldValue fkPamsTable1 : table1Fields) {
        if (fkPamsTable1 != null && singlePamId.equals(fkPamsTable1.getValue())) {
          List<FieldValue> fields = hasRecordsAndFields(fkPamsTable1);
          singlePaMVO.setImplementationPeriodStart(
              getValue(fields, schemaIds.get(PaMConstants.IMPLEMENTATION_PERIOD_START)));
          singlePaMVO.setImplementationPeriodFinish(
              getValue(fields, schemaIds.get(PaMConstants.IMPLEMENTATION_PERIOD_FINISH)));
          singlePaMVO.setImplementationPeriodComment(
              getValue(fields, schemaIds.get(PaMConstants.IMPLEMENTATION_PERIOD_COMMENT)));
          singlePaMVO.setStatusImplementation(
              getValue(fields, schemaIds.get(PaMConstants.STATUS_IMPLEMENTATION)));
          singlePaMVO.setProjectionsScenario(
              getValue(fields, schemaIds.get(PaMConstants.PROJECTIONS_SCENARIO)));
          singlePaMVO.setOtherPolicyInstrument(
              getValue(fields, schemaIds.get(PaMConstants.OTHER_POLICY_INSTRUMENT)));
          singlePaMVO.setUnionPolicy(getValue(fields, schemaIds.get(PaMConstants.UNION_POLICY_T1)));
          String unionPolicyList = getValue(fields, schemaIds.get(PaMConstants.UNION_POLICY_LIST));
          String typePolicyInstrumentList =
              getValue(fields, schemaIds.get(PaMConstants.TYPE_POLICY_INSTRUMENT));
          String ghgAffectedList = getValue(fields, schemaIds.get(PaMConstants.GHG_AFFECTED));
          List<String> unionPolicyIdList = getListSplit(unionPolicyList);
          List<String> unionPolicy = getUnionPolicyListValue(otherDatasetFields, unionPolicyIdList);
          singlePaMVO.setUnionPolicyList(unionPolicy);
          singlePaMVO.setTypePolicyInstrument(getListSplit(typePolicyInstrumentList));
          singlePaMVO.setGhgAffected(getListSplit(ghgAffectedList));
        }
      }
    }

  }


  /**
   * Gets the union policy list value.
   *
   * @param otherDatasetFields the other dataset fields
   * @param unionPolicyIdList the union policy id list
   * @return the union policy list value
   */
  private List<String> getUnionPolicyListValue(Map<String, List<FieldVO>> otherDatasetFields,
      List<String> unionPolicyIdList) {
    List<String> unionPolicy = new ArrayList<>();
    if (unionPolicyIdList != null) {
      for (String id : unionPolicyIdList) {
        unionPolicy.add(getValueOtherTable(otherDatasetFields.get(PaMConstants.UNION_POLICY), id));
      }
    }
    return unionPolicy;
  }


  /**
   * Gets the list split.
   *
   * @param valueList the value list
   * @return the list split
   */
  private List<String> getListSplit(String valueList) {
    return valueList != null ? Arrays.asList(valueList.split(PaMConstants.SEPARATOR)) : null;
  }



  /**
   * Gets the pa ms schema ids.
   *
   * @param datasetId the dataset id
   * @param dataflowId the dataflow id
   *
   * @return the pa ms schema ids
   *
   * @throws EEAException the EEA exception
   */
  private Map<String, String> getPaMsSchemaIds(Long datasetId, Long dataflowId)
      throws EEAException {
    Map<String, String> schemaIds = new HashMap<>();
    // GET ID'S tables
    DataSetSchemaVO schema = fileCommonUtils.getDataSetSchemaVO(dataflowId, datasetId);
    String tablePamsId = fileCommonUtils.getIdTableSchema(PaMConstants.PAMS, schema);
    String table1Id = fileCommonUtils.getIdTableSchema(PaMConstants.TABLE_1, schema);
    String entitiesId = fileCommonUtils.getIdTableSchema(PaMConstants.ENTITIES, schema);
    String table2Id = fileCommonUtils.getIdTableSchema(PaMConstants.TABLE_2, schema);
    String sectorObjectivesId =
        fileCommonUtils.getIdTableSchema(PaMConstants.SECTOR_OBJECTIVES, schema);
    String otherObjectivesId =
        fileCommonUtils.getIdTableSchema(PaMConstants.OTHER_OBJECTIVES, schema);
    String unionPolicyOtherId =
        fileCommonUtils.getIdTableSchema(PaMConstants.UNION_POLICY_OTHER, schema);


    // PAMS
    schemaIds.put(PaMConstants.LIST_OF_SINGLE_PAMS, isFieldSchemaNull(
        fileCommonUtils.findIdFieldSchema(PaMConstants.LIST_OF_SINGLE_PAMS, tablePamsId, schema)));
    schemaIds.put(PaMConstants.ID,
        isFieldSchemaNull(fileCommonUtils.findIdFieldSchema(PaMConstants.ID, tablePamsId, schema)));
    schemaIds.put(PaMConstants.TITLE, isFieldSchemaNull(
        fileCommonUtils.findIdFieldSchema(PaMConstants.TITLE, tablePamsId, schema)));

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
    schemaIds.put(PaMConstants.PROJECTIONS_SCENARIO, isFieldSchemaNull(
        fileCommonUtils.findIdFieldSchema(PaMConstants.PROJECTIONS_SCENARIO, table1Id, schema)));
    schemaIds.put(PaMConstants.UNION_POLICY_LIST, isFieldSchemaNull(
        fileCommonUtils.findIdFieldSchema(PaMConstants.UNION_POLICY_LIST, table1Id, schema)));
    schemaIds.put(PaMConstants.TYPE_POLICY_INSTRUMENT, isFieldSchemaNull(
        fileCommonUtils.findIdFieldSchema(PaMConstants.TYPE_POLICY_INSTRUMENT, table1Id, schema)));
    schemaIds.put(PaMConstants.GHG_AFFECTED, isFieldSchemaNull(
        fileCommonUtils.findIdFieldSchema(PaMConstants.GHG_AFFECTED, table1Id, schema)));
    schemaIds.put(PaMConstants.OTHER_POLICY_INSTRUMENT, isFieldSchemaNull(
        fileCommonUtils.findIdFieldSchema(PaMConstants.OTHER_POLICY_INSTRUMENT, table1Id, schema)));
    schemaIds.put(PaMConstants.UNION_POLICY_T1, isFieldSchemaNull(
        fileCommonUtils.findIdFieldSchema(PaMConstants.UNION_POLICY, table1Id, schema)));

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


    // UNION POLICY OTHER
    schemaIds.put(PaMConstants.FK_PAMS_UNION_POLICY_OTHER, isFieldSchemaNull(
        fileCommonUtils.findIdFieldSchema(PaMConstants.FK_PAMS, unionPolicyOtherId, schema)));
    schemaIds.put(PaMConstants.OTHER_UNION_POLICY, isFieldSchemaNull(fileCommonUtils
        .findIdFieldSchema(PaMConstants.OTHER_UNION_POLICY, unionPolicyOtherId, schema)));


    // GET PKS OTHER SCHEMA
    DataSetSchemaVO schemaPKs =
        getSchemaPK(datasetId, schemaIds.get(PaMConstants.SECTOR_AFFECTED), schema, schemaIds);
    String objectivesId = fileCommonUtils.getIdTableSchema(PaMConstants.OBJECTIVES, schemaPKs);
    String sectorsId = fileCommonUtils.getIdTableSchema(PaMConstants.SECTOR, schemaPKs);
    String unionPoliciesId =
        fileCommonUtils.getIdTableSchema(PaMConstants.UNION_POLICIES, schemaPKs);

    schemaIds.put(PaMConstants.OBJECTIVE_OBJECTIVES, isFieldSchemaNull(
        fileCommonUtils.findIdFieldSchema(PaMConstants.OBJECTIVE, objectivesId, schemaPKs)));
    schemaIds.put(PaMConstants.OBJECTIVE_PK, isFieldSchemaNull(
        fileCommonUtils.findIdFieldSchema(PaMConstants.ID, objectivesId, schemaPKs)));
    schemaIds.put(PaMConstants.SECTOR_PK, isFieldSchemaNull(
        fileCommonUtils.findIdFieldSchema(PaMConstants.ID, sectorsId, schemaPKs)));
    schemaIds.put(PaMConstants.SECTOR, isFieldSchemaNull(
        fileCommonUtils.findIdFieldSchema(PaMConstants.SECTOR, sectorsId, schemaPKs)));
    schemaIds.put(PaMConstants.UNION_POLICIES_PK, isFieldSchemaNull(
        fileCommonUtils.findIdFieldSchema(PaMConstants.ID, unionPoliciesId, schemaPKs)));
    schemaIds.put(PaMConstants.UNION_POLICY, isFieldSchemaNull(
        fileCommonUtils.findIdFieldSchema(PaMConstants.UNION_POLICY, unionPoliciesId, schemaPKs)));

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
        List<String> items = Arrays.asList(fieldValue.getValue().split("\\s*;\\s*"));

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
      if (null != ((Document) documentFieldList).get(HEADER_NAME)
          && "ListOfSinglePams".equals(((Document) documentFieldList).getString(HEADER_NAME))) {
        idListOfSinglePamsField = ((Document) documentFieldList).get("_id").toString();
      }
    }

    // we update id of the group in the webform
    List<FieldValue> fieldValuesWithData =
        fieldRepository.findAllCascadeListOfSinglePams(idListOfSinglePamsField, fieldValuePk);

    // if we find atleast one listofsingle filled we transform it
    if (!CollectionUtils.isEmpty(fieldValuesWithData)) {

      for (FieldValue fieldValue : fieldValuesWithData) {
        // we split and separate any , value
        List<String> items = Arrays.asList(fieldValue.getValue().split("\\s*;\\s*"));

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
   *
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
        valueCompose.append(itemsLong.get(i)).append("; ");
      }
    }
    // we delete the espaces and commas in the 1 or 2 char in the string to clean it
    if (';' == valueCompose.charAt(0) || ' ' == valueCompose.charAt(0)) {
      valueCompose.substring(1, valueCompose.length() - 1);
      if (';' == valueCompose.charAt(0) || ' ' == valueCompose.charAt(0)) {
        valueCompose.substring(1, valueCompose.length() - 1);
      }
    }
    // we delete the espaces and commas in the last or penultimate char in the string to clean it
    if (';' == valueCompose.charAt(valueCompose.length() - 1)
        || ' ' == valueCompose.charAt(valueCompose.length() - 1)) {
      valueCompose.substring(0, valueCompose.length() - 1);
      if (';' == valueCompose.charAt(valueCompose.length() - 1)
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
   *
   * @return the value
   */
  private String getValue(List<FieldValue> fields, String schemaId) {
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
   *
   * @return the string
   */
  private String isFieldSchemaNull(FieldSchemaVO fieldSchema) {
    return fieldSchema != null ? fieldSchema.getId() : null;
  }

  /**
   * Checks for records and fields.
   *
   * @param fieldValue the field value
   *
   * @return the list
   */
  private List<FieldValue> hasRecordsAndFields(FieldValue fieldValue) {
    return fieldValue != null && fieldValue.getRecord() != null
        && fieldValue.getRecord().getFields() != null ? fieldValue.getRecord().getFields() : null;
  }

  /**
   * Gets the schema PK.
   *
   * @param datasetIdReference the dataset id reference
   * @param idFieldSchemaFK the id field schema FK
   * @param schemaFK the schema FK
   * @param schemaIds the schema ids
   * @return the schema PK
   */
  private DataSetSchemaVO getSchemaPK(Long datasetIdReference, String idFieldSchemaFK,
      DataSetSchemaVO schemaFK, Map<String, String> schemaIds) {
    String idFieldSchemaPKString = getIdFieldSchemaPK(idFieldSchemaFK, schemaFK);
    // Id Dataset contains PK list
    Long datasetIdRefered = datasetMetabaseService
        .getDatasetDestinationForeignRelation(datasetIdReference, idFieldSchemaPKString);
    schemaIds.put(PaMConstants.DATASET_ID_PK, datasetIdRefered.toString());
    // Get PK Schema
    String pkSchemaId = datasetMetabaseService.findDatasetSchemaIdById(datasetIdRefered);
    // DataSetSchema datasetSchemaPK
    return datasetSchemaService.getDataSchemaById(pkSchemaId);
  }

  /**
   * Gets the id field schema PK.
   *
   * @param idFieldSchema the id field schema
   * @param datasetSchemaFK the dataset schema FK
   * @return the id field schema PK
   */
  private static String getIdFieldSchemaPK(String idFieldSchema, DataSetSchemaVO datasetSchemaFK) {
    FieldSchemaVO idFieldSchemaPk = getPKFieldFromFKField(datasetSchemaFK, idFieldSchema);

    String idFieldSchemaPKString = "";
    if (null != idFieldSchemaPk && null != idFieldSchemaPk.getReferencedField()
        && null != idFieldSchemaPk.getReferencedField().getIdPk()) {
      idFieldSchemaPKString = idFieldSchemaPk.getReferencedField().getIdPk();
    }
    return idFieldSchemaPKString;
  }

  /**
   * Gets the PK field from FK field.
   *
   * @param schema the schema
   * @param idFieldSchema the id field schema
   * @return the PK field from FK field
   */
  private static FieldSchemaVO getPKFieldFromFKField(DataSetSchemaVO schema, String idFieldSchema) {

    FieldSchemaVO pkField = null;
    Boolean locatedPK = false;
    if (schema != null && schema.getTableSchemas() != null) {
      for (TableSchemaVO table : schema.getTableSchemas()) {
        for (FieldSchemaVO field : table.getRecordSchema().getFieldSchema()) {
          if (field.getId().equals(idFieldSchema)) {
            pkField = field;
            locatedPK = Boolean.TRUE;
            break;
          }
        }
        if (locatedPK.equals(Boolean.TRUE)) {
          break;
        }
      }
    }
    return pkField;
  }
}
