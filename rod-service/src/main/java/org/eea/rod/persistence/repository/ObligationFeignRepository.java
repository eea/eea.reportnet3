package org.eea.rod.persistence.repository;

import java.util.List;
import org.eea.rod.persistence.domain.Obligation;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The interface Obligation feign repository.
 */
@FeignClient(name = "rodObligationInterface", url = "${rod.url}", path = "/rest/obligation")
public interface ObligationFeignRepository {

  /**
   * Find opened obligations list.
   *
   * @param clientId the client id
   * @param issueId the issue id
   * @param spatialId the spatial id
   * @param dateFrom the date from
   * @param dateTo the date to
   *
   * @return the list
   */
  @Cacheable("rod_obligation_cache")
  @GetMapping(value = "/findOpened")
  List<Obligation> findOpenedObligations(
      @RequestParam(value = "clientId", required = false) Integer clientId,
      @RequestParam(value = "issueId", required = false) Integer issueId,
      @RequestParam(value = "spatialId", required = false) Integer spatialId,
      @RequestParam(value = "dateFrom", required = false) Long dateFrom,
      @RequestParam(value = "dateTo", required = false) Long dateTo);

  /**
   * Find obligation by obligation id.
   *
   * @param id the obligation id
   * @return the obligation vo
   */
  @Cacheable(value = "rod_single_obligation_cache")
  @GetMapping(value = "/{id}")
  Obligation findObligationById(@PathVariable(value = "id") Integer id);

}
