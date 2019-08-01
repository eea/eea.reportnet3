package org.eea.security.jwt.expression;

import org.aopalliance.intercept.MethodInvocation;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;

/**
 * The type Eea method security expression handler.
 */
public class EeaMethodSecurityExpressionHandler extends
    DefaultMethodSecurityExpressionHandler {

  private UserManagementControllerZull userManagementControllerZull;

  private AuthenticationTrustResolver trustResolver =
      new AuthenticationTrustResolverImpl();

  /**
   * Instantiates a new Eea method security expression handler.
   *
   * @param userManagementControllerZull the user management controller zull
   */
  public EeaMethodSecurityExpressionHandler(
      UserManagementControllerZull userManagementControllerZull) {
    this.userManagementControllerZull = userManagementControllerZull;
  }

  /**
   * Create Security Expression Root
   *
   * @return MethodSecurityExpressionOperations
   */
  @Override
  protected MethodSecurityExpressionOperations createSecurityExpressionRoot(
      Authentication authentication, MethodInvocation invocation) {
    EeaSecurityExpressionRoot root =
        new EeaSecurityExpressionRoot(authentication, userManagementControllerZull);
    root.setPermissionEvaluator(getPermissionEvaluator());
    root.setTrustResolver(this.trustResolver);
    root.setRoleHierarchy(getRoleHierarchy());
    return root;
  }

}
