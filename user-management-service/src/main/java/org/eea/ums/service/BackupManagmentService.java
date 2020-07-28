package org.eea.ums.service;

import java.io.IOException;
import java.io.InputStream;

/**
 * The Interface BackupManagmentControlerService.
 */
public interface BackupManagmentService {

  /**
   * Read and safe users.
   *
   * @param is the is
   * @throws IOException Signals that an I/O exception has occurred.
   */
  void readAndSaveUsers(InputStream is) throws IOException;
}
