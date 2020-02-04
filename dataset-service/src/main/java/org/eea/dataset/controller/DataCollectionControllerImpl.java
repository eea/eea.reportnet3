package org.eea.dataset.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.eea.dataset.service.DataCollectionService;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.dataset.service.DesignDatasetService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.controller.dataset.DataCollectionController;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.eea.interfaces.vo.dataset.DataCollectionVO;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
import org.eea.interfaces.vo.dataset.enums.TypeDatasetEnum;
import org.eea.interfaces.vo.ums.ResourceAssignationVO;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.eea.thread.ThreadPropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
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

  /** The design dataset service. */
  @Autowired
  private DesignDatasetService designDatasetService;

  /** The schema service. */
  @Autowired
  private DatasetSchemaService schemaService;

  @Autowired
  private UserManagementControllerZull userManagementControllerZuul;



  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DataCollectionControllerImpl.class);



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

    // Set the user name on the thread
    ThreadPropertiesManager.setVariable("user",
        SecurityContextHolder.getContext().getAuthentication().getName());

    if (dataCollectionVO.getIdDataflow() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATAFLOW_INCORRECT_ID);
    }

    // 1. Get the design datasets
    List<DesignDatasetVO> designs =
        designDatasetService.getDesignDataSetIdByDataflowId(dataCollectionVO.getIdDataflow());

    // Get the providers who are going to provide data
    List<RepresentativeVO> representatives = representativeControllerZuul
        .findRepresentativesByIdDataFlow(dataCollectionVO.getIdDataflow());
    // 2. Create reporting datasets as many providers are by design dataset
    // only if there are design datasets and providers
    int numberOfDesignsToIterate = designs.size() - 1;
    Boolean schemasIntegrity = true;
    for (DesignDatasetVO design : designs) {
      if (!schemaService.validateSchema(design.getDatasetSchema())) {
        schemasIntegrity = false;
      }
    }
    // Check if the dataflow status is correct, the schema is correct and there are representatives
    // selected
    if (!designs.isEmpty() && !representatives.isEmpty() && schemasIntegrity
        && TypeStatusEnum.DESIGN.equals(
            dataflowControllerZuul.getMetabaseById(dataCollectionVO.getIdDataflow()).getStatus())) {
      try {
        for (DesignDatasetVO design : designs) {

          // Create the DC per design dataset
          datasetMetabaseService.createEmptyDataset(TypeDatasetEnum.COLLECTION,
              "Data Collection" + " - " + design.getDataSetName(), design.getDatasetSchema(),
              dataCollectionVO.getIdDataflow(), dataCollectionVO.getDueDate(), null,
              numberOfDesignsToIterate);

          datasetMetabaseService.createEmptyDataset(TypeDatasetEnum.REPORTING, null,
              design.getDatasetSchema(), dataCollectionVO.getIdDataflow(), null, representatives,
              numberOfDesignsToIterate);
          numberOfDesignsToIterate--;
        }
        // Add the dataflow to the providers
        Set<ResourceAssignationVO> resourcesDataflow = new HashSet<>();
        for (RepresentativeVO representative : representatives) {
          ResourceAssignationVO resourceDFP =
              fillResourceAssignation(dataCollectionVO.getIdDataflow(),
                  representative.getProviderAccount(), ResourceGroupEnum.DATAFLOW_PROVIDER);
          resourcesDataflow.add(resourceDFP);
        }
        userManagementControllerZuul
            .addContributorsToResources(resourcesDataflow.stream().collect(Collectors.toList()));



        // 4. Update the dataflow status to DRAFT
        dataflowControllerZuul.updateDataFlowStatus(dataCollectionVO.getIdDataflow(),
            TypeStatusEnum.DRAFT);
        LOG.info("Dataflow {} changed status to DRAFT", dataCollectionVO.getIdDataflow());
      } catch (EEAException e) {
        LOG_ERROR.error("Error creating a new empty data collection. Error message: {}",
            e.getMessage(), e);
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
            EEAErrorMessage.EXECUTION_ERROR);
      }
    } else {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATA_COLLECTION_NOT_CREATED);
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


  /**
   * Fill resource assignation.
   *
   * @param id the id
   * @param email the email
   * @param group the group
   * @return the resource assignation VO
   */
  private ResourceAssignationVO fillResourceAssignation(Long id, String email,
      ResourceGroupEnum group) {

    ResourceAssignationVO resource = new ResourceAssignationVO();
    resource.setResourceId(id);
    resource.setEmail(email);
    resource.setResourceGroup(group);

    return resource;
  }

}
