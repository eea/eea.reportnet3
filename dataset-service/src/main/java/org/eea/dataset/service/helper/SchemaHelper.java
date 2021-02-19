package org.eea.dataset.service.helper;

import java.io.IOException;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.exception.EEAException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * The type Schema helper.
 */
@Component
public class SchemaHelper {

  @Autowired
  private DatasetSchemaService datasetSchemaService;

  /**
   * Import schemas.
   *
   * @param dataflowId the dataflow id
   * @param multipartFile the multipart file
   *
   * @throws IOException the io exception
   * @throws EEAException the eea exception
   */
  //this method has been added mainly to avoid circular reference in datasetSchemaService with Async Executor
  //not using @Lazy in DataschemaServiceImpl because it involves adding in several points where the service is used and can lead to a spaghetti init problem
  @Async
  public void importSchemas(Long dataflowId, MultipartFile multipartFile)
      throws IOException, EEAException {
    datasetSchemaService.importSchemas(dataflowId, multipartFile);
  }

}
