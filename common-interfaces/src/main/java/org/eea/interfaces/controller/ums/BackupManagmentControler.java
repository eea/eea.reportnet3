package org.eea.interfaces.controller.ums;

import java.io.IOException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

/**
 * The Interface BackupManagmentControler.
 */
public interface BackupManagmentControler {

  /**
   * Sets the users.
   *
   * @param model the model
   * @param file the file
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @RequestMapping(value = "/users", method = RequestMethod.POST)
  void setUsers(MultipartFile file) throws IOException;
}
