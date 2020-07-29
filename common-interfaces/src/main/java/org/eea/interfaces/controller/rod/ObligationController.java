package org.eea.interfaces.controller.rod;

import java.util.List;
import org.eea.interfaces.vo.rod.ObligationVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

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
   * @param clientId the client id
   * @param spatialId the spatial id
   * @param issueId the issue id
   * @param deadlineDateFrom the deadline date from
   * @param deadlineDateTo the deadline date to
   *
   * @return the list
   */
  @GetMapping(value = "/findOpened")
  List<ObligationVO> findOpenedObligations(
      @RequestParam(value = "clientId", required = false) Integer clientId,
      @RequestParam(value = "spatialId", required = false) Integer spatialId,
      @RequestParam(value = "issueId", required = false) Integer issueId,
      @RequestParam(value = "deadlineDateFrom", required = false) Long deadlineDateFrom,
      @RequestParam(value = "deadlineDateTo", required = false) Long deadlineDateTo);

  /**
   * Find obligation by obligation id
   *
   * @param id the obligation id
   *
   * @return the obligation vo
   */
  @GetMapping(value = "/{id}")
  ObligationVO findObligationById(@PathVariable(value = "id") Integer id);
}
