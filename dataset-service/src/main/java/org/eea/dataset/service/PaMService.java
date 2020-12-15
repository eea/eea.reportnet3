package org.eea.dataset.service;

import java.util.List;
import org.eea.dataset.persistence.data.domain.FieldValue;
import org.eea.interfaces.vo.pams.SinglePaMVO;


/**
 * The Interface PaMsService.
 */
public interface PaMService {

  /**
   * Gets the list single paM.
   *
   * @return the list single paM
   */
  List<SinglePaMVO> getListSinglePaM();


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
  void deleteGroups(List<?> fieldSchemasList, String fieldValuePk);
}
