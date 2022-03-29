package org.eea.dataflow.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.eea.dataflow.mapper.DataProviderGroupMapper;
import org.eea.dataflow.mapper.DataProviderMapper;
import org.eea.dataflow.mapper.FMEUserMapper;
import org.eea.dataflow.mapper.LeadReporterMapper;
import org.eea.dataflow.mapper.RepresentativeMapper;
import org.eea.dataflow.persistence.domain.DataProvider;
import org.eea.dataflow.persistence.domain.DataProviderGroup;
import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.dataflow.persistence.domain.FMEUser;
import org.eea.dataflow.persistence.domain.LeadReporter;
import org.eea.dataflow.persistence.domain.Representative;
import org.eea.dataflow.persistence.repository.DataProviderGroupRepository;
import org.eea.dataflow.persistence.repository.DataProviderRepository;
import org.eea.dataflow.persistence.repository.DataflowRepository;
import org.eea.dataflow.persistence.repository.FMEUserRepository;
import org.eea.dataflow.persistence.repository.LeadReporterRepository;
import org.eea.dataflow.persistence.repository.RepresentativeRepository;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSnapshotController.DataSetSnapshotControllerZuul;
import org.eea.interfaces.controller.dataset.ReferenceDatasetController.ReferenceDatasetControllerZuul;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataflow.FMEUserVO;
import org.eea.interfaces.vo.dataflow.LeadReporterVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.dataflow.enums.TypeDataProviderEnum;
import org.eea.interfaces.vo.dataflow.enums.TypeDataflowEnum;
import org.eea.interfaces.vo.dataset.ReportingDatasetVO;
import org.eea.interfaces.vo.metabase.SnapshotVO;
import org.eea.interfaces.vo.ums.UserRepresentationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.security.authorization.ObjectAccessRoleEnum;
import org.eea.security.jwt.utils.EeaUserDetails;
import org.eea.thread.ThreadPropertiesManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

public class RepresentativeServiceImplTest {

  @InjectMocks
  private RepresentativeServiceImpl representativeServiceImpl;

  @Mock
  private DataflowRepository dataflowRepository;

  @Mock
  private UserManagementControllerZull userManagementControllerZull;

  @Mock
  private RepresentativeRepository representativeRepository;

  @Mock
  private RepresentativeMapper representativeMapper;

  @Mock
  private LeadReporterMapper leadReporterMapper;

  @Mock
  private DataProviderRepository dataProviderRepository;

  @Mock
  private DataProviderGroupRepository dataProviderGroupRepository;

  @Mock
  private DataProviderGroupMapper dataProviderGroupMapper;

  @Mock
  private LeadReporterRepository leadReporterRepository;

  @Mock
  private DataProviderMapper dataProviderMapper;

  @Mock
  private ReferenceDatasetControllerZuul referenceDatasetControllerZuul;

  @Mock
  private DataSetMetabaseControllerZuul datasetMetabaseController;

  @Mock
  private DataSetSnapshotControllerZuul datasetSnapshotController;

  @Mock
  private KafkaSenderUtils kafkaSenderUtils;

  @Mock
  private FMEUserRepository fmeUserRepository;

  @Mock
  private FMEUserMapper fmeUserMapper;

  private Representative representative;

  private RepresentativeVO representativeVO;

  private List<Representative> arrayId;

  private List<LeadReporterVO> leadReportersVO;

  private LeadReporter leadReporter;

  private LeadReporterVO leadReporterVO;

  private List<LeadReporter> leadReporters;

  /** The security context. */
  private SecurityContext securityContext;

  /** The authentication. */
  private Authentication authentication;


