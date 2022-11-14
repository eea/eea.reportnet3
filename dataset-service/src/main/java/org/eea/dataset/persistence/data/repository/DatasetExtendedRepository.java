package org.eea.dataset.persistence.data.repository;

import org.eea.interfaces.vo.dataset.PgStatActivityVO;

import java.util.List;

/**
 * The Interface DatasetExtendedRepository.
 */
public interface DatasetExtendedRepository {

  /**
   * Delete schema.
   *
   * @param schema the schema
   */
  void deleteSchema(String schema);

  /**
   * Get pg_stat_activity information
   * @return
   */
  List<PgStatActivityVO> getPgStatActivityResults();
}
