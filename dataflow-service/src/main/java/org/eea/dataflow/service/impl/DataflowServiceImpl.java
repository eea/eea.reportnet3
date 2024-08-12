package org.eea.dataflow.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.eea.dataflow.mapper.DataflowMapper;
import org.eea.dataflow.mapper.DataflowNoContentMapper;
import org.eea.dataflow.mapper.DataflowPrivateMapper;
import org.eea.dataflow.mapper.DataflowPublicMapper;
import org.eea.dataflow.mapper.RepresentativeMapper;
import org.eea.dataflow.persistence.domain.DataProviderGroup;
import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.dataflow.persistence.domain.DataflowStatusDataset;
import org.eea.dataflow.persistence.domain.FMEUser;
import org.eea.dataflow.persistence.domain.Representative;
import org.eea.dataflow.persistence.domain.TempUser;
import org.eea.dataflow.persistence.repository.ContributorRepository;
import org.eea.dataflow.persistence.repository.DataProviderGroupRepository;
import org.eea.dataflow.persistence.repository.DataflowRepository;
import org.eea.dataflow.persistence.repository.DataflowRepository.IDataflowCount;
import org.eea.dataflow.persistence.repository.DataflowRepository.IDatasetStatus;
import org.eea.dataflow.persistence.repository.FMEUserRepository;
import org.eea.dataflow.persistence.repository.RepresentativeRepository;
import org.eea.dataflow.persistence.repository.TempUserRepository;
import org.eea.dataflow.service.ContributorService;
import org.eea.dataflow.service.DataflowService;
import org.eea.dataflow.service.RepresentativeService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DataCollectionController.DataCollectionControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSchemaController.DatasetSchemaControllerZuul;
import org.eea.interfaces.controller.dataset.EUDatasetController.EUDatasetControllerZuul;
import org.eea.interfaces.controller.dataset.ReferenceDatasetController.ReferenceDatasetControllerZuul;
import org.eea.interfaces.controller.dataset.TestDatasetController.TestDatasetControllerZuul;
import org.eea.interfaces.controller.document.DocumentController.DocumentControllerZuul;
import org.eea.interfaces.controller.rod.ObligationController.ObligationControllerZull;
import org.eea.interfaces.controller.ums.ResourceManagementController.ResourceManagementControllerZull;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataflow.DataflowCountVO;
import org.eea.interfaces.vo.dataflow.DataflowPrivateVO;
import org.eea.interfaces.vo.dataflow.DataflowPublicVO;
import org.eea.interfaces.vo.dataflow.DatasetsSummaryVO;
import org.eea.interfaces.vo.dataflow.PaginatedDataflowVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.dataflow.enums.TypeDataflowEnum;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
import org.eea.interfaces.vo.dataset.ReportingDatasetPublicVO;
import org.eea.interfaces.vo.dataset.enums.DatasetStatusEnum;
import org.eea.interfaces.vo.document.DocumentVO;
import org.eea.interfaces.vo.enums.EntityClassEnum;
import org.eea.interfaces.vo.rod.ObligationVO;
import org.eea.interfaces.vo.ums.DataflowUserRoleVO;
import org.eea.interfaces.vo.ums.ResourceAccessVO;
import org.eea.interfaces.vo.ums.ResourceInfoVO;
import org.eea.interfaces.vo.ums.UserRepresentationVO;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.eea.interfaces.vo.ums.enums.ResourceTypeEnum;
import org.eea.interfaces.vo.ums.enums.SecurityRoleEnum;
import org.eea.interfaces.vo.weblink.WeblinkVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.thread.ThreadPropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;

/**
 * The Class DataflowServiceImpl.
 */
@Service("dataflowService")
public class DataflowServiceImpl implements DataflowService {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(DataflowServiceImpl.class);

  /** The max message length. */
  @Value("${spring.health.db.check.frequency}")
  private int maxMessageLength;

  /** The dataset metabase controller. */
  @Autowired
  private DataSetMetabaseControllerZuul datasetMetabaseControllerZuul;

  /** The user management controller zull. */
  @Autowired
  private UserManagementControllerZull userManagementControllerZull;

  /** The resource management controller zull. */
  @Autowired
  private ResourceManagementControllerZull resourceManagementControllerZull;

  /** The dataset schema controller zuul. */
  @Autowired
  private DatasetSchemaControllerZuul datasetSchemaControllerZuul;

  /** The document controller zuul. */
  @Autowired
  private DocumentControllerZuul documentControllerZuul;

  /** The data collection controller zuul. */
  @Autowired
  private DataCollectionControllerZuul dataCollectionControllerZuul;

  /** The obligation controller. */
  @Autowired
  private ObligationControllerZull obligationControllerZull;

  /** The eu dataset controller zuul. */
  @Autowired
  private EUDatasetControllerZuul euDatasetControllerZuul;

  /** The representative repository. */
  @Autowired
  private RepresentativeRepository representativeRepository;

  /** The temp user repository. */
  @Autowired
  private TempUserRepository tempUserRepository;

  /** The kafka sender utils. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;


  /** The dataflow repository. */
  @Autowired
  private DataflowRepository dataflowRepository;

  /** The contributor repository. */
  @Autowired
  private ContributorRepository contributorRepository;

  /** The dataflow mapper. */
  @Autowired
  private DataflowMapper dataflowMapper;

  /** The dataflow no content mapper. */
  @Autowired
  private DataflowNoContentMapper dataflowNoContentMapper;

  /** The dataflow public mapper. */
  @Autowired
  private DataflowPublicMapper dataflowPublicMapper;

  /** The representative service. */
  @Autowired
  private RepresentativeService representativeService;

  /** The contributor service. */
  @Autowired
  private ContributorService contributorService;

  /** The dataset controller zuul. */
  @Autowired
  private DataSetControllerZuul dataSetControllerZuul;

  /** The dataset Test controller zuul. */
  @Autowired
  private TestDatasetControllerZuul testDataSetControllerZuul;

  /** The reference dataset controller zuul. */
  @Autowired
  private ReferenceDatasetControllerZuul referenceDatasetControllerZuul;

  /** The data provider group repository. */
  @Autowired
  private DataProviderGroupRepository dataProviderGroupRepository;

  /** The fme user repository. */
  @Autowired
  private FMEUserRepository fmeUserRepository;

  /** The dataflow private mapper. */
  @Autowired
  private DataflowPrivateMapper dataflowPrivateMapper;

  /** The representative mapper. */
  @Autowired
  private RepresentativeMapper representativeMapper;


  /**
   * Gets the by id.
   *
   * @param id the id
   * @return the by id
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public DataFlowVO getById(Long id, boolean removeWeblinksAndDocuments) throws EEAException {
    DataFlowVO result = getByIdWithCondition(id, true, null);
    if (removeWeblinksAndDocuments) {
      removeWebLinksAndDocuments(result);
    }
    return result;
  }

  /**
   * Gets the name by id.
   *
   * @param id the id
   * @return the name
   * @throws EEAException the EEA exception
   */
  @Override
  public String getDataflowNameById(Long id) {
    Optional<Dataflow> dataflowVO = dataflowRepository.findById(id);
    if(dataflowVO.isPresent()){
      return dataflowVO.get().getName();
    }
    return null;
  }

  /**
   * Gets the by id with representatives filtered by user email.
   *
   * @param id the id
   * @param providerId the provider id
   * @return the by id with representatives filtered by user email
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public DataFlowVO getByIdWithRepresentativesFilteredByUserEmail(Long id, Long providerId)
      throws EEAException {
    DataFlowVO result = getByIdWithCondition(id, false, providerId);
    removeWebLinksAndDocuments(result);
    return result;
  }



  /**
   * Gets the by status.
   *
   * @param status the status
   * @return the by status
   * @throws EEAException the EEA exception
   */
  @Override
  public List<DataFlowVO> getByStatus(TypeStatusEnum status) throws EEAException {
    List<Dataflow> dataflows = dataflowRepository.findByStatus(status);
    return dataflowMapper.entityListToClass(dataflows);
  }

