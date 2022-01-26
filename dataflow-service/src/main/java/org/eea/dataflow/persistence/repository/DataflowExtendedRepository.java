package org.eea.dataflow.persistence.repository;

import java.util.List;
import java.util.Map;
import org.eea.dataflow.persistence.domain.Dataflow;
import org.springframework.data.domain.Pageable;

/**
 * The Interface DataflowExtendedRepository.
 */
public interface DataflowExtendedRepository {


  /**
   * Find completed.
   *
   * @param userIdRequester the user id requester
   * @param pageable the pageable
   * @return the list
   */
  List<Dataflow> findCompleted(String userIdRequester, Pageable pageable);



  /**
   * Find paginated.
   *
   * @param json the json
   * @param pageable the pageable
   * @param isPublic the is public
   * @param filters the filters
   * @param orderHeader the order header
   * @param asc the asc
   * @return the list
   */
  List<Dataflow> findPaginated(String json, Pageable pageable, boolean isPublic,
      Map<String, String> filters, String orderHeader, boolean asc);



  /**
   * Count paginated.
   *
   * @param json the json
   * @param pageable the pageable
   * @param isPublic the is public
   * @param filters the filters
   * @param orderHeader the order header
   * @param asc the asc
   * @return the long
   */
  Long countPaginated(String json, Pageable pageable, boolean isPublic, Map<String, String> filters,
      String orderHeader, boolean asc);



}
