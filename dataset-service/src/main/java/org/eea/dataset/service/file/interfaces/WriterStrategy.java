package org.eea.dataset.service.file.interfaces;

/**
 * The Interface ReaderStrategy.
 */
@FunctionalInterface
public interface WriterStrategy {


  /**
   * Write file.
   *
   * @param dataflowId the dataflow id
   * @param partitionId the partition id
   * @param idTableSchema the id table schema
   * @return the data set VO
   */
  byte[] writeFile(Long dataflowId, Long partitionId, String idTableSchema);
}
