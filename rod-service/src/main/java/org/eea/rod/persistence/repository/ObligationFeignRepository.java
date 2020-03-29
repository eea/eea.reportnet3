package org.eea.rod.persistence.repository;

import java.util.List;
import org.eea.rod.persistence.domain.Obligation;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "rodObligationInterface", url = "${rod.url}", path = "/rest/obligation")
public interface ObligationFeignRepository {

  /**
   * Find opened obligations list.
   *
   * @return the list
   */
  @Cacheable("rod_obligation_cache")
  @RequestMapping(value = "/findOpened", method = RequestMethod.GET)
  List<Obligation> findOpenedObligations();

  /**
   * Find obligation by obligation id
   *
   * @param id the obligation id
   *
   * @return the obligation vo
   */
  @Cacheable(value = "rod_single_obligation_cache")
  @RequestMapping(value = "/{id}", method = RequestMethod.GET)
  Obligation findObligationById(@PathVariable(value = "id") Integer id);

}
