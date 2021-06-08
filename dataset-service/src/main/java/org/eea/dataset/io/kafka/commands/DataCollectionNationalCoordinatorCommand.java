package org.eea.dataset.io.kafka.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.controller.ums.ResourceManagementController.ResourceManagementControllerZull;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.dataflow.enums.TypeDataflowEnum;
import org.eea.interfaces.vo.ums.ResourceAssignationVO;
import org.eea.interfaces.vo.ums.ResourceInfoVO;
import org.eea.interfaces.vo.ums.UserRepresentationVO;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.eea.interfaces.vo.ums.enums.ResourceTypeEnum;
import org.eea.interfaces.vo.ums.enums.SecurityRoleEnum;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * The Class DataCollectionNationalCoordinatorCommand.
 */
@Component
public class DataCollectionNationalCoordinatorCommand extends AbstractEEAEventHandlerCommand {

  /** The resource management controller zuul. */
  @Autowired
  private ResourceManagementControllerZull resourceManagementControllerZuul;

  /** The user management controller zull. */
  @Autowired
  private UserManagementControllerZull userManagementControllerZull;

  /** The representative controller zuul. */
  @Autowired
  private RepresentativeControllerZuul representativeControllerZuul;


  /** The data set metabase repository. */
  @Autowired
  private DataSetMetabaseRepository dataSetMetabaseRepository;

  /** The kafka sender utils. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /** The dataflow controller zuul. */
  @Autowired
  private DataFlowControllerZuul dataflowControllerZuul;


  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant LOG. */
  private static final Logger LOG =
      LoggerFactory.getLogger(DataCollectionNationalCoordinatorCommand.class);

  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.DATACOLLECTION_NATIONAL_COORDINATOR_EVENT;
  }

  /**
   * Execute.
   *
   * @param eeaEventVO the eea event VO
   * @throws EEAException the EEA exception
   */
  @Override
  public void execute(EEAEventVO eeaEventVO) throws EEAException {
    Long dataflowId = Long.parseLong(String.valueOf(eeaEventVO.getData().get("dataflowId")));


    // we create national coordinator dataflow resource
    resourceManagementControllerZuul.createResource(
        createGroup(dataflowId, ResourceTypeEnum.DATAFLOW, SecurityRoleEnum.NATIONAL_COORDINATOR));

    LOG.info("Dataflow-{}-NATIONAL_COORDINATOR created", dataflowId);

    Map<Long, String> mapRepresentativeProviders = mapRepresentativeProviders(dataflowId);

    // we find all providers in that dataflow to assing the dataset provider
    for (Map.Entry<Long, String> providers : mapRepresentativeProviders.entrySet()) {

      // we find all users who have a national coordinator group, to assing its news dataflow and
      // datasets
      List<ResourceAssignationVO> resourcesForDataProvider = new ArrayList<>();
      String resource =
          ResourceGroupEnum.PROVIDER_NATIONAL_COORDINATOR.getGroupName(providers.getValue());
      List<UserRepresentationVO> users = userManagementControllerZull.getUsersByGroup(resource);
      if (null != users) {
        for (UserRepresentationVO userRepresentationVO : users) {
          // we find all datasets for that user with the provider id to assing it
          List<Long> datasetIds = dataSetMetabaseRepository
              .getDatasetIdsByDataflowIdAndDataProviderId(dataflowId, providers.getKey());

          // and we create all groups and assing all the data for all of them
          for (Long datasetId : datasetIds) {
            resourceManagementControllerZuul.createResource(createGroup(datasetId,
                ResourceTypeEnum.DATASET, SecurityRoleEnum.NATIONAL_COORDINATOR));
            resourcesForDataProvider.add(fillResourceAssignation(datasetId,
                userRepresentationVO.getEmail(), ResourceGroupEnum.DATASET_NATIONAL_COORDINATOR));
          }
          resourcesForDataProvider.add(fillResourceAssignation(dataflowId,
              userRepresentationVO.getEmail(), ResourceGroupEnum.DATAFLOW_NATIONAL_COORDINATOR));
        }

        // finally we add all contributors for this providerid
        userManagementControllerZull.addContributorsToResources(resourcesForDataProvider);

      }
      LOG.info("all national coordinator for provider {} created", providers.getKey());
    }

    // Release the notification to end the process
    EventType successEvent = null;
    Boolean isCreation =
        Boolean.parseBoolean(String.valueOf(eeaEventVO.getData().get("isCreation")));
    DataFlowVO dataflow = dataflowControllerZuul.getMetabaseById(dataflowId);
    if (dataflow != null && !TypeDataflowEnum.REFERENCE.equals(dataflow.getType())) {
      successEvent = Boolean.TRUE.equals(isCreation) ? EventType.ADD_DATACOLLECTION_COMPLETED_EVENT
          : EventType.UPDATE_DATACOLLECTION_COMPLETED_EVENT;
    } else {
      successEvent = EventType.REFERENCE_DATAFLOW_PROCESSED_EVENT;
    }

    try {
      kafkaSenderUtils.releaseNotificableKafkaEvent(successEvent, null,
          NotificationVO.builder()
              .user(SecurityContextHolder.getContext().getAuthentication().getName())
              .dataflowId(dataflowId).build());
    } catch (EEAException e) {
      LOG_ERROR.error("Error releasing {} event: ", successEvent, e);
    }
  }

  /**
   * Map representative providers. to know exactly the number providers to assing it to a national
   * coordinator
   *
   * @param dataflowId the dataflow id
   * @return the map
   */
  private Map<Long, String> mapRepresentativeProviders(Long dataflowId) {
    List<RepresentativeVO> representatives =
        representativeControllerZuul.findRepresentativesByIdDataFlow(dataflowId);

    List<DataProviderVO> dataProviders = new ArrayList<>();
    if (representatives != null && !representatives.isEmpty()) {
      dataProviders = representativeControllerZuul.findDataProvidersByIds(representatives.stream()
          .map(RepresentativeVO::getDataProviderId).collect(Collectors.toList()));
    }

    Map<Long, String> mapRepresentativeProviders = new HashMap<>();
    for (RepresentativeVO representative : representatives) {
      for (DataProviderVO dataProvider : dataProviders) {
        if (dataProvider.getId().equals(representative.getDataProviderId())) {
          mapRepresentativeProviders.put(representative.getDataProviderId(),
              dataProvider.getCode());
          dataProviders.remove(dataProvider);
          break;
        }
      }
    }
    return mapRepresentativeProviders;
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

  /**
   * Creates the group.
   *
   * @param datasetId the dataset id
   * @param type the type
   * @param role the role
   * @return the resource info VO
   */
  private ResourceInfoVO createGroup(Long datasetId, ResourceTypeEnum type, SecurityRoleEnum role) {
    ResourceInfoVO resourceInfoVO = new ResourceInfoVO();
    resourceInfoVO.setResourceId(datasetId);
    resourceInfoVO.setResourceTypeEnum(type);
    resourceInfoVO.setSecurityRoleEnum(role);
    resourceInfoVO.setName(type + "-" + datasetId + "-" + role);
    return resourceInfoVO;
  }
}
