package org.eea.security.jwt.expression;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;

/**
 * The type Second level authorize method security expression handler.
 */
public class SecondLevelAuthorizeMethodSecurityExpressionHandler extends
    DefaultMethodSecurityExpressionHandler {

  private AuthenticationTrustResolver trustResolver =
      new AuthenticationTrustResolverImpl();

  /**
   * Create Security Expression Root
   *
   * @return MethodSecurityExpressionOperations
   */
  @Override
  protected MethodSecurityExpressionOperations createSecurityExpressionRoot(
      Authentication authentication, MethodInvocation invocation) {
    SecondLevelAuthorizationSecurityExpression root =
        new SecondLevelAuthorizationSecurityExpression(authentication);
    root.setPermissionEvaluator(getPermissionEvaluator());
    root.setTrustResolver(this.trustResolver);
    root.setRoleHierarchy(getRoleHierarchy());
    return root;
  }

}
