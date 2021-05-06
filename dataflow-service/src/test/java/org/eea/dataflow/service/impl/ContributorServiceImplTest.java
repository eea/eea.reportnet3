package org.eea.dataflow.service.impl;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.ums.ResourceManagementController.ResourceManagementControllerZull;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.contributor.ContributorVO;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
import org.eea.interfaces.vo.dataset.ReportingDatasetVO;
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

  /** The data set metabase controller zuul. */
  @Mock
  private DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul;

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
    contributorVOWrite.setRole("EDITOR");

    contributorVORead = new ContributorVO();
    contributorVORead.setAccount("read@reportnet.net");
    contributorVORead.setRole("EDITOR");

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
    MockitoAnnotations.initMocks(this);
  }

  // /**
  // * Delete contributor editor.
  // *
  // * @throws EEAException the EEA exception
  // */
  // @Test
  // public void deleteContributorEditor() throws EEAException {
  // when(dataSetMetabaseControllerZuul.findDesignDataSetIdByDataflowId(1L))
  // .thenReturn(designDatasets);
  // contributorServiceImpl.deleteContributor(1L, "reportnet@reportnet.net", "EDITOR", 1L);
  // Mockito.verify(userManagementControllerZull, times(1))
  // .removeContributorsFromResources(Mockito.any());
  //
  // }
  //
  // /**
  // * Delete contributor report.
  // *
  // * @throws EEAException the EEA exception
  // */
  // @Test
  // public void deleteContributorReport() throws EEAException {
  // when(dataSetMetabaseControllerZuul.findReportingDataSetIdByDataflowId(1L))
  // .thenReturn(reportingDatasets);
  // contributorServiceImpl.deleteContributor(1L, "reportnet@reportnet.net", "REPORTER", 1L);
  // Mockito.verify(userManagementControllerZull, times(1))
  // .removeContributorsFromResources(Mockito.any());
  // }


  // /**
  // * Creates the contributor editor read.
  // *
  // * @throws EEAException the EEA exception
  // */
  // @Test
  // public void createContributorEditorRead() throws EEAException {
  // when(dataSetMetabaseControllerZuul.findDesignDataSetIdByDataflowId(1L))
  // .thenReturn(designDatasets);
  // contributorServiceImpl.createContributor(1L, contributorVORead, "EDITOR", null, null);
  // Mockito.verify(dataSetMetabaseControllerZuul, times(1)).findDesignDataSetIdByDataflowId(1L);
  // }

  // /**
  // * Creates the contributor editor write.
  // *
  // * @throws EEAException the EEA exception
  // */
  // @Test()
  // public void createContributorEditorWrite() throws EEAException {
  // when(dataSetMetabaseControllerZuul.findDesignDataSetIdByDataflowId(1L))
  // .thenReturn(designDatasets);
  // contributorServiceImpl.createContributor(1L, contributorVOWrite, "EDITOR", null, null);
  // Mockito.verify(dataSetMetabaseControllerZuul, times(1)).findDesignDataSetIdByDataflowId(1L);
  // }

  // /**
  // * Creates the contributor report read.
  // *
  // * @throws EEAException the EEA exception
  // */
  // @Test
  // public void createContributorReportRead() throws EEAException {
  // when(dataSetMetabaseControllerZuul.findReportingDataSetIdByDataflowId(1L))
  // .thenReturn(reportingDatasets);
  // ResourceInfoVO resourceInfoVO = new ResourceInfoVO();
  // when(resourceManagementControllerZull.getResourceDetail(1L,
  // ResourceGroupEnum.DATAFLOW_REPORTER_READ)).thenReturn(resourceInfoVO);
  // when(resourceManagementControllerZull.getResourceDetail(1L,
  // ResourceGroupEnum.DATASCHEMA_REPORTER_READ)).thenReturn(resourceInfoVO);
  // contributorServiceImpl.createContributor(1L, contributorVORead, "REPORTER", 1L, null);
  // Mockito.verify(dataSetMetabaseControllerZuul, times(1)).findReportingDataSetIdByDataflowId(1L);
  // }

  // /**
  // * Update contributor.
  // *
  // * @throws EEAException the EEA exception
  // */
  // @Test
  // public void updateContributor() throws EEAException {
  // ResourceAccessVO resourceAccessVO = new ResourceAccessVO();
  // resourceAccessVO.setId(1L);
  // resourceAccessVO.setResource(ResourceTypeEnum.DATAFLOW);
  // resourceAccessVO.setRole(SecurityRoleEnum.EDITOR_WRITE);
  // List<ResourceAccessVO> resourceAccessVOs = new ArrayList<>();
  // resourceAccessVOs.add(resourceAccessVO);
  // when(userManagementControllerZull.getResourcesByUserEmail(Mockito.any()))
  // .thenReturn(resourceAccessVOs);
  // when(dataSetMetabaseControllerZuul.findDesignDataSetIdByDataflowId(1L))
  // .thenReturn(designDatasets);
  // contributorServiceImpl.updateContributor(1L, contributorVOWrite, "EDITOR", 1l);
  // Mockito.verify(dataSetMetabaseControllerZuul, times(2)).findDesignDataSetIdByDataflowId(1L);
  // }

  // /**
  // * Update contributor reporter test.
  // *
  // * @throws EEAException the EEA exception
  // */
  // @Test
  // public void updateContributorReporterTest() throws EEAException {
  // ResourceAccessVO resourceAccessVO = new ResourceAccessVO();
  // resourceAccessVO.setId(1L);
  // resourceAccessVO.setResource(ResourceTypeEnum.DATASET);
  // resourceAccessVO.setRole(SecurityRoleEnum.REPORTER_READ);
  // List<ResourceAccessVO> resourceAccessVOs = new ArrayList<>();
  // resourceAccessVOs.add(resourceAccessVO);
  // when(userManagementControllerZull.getResourcesByUserEmail(Mockito.any()))
  // .thenReturn(resourceAccessVOs);
  // when(dataSetMetabaseControllerZuul.findReportingDataSetIdByDataflowId(1L))
  // .thenReturn(reportingDatasets);
  // ResourceInfoVO resourceInfoVO = new ResourceInfoVO();
  // resourceInfoVO.setName("name");
  // when(resourceManagementControllerZull.getResourceDetail(Mockito.any(), Mockito.any()))
  // .thenReturn(resourceInfoVO);
  // when(resourceManagementControllerZull.getResourceDetail(Mockito.any(), Mockito.any()))
  // .thenReturn(resourceInfoVO);
  // contributorServiceImpl.updateContributor(1L, contributorVOWrite, "REPORTER", 1l);
  // Mockito.verify(dataSetMetabaseControllerZuul, times(1))
  // .findReportingDataSetIdByDataflowId(Mockito.any());
  // }

  /**
   * Find contributors by resource id editor test.
   */
  @Test
  public void findContributorsByResourceIdEditorTest() {
    assertNotNull(contributorServiceImpl.findContributorsByResourceId(1L, 1L, "EDITOR"));
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

  // /**
  // * Update contributor exception test.
  // *
  // * @throws EEAException the EEA exception
  // */
  // @Test(expected = ResponseStatusException.class)
  // public void updateContributorExceptionTest() throws EEAException {
  // try {
  // contributorServiceImpl.updateContributor(1L, contributorVOWrite, "LEAD_REPORTER", 1l);
  // } catch (ResponseStatusException ex) {
  // assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
  // assertEquals("Role LEAD_REPORTER doesn't exist", ex.getReason());
  // throw ex;
  // }
  // }

  /**
   * Insert document exception 1 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createAssociatedPermissionsEmpty() throws EEAException {
    List<UserRepresentationVO> usersEmpty = new ArrayList();
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
