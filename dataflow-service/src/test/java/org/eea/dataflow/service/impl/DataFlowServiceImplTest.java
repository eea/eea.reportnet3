package org.eea.dataflow.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.eea.dataflow.mapper.DataflowMapper;
import org.eea.dataflow.mapper.DataflowNoContentMapper;
import org.eea.dataflow.mapper.DataflowPublicMapper;
import org.eea.dataflow.mapper.DocumentMapper;
import org.eea.dataflow.persistence.domain.Contributor;
import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.dataflow.persistence.domain.Document;
import org.eea.dataflow.persistence.domain.Representative;
import org.eea.dataflow.persistence.repository.ContributorRepository;
import org.eea.dataflow.persistence.repository.DataProviderGroupRepository;
import org.eea.dataflow.persistence.repository.DataProviderRepository;
import org.eea.dataflow.persistence.repository.DataflowRepository;
import org.eea.dataflow.persistence.repository.DataflowRepository.IDatasetStatus;
import org.eea.dataflow.persistence.repository.DocumentRepository;
import org.eea.dataflow.persistence.repository.FMEUserRepository;
import org.eea.dataflow.persistence.repository.RepresentativeRepository;
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
import org.eea.interfaces.vo.dataflow.DataflowPublicVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.dataflow.enums.TypeDataflowEnum;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.eea.interfaces.vo.dataset.DataCollectionVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
import org.eea.interfaces.vo.dataset.EUDatasetVO;
import org.eea.interfaces.vo.dataset.ReportingDatasetVO;
import org.eea.interfaces.vo.dataset.TestDatasetVO;
import org.eea.interfaces.vo.dataset.enums.DatasetStatusEnum;
import org.eea.interfaces.vo.document.DocumentVO;
import org.eea.interfaces.vo.enums.EntityClassEnum;
import org.eea.interfaces.vo.rod.ObligationVO;
import org.eea.interfaces.vo.ums.ResourceAccessVO;
import org.eea.interfaces.vo.ums.enums.ResourceTypeEnum;
import org.eea.interfaces.vo.weblink.WeblinkVO;
import org.eea.kafka.utils.KafkaSenderUtils;
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
  private ObligationControllerZull obligationControllerZull;

  /** The eu dataset controller zuul. */
  @Mock
  private EUDatasetControllerZuul euDatasetControllerZuul;

  /** The dataflow public mapper. */
  @Mock
  private DataflowPublicMapper dataflowPublicMapper;

  /** The data set controller zuul. */
  @Mock
  private DataSetControllerZuul dataSetControllerZuul;

  /** The test data set controller zuul. */
  @Mock
  private TestDatasetControllerZuul testDataSetControllerZuul;

  /** The reference controller zuul. */
  @Mock
  private ReferenceDatasetControllerZuul referenceControllerZuul;

  @Mock
  private DataProviderGroupRepository dataProviderGroupRepository;

  @Mock
  private FMEUserRepository fmeUserRepository;
  /**
   * The kafka sender utils.
   */
  @Mock
  private KafkaSenderUtils kafkaSenderUtils;

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
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("name");
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
    List<WeblinkVO> weblinks = new ArrayList<>();
    WeblinkVO weblinkVO = new WeblinkVO();
    weblinkVO.setDescription("bbbb");
    WeblinkVO weblinkVO2 = new WeblinkVO();
    weblinkVO2.setDescription("aaa");
    weblinks.add(weblinkVO);
    weblinks.add(weblinkVO2);
    dataFlowVO.setStatus(TypeStatusEnum.DRAFT);

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("name");
    when(userManagementControllerZull.getResourcesByUser(Mockito.any(ResourceTypeEnum.class)))
        .thenReturn(new ArrayList<>());
    when(dataflowRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(new Dataflow()));
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
    dataFlowVO.setWeblinks(weblinks);
    assertEquals("fail", dataFlowVO, dataflowServiceImpl.getById(1L));
  }

  @Test
  public void getByIdBusinessTest() throws EEAException {
    DataFlowVO dataFlowVO = new DataFlowVO();
    ReportingDatasetVO reportingDatasetVO = new ReportingDatasetVO();
    reportingDatasetVO.setId(1L);
    List<ReportingDatasetVO> reportingDatasetVOs = new ArrayList<>();
    reportingDatasetVOs.add(reportingDatasetVO);
    List<DesignDatasetVO> designDatasetVOs = new ArrayList<>();
    DesignDatasetVO designDatasetVO = new DesignDatasetVO();
    designDatasetVOs.add(designDatasetVO);
    List<WeblinkVO> weblinks = new ArrayList<>();
    WeblinkVO weblinkVO = new WeblinkVO();
    weblinkVO.setDescription("bbbb");
    WeblinkVO weblinkVO2 = new WeblinkVO();
    weblinkVO2.setDescription("aaa");
    weblinks.add(weblinkVO);
    weblinks.add(weblinkVO2);
    dataFlowVO.setStatus(TypeStatusEnum.DRAFT);
    dataFlowVO.setType(TypeDataflowEnum.BUSINESS);

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("name");
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
    when(dataProviderGroupRepository.findById(Mockito.any())).thenReturn(Optional.empty());
    when(fmeUserRepository.findById(Mockito.any())).thenReturn(Optional.empty());
    when(dataflowRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(new Dataflow()));
    dataFlowVO.setReportingDatasets(reportingDatasetVOs);
    dataFlowVO.setDesignDatasets(designDatasetVOs);
    ObligationVO obligation = new ObligationVO();
    obligation.setObligationId(1);
    dataFlowVO.setObligation(obligation);
    dataFlowVO.setWeblinks(weblinks);
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
    List<DataFlowVO> dataflowsVO = new ArrayList<>();
    dataflowsVO.add(dfVO);

    ResourceAccessVO resource = new ResourceAccessVO();
    resource.setId(1L);
    List<ResourceAccessVO> resources = new ArrayList<>();
    resources.add(resource);
    when(userManagementControllerZull.getResourcesByUser(Mockito.any(ResourceTypeEnum.class)))
        .thenReturn(resources);
    IDatasetStatus ida1 = new IDatasetStatus() {

      @Override
      public String getStatus() {
        return DatasetStatusEnum.CORRECTION_REQUESTED.toString();
      }

      @Override
      public Long getId() {
        return 1L;
      }
    };
    IDatasetStatus ida2 = new IDatasetStatus() {

      @Override
      public String getStatus() {
        return DatasetStatusEnum.PENDING.toString();
      }

      @Override
      public Long getId() {
        return 2L;
      }
    };
    List<IDatasetStatus> listObject = Arrays.asList(ida1, ida2);
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
    dataFlowVO.setStatus(TypeStatusEnum.DRAFT);
    List<ResourceAccessVO> resourceList = new ArrayList<>();
    ResourceAccessVO resource = new ResourceAccessVO();
    resource.setId(1L);
    resourceList.add(resource);

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("name");
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
    dataFlowVO.setStatus(TypeStatusEnum.DRAFT);

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("name");
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
  @Test
  public void deleteDataFlowDocumentsVerifyTest() throws Exception {

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
    dataFlowVO.setStatus(TypeStatusEnum.DRAFT);

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("name");
    doThrow(EEAException.class).when(documentControllerZuul).deleteDocument(1L, Boolean.TRUE);
    when(dataflowMapper.entityToClass(Mockito.any())).thenReturn(dataFlowVO);
    when(datasetMetabaseController.findReportingDataSetIdByDataflowId(1L))
        .thenReturn(new ArrayList<>());
    when(datasetMetabaseController.findDesignDataSetIdByDataflowId(1L))
        .thenReturn(new ArrayList<>());
    when(dataflowRepository.findById(1L)).thenReturn(Optional.of(dataflow));

    dataflowServiceImpl.deleteDataFlow(1L);
  }

  /**
   * Delete data flow throws dataset schema.
   *
   * @throws Exception the exception
   */
  @Test
  public void deleteDataFlowDeleteDatasetSchemaVerify() throws Exception {
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
    dataFlowVO.setStatus(TypeStatusEnum.DRAFT);
    List<ResourceAccessVO> resourceList = new ArrayList<>();
    ResourceAccessVO resource = new ResourceAccessVO();
    resource.setId(1L);
    resourceList.add(resource);

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("name");
    when(userManagementControllerZull.getResourcesByUser(Mockito.any(ResourceTypeEnum.class)))
        .thenReturn(resourceList);
    when(dataflowMapper.entityToClass(Mockito.any())).thenReturn(dataFlowVO);
    when(datasetMetabaseController.findReportingDataSetIdByDataflowId(1L))
        .thenReturn(reportingDatasetVOs);
    when(datasetMetabaseController.findDesignDataSetIdByDataflowId(1L))
        .thenReturn(designDatasetVOs);
    when(dataflowRepository.findById(Mockito.any())).thenReturn(Optional.of(new Dataflow()));
    doThrow(MockitoException.class).when(datasetSchemaControllerZuul).deleteDatasetSchema(1L, true);
    dataflowServiceImpl.deleteDataFlow(1L);
  }

  /**
   * Delete data flow throws delete dataflow.
   *
   * @throws Exception the exception
   */
  @Test
  public void deleteDataFlowVerifyTest() {
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
    dataFlowVO.setStatus(TypeStatusEnum.DRAFT);
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
    List<TestDatasetVO> testDatasetVOs = new ArrayList<>();
    TestDatasetVO testDatasetVO = new TestDatasetVO();
    testDatasetVO.setId(1L);
    testDatasetVOs.add(testDatasetVO);

    Dataflow dataflowEntity = new Dataflow();
    Set<Representative> representatives = new HashSet<>();
    Representative representative = new Representative();
    representative.setId(1L);
    representatives.add(representative);
    dataflowEntity.setRepresentatives(representatives);

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("name");
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
    when(testDataSetControllerZuul.findTestDatasetByDataflowId(1L)).thenReturn(testDatasetVOs);

    when(dataflowRepository.findById(Mockito.any())).thenReturn(Optional.of(dataflowEntity));
    dataflowServiceImpl.deleteDataFlow(1L);
  }

  /**
   * Delete data flow delete representative.
   *
   * @throws Exception the exception
   */
  @Test
  public void deleteDataFlowDeleteRepresentatives() throws Exception {
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DRAFT);
    List<RepresentativeVO> representatives = new ArrayList<>();
    RepresentativeVO representative = new RepresentativeVO();
    representatives.add(representative);
    dataflowVO.setRepresentatives(representatives);


    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("name");
    when(dataflowRepository.findById(Mockito.any())).thenReturn(Optional.of(new Dataflow()));
    when(userManagementControllerZull.getResourcesByUser(Mockito.any(ResourceTypeEnum.class)))
        .thenReturn(new ArrayList<ResourceAccessVO>());
    when(dataflowMapper.entityToClass(Mockito.any())).thenReturn(dataflowVO);
    when(datasetMetabaseController.findReportingDataSetIdByDataflowId(Mockito.any()))
        .thenReturn(new ArrayList<ReportingDatasetVO>());
    when(datasetMetabaseController.findDesignDataSetIdByDataflowId(Mockito.any()))
        .thenReturn(new ArrayList<DesignDatasetVO>());
    when(dataCollectionControllerZuul.findDataCollectionIdByDataflowId(Mockito.any()))
        .thenReturn(new ArrayList<DataCollectionVO>());
    when(representativeService.getRepresetativesByIdDataFlow(Mockito.any()))
        .thenReturn(representatives);

    dataflowServiceImpl.deleteDataFlow(1L);
    Mockito.verify(resourceManagementControllerZull, times(1)).deleteResource(Mockito.any());
  }

  /**
   * Delete data flow throws resource.
   *
   * @throws Exception the exception
   */
  @Test
  public void deleteDataFlowResourceVerifyTest() throws Exception {
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
    dataFlowVO.setStatus(TypeStatusEnum.DRAFT);
    List<ResourceAccessVO> resourceList = new ArrayList<>();
    ResourceAccessVO resource = new ResourceAccessVO();
    resource.setId(1L);
    resourceList.add(resource);


    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("name");
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
    dataflowServiceImpl.deleteDataFlow(1L);
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
   * Gets the public dataflows test.
   *
   * @return the public dataflows test
   */
  @Test
  public void getPublicDataflowsTest() {
    DataflowPublicVO dataflow = new DataflowPublicVO();
    dataflow.setId(1L);
    ObligationVO obligation = new ObligationVO();
    obligation.setObligationId(1);
    dataflow.setObligation(obligation);
    Mockito.when(dataflowPublicMapper.entityListToClass(Mockito.any()))
        .thenReturn(Arrays.asList(dataflow));
    assertNotNull(dataflowServiceImpl.getPublicDataflows());
  }

  /**
   * Gets the public dataflow by id test.
   *
   * @return the public dataflow by id test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getPublicDataflowByIdTest() throws EEAException {
    DataflowPublicVO dataflow = new DataflowPublicVO();
    dataflow.setId(1L);
    ObligationVO obligation = new ObligationVO();
    obligation.setObligationId(1);
    dataflow.setObligation(obligation);
    Mockito.when(dataflowPublicMapper.entityToClass(Mockito.any())).thenReturn(dataflow);
    assertNotNull(dataflowServiceImpl.getPublicDataflowById(1L));
  }

  /**
   * Gets the public dataflow by id exception test.
   *
   * @return the public dataflow by id exception test
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void getPublicDataflowByIdExceptionTest() throws EEAException {
    try {
      dataflowServiceImpl.getPublicDataflowById(1L);
    } catch (EEAException e) {
      assertEquals(EEAErrorMessage.DATAFLOW_NOTFOUND, e.getLocalizedMessage());
      throw e;
    }
  }

  @Test
  public void updateDataFlowPublicStatus() {
    dataflowServiceImpl.updateDataFlowPublicStatus(1L, false);
    Mockito.verify(dataflowRepository, times(1)).updatePublicStatus(Mockito.any(),
        Mockito.anyBoolean());
  }

  @Test
  public void getUserRolesTest() {
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DRAFT);
    assertNotNull("is null", dataflowServiceImpl.getUserRoles(1L, Arrays.asList(dataflowVO)));
  }

  @Test
  public void getPublicDataflowsByCountrySortName() {
    DataflowPublicVO dataflowPublicVO = new DataflowPublicVO();
    DataProviderVO dataprovider = new DataProviderVO();
    Mockito.when(dataflowPublicMapper.entityListToClass(Mockito.any()))
        .thenReturn(Arrays.asList(dataflowPublicVO));
    Mockito.when(representativeService.findDataProvidersByCode("FR"))
        .thenReturn(Arrays.asList(dataprovider));
    assertNotNull("assertion error",
        dataflowServiceImpl.getPublicDataflowsByCountry("FR", "name", false, 0, 12));
  }

  @Test
  public void getPublicDataflowsByCountrySortObligation() {
    assertNotNull("assertion error",
        dataflowServiceImpl.getPublicDataflowsByCountry("FR", "obligation", false, 0, 12));
  }

  @Test
  public void getPublicDataflowsByCountrySortLegalInstrument() {
    assertNotNull("assertion error",
        dataflowServiceImpl.getPublicDataflowsByCountry("FR", "legalInstrument", false, 0, 12));
  }

  @Test
  public void getPublicDataflowsByCountrySortStatus() {
    assertNotNull("assertion error",
        dataflowServiceImpl.getPublicDataflowsByCountry("FR", "status", true, 0, 12));
  }

  @Test
  public void getPublicDataflowsByCountrySortDeadline() {
    assertNotNull("assertion error",
        dataflowServiceImpl.getPublicDataflowsByCountry("FR", "deadline", false, 0, 12));
  }

  @Test
  public void getPublicDataflowsByCountrySortIsReleased() {
    assertNotNull("assertion error",
        dataflowServiceImpl.getPublicDataflowsByCountry("FR", "isReleased", false, 0, 12));
  }

  @Test
  public void getPublicDataflowsByCountrySortReleaseDate() {
    assertNotNull("assertion error",
        dataflowServiceImpl.getPublicDataflowsByCountry("FR", "releaseDate", false, 0, 12));
  }

  @Test
  public void isReferenceDataflowDraftDataflowTest() {
    Dataflow dataflow = new Dataflow();
    dataflow.setStatus(TypeStatusEnum.DRAFT);
    dataflow.setType(TypeDataflowEnum.REFERENCE);
    dataflow.setId(1L);
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DRAFT);
    dataflowVO.setType(TypeDataflowEnum.REFERENCE);
    dataflowVO.setId(1L);
    Mockito.when(dataflowRepository.findById(dataflow.getId())).thenReturn(Optional.of(dataflow));
    Mockito.when(dataflowNoContentMapper.entityToClass(Mockito.any())).thenReturn(dataflowVO);
    assertTrue(dataflowServiceImpl.isReferenceDataflowDraft(EntityClassEnum.DATAFLOW, 1L));
  }

  @Test
  public void isReferenceDataflowDraftDatasetTest() {
    Dataflow dataflow = new Dataflow();
    dataflow.setStatus(TypeStatusEnum.DRAFT);
    dataflow.setType(TypeDataflowEnum.REFERENCE);
    dataflow.setId(1L);
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DRAFT);
    dataflowVO.setType(TypeDataflowEnum.REFERENCE);
    dataflowVO.setId(1L);
    DataSetMetabaseVO dataset = new DataSetMetabaseVO();
    dataset.setId(1L);
    dataset.setDataflowId(1L);
    Mockito.when(dataflowRepository.findById(dataflow.getId())).thenReturn(Optional.of(dataflow));
    Mockito.when(dataflowNoContentMapper.entityToClass(Mockito.any())).thenReturn(dataflowVO);
    Mockito.when(datasetMetabaseController.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(dataset);

    assertTrue(dataflowServiceImpl.isReferenceDataflowDraft(EntityClassEnum.DATASET, 1L));
  }

  @Test
  public void getReferenceDataflowsTest() throws EEAException {
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DRAFT);
    dataflowVO.setType(TypeDataflowEnum.REFERENCE);
    dataflowVO.setId(0L);
    ResourceAccessVO resourceAccessVO = new ResourceAccessVO();
    resourceAccessVO.setId(0L);
    Mockito.when(userManagementControllerZull.getResourcesByUser(ResourceTypeEnum.DATAFLOW))
        .thenReturn(Arrays.asList(resourceAccessVO));
    Mockito.when(dataflowRepository.findReferenceByStatusAndIdInOrderByStatusDescCreationDateDesc(
        Mockito.any(), Mockito.any())).thenReturn(dataflows);
    Mockito.when(dataflowNoContentMapper.entityToClass(Mockito.any())).thenReturn(dataflowVO);
    Mockito
        .when(dataflowRepository
            .findReferenceByStatusInOrderByStatusDescCreationDateDesc(Mockito.any()))
        .thenReturn(dataflows);
    assertNotNull(dataflowServiceImpl.getReferenceDataflows(""));
  }

  @Test
  public void getBusinessDataflowsTest() throws EEAException {
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DRAFT);
    dataflowVO.setType(TypeDataflowEnum.REFERENCE);
    dataflowVO.setId(0L);
    ResourceAccessVO resourceAccessVO = new ResourceAccessVO();
    resourceAccessVO.setId(0L);
    Collection<SimpleGrantedAuthority> authorities = new HashSet<>();
    authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.doReturn(authorities).when(authentication).getAuthorities();
    Mockito.when(userManagementControllerZull.getResourcesByUser(ResourceTypeEnum.DATAFLOW))
        .thenReturn(Arrays.asList(resourceAccessVO));
    Mockito.when(dataflowRepository.findBusinessInOrderByStatusDescCreationDateDesc())
        .thenReturn(dataflows);
    Mockito.when(dataflowNoContentMapper.entityToClass(Mockito.any())).thenReturn(dataflowVO);

    assertNotNull(dataflowServiceImpl.getBusinessDataflows(""));
  }


  @Test
  public void getDataflowsByDataProviderIdsTest() {
    when(userManagementControllerZull.getResourcesByUser(ResourceTypeEnum.DATAFLOW))
        .thenReturn(new ArrayList<>());
    when(dataflowRepository.findDataflowsByDataproviderIdsAndDataflowIds(Mockito.any(),
        Mockito.any())).thenReturn(new ArrayList<>());
    assertNotNull(dataflowServiceImpl.getDataflowsByDataProviderIds(new ArrayList<>()));
  }

  @Test
  public void isAdminTest() {
    Collection<SimpleGrantedAuthority> authorities = new HashSet<>();
    authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.doReturn(authorities).when(authentication).getAuthorities();
    assertTrue(dataflowServiceImpl.isAdmin());
  }

  @Test
  public void getEmptyDataflowByIDTest() throws EEAException {


    authentication = Mockito.mock(Authentication.class);
    securityContext = Mockito.mock(SecurityContext.class);
    securityContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(securityContext);

    DataFlowVO emptyDataflow = new DataFlowVO();
    emptyDataflow.setId(1L);
    emptyDataflow.setStatus(TypeStatusEnum.DESIGN);

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("name");
    when(dataflowRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(new Dataflow()));
    when(dataflowMapper.entityToClass(Mockito.any())).thenReturn(emptyDataflow);
    when(userManagementControllerZull.getResourcesByUser(Mockito.any(ResourceTypeEnum.class)))
        .thenReturn(new ArrayList<>());

    DataFlowVO searchDataflow = dataflowServiceImpl.getById(1L);

    assertEquals("Datasets don't match when using getDataflowByID", emptyDataflow, searchDataflow);
  }


  @Test
  public void isDataflowTypeDataflowTest() {
    Dataflow dataflow = new Dataflow();
    dataflow.setStatus(TypeStatusEnum.DRAFT);
    dataflow.setType(TypeDataflowEnum.REFERENCE);
    dataflow.setId(1L);
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DRAFT);
    dataflowVO.setType(TypeDataflowEnum.REFERENCE);
    dataflowVO.setId(1L);
    Mockito.when(dataflowRepository.findById(dataflow.getId())).thenReturn(Optional.of(dataflow));
    Mockito.when(dataflowNoContentMapper.entityToClass(Mockito.any())).thenReturn(dataflowVO);
    assertTrue(dataflowServiceImpl.isDataflowType(TypeDataflowEnum.REFERENCE,
        EntityClassEnum.DATAFLOW, 1L));
  }

  @Test
  public void isDataflowTypeDatasetTest() {
    Dataflow dataflow = new Dataflow();
    dataflow.setStatus(TypeStatusEnum.DRAFT);
    dataflow.setType(TypeDataflowEnum.REFERENCE);
    dataflow.setId(1L);
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DRAFT);
    dataflowVO.setType(TypeDataflowEnum.REFERENCE);
    dataflowVO.setId(1L);
    DataSetMetabaseVO dataset = new DataSetMetabaseVO();
    dataset.setId(1L);
    dataset.setDataflowId(1L);
    Mockito.when(dataflowRepository.findById(dataflow.getId())).thenReturn(Optional.of(dataflow));
    Mockito.when(dataflowNoContentMapper.entityToClass(Mockito.any())).thenReturn(dataflowVO);
    Mockito.when(datasetMetabaseController.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(dataset);

    assertTrue(dataflowServiceImpl.isDataflowType(TypeDataflowEnum.REFERENCE,
        EntityClassEnum.DATASET, 1L));
  }

}
