package org.eea.recordstore.persistence.repository;

import java.util.Optional;
import org.eea.recordstore.persistence.domain.Process;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * The Interface ProcessRepository.
 */
public interface ProcessRepository
    extends PagingAndSortingRepository<Process, Long>, ProcessExtendedRepository {

  /**
   * Find one by uuid.
   *
   * @param uuid the uuid
   * @return the optional
   */
  Optional<Process> findOneByProcessId(String processId);

  /**
   * Count processes.
   *
   * @return the long
   */
  @Query(nativeQuery = true,
      value = "select count(*) from process where process_type = 'VALIDATION'")
  Long countProcesses();
}
