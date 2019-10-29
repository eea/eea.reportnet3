package org.eea.dataflow.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
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
import org.eea.dataflow.persistence.domain.UserRequest;
import org.eea.dataflow.persistence.repository.ContributorRepository;
import org.eea.dataflow.persistence.repository.DataflowRepository;
import org.eea.dataflow.persistence.repository.DocumentRepository;
import org.eea.dataflow.persistence.repository.UserRequestRepository;
import org.eea.dataflow.service.impl.DataflowServiceImpl;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.enums.TypeRequestEnum;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
import org.eea.interfaces.vo.dataset.ReportingDatasetVO;
import org.eea.interfaces.vo.ums.ResourceAccessVO;
import org.eea.interfaces.vo.ums.enums.ResourceTypeEnum;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
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
   * The document mapper.
   */
  @Mock
  private DocumentMapper documentMapper;

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
    dataflowServiceImpl.getById(null);
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
    dataFlowVO.setReportingDatasets(reportingDatasetVOs);
    dataFlowVO.setDesignDatasets(designDatasetVOs);
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
    Mockito.doNothing().when(userManagementControllerZull).addContributorToResource(Mockito.any(),
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
   */
  @Test
  public void createDataFlowExist() {
    DataFlowVO dataFlowVO = new DataFlowVO();
    when(dataflowRepository.findByName(dataFlowVO.getName()))
        .thenReturn(Optional.of(new Dataflow()));
    dataflowServiceImpl.createDataFlow(dataFlowVO);
  }

  /**
   * Creates the data flow non exist.
   */
  @Test
  public void createDataFlowNonExist() {
    DataFlowVO dataFlowVO = new DataFlowVO();
    dataflowServiceImpl.createDataFlow(dataFlowVO);
  }


  /**
   * Test get datasets id.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testGetDatasetsId() throws EEAException {

    dataflowServiceImpl.getReportingDatasetsId(1L);
  }


  /**
   * Test get datasets id error.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void testGetDatasetsIdError() throws EEAException {

    dataflowServiceImpl.getReportingDatasetsId(null);
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
    dataflowServiceImpl.getMetabaseById(null);
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

}
