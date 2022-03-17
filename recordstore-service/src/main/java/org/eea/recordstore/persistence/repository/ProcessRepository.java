package org.eea.recordstore.persistence.repository;

import java.util.Optional;
import org.eea.recordstore.persistence.domain.EEAProcess;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * The Interface ProcessRepository.
 */
public interface ProcessRepository
    extends PagingAndSortingRepository<EEAProcess, Long>, ProcessExtendedRepository {

  /**
   * Find one by process id.
   *
   * @param processId the process id
   * @return the optional
   */
  Optional<EEAProcess> findOneByProcessId(String processId);

  /**
   * Count processes.
   *
   * @return the long
   */
  @Query(nativeQuery = true,
      value = "select count(*) from process where process_type = 'VALIDATION'")
  Long countProcesses();
}
