package org.eea.dataflow.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.eea.dataflow.service.RepresentativeService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.dataflow.DataProviderCodeVO;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataflow.LeadReporterVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.dataflow.enums.TypeDataProviderEnum;
import org.eea.interfaces.vo.dataset.enums.FileTypeEnum;
import org.eea.interfaces.vo.ums.UserRepresentationVO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

/** The Class RepresentativeControllerImplTest. */
@RunWith(MockitoJUnitRunner.class)
public class RepresentativeControllerImplTest {

  /** The representative controller impl. */
  @InjectMocks
  private RepresentativeControllerImpl representativeControllerImpl;

  /** The user management controller zull. */
  @Mock
  private UserManagementControllerZull userManagementControllerZull;

  /** The representative service. */
  @Mock
  private RepresentativeService representativeService;

  /** The representative VO. */
  private RepresentativeVO representativeVO;

  /** The users. */
  private List<UserRepresentationVO> users;

  /** The emails. */
  private List<String> emails;

  /** The user. */
  private UserRepresentationVO user;

  /** The representative V os. */
  private List<RepresentativeVO> representativeVOs;

  /** The lead reporters. */
  private List<LeadReporterVO> leadReporters;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    user = new UserRepresentationVO();
    user.setEmail("email@host.com");
    users = new ArrayList<>();
    users.add(user);
    emails = new ArrayList<>();
    emails.add("email@host.com");
    representativeVO = new RepresentativeVO();
    leadReporters = new ArrayList<>();
    representativeVO.setLeadReporters(leadReporters);
    representativeVOs = new ArrayList<>();
    representativeVOs.add(representativeVO);
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Find all data provider by group id success test.
   */
  @Test
  public void findAllDataProviderByGroupIdSuccessTest() {
    Mockito.when(representativeService.getAllDataProviderByGroupId(Mockito.any()))
        .thenReturn(new ArrayList<DataProviderVO>());
    assertEquals(0, representativeControllerImpl.findAllDataProviderByGroupId(1L).size());
  }

  /**
   * Find all data provider by group id exception test.
   */
  @Test
  public void findAllDataProviderByGroupIdExceptionTest() {
    try {
      representativeControllerImpl.findAllDataProviderByGroupId(null);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      assertEquals(EEAErrorMessage.REPRESENTATIVE_TYPE_INCORRECT, e.getReason());
    }
  }

  /**
   * Find all data provider types success test.
   */
  @Test
  public void getDataProviderGroupByCountryTypeSuccessTest() {
    when(representativeService.getDataProviderGroupByType(TypeDataProviderEnum.COUNTRY))
        .thenReturn(new ArrayList<DataProviderCodeVO>());
    assertEquals(0, representativeControllerImpl.findAllDataProviderCountryType().size());
  }

