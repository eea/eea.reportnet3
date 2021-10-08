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
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

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
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_CUSTODIAN','DATASET_STEWARD','DATASET_OBSERVER','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATACOLLECTION_CUSTODIAN','DATASCHEMA_CUSTODIAN','DATASCHEMA_STEWARD','DATASCHEMA_EDITOR_WRITE','DATASCHEMA_EDITOR_READ','DATASET_NATIONAL_COORDINATOR','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','EUDATASET_OBSERVER','DATACOLLECTION_OBSERVER','REFERENCEDATASET_CUSTODIAN','DATACOLLECTION_STEWARD','REFERENCEDATASET_OBSERVER','REFERENCEDATASET_STEWARD','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD') OR (hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') AND checkAccessReferenceEntity('DATASET',#datasetId))")
  @ApiOperation(value = "Get List single PAM", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully get data"),
      @ApiResponse(code = 500, message = "Error getting the data")})
  public List<SinglePaMVO> getListSinglePaM(
      @ApiParam(type = "Long", value = "Dataset Id",
          example = "0") @PathVariable("datasetId") Long datasetId,
      @ApiParam(type = "String", value = "group pam Id",
          example = "0") @PathVariable("groupPaMId") String groupPaMId) {
    try {
      return paMService.getListSinglePaM(datasetId, groupPaMId);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

}
