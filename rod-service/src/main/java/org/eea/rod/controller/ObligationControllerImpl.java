package org.eea.rod.controller;

import java.util.List;
import javax.ws.rs.QueryParam;
import org.eea.interfaces.controller.rod.ObligationController;
import org.eea.interfaces.vo.rod.ObligationVO;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * The type Obligation controller.
 */
@RestController
@RequestMapping("/obligation")
public class ObligationControllerImpl implements ObligationController {

  @Override
  @RequestMapping(value = "/findOpenened", method = RequestMethod.GET)
  public List<ObligationVO> findOpenedObligations() {
    return null;
  }

  @Override
  @RequestMapping(value = "/{id}", method = RequestMethod.GET)
  public ObligationVO findObligationById(@QueryParam(value = "id") Long id) {
    return null;
  }
}