  private List<ResourceAccessVO> removeResourcesIfNeeded(List<ResourceAccessVO> resources){
    resources.removeIf(resourceVO -> {
              SecurityRoleEnum role = resourceVO.getRole();
              return role != null
                      && role.equals(SecurityRoleEnum.NATIONAL_COORDINATOR)
                      && !dataflowRepository.getPublicInfoByDataflowId(resourceVO.getId());
            }
      );
    return resources;
  }

  /**
   * Gets the dataflows.
   *
   * @param userId the user id
   * @param dataflowType the dataflow type
   * @return the dataflows
   * @throws EEAException the EEA exception
   */
  @Override
  public PaginatedDataflowVO getDataflows(String userId, TypeDataflowEnum dataflowType,
      Map<String, String> filters, String orderHeader, boolean asc, Integer pageSize,
      Integer pageNum) throws EEAException {
    try {
      List<DataFlowVO> dataflowVOs = new ArrayList<>();
      PaginatedDataflowVO paginatedDataflowVO = new PaginatedDataflowVO();

      // get obligations and pageable
      List<ObligationVO> obligations = obligationControllerZull
          .findOpenedObligations(null, null, null, null, null).getObligations();
      ObjectMapper objectMapper = new ObjectMapper();
      String arrayToJson = objectMapper.writeValueAsString(obligations);
      Pageable pageable = null;
      if (null != pageNum && null != pageSize) {
        pageable = PageRequest.of(pageNum, pageSize);
      }

      // Get user's datasets
      Map<Long, List<DataflowStatusDataset>> map = getDatasetsStatusByUser();
      boolean userAdmin = isAdmin();
      List<Long> idsResources = null;
      List<Long> idsResourcesWithoutRole = null;
      if (MapUtils.isNotEmpty(filters) && filters.containsKey("role")) {
        List<ResourceAccessVO> resourcesByUser = userManagementControllerZull.getResourcesByUser(ResourceTypeEnum.DATAFLOW,
                SecurityRoleEnum.fromValue(filters.get("role")));
        resourcesByUser = removeResourcesIfNeeded(resourcesByUser);
        idsResources = resourcesByUser.stream().map(ResourceAccessVO::getId).collect(Collectors.toList());

        List<ResourceAccessVO> resourcesByUserWithoutRole = userManagementControllerZull.getResourcesByUser(ResourceTypeEnum.DATAFLOW);
        resourcesByUserWithoutRole = removeResourcesIfNeeded(resourcesByUserWithoutRole);
        idsResourcesWithoutRole = resourcesByUserWithoutRole.stream().map(ResourceAccessVO::getId).collect(Collectors.toList());
        filters.remove("role");
      } else {
        List<ResourceAccessVO> resourcesByUser = userManagementControllerZull.getResourcesByUser(ResourceTypeEnum.DATAFLOW);
        resourcesByUser = removeResourcesIfNeeded(resourcesByUser);
        idsResources = resourcesByUser.stream().map(ResourceAccessVO::getId).collect(Collectors.toList());
      }

      Map<String, List<String>> attributes = userManagementControllerZull.getUserAttributes();
      List<String> pinnedDataflows = new ArrayList<>();
      if (MapUtils.isNotEmpty(attributes) && attributes.containsKey("pinnedDataflows")) {
        pinnedDataflows.addAll(attributes.get("pinnedDataflows"));
      }

      // Get user's dataflows sorted by status and creation date
      if (CollectionUtils.isNotEmpty(idsResources) || userAdmin
          || dataflowType == TypeDataflowEnum.REFERENCE) {
        List<Dataflow> dataflows = new ArrayList<>();
        List<Long> dataflowIdsInput = userAdmin ? null : idsResources;
        List<Long> dataflowIdsInput2 = userAdmin ? null : idsResourcesWithoutRole != null ? idsResourcesWithoutRole : idsResources;

        if (CollectionUtils.isNotEmpty(idsResources) || userAdmin) {
          dataflows = dataflowRepository.findPaginated(arrayToJson, pageable, false, filters,
                  orderHeader, asc, dataflowType, dataflowIdsInput, pinnedDataflows);
          paginatedDataflowVO.setFilteredRecords(
                  dataflowRepository.countPaginated(arrayToJson, pageable, false, filters,
                          orderHeader, asc, dataflowType, dataflowIdsInput, pinnedDataflows));
        } else {
          paginatedDataflowVO.setFilteredRecords(0L);
        }

        paginatedDataflowVO.setTotalRecords(
                dataflowRepository.countPaginated(arrayToJson, null, false, null, null,
                        asc, dataflowType, dataflowIdsInput2, pinnedDataflows));

        dataflows.forEach(dataflow -> {
          DataFlowVO dataflowVO = dataflowNoContentMapper.entityToClass(dataflow);
          List<DataflowStatusDataset> datasetsStatusList = map.get(dataflowVO.getId());
          if (!map.isEmpty() && null != datasetsStatusList) {
            setReportingDatasetStatus(datasetsStatusList, dataflowVO);
          }
          dataflowVOs.add(dataflowVO);
        });

        // SET OBLIGATIONS
        if (!TypeDataflowEnum.REFERENCE.equals(dataflowType)) {
          for (DataFlowVO dataflowVO : dataflowVOs) {
            for (ObligationVO obligation : obligations) {
              if (dataflowVO.getObligation().getObligationId()
                  .equals(obligation.getObligationId())) {
                dataflowVO.setObligation(obligation);
              }
            }
          }
        }
      } else {
        paginatedDataflowVO.setFilteredRecords(Long.valueOf(0));
        if (idsResourcesWithoutRole != null) {
          paginatedDataflowVO.setTotalRecords(userAdmin
              ? dataflowRepository.countPaginated(arrayToJson, null, Boolean.FALSE, null, null, asc,
                  dataflowType, null, pinnedDataflows)
              : dataflowRepository.countPaginated(arrayToJson, null, Boolean.FALSE, null, null, asc,
                  dataflowType, idsResourcesWithoutRole, pinnedDataflows));
        } else {
          paginatedDataflowVO.setTotalRecords(Long.valueOf(0));
        }
      }
      paginatedDataflowVO.setDataflows(dataflowVOs);

      return paginatedDataflowVO;
    } catch (Exception e) {
      LOG.error(
          "Error retrieving dataflows {} due to reason {}", userId,
          e.getMessage(), e);
      throw new EEAException(EEAErrorMessage.DATAFLOW_GET_ERROR);
    }
  }

  /**
   * Gets the cloneable dataflows.
   *
   * @param userId the user id
   * @return the cloneable dataflows
   * @throws EEAException the EEA exception
   */
  @Override
  public List<DataFlowVO> getCloneableDataflows(String userId) throws EEAException {

    List<DataFlowVO> dataflowVOs = new ArrayList<>();

    // Get user's datasets
    Map<Long, List<DataflowStatusDataset>> map = getDatasetsStatusByUser();
    boolean userAdmin = isAdmin();

    // Get user's dataflows sorted by status and creation date
    List<Long> idsResources =
        userManagementControllerZull.getResourcesByUser(ResourceTypeEnum.DATAFLOW).stream()
            .filter(resource -> SecurityRoleEnum.DATA_STEWARD.equals(resource.getRole())
                || SecurityRoleEnum.DATA_CUSTODIAN.equals(resource.getRole()))
            .map(ResourceAccessVO::getId).collect(Collectors.toList());
    if (null != idsResources && !idsResources.isEmpty() || userAdmin) {
      List<Dataflow> dataflows = new ArrayList<>();

      // All dataflows except REFERENCE type dataflow
      dataflows = userAdmin
          ? dataflowRepository.findDataflowsExceptReferenceInOrderByStatusDescCreationDateDesc()
          : dataflowRepository
              .findDataflowsExceptReferenceAndIdInOrderByStatusDescCreationDateDesc(idsResources);

      dataflows.forEach(dataflow -> {
        DataFlowVO dataflowVO = dataflowNoContentMapper.entityToClass(dataflow);
        List<DataflowStatusDataset> datasetsStatusList = map.get(dataflowVO.getId());
        if (!map.isEmpty() && null != datasetsStatusList) {
          setReportingDatasetStatus(datasetsStatusList, dataflowVO);
        }
        dataflowVOs.add(dataflowVO);
      });
      try {
        getOpenedObligations(dataflowVOs);
      } catch (FeignException e) {
        LOG.error(
            "Error retrieving obligations for dataflows from user id {} due to reason {}", userId,
            e.getMessage(), e);
      }
    }

    return dataflowVOs;
  }



