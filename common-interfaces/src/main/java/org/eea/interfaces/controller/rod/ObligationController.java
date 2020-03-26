package org.eea.interfaces.controller.rod;

import java.util.List;
import javax.ws.rs.QueryParam;
import org.eea.interfaces.vo.rod.ObligationVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * The interface Obligation controller.
 */
public interface ObligationController {

  /**
   * The interface Record store controller zull.
   */
  @FeignClient(value = "rod", path = "/obligation")
  interface ObligationControllerZull extends ObligationController {

  }


  /**
   * Find opened obligations list.
   *
   * @return the list
   */
  @RequestMapping(value = "/findOpenened", method = RequestMethod.GET)
  List<ObligationVO> findOpenedObligations();

  /**
   * Find obligation by obligation id
   *
   * @param id the obligation id
   *
   * @return the obligation vo
   */
  @RequestMapping(value = "/{id}", method = RequestMethod.GET)
  ObligationVO findObligationById(@QueryParam(value = "id") Long id);
}