  @Before
  public void initMocks() {

    authentication = Mockito.mock(Authentication.class);
    securityContext = Mockito.mock(SecurityContext.class);
    securityContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(securityContext);

    ThreadPropertiesManager.setVariable("user", "user");

    Dataflow dataflow = new Dataflow();
    dataflow.setId(1L);
    representative = new Representative();
    representative.setId(1L);
    representative.setDataflow(dataflow);
    representative.setLeadReporters(new ArrayList<>());
    leadReporterVO = new LeadReporterVO();
    leadReporterVO.setId(1L);
    leadReporterVO.setEmail("email@user.com");
    leadReportersVO = new ArrayList<>();
    leadReportersVO.add(leadReporterVO);
    representativeVO = new RepresentativeVO();
    representativeVO.setId(1L);
    representativeVO.setLeadReporters(leadReportersVO);
    arrayId = new ArrayList<>();
    arrayId.add(new Representative());
    leadReporter = new LeadReporter(1L, "email@host.com", representative, null);
    leadReporters = new ArrayList<>();
    leadReporters.add(leadReporter);
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Delete dataflow representative exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void deleteDataflowRepresentativeExceptionTest() throws EEAException {
    try {
      representativeServiceImpl.deleteDataflowRepresentative(null);
    } catch (EEAException e) {
      assertEquals("error in the message", EEAErrorMessage.DATAFLOW_NOTFOUND, e.getMessage());
      throw e;
    }
  }

  /**
   * Delete dataflow representative test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteDataflowRepresentativeTest() throws EEAException {
    representativeServiceImpl.deleteDataflowRepresentative(1L);
    Mockito.verify(representativeRepository, times(1)).deleteById(Mockito.any());
  }

  /**
   * Update dataflow representative success test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateDataflowRepresentativeSuccessTest() throws EEAException {
    representativeVO.setDataProviderId(1L);
    when(representativeRepository.findById(Mockito.any())).thenReturn(Optional.of(representative));
    when(representativeRepository.save(Mockito.any())).thenReturn(representative);
    assertEquals("error in the message", (Long) 1L,
        representativeServiceImpl.updateDataflowRepresentative(representativeVO));
  }

  @Test
  public void updateDataflowRepresentativeReceiptSuccessTest() throws EEAException {
    representativeVO.setDataProviderId(1L);
    representativeVO.setReceiptDownloaded(true);
    representativeVO.setReceiptOutdated(true);
    when(representativeRepository.findById(Mockito.any())).thenReturn(Optional.of(representative));
    when(representativeRepository.save(Mockito.any())).thenReturn(representative);
    assertEquals("error in the message", (Long) 1L,
        representativeServiceImpl.updateDataflowRepresentative(representativeVO));
  }

  /**
   * Update dataflow representative success no changes test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateDataflowRepresentativeSuccessNoChangesTest() throws EEAException {
    representativeVO.setDataProviderId(null);
    DataProvider dataProvider = new DataProvider();
    dataProvider.setId(1L);
    representative.setDataProvider(dataProvider);
    when(representativeRepository.findById(Mockito.any())).thenReturn(Optional.of(representative));
    when(representativeRepository.save(Mockito.any())).thenReturn(representative);
    assertEquals("error in the message", (Long) 1L,
        representativeServiceImpl.updateDataflowRepresentative(representativeVO));
  }

  /**
   * Gets the data provider group by country type success test.
   *
   * @return the data provider group by country type success test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getDataProviderGroupByCountryTypeSuccessTest() throws EEAException {

    DataProviderGroup dataProviderGroup = new DataProviderGroup();
    dataProviderGroup.setId(1L);
    dataProviderGroup.setType(TypeDataProviderEnum.COUNTRY);
    List<DataProviderGroup> dataProviderGroups = new ArrayList<>();
    dataProviderGroups.add(dataProviderGroup);
    when(dataProviderGroupRepository.findDistinctCode(TypeDataProviderEnum.COUNTRY))
        .thenReturn(dataProviderGroups);



    assertEquals("error in the message",
        dataProviderGroupMapper.entityListToClass(dataProviderGroups),
        representativeServiceImpl.getDataProviderGroupByType(TypeDataProviderEnum.COUNTRY));
  }

  /**
   * Gets the representatives by id data flow exception test.
   *
   * @return the representatives by id data flow exception test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getRepresetativesByIdDataFlowExceptionTest() throws EEAException {
    try {
      representativeServiceImpl.getRepresetativesByIdDataFlow(null);
    } catch (EEAException e) {
      assertEquals("error in the message", EEAErrorMessage.DATAFLOW_NOTFOUND, e.getMessage());
    }
  }

  /**
   * Gets the representatives by id data flow success test.
   *
   * @return the representatives by id data flow success test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getRepresetativesByIdDataFlowSuccessTest() throws EEAException {
    List<RepresentativeVO> representativeVOs = new ArrayList<>();
    representativeVOs.add(representativeVO);
    when(representativeMapper.entityListToClass(Mockito.any())).thenReturn(representativeVOs);
    assertEquals("error in the message", representativeVOs,
        representativeServiceImpl.getRepresetativesByIdDataFlow(1L));
  }

  /**
   * Gets the all data provider by group id success test.
   *
   * @return the all data provider by group id success test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getAllDataProviderByGroupIdSuccessTest() throws EEAException {
    when(dataProviderMapper.entityListToClass(Mockito.any())).thenReturn(new ArrayList<>());
    when(dataProviderRepository.findAllByDataProviderGroup_id(1L)).thenReturn(new ArrayList<>());
    assertEquals("error in the message", new ArrayList<>(),
        representativeServiceImpl.getAllDataProviderByGroupId(1L));
  }

  /**
   * Creates the representative test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createRepresentativeTest() throws EEAException {
    Representative representative = new Representative();
    representative.setId(1L);
    representative.setLeadReporters(leadReporters);
    RepresentativeVO representativeVO = new RepresentativeVO();
    representativeVO.setLeadReporters(leadReportersVO);
    representativeVO.setDataProviderId(1L);

    Mockito.when(dataflowRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(new Dataflow()));
    Mockito.when(userManagementControllerZull.getUserByEmail(Mockito.any()))
        .thenReturn(new UserRepresentationVO());
    Mockito.when(
        representativeRepository.findByDataProviderIdAndDataflowId(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.empty());
    Mockito.when(representativeMapper.classToEntity(Mockito.any())).thenReturn(representative);
    Mockito.when(representativeRepository.save(Mockito.any())).thenReturn(representative);

    Assert.assertEquals(0,
        representativeServiceImpl.createRepresentative(1L, representativeVO).longValue());
  }


  /**
   * Creates the representative dataflow not found exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void createRepresentativeDataflowNotFoundExceptionTest() throws EEAException {
    RepresentativeVO representativeVO = new RepresentativeVO();
    representativeVO.setLeadReporters(leadReportersVO);
    representativeVO.setDataProviderId(1L);

    Mockito.when(dataflowRepository.findById(Mockito.any())).thenReturn(Optional.empty());
    try {
      representativeServiceImpl.createRepresentative(1L, representativeVO);
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.DATAFLOW_NOTFOUND, e.getMessage());
      throw e;
    }
  }

  /**
   * Creates the representative user requestot found exception test.
   *
   * @throws EEAException the EEA exception
   */
  // @Test(expected = EEAException.class)
  public void createRepresentativeUserRequestotFoundExceptionTest() throws EEAException {
    RepresentativeVO representativeVO = new RepresentativeVO();
    representativeVO.setLeadReporters(leadReportersVO);
    representativeVO.setDataProviderId(1L);

    Mockito.when(dataflowRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(new Dataflow()));
    Mockito.when(userManagementControllerZull.getUserByEmail(Mockito.any())).thenReturn(null);
    try {
      representativeServiceImpl.createRepresentative(1L, representativeVO);
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.USER_REQUEST_NOTFOUND, e.getMessage());
      throw e;
    }
  }

