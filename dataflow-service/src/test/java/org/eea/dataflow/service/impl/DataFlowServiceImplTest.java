package org.eea.dataflow.service.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.eea.dataflow.mapper.DataflowMapper;
import org.eea.dataflow.mapper.DataflowNoContentMapper;
import org.eea.dataflow.mapper.DocumentMapper;
import org.eea.dataflow.mapper.MessageMapper;
import org.eea.dataflow.persistence.domain.Contributor;
import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.dataflow.persistence.domain.Document;
import org.eea.dataflow.persistence.domain.Message;
import org.eea.dataflow.persistence.domain.Representative;
import org.eea.dataflow.persistence.domain.UserRequest;
import org.eea.dataflow.persistence.repository.ContributorRepository;
import org.eea.dataflow.persistence.repository.DataProviderRepository;
import org.eea.dataflow.persistence.repository.DataflowRepository;
import org.eea.dataflow.persistence.repository.DocumentRepository;
import org.eea.dataflow.persistence.repository.MessageRepository;
import org.eea.dataflow.persistence.repository.RepresentativeRepository;
import org.eea.dataflow.persistence.repository.UserRequestRepository;
import org.eea.dataflow.service.RepresentativeService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DataCollectionController.DataCollectionControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSchemaController.DatasetSchemaControllerZuul;
import org.eea.interfaces.controller.dataset.EUDatasetController.EUDatasetControllerZuul;
import org.eea.interfaces.controller.document.DocumentController.DocumentControllerZuul;
import org.eea.interfaces.controller.rod.ObligationController;
import org.eea.interfaces.controller.ums.ResourceManagementController.ResourceManagementControllerZull;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.MessageVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.dataflow.enums.TypeRequestEnum;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.eea.interfaces.vo.dataset.DataCollectionVO;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
import org.eea.interfaces.vo.dataset.EUDatasetVO;
import org.eea.interfaces.vo.dataset.ReportingDatasetVO;
import org.eea.interfaces.vo.dataset.enums.DatasetStatusEnum;
import org.eea.interfaces.vo.document.DocumentVO;
import org.eea.interfaces.vo.rod.ObligationVO;
import org.eea.interfaces.vo.ums.ResourceAccessVO;
import org.eea.interfaces.vo.ums.enums.ResourceTypeEnum;
import org.eea.security.authorization.ObjectAccessRoleEnum;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * The Class DataFlowServiceImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class DataFlowServiceImplTest {

  /** The dataflow service impl. */
  @InjectMocks
  private DataflowServiceImpl dataflowServiceImpl;

  /** The data provider repository. */
  @Mock
  private DataProviderRepository dataProviderRepository;

  /** The dataflow repository. */
  @Mock
  private DataflowRepository dataflowRepository;

  /** The user request repository. */
  @Mock
  private UserRequestRepository userRequestRepository;

  /** The contributor repository. */
  @Mock
  private ContributorRepository contributorRepository;

  /** The document repository. */
  @Mock
  private DocumentRepository documentRepository;

  /** The dataflow mapper. */
  @Mock
  private DataflowMapper dataflowMapper;

  /** The dataflow no content mapper. */
  @Mock
  private DataflowNoContentMapper dataflowNoContentMapper;

  /** The dataset metabase controller. */
  @Mock
  private DataSetMetabaseControllerZuul datasetMetabaseController;

  /** The user management controller zull. */
  @Mock
  private UserManagementControllerZull userManagementControllerZull;

  /** The resource management controller zull. */
  @Mock
  private ResourceManagementControllerZull resourceManagementControllerZull;

  /** The document mapper. */
  @Mock
  private DocumentMapper documentMapper;

  /** The dataset schema controller zuul. */
  @Mock
  private DatasetSchemaControllerZuul datasetSchemaControllerZuul;

  /** The document controller zuul. */
  @Mock
  private DocumentControllerZuul documentControllerZuul;

  /** The data collection controller zuul. */
  @Mock
  private DataCollectionControllerZuul dataCollectionControllerZuul;

  /** The representative repository. */
  @Mock
  private RepresentativeRepository representativeRepository;

  /** The representative service. */
  @Mock
  private RepresentativeService representativeService;

  /** The obligation controller. */
  @Mock
  private ObligationController obligationController;

  /** The eu dataset controller zuul. */
  @Mock
  private EUDatasetControllerZuul euDatasetControllerZuul;

  /** The message repository. */
  @Mock
  private MessageRepository messageRepository;

  /** The message mapper. */
  @Mock
  private MessageMapper messageMapper;

  /** The dataflows. */
  private List<Dataflow> dataflows;

  /** The pageable. */
  private Pageable pageable;

  /** The security context. */
  private SecurityContext securityContext;

  /** The authentication. */
  private Authentication authentication;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    dataflows = new ArrayList<>();
    dataflows.add(new Dataflow());
    pageable = PageRequest.of(1, 1);
    authentication = Mockito.mock(Authentication.class);
    securityContext = Mockito.mock(SecurityContext.class);
    securityContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(securityContext);
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Gets the by id throws.
   *
   * @return the by id throws
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void getByIdThrows() throws EEAException {
    try {
      dataflowServiceImpl.getById(null);
    } catch (EEAException ex) {
      assertEquals(EEAErrorMessage.DATAFLOW_NOTFOUND, ex.getMessage());
      throw ex;
    }
  }

  /**
   * Gets the by id.
   *
   * @return the by id
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void getById() throws EEAException {
    DataFlowVO dataFlowVO = new DataFlowVO();
    ReportingDatasetVO reportingDatasetVO = new ReportingDatasetVO();
    reportingDatasetVO.setId(1L);
    List<ReportingDatasetVO> reportingDatasetVOs = new ArrayList<>();
    reportingDatasetVOs.add(reportingDatasetVO);
    List<DesignDatasetVO> designDatasetVOs = new ArrayList<>();
    DesignDatasetVO designDatasetVO = new DesignDatasetVO();
    designDatasetVOs.add(designDatasetVO);
    when(userManagementControllerZull.getResourcesByUser(Mockito.any(ResourceTypeEnum.class)))
        .thenReturn(new ArrayList<>());
    when(dataflowMapper.entityToClass(Mockito.any())).thenReturn(dataFlowVO);
    when(datasetMetabaseController.findReportingDataSetIdByDataflowId(1L))
        .thenReturn(reportingDatasetVOs);
    when(datasetMetabaseController.findDesignDataSetIdByDataflowId(1L))
        .thenReturn(designDatasetVOs);
    RepresentativeVO representative = new RepresentativeVO();
    representative.setDataProviderId(1L);
    when(representativeService.getRepresetativesByIdDataFlow(Mockito.anyLong()))
        .thenReturn(Arrays.asList(representative));
    dataFlowVO.setReportingDatasets(reportingDatasetVOs);
    dataFlowVO.setDesignDatasets(designDatasetVOs);
    ObligationVO obligation = new ObligationVO();
    obligation.setObligationId(1);
    dataFlowVO.setObligation(obligation);
    assertEquals("fail", dataFlowVO, dataflowServiceImpl.getById(1L));
  }


  /**
   * Gets the by status.
   *
   * @return the by status
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void getByStatus() throws EEAException {
    when(dataflowRepository.findByStatus(Mockito.any())).thenReturn(new ArrayList<>());
    dataflowServiceImpl.getByStatus(Mockito.any());
    assertEquals("fail", new ArrayList<>(), dataflowServiceImpl.getByStatus(Mockito.any()));
  }

  /**
   * Gets the pending accepted.
   *
   * @return the pending accepted
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void getDataflows() throws EEAException {
    DataFlowVO dfVO = new DataFlowVO();
    dfVO.setId(1L);
    ObligationVO obligation = new ObligationVO();
    obligation.setObligationId(1);
    dfVO.setObligation(obligation);
    dfVO.setUserRequestStatus(TypeRequestEnum.ACCEPTED);
    List<DataFlowVO> dataflowsVO = new ArrayList<>();
    dataflowsVO.add(dfVO);

    ResourceAccessVO resource = new ResourceAccessVO();
    resource.setId(1L);
    List<ResourceAccessVO> resources = new ArrayList<>();
    resources.add(resource);
    when(userManagementControllerZull.getResourcesByUser(Mockito.any(ResourceTypeEnum.class)))
        .thenReturn(resources);
    Object[] object = {BigInteger.ONE, DatasetStatusEnum.CORRECTION_REQUESTED.toString()};
    Object[] object1 = {BigInteger.ONE, DatasetStatusEnum.FINAL_FEEDBACK.toString()};
    Object[] object2 = {BigInteger.ONE, DatasetStatusEnum.PENDING.toString()};
    Object[] object3 = {BigInteger.ONE, DatasetStatusEnum.TECHNICALLY_ACCEPTED.toString()};
    Object[] object4 = {BigInteger.ONE, DatasetStatusEnum.RELEASED.toString()};
    List<Object[]> listObject =
        new ArrayList<>(Arrays.asList(object, object1, object2, object3, object4));
    when(dataflowRepository.getDatasetsStatus(Mockito.any())).thenReturn(listObject);

    dataflowServiceImpl.getDataflows(Mockito.any());
    List<Dataflow> list = new ArrayList<>();
    list.add(new Dataflow());
    Mockito.when(dataflowRepository.findByIdInOrderByStatusDescCreationDateDesc(Mockito.any()))
        .thenReturn(list);
    Mockito.when(dataflowNoContentMapper.entityToClass(Mockito.any())).thenReturn(dfVO);
    assertEquals("fail", dataflowsVO, dataflowServiceImpl.getDataflows(Mockito.any()));
  }

  /**
   * Gets the pending by user.
   *
   * @return the pending by user
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void getPendingByUser() throws EEAException {
    when(dataflowRepository.findByStatusAndUserRequester(Mockito.any(), Mockito.any()))
        .thenReturn(new ArrayList<>());
    dataflowServiceImpl.getPendingByUser(Mockito.any(), Mockito.any());
    assertEquals("fail", new ArrayList<>(),
        dataflowServiceImpl.getPendingByUser(Mockito.any(), Mockito.any()));
  }

  /**
   * Gets the completed empty.
   *
   * @return the completed empty
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void getCompletedEmpty() throws EEAException {
    when(dataflowRepository.findCompleted(Mockito.any(), Mockito.any()))
        .thenReturn(new ArrayList<>());
    dataflowServiceImpl.getCompleted("1L", pageable);
    assertEquals("fail", new ArrayList<>(), dataflowServiceImpl.getCompleted("1L", pageable));
  }

  /**
   * Gets the completed.
   *
   * @return the completed
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void getCompleted() throws EEAException {
    when(dataflowRepository.findCompleted(Mockito.any(), Mockito.any())).thenReturn(dataflows);
    dataflowServiceImpl.getCompleted("1L", pageable);
    assertEquals("fail", new ArrayList<>(), dataflowServiceImpl.getCompleted("1L", pageable));
    dataflows.add(new Dataflow());
    dataflows.add(new Dataflow());
    when(dataflowRepository.findCompleted(Mockito.any(), Mockito.any())).thenReturn(dataflows);
    dataflowServiceImpl.getCompleted("1L", pageable);
    assertEquals("fail", new ArrayList<>(), dataflowServiceImpl.getCompleted("1L", pageable));
  }


  /**
   * Update user request status.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateUserRequestStatus() throws EEAException {
    Mockito.doNothing().when(userRequestRepository).updateUserRequestStatus(Mockito.any(),
        Mockito.any());
    Optional<UserRequest> ur = Optional.of(new UserRequest());
    Set<Dataflow> dfs = new HashSet<>();
    Dataflow df = new Dataflow();
    df.setId(1L);
    dfs.add(df);
    ur.get().setDataflows(dfs);

    when(userRequestRepository.findById(Mockito.anyLong())).thenReturn(ur);
    Mockito.doNothing().when(userManagementControllerZull).addUserToResource(Mockito.any(),
        Mockito.any());

    dataflowServiceImpl.updateUserRequestStatus(1L, TypeRequestEnum.ACCEPTED);
    Mockito.verify(userRequestRepository, times(1)).updateUserRequestStatus(Mockito.any(),
        Mockito.any());
  }

  /**
   * Update user request status 2.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateUserRequestStatus2() throws EEAException {
    Mockito.doNothing().when(userRequestRepository).updateUserRequestStatus(Mockito.any(),
        Mockito.any());
    dataflowServiceImpl.updateUserRequestStatus(1L, TypeRequestEnum.REJECTED);
    Mockito.verify(userRequestRepository, times(1)).updateUserRequestStatus(Mockito.any(),
        Mockito.any());
  }


  /**
   * Adds the contributor.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void addContributor() throws EEAException {
    when(contributorRepository.save(Mockito.any())).thenReturn(new Contributor());
    dataflowServiceImpl.addContributorToDataflow(1L, "");
    Mockito.verify(contributorRepository, times(1)).save(Mockito.any());
  }

  /**
   * Removes the contributor.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void removeContributor() throws EEAException {
    Mockito.doNothing().when(contributorRepository).removeContributorFromDataset(Mockito.any(),
        Mockito.any());
    dataflowServiceImpl.removeContributorFromDataflow(1L, "");
    Mockito.verify(contributorRepository, times(1)).removeContributorFromDataset(Mockito.any(),
        Mockito.any());
  }

  /**
   * Creates the data flow exist.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void createDataFlowExist() throws EEAException {
    DataFlowVO dataFlowVO = new DataFlowVO();
    when(dataflowRepository.findByNameIgnoreCase(dataFlowVO.getName()))
        .thenReturn(Optional.of(new Dataflow()));
    try {
      dataflowServiceImpl.createDataFlow(dataFlowVO);
    } catch (EEAException ex) {
      assertEquals(EEAErrorMessage.DATAFLOW_EXISTS_NAME, ex.getMessage());
      throw ex;
    }

  }

  /**
   * Creates the data flow non exist.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createDataFlowNonExist() throws EEAException {
    DataFlowVO dataflowVO = new DataFlowVO();
    Dataflow dataflow = new Dataflow();
    when(dataflowMapper.classToEntity(dataflowVO)).thenReturn(dataflow);
    doNothing().when(resourceManagementControllerZull).createResource(Mockito.any());
    doNothing().when(userManagementControllerZull).addUserToResource(Mockito.any(), Mockito.any());
    when(dataflowRepository.save(dataflow)).thenReturn(new Dataflow());
    dataflowServiceImpl.createDataFlow(dataflowVO);
    Mockito.verify(resourceManagementControllerZull, times(4)).createResource(Mockito.any());
  }

  /**
   * Update data flow exist.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void updateDataFlowExist() throws EEAException {
    DataFlowVO dataFlowVO = new DataFlowVO();
    dataFlowVO.setId(1L);
    Dataflow dataflowResponse = new Dataflow();
    dataflowResponse.setId(2l);
    when(dataflowRepository.findByNameIgnoreCase(dataFlowVO.getName()))
        .thenReturn(Optional.of(dataflowResponse));
    try {
      dataflowServiceImpl.updateDataFlow(dataFlowVO);
    } catch (EEAException ex) {
      assertEquals(EEAErrorMessage.DATAFLOW_EXISTS_NAME, ex.getMessage());
      throw ex;
    }
  }

  /**
   * Update dataflow.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateDataflow() throws EEAException {
    DataFlowVO dataflowVO = new DataFlowVO();
    ObligationVO obligation = new ObligationVO();
    obligation.setObligationId(1);
    dataflowVO.setId(1L);
    dataflowVO.setName("test");
    dataflowVO.setObligation(obligation);
    when(dataflowRepository.findByNameIgnoreCase(dataflowVO.getName()))
        .thenReturn(Optional.empty());
    when(dataflowRepository.findById(dataflowVO.getId())).thenReturn(Optional.of(new Dataflow()));
    dataflowServiceImpl.updateDataFlow(dataflowVO);
    Mockito.verify(dataflowRepository, times(1)).save(Mockito.any());
  }

  /**
   * Test get datasets id.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testGetDatasetsId() throws EEAException {
    List<ReportingDatasetVO> reportings = new ArrayList<>();
    Mockito.when(datasetMetabaseController.getReportingsIdBySchemaId(Mockito.any()))
        .thenReturn(reportings);
    assertEquals("failed assertion", reportings,
        dataflowServiceImpl.getReportingDatasetsId("").getReportingDatasets());
  }


  /**
   * Test get datasets id error.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void testGetDatasetsIdError() throws EEAException {
    try {
      dataflowServiceImpl.getReportingDatasetsId(null);
    } catch (EEAException ex) {
      assertEquals(EEAErrorMessage.SCHEMA_NOT_FOUND, ex.getMessage());
      throw ex;
    }
  }


  /**
   * Gets the metabase by id throws.
   *
   * @return the metabase by id throws
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void getMetabaseByIdThrows() throws EEAException {
    try {
      dataflowServiceImpl.getMetabaseById(null);
    } catch (EEAException ex) {
      assertEquals(EEAErrorMessage.DATAFLOW_NOTFOUND, ex.getMessage());
      throw ex;
    }
  }


  /**
   * Gets the metabase by id.
   *
   * @return the metabase by id
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void getMetabaseById() throws EEAException {

    dataflowServiceImpl.getMetabaseById(1L);
    Mockito.verify(dataflowRepository, times(1)).findById(Mockito.any());
  }


  /**
   * Delete data flow.
   *
   * @throws Exception the exception
   */
  @Test
  public void deleteDataFlow() throws Exception {
    DataFlowVO dataFlowVO = new DataFlowVO();
    ReportingDatasetVO reportingDatasetVO = new ReportingDatasetVO();
    reportingDatasetVO.setId(1L);
    List<ReportingDatasetVO> reportingDatasetVOs = new ArrayList<>();
    reportingDatasetVOs.add(reportingDatasetVO);
    List<DesignDatasetVO> designDatasetVOs = new ArrayList<>();
    DesignDatasetVO designDatasetVO = new DesignDatasetVO();
    designDatasetVO.setId(1L);
    designDatasetVOs.add(designDatasetVO);
    DocumentVO document = new DocumentVO();
    document.setId(1L);
    List<DocumentVO> listDocument = new ArrayList<>();
    listDocument.add(document);
    dataFlowVO.setDocuments(listDocument);
    dataFlowVO.setReportingDatasets(reportingDatasetVOs);
    dataFlowVO.setDesignDatasets(designDatasetVOs);
    List<ResourceAccessVO> resourceList = new ArrayList<>();
    ResourceAccessVO resource = new ResourceAccessVO();
    resource.setId(1L);
    resourceList.add(resource);

    doNothing().when(datasetSchemaControllerZuul).deleteDatasetSchema(1L, true);
    when(userManagementControllerZull.getResourcesByUser(Mockito.any(ResourceTypeEnum.class)))
        .thenReturn(resourceList);
    when(dataflowMapper.entityToClass(Mockito.any())).thenReturn(dataFlowVO);
    when(datasetMetabaseController.findReportingDataSetIdByDataflowId(1L))
        .thenReturn(reportingDatasetVOs);
    when(datasetMetabaseController.findDesignDataSetIdByDataflowId(1L))
        .thenReturn(designDatasetVOs);
    when(dataflowRepository.findById(Mockito.any())).thenReturn(Optional.of(new Dataflow()));
    dataflowServiceImpl.deleteDataFlow(1L);

    Mockito.verify(dataflowRepository, times(1)).findById(Mockito.any());
  }

  /**
   * Delete data flow empty.
   *
   * @throws Exception the exception
   */
  @Test
  public void deleteDataFlowEmpty() throws Exception {
    DataFlowVO dataFlowVO = new DataFlowVO();
    when(userManagementControllerZull.getResourcesByUser(Mockito.any(ResourceTypeEnum.class)))
        .thenReturn(new ArrayList<>());
    when(dataflowMapper.entityToClass(Mockito.any())).thenReturn(dataFlowVO);
    when(datasetMetabaseController.findReportingDataSetIdByDataflowId(1L))
        .thenReturn(new ArrayList<>());
    when(datasetMetabaseController.findDesignDataSetIdByDataflowId(1L))
        .thenReturn(new ArrayList<>());
    when(dataflowRepository.findById(Mockito.any())).thenReturn(Optional.of(new Dataflow()));
    dataflowServiceImpl.deleteDataFlow(1L);

    Mockito.verify(dataflowRepository, times(1)).findById(Mockito.any());
  }

  /**
   * Delete data flow throws documents.
   *
   * @throws Exception the exception
   */
  @Test(expected = EEAException.class)
  public void deleteDataFlowThrowsDocuments() throws Exception {

    Dataflow dataflow = new Dataflow();
    List<Document> listDocu = new ArrayList<>();
    Document docus = new Document();
    docus.setId(1L);
    listDocu.add(docus);
    dataflow.setId(1L);
    dataflow.setDocuments(listDocu);
    DataFlowVO dataFlowVO = new DataFlowVO();
    ReportingDatasetVO reportingDatasetVO = new ReportingDatasetVO();
    reportingDatasetVO.setId(1L);
    List<ReportingDatasetVO> reportingDatasetVOs = new ArrayList<>();
    reportingDatasetVOs.add(reportingDatasetVO);
    List<DesignDatasetVO> designDatasetVOs = new ArrayList<>();
    DesignDatasetVO designDatasetVO = new DesignDatasetVO();
    designDatasetVO.setId(1L);
    designDatasetVOs.add(designDatasetVO);
    DocumentVO document = new DocumentVO();
    document.setId(1L);
    List<DocumentVO> listDocument = new ArrayList<>();
    listDocument.add(document);
    dataFlowVO.setDocuments(listDocument);
    dataFlowVO.setReportingDatasets(reportingDatasetVOs);
    dataFlowVO.setDesignDatasets(designDatasetVOs);

    doThrow(EEAException.class).when(documentControllerZuul).deleteDocument(1L, Boolean.TRUE);
    when(dataflowMapper.entityToClass(Mockito.any())).thenReturn(dataFlowVO);
    when(datasetMetabaseController.findReportingDataSetIdByDataflowId(1L))
        .thenReturn(new ArrayList<>());
    when(datasetMetabaseController.findDesignDataSetIdByDataflowId(1L))
        .thenReturn(new ArrayList<>());
    when(dataflowRepository.findById(1L)).thenReturn(Optional.of(dataflow));

    try {
      dataflowServiceImpl.deleteDataFlow(1L);
    } catch (EEAException ex) {
      assertEquals("Error Deleting document null with 1", ex.getMessage());
      throw ex;
    }
  }

  /**
   * Delete data flow throws dataset schema.
   *
   * @throws Exception the exception
   */
  @Test(expected = EEAException.class)
  public void deleteDataFlowThrowsDatasetSchema() throws Exception {
    DataFlowVO dataFlowVO = new DataFlowVO();
    ReportingDatasetVO reportingDatasetVO = new ReportingDatasetVO();
    reportingDatasetVO.setId(1L);
    List<ReportingDatasetVO> reportingDatasetVOs = new ArrayList<>();
    reportingDatasetVOs.add(reportingDatasetVO);
    List<DesignDatasetVO> designDatasetVOs = new ArrayList<>();
    DesignDatasetVO designDatasetVO = new DesignDatasetVO();
    designDatasetVO.setId(1L);
    designDatasetVOs.add(designDatasetVO);
    DocumentVO document = new DocumentVO();
    document.setId(1L);
    List<DocumentVO> listDocument = new ArrayList<>();
    listDocument.add(document);
    dataFlowVO.setDocuments(listDocument);
    dataFlowVO.setReportingDatasets(reportingDatasetVOs);
    dataFlowVO.setDesignDatasets(designDatasetVOs);
    List<ResourceAccessVO> resourceList = new ArrayList<>();
    ResourceAccessVO resource = new ResourceAccessVO();
    resource.setId(1L);
    resourceList.add(resource);

    when(userManagementControllerZull.getResourcesByUser(Mockito.any(ResourceTypeEnum.class)))
        .thenReturn(resourceList);
    when(dataflowMapper.entityToClass(Mockito.any())).thenReturn(dataFlowVO);
    when(datasetMetabaseController.findReportingDataSetIdByDataflowId(1L))
        .thenReturn(reportingDatasetVOs);
    when(datasetMetabaseController.findDesignDataSetIdByDataflowId(1L))
        .thenReturn(designDatasetVOs);
    when(dataflowRepository.findById(Mockito.any())).thenReturn(Optional.of(new Dataflow()));
    doThrow(MockitoException.class).when(datasetSchemaControllerZuul).deleteDatasetSchema(1L, true);
    try {
      dataflowServiceImpl.deleteDataFlow(1L);
    } catch (EEAException ex) {
      assertEquals("Error Deleting dataset null with 1", ex.getMessage());
      throw ex;
    }
  }

  /**
   * Delete data flow throws delete dataflow.
   *
   * @throws Exception the exception
   */
  @Test(expected = EEAException.class)
  public void deleteDataFlowThrowsDeleteDataflow() throws Exception {
    DataFlowVO dataFlowVO = new DataFlowVO();
    ReportingDatasetVO reportingDatasetVO = new ReportingDatasetVO();
    reportingDatasetVO.setId(1L);
    List<ReportingDatasetVO> reportingDatasetVOs = new ArrayList<>();
    reportingDatasetVOs.add(reportingDatasetVO);
    List<DesignDatasetVO> designDatasetVOs = new ArrayList<>();
    DesignDatasetVO designDatasetVO = new DesignDatasetVO();
    designDatasetVO.setId(1L);
    designDatasetVOs.add(designDatasetVO);
    DocumentVO document = new DocumentVO();
    document.setId(1L);
    List<DocumentVO> listDocument = new ArrayList<>();
    listDocument.add(document);
    dataFlowVO.setDocuments(listDocument);
    dataFlowVO.setReportingDatasets(reportingDatasetVOs);
    dataFlowVO.setDesignDatasets(designDatasetVOs);
    List<ResourceAccessVO> resourceList = new ArrayList<>();
    ResourceAccessVO resource = new ResourceAccessVO();
    resource.setId(1L);
    resourceList.add(resource);
    DataCollectionVO dcVO = new DataCollectionVO();
    dcVO.setId(1L);
    dataFlowVO.setDataCollections(Arrays.asList(dcVO));
    EUDatasetVO euDatasetVO = new EUDatasetVO();
    euDatasetVO.setId(3L);
    List<EUDatasetVO> euDatasetVOs = new ArrayList<>();
    euDatasetVOs.add(euDatasetVO);

    Dataflow dataflowEntity = new Dataflow();
    Set<Representative> representatives = new HashSet<>();
    Representative representative = new Representative();
    representative.setId(1L);
    representatives.add(representative);
    dataflowEntity.setRepresentatives(representatives);
    when(userManagementControllerZull.getResourcesByUser(Mockito.any(ResourceTypeEnum.class)))
        .thenReturn(resourceList);
    when(dataflowMapper.entityToClass(Mockito.any())).thenReturn(dataFlowVO);
    when(datasetMetabaseController.findReportingDataSetIdByDataflowId(1L))
        .thenReturn(reportingDatasetVOs);
    when(datasetMetabaseController.findDesignDataSetIdByDataflowId(1L))
        .thenReturn(designDatasetVOs);
    when(dataCollectionControllerZuul.findDataCollectionIdByDataflowId(1L))
        .thenReturn(Arrays.asList(dcVO));
    when(euDatasetControllerZuul.findEUDatasetByDataflowId(1L)).thenReturn(euDatasetVOs);
    when(dataflowRepository.findById(Mockito.any())).thenReturn(Optional.of(dataflowEntity));
    dataflowServiceImpl.deleteDataFlow(1L);
    doThrow(MockitoException.class).when(dataflowRepository).deleteNativeDataflow(Mockito.any());
    try {
      dataflowServiceImpl.deleteDataFlow(1L);
    } catch (EEAException ex) {
      assertEquals("Error Deleting dataflow ", ex.getMessage());
      throw ex;
    }
  }

  /**
   * Delete data flow throws delete representative.
   *
   * @throws Exception the exception
   */
  @Test(expected = EEAException.class)
  public void deleteDataFlowThrowsDeleteRepresentative() throws Exception {
    DataFlowVO dataflowVO = new DataFlowVO();
    List<RepresentativeVO> representatives = new ArrayList<>();
    RepresentativeVO representative = new RepresentativeVO();
    representatives.add(representative);
    Mockito.when(dataflowRepository.findById(Mockito.any())).thenReturn(Optional.empty());
    Mockito
        .when(userManagementControllerZull.getResourcesByUser(Mockito.any(ResourceTypeEnum.class)))
        .thenReturn(new ArrayList<ResourceAccessVO>());
    Mockito.when(dataflowMapper.entityToClass(Mockito.any())).thenReturn(dataflowVO);
    Mockito.when(datasetMetabaseController.findReportingDataSetIdByDataflowId(Mockito.any()))
        .thenReturn(new ArrayList<ReportingDatasetVO>());
    Mockito.when(datasetMetabaseController.findDesignDataSetIdByDataflowId(Mockito.any()))
        .thenReturn(new ArrayList<DesignDatasetVO>());
    Mockito.when(dataCollectionControllerZuul.findDataCollectionIdByDataflowId(Mockito.any()))
        .thenReturn(new ArrayList<DataCollectionVO>());
    Mockito.when(representativeService.getRepresetativesByIdDataFlow(Mockito.any()))
        .thenReturn(representatives);
    Mockito.doThrow(IllegalArgumentException.class).when(representativeRepository)
        .deleteById(Mockito.any());
    try {
      dataflowServiceImpl.deleteDataFlow(1L);
    } catch (EEAException e) {
      Assert.assertTrue(e.getMessage().startsWith("Error Deleting representative"));
      throw e;
    }
  }

  /**
   * Delete data flow throws resource.
   *
   * @throws Exception the exception
   */
  @Test(expected = EEAException.class)
  public void deleteDataFlowThrowsResource() throws Exception {
    DataFlowVO dataFlowVO = new DataFlowVO();
    ReportingDatasetVO reportingDatasetVO = new ReportingDatasetVO();
    reportingDatasetVO.setId(1L);
    List<ReportingDatasetVO> reportingDatasetVOs = new ArrayList<>();
    reportingDatasetVOs.add(reportingDatasetVO);
    List<DesignDatasetVO> designDatasetVOs = new ArrayList<>();
    DesignDatasetVO designDatasetVO = new DesignDatasetVO();
    designDatasetVO.setId(1L);
    designDatasetVOs.add(designDatasetVO);
    DocumentVO document = new DocumentVO();
    document.setId(1L);
    List<DocumentVO> listDocument = new ArrayList<>();
    listDocument.add(document);
    dataFlowVO.setDocuments(listDocument);
    dataFlowVO.setReportingDatasets(reportingDatasetVOs);
    dataFlowVO.setDesignDatasets(designDatasetVOs);
    List<ResourceAccessVO> resourceList = new ArrayList<>();
    ResourceAccessVO resource = new ResourceAccessVO();
    resource.setId(1L);
    resourceList.add(resource);

    when(userManagementControllerZull.getResourcesByUser(Mockito.any(ResourceTypeEnum.class)))
        .thenReturn(resourceList);
    when(dataflowMapper.entityToClass(Mockito.any())).thenReturn(dataFlowVO);
    when(datasetMetabaseController.findReportingDataSetIdByDataflowId(1L))
        .thenReturn(reportingDatasetVOs);
    when(datasetMetabaseController.findDesignDataSetIdByDataflowId(1L))
        .thenReturn(designDatasetVOs);
    when(dataflowRepository.findById(Mockito.any())).thenReturn(Optional.of(new Dataflow()));
    doThrow(MockitoException.class).when(resourceManagementControllerZull)
        .deleteResource(Mockito.any());
    try {
      dataflowServiceImpl.deleteDataFlow(1L);
    } catch (EEAException ex) {
      assertEquals("Error deleting resource in keycloack ", ex.getMessage());
      throw ex;
    }
  }

  /**
   * Test update dataflow status.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testUpdateDataflowStatus() throws EEAException {

    when(dataflowRepository.findById(Mockito.any())).thenReturn(Optional.of(new Dataflow()));
    dataflowServiceImpl.updateDataFlowStatus(1L, TypeStatusEnum.DESIGN, null);
    Mockito.verify(dataflowRepository, times(1)).save(Mockito.any());
  }

  /**
   * Test update dataflow status exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void testUpdateDataflowStatusException() throws EEAException {
    try {
      dataflowServiceImpl.updateDataFlowStatus(1L, TypeStatusEnum.DESIGN, null);
    } catch (EEAException e) {
      assertEquals(EEAErrorMessage.DATAFLOW_NOTFOUND, e.getMessage());
      throw e;
    }
  }

  /**
   * Creates the message test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createMessageLeadReporterTest() throws EEAException {
    Collection<SimpleGrantedAuthority> authorities = new HashSet<>();
    authorities.add(
        new SimpleGrantedAuthority(ObjectAccessRoleEnum.DATAFLOW_LEAD_REPORTER.getAccessRole(1L)));
    authorities.add(
        new SimpleGrantedAuthority(ObjectAccessRoleEnum.DATASET_LEAD_REPORTER.getAccessRole(1L)));
    Mockito
        .when(datasetMetabaseController
            .getDatasetIdsByDataflowIdAndDataProviderId(Mockito.anyLong(), Mockito.anyLong()))
        .thenReturn(Arrays.asList(1L));
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.doReturn(authorities).when(authentication).getAuthorities();
    Mockito.when(messageRepository.save(Mockito.any())).thenReturn(new Message());
    Mockito.when(messageMapper.entityToClass(Mockito.any())).thenReturn(new MessageVO());
    Assert.assertNotNull(dataflowServiceImpl.createMessage(1L, 1L, "content"));
  }

  /**
   * Creates the message reporter read test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createMessageReporterReadTest() throws EEAException {
    Collection<SimpleGrantedAuthority> authorities = new HashSet<>();
    authorities.add(
        new SimpleGrantedAuthority(ObjectAccessRoleEnum.DATAFLOW_REPORTER_READ.getAccessRole(1L)));
    authorities.add(
        new SimpleGrantedAuthority(ObjectAccessRoleEnum.DATASET_REPORTER_READ.getAccessRole(1L)));
    Mockito
        .when(datasetMetabaseController
            .getDatasetIdsByDataflowIdAndDataProviderId(Mockito.anyLong(), Mockito.anyLong()))
        .thenReturn(Arrays.asList(1L));
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.doReturn(authorities).when(authentication).getAuthorities();
    Mockito.when(messageRepository.save(Mockito.any())).thenReturn(new Message());
    Mockito.when(messageMapper.entityToClass(Mockito.any())).thenReturn(new MessageVO());
    Assert.assertNotNull(dataflowServiceImpl.createMessage(1L, 1L, "content"));
  }

  /**
   * Creates the message reporter write test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createMessageReporterWriteTest() throws EEAException {
    Collection<SimpleGrantedAuthority> authorities = new HashSet<>();
    authorities.add(
        new SimpleGrantedAuthority(ObjectAccessRoleEnum.DATAFLOW_REPORTER_WRITE.getAccessRole(1L)));
    authorities.add(
        new SimpleGrantedAuthority(ObjectAccessRoleEnum.DATASET_REPORTER_WRITE.getAccessRole(1L)));
    Mockito
        .when(datasetMetabaseController
            .getDatasetIdsByDataflowIdAndDataProviderId(Mockito.anyLong(), Mockito.anyLong()))
        .thenReturn(Arrays.asList(1L));
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.doReturn(authorities).when(authentication).getAuthorities();
    Mockito.when(messageRepository.save(Mockito.any())).thenReturn(new Message());
    Mockito.when(messageMapper.entityToClass(Mockito.any())).thenReturn(new MessageVO());
    Assert.assertNotNull(dataflowServiceImpl.createMessage(1L, 1L, "content"));
  }

  /**
   * Creates the message custodian test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createMessageCustodianTest() throws EEAException {
    Collection<SimpleGrantedAuthority> authorities = new HashSet<>();
    authorities
        .add(new SimpleGrantedAuthority(ObjectAccessRoleEnum.DATAFLOW_CUSTODIAN.getAccessRole(1L)));
    Mockito
        .when(datasetMetabaseController
            .getDatasetIdsByDataflowIdAndDataProviderId(Mockito.anyLong(), Mockito.anyLong()))
        .thenReturn(Arrays.asList(1L));
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.doReturn(authorities).when(authentication).getAuthorities();
    Mockito.when(messageRepository.save(Mockito.any())).thenReturn(new Message());
    Mockito.when(messageMapper.entityToClass(Mockito.any())).thenReturn(new MessageVO());
    Assert.assertNotNull(dataflowServiceImpl.createMessage(1L, 1L, "content"));
  }

  /**
   * Creates the message reporter excpetion test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void createMessageReporterExcpetionTest() throws EEAException {
    Collection<SimpleGrantedAuthority> authorities = new HashSet<>();
    authorities.add(
        new SimpleGrantedAuthority(ObjectAccessRoleEnum.DATAFLOW_LEAD_REPORTER.getAccessRole(1L)));
    Mockito
        .when(datasetMetabaseController
            .getDatasetIdsByDataflowIdAndDataProviderId(Mockito.anyLong(), Mockito.anyLong()))
        .thenReturn(Arrays.asList(1L));
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.doReturn(authorities).when(authentication).getAuthorities();
    try {
      dataflowServiceImpl.createMessage(1L, 1L, "content");
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.MESSAGING_AUTHORIZATION_FAILED, e.getMessage());
      throw e;
    }
  }

  /**
   * Creates the message excpetion test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void createMessageExcpetionTest() throws EEAException {
    Mockito
        .when(datasetMetabaseController
            .getDatasetIdsByDataflowIdAndDataProviderId(Mockito.anyLong(), Mockito.anyLong()))
        .thenReturn(null);
    try {
      dataflowServiceImpl.createMessage(1L, 1L, "content");
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.MESSAGING_AUTHORIZATION_FAILED, e.getMessage());
      throw e;
    }
  }

  /**
   * Creates the message empty dataset list excpetion test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void createMessageEmptyDatasetListExcpetionTest() throws EEAException {
    Mockito
        .when(datasetMetabaseController
            .getDatasetIdsByDataflowIdAndDataProviderId(Mockito.anyLong(), Mockito.anyLong()))
        .thenReturn(new ArrayList<Long>());
    try {
      dataflowServiceImpl.createMessage(1L, 1L, "content");
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.MESSAGING_AUTHORIZATION_FAILED, e.getMessage());
      throw e;
    }
  }

  /**
   * Find messages read test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void findMessagesReadTest() throws EEAException {
    Page<Message> pageResponse = Mockito.mock(Page.class);
    Collection<SimpleGrantedAuthority> authorities = new HashSet<>();
    authorities
        .add(new SimpleGrantedAuthority(ObjectAccessRoleEnum.DATAFLOW_CUSTODIAN.getAccessRole(1L)));
    Mockito
        .when(datasetMetabaseController
            .getDatasetIdsByDataflowIdAndDataProviderId(Mockito.anyLong(), Mockito.anyLong()))
        .thenReturn(Arrays.asList(1L));
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.doReturn(authorities).when(authentication).getAuthorities();
    Mockito.when(messageRepository.findByDataflowIdAndProviderIdAndRead(Mockito.anyLong(),
        Mockito.anyLong(), Mockito.anyBoolean(), Mockito.any())).thenReturn(pageResponse);
    Mockito.when(pageResponse.getContent()).thenReturn(new ArrayList<Message>());
    Mockito.when(messageMapper.entityListToClass(Mockito.any()))
        .thenReturn(new ArrayList<MessageVO>());
    Assert.assertNotNull(dataflowServiceImpl.findMessages(1L, 1L, true, 1));
  }

  /**
   * Find messages test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void findMessagesTest() throws EEAException {
    Page<Message> pageResponse = Mockito.mock(Page.class);
    Collection<SimpleGrantedAuthority> authorities = new HashSet<>();
    authorities
        .add(new SimpleGrantedAuthority(ObjectAccessRoleEnum.DATAFLOW_CUSTODIAN.getAccessRole(1L)));
    Mockito
        .when(datasetMetabaseController
            .getDatasetIdsByDataflowIdAndDataProviderId(Mockito.anyLong(), Mockito.anyLong()))
        .thenReturn(Arrays.asList(1L));
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.doReturn(authorities).when(authentication).getAuthorities();
    Mockito.when(messageRepository.findByDataflowIdAndProviderId(Mockito.anyLong(),
        Mockito.anyLong(), Mockito.any())).thenReturn(pageResponse);
    Mockito.when(pageResponse.getContent()).thenReturn(new ArrayList<Message>());
    Mockito.when(messageMapper.entityListToClass(Mockito.any()))
        .thenReturn(new ArrayList<MessageVO>());
    Assert.assertNotNull(dataflowServiceImpl.findMessages(1L, 1L, null, 1));
  }

  /**
   * Update message read status steward test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateMessageReadStatusStewardTest() throws EEAException {
    MessageVO messageVO = new MessageVO();
    messageVO.setId(1L);
    messageVO.setRead(false);
    messageVO.setDirection(true);
    List<MessageVO> messageVOs = new ArrayList<>();
    messageVOs.add(messageVO);
    Message message = new Message();
    message.setId(1L);
    message.setRead(true);
    message.setDirection(true);
    List<Message> messages = new ArrayList<>();
    messages.add(message);
    Collection<SimpleGrantedAuthority> authorities = new HashSet<>();
    authorities
        .add(new SimpleGrantedAuthority(ObjectAccessRoleEnum.DATAFLOW_STEWARD.getAccessRole(1L)));
    Mockito.when(messageRepository.findByDataflowIdAndIdIn(Mockito.anyLong(), Mockito.any()))
        .thenReturn(messages);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.doReturn(authorities).when(authentication).getAuthorities();
    dataflowServiceImpl.updateMessageReadStatus(1L, messageVOs);
    Mockito.verify(messageRepository, times(1)).saveAll(Mockito.anyIterable());
  }

  /**
   * Update message read status custodian test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateMessageReadStatusCustodianTest() throws EEAException {
    MessageVO messageVO = new MessageVO();
    messageVO.setId(1L);
    messageVO.setRead(false);
    messageVO.setDirection(true);
    List<MessageVO> messageVOs = new ArrayList<>();
    messageVOs.add(messageVO);
    Message message = new Message();
    message.setId(1L);
    message.setRead(true);
    message.setDirection(true);
    List<Message> messages = new ArrayList<>();
    messages.add(message);
    Collection<SimpleGrantedAuthority> authorities = new HashSet<>();
    authorities
        .add(new SimpleGrantedAuthority(ObjectAccessRoleEnum.DATAFLOW_CUSTODIAN.getAccessRole(1L)));
    Mockito.when(messageRepository.findByDataflowIdAndIdIn(Mockito.anyLong(), Mockito.any()))
        .thenReturn(messages);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.doReturn(authorities).when(authentication).getAuthorities();
    dataflowServiceImpl.updateMessageReadStatus(1L, messageVOs);
    Mockito.verify(messageRepository, times(1)).saveAll(Mockito.anyIterable());
  }

  /**
   * Update message read status exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void updateMessageReadStatusExceptionTest() throws EEAException {
    List<Long> providerIds = new ArrayList<>();
    providerIds.add(1L);
    MessageVO messageVO1 = new MessageVO();
    messageVO1.setId(1L);
    messageVO1.setDirection(false);
    messageVO1.setProviderId(1L);
    MessageVO messageVO2 = new MessageVO();
    messageVO2.setId(2L);
    messageVO2.setDirection(true);
    messageVO2.setProviderId(1L);
    MessageVO messageVO3 = new MessageVO();
    messageVO3.setId(3L);
    messageVO3.setDirection(false);
    messageVO3.setProviderId(2L);
    List<MessageVO> messageVOs = new ArrayList<>();
    messageVOs.add(messageVO1);
    messageVOs.add(messageVO2);
    messageVOs.add(messageVO3);
    Message message1 = new Message();
    message1.setId(1L);
    message1.setDirection(false);
    message1.setProviderId(1L);
    Message message2 = new Message();
    message2.setId(2L);
    message2.setDirection(true);
    message2.setProviderId(1L);
    Message message3 = new Message();
    message3.setId(3L);
    message3.setDirection(false);
    message3.setProviderId(2L);
    List<Message> messages = new ArrayList<>();
    messages.add(message1);
    messages.add(message2);
    messages.add(message3);
    Collection<SimpleGrantedAuthority> authorities = new HashSet<>();
    authorities.add(
        new SimpleGrantedAuthority(ObjectAccessRoleEnum.DATAFLOW_LEAD_REPORTER.getAccessRole(1L)));
    Mockito.when(messageRepository.findByDataflowIdAndIdIn(Mockito.anyLong(), Mockito.any()))
        .thenReturn(messages);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.doReturn(authorities).when(authentication).getAuthorities();
    Mockito.when(datasetMetabaseController.getUserProviderIdsByDataflowId(Mockito.anyLong()))
        .thenReturn(providerIds);
    try {
      dataflowServiceImpl.updateMessageReadStatus(1L, messageVOs);
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.MESSAGING_AUTHORIZATION_FAILED, e.getMessage());
      throw e;
    }
  }
}
