package org.eea.rod.controller;

import java.util.List;
import org.eea.interfaces.controller.rod.ObligationController;
import org.eea.interfaces.vo.rod.ObligationVO;
import org.eea.rod.service.ObligationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * The type Obligation controller.
 */
@RestController
@RequestMapping("/obligation")
public class ObligationControllerImpl implements ObligationController {

  @Autowired
  private ObligationService obligationService;

  @Override
  @RequestMapping(value = "/findOpened", method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON_VALUE})
  public List<ObligationVO> findOpenedObligations() {

    return obligationService.findOpenedObligation();
  }

  @Override
  @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON_VALUE})
  public ObligationVO findObligationById(@PathVariable(value = "id") Integer id) {

    return obligationService.findObligationById(id);
  }
}