  /**
   * Creates the representative duplicate test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void createRepresentativeDuplicateTest() throws EEAException {
    Representative repre = new Representative();

    Representative representative = new Representative();
    representative.setId(1L);
    representative.setLeadReporters(leadReporters);
    RepresentativeVO representativeVO = new RepresentativeVO();
    representativeVO.setLeadReporters(leadReportersVO);
    representativeVO.setDataProviderId(1L);

    Mockito.when(dataflowRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(new Dataflow()));
    Mockito.when(userManagementControllerZull.getUserByEmail(Mockito.any()))
        .thenReturn(new UserRepresentationVO());
    Mockito.when(representativeRepository.findOneByDataflow_IdAndDataProvider_Id(Mockito.any(),
        Mockito.any())).thenReturn(repre);

    try {
      representativeServiceImpl.createRepresentative(1L, representativeVO);
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.REPRESENTATIVE_DUPLICATED, e.getMessage());
      throw e;
    }
  }

  /**
   * Gets the data provider by id test.
   *
   * @return the data provider by id test
   */
  @Test
  public void getDataProviderByIdTest() {
    representativeServiceImpl.getDataProviderById(1L);
    Mockito.verify(dataProviderMapper, times(1)).entityToClass(Mockito.any());
  }

  /**
   * Find data providers by ids test.
   */
  @Test
  public void findDataProvidersByIdsTest() {
    List<DataProvider> dataProviders = new ArrayList<>();
    dataProviders.add(new DataProvider());
    dataProviders.add(new DataProvider());
    Mockito.when(dataProviderRepository.findAllById(Mockito.any())).thenReturn(dataProviders);
    representativeServiceImpl.findDataProvidersByIds(new ArrayList<>());
    Mockito.verify(dataProviderMapper, times(2)).entityToClass(Mockito.any());
  }

  /**
   * Find Representatives by dataflowId and Email test.
   */
  @Test
  public void getRepresetativesByDataflowIdAndEmailTest() {
    List<Representative> representatives = new ArrayList<>();
    Mockito.when(representativeRepository.findByDataflowIdAndEmail(Mockito.any(), Mockito.any()))
        .thenReturn(representatives);
    representativeServiceImpl.getRepresetativesByDataflowIdAndEmail(1L, "provider@reportnet.net");
    Assert.assertEquals(0, representativeServiceImpl
        .getRepresetativesByDataflowIdAndEmail(1L, "provider@reportnet.net").size());
  }

