package org.eea.rod.controller;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.eea.interfaces.controller.rod.ObligationController;
import org.eea.interfaces.vo.rod.ObligationVO;
import org.eea.rod.service.ObligationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import springfox.documentation.annotations.ApiIgnore;

/**
 * The type Obligation controller.
 */
@RestController
@ApiIgnore
@RequestMapping("/obligation")
public class ObligationControllerImpl implements ObligationController {

  /** The obligation service. */
  @Autowired
  private ObligationService obligationService;


  /**
   * Find opened obligations.
   *
   * @param clientId the client id
   * @param spatialId the spatial id
   * @param issueId the issue id
   * @param deadlineDateFrom the deadline date from
   * @param deadlineDateTo the deadline date to
   * @return the list
   */
  @Override
  @GetMapping(value = "/findOpened")
  @ApiOperation(value = "Gets a list with all the opened obligations",
      response = ObligationVO.class, responseContainer = "List", hidden = true)
  public List<ObligationVO> findOpenedObligations(
      @ApiParam(value = "Client Id", example = "0") @RequestParam(value = "clientId",
          required = false) Integer clientId,
      @ApiParam(value = "Spatial Id", example = "0") @RequestParam(value = "spatialId",
          required = false) Integer spatialId,
      @ApiParam(value = "Issue Id", example = "0") @RequestParam(value = "issueId",
          required = false) Integer issueId,
      @ApiParam(value = "Deadline date starting from", example = "YYYY-MM-DD",
          required = false) @RequestParam(value = "deadlineDateFrom",
              required = false) Long deadlineDateFrom,
      @ApiParam(value = "Deadline date ending on", example = "YYYY-MM-DD",
          required = false) @RequestParam(value = "deadlineDateTo",
              required = false) Long deadlineDateTo) {
    Date deadlineFrom =
        Optional.ofNullable(deadlineDateFrom).map(date -> new Date(date)).orElse(null);
    Date deadlineTo = Optional.ofNullable(deadlineDateTo).map(date -> new Date(date)).orElse(null);
    return obligationService.findOpenedObligation(clientId, spatialId, issueId, deadlineFrom,
        deadlineTo);
  }

  /**
   * Find obligation by id.
   *
   * @param id the id
   * @return the obligation VO
   */
  @Override
  @GetMapping(value = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
  @ApiOperation(value = "Gets an obligation based on its Id", response = ObligationVO.class,
      hidden = true)
  public ObligationVO findObligationById(@PathVariable(value = "id") Integer id) {

    return obligationService.findObligationById(id);
  }
}
