package org.eea.dataset.controller;

import java.util.List;
import org.eea.dataset.service.PaMService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.PamController;
import org.eea.interfaces.vo.pams.SinglePaMVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * The Class EUDatasetControllerImpl.
 */
@RestController
@RequestMapping("/pam")
public class PamControllerImpl implements PamController {

  /** The pa M service. */
  @Autowired
  private PaMService paMService;

  /**
   * Gets the list single paM.
   *
   * @param datasetId the dataset id
   * @param groupPaMId the group paM id
   * @return the list single paM
   */
  @Override
  @GetMapping("/{datasetId}/getListSinglePaM/{groupPaMId}")
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_STEWARD','DATASCHEMA_STEWARD','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','DATASET_NATIONAL_COORDINATOR','DATASET_CUSTODIAN')")
  public List<SinglePaMVO> getListSinglePaM(@PathVariable("datasetId") Long datasetId,
      @PathVariable("groupPaMId") String groupPaMId) {
    try {
      return paMService.getListSinglePaM(datasetId, groupPaMId);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

}
