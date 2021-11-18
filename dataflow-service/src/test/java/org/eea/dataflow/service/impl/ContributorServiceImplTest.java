package org.eea.dataflow.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.eea.dataflow.service.DataflowService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DataCollectionController.DataCollectionControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.dataset.EUDatasetController.EUDatasetControllerZuul;
import org.eea.interfaces.controller.dataset.ReferenceDatasetController.ReferenceDatasetControllerZuul;
import org.eea.interfaces.controller.dataset.TestDatasetController.TestDatasetControllerZuul;
import org.eea.interfaces.controller.ums.ResourceManagementController.ResourceManagementControllerZull;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.contributor.ContributorVO;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
import org.eea.interfaces.vo.dataset.ReportingDatasetVO;
import org.eea.interfaces.vo.ums.ResourceAccessVO;
import org.eea.interfaces.vo.ums.ResourceInfoVO;
import org.eea.interfaces.vo.ums.UserRepresentationVO;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.eea.interfaces.vo.ums.enums.ResourceTypeEnum;
import org.eea.interfaces.vo.ums.enums.SecurityRoleEnum;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

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

  /** The data set metabase controller zuul. */
  @Mock
  private DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul;

  /** The data collection controller zuul. */
  @Mock
  private DataCollectionControllerZuul dataCollectionControllerZuul;

  /** The EU dataset controller zuul. */
  @Mock
  private EUDatasetControllerZuul eUDatasetControllerZuul;

  /** The test dataset controller zuul. */
  @Mock
  private TestDatasetControllerZuul testDatasetControllerZuul;

  /** The dataflow service. */
  @Mock
  private DataflowService dataflowService;

  @Mock
  private ReferenceDatasetControllerZuul referenceDatasetControllerZuul;

  /** The contributor VO write. */
  private ContributorVO contributorVOWrite;

  /** The contributor VO read. */
  private ContributorVO contributorVORead;


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
    contributorVOWrite.setRole("EDITOR_WRITE");

    contributorVORead = new ContributorVO();
    contributorVORead.setAccount("read@reportnet.net");
    contributorVORead.setRole("EDITOR_READ");

    designDatasets = new ArrayList<>();
    reportingDatasets = new ArrayList<>();
    listUserWrite = new ArrayList<>();

    DesignDatasetVO designDatasetVO = new DesignDatasetVO();
    designDatasetVO.setId(1L);
    designDatasets.add(designDatasetVO);

    ReportingDatasetVO reportingDatasetVO = new ReportingDatasetVO();
    reportingDatasetVO.setDataProviderId(1L);
    reportingDatasetVO.setId(1L);
    reportingDatasets.add(reportingDatasetVO);
    listUserWrite.add(new UserRepresentationVO());
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Delete contributor editor.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteContributorEditor() throws EEAException {
    when(dataSetMetabaseControllerZuul.findDesignDataSetIdByDataflowId(1L))
        .thenReturn(designDatasets);
    contributorServiceImpl.deleteContributor(1L, "reportnet@reportnet.net", "EDITOR_WRITE", 1L);
    Mockito.verify(userManagementControllerZull, times(1))
        .removeContributorsFromResources(Mockito.any());

  }

  @Test
  public void deleteContributorObserver() throws EEAException {
    contributorServiceImpl.deleteContributor(1L, "reportnet@reportnet.net", "DATA_OBSERVER", 1L);
    Mockito.verify(userManagementControllerZull, times(1))
        .removeContributorsFromResources(Mockito.any());

  }

  @Test
  public void deleteContributorCustodian() throws EEAException {
    Mockito.when(dataflowService.isAdmin()).thenReturn(Boolean.TRUE);
    contributorServiceImpl.deleteContributor(1L, "reportnet@reportnet.net", "DATA_CUSTODIAN", 1L);
    Mockito.verify(userManagementControllerZull, times(1))
        .removeContributorsFromResources(Mockito.any());

  }

  @Test
  public void deleteContributorSteward() throws EEAException {
    Mockito.when(dataflowService.isAdmin()).thenReturn(Boolean.TRUE);
    contributorServiceImpl.deleteContributor(1L, "reportnet@reportnet.net", "DATA_STEWARD", 1L);
    Mockito.verify(userManagementControllerZull, times(1))
        .removeContributorsFromResources(Mockito.any());

  }


  /**
   * Delete contributor report.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteContributorReport() throws EEAException {
    when(dataSetMetabaseControllerZuul.findReportingDataSetIdByDataflowId(1L))
        .thenReturn(reportingDatasets);
    contributorServiceImpl.deleteContributor(1L, "reportnet@reportnet.net", "REPORTER_READ", 1L);
    Mockito.verify(userManagementControllerZull, times(1))
        .removeContributorsFromResources(Mockito.any());
  }


  /**
   * Creates the contributor editor read.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createContributorEditorRead() throws EEAException {
    when(dataSetMetabaseControllerZuul.findDesignDataSetIdByDataflowId(1L))
        .thenReturn(designDatasets);
    contributorServiceImpl.createContributor(1L, contributorVORead, null, null);
    Mockito.verify(dataSetMetabaseControllerZuul, times(1)).findDesignDataSetIdByDataflowId(1L);
  }

  /**
   * Creates the contributor editor write.
   *
   * @throws EEAException the EEA exception
   */
  @Test()
  public void createContributorEditorWrite() throws EEAException {
    when(dataSetMetabaseControllerZuul.findDesignDataSetIdByDataflowId(1L))
        .thenReturn(designDatasets);
    contributorServiceImpl.createContributor(1L, contributorVOWrite, null, null);
    Mockito.verify(dataSetMetabaseControllerZuul, times(1)).findDesignDataSetIdByDataflowId(1L);
  }

  /**
   * Creates the contributor report read.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createContributorReportRead() throws EEAException {
    contributorVORead.setRole("REPORTER_READ");
    Mockito.when(resourceManagementControllerZull.getResourceDetail(Mockito.any(), Mockito.any()))
        .thenReturn(new ResourceInfoVO());
    contributorServiceImpl.createContributor(1L, contributorVORead, 1L, false);
    Mockito.verify(userManagementControllerZull, times(1))
        .addContributorsToResources(Mockito.any());
  }

  @Test
  public void createContributorReportWrite() throws EEAException {
    contributorVORead.setRole("REPORTER_WRITE");
    Mockito.when(resourceManagementControllerZull.getResourceDetail(Mockito.any(), Mockito.any()))
        .thenReturn(new ResourceInfoVO());
    contributorServiceImpl.createContributor(1L, contributorVORead, 1L, false);
    Mockito.verify(userManagementControllerZull, times(1))
        .addContributorsToResources(Mockito.any());
  }

  @Test
  public void createContributorReporterCustodian() throws EEAException {
    contributorVORead.setRole("DATA_CUSTODIAN");
    Mockito.when(resourceManagementControllerZull.getResourceDetail(Mockito.any(), Mockito.any()))
        .thenReturn(new ResourceInfoVO());
    contributorServiceImpl.createContributor(1L, contributorVORead, 1L, false);
    Mockito.verify(userManagementControllerZull, times(1))
        .addContributorsToResources(Mockito.any());
  }

  @Test
  public void createContributorReporterSteward() throws EEAException {
    contributorVORead.setRole("DATA_STEWARD");
    Mockito.when(resourceManagementControllerZull.getResourceDetail(Mockito.any(), Mockito.any()))
        .thenReturn(new ResourceInfoVO());
    contributorServiceImpl.createContributor(1L, contributorVORead, 1L, false);
    Mockito.verify(userManagementControllerZull, times(1))
        .addContributorsToResources(Mockito.any());
  }

  @Test
  public void createContributorReporterObserver() throws EEAException {
    contributorVORead.setRole("DATA_OBSERVER");
    Mockito.when(resourceManagementControllerZull.getResourceDetail(Mockito.any(), Mockito.any()))
        .thenReturn(new ResourceInfoVO());
    contributorServiceImpl.createContributor(1L, contributorVORead, 1L, false);
    Mockito.verify(userManagementControllerZull, times(1))
        .addContributorsToResources(Mockito.any());
  }



  /**
   * Update contributor.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateContributor() throws EEAException {
    ResourceAccessVO resourceAccessVO = new ResourceAccessVO();
    resourceAccessVO.setId(1L);
    resourceAccessVO.setResource(ResourceTypeEnum.DATAFLOW);
    resourceAccessVO.setRole(SecurityRoleEnum.EDITOR_WRITE);
    List<ResourceAccessVO> resourceAccessVOs = new ArrayList<>();
    resourceAccessVOs.add(resourceAccessVO);
    when(userManagementControllerZull.getResourcesByUserEmail(Mockito.any()))
        .thenReturn(resourceAccessVOs);
    when(dataSetMetabaseControllerZuul.findDesignDataSetIdByDataflowId(1L))
        .thenReturn(designDatasets);
    contributorServiceImpl.updateContributor(1L, contributorVOWrite, 1l);
    Mockito.verify(userManagementControllerZull, times(1))
        .addContributorsToResources(Mockito.any());
  }

  /**
   * Update contributor reporter test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateContributorReporterTest() throws EEAException {
    ResourceAccessVO resourceAccessVO = new ResourceAccessVO();
    resourceAccessVO.setId(1L);
    resourceAccessVO.setResource(ResourceTypeEnum.DATASET);
    resourceAccessVO.setRole(SecurityRoleEnum.REPORTER_READ);
    List<ResourceAccessVO> resourceAccessVOs = new ArrayList<>();
    resourceAccessVOs.add(resourceAccessVO);
    when(userManagementControllerZull.getResourcesByUserEmail(Mockito.any()))
        .thenReturn(resourceAccessVOs);
    when(dataSetMetabaseControllerZuul.findReportingDataSetIdByDataflowId(1L))
        .thenReturn(reportingDatasets);
    ResourceInfoVO resourceInfoVO = new ResourceInfoVO();
    resourceInfoVO.setName("name");
    contributorServiceImpl.updateContributor(1L, contributorVOWrite, 1l);
    Mockito.verify(userManagementControllerZull, times(1))
        .addContributorsToResources(Mockito.any());
  }

  /**
   * Find contributors by resource id editor test.
   */
  @Test
  public void findContributorsByResourceIdEditorTest() {
    assertNotNull(contributorServiceImpl.findContributorsByResourceId(1L, 1L, "REQUESTER"));
  }

  /**
   * Find contributors by resource id reporter test.
   */
  @Test
  public void findContributorsByResourceIdReporterTest() {
    reportingDatasets.get(0).setDataProviderId(2L);
    when(dataSetMetabaseControllerZuul.findReportingDataSetIdByDataflowId(Mockito.any()))
        .thenReturn(reportingDatasets);
    when(userManagementControllerZull.getUsersByGroup(Mockito.any())).thenReturn(listUserWrite);
    assertNotNull(contributorServiceImpl.findContributorsByResourceId(1L, 1L, "REPORTER"));
  }

  /**
   * Update contributor exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void updateContributorExceptionTest() throws EEAException {
    try {
      contributorVOWrite.setRole("LEAD_REPORTER");
      contributorServiceImpl.updateContributor(1L, contributorVOWrite, 1l);
    } catch (ResponseStatusException ex) {
      assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
      assertEquals("Role LEAD_REPORTER doesn't exist", ex.getReason());
      throw ex;
    }
  }

  /**
   * Insert document exception 1 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createAssociatedPermissionsEmpty() throws EEAException {
    List<UserRepresentationVO> usersEmpty = new ArrayList<>();
    when(userManagementControllerZull
        .getUsersByGroup(ResourceGroupEnum.DATAFLOW_EDITOR_READ.getGroupName(1L)))
            .thenReturn(usersEmpty);
    when(userManagementControllerZull
        .getUsersByGroup(ResourceGroupEnum.DATAFLOW_STEWARD.getGroupName(1L)))
            .thenReturn(usersEmpty);
    when(userManagementControllerZull
        .getUsersByGroup(ResourceGroupEnum.DATAFLOW_EDITOR_WRITE.getGroupName(1L)))
            .thenReturn(usersEmpty);
    when(userManagementControllerZull
        .getUsersByGroup(ResourceGroupEnum.DATAFLOW_CUSTODIAN.getGroupName(1L)))
            .thenReturn(usersEmpty);
    contributorServiceImpl.createAssociatedPermissions(1L, 1L);
    Mockito.verify(userManagementControllerZull, times(4)).getUsersByGroup((Mockito.any()));
  }



  /**
   * Creates the associated permissions filled.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createAssociatedPermissionsFilled() throws EEAException {
    List<UserRepresentationVO> usersEditorRead = new ArrayList<>();
    UserRepresentationVO userRepresentationVORead = new UserRepresentationVO();
    userRepresentationVORead.setEmail("reportnet@reportnet.net");
    usersEditorRead.add(userRepresentationVORead);
    List<UserRepresentationVO> usersEditorWrite = new ArrayList<>();
    UserRepresentationVO userRepresentationVOWrite = new UserRepresentationVO();
    userRepresentationVOWrite.setEmail("reportnet@reportnet.net");
    usersEditorWrite.add(userRepresentationVOWrite);
    List<UserRepresentationVO> usersCustodian = new ArrayList<>();
    UserRepresentationVO userRepresentationVOCustodian = new UserRepresentationVO();
    userRepresentationVOCustodian.setEmail("reportnet@reportnet.net");
    usersCustodian.add(userRepresentationVOWrite);
    when(userManagementControllerZull
        .getUsersByGroup(ResourceGroupEnum.DATAFLOW_EDITOR_READ.getGroupName(1L)))
            .thenReturn(usersEditorRead);
    when(userManagementControllerZull
        .getUsersByGroup(ResourceGroupEnum.DATAFLOW_EDITOR_WRITE.getGroupName(1L)))
            .thenReturn(usersEditorWrite);
    when(userManagementControllerZull
        .getUsersByGroup(ResourceGroupEnum.DATAFLOW_CUSTODIAN.getGroupName(1L)))
            .thenReturn(usersCustodian);
    contributorServiceImpl.createAssociatedPermissions(1L, 1L);
    Mockito.verify(userManagementControllerZull, times(1))
        .addContributorsToResources(Mockito.any());
  }
}