  /**
   * Find represetatives by id data flow exception 1 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void findRepresetativesByIdDataFlowException1Test() throws EEAException {
    try {
      representativeControllerImpl.findRepresentativesByIdDataFlow(null);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      assertEquals(EEAErrorMessage.DATAFLOW_NOTFOUND, e.getReason());
    }
  }

  /**
   * Find represetatives by id data flow exception 2 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void findRepresetativesByIdDataFlowException2Test() throws EEAException {
    doThrow(new EEAException()).when(representativeService)
        .getRepresetativesByIdDataFlow(Mockito.anyLong());
    try {
      representativeControllerImpl.findRepresentativesByIdDataFlow(1L);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
      assertEquals(EEAErrorMessage.REPRESENTATIVE_NOT_FOUND, e.getReason());
    }
  }

  /**
   * Find represetatives by id data flow success test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void findRepresetativesByIdDataFlowSuccessTest() throws EEAException {
    when(representativeService.getRepresetativesByIdDataFlow(Mockito.anyLong()))
        .thenReturn(representativeVOs);
    assertEquals("error in the message", representativeVOs,
        representativeControllerImpl.findRepresentativesByIdDataFlow(1L));
  }

  /**
   * Update representative success test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateRepresentativeSuccessTest() throws EEAException {
    when(representativeService.authorizeByRepresentativeId(Mockito.any())).thenReturn(true);
    representativeControllerImpl.updateRepresentative(representativeVO);
    Mockito.verify(representativeService, times(1)).updateDataflowRepresentative(Mockito.any());
  }

  /**
   * Update representative exception 1 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateRepresentativeException1Test() throws EEAException {
    try {
      representativeControllerImpl.updateRepresentative(representativeVO);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
      assertNull(e.getReason());
    }

  }


  /**
   * Delete representative exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteRepresentativeExceptionTest() throws EEAException {
    doThrow(new EEAException()).when(representativeService)
        .deleteDataflowRepresentative(Mockito.any());
    try {
      representativeControllerImpl.deleteRepresentative(null, 0L);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
      assertEquals(EEAErrorMessage.REPRESENTATIVE_NOT_FOUND, e.getReason());
    }
  }

  /**
   * Delete representative success test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteRepresentativeSuccessTest() throws EEAException {
    representativeControllerImpl.deleteRepresentative(1L, 0L);
    Mockito.verify(representativeService, times(1)).deleteDataflowRepresentative(Mockito.any());
  }

  /**
   * Creates the representative test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createRepresentativeTest() throws EEAException {
    Mockito.when(representativeService.createRepresentative(Mockito.any(), Mockito.any()))
        .thenReturn(1L);
    Assert.assertEquals(1,
        representativeControllerImpl.createRepresentative(1L, representativeVO).longValue());
  }

  /**
   * Creates the representative exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void createRepresentativeExceptionTest() throws EEAException {
    doThrow(new EEAException()).when(representativeService).createRepresentative(Mockito.any(),
        Mockito.any());
    try {
      representativeControllerImpl.createRepresentative(1L, new RepresentativeVO());
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      throw e;
    }
  }

  /**
   * Find data provider by id test.
   */
  @Test
  public void findDataProviderByIdTest() {
    DataProviderVO dataProvider = new DataProviderVO();
    Mockito.when(representativeService.getDataProviderById(Mockito.any())).thenReturn(dataProvider);
    assertEquals(dataProvider, representativeControllerImpl.findDataProviderById(1L));
  }

  /**
   * Find data provider by id exception.
   */
  @Test(expected = ResponseStatusException.class)
  public void findDataProviderByIdException() {
    try {
      representativeControllerImpl.findDataProviderById(null);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
      assertEquals(EEAErrorMessage.REPRESENTATIVE_NOT_FOUND, e.getReason());
      throw e;
    }
  }

  /**
   * Find data providers by ids test.
   */
  @Test
  public void findDataProvidersByIdsTest() {
    List<DataProviderVO> dataProviders = new ArrayList<>();
    Mockito.when(representativeService.findDataProvidersByIds(Mockito.any()))
        .thenReturn(dataProviders);
    assertEquals(dataProviders,
        representativeControllerImpl.findDataProvidersByIds(new ArrayList<>()));
  }

