package org.eea.dataflow.persistence.repository;

import java.util.List;
import java.util.Map;
import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.interfaces.vo.dataflow.enums.TypeDataflowEnum;
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
   * @param type the type
   * @param dataflowIds the dataflow ids
   * @return the list
   */
  List<Dataflow> findPaginated(String json, Pageable pageable, boolean isPublic,
      Map<String, String> filters, String orderHeader, boolean asc, TypeDataflowEnum type,
      List<Long> dataflowIds);



  /**
   * Count paginated.
   *
   * @param json the json
   * @param pageable the pageable
   * @param isPublic the is public
   * @param filters the filters
   * @param orderHeader the order header
   * @param asc the asc
   * @param type the type
   * @param dataflowIds the dataflow ids
   * @return the long
   */
  Long countPaginated(String json, Pageable pageable, boolean isPublic, Map<String, String> filters,
      String orderHeader, boolean asc, TypeDataflowEnum type, List<Long> dataflowIds);



  /**
   * Find paginated by country.
   *
   * @param obligationJson the obligation json
   * @param pageable the pageable
   * @param filters the filters
   * @param orderHeader the order header
   * @param asc the asc
   * @param countryCode the country code
   * @return the list
   */
  List<Dataflow> findPaginatedByCountry(String obligationJson, Pageable pageable,
      Map<String, String> filters, String orderHeader, boolean asc, String countryCode);



  /**
   * Count paginated by country.
   *
   * @param obligationJson the obligation json
   * @param filters the filters
   * @param orderHeader the order header
   * @param asc the asc
   * @param countryCode the country code
   * @return the long
   */
  Long countByCountry(String obligationJson, Map<String, String> filters, String orderHeader,
      boolean asc, String countryCode);

  /**
   * Count by country filtered.
   *
   * @param obligationJson the obligation json
   * @param filters the filters
   * @param orderHeader the order header
   * @param asc the asc
   * @param countryCode the country code
   * @return the long
   */
  Long countByCountryFiltered(String obligationJson, Map<String, String> filters,
      String orderHeader, boolean asc, String countryCode);



}
