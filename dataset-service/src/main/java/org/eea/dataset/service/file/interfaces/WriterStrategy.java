package org.eea.dataset.service.file.interfaces;

import java.util.List;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.ExportFilterVO;

/**
 * The Interface WriterStrategy.
 */
public interface WriterStrategy {

  /**
   * Write file.
   *
   * @param dataflowId the dataflow id
   * @param partitionId the partition id
   * @param idTableSchema the id table schema
   * @param includeCountryCode the include country code
   * @param includeValidations the include validations
   * @param filters the filters
   * @return the byte[]
   * @throws EEAException the EEA exception
   */
  byte[] writeFile(Long dataflowId, Long partitionId, String idTableSchema,
      boolean includeCountryCode, boolean includeValidations, ExportFilterVO filters)
      throws EEAException;

  /**
   * Write file list.
   *
   * @param dataflowId the dataflow id
   * @param partitionId the partition id
   * @param includeCountryCode the include country code
   * @param includeValidations the include validations
   * @return the list
   * @throws EEAException the EEA exception
   */
  List<byte[]> writeFileList(Long dataflowId, Long partitionId, boolean includeCountryCode,
      boolean includeValidations) throws EEAException;
}
