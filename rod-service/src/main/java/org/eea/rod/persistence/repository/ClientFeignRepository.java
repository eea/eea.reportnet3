package org.eea.rod.persistence.repository;

import java.util.List;
import org.eea.rod.persistence.domain.Client;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * The interface Client feign repository.
 */
@FeignClient(name = "rodClientInterface", url = "${rod.url}", path = "/rest/client")
public interface ClientFeignRepository {

  /**
   * Find all list.
   *
   * @return the list
   */
  @Cacheable("rod_client_cache")
  @GetMapping(value = "/findAll")
  List<Client> findAll();


}
