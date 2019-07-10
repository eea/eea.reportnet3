package org.eea.ums.controller;

import org.eea.interfaces.controller.ums.UserManagementController;
import org.eea.ums.service.SecurityProviderInterfaceService;
import org.springframework.beans.factory.annotation.Autowired;
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
  @RequestMapping(value = "/user/test-security", method = RequestMethod.GET)
  public String testSecuredService() {
    return "OLEEEEE";
  }
}
