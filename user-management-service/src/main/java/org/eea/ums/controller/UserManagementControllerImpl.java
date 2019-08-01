package org.eea.ums.controller;

import org.eea.interfaces.controller.ums.UserManagementController;
import org.eea.interfaces.vo.ums.enums.AccessScopeEnum;
import org.eea.ums.service.SecurityProviderInterfaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
//@RequestMapping(value = "/user")
public class UserManagementControllerImpl implements UserManagementController {

  @Autowired
  private SecurityProviderInterfaceService securityProviderInterfaceService;

  @Override
  @RequestMapping(value = "/user/generateToken", method = RequestMethod.POST)
  public String generateToken(@RequestParam("username") String username,
      @RequestParam("password") String password) {
    return securityProviderInterfaceService.doLogin(username, password);
  }

  @Override
  @RequestMapping(value = "/user/checkAccess", method = RequestMethod.GET)
  public Boolean checkResourceAccessPermission(@RequestParam("resource") String resource,
      @RequestParam("scopes") AccessScopeEnum[] scopes) {
    return securityProviderInterfaceService.checkAccessPermission(resource, scopes);
  }

  @Override
  @RequestMapping(value = "/user/test-security", method = RequestMethod.GET)
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_REQUESTOR','DATAFLOW_PROVIDER') AND checkPermission('Dataflow','READ')")
  public String testSecuredService(@RequestParam("dataflowId") Long dataflowId) {
    return "OLEEEEE";
  }
}
