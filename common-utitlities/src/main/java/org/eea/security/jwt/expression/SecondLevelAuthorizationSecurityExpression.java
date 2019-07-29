package org.eea.security.jwt.expression;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.eea.security.authorization.ObjectAccessRoleEnum;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;


public class SecondLevelAuthorizationSecurityExpression extends SecurityExpressionRoot implements
    MethodSecurityExpressionOperations {

  private Object returnObject;
  private Object filterObject;

  /**
   * Instantiates a new Security expression.
   *
   * @param authentication the authentication
   */
  public SecondLevelAuthorizationSecurityExpression(Authentication authentication) {
    super(authentication);
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
    Collection<String> authorities = SecurityContextHolder.getContext()
        .getAuthentication()
        .getAuthorities().stream().map(authority -> ((GrantedAuthority) authority).getAuthority())
        .collect(
            Collectors.toList());
    List<String> roles = Arrays.asList(objectAccessRoles).stream()
        .map(objectAccessRoleEnum -> objectAccessRoleEnum.getAccessRole(idEntity)).collect(
            Collectors.toList());

    return !roles.stream().filter(authorities::contains).findFirst().orElse("not_found")
        .equals("not_found");
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
