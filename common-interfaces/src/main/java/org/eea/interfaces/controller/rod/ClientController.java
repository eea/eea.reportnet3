package org.eea.interfaces.controller.rod;

import java.util.List;
import org.eea.interfaces.vo.rod.ClientVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;


/**
 * The interface Client controller.
 */
public interface ClientController {

  /**
   * The interface Record store controller zull.
   */
  @FeignClient(value = "rodClient", path = "/obligation_client")
  interface ClientControllerZull extends ClientController {

  }


  /**
   * Find all clients.
   *
   * @return the list
   */
  @GetMapping(value = "/")
  List<ClientVO> findAll();

}
