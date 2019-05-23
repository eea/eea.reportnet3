package org.eea.dataset.service.callable;

import java.io.IOException;
import java.util.concurrent.Callable;
import org.eea.dataset.service.DatasetService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/**
 * The type Load data callable.
 */
public class LoadDataCallable implements Callable<Void> {

  private final DatasetService datasetService;
  private final MultipartFile file;
  private final Long datasetId;

  /**
   * Instantiates a new Load data callable.
   *
   * @param datasetService the dataset service
   * @param dataSetId the data set id
   * @param file the file
   */
  public LoadDataCallable(final DatasetService datasetService, final Long dataSetId,
      final MultipartFile file) {
    this.datasetService = datasetService;
    this.file = file;
    this.datasetId = dataSetId;
  }

  @Override
  public Void call() throws Exception {
    try {
      datasetService.processFile(datasetId, file);
    } catch (final EEAException e) {
      if (e.getMessage().equals(EEAErrorMessage.FILE_FORMAT)
          || e.getMessage().equals(EEAErrorMessage.FILE_EXTENSION)) {
        throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage(), e);
      }
    } catch (final IOException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }

    return null;
  }
}
