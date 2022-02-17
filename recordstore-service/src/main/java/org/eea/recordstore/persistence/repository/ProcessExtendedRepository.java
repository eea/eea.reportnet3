package org.eea.recordstore.persistence.repository;

import java.util.List;
import org.eea.interfaces.vo.recordstore.enums.ProcessTypeEnum;
import org.eea.recordstore.persistence.domain.Process;
import org.springframework.data.domain.Pageable;

/**
 * The Interface ProcessExtendedRepository.
 */
public interface ProcessExtendedRepository {

  /**
   * Gets the processes paginated.
   *
   * @param pageable the pageable
   * @param asc the asc
   * @param status the status
   * @param dataflowId the dataflow id
   * @param user the user
   * @param type the type
   * @param header the header
   * @return the processes paginated
   */
  List<Process> getProcessesPaginated(Pageable pageable, boolean asc, String status,
      Long dataflowId, String user, ProcessTypeEnum type, String header);

  /**
   * Count processes paginated.
   *
   * @param asc the asc
   * @param status the status
   * @param dataflowId the dataflow id
   * @param user the user
   * @param type the type
   * @param header the header
   * @return the long
   */
  Long countProcessesPaginated(boolean asc, String status, Long dataflowId, String user,
      ProcessTypeEnum type, String header);

}
