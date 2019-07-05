package org.eea.interfaces.controller.ums;

import org.eea.interfaces.controller.recordstore.RecordStoreController;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

public interface UserManagementController {

  @FeignClient(value = "ums", path = "/user")
  interface UserManagementControllerZull extends UserManagementController {

  }

  @RequestMapping(value = "/generateToken", method = RequestMethod.POST)
  String generateToken(@RequestParam("username") String username,
      @RequestParam("password") String password);

  @RequestMapping(value = "/test-security", method = RequestMethod.GET)
  String testSecuredService();
}
