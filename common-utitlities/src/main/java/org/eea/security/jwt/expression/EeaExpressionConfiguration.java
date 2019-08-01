package org.eea.security.jwt.expression;

import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

/**
 * The type Eea expression configuration.
 */
@Configuration
@EnableGlobalMethodSecurity(
    prePostEnabled = true
)
public class EeaExpressionConfiguration extends GlobalMethodSecurityConfiguration {

  @Autowired
  private UserManagementControllerZull userManagementControllerZull;

  @Override
  protected MethodSecurityExpressionHandler createExpressionHandler() {
    EeaMethodSecurityExpressionHandler expressionHandler =
        new EeaMethodSecurityExpressionHandler(userManagementControllerZull);
    return expressionHandler;
  }

}
