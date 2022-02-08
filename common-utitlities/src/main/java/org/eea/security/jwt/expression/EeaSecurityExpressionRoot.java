package org.eea.security.jwt.expression;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.dataflow.enums.TypeDataflowEnum;
import org.eea.interfaces.vo.enums.EntityClassEnum;
import org.eea.interfaces.vo.ums.ResourceAccessVO;
import org.eea.interfaces.vo.ums.enums.AccessScopeEnum;
import org.eea.security.authorization.ObjectAccessRoleEnum;
import org.eea.security.jwt.utils.AuthenticationDetails;
import org.eea.security.jwt.utils.EntityAccessService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;

/**
 * The type Eea security expression root.
 */

@Slf4j
public class EeaSecurityExpressionRoot extends SecurityExpressionRoot
    implements MethodSecurityExpressionOperations {

  /** The return object. */
  private Object returnObject;

  /** The filter object. */
  private Object filterObject;

  /** The user management controller zull. */
  private UserManagementControllerZull userManagementControllerZull;

  /** The entity access service. */
  private EntityAccessService entityAccessService;



  /**
   * Creates a new instance.
   *
   * @param authentication the {@link Authentication} to use. Cannot be null.
   * @param userManagementControllerZull the user management controller zull
   * @param entityAccessService the entity access service
   */
  public EeaSecurityExpressionRoot(Authentication authentication,
      UserManagementControllerZull userManagementControllerZull,
      EntityAccessService entityAccessService) {
    super(authentication);
    this.userManagementControllerZull = userManagementControllerZull;
    this.entityAccessService = entityAccessService;
  }

  /**
   * Second level authorize boolean.
   *
   * @param idEntity the id entity
   * @param objectAccessRoles the object access roles
   *
   * @return the boolean
   */
  public boolean secondLevelAuthorize(Long idEntity, ObjectAccessRoleEnum... objectAccessRoles) {
    return checkAuthorize(idEntity, objectAccessRoles) && !isApiKey();
  }


  /**
   * Second level authorize with api key.
   *
   * @param idEntity the id entity
   * @param objectAccessRoles the object access roles
   * @return true, if successful
   */
  public boolean secondLevelAuthorizeWithApiKey(Long idEntity,
      ObjectAccessRoleEnum... objectAccessRoles) {
    return checkAuthorize(idEntity, objectAccessRoles);
  }

  /**
   * Check authorize.
   *
   * @param idEntity the id entity
   * @param objectAccessRoles the object access roles
   * @return true, if successful
   */
  private boolean checkAuthorize(Long idEntity, ObjectAccessRoleEnum... objectAccessRoles) {
    boolean canAccess = false;
    if (SecurityContextHolder.getContext().getAuthentication().getAuthorities()
        .contains(new SimpleGrantedAuthority("ROLE_FEIGN"))) {
      log.warn("Invocation was made from a feign client with a due token. Letting it go");
      canAccess = true;
    } else {
      log.info("Checking available authorities for user {}",
          SecurityContextHolder.getContext().getAuthentication().getName());
      Collection<String> authorities = SecurityContextHolder.getContext().getAuthentication()
          .getAuthorities().stream().map(authority -> ((GrantedAuthority) authority).getAuthority())
          .collect(Collectors.toList());
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
          resourceAccessVOS = this.userManagementControllerZull.getResourcesByUser();
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
    }
    return canAccess;
  }


  /**
   * Cherck permission boolean.
   *
   * @param resource the resource
   * @param accessScopeEnums the access scope enums
   *
   * @return the boolean
   */
  public boolean checkPermission(String resource, AccessScopeEnum... accessScopeEnums) {
    boolean canAccess = false;
    if (SecurityContextHolder.getContext().getAuthentication().getAuthorities()
        .contains(new SimpleGrantedAuthority("ROLE_FEIGN"))) {
      log.warn("Invocation was made from a feign client with a due token. Letting it go");
      canAccess = true;
    } else {
      log.info("Checking available permissions for user {}",
          SecurityContextHolder.getContext().getAuthentication().getName());
      canAccess =
          userManagementControllerZull.checkResourceAccessPermission(resource, accessScopeEnums);
    }
    return canAccess;
  }


  /**
   * Check access reference entity.
   *
   * @param entity the entity
   * @param entityId the entity id
   * @return true, if successful
   */
  public boolean checkAccessReferenceEntity(EntityClassEnum entity, Long entityId) {
    return entityAccessService.isReferenceDataflowDraft(entity, entityId) && !isApiKey();
  }

  /**
   * Check access entity.
   *
   * @param dataflowType the dataflow type
   * @param entity the entity
   * @param entityId the entity id
   * @return true, if successful
   */
  public boolean checkAccessEntity(TypeDataflowEnum dataflowType, EntityClassEnum entity,
      Long entityId) {
    return entityAccessService.isDataflowType(dataflowType, entity, entityId) && !isApiKey();
  }


  /**
   * Check access super user.
   *
   * @param entity the entity
   * @param entityId the entity id
   * @return true, if successful
   */
  public boolean checkAccessSuperUser(EntityClassEnum entity, Long entityId) {
    return entityAccessService.accessSuperUser(entity, entityId) && !isApiKey();
  }

  /**
   * Check api key boolean.
   *
   * @param dataflowId the dataflow id
   * @param dataProvider the data provider
   * @param idEntity the id entity
   * @param objectAccessRoles the object access roles
   * @return the boolean
   */
  public boolean checkApiKey(final Long dataflowId, final Long dataProvider, Long idEntity,
      ObjectAccessRoleEnum... objectAccessRoles) {
    boolean canAccess = false;
    Object details = SecurityContextHolder.getContext().getAuthentication().getDetails();

    if (details instanceof Map) {
      String userId = ((Map<String, String>) details).get(AuthenticationDetails.USER_ID);
      String apiKey = this.userManagementControllerZull.getApiKey(userId, dataflowId, dataProvider);
      canAccess = StringUtils.isNotBlank(apiKey) && SecurityContextHolder.getContext()
          .getAuthentication().getCredentials().toString().contains(apiKey);
    }
    if (canAccess) {
      canAccess = checkAuthorize(idEntity, objectAccessRoles);
    }

    return canAccess;
  }

  /**
   * Checks if is api key.
   *
   * @return true, if is api key
   */
  public boolean isApiKey() {
    Object details = SecurityContextHolder.getContext().getAuthentication().getDetails();

    if (details instanceof Map) {
      return SecurityContextHolder.getContext().getAuthentication().getCredentials().toString()
          .contains("ApiKey");
    }

    return false;
  }

  /**
   * Sets the filter object.
   *
   * @param filterObject the new filter object
   */
  @Override
  public void setFilterObject(Object filterObject) {
    this.filterObject = filterObject;
  }

  /**
   * Gets the filter object.
   *
   * @return the filter object
   */
  @Override
  public Object getFilterObject() {
    return this.filterObject;
  }

  /**
   * Sets the return object.
   *
   * @param returnObject the new return object
   */
  @Override
  public void setReturnObject(Object returnObject) {
    this.returnObject = returnObject;
  }

  /**
   * Gets the return object.
   *
   * @return the return object
   */
  @Override
  public Object getReturnObject() {
    return this.returnObject;
  }

  /**
   * Gets the this.
   *
   * @return the this
   */
  @Override
  public Object getThis() {
    return this;
  }
}
