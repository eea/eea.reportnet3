package org.eea.dataset.controller;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.eea.dataset.service.DataCollectionService;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DesignDatasetService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.controller.dataset.DataCollectionController;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.eea.interfaces.vo.dataset.DataCollectionVO;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
import org.eea.interfaces.vo.dataset.enums.TypeDatasetEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

/**
 * The Class DataCollectionControllerImpl.
 */
@RestController
@RequestMapping("/datacollection")
public class DataCollectionControllerImpl implements DataCollectionController {

  /** The data collection service. */
  @Autowired
  private DataCollectionService dataCollectionService;

  /** The representative controller zuul. */
  @Autowired
  private RepresentativeControllerZuul representativeControllerZuul;

  /** The dataflow controller zuul. */
  @Autowired
  private DataFlowControllerZuul dataflowControllerZuul;

  /** The dataset metabase service. */
  @Autowired
  private DatasetMetabaseService datasetMetabaseService;

  @Autowired
  private DesignDatasetService designDatasetService;


  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");



  /**
   * Creates the empty data collection.
   *
   * @param dataCollectionVO the data collection VO
   */
  @Override
  @HystrixCommand
  @PostMapping(value = "/create")
  @PreAuthorize("hasRole('DATA_CUSTODIAN')")
  public void createEmptyDataCollection(@RequestBody DataCollectionVO dataCollectionVO) {
    if (StringUtils.isBlank(dataCollectionVO.getDataSetName())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }

    // 1. Get the design datasets
    List<DesignDatasetVO> designs =
        designDatasetService.getDesignDataSetIdByDataflowId(dataCollectionVO.getIdDataflow());

    // Get the providers who are going to provide data
    List<RepresentativeVO> representatives = representativeControllerZuul
        .findRepresentativesByIdDataFlow(dataCollectionVO.getIdDataflow());
    // 2. Create reporting datasets as many providers are by design dataset
    for (DesignDatasetVO design : designs) {
      try {

        for (RepresentativeVO representative : representatives) {
          /*
           * Long newDatasetId =
           * datasetMetabaseService.createEmptyDataset(TypeDatasetEnum.REPORTING, null,
           * dataCollectionVO.getDatasetSchema(), dataCollectionVO.getIdDataflow(), null,
           * representative.getDataProviderId());
           * 
           * // Create the reporting dataset in keycloak and add it to the user provider
           * datasetMetabaseService.createGroupProviderAndAddUser(newDatasetId,
           * representative.getProviderAccount());
           */

          datasetMetabaseService.crearDatasetAsync(TypeDatasetEnum.REPORTING, null,
              dataCollectionVO.getDatasetSchema(), dataCollectionVO.getIdDataflow(), null,
              representative);
        }

        // 3.Create the DC per design dataset
        /*
         * Long newDc = datasetMetabaseService.createEmptyDataset(TypeDatasetEnum.COLLECTION,
         * dataCollectionVO.getDataSetName(), dataCollectionVO.getDatasetSchema(),
         * dataCollectionVO.getIdDataflow(), dataCollectionVO.getDueDate(), null);
         * datasetMetabaseService.createGroupDcAndAddUser(newDc);
         */


        datasetMetabaseService.crearDatasetAsync(TypeDatasetEnum.COLLECTION,
            dataCollectionVO.getDataSetName(), dataCollectionVO.getDatasetSchema(),
            dataCollectionVO.getIdDataflow(), dataCollectionVO.getDueDate(), null);

        // 4. Update the dataflow status to DRAFT
        dataflowControllerZuul.updateDataFlowStatus(dataCollectionVO.getIdDataflow(),
            TypeStatusEnum.DRAFT);


      } catch (EEAException e) {
        LOG_ERROR.error("Error creating a new empty data collection. Error message: {}",
            e.getMessage(), e);
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
            EEAErrorMessage.EXECUTION_ERROR);
      }
    }

  }

  /**
   * Find data collection id by dataflow id.
   *
   * @param idDataflow the id dataflow
   * @return the list
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/dataflow/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<DataCollectionVO> findDataCollectionIdByDataflowId(Long idDataflow) {

    return dataCollectionService.getDataCollectionIdByDataflowId(idDataflow);

  }

}
