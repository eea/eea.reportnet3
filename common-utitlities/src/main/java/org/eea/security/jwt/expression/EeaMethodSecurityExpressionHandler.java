package org.eea.security.jwt.expression;

import org.aopalliance.intercept.MethodInvocation;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.security.jwt.utils.EntityAccessService;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;

/**
 * The type Eea method security expression handler.
 */
public class EeaMethodSecurityExpressionHandler extends DefaultMethodSecurityExpressionHandler {

  private UserManagementControllerZull userManagementControllerZull;

  private EntityAccessService entityAccessService;

  private AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();

  /**
   * Instantiates a new Eea method security expression handler.
   *
   * @param userManagementControllerZull the user management controller zull
   * @param entityAccessService the entity access service
   */
  public EeaMethodSecurityExpressionHandler(
      UserManagementControllerZull userManagementControllerZull,
      EntityAccessService entityAccessService) {
    this.userManagementControllerZull = userManagementControllerZull;
    this.entityAccessService = entityAccessService;
  }

  /**
   * Create Security Expression Root
   *
   * @return MethodSecurityExpressionOperations
   */
  @Override
  protected MethodSecurityExpressionOperations createSecurityExpressionRoot(
      Authentication authentication, MethodInvocation invocation) {
    EeaSecurityExpressionRoot root = new EeaSecurityExpressionRoot(authentication,
        userManagementControllerZull, entityAccessService);
    root.setPermissionEvaluator(getPermissionEvaluator());
    root.setTrustResolver(this.trustResolver);
    root.setRoleHierarchy(getRoleHierarchy());
    return root;
  }

}
