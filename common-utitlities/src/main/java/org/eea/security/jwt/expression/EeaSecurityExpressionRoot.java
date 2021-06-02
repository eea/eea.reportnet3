package org.eea.security.jwt.expression;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.ums.ResourceAccessVO;
import org.eea.interfaces.vo.ums.enums.AccessScopeEnum;
import org.eea.security.authorization.ObjectAccessRoleEnum;
import org.eea.security.jwt.utils.AuthenticationDetails;
import org.eea.utils.LiteralConstants;
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

  private Object returnObject;
  private Object filterObject;
  private UserManagementControllerZull userManagementControllerZull;

  /**
   * Creates a new instance
   *
   * @param authentication the {@link Authentication} to use. Cannot be null.
   * @param userManagementControllerZull the user management controller zull
   */
  public EeaSecurityExpressionRoot(Authentication authentication,
      UserManagementControllerZull userManagementControllerZull) {
    super(authentication);
    this.userManagementControllerZull = userManagementControllerZull;
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
    boolean canAccess = checkAuthorize(idEntity, objectAccessRoles);
    if (canAccess) {
      canAccess = !isApiKey();
    }
    return canAccess;
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

      canAccess = !roles.stream().filter(authorities::contains).findFirst()
          .orElse(LiteralConstants.NOT_FOUND).equals(LiteralConstants.NOT_FOUND);
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
        canAccess = !roles.stream().filter(resourceRoles::contains).findFirst()
            .orElse(LiteralConstants.NOT_FOUND).equals(LiteralConstants.NOT_FOUND);
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
   * Check api key boolean.
   *
   * @param dataflowId the dataflow id
   * @param dataProvider the data provider
   *
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

  public boolean isApiKey() {
    Object details = SecurityContextHolder.getContext().getAuthentication().getDetails();

    if (details instanceof Map) {
      return SecurityContextHolder.getContext().getAuthentication().getCredentials().toString()
          .contains("ApiKey");
    }

    return false;
  }

  @Override
  public void setFilterObject(Object filterObject) {
    this.filterObject = filterObject;
  }

  @Override
  public Object getFilterObject() {
    return this.filterObject;
  }

  @Override
  public void setReturnObject(Object returnObject) {
    this.returnObject = returnObject;
  }

  @Override
  public Object getReturnObject() {
    return this.returnObject;
  }

  @Override
  public Object getThis() {
    return this;
  }
}
