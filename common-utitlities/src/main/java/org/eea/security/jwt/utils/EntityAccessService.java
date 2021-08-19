package org.eea.security.jwt.utils;

import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.vo.dataflow.enums.TypeDataflowEnum;
import org.eea.interfaces.vo.enums.EntityClassEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;



/**
 * The Class EntityAccessService.
 */
@Service("entityAccessService")
public class EntityAccessService {


  /** The dataflow controller zuul. */
  @Autowired
  private DataFlowControllerZuul dataflowControllerZuul;

  /**
   * Checks if is reference dataflow draft.
   *
   * @param entity the entity
   * @param entityId the entity id
   * @return true, if is reference dataflow draft
   */
  @Cacheable(value = "accessReferenceEntity")
  public boolean isReferenceDataflowDraft(EntityClassEnum entity, Long entityId) {
    return dataflowControllerZuul.accessReferenceEntity(entity, entityId);
  }


  /**
   * Checks if is dataflow type.
   *
   * @param dataflowType the dataflow type
   * @param entity the entity
   * @param entityId the entity id
   * @return true, if is dataflow type
   */
  @Cacheable(value = "accessEntityByDataflowType")
  public boolean isDataflowType(TypeDataflowEnum dataflowType, EntityClassEnum entity,
      Long entityId) {
    return dataflowControllerZuul.accessEntity(dataflowType, entity, entityId);
  }


}
