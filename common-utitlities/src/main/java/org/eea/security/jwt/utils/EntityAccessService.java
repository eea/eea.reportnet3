package org.eea.security.jwt.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.dataflow.enums.TypeDataflowEnum;
import org.eea.interfaces.vo.enums.EntityClassEnum;
import org.eea.interfaces.vo.ums.ResourceAccessVO;
import org.eea.security.authorization.ObjectAccessRoleEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import feign.FeignException;



/**
 * The Class EntityAccessService.
 */
@Service("entityAccessService")
public class EntityAccessService {


  /** The dataflow controller zuul. */
  @Autowired
  private DataFlowControllerZuul dataflowControllerZuul;


  /** The user management controller zull. */
  @Autowired
  private UserManagementControllerZull userManagementControllerZull;

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


  /**
   * Access super user.
   *
   * @param entity the entity
   * @param entityId the entity id
   * @return true, if successful
   * @throws EEAException
   */
  public boolean accessSuperUser(EntityClassEnum entity, Long entityId) {
    boolean result = false;
    if (EntityClassEnum.DATASET.equals(entity)) {
      List<ObjectAccessRoleEnum> objectAccessRoles = List.of(ObjectAccessRoleEnum.DATASET_STEWARD,
          ObjectAccessRoleEnum.DATASET_CUSTODIAN, ObjectAccessRoleEnum.DATASET_OBSERVER,
          ObjectAccessRoleEnum.DATASET_LEAD_REPORTER, ObjectAccessRoleEnum.DATASET_REPORTER_READ,
          ObjectAccessRoleEnum.DATASET_REPORTER_WRITE,
          ObjectAccessRoleEnum.DATASET_NATIONAL_COORDINATOR,
          ObjectAccessRoleEnum.DATASCHEMA_CUSTODIAN, ObjectAccessRoleEnum.DATASCHEMA_STEWARD,
          ObjectAccessRoleEnum.DATASCHEMA_EDITOR_READ, ObjectAccessRoleEnum.DATASCHEMA_EDITOR_WRITE,
          ObjectAccessRoleEnum.TESTDATASET_CUSTODIAN, ObjectAccessRoleEnum.TESTDATASET_STEWARD,
          ObjectAccessRoleEnum.REFERENCEDATASET_OBSERVER,
          ObjectAccessRoleEnum.REFERENCEDATASET_CUSTODIAN,
          ObjectAccessRoleEnum.REFERENCEDATASET_STEWARD, ObjectAccessRoleEnum.EUDATASET_CUSTODIAN,
          ObjectAccessRoleEnum.EUDATASET_STEWARD, ObjectAccessRoleEnum.DATACOLLECTION_CUSTODIAN,
          ObjectAccessRoleEnum.DATACOLLECTION_STEWARD, ObjectAccessRoleEnum.DATACOLLECTION_OBSERVER,
          ObjectAccessRoleEnum.EUDATASET_OBSERVER,
          ObjectAccessRoleEnum.DATASET_NATIONAL_COORDINATOR,
          ObjectAccessRoleEnum.DATASET_STEWARD_SUPPORT,
          ObjectAccessRoleEnum.EUDATASET_STEWARD_SUPPORT,
          ObjectAccessRoleEnum.DATACOLLECTION_STEWARD_SUPPORT,
          ObjectAccessRoleEnum.REFERENCEDATASET_STEWARD_SUPPORT,
          ObjectAccessRoleEnum.TESTDATASET_STEWARD_SUPPORT);
      result = (canAccess(entityId, objectAccessRoles.toArray(ObjectAccessRoleEnum[]::new))
          || dataflowControllerZuul.accessReferenceEntity(entity, entityId));
    } else {
      result = canAccess(entityId, List.of(ObjectAccessRoleEnum.DATAFLOW_CUSTODIAN,
          ObjectAccessRoleEnum.DATAFLOW_OBSERVER, ObjectAccessRoleEnum.DATAFLOW_STEWARD,
          ObjectAccessRoleEnum.DATAFLOW_NATIONAL_COORDINATOR,
          ObjectAccessRoleEnum.DATAFLOW_LEAD_REPORTER, ObjectAccessRoleEnum.DATAFLOW_REPORTER_WRITE,
          ObjectAccessRoleEnum.DATAFLOW_REPORTER_READ,
          ObjectAccessRoleEnum.DATAFLOW_STEWARD_SUPPORT).toArray(ObjectAccessRoleEnum[]::new))
          || dataflowControllerZuul.accessReferenceEntity(entity, entityId);
    }
    return result;
  }

  /**
   * Can access.
   *
   * @param idEntity the id entity
   * @param objectAccessRoles the object access roles
   * @return true, if successful
   * @throws EEAException
   */
  private boolean canAccess(Long idEntity, ObjectAccessRoleEnum... objectAccessRoles) {
    boolean canAccess = false;
    Collection<String> authorities =
        SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
            .map(authority -> authority.getAuthority()).collect(Collectors.toList());
    List<String> roles = Arrays.asList(objectAccessRoles).stream()
        .map(objectAccessRoleEnum -> objectAccessRoleEnum.getAccessRole(idEntity))
        .collect(Collectors.toList());

    canAccess = roles.stream().anyMatch(authorities::contains);
    // No authority found in the current token. Check against keycloak to find it
    if (!canAccess) {
      // if there were some change at User rights that wasn't be propagated to the
      // token yet
      List<ResourceAccessVO> resourceAccessVOS = null;
      try {
        resourceAccessVOS = userManagementControllerZull.getResourcesByUser();
      } catch (FeignException e) {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
      }
      // ObjectAccessRoleEnum expression has the following formate
      // ROLE_DATASCHEMA-1-DATA_CUSTODIAN
      List<String> resourceRoles = resourceAccessVOS.stream().map(resourceAccessVO -> {
        StringBuilder builder = new StringBuilder("ROLE_");
        return builder.append(resourceAccessVO.getResource().toString()).append("-")
            .append(resourceAccessVO.getId()).append("-").append(resourceAccessVO.getRole())
            .toString().toUpperCase();
      }).collect(Collectors.toList());
      canAccess = roles.stream().anyMatch(resourceRoles::contains);
    }
    return canAccess;
  }

}
