package org.eea.dataset.controller;

import java.util.List;
import org.eea.dataset.service.PaMsService;
import org.eea.interfaces.controller.dataset.PamsController;
import org.eea.interfaces.vo.pams.SinglePaMsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The Class EUDatasetControllerImpl.
 */
@RestController
@RequestMapping("/pams")
public class PamsControllerImpl implements PamsController {

  @Autowired
  PaMsService paMsService;

  @Override
  @GetMapping("/{datasetId}/getSinglePaMs/{paMsId}")
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','DATASET_NATIONAL_COORDINATOR','DATASET_CUSTODIAN')")
  public List<SinglePaMsVO> getSinglesPaMs(@PathVariable("datasetId") Long datasetId,
      @PathVariable("paMsId") String paMsId) {
    return paMsService.getSinglePaMs();
  }

}