  /**
   * Gets the completed.
   *
   * @param userId the user id
   * @param pageable the pageable
   * @return the completed
   * @throws EEAException the EEA exception
   */
  @Override
  public List<DataFlowVO> getCompleted(String userId, Pageable pageable) throws EEAException {

    List<Dataflow> dataflows = dataflowRepository.findCompleted(userId, pageable);
    List<DataFlowVO> dataflowVOs = new ArrayList<>();
    if (!dataflows.isEmpty()) {
      dataflowVOs = dataflowNoContentMapper.entityListToClass(dataflows);
    }
    LOG.info("Get the dataflows completed of the user id: {}. {} per page, current page {}", userId,
        pageable.getPageSize(), pageable.getPageNumber());

    return dataflowVOs;
  }

  /**
   * Creates the data flow.
   *
   * @param dataflowVO the dataflow VO
   * @return the long
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public Long createDataFlow(DataFlowVO dataflowVO) throws EEAException {
    Dataflow dataFlowSaved;
    // we find if the name of this dataflow exist
    if (dataflowRepository.findByNameIgnoreCase(dataflowVO.getName()).isPresent()) {
      LOG.info("The dataflow: {} already exists.", dataflowVO.getName());
      throw new EEAException(EEAErrorMessage.DATAFLOW_EXISTS_NAME);
    }
    // if type comes null, set the default (REPORTING) type to the dataflow
    if (null == dataflowVO.getType()) {
      dataflowVO.setType(TypeDataflowEnum.REPORTING);
    }
    if (TypeDataflowEnum.BUSINESS.equals(dataflowVO.getType())) {
      if (!dataProviderGroupRepository.existsById(dataflowVO.getDataProviderGroupId())) {
        LOG.info("The company group : {} don't exists.", dataflowVO.getDataProviderGroupId());
        throw new EEAException(EEAErrorMessage.COMPANY_GROUP_NOTFOUND);
      }
      if (!fmeUserRepository.existsById(dataflowVO.getFmeUserId())) {
        LOG.info("The User fme: {} don't exists.", dataflowVO.getFmeUserId());
        throw new EEAException(EEAErrorMessage.USERFME_NOTFOUND);
      }
    }
    if(dataflowVO.getBigData() == null){
      dataflowVO.setBigData(false);
    }
    dataflowVO.setCreationDate(new Date());
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    dataflowVO.setReleasable(true);
    dataFlowSaved = dataflowRepository.save(dataflowMapper.classToEntity(dataflowVO));
    LOG.info("The dataflow {} has been created.", dataFlowSaved.getName());

    // With that method we create the group in keycloack
    resourceManagementControllerZull.createResource(createGroup(dataFlowSaved.getId(),
        ResourceTypeEnum.DATAFLOW, SecurityRoleEnum.DATA_CUSTODIAN));

    resourceManagementControllerZull.createResource(createGroup(dataFlowSaved.getId(),
        ResourceTypeEnum.DATAFLOW, SecurityRoleEnum.LEAD_REPORTER));

    resourceManagementControllerZull.createResource(createGroup(dataFlowSaved.getId(),
        ResourceTypeEnum.DATAFLOW, SecurityRoleEnum.EDITOR_READ));

    resourceManagementControllerZull.createResource(createGroup(dataFlowSaved.getId(),
        ResourceTypeEnum.DATAFLOW, SecurityRoleEnum.EDITOR_WRITE));
    if (dataflowVO.getType() != TypeDataflowEnum.BUSINESS) {
      userManagementControllerZull.addUserToResource(dataFlowSaved.getId(),
          ResourceGroupEnum.DATAFLOW_CUSTODIAN);
    }
    return dataFlowSaved.getId();
  }

  /**
   * Update data flow.
   *
   * @param dataflowVO the dataflow VO
   * @throws EEAException the EEA exception
   */
  @Override
  @CacheEvict(value = "dataflowVO", key = "#dataflowVO.id")
  public void updateDataFlow(DataFlowVO dataflowVO) throws EEAException {

    Optional<Dataflow> dataflow = dataflowRepository.findByNameIgnoreCase(dataflowVO.getName());
    // we find if the name of this dataflow exist
    if (dataflow.isPresent() && !dataflow.get().getId().equals(dataflowVO.getId())) {
      LOG.info("The dataflow: {} already exists.", dataflowVO.getName());
      throw new EEAException(EEAErrorMessage.DATAFLOW_EXISTS_NAME);
    } else {
      Optional<Dataflow> dataflowSave = dataflowRepository.findById(dataflowVO.getId());
      if (dataflowSave.isPresent()) {
        dataflowSave.get().setName(dataflowVO.getName());
        if (!StringUtils.isBlank(dataflowVO.getDescription())) {
          dataflowSave.get().setDescription(dataflowVO.getDescription());
        }
        if (null != dataflowVO.getObligation()) {
          dataflowSave.get().setObligationId(dataflowVO.getObligation().getObligationId());
        }
        dataflowSave.get().setReleasable(dataflowVO.isReleasable());
        dataflowSave.get().setShowPublicInfo(dataflowVO.isShowPublicInfo());
        if (null != dataflowVO.getFmeUserId()) {
          dataflowSave.get().setFmeUserId(dataflowVO.getFmeUserId());
        }
        if (null != dataflowVO.getDataProviderGroupId()) {
          dataflowSave.get().setDataProviderGroupId(dataflowVO.getDataProviderGroupId());
        }
        dataflowRepository.save(dataflowSave.get());
        LOG.info("The dataflow {} has been updated.", dataflowSave.get().getName());
      }
    }
  }

