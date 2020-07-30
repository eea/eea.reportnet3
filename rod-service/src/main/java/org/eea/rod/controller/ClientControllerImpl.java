package org.eea.rod.controller;

import java.util.List;
import org.eea.interfaces.controller.rod.ClientController;
import org.eea.interfaces.vo.rod.ClientVO;
import org.eea.rod.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The type Client controller.
 */
@RestController
@RequestMapping("/obligation_client")
public class ClientControllerImpl implements ClientController {

  /** The client service. */
  @Autowired
  private ClientService clientService;

  /**
   * Find all.
   *
   * @return the list
   */
  @Override
  @GetMapping(value = "/")
  public List<ClientVO> findAll() {
    return clientService.findAll();
  }
}