  @Test
  public void exportFile() throws EEAException, IOException {
    List<Representative> representatives = new ArrayList<>();
    Representative representative = new Representative();
    DataProvider dataProvider = new DataProvider();
    dataProvider.setId(1L);
    representative.setDataProvider(dataProvider);
    representative.setLeadReporters(leadReporters);
    representatives.add(representative);
    Mockito.when(representativeRepository.findAllByDataflow_Id(1L)).thenReturn(representatives);
    byte[] expectedResult = "".getBytes();
    Assert.assertNotEquals(expectedResult, representativeServiceImpl.exportFile(1L));
  }

  @Test
  public void exportFileEmptyTest() throws EEAException, IOException {
    List<Representative> representatives = new ArrayList<>();
    Representative representative = new Representative();
    DataProvider dataProvider = new DataProvider();
    dataProvider.setId(1L);
    representative.setDataProvider(dataProvider);
    representatives.add(representative);
    Mockito.when(representativeRepository.findAllByDataflow_Id(1L)).thenReturn(representatives);
    byte[] expectedResult = "".getBytes();
    Assert.assertNotEquals(expectedResult, representativeServiceImpl.exportFile(1L));
  }

  @Test
  public void exportTemplateReportersFile() throws EEAException, IOException {
    List<DataProvider> dataProviderList = new ArrayList<>();
    DataProvider dataProvider = new DataProvider();
    dataProviderList.add(dataProvider);
    Mockito.when(dataProviderRepository.findAllByDataProviderGroup_id(1L))
        .thenReturn(dataProviderList);
    byte[] expectedResult = "".getBytes();
    Assert.assertNotEquals(expectedResult,
        representativeServiceImpl.exportTemplateReportersFile(1L));
  }

