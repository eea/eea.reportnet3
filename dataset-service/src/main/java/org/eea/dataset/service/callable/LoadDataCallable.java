package org.eea.dataset.service.callable;

import java.io.InputStream;
import java.util.concurrent.Callable;
import org.eea.dataset.service.file.FileTreatmentHelper;

/**
 * The type Load data callable.
 */
public class LoadDataCallable implements Callable<Void> {

  /** The file name. */
  private final String fileName;

  /** The dataset id. */
  private final Long datasetId;

  /** The is. */
  private final InputStream is;


  /** The id table schema. */
  private final String idTableSchema;

  /** The file treatment helper. */
  private FileTreatmentHelper fileTreatmentHelper;

  /**
   * Instantiates a new Load data callable.
   *
   * @param dataSetId the data set id
   * @param fileName the file
   * @param is the is
   * @param idTableSchema the id table schema
   */
  public LoadDataCallable(final FileTreatmentHelper fileTreatmentHelper, final Long dataSetId,
      final String fileName, InputStream is, final String idTableSchema) {
    this.fileName = fileName;
    this.datasetId = dataSetId;
    this.is = is;
    this.idTableSchema = idTableSchema;
    this.fileTreatmentHelper = fileTreatmentHelper;
  }

  /**
   * Call.
   *
   * @return the void
   * @throws Exception the exception
   */
  @Override
  public Void call() throws Exception {
    fileTreatmentHelper.executeFileProcess(datasetId, fileName, is, idTableSchema);
    return null;
  }
}
