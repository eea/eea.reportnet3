package org.eea.ums.controller;

import java.io.IOException;
import java.io.InputStream;
import org.eea.interfaces.controller.ums.BackupManagmentControler;
import org.eea.ums.service.BackupManagmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

/**
 * The Class BackupManagmentControlerImpl.
 */
@RestController
@RequestMapping(value = "/users")
public class BackupManagmentControlerImpl implements BackupManagmentControler {

  /** The backup managment controler service. */
  @Autowired
  private BackupManagmentService backupManagmentControlerService;

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * Sets the users.
   *
   * @param model the model
   * @param file the file
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Override
  @HystrixCommand
  @RequestMapping(value = "/readExcelDatatoKeyCloack", method = RequestMethod.POST)
  public void setUsers(MultipartFile file) throws IOException {
    InputStream is = file.getInputStream();
    backupManagmentControlerService.readExcelDatatoKeyCloack(is);

  }

}
