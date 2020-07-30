package org.eea.rod.persistence.repository;

import java.util.List;
import org.eea.rod.persistence.domain.Country;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;


/**
 * The interface Country feign repository.
 */
@FeignClient(name = "rodCountryInterface", url = "${rod.url}", path = "/rest/country")
public interface CountryFeignRepository {


  /**
   * Find all list.
   *
   * @return the list
   */
  @Cacheable("rod_country_cache")
  @GetMapping(value = "/findAll")
  List<Country> findAll();


}