  /**
   * Gets the reporting datasets id.
   *
   * @param dataschemaId the dataschema id
   * @return the reporting datasets id
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public DataFlowVO getReportingDatasetsId(String dataschemaId) throws EEAException {

    if (dataschemaId == null) {
      throw new EEAException(EEAErrorMessage.SCHEMA_NOT_FOUND);
    }

    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setReportingDatasets(
        datasetMetabaseControllerZuul.getReportingsIdBySchemaId(dataschemaId));

    return dataflowVO;
  }

  /**
   * Gets the metabase by id.
   *
   * @param id the id
   * @return the metabase by id
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  @Cacheable(value = "dataflowVO", key = "#id")
  public DataFlowVO getMetabaseById(Long id) throws EEAException {

    if (id == null) {
      throw new EEAException(EEAErrorMessage.DATAFLOW_NOTFOUND);
    }
    Dataflow result = dataflowRepository.findById(id).orElse(null);
    DataFlowVO dataflowVO = dataflowNoContentMapper.entityToClass(result);

    LOG.info("Get the dataflow metabase with id {}", id);

    return dataflowVO;
  }

  /**
   * Delete data flow.
   *
   * @param idDataflow the id dataflow
   */
  @Override
  @Transactional
  @Async
  public void deleteDataFlow(Long idDataflow) {
    // take the jpa entity

    try {
      DataFlowVO dataflowVO = getById(idDataflow, false);

      // use it to take all datasets Design


      LOG.info("Get the dataflow metabase with id {}", idDataflow);

      // // PART DELETE DOCUMENTS
      if (null != dataflowVO.getDocuments() && !dataflowVO.getDocuments().isEmpty()) {
        deleteDocuments(idDataflow, dataflowVO);
        LOG.info("Deleted documents for dataflowId: {}", idDataflow);
      }

      // PART OF DELETE ALL THE DATASETSCHEMA we have in the dataflow
      if (null != dataflowVO.getDesignDatasets() && !dataflowVO.getDesignDatasets().isEmpty()) {
        deleteDatasetSchemas(idDataflow, dataflowVO);
        LOG.info("Deleted dataflow schemas with dataflowId: {}", idDataflow);
      }

      // PART OF DELETE ALL THE REPRESENTATIVE we have in the dataflow
      if (null != dataflowVO.getRepresentatives() && !dataflowVO.getRepresentatives().isEmpty()) {
        deleteRepresentatives(dataflowVO);
        LOG.info("Deleted representatives for dataflowId: {}", idDataflow);
      }

      // Delete the dataflow metabase info. Also by the foreign keys of the database, entities
      // like weblinks are also deleted
      deleteDataflowMetabaseInfo(idDataflow);
      LOG.info("Deleted metabase info for dataflowId: {}", idDataflow);

      // add resource to delete(DATAFLOW PART)
      deleteDataflowResources(idDataflow);
      LOG.info("Deleted dataflow resources for dataflowId: {}", idDataflow);

      NotificationVO notificationVO = NotificationVO.builder().dataflowId(idDataflow)
          .user(SecurityContextHolder.getContext().getAuthentication().getName()).build();

      LOG.info("Successfully deleted dataflow with id {}", idDataflow);

      try {
        kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.DELETE_DATAFLOW_COMPLETED_EVENT,
            null, notificationVO);
      } catch (EEAException e) {
        LOG.error(
            "Failed sending kafka delete dataflow completed event notification. DataflowId: {}",
            idDataflow);
      }

    } catch (Exception e) {
      NotificationVO notificationVO = NotificationVO.builder().dataflowId(idDataflow)
          .user(SecurityContextHolder.getContext().getAuthentication().getName()).build();
      try {
        kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.DELETE_DATAFLOW_FAILED_EVENT, null,
            notificationVO);
        LOG.error("Error deleting dataflow with id: {}", idDataflow);
        throw new EEAException(String.format("Error deleting dataflow with id: %s", idDataflow), e);
      } catch (EEAException e1) {
        LOG.error("Failed sending kafka delete dataflow failed event notification. DataflowId: {}",
            idDataflow);
      }
    }
  }

  /**
   * Update data flow status.
   *
   * @param id the id
   * @param status the status
   * @param deadlineDate the deadline date
   * @throws EEAException the EEA exception
   */
  @Override
  @CacheEvict(value = "dataflowVO", key = "#id")
  @Transactional
  public void updateDataFlowStatus(Long id, TypeStatusEnum status, Date deadlineDate)
      throws EEAException {
    Optional<Dataflow> dataflow = dataflowRepository.findById(id);
    if (dataflow.isPresent()) {
      dataflow.get().setStatus(status);
      dataflow.get().setDeadlineDate(deadlineDate);
      dataflowRepository.save(dataflow.get());
      LOG.info("The dataflow {} has been saved.", dataflow.get().getName());
    } else {
      throw new EEAException(EEAErrorMessage.DATAFLOW_NOTFOUND);
    }
  }

  /**
   * Gets the public dataflows.
   *
   * @param filters the filters
   * @param orderHeader the order header
   * @param asc the asc
   * @param pageSize the page size
   * @param pageNum the page num
   * @return the public dataflows
   * @throws EEAException the EEA exception
   */
  @Override
  public PaginatedDataflowVO getPublicDataflows(Map<String, String> filters, String orderHeader,
      boolean asc, Integer pageSize, Integer pageNum) throws EEAException {

    // get obligations
    try {
      Pageable pageable = null;
      if (null != pageNum && null != pageSize) {
        pageable = PageRequest.of(pageNum, pageSize);
      }
      List<ObligationVO> obligations = obligationControllerZull
          .findOpenedObligations(null, null, null, null, null).getObligations();
      ObjectMapper objectMapper = new ObjectMapper();

      String arrayToJson = objectMapper.writeValueAsString(obligations);

      List<Dataflow> dataflows = dataflowRepository.findPaginated(arrayToJson, pageable,
          Boolean.TRUE, filters, orderHeader, asc, null, null, null);
      List<DataflowPublicVO> dfpublic = dataflowPublicMapper.entityListToClass(dataflows);

      // SET OBLIGATIONS
      for (DataflowPublicVO dataflowPublicVO : dfpublic) {
        for (ObligationVO obligation : obligations) {
          if (dataflowPublicVO.getObligation().getObligationId()
              .equals(obligation.getObligationId())) {
            dataflowPublicVO.setObligation(obligation);
          }
        }
      }
      PaginatedDataflowVO pag = new PaginatedDataflowVO();
      pag.setDataflows(dfpublic);
      pag.setTotalRecords(dataflowRepository.countByShowPublicInfo(Boolean.TRUE));
      pag.setFilteredRecords(dataflowRepository.countPaginated(arrayToJson, pageable, Boolean.TRUE,
          filters, orderHeader, asc, null, null, null));

      return pag;

    } catch (Exception e) {
      throw new EEAException(EEAErrorMessage.DATAFLOW_GET_ERROR);
    }
  }

  /**
   * Gets the public dataflows by country.
   *
   * @param countryCode the country code
   * @param header the header
   * @param asc the asc
   * @param page the page
   * @param pageSize the page size
   * @param filters the filters
   * @return the public dataflows by country
   * @throws EEAException the EEA exception
   */
  @Override
  public PaginatedDataflowVO getPublicDataflowsByCountry(String countryCode, String header,
      boolean asc, int page, int pageSize, Map<String, String> filters) throws EEAException {

    try {
      Pageable pageable = PageRequest.of(page, pageSize);
      List<ObligationVO> obligations = obligationControllerZull
          .findOpenedObligations(null, null, null, null, null).getObligations();
      ObjectMapper objectMapper = new ObjectMapper();

      String obligationJson = objectMapper.writeValueAsString(obligations);

      List<Dataflow> publicDataflows = dataflowRepository.findPaginatedByCountry(obligationJson,
          pageable, filters, header, asc, countryCode);

      List<DataflowPublicVO> publicDataflowsVOList =
          dataflowPublicMapper.entityListToClass(publicDataflows);

      // SET OBLIGATIONS
      for (DataflowPublicVO dataflowPublicVO : publicDataflowsVOList) {
        for (ObligationVO obligation : obligations) {
          if (dataflowPublicVO.getObligation().getObligationId()
              .equals(obligation.getObligationId())) {
            dataflowPublicVO.setObligation(obligation);
          }
        }
      }

      List<DataProviderVO> providerId = representativeService.findDataProvidersByCode(countryCode);
      setReportings(publicDataflowsVOList, providerId);

      publicDataflowsVOList.stream().forEach(dataflow -> {
        dataflow.setReferenceDatasets(referenceDatasetControllerZuul
            .findReferenceDataSetPublicByDataflowId(dataflow.getId()));
      });

      PaginatedDataflowVO dataflowPublicPaginated = new PaginatedDataflowVO();

      dataflowPublicPaginated.setDataflows(publicDataflowsVOList);
      dataflowPublicPaginated.setTotalRecords(
          dataflowRepository.countByCountry(obligationJson, filters, header, asc, countryCode));
      dataflowPublicPaginated.setFilteredRecords(dataflowRepository
          .countByCountryFiltered(obligationJson, filters, header, asc, countryCode));

      return dataflowPublicPaginated;


    } catch (JsonProcessingException e) {
      throw new EEAException(EEAErrorMessage.DATAFLOW_GET_ERROR, e);
    }
  }


  /**
   * Gets the user roles.
   *
   * @param dataProviderId the data provider id
   * @param dataflowList the dataflow list
   * @return the user roles
   */
  @Override
  public List<DataflowUserRoleVO> getUserRoles(Long dataProviderId, List<DataFlowVO> dataflowList) {
    List<DataflowUserRoleVO> dataflowUserRoleVOList = new ArrayList<>();
    for (DataFlowVO dataflowVO : dataflowList) {
      if (TypeStatusEnum.DRAFT.equals(dataflowVO.getStatus())) {
        DataflowUserRoleVO dataflowUserRoleVO = new DataflowUserRoleVO();
        dataflowUserRoleVO.setDataflowId(dataflowVO.getId());
        dataflowUserRoleVO.setDataflowName(dataflowVO.getName());
        dataflowUserRoleVO.setUsers(userManagementControllerZull
            .getUserRolesByDataflowAndCountry(dataflowVO.getId(), dataProviderId));
        if (!dataflowUserRoleVO.getUsers().isEmpty()) {
          dataflowUserRoleVOList.add(dataflowUserRoleVO);
        }
      }
    }
    return dataflowUserRoleVOList;

  }


  /**
   * Gets the public dataflow by id.
   *
   * @param dataflowId the dataflow id
   * @return the public dataflow by id
   * @throws EEAException the EEA exception
   */
  @Override
  public DataflowPublicVO getPublicDataflowById(Long dataflowId) throws EEAException {
    DataflowPublicVO dataflowPublicVO = dataflowPublicMapper
        .entityToClass(dataflowRepository.findByIdAndShowPublicInfoTrue(dataflowId));
    if (null == dataflowPublicVO) {
      throw new EEAException(EEAErrorMessage.DATAFLOW_NOTFOUND);
    }

    if (!TypeDataflowEnum.BUSINESS.equals(dataflowPublicVO.getType())) {
      dataflowPublicVO.setReportingDatasets(
          datasetMetabaseControllerZuul.findReportingDataSetPublicByDataflowId(dataflowId));

      dataflowPublicVO.setReferenceDatasets(
          referenceDatasetControllerZuul.findReferenceDataSetPublicByDataflowId(dataflowId));
    }

    Dataflow dataflow = dataflowRepository.findById(dataflowId).orElse(null);
    if (dataflow != null) {
      DataFlowVO dataflowVO = dataflowMapper.entityToClass(dataflow);
      if (dataflowVO.getDocuments() != null) {
        List<DocumentVO> publicDocuments = new ArrayList<>();
        dataflowVO.getDocuments().forEach(document -> {
          if (Boolean.TRUE.equals(document.getIsPublic())) {
            publicDocuments.add(document);
          }
        });
        dataflowPublicVO.setDocuments(publicDocuments);
      }
      if (dataflowVO.getWeblinks() != null) {
        List<WeblinkVO> publicWeblinks = new ArrayList<>();
        dataflowVO.getWeblinks().forEach(weblink -> {
          if (Boolean.TRUE.equals(weblink.getIsPublic())) {
            publicWeblinks.add(weblink);
          }
        });
        dataflowPublicVO.setWeblinks(publicWeblinks);
      }
    }

    findObligationPublicDataflow(dataflowPublicVO);
    return dataflowPublicVO;
  }


  /**
   * Update data flow public status.
   *
   * @param dataflowId the dataflow id
   * @param showPublicInfo the show public info
   */
  @Override
  public void updateDataFlowPublicStatus(Long dataflowId, boolean showPublicInfo) {
    dataflowRepository.updatePublicStatus(dataflowId, showPublicInfo);
  }

  /**
   * Gets the dataflows by data provider ids.
   *
   * @param dataProviderIds the data provider ids
   * @return the dataflows by data provider ids
   */
  @Override
  public List<DataFlowVO> getDataflowsByDataProviderIds(List<Long> dataProviderIds) {
    return dataflowMapper
        .entityListToClass(
            dataflowRepository
                .findDataflowsByDataproviderIdsAndDataflowIds(
                    userManagementControllerZull.getResourcesByUser(ResourceTypeEnum.DATAFLOW)
                        .stream().map(ResourceAccessVO::getId).collect(Collectors.toList()),
                    dataProviderIds));
  }


  /**
   * Checks if is reference dataflow draft.
   *
   * @param entity the entity
   * @param entityId the entity id
   * @return true, if is reference dataflow draft
   */
  @Override
  @Transactional
  public boolean isReferenceDataflowDraft(EntityClassEnum entity, Long entityId) {
    boolean reference = false;
    DataFlowVO dataflow = new DataFlowVO();
    try {
      switch (entity) {
        case DATAFLOW:
          dataflow = getMetabaseById(entityId);
          if (TypeDataflowEnum.REFERENCE.equals(dataflow.getType())
              && TypeStatusEnum.DRAFT.equals(dataflow.getStatus())) {
            reference = true;
          }
          break;
        case DATASET:
          DataSetMetabaseVO dataset =
              datasetMetabaseControllerZuul.findDatasetMetabaseById(entityId);
          Long dataflowId = dataset.getDataflowId();
          dataflow = getMetabaseById(dataflowId);
          if (TypeDataflowEnum.REFERENCE.equals(dataflow.getType())
              && TypeStatusEnum.DRAFT.equals(dataflow.getStatus())) {
            reference = true;
          }
          break;
        default:
          break;
      }
    } catch (EEAException e) {
      LOG.error("Error knowing if the entity {} with id {} is reference. Message {}", entity,
          entityId, e.getMessage(), e);
    } catch (Exception e) {
      LOG.error("Unexpected error! Could not find if the entity {} with id {} is reference. Message {}", entity,
              entityId, e.getMessage());
      throw e;
    }
    return reference;
  }

  /**
   * Checks if is dataflow type.
   *
   * @param dataflowType the dataflow type
   * @param entity the entity
   * @param entityId the entity id
   * @return true, if is dataflow type
   */
  @Override
  @Transactional
  public boolean isDataflowType(TypeDataflowEnum dataflowType, EntityClassEnum entity,
      Long entityId) {
    boolean correctType = false;
    DataFlowVO dataflow = new DataFlowVO();
    try {
      switch (entity) {
        case DATAFLOW:
          dataflow = getMetabaseById(entityId);
          if (dataflowType.equals(dataflow.getType())) {
            correctType = true;
          }
          break;
        case DATASET:
          DataSetMetabaseVO dataset =
              datasetMetabaseControllerZuul.findDatasetMetabaseById(entityId);
          Long dataflowId = dataset.getDataflowId();
          dataflow = getMetabaseById(dataflowId);
          if (dataflowType.equals(dataflow.getType())) {
            correctType = true;
          }
          break;
        default:
          break;
      }
    } catch (EEAException e) {
      LOG.error("Error knowing if the entity {} with id {} is a dataflow type {}. Message {}",
          entity, entityId, dataflowType, e.getMessage(), e);
    } catch (Exception e) {
      LOG.error("Unexpected error! Could not find if the entity {} with id {} is dataflow type. Message {}", entity,
              entityId, e.getMessage());
      throw e;
    }
    return correctType;
  }


  /**
   * Checks if is admin.
   *
   * @return true, if is admin
   */
  @Override
  public boolean isAdmin() {
    String roleAdmin = "ROLE_" + SecurityRoleEnum.ADMIN;
    return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
        .anyMatch(role -> roleAdmin.equals(role.getAuthority()));
  }

  /**
   * Sets the reportings.
   *
   * @param dataflowPublicList the dataflow public list
   * @param providerId the provider id
   */
  private void setReportings(List<DataflowPublicVO> dataflowPublicList,
      List<DataProviderVO> providerId) {
    dataflowPublicList.stream().forEach(dataflow -> {
      findObligationPublicDataflow(dataflow);
      dataflow.setReportingDatasets(new ArrayList<>());
      List<ReportingDatasetPublicVO> reportings =
          datasetMetabaseControllerZuul.findReportingDataSetPublicByDataflowId(dataflow.getId());
      if (!reportings.isEmpty()) {
        for (DataProviderVO dataProviderVO : providerId) {
          List<ReportingDatasetPublicVO> reportingsProvider =
              reportings.stream().filter(r -> r.getDataProviderId().equals(dataProviderVO.getId()))
                  .collect(Collectors.toList());
          if (CollectionUtils.isNotEmpty(reportingsProvider)) {
            dataflow.getReportingDatasets().addAll(reportingsProvider);
          }
        }
      }
    });
  }

  /**
   * Find obligation public dataflow.
   *
   * @param dataflowPublicVO the dataflow public VO
   */
  private void findObligationPublicDataflow(DataflowPublicVO dataflowPublicVO) {
    try {
      if (dataflowPublicVO.getObligation() != null
          && dataflowPublicVO.getObligation().getObligationId() != null) {
        dataflowPublicVO.setObligation(obligationControllerZull
            .findObligationById(dataflowPublicVO.getObligation().getObligationId()));
      }
    } catch (FeignException e) {
      LOG.error("Error retrieving obligation for dataflow id {} due to reason {}",
          dataflowPublicVO.getId(), e.getMessage(), e);
    }
  }

  /**
   * Delete documents.
   *
   * @param idDataflow the id dataflow
   * @param dataflowVO the dataflow VO
   * @throws Exception the exception
   */
  private void deleteDocuments(Long idDataflow, DataFlowVO dataflowVO) throws Exception {
    for (DocumentVO document : dataflowVO.getDocuments()) {
      try {
        documentControllerZuul.deleteDocument(document.getId(), dataflowVO.getId(), Boolean.FALSE);
      } catch (EEAException e) {
        LOG.error("Error deleting document with id {}", document.getId());
        throw new EEAException(new StringBuilder().append("Error Deleting document ")
            .append(document.getName()).append(" with ").append(document.getId()).toString(), e);
      }
    }
    LOG.info("Documents deleted to dataflow with id: {}", idDataflow);
  }

  /**
   * Delete dataset schemas.
   *
   * @param idDataflow the id dataflow
   * @param dataflowVO the dataflow VO
   * @throws EEAException the EEA exception
   */
  private void deleteDatasetSchemas(Long idDataflow, DataFlowVO dataflowVO) throws EEAException {
    for (DesignDatasetVO designDatasetVO : dataflowVO.getDesignDatasets()) {
      try {
        datasetSchemaControllerZuul.deleteDatasetSchema(designDatasetVO.getId(), true);
      } catch (Exception e) {
        LOG.error("Error deleting DesignDataset with id {}", designDatasetVO.getId(), e);
        throw new EEAException(new StringBuilder().append("Error Deleting dataset ")
            .append(designDatasetVO.getDataSetName()).append(" with ")
            .append(designDatasetVO.getId()).toString(), e);
      }
    }
    LOG.info("Delete full datasetSchemas with dataflow id: {}", idDataflow);
  }

  /**
   * Delete representatives.
   *
   * @param dataflowVO the dataflow VO
   * @throws EEAException the EEA exception
   */
  private void deleteRepresentatives(DataFlowVO dataflowVO) throws EEAException {
    for (RepresentativeVO representative : dataflowVO.getRepresentatives()) {
      try {
        representativeRepository.deleteById(representative.getId());
      } catch (Exception e) {
        LOG.error("Error deleting representative with id {}", representative.getId(), e);
        throw new EEAException(new StringBuilder().append("Error Deleting representative")
            .append(" with id ").append(representative.getId()).toString(), e);
      }
    }
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
    return resourceInfoVO;
  }

  /**
   * Gets the obligation.
   *
   * @param dataflow the dataflow
   * @return the obligation
   */
  private void getObligation(DataFlowVO dataflow) {
    // Get the obligationVO from ROD and Set in dataflow VO
    // We check that the field is not empty to avoid the call to rod due to maintain backward
    // compatibility concerns
    if (dataflow.getObligation() != null && dataflow.getObligation().getObligationId() != null) {
      try {
        dataflow.setObligation(obligationControllerZull
            .findObligationById(dataflow.getObligation().getObligationId()));

      } catch (FeignException e) {
        LOG.error("Error while getting obligation by id {}", e.getMessage(), e);
        ObligationVO obligationVO = new ObligationVO();
        dataflow.setObligation(obligationVO);
      }
    }
  }

  /**
   * Gets the by id with condition.
   *
   * @param id the id
   * @param includeAllRepresentatives the include all representatives
   * @param providerId the provider id
   * @return the by id with condition
   * @throws EEAException the EEA exception
   */
  private DataFlowVO getByIdWithCondition(Long id, boolean includeAllRepresentatives,
      Long providerId) throws EEAException {

    DataFlowVO dataflowVO = new DataFlowVO();
    ThreadPropertiesManager.setVariable("user",
        SecurityContextHolder.getContext().getAuthentication().getName());

    if (id == null) {
      throw new EEAException(EEAErrorMessage.DATAFLOW_NOTFOUND);
    }
    Dataflow result = dataflowRepository.findById(id).orElse(null);

    if (result != null) {
      dataflowVO = dataflowMapper.entityToClass(result);

      if (TypeDataflowEnum.BUSINESS.equals(dataflowVO.getType())) {
        dataflowVO.setDataProviderGroupName(
            dataProviderGroupRepository.findById(dataflowVO.getDataProviderGroupId())
                .orElse(new DataProviderGroup()).getName());
        dataflowVO.setFmeUserName(fmeUserRepository.findById(dataflowVO.getFmeUserId())
            .orElse(new FMEUser()).getUsername());
      }

      // filter design datasets (schemas) showed to the user depending on permissions
      List<ResourceAccessVO> datasets =
          userManagementControllerZull.getResourcesByUser(ResourceTypeEnum.DATA_SCHEMA);

      if (TypeStatusEnum.DRAFT.equals(dataflowVO.getStatus())) {
        // add to the filter the reporting datasets
        datasets.addAll(userManagementControllerZull.getResourcesByUser(ResourceTypeEnum.DATASET));
        // also, add to the filter the data collection
        datasets.addAll(
            userManagementControllerZull.getResourcesByUser(ResourceTypeEnum.DATA_COLLECTION));
        // and the eu datasets
        datasets
            .addAll(userManagementControllerZull.getResourcesByUser(ResourceTypeEnum.EU_DATASET));
        // add the test datasets
        datasets
            .addAll(userManagementControllerZull.getResourcesByUser(ResourceTypeEnum.TEST_DATASET));
        // add the reference datasets
        datasets.addAll(
            userManagementControllerZull.getResourcesByUser(ResourceTypeEnum.REFERENCE_DATASET));
      }

      List<Long> datasetsIds =
          datasets.stream().map(ResourceAccessVO::getId).collect(Collectors.toList());


      // If the dataflow it's on design status, no need to call for DC, EU datasets, Tests, etc. and
      // we can
      // save some calls
      if (TypeStatusEnum.DRAFT.equals(dataflowVO.getStatus())) {
        // Set the reporting datasets
        if (providerId == null) {
          dataflowVO.setReportingDatasets(
              datasetMetabaseControllerZuul.findReportingDataSetIdByDataflowId(id).stream()
                  .filter(dataset -> datasetsIds.contains(dataset.getId()))
                  .collect(Collectors.toList()));
        } else {
          dataflowVO.setReportingDatasets(datasetMetabaseControllerZuul
              .findReportingDataSetIdByDataflowIdAndProviderId(id, providerId).stream()
              .filter(dataset -> datasetsIds.contains(dataset.getId()))
              .collect(Collectors.toList()));
        }

        // Add the data collections
        dataflowVO.setDataCollections(dataCollectionControllerZuul
            .findDataCollectionIdByDataflowId(id).stream()
            .filter(dataset -> datasetsIds.contains(dataset.getId())).collect(Collectors.toList()));

        // Add the EU datasets
        dataflowVO.setEuDatasets(euDatasetControllerZuul.findEUDatasetByDataflowId(id).stream()
            .filter(dataset -> datasetsIds.contains(dataset.getId())).collect(Collectors.toList()));

        // Add the Test datasets
        dataflowVO.setTestDatasets(testDataSetControllerZuul.findTestDatasetByDataflowId(id)
            .stream().filter(dataset -> datasetsIds.contains(dataset.getId()))
            .collect(Collectors.toList()));

        // Add the Reference datasets
        dataflowVO.setReferenceDatasets(referenceDatasetControllerZuul
            .findReferenceDatasetByDataflowId(id).stream()
            .filter(dataset -> datasetsIds.contains(dataset.getId())).collect(Collectors.toList()));

      } else {
        dataflowVO.setReportingDatasets(new ArrayList<>());
        dataflowVO.setDataCollections(new ArrayList<>());
        dataflowVO.setEuDatasets(new ArrayList<>());
        dataflowVO.setTestDatasets(new ArrayList<>());
        dataflowVO.setReferenceDatasets(new ArrayList<>());
      }

      // special logic to REFERENCE DATAFLOWS that are in DRAFT status
      if (TypeDataflowEnum.REFERENCE.equals(dataflowVO.getType())
          && TypeStatusEnum.DRAFT.equals(dataflowVO.getStatus())
          && dataflowVO.getReferenceDatasets().isEmpty()) {
        dataflowVO.setReferenceDatasets(
            referenceDatasetControllerZuul.findReferenceDatasetByDataflowId(id));
      }


      // Add the representatives and design datasets
      if (includeAllRepresentatives) {
        dataflowVO.setRepresentatives(representativeService.getRepresetativesByIdDataFlow(id));
        dataflowVO
            .setDesignDatasets(datasetMetabaseControllerZuul.findDesignDataSetIdByDataflowId(id));
      } else {
        dataflowVO.setDesignDatasets(datasetMetabaseControllerZuul
            .findDesignDataSetIdByDataflowId(id).stream()
            .filter(dataset -> datasetsIds.contains(dataset.getId())).collect(Collectors.toList()));
        UserRepresentationVO user = userManagementControllerZull.getUserByUserId();
        dataflowVO.setRepresentatives(
            representativeService.getRepresetativesByDataflowIdAndEmail(id, user.getEmail()));
      }
      try {
        getObligation(dataflowVO);
      } catch (FeignException e) {
        LOG.error("Error retrieving obligation for dataflow id {} due to reason {}", id,
            e.getMessage(), e);
      }
      // we sort the weblinks and documents
      if (!CollectionUtils.isEmpty(dataflowVO.getWeblinks())) {
        dataflowVO.getWeblinks()
            .sort(Comparator.comparing(WeblinkVO::getDescription, String.CASE_INSENSITIVE_ORDER));
      }
      if (!CollectionUtils.isEmpty(dataflowVO.getDocuments())) {
        dataflowVO.getDocuments()
            .sort(Comparator.comparing(DocumentVO::getDescription, String.CASE_INSENSITIVE_ORDER));
      }

      // Calculate anySchemaAvailableInPublic
      dataflowVO.setAnySchemaAvailableInPublic(
          dataSetControllerZuul.checkAnySchemaAvailableInPublic(dataflowVO.getId()));

      LOG.info("Get the dataflow information with id {}", id);

    }

    return dataflowVO;
  }

  /**
   * Gets the opened obligations.
   *
   * @param dataflowVOs the dataflow V os
   * @return the opened obligations
   */
  private void getOpenedObligations(List<DataFlowVO> dataflowVOs) {

    try {
      // Get all opened obligations from ROD
      List<ObligationVO> obligations = obligationControllerZull
          .findOpenedObligations(null, null, null, null, null).getObligations();

      Map<Integer, ObligationVO> obligationMap = obligations.stream()
          .collect(Collectors.toMap(ObligationVO::getObligationId, obligation -> obligation));

      for (DataFlowVO dataFlowVO : dataflowVOs) {
        if (dataFlowVO.getObligation() != null
            && dataFlowVO.getObligation().getObligationId() != null) {
          dataFlowVO.setObligation(obligationMap.get(dataFlowVO.getObligation().getObligationId()));
        }
      }
    } catch (FeignException e) {
      LOG.error("Error while getting all opened obligations {}", e.getMessage(), e);
      for (DataFlowVO dataFlowVO : dataflowVOs) {
        ObligationVO obligationVO = new ObligationVO();
        dataFlowVO.setObligation(obligationVO);
      }
    }
  }

  /**
   * Delete dataflow resources.
   *
   * @param dataflowId the dataflow id
   * @throws EEAException the EEA exception
   */
  private void deleteDataflowResources(Long dataflowId) throws EEAException {
    // add resource to delete(DATAFLOW PART)
    try {
      List<ResourceInfoVO> resourceCustodian = resourceManagementControllerZull
          .getGroupsByIdResourceType(dataflowId, ResourceTypeEnum.DATAFLOW);
      resourceManagementControllerZull.deleteResource(resourceCustodian);
    } catch (Exception e) {
      LOG.error("Error deleting resources in keycloak, group with the id: {}, and exception {}",
          dataflowId, e.getMessage());
      throw new EEAException("Error deleting resource in keycloak ", e);
    }
  }

  /**
   * Delete dataflow metabase info.
   *
   * @param dataflowId the dataflow id
   * @throws EEAException the EEA exception
   */
  private void deleteDataflowMetabaseInfo(Long dataflowId) throws EEAException {
    try {
      dataflowRepository.deleteNativeDataflow(dataflowId);
    } catch (Exception e) {
      LOG.error("Error deleting native dataflow with id: {}", dataflowId, e);
      throw new EEAException(
          String.format("Error deleting native dataflow with id: %s", dataflowId), e);
    }
  }

  /**
   * Sets the reporting dataset status.
   *
   * @param datasetsStatusList the datasets status list
   * @param dataflowVO the dataflow VO
   */
  private void setReportingDatasetStatus(List<DataflowStatusDataset> datasetsStatusList,
      DataFlowVO dataflowVO) {
    boolean containsPending = false;
    int releasedCount = 0;
    int techAcceptedCount = 0;
    boolean containsCorrectionR = false;
    boolean containsFinalFeedback = false;
    if (null != datasetsStatusList) {
      for (int i = 0; i < datasetsStatusList.size() && !containsPending; i++) {
        if(datasetsStatusList.get(i).getStatus() == null) {
          LOG.error("For dataflowId {} there is a dataset with status null and providerId {}. This dataset must be removed from keycloak", datasetsStatusList.get(i).getId(), datasetsStatusList.get(i).getDataProviderId());
        }
        switch (datasetsStatusList.get(i).getStatus()) {
          case PENDING:
            containsPending = true;
            break;
          case RELEASED:
            releasedCount++;
            break;
          case CORRECTION_REQUESTED:
            containsCorrectionR = true;
            break;
          case TECHNICALLY_ACCEPTED:
            techAcceptedCount++;
            break;
          case FINAL_FEEDBACK:
            containsFinalFeedback = true;
            break;
          default:
            containsPending = true;
            break;
        }
      }
      if (containsPending) {
        dataflowVO.setReportingStatus(DatasetStatusEnum.PENDING);
      } else {
        if (releasedCount == datasetsStatusList.size()) {
          dataflowVO.setReportingStatus(DatasetStatusEnum.RELEASED);
        } else if (techAcceptedCount == datasetsStatusList.size()) {
          dataflowVO.setReportingStatus(DatasetStatusEnum.TECHNICALLY_ACCEPTED);
        } else if (containsCorrectionR) {
          dataflowVO.setReportingStatus(DatasetStatusEnum.CORRECTION_REQUESTED);
        } else if (containsFinalFeedback) {
          dataflowVO.setReportingStatus(DatasetStatusEnum.FINAL_FEEDBACK);
        }
      }
      // If there's a user assigned to more than one provider, we mark MULTIPLE
      if (CollectionUtils.isNotEmpty(datasetsStatusList)) {
        Set<Long> providersInDataflow = new HashSet<>();
        datasetsStatusList.stream().forEach(d -> providersInDataflow.add(d.getDataProviderId()));
        if (providersInDataflow.size() > 1) {
          dataflowVO.setReportingStatus(DatasetStatusEnum.MULTIPLE);
        }
      }

    }
  }


  /**
   * Gets the datasets status by user.
   *
   * @return the datasets status by user
   */
  private Map<Long, List<DataflowStatusDataset>> getDatasetsStatusByUser() {
    Map<Long, List<DataflowStatusDataset>> map = new HashMap<>();

    List<Long> listDatasets =
        userManagementControllerZull.getResourcesByUser(ResourceTypeEnum.DATASET).stream()
            .map(ResourceAccessVO::getId).collect(Collectors.toList());
    if (!listDatasets.isEmpty()) {
      List<IDatasetStatus> queryResult = dataflowRepository.getDatasetsStatus(listDatasets);
      for (IDatasetStatus object : queryResult) {
        List<DataflowStatusDataset> list = new ArrayList<>();
        DataflowStatusDataset dataflowStatusDataset = new DataflowStatusDataset();
        dataflowStatusDataset.setId(object.getId());
        if (null != object.getStatus()) {
          dataflowStatusDataset.setStatus(DatasetStatusEnum.valueOf(object.getStatus()));
        }
        if (null != object.getDataProviderId()) {
          dataflowStatusDataset.setDataProviderId(object.getDataProviderId());
        }
        list.add(dataflowStatusDataset);
        if (map.get(dataflowStatusDataset.getId()) != null) {
          map.get(dataflowStatusDataset.getId()).addAll(list);
        } else {
          map.put(dataflowStatusDataset.getId(), list);
        }

      }
    }
    return map;
  }

  /**
   * Gets the private dataflow by id.
   *
   * @param dataflowId the dataflow id
   * @return the private dataflow by id
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public DataflowPrivateVO getPrivateDataflowById(Long dataflowId) throws EEAException {
    DataflowPrivateVO dataflowPrivateVO = null;
    if (null != dataflowId) {
      Dataflow dataflow = dataflowRepository.findById(dataflowId).orElse(null);
      if (null != dataflow) {
        dataflowPrivateVO = dataflowPrivateMapper.entityToClass(dataflow);
        dataflowPrivateVO
            .setDocuments(documentControllerZuul.getAllDocumentsByDataflow(dataflowId));
      } else {
        throw new EEAException(EEAErrorMessage.DATAFLOW_NOTFOUND);
      }
    } else {
      throw new EEAException(EEAErrorMessage.DATAFLOW_INCORRECT_ID);
    }
    return dataflowPrivateVO;
  }

  /**
   * Gets the dataset summary.
   *
   * @param dataflowId the dataflow id
   * @return the dataset summary
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public List<DatasetsSummaryVO> getDatasetSummary(Long dataflowId) throws EEAException {
    List<DatasetsSummaryVO> datasetsSummaryList = new ArrayList<>();
    if (null != dataflowId) {
      Dataflow dataflow = dataflowRepository.findById(dataflowId).orElse(null);
      if (null != dataflow) {
        datasetsSummaryList = datasetMetabaseControllerZuul.getDatasetsSummaryList(dataflowId);
      } else {
        throw new EEAException(EEAErrorMessage.DATAFLOW_NOTFOUND);
      }
    } else {
      throw new EEAException(EEAErrorMessage.DATAFLOW_INCORRECT_ID);
    }
    return datasetsSummaryList;
  }

  /**
   * Gets the dataflows count.
   *
   * @return the dataflows count
   */
  @Override
  public List<DataflowCountVO> getDataflowsCount() {

    boolean isAdmin = isAdmin();

    List<ResourceAccessVO> resourcesByUser = userManagementControllerZull.getResourcesByUser(ResourceTypeEnum.DATAFLOW);
    LOG.info("resourcesByUser {}", resourcesByUser);
    resourcesByUser = removeResourcesIfNeeded(resourcesByUser);
    LOG.info("resourcesByUser after deletion {}", resourcesByUser);
    List<Long>  idsResources = resourcesByUser.stream().map(ResourceAccessVO::getId).collect(Collectors.toList());
    LOG.info("idsResources {}", idsResources);

    List<IDataflowCount> dataflowCountList = new ArrayList<>();

    if (CollectionUtils.isNotEmpty(idsResources) || isAdmin) {
      dataflowCountList = isAdmin ? dataflowRepository.countDataflowByType()
          : dataflowRepository.countDataflowByTypeAndUser(idsResources);
    }

    List<DataflowCountVO> dataflowCountVOList = new ArrayList<>();

    for (IDataflowCount dataflow : dataflowCountList) {
      DataflowCountVO newDataflowCountVO = new DataflowCountVO();

      /* Commented out section for ticket Feature-265682 */
//      if (dataflow.getType() == TypeDataflowEnum.REFERENCE && !isAdmin) {
//        continue;
//      }

      newDataflowCountVO.setType(dataflow.getType());
      newDataflowCountVO.setAmount(dataflow.getAmount());
      dataflowCountVOList.add(newDataflowCountVO);
    }

    /* Commented out section for ticket Feature-265682 */
//    if (!isAdmin) {
//      IDataflowCount draftReferenceDataflow = dataflowRepository.countReferenceDataflowsDraft();
//      IDataflowCount designReferenceDataflow = null;
//
//      if (CollectionUtils.isNotEmpty(idsResources))
//        designReferenceDataflow =
//            dataflowRepository.countReferenceDataflowsDesignByUser(idsResources);
//
//      long totalReferenceAmount = 0L;
//
//      if (designReferenceDataflow != null) {
//        totalReferenceAmount += designReferenceDataflow.getAmount();
//      }
//
//      if (draftReferenceDataflow != null) {
//        totalReferenceAmount += draftReferenceDataflow.getAmount();
//      }
//
//      DataflowCountVO totalReferenceCountVO = new DataflowCountVO();
//      totalReferenceCountVO.setType(TypeDataflowEnum.REFERENCE);
//      totalReferenceCountVO.setAmount(totalReferenceAmount);
//      dataflowCountVOList.add(totalReferenceCountVO);
//    }

    return dataflowCountVOList;
  }

  /**
   * Validate all reporters.
   *
   * @param userId the user id
   * @throws EEAException the EEA exception
   */
  @Override
  @Async
  public void validateAllReporters(String userId) throws EEAException {

    try {
      List<Representative> representativeList = representativeRepository.findAllByInvalid(true);
      List<TempUser> tempUserList = tempUserRepository.findAll();

      Set<Long> dataflowsToCheck = new HashSet<>();
      for (Representative representative : representativeList) {
        dataflowsToCheck.add(representative.getDataflow().getId());
      }
      for (Long dataflowId : dataflowsToCheck) {
        representativeService.validateLeadReporters(dataflowId, false);
      }

      for (TempUser tempuser : tempUserList) {
        contributorService.validateReporters(tempuser.getDataflowId(), tempuser.getDataProviderId(),
            false);
      }

      NotificationVO notificationVO = NotificationVO.builder()
          .user(SecurityContextHolder.getContext().getAuthentication().getName()).build();

      kafkaSenderUtils.releaseNotificableKafkaEvent(
          EventType.VALIDATE_ALL_REPORTERS_COMPLETED_EVENT, null, notificationVO);
      LOG.info("Successfully validated all reporters with userId {}", userId);

    } catch (EEAException e) {
      LOG.error(
          "An error was produced while validating reporters and lead reporters for all dataflows");
      NotificationVO notificationVO = NotificationVO.builder()
          .user(SecurityContextHolder.getContext().getAuthentication().getName()).build();

      kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.VALIDATE_ALL_REPORTERS_FAILED_EVENT,
          null, notificationVO);
    }
  }

  /**
   * Update data flow automatic reporting deletion.
   *
   * @param dataflowId the dataflow id
   * @param automaticReportingDeletion the automatic reporting deletion
   */
  @Override
  public void updateDataFlowAutomaticReportingDeletion(Long dataflowId,
      boolean automaticReportingDeletion) {
    dataflowRepository.updateAutomaticReportingDeletion(dataflowId, automaticReportingDeletion);
  }

  /**
   * Gets the dataflows metabase by id.
   *
   * @param dataflowIds the dataflow ids
   * @return the dataflows metabase by id
   */
  @Override
  public List<DataFlowVO> getDataflowsMetabaseById(List<Long> dataflowIds) {
    return dataflowMapper
        .entityListToClass(dataflowRepository.findMetabaseByDataflowIds(dataflowIds));
  }

  /**
   * Removes the web links and documents.
   *
   * @param result the result
   */
  private void removeWebLinksAndDocuments(DataFlowVO result) {
    result.setWeblinks(null);
    result.setDocuments(null);
  }

}