  @Test
  public void authorizeByRepresentativeIdTest() {
    Dataflow dataflow = new Dataflow();
    dataflow.setId(1L);
    Representative representative = new Representative();
    representative.setDataflow(dataflow);
    Optional<Representative> optionalRepresentative = Optional.of(representative);

    UserDetails userDetails = EeaUserDetails.create("user",
        new HashSet<>(Arrays.asList(ObjectAccessRoleEnum.DATAFLOW_STEWARD.getAccessRole(1L))));
    SecurityContextHolder.clearContext();
    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
        userDetails, "password", userDetails.getAuthorities()));

    Mockito.when(representativeRepository.findById(Mockito.anyLong()))
        .thenReturn(optionalRepresentative);

    Assert.assertTrue(representativeServiceImpl.authorizeByRepresentativeId(1L));
  }

  @Test
  public void createLeadReporterTest() throws EEAException {
    Representative representative = new Representative();
    representative.setId(1L);
    representative.setLeadReporters(leadReporters);
    RepresentativeVO representativeVO = new RepresentativeVO();
    representativeVO.setLeadReporters(leadReportersVO);
    representativeVO.setDataProviderId(1L);

    Mockito.when(representativeRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(representative));
    Mockito.when(userManagementControllerZull.getUserByEmail(Mockito.any()))
        .thenReturn(new UserRepresentationVO());
    Mockito.when(leadReporterMapper.classToEntity(Mockito.any())).thenReturn(leadReporter);
    Mockito.when(leadReporterRepository.save(Mockito.any())).thenReturn(leadReporter);

    Assert.assertEquals(1L,
        representativeServiceImpl.createLeadReporter(1L, leadReporterVO).longValue());
  }

  @Test(expected = EEAException.class)
  public void createLeadReporterNotFoundRepresentativeExceptionTest() throws EEAException {

    Mockito.when(representativeRepository.findById(Mockito.any())).thenReturn(Optional.empty());
    try {
      representativeServiceImpl.createLeadReporter(1L, leadReporterVO);
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.REPRESENTATIVE_NOT_FOUND, e.getMessage());
      throw e;
    }
  }

  @Test(expected = EEAException.class)
  public void createLeadReporterDuplicatedUserExceptionTest() throws EEAException {
    LeadReporter leadReporter2 = new LeadReporter(1L, "email@user.com", representative, null);
    leadReporters.add(leadReporter2);
    Representative representative = new Representative();
    representative.setId(1L);
    representative.setLeadReporters(leadReporters);
    RepresentativeVO representativeVO = new RepresentativeVO();
    representativeVO.setLeadReporters(leadReportersVO);
    representativeVO.setDataProviderId(1L);

    Mockito.when(representativeRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(representative));
    Mockito.when(userManagementControllerZull.getUserByEmail(Mockito.any()))
        .thenReturn(new UserRepresentationVO());
    try {
      representativeServiceImpl.createLeadReporter(1L, leadReporterVO);
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.USER_AND_COUNTRY_EXIST, e.getMessage());
      throw e;
    }
  }

  @Test
  public void updateLeadReporterTest() throws EEAException {
    Representative representative = new Representative();
    representative.setId(1L);
    representative.setLeadReporters(leadReporters);
    representative.setHasDatasets(true);
    representative.setDataflow(new Dataflow());
    representative.setDataProvider(new DataProvider());
    RepresentativeVO representativeVO = new RepresentativeVO();
    representativeVO.setLeadReporters(leadReportersVO);
    representativeVO.setDataProviderId(1L);
    leadReporterVO.setRepresentativeId(1L);
    ReportingDatasetVO dataset = new ReportingDatasetVO();
    dataset.setId(1L);
    List<ReportingDatasetVO> datasets = new ArrayList<>();
    datasets.add(dataset);
    Mockito.when(leadReporterRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(leadReporter));
    Mockito.when(representativeRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(representative));
    Mockito.when(userManagementControllerZull.getUserByEmail(Mockito.any()))
        .thenReturn(new UserRepresentationVO());

    Mockito
        .when(datasetMetabaseController
            .findReportingDataSetIdByDataflowIdAndProviderId(Mockito.any(), Mockito.any()))
        .thenReturn(datasets);
    Mockito.when(leadReporterMapper.classToEntity(Mockito.any())).thenReturn(leadReporter);
    Mockito.when(leadReporterRepository.save(Mockito.any())).thenReturn(leadReporter);

    Assert.assertEquals(1L,
        representativeServiceImpl.updateLeadReporter(leadReporterVO).longValue());
  }


  @Test(expected = EEAException.class)
  public void updateLeadReporterRepresentativeNotFoundExceptionTest() throws EEAException {
    Representative representative = new Representative();
    representative.setId(1L);
    representative.setLeadReporters(leadReporters);
    representative.setHasDatasets(true);
    representative.setDataflow(new Dataflow());
    representative.setDataProvider(new DataProvider());
    leadReporterVO.setRepresentativeId(1L);

    Mockito.when(leadReporterRepository.findById(Mockito.any())).thenReturn(Optional.empty());
    try {
      representativeServiceImpl.updateLeadReporter(leadReporterVO);
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.REPRESENTATIVE_NOT_FOUND, e.getMessage());
      throw e;
    }
  }

  @Test(expected = EEAException.class)
  public void updateLeadReporterRepresentativeDuplicatedExceptionTest() throws EEAException {
    Representative representative = new Representative();
    representative.setId(1L);
    representative.setLeadReporters(leadReporters);
    representative.setHasDatasets(true);
    representative.setDataflow(new Dataflow());
    representative.setDataProvider(new DataProvider());
    leadReporterVO.setRepresentativeId(1L);

    Mockito.when(leadReporterRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(leadReporter));
    Mockito.when(representativeRepository.findById(Mockito.any())).thenReturn(Optional.empty());
    try {
      representativeServiceImpl.updateLeadReporter(leadReporterVO);
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.REPRESENTATIVE_NOT_FOUND, e.getMessage());
      throw e;
    }
  }

  @Test
  public void deleteLeadReporterTest() throws EEAException {
    Mockito.when(leadReporterRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(leadReporter));
    doNothing().when(leadReporterRepository).deleteById(Mockito.any());

    representativeServiceImpl.deleteLeadReporter(1L);
    Mockito.verify(leadReporterRepository, times(1)).deleteById(Mockito.any());
  }

  @Test
  public void updateRepresentativeVisibilityRestrictionsTest() {
    representativeServiceImpl.updateRepresentativeVisibilityRestrictions(1L, 1L, true);
    Mockito.verify(representativeRepository, times(1)).updateRepresentativeVisibilityRestrictions(
        Mockito.anyLong(), Mockito.anyLong(), Mockito.anyBoolean());
  }

  @Test
  public void findDataProvidersByCodeTest() {
    assertNotNull("is null", representativeServiceImpl.findDataProvidersByCode("ES"));
  }

  @Test
  public void importFile() throws EEAException, IOException {
    ReflectionTestUtils.setField(representativeServiceImpl, "delimiter", ',');
    String csv = "Representing,Email\r\n" + "AL,provider1@reportnet.net\r\n"
        + "AL,provider2@reportnet.net\r\n";
    MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal.csv", "cvs", csv.getBytes());
    UserRepresentationVO user = new UserRepresentationVO();
    user.setEmail("provider1@reportnet.net");

    DataProvider dataProvider = new DataProvider();
    dataProvider.setId(1L);
    dataProvider.setCode("AL");
    List<DataProvider> dataProviderList = new ArrayList<>();
    dataProviderList.add(dataProvider);

    Mockito.when(dataProviderRepository.findAllByDataProviderGroup_id(Mockito.any()))
        .thenReturn(dataProviderList);
    Mockito.when(userManagementControllerZull.getUserByEmail(Mockito.any())).thenReturn(user);
    Dataflow dataflow = new Dataflow();
    dataflow.setId(1L);
    dataflow.setType(TypeDataflowEnum.BUSINESS);
    Mockito.when(dataflowRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(dataflow));

    representativeServiceImpl.importLeadReportersFile(1L, 2L, file);
    Mockito.verify(representativeRepository, times(1)).saveAll(Mockito.any());

  }

  @Test
  public void getProviderIdsTest() throws EEAException {
    Collection<SimpleGrantedAuthority> authorities = new HashSet<>();
    authorities.add(new SimpleGrantedAuthority("ROLE_PROVIDER-AL-NATIONAL_COORDINATOR"));

    DataProvider dataProvider = new DataProvider();
    dataProvider.setId(1L);
    dataProvider.setCode("AL");
    List<DataProvider> dataProviderList = new ArrayList<>();
    dataProviderList.add(dataProvider);

    DataProviderVO dataProviderVO = new DataProviderVO();
    dataProviderVO.setId(1L);
    dataProviderVO.setCode("AL");
    List<DataProviderVO> dataProviderListVO = new ArrayList<>();
    dataProviderListVO.add(dataProviderVO);

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.doReturn(authorities).when(authentication).getAuthorities();
    Mockito.when(dataProviderRepository.findByCode(Mockito.any())).thenReturn(dataProviderList);
    Mockito.when(dataProviderMapper.entityListToClass(Mockito.any()))
        .thenReturn(dataProviderListVO);
    assertEquals(Arrays.asList(1L), representativeServiceImpl.getProviderIds());
  }

  @Test
  public void findRepresentativesByDataflowIdAnDataproviderListTest() {
    List<Representative> representatives = new ArrayList<>();
    Representative representative = new Representative();
    representative.setId(1L);
    representatives.add(representative);
    List<RepresentativeVO> representativeVOs = new ArrayList<>();
    RepresentativeVO representativeVO = new RepresentativeVO();
    representativeVO.setId(1L);
    representativeVOs.add(representativeVO);
    Mockito.when(representativeRepository.findByDataflowIdAndDataProviderIdIn(Mockito.anyLong(),
        Mockito.any())).thenReturn(representatives);
    Mockito.when(representativeMapper.entityListToClass(Mockito.anyList()))
        .thenReturn(representativeVOs);
    assertEquals(representativeVOs, representativeServiceImpl
        .findRepresentativesByDataflowIdAndDataproviderList(1L, Arrays.asList(1L)));
  }

  @Test
  public void validateLeadReportersSendNotificationTest() throws EEAException, IOException {
    List<LeadReporterVO> listLeadReporterVO = new ArrayList<>();
    LeadReporterVO leadReporterVO = new LeadReporterVO();
    leadReporterVO.setInvalid(true);
    leadReporterVO.setRepresentativeId(1L);
    leadReporterVO.setEmail("email");
    leadReporterVO.setId(1L);
    LeadReporter leadReporter = new LeadReporter();
    leadReporter.setInvalid(true);
    leadReporter.setRepresentative(representative);
    leadReporter.setEmail("email");
    leadReporter.setId(1L);
    Representative repre = new Representative();
    leadReporter.setRepresentative(repre);
    leadReporter.setRepresentative(representative);
    listLeadReporterVO.add(leadReporterVO);
    List<RepresentativeVO> listRepresentativeVO = new ArrayList<>();
    RepresentativeVO representativeVO = new RepresentativeVO();
    representativeVO.setLeadReporters(listLeadReporterVO);
    listRepresentativeVO.add(representativeVO);

    authentication = Mockito.mock(Authentication.class);
    securityContext = Mockito.mock(SecurityContext.class);
    securityContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(securityContext);

    Mockito.when(leadReporterRepository.findById(Mockito.anyLong()))
        .thenReturn(Optional.of(leadReporter));
    Mockito.when(representativeRepository.findById(Mockito.anyLong()))
        .thenReturn(Optional.of(repre));
    Mockito.when(leadReporterRepository.save(Mockito.any())).thenReturn(leadReporter);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("name");

    Mockito.when(representativeServiceImpl.getRepresetativesByIdDataFlow(Mockito.anyLong()))
        .thenReturn(listRepresentativeVO);
    Mockito.doNothing().when(kafkaSenderUtils).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());

    representativeServiceImpl.validateLeadReporters(1L, true);

    Mockito.verify(leadReporterRepository, Mockito.atLeastOnce()).save(Mockito.any());
    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
  }

  @Test
  public void validateLeadReportersExceptionTest() throws EEAException, IOException {
    List<LeadReporterVO> listLeadReporterVO = new ArrayList<>();
    LeadReporterVO leadReporterVO = new LeadReporterVO();
    leadReporterVO.setInvalid(true);
    leadReporterVO.setRepresentativeId(1L);
    leadReporterVO.setEmail("email");
    leadReporterVO.setId(1L);
    LeadReporter leadReporter = new LeadReporter();
    leadReporter.setInvalid(true);
    leadReporter.setRepresentative(representative);
    leadReporter.setEmail("email");
    leadReporter.setId(1L);
    Representative repre = new Representative();
    leadReporter.setRepresentative(repre);
    leadReporter.setRepresentative(representative);
    listLeadReporterVO.add(leadReporterVO);
    List<RepresentativeVO> listRepresentativeVO = new ArrayList<>();
    RepresentativeVO representativeVO = new RepresentativeVO();
    representativeVO.setLeadReporters(listLeadReporterVO);
    listRepresentativeVO.add(representativeVO);

    authentication = Mockito.mock(Authentication.class);
    securityContext = Mockito.mock(SecurityContext.class);
    securityContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(securityContext);

    Mockito.when(leadReporterRepository.findById(Mockito.anyLong()))
        .thenThrow(IllegalArgumentException.class);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("name");

    Mockito.when(representativeServiceImpl.getRepresetativesByIdDataFlow(Mockito.anyLong()))
        .thenReturn(listRepresentativeVO);
    representativeServiceImpl.validateLeadReporters(1L, true);
    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
  }

  @Test(expected = EEAException.class)
  public void checkRestrictFromPublicExceptionTest() throws EEAException {
    try {
      Mockito.when(representativeRepository.findAllByDataflow_Id(Mockito.anyLong()))
          .thenReturn(null);
      Mockito.when(representativeMapper.entityListToClass(Mockito.anyList())).thenReturn(null);
      Mockito.when(representativeServiceImpl.getRepresetativesByIdDataFlow(Mockito.anyLong()))
          .thenReturn(null);
      representativeServiceImpl.checkRestrictFromPublic(1L, 1L);
    } catch (EEAException e) {
      assertNotNull(e);
      throw e;
    }
  }

  @Test
  public void checkRestrictFromPublicTest() throws EEAException {
    RepresentativeVO representativeVO = new RepresentativeVO();
    representativeVO.setId(1L);
    representativeVO.setRestrictFromPublic(true);
    representativeVO.setDataProviderId(1L);
    List<RepresentativeVO> representativesVO = new ArrayList<>();
    List<Representative> representatives = new ArrayList<>();
    Representative representative = new Representative();
    representative.setId(1L);
    representative.setRestrictFromPublic(true);
    representatives.add(representative);
    representativesVO.add(representativeVO);
    Mockito.when(representativeRepository.findAllByDataflow_Id(Mockito.anyLong()))
        .thenReturn(representatives);
    Mockito.when(representativeMapper.entityListToClass(Mockito.anyList()))
        .thenReturn(representativesVO);
    Mockito.when(representativeServiceImpl.getRepresetativesByIdDataFlow(Mockito.anyLong()))
        .thenReturn(representativesVO);
    representativeServiceImpl.checkRestrictFromPublic(1L, 1L);
    assertEquals(representativeServiceImpl.checkRestrictFromPublic(1L, 1L),
        representative.isRestrictFromPublic());
  }

  @Test
  public void checkIfDataHaveBeenReleaseTest() throws EEAException {
    List<ReportingDatasetVO> reportings = new ArrayList<>();
    ReportingDatasetVO reporting = new ReportingDatasetVO();
    reporting.setId(1L);
    reporting.setDataProviderId(1L);
    reporting.setIsReleased(true);
    reportings.add(reporting);
    List<SnapshotVO> snapshots = new ArrayList<>();
    SnapshotVO snapshot = new SnapshotVO();
    snapshot.setId(1L);
    snapshots.add(snapshot);
    Mockito.when(datasetMetabaseController.findReportingDataSetIdByDataflowId(Mockito.anyLong()))
        .thenReturn(reportings);
    Mockito.when(datasetSnapshotController.getSnapshotsEnabledByIdDataset(Mockito.anyLong()))
        .thenReturn(snapshots);
    assertTrue(representativeServiceImpl.checkDataHaveBeenRelease(1L, 1L));
  }

  @Test(expected = EEAException.class)
  public void checkIfDataHaveBeenReleaseExceptionTest() throws EEAException {
    try {
      Mockito.when(datasetMetabaseController.findReportingDataSetIdByDataflowId(Mockito.anyLong()))
          .thenReturn(null);
      representativeServiceImpl.checkDataHaveBeenRelease(1L, 1L);
    } catch (EEAException e) {
      assertNotNull(e);
      throw e;
    }
  }

  @Test
  public void checkIfDataHaveBeenReleaseFalseTest() throws EEAException {
    List<ReportingDatasetVO> reportings = new ArrayList<>();
    ReportingDatasetVO reporting = new ReportingDatasetVO();
    reporting.setId(1L);
    reporting.setDataProviderId(1L);
    reporting.setIsReleased(false);
    reportings.add(reporting);
    Mockito.when(datasetMetabaseController.findReportingDataSetIdByDataflowId(Mockito.anyLong()))
        .thenReturn(reportings);
    assertFalse(representativeServiceImpl.checkDataHaveBeenRelease(1L, 1L));
  }

  @Test
  public void findFmeUsersTest() {
    FMEUser user = new FMEUser();
    FMEUserVO userVO = new FMEUserVO();
    Mockito.when(fmeUserRepository.findAll()).thenReturn(Arrays.asList(user));
    Mockito.when(fmeUserMapper.entityListToClass(Mockito.anyList()))
        .thenReturn(Arrays.asList(userVO));
    assertNotNull(representativeServiceImpl.findFmeUsers());
  }

  @Test(expected = EEAException.class)
  public void getProviderIdsEEAExceptionTest() throws EEAException {
    Collection<SimpleGrantedAuthority> authorities = new HashSet<>();
    authorities.add(new SimpleGrantedAuthority("test"));

    DataProvider dataProvider = new DataProvider();
    dataProvider.setId(1L);
    List<DataProvider> dataProviderList = new ArrayList<>();
    dataProviderList.add(dataProvider);

    DataProviderVO dataProviderVO = new DataProviderVO();
    dataProviderVO.setId(1L);
    List<DataProviderVO> dataProviderListVO = new ArrayList<>();
    dataProviderListVO.add(dataProviderVO);

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.doReturn(authorities).when(authentication).getAuthorities();
    Mockito.when(dataProviderRepository.findByCode(Mockito.any())).thenReturn(dataProviderList);
    Mockito.when(dataProviderMapper.entityListToClass(Mockito.any()))
        .thenReturn(dataProviderListVO);
    try {
      representativeServiceImpl.getProviderIds();
    } catch (EEAException e) {
      assertNotNull(e);
      throw e;
    }
  }

  @Test(expected = EEAException.class)
  public void updateLeadReporterRepresentativeNotFoundTest() throws EEAException {
    LeadReporter leadReporter = new LeadReporter();
    leadReporter.setId(1L);
    Representative representative = new Representative();
    representative.setId(1L);
    leadReporter.setRepresentative(representative);
    LeadReporterVO leadReporterVO = new LeadReporterVO();
    leadReporterVO.setId(2L);
    leadReporterVO.setRepresentativeId(2L);
    Mockito.when(leadReporterRepository.findById(Mockito.anyLong()))
        .thenReturn(Optional.of(leadReporter));
    Mockito.when(representativeRepository.findById(Mockito.anyLong()))
        .thenReturn(Optional.of(representative));
    try {
      representativeServiceImpl.updateLeadReporter(leadReporterVO);
    } catch (EEAException e) {
      assertEquals(EEAErrorMessage.REPRESENTATIVE_NOT_FOUND, e.getMessage());
      throw e;
    }
  }

  @Test
  public void createLeadReporterAndSetInvalidTest() throws EEAException {
    LeadReporterVO leadReporterVO = new LeadReporterVO();
    leadReporterVO.setEmail("email@email.com");
    Representative representative = new Representative();
    representative.setId(1L);
    LeadReporter leadReporter = new LeadReporter();
    leadReporter.setId(1L);
    leadReporter.setEmail("email2@email.com");
    representative.setLeadReporters(Arrays.asList(leadReporter));
    Mockito.when(representativeRepository.findById(Mockito.anyLong()))
        .thenReturn(Optional.of(representative));
    Mockito.when(leadReporterMapper.classToEntity(Mockito.any())).thenReturn(leadReporter);
    Mockito.when(userManagementControllerZull.getUserByEmail(Mockito.anyString())).thenReturn(null);
    Mockito.when(leadReporterRepository.save(Mockito.any())).thenReturn(leadReporter);
    representativeServiceImpl.createLeadReporter(1L, leadReporterVO);
    assertTrue(leadReporter.getInvalid() == true);
  }
}
