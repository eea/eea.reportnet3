package org.eea.dataset.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bson.Document;
import org.eea.dataset.persistence.data.domain.FieldValue;
import org.eea.dataset.persistence.data.repository.FieldRepository;
import org.eea.dataset.service.PaMService;
import org.eea.interfaces.vo.pams.EntityPaMVO;
import org.eea.interfaces.vo.pams.SectorVO;
import org.eea.interfaces.vo.pams.SinglePaMVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * The Class PaMsServiceImpl.
 */
@Service
public class PaMServiceImpl implements PaMService {

  /** The field repository. */
  @Autowired
  private FieldRepository fieldRepository;

  /**
   * Gets the list single paM.
   *
   * @return the list single paM
   */
  @Override
  public List<SinglePaMVO> getListSinglePaM() {
    SinglePaMVO singlePaMs = new SinglePaMVO();
    SectorVO sectorVO = new SectorVO();
    EntityPaMVO entity = new EntityPaMVO();
    List<SinglePaMVO> singlesPaMs = new ArrayList<>();
    List<EntityPaMVO> entitiesList = new ArrayList<>();
    List<String> unionPolicyList = new ArrayList<>();
    List<String> objectives = new ArrayList<>();
    List<String> otherObjectives = new ArrayList<>();
    List<SectorVO> sectors = new ArrayList<>();
    unionPolicyList.add("F-gas Regulation 2006/842/EC");
    unionPolicyList.add("Cogeneration Directive 2004/8/EC");
    objectives.add("Increase in renewable energy sources in the electricity sector");
    objectives.add("Switch to less carbon-intensive fuels");
    otherObjectives.add("1111");
    otherObjectives.add("2222");
    sectorVO.setSectorAffected("4");
    sectorVO.setOtherSectors("");
    sectorVO.setOtherObjectives(otherObjectives);
    sectorVO.setObjectives(objectives);
    sectors.add(sectorVO);
    entity.setName("National government");
    entity.setType("Government");
    entitiesList.add(entity);
    singlePaMs.setId("1");
    singlePaMs.setImplementationPeriodComment("implementationPeriodComment");
    singlePaMs.setImplementationPeriodFinish("2020");
    singlePaMs.setImplementationPeriodStart("2008");
    singlePaMs.setIsPolicyMeasureEnvisaged("No");
    singlePaMs.setProjectionsScenario("With existing measures");
    singlePaMs.setStatusImplementation("Implemented");
    singlePaMs.setUnionPolicyList(unionPolicyList);
    singlePaMs.setSectors(sectors);
    singlePaMs.setEntities(entitiesList);
    singlePaMs.setPolicyImpacting("EU ETS");
    singlesPaMs.add(singlePaMs);
    return singlesPaMs;
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

}
