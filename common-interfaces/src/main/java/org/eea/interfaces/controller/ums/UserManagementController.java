package org.eea.interfaces.controller.ums;

import org.eea.interfaces.controller.recordstore.RecordStoreController;
import org.eea.interfaces.vo.ums.enums.AccessScopeEnum;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The interface User management controller.
 */
public interface UserManagementController {

  /**
   * The interface User management controller zull.
   */
  @FeignClient(value = "ums", path = "/user")
  interface UserManagementControllerZull extends UserManagementController {

  }

  /**
   * Generate token string.
   *
   * @param username the username
   * @param password the password
   *
   * @return the string
   */
  @RequestMapping(value = "/generateToken", method = RequestMethod.POST)
  String generateToken(@RequestParam("username") String username,
      @RequestParam("password") String password);

  /**
   * Check resource access permission boolean.
   *
   * @param resource the resource
   * @param scopes the scopes
   *
   * @return the boolean
   */
  @RequestMapping(value = "/checkAccess", method = RequestMethod.GET)
  Boolean checkResourceAccessPermission(@RequestParam("resource") String resource,
      @RequestParam("scopes") AccessScopeEnum[] scopes);

  /**
   * Test secured service string.
   *
   * @param dataflowId the dataflow id
   *
   * @return the string
   */
  @RequestMapping(value = "/test-security", method = RequestMethod.GET)
  String testSecuredService(@RequestParam("dataflowId") Long dataflowId);
}
