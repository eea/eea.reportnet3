package org.eea.security.jwt.expression;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

/**
 * The type Second level authorization configuration.
 */
@Configuration
@EnableGlobalMethodSecurity(
    prePostEnabled = true
)
public class SecondLevelAuthorizationConfiguration extends GlobalMethodSecurityConfiguration {

  @Override
  protected MethodSecurityExpressionHandler createExpressionHandler() {
    SecondLevelAuthorizeMethodSecurityExpressionHandler expressionHandler =
        new SecondLevelAuthorizeMethodSecurityExpressionHandler();
    return expressionHandler;
  }

}
