package org.eea.ums.service;

import java.io.IOException;
import java.io.InputStream;

/**
 * The Interface BackupManagmentControlerService.
 */
public interface BackupManagmentService {

  /**
   * Read excel data to key cloack.
   *
   * @param is the read excel users
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public void readExcelDatatoKeyCloack(InputStream is) throws IOException;

}
