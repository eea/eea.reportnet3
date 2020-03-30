package org.eea.rod.controller;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.eea.interfaces.controller.rod.ObligationController;
import org.eea.interfaces.vo.rod.ObligationVO;
import org.eea.rod.service.ObligationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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
  @RequestMapping(value = "/findOpenened", method = RequestMethod.GET)
  public List<ObligationVO> findOpenedObligations(
      @RequestParam(value = "clientId", required = false) Integer clientId,
      @RequestParam(value = "spatialId", required = false) Integer spatialId,
      @RequestParam(value = "issueId", required = false) Integer issueId,
      @RequestParam(value = "deadlineDateFrom", required = false) Long deadlineDateFrom,
      @RequestParam(value = "deadlineDateTo", required = false) Long deadlineDateTo) {
    Date deadlineFrom = Optional.ofNullable(deadlineDateFrom).map(date -> new Date(date))
        .orElse(null);
    Date deadlineTo = Optional.ofNullable(deadlineDateTo).map(date -> new Date(date)).orElse(null);
    return obligationService.findOpenedObligation();
  }

  @Override
  @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON_VALUE})
  public ObligationVO findObligationById(@PathVariable(value = "id") Integer id) {

    return obligationService.findObligationById(id);
  }
}
