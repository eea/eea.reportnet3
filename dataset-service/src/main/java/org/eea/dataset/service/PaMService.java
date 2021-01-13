package org.eea.dataset.service;

import java.util.List;
import org.bson.Document;
import org.eea.dataset.persistence.data.domain.FieldValue;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.pams.SinglePaMVO;


/**
 * The Interface PaMsService.
 */
public interface PaMService {

  /**
   * Gets the list single paM.
   *
   * @param datasetId the dataset id
   * @param groupPaMId the group pa M id
   * @return the list single paM
   * @throws EEAException the EEA exception
   */
  List<SinglePaMVO> getListSinglePaM(Long datasetId, String groupPaMId) throws EEAException;


  /**
   * Update groups.
   *
   * @param idListOfSinglePamsField the id list of single pams field
   * @param fieldValueToUpdate the field value to update
   * @param fieldValueInRecord the field value in record
   */
  void updateGroups(String idListOfSinglePamsField, FieldValue fieldValueToUpdate,
      FieldValue fieldValueInRecord);

  /**
   * Delete groups.
   *
   * @param fieldSchemasList the field schemas list
   * @param fieldValuePk the field value pk
   */
  void deleteGroups(List<Document> fieldSchemasList, String fieldValuePk);
}