  /**
   * Export file.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void exportFile() throws EEAException, IOException {
    byte[] file = null;
    String fileName = "Dataflow-1-Lead-Reporters.csv";
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
    Mockito.when(representativeService.exportFile(Mockito.any())).thenReturn(file);
    assertEquals(new ResponseEntity<>(file, httpHeaders, HttpStatus.OK),
        representativeControllerImpl.exportLeadReportersFile(1L));
  }

  /**
   * Export file error.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void exportFileError() throws EEAException, IOException {
    Mockito.when(representativeService.exportFile(Mockito.any())).thenThrow(EEAException.class);
    try {
      representativeControllerImpl.exportLeadReportersFile(1L);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      assertEquals(null, e.getReason());
    }
  }

  /**
   * Export file lead.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void exportFileLead() throws EEAException, IOException {
    byte[] file = null;
    String fileName = "CountryCodes-Lead-Reporters.csv";
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
    Mockito.when(representativeService.exportTemplateReportersFile(Mockito.any())).thenReturn(file);
    assertEquals(new ResponseEntity<>(file, httpHeaders, HttpStatus.OK),
        representativeControllerImpl.exportTemplateReportersFile(1L));
  }

  /**
   * Export file lead error.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void exportFileLeadError() throws EEAException, IOException {
    Mockito.when(representativeService.exportTemplateReportersFile(Mockito.any()))
        .thenThrow(EEAException.class);
    try {
      representativeControllerImpl.exportTemplateReportersFile(1L);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      assertEquals(null, e.getReason());
    }
  }

  @Test
  public void updateLeadReporterTest() throws EEAException {
    LeadReporterVO leadReporterVO = new LeadReporterVO();
    leadReporterVO.setRepresentativeId(1L);
    leadReporterVO.setEmail("sample@email.com");

    Mockito.when(representativeService.authorizeByRepresentativeId(Mockito.anyLong()))
        .thenReturn(true);
    Mockito.when(representativeService.updateLeadReporter(Mockito.any())).thenReturn(1L);
    Assert.assertEquals(1L,
        representativeControllerImpl.updateLeadReporter(leadReporterVO, 0l).longValue());
  }

  @Test(expected = ResponseStatusException.class)
  public void updateLeadReporterNotFoundTest() throws EEAException {
    LeadReporterVO leadReporterVO = new LeadReporterVO();
    leadReporterVO.setRepresentativeId(1L);
    leadReporterVO.setEmail("sample@email.com");

    Mockito.when(representativeService.authorizeByRepresentativeId(Mockito.anyLong()))
        .thenReturn(true);
    Mockito.when(representativeService.updateLeadReporter(Mockito.any()))
        .thenThrow(EEAException.class);
    try {
      representativeControllerImpl.updateLeadReporter(leadReporterVO, 0L);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
      throw e;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void updateLeadReporterInvalidEmailTest() throws EEAException {
    LeadReporterVO leadReporterVO = new LeadReporterVO();
    leadReporterVO.setRepresentativeId(1L);
    leadReporterVO.setEmail("@email.com");

    Mockito.when(representativeService.authorizeByRepresentativeId(Mockito.anyLong()))
        .thenReturn(true);
    try {
      representativeControllerImpl.updateLeadReporter(leadReporterVO, 0L);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      throw e;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void updateLeadReporterEmptyEmailTest() throws EEAException {
    LeadReporterVO leadReporterVO = new LeadReporterVO();
    leadReporterVO.setRepresentativeId(1L);

    Mockito.when(representativeService.authorizeByRepresentativeId(Mockito.anyLong()))
        .thenReturn(true);
    try {
      representativeControllerImpl.updateLeadReporter(leadReporterVO, 0L);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      throw e;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void updateLeadReporterUnauthorizedTest() throws EEAException {
    LeadReporterVO leadReporterVO = new LeadReporterVO();
    leadReporterVO.setRepresentativeId(1L);

    Mockito.when(representativeService.authorizeByRepresentativeId(Mockito.anyLong()))
        .thenReturn(false);
    try {
      representativeControllerImpl.updateLeadReporter(leadReporterVO, 0L);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
      throw e;
    }
  }

  @Test
  public void importFileCountryTemplateTest() {
    MockMultipartFile fileMock = new MockMultipartFile("file", "fileOriginal.csv",
        FileTypeEnum.CSV.getValue(), "content".getBytes());
    assertNotNull(representativeControllerImpl.importFileCountryTemplate(1L, 1L, fileMock));
  }

  @Test(expected = ResponseStatusException.class)
  public void importFileCountryTemplateFileExtensionTest() {
    MockMultipartFile fileMock = new MockMultipartFile("file", "fileOriginal",
        FileTypeEnum.CSV.getValue(), "content".getBytes());
    try {
      representativeControllerImpl.importFileCountryTemplate(1L, 1L, fileMock);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      assertEquals(EEAErrorMessage.FILE_EXTENSION, e.getReason());
      throw e;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void importFileCountryTemplateCSVExceptionTest() {
    MockMultipartFile fileMock = new MockMultipartFile("file", "fileOriginal.xlsx",
        FileTypeEnum.XLSX.getValue(), "content".getBytes());
    try {
      representativeControllerImpl.importFileCountryTemplate(1L, 1L, fileMock);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      assertEquals(EEAErrorMessage.CSV_FILE_ERROR, e.getReason());
      throw e;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void importFileCountryTemplateExceptionTest() throws EEAException, IOException {
    MockMultipartFile fileMock = new MockMultipartFile("file", "fileOriginal.csv",
        FileTypeEnum.CSV.getValue(), "content".getBytes());
    when(representativeService.importFile(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenThrow(EEAException.class);
    try {
      representativeControllerImpl.importFileCountryTemplate(1L, 1L, fileMock);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      throw e;
    }
  }

  @Test
  public void updateRepresentativeVisibilityRestrictionsTest() {
    representativeControllerImpl.updateRepresentativeVisibilityRestrictions(1L, 1L, true);
    Mockito.verify(representativeService, times(1)).updateRepresentativeVisibilityRestrictions(
        Mockito.any(), Mockito.any(), Mockito.anyBoolean());
  }

  @Test
  public void createLeadReporterTest() {
    LeadReporterVO leadReporter = new LeadReporterVO();
    leadReporter.setEmail("test@test.com");
    assertEquals(0L,
        representativeControllerImpl.createLeadReporter(1L, leadReporter, 0L).longValue());
  }

  @Test(expected = ResponseStatusException.class)
  public void createLeadReporterNullTest() {
    try {
      representativeControllerImpl.createLeadReporter(null, null, 0L);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      assertEquals(EEAErrorMessage.USER_NOTFOUND, e.getReason());
      throw e;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void createLeadReporterEmailExceptionTest() {
    LeadReporterVO leadReporter = new LeadReporterVO();
    leadReporter.setEmail("");
    try {
      representativeControllerImpl.createLeadReporter(null, leadReporter, 0L);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      assertEquals(String.format(EEAErrorMessage.NOT_EMAIL, ""), e.getReason());
      throw e;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void createLeadReporterExceptionTest() throws EEAException {
    LeadReporterVO leadReporter = new LeadReporterVO();
    leadReporter.setEmail("test@test.com");
    Mockito.when(representativeService.createLeadReporter(Mockito.any(), Mockito.any()))
        .thenThrow(EEAException.class);
    try {
      representativeControllerImpl.createLeadReporter(1L, leadReporter, 0L);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      throw e;
    }
  }

  @Test
  public void deleteLeadReporterTest() throws EEAException {
    representativeControllerImpl.deleteLeadReporter(0L, 0L);
    Mockito.verify(representativeService, times(1)).deleteLeadReporter(Mockito.any());
  }

  @Test(expected = ResponseStatusException.class)
  public void deleteLeadReporterExceptionTest() throws EEAException {
    doThrow(new EEAException()).when(representativeService).deleteLeadReporter(Mockito.any());
    try {
      representativeControllerImpl.deleteLeadReporter(1L, 0L);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
      throw e;
    }
  }

  @Test
  public void updateInternalRepresentativeTest() {
    assertEquals(0L, representativeControllerImpl
        .updateInternalRepresentative(new RepresentativeVO()).longValue());
  }

  @Test
  public void findDataProvidersByCodeTest() {
    assertNotNull("is null", representativeControllerImpl.findDataProvidersByCode("ES"));
  }

  @Test
  public void findRepresentativesByDataFlowIdAndProviderIdListTest() {
    assertNotNull("is null", representativeControllerImpl
        .findRepresentativesByDataFlowIdAndProviderIdList(0L, new ArrayList<>()));
  }

  @Test
  public void updateRestrictFromPublicTest() {
    representativeControllerImpl.updateRestricFromPublic(1L, 1L, true);
    Mockito.verify(representativeService, times(1)).updateRepresentativeVisibilityRestrictions(
        Mockito.anyLong(), Mockito.anyLong(), Mockito.anyBoolean());
  }
}
