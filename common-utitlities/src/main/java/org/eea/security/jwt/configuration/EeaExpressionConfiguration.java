package org.eea.security.jwt.configuration;

import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.security.jwt.expression.EeaMethodSecurityExpressionHandler;
import org.eea.security.jwt.utils.EntityAccessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

/**
 * The type Eea expression configuration.
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class EeaExpressionConfiguration extends GlobalMethodSecurityConfiguration {

  /** The user management controller zull. */
  @Autowired
  private UserManagementControllerZull userManagementControllerZull;

  /** The entity access service. */
  @Autowired
  private EntityAccessService entityAccessService;



  /**
   * Creates the expression handler.
   *
   * @return the method security expression handler
   */
  @Override
  protected MethodSecurityExpressionHandler createExpressionHandler() {
    return new EeaMethodSecurityExpressionHandler(userManagementControllerZull,
        entityAccessService);
  }

}
