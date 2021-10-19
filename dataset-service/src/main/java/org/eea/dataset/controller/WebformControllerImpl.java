package org.eea.dataset.controller;

import java.util.List;
import org.eea.dataset.service.WebformService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.WebformController;
import org.eea.interfaces.vo.dataset.schemas.WebformVO;
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


/**
 * The Class WebformControllerImpl.
 */
@RestController
@RequestMapping("/webform")
public class WebformControllerImpl implements WebformController {


  /** The webform service. */
  @Autowired
  private WebformService webformService;


  /**
   * Gets the list webforms by dataset id.
   *
   * @param datasetId the dataset id
   * @return the list webforms by dataset id
   */
  @Override
  @GetMapping("/{datasetId}/webforms")
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_CUSTODIAN','DATASET_STEWARD','DATASET_OBSERVER','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATACOLLECTION_CUSTODIAN','DATASCHEMA_CUSTODIAN','DATASCHEMA_STEWARD','DATASCHEMA_EDITOR_WRITE','DATASCHEMA_EDITOR_READ','DATASET_NATIONAL_COORDINATOR','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','EUDATASET_OBSERVER','DATACOLLECTION_OBSERVER','REFERENCEDATASET_CUSTODIAN','DATACOLLECTION_STEWARD','REFERENCEDATASET_OBSERVER','REFERENCEDATASET_STEWARD','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD') OR (hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') AND checkAccessReferenceEntity('DATASET',#datasetId))")
  @ApiOperation(value = "Gets all the webforms for a given dataset Id", hidden = false)
  public List<WebformVO> getListWebformsByDatasetId(
      @ApiParam(value = "Dataset Id used to search the webforms.",
          example = "5") @PathVariable("datasetId") Long datasetId) {
    try {
      return webformService.getListWebformsByDatasetId(datasetId);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

}
