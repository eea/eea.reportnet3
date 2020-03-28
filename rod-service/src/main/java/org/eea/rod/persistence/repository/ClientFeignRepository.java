package org.eea.rod.persistence.repository;

import java.util.List;
import org.eea.rod.persistence.domain.Client;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

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
  @RequestMapping(value = "/findAll", method = RequestMethod.GET)
  List<Client> findAll();


}
