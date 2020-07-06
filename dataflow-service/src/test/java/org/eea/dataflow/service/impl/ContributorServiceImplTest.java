package org.eea.dataflow.service.impl;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.ums.ResourceManagementController.ResourceManagementControllerZull;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.contributor.ContributorVO;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
import org.eea.interfaces.vo.dataset.ReportingDatasetVO;
import org.eea.interfaces.vo.ums.ResourceInfoVO;
import org.eea.interfaces.vo.ums.UserRepresentationVO;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The Class ContributorServiceImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class ContributorServiceImplTest {

  /** The contributor service impl. */
  @InjectMocks
  private ContributorServiceImpl contributorServiceImpl;

  /** The user management controller zull. */
  @Mock
  private UserManagementControllerZull userManagementControllerZull;

  /** The resource management controller zull. */
  @Mock
  private ResourceManagementControllerZull resourceManagementControllerZull;

  /** The dataflow controlle zuul. */
  @Mock
  private DataFlowControllerZuul dataflowControllerZuul;
  /** The contributor VO write. */
  private ContributorVO contributorVOWrite;

  /** The contributor VO read. */
  private ContributorVO contributorVORead;

  /** The dataflow VO. */
  private DataFlowVO dataflowVO;

  /** The design datasets. */
  private List<DesignDatasetVO> designDatasets;

  /** The reporting datasets. */
  private List<ReportingDatasetVO> reportingDatasets;

  /**
   * Inits the mocks.
   */
  private List<UserRepresentationVO> listUserWrite;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    contributorVOWrite = new ContributorVO();
    contributorVOWrite.setAccount("write@reportnet.net");
    contributorVOWrite.setRole("EDITOR");
    contributorVOWrite.setWritePermission(true);

    contributorVORead = new ContributorVO();
    contributorVORead.setAccount("read@reportnet.net");
    contributorVORead.setRole("EDITOR");
    contributorVORead.setWritePermission(false);

    dataflowVO = new DataFlowVO();
    dataflowVO.setId(1L);
    designDatasets = new ArrayList();
    reportingDatasets = new ArrayList();
    listUserWrite = new ArrayList<>();

    DesignDatasetVO designDatasetVO = new DesignDatasetVO();
    designDatasetVO.setId(1L);
    designDatasets.add(designDatasetVO);

    ReportingDatasetVO reportingDatasetVO = new ReportingDatasetVO();
    reportingDatasetVO.setDataProviderId(1L);
    reportingDatasetVO.setId(1L);
    reportingDatasets.add(reportingDatasetVO);
    dataflowVO.setReportingDatasets(reportingDatasets);
    dataflowVO.setDesignDatasets(designDatasets);
    listUserWrite.add(new UserRepresentationVO());
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Insert document exception 1 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createAssociatedPermissionsEmpty() throws EEAException {
    List<UserRepresentationVO> usersEditorRead = new ArrayList();
    List<UserRepresentationVO> usersEditorWrite = new ArrayList();
    when(userManagementControllerZull
        .getUsersByGroup(ResourceGroupEnum.DATAFLOW_EDITOR_READ.getGroupName(1L)))
            .thenReturn(usersEditorRead);
    when(userManagementControllerZull
        .getUsersByGroup(ResourceGroupEnum.DATAFLOW_EDITOR_WRITE.getGroupName(1L)))
            .thenReturn(usersEditorWrite);
    contributorServiceImpl.createAssociatedPermissions(1L, 1L);
    Mockito.verify(userManagementControllerZull, times(2)).getUsersByGroup((Mockito.any()));
  }

  /**
   * Creates the associated permissions filled.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createAssociatedPermissionsFilled() throws EEAException {
    List<UserRepresentationVO> usersEditorRead = new ArrayList();
    UserRepresentationVO userRepresentationVORead = new UserRepresentationVO();
    userRepresentationVORead.setEmail("reportnet@reportnet.net");
    usersEditorRead.add(userRepresentationVORead);
    List<UserRepresentationVO> usersEditorWrite = new ArrayList();
    UserRepresentationVO userRepresentationVOWrite = new UserRepresentationVO();
    userRepresentationVOWrite.setEmail("reportnet@reportnet.net");
    usersEditorWrite.add(userRepresentationVOWrite);
    when(userManagementControllerZull
        .getUsersByGroup(ResourceGroupEnum.DATAFLOW_EDITOR_READ.getGroupName(1L)))
            .thenReturn(usersEditorRead);
    when(userManagementControllerZull
        .getUsersByGroup(ResourceGroupEnum.DATAFLOW_EDITOR_WRITE.getGroupName(1L)))
            .thenReturn(usersEditorWrite);
    doNothing().when(resourceManagementControllerZull).createResource(Mockito.any());
    contributorServiceImpl.createAssociatedPermissions(1L, 1L);
    Mockito.verify(userManagementControllerZull, times(1))
        .addContributorsToResources(Mockito.any());
  }



  /**
   * Delete contributor editor.
   *
   * @throws EEAException the EEA exception
   */
  @Test()
  public void deleteContributorEditor() throws EEAException {
    when(dataflowControllerZuul.findById(1L)).thenReturn(dataflowVO);
    contributorServiceImpl.deleteContributor(1L, "reportnet@reportnet.net", "EDITOR", 1L);
    Mockito.verify(userManagementControllerZull, times(1))
        .removeContributorsFromResources(Mockito.any());

  }

  /**
   * Delete contributor report.
   *
   * @throws EEAException the EEA exception
   */
  @Test()
  public void deleteContributorReport() throws EEAException {
    when(dataflowControllerZuul.findById(1L)).thenReturn(dataflowVO);
    contributorServiceImpl.deleteContributor(1L, "reportnet@reportnet.net", "REPORTER", 1L);
    Mockito.verify(userManagementControllerZull, times(1))
        .removeContributorsFromResources(Mockito.any());
  }


  /**
   * Creates the contributor editor read.
   *
   * @throws EEAException the EEA exception
   */
  @Test()
  public void createContributorEditorRead() throws EEAException {
    when(dataflowControllerZuul.findById(1L)).thenReturn(dataflowVO);
    ResourceInfoVO resourceInfoVO = new ResourceInfoVO();
    when(resourceManagementControllerZull.getResourceDetail(1L,
        ResourceGroupEnum.DATAFLOW_EDITOR_READ)).thenReturn(resourceInfoVO);
    when(resourceManagementControllerZull.getResourceDetail(1L,
        ResourceGroupEnum.DATASCHEMA_EDITOR_READ)).thenReturn(resourceInfoVO);
    contributorServiceImpl.createContributor(1L, contributorVORead, "EDITOR");
    Mockito.verify(dataflowControllerZuul, times(1)).findById(1L);
  }

  /**
   * Creates the contributor editor write.
   *
   * @throws EEAException the EEA exception
   */
  @Test()
  public void createContributorEditorWrite() throws EEAException {
    when(dataflowControllerZuul.findById(1L)).thenReturn(dataflowVO);
    ResourceInfoVO resourceInfoVO = new ResourceInfoVO();
    when(resourceManagementControllerZull.getResourceDetail(1L,
        ResourceGroupEnum.DATAFLOW_EDITOR_WRITE)).thenReturn(resourceInfoVO);
    when(resourceManagementControllerZull.getResourceDetail(1L,
        ResourceGroupEnum.DATASCHEMA_EDITOR_WRITE)).thenReturn(resourceInfoVO);
    contributorServiceImpl.createContributor(1L, contributorVOWrite, "EDITOR");

    Mockito.verify(dataflowControllerZuul, times(1)).findById(1L);
  }

  /**
   * Creates the contributor report read.
   *
   * @throws EEAException the EEA exception
   */
  @Test()
  public void createContributorReportRead() throws EEAException {
    when(dataflowControllerZuul.findById(1L)).thenReturn(dataflowVO);
    ResourceInfoVO resourceInfoVO = new ResourceInfoVO();
    when(resourceManagementControllerZull.getResourceDetail(1L,
        ResourceGroupEnum.DATAFLOW_REPORTER_READ)).thenReturn(resourceInfoVO);
    when(resourceManagementControllerZull.getResourceDetail(1L,
        ResourceGroupEnum.DATASCHEMA_REPORTER_READ)).thenReturn(resourceInfoVO);
    contributorServiceImpl.createContributor(1L, contributorVORead, "REPORTER");
    Mockito.verify(dataflowControllerZuul, times(1)).findById(1L);
  }


  /**
   * Update contributor.
   * 
   * @throws EEAException the EEA exception
   */
  @Test()
  public void updateContributor() throws EEAException {
    when(dataflowControllerZuul.findById(1L)).thenReturn(dataflowVO);
    ResourceInfoVO resourceInfoVO = new ResourceInfoVO();
    when(resourceManagementControllerZull.getResourceDetail(1L,
        ResourceGroupEnum.DATAFLOW_EDITOR_WRITE)).thenReturn(resourceInfoVO);
    when(resourceManagementControllerZull.getResourceDetail(1L,
        ResourceGroupEnum.DATASCHEMA_EDITOR_WRITE)).thenReturn(resourceInfoVO);
    contributorServiceImpl.updateContributor(1L, contributorVOWrite, "EDITOR", 1l);
    Mockito.verify(dataflowControllerZuul, times(2)).findById(1L);
  }


  /**
   * Find contributors by resource id editor test.
   */
  @Test
  public void findContributorsByResourceIdEditorTest() {
    when(dataflowControllerZuul.findById(Mockito.any())).thenReturn(dataflowVO);
    assertNotNull(contributorServiceImpl.findContributorsByResourceId(1L, 1L, "EDITOR"));
  }

  /**
   * Find contributors by resource id reporter test.
   */
  @Test
  public void findContributorsByResourceIdReporterTest() {
    reportingDatasets.get(0).setDataProviderId(2L);
    when(dataflowControllerZuul.findById(Mockito.any())).thenReturn(dataflowVO);
    when(userManagementControllerZull.getUsersByGroup(Mockito.any())).thenReturn(listUserWrite);
    assertNotNull(contributorServiceImpl.findContributorsByResourceId(1L, 1L, "REPORTER"));
  }

}
