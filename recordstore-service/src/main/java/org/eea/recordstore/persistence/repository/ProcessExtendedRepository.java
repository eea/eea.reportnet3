package org.eea.recordstore.persistence.repository;

import java.util.List;
import org.eea.interfaces.vo.recordstore.enums.ProcessTypeEnum;
import org.eea.recordstore.persistence.domain.EEAProcess;
import org.springframework.data.domain.Pageable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

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
   * @throws JsonMappingException the json mapping exception
   * @throws JsonProcessingException the json processing exception
   */
  List<EEAProcess> getProcessesPaginated(Pageable pageable, boolean asc, String status,
      Long dataflowId, String user, ProcessTypeEnum type, String header)
      throws JsonMappingException, JsonProcessingException;

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

  /**
   * Flush.
   */
  void flush();
}
