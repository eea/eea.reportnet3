package org.eea.interfaces.controller.rod;

import java.util.List;
import org.eea.interfaces.vo.rod.ClientVO;
import org.eea.interfaces.vo.rod.IssueVO;
import org.eea.interfaces.vo.rod.ObligationVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


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
  @RequestMapping(value = "/", method = RequestMethod.GET)
  List<ClientVO> findAll();

}
