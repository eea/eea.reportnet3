package org.eea.dataflow.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.eea.dataflow.mapper.DataflowMapper;
import org.eea.dataflow.mapper.DataflowNoContentMapper;
import org.eea.dataflow.mapper.DocumentMapper;
import org.eea.dataflow.persistence.domain.Contributor;
import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.dataflow.persistence.domain.DataflowWithRequestType;
import org.eea.dataflow.persistence.domain.Document;
import org.eea.dataflow.persistence.domain.Representative;
import org.eea.dataflow.persistence.domain.UserRequest;
import org.eea.dataflow.persistence.repository.ContributorRepository;
import org.eea.dataflow.persistence.repository.DataflowRepository;
import org.eea.dataflow.persistence.repository.DocumentRepository;
import org.eea.dataflow.persistence.repository.RepresentativeRepository;
import org.eea.dataflow.persistence.repository.UserRequestRepository;
import org.eea.dataflow.service.impl.DataflowServiceImpl;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DataCollectionController.DataCollectionControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSchemaController.DataSetSchemaControllerZuul;
import org.eea.interfaces.controller.document.DocumentController.DocumentControllerZuul;
import org.eea.interfaces.controller.rod.ObligationController;
import org.eea.interfaces.controller.ums.ResourceManagementController.ResourceManagementControllerZull;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.dataflow.enums.TypeRequestEnum;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.eea.interfaces.vo.dataset.DataCollectionVO;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
import org.eea.interfaces.vo.dataset.ReportingDatasetVO;
import org.eea.interfaces.vo.document.DocumentVO;
import org.eea.interfaces.vo.rod.ObligationVO;
import org.eea.interfaces.vo.ums.ResourceAccessVO;
import org.eea.interfaces.vo.ums.enums.ResourceTypeEnum;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * The Class DataFlowServiceImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class DataFlowServiceImplTest {

  /**
   * The dataflow service impl.
   */
  @InjectMocks
  private DataflowServiceImpl dataflowServiceImpl;

  /**
   * The dataflow repository.
   */
  @Mock
  private DataflowRepository dataflowRepository;


  /**
   * The user request repository.
   */
  @Mock
  private UserRequestRepository userRequestRepository;

  /**
   * The contributor repository.
   */
  @Mock
  private ContributorRepository contributorRepository;

  /**
   * The document repository.
   */
  @Mock
  private DocumentRepository documentRepository;

  /**
   * The dataflow mapper.
   */
  @Mock
  private DataflowMapper dataflowMapper;

  /**
   * The dataflow no content mapper.
   */
  @Mock
  private DataflowNoContentMapper dataflowNoContentMapper;

  /**
   * The dataset controller.
   */
  @Mock
  private DataSetMetabaseControllerZuul datasetMetabaseController;

  /**
   * The user management controller zull.
   */
  @Mock
  private UserManagementControllerZull userManagementControllerZull;

  /**
   * The resource management controller zull.
   */
  @Mock
  private ResourceManagementControllerZull resourceManagementControllerZull;
  /**
   * The document mapper.
   */
  @Mock
  private DocumentMapper documentMapper;

  /**
   * The data set schema controller zuul.
   */
  @Mock
  private DataSetSchemaControllerZuul dataSetSchemaControllerZuul;

  /**
   * The document controller zuul.
   */
  @Mock
  private DocumentControllerZuul documentControllerZuul;


  @Mock
  private DataCollectionControllerZuul dataCollectionControllerZuul;

  @Mock
  private RepresentativeRepository representativeRepository;

  @Mock
  private RepresentativeService representativeService;

  @Mock
  private ObligationController obligationController;

  /**
   * The dataflows.
   */
  private List<Dataflow> dataflows;

  /**
   * The pageable.
   */
  private Pageable pageable;


  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    dataflows = new ArrayList<>();
    dataflows.add(new Dataflow());
    pageable = PageRequest.of(1, 1);
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
  public void getPendingAccepted() throws EEAException {
    List<DataflowWithRequestType> dataflows = new ArrayList<>();
    Dataflow dataflow = new Dataflow();
    dataflow.setId(1L);
    DataflowWithRequestType df = new DataflowWithRequestType() {

      @Override
      public TypeRequestEnum getTypeRequestEnum() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public Long getRequestId() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public Dataflow getDataflow() {
        // TODO Auto-generated method stub
        return dataflow;
      }
    };
    dataflows.add(df);
    when(dataflowRepository.findPending(Mockito.any())).thenReturn(dataflows);

    DataFlowVO dfVO = new DataFlowVO();
    dfVO.setId(1L);
    List<DataFlowVO> dataflowsVO = new ArrayList<>();
    dataflowsVO.add(dfVO);
    when(dataflowNoContentMapper.entityListToClass(Mockito.any())).thenReturn(dataflowsVO);

    ResourceAccessVO resource = new ResourceAccessVO();
    resource.setId(1L);
    List<ResourceAccessVO> resources = new ArrayList<>();
    resources.add(resource);
    when(userManagementControllerZull.getResourcesByUser(Mockito.any(ResourceTypeEnum.class)))
        .thenReturn(resources);

    Optional<Dataflow> df2 = Optional.of(df.getDataflow());

    dataflowServiceImpl.getPendingAccepted(Mockito.any());
    List<Dataflow> list = new ArrayList<>();
    list.add(new Dataflow());
    Mockito.when(dataflowRepository.findAllById(Mockito.any())).thenReturn(list);
    Mockito.when(dataflowNoContentMapper.entityToClass(Mockito.any())).thenReturn(new DataFlowVO());
    assertEquals("fail", dataflowsVO, dataflowServiceImpl.getPendingAccepted(Mockito.any()));
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
    Mockito.verify(resourceManagementControllerZull, times(2)).createResource(Mockito.any());
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

    dataflowServiceImpl.getReportingDatasetsId("");
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

    doNothing().when(dataSetSchemaControllerZuul).deleteDatasetSchema(1L, true);
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
    doThrow(MockitoException.class).when(dataSetSchemaControllerZuul).deleteDatasetSchema(1L, true);
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

  @Test(expected = EEAException.class)
  public void deleteDataFlowThrowsDeleteRepresentative() throws Exception {
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
    List<RepresentativeVO> representatives = new ArrayList<>();
    RepresentativeVO representative = new RepresentativeVO();
    representative.setId(1L);
    representatives.add(representative);
    dataFlowVO.setRepresentatives(representatives);
    when(userManagementControllerZull.getResourcesByUser(Mockito.any(ResourceTypeEnum.class)))
        .thenReturn(resourceList);
    when(dataflowMapper.entityToClass(Mockito.any())).thenReturn(dataFlowVO);
    when(datasetMetabaseController.findReportingDataSetIdByDataflowId(1L))
        .thenReturn(reportingDatasetVOs);
    when(datasetMetabaseController.findDesignDataSetIdByDataflowId(1L))
        .thenReturn(designDatasetVOs);
    when(dataCollectionControllerZuul.findDataCollectionIdByDataflowId(1L))
        .thenReturn(Arrays.asList(dcVO));
    when(dataflowRepository.findById(Mockito.any())).thenReturn(Optional.of(new Dataflow()));
    dataflowServiceImpl.deleteDataFlow(1L);
    doThrow(MockitoException.class).when(representativeRepository).deleteById(Mockito.anyLong());
    try {
      dataflowServiceImpl.deleteDataFlow(1L);
    } catch (EEAException ex) {
      assertEquals("Error Deleting representative with id 1", ex.getMessage());
      throw ex;
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


  @Test
  public void testUpdateDataflowStatus() throws EEAException {

    when(dataflowRepository.findById(Mockito.any())).thenReturn(Optional.of(new Dataflow()));
    dataflowServiceImpl.updateDataFlowStatus(1L, TypeStatusEnum.DESIGN, null);
    Mockito.verify(dataflowRepository, times(1)).save(Mockito.any());
  }


  @Test(expected = EEAException.class)
  public void testUpdateDataflowStatusException() throws EEAException {
    try {
      dataflowServiceImpl.updateDataFlowStatus(1L, TypeStatusEnum.DESIGN, null);
    } catch (EEAException e) {
      assertEquals(EEAErrorMessage.DATAFLOW_NOTFOUND, e.getMessage());
      throw e;
    }
  }
}
