package org.eea.dataflow.service.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.eea.dataflow.mapper.DataProviderMapper;
import org.eea.dataflow.mapper.RepresentativeMapper;
import org.eea.dataflow.persistence.domain.DataProvider;
import org.eea.dataflow.persistence.domain.DataProviderCode;
import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.dataflow.persistence.domain.Representative;
import org.eea.dataflow.persistence.domain.User;
import org.eea.dataflow.persistence.repository.DataProviderRepository;
import org.eea.dataflow.persistence.repository.DataflowRepository;
import org.eea.dataflow.persistence.repository.RepresentativeRepository;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.dataflow.DataProviderCodeVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.ums.UserRepresentationVO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/** The Class RepresentativeServiceImplTest. */
public class RepresentativeServiceImplTest {

  /** The representative service impl. */
  @InjectMocks
  private RepresentativeServiceImpl representativeServiceImpl;

  /** The dataflow repository. */
  @Mock
  private DataflowRepository dataflowRepository;

  /** The user management controller zull. */
  @Mock
  private UserManagementControllerZull userManagementControllerZull;

  /** The representative repository. */
  @Mock
  private RepresentativeRepository representativeRepository;

  /** The representative mapper. */
  @Mock
  private RepresentativeMapper representativeMapper;

  /** The data provider repository. */
  @Mock
  private DataProviderRepository dataProviderRepository;

  /** The data provider mapper. */
  @Mock
  private DataProviderMapper dataProviderMapper;

  /** The representative. */
  private Representative representative;

  /** The representative VO. */
  private RepresentativeVO representativeVO;

  /** The array id. */
  private List<Representative> arrayId;

  /** The emails. */
  private List<String> emails;

  private User user;

  private Set<User> users;

  /** Inits the mocks. */
  @Before
  public void initMocks() {
    Dataflow dataflow = new Dataflow();
    dataflow.setId(1L);
    representative = new Representative();
    representative.setId(1L);
    representative.setDataflow(dataflow);
    representative.setReporters(new HashSet<User>());
    emails = new ArrayList<>();
    emails.add("email");
    representativeVO = new RepresentativeVO();
    representativeVO.setId(1L);
    representativeVO.setProviderAccounts(emails);
    arrayId = new ArrayList<>();
    arrayId.add(new Representative());
    Set<Representative> representatives = new HashSet<>();
    representatives.add(representative);
    user = new User("email@host.com", representatives);
    users = new HashSet<>();
    users.add(user);
    MockitoAnnotations.initMocks(this);
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
   * Update dataflow representative exception 1 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateDataflowRepresentativeException1Test() throws EEAException {
    try {
      representativeServiceImpl.updateDataflowRepresentative(null);
    } catch (EEAException e) {
      assertEquals("error in the message", EEAErrorMessage.DATAFLOW_NOTFOUND, e.getMessage());
    }
  }

  /**
   * Update dataflow representative exception 2 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateDataflowRepresentativeException2Test() throws EEAException {
    when(representativeRepository.findById(Mockito.any())).thenReturn(Optional.empty());
    try {
      representativeServiceImpl.updateDataflowRepresentative(representativeVO);
    } catch (EEAException e) {
      assertEquals("error in the message", EEAErrorMessage.REPRESENTATIVE_NOT_FOUND,
          e.getMessage());
    }
  }

  /**
   * Update dataflow representative success test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateDataflowRepresentativeSuccessTest() throws EEAException {
    representativeVO.setProviderAccounts(Arrays.asList("user"));
    representativeVO.setDataProviderId(1L);
    when(representativeRepository.findById(Mockito.any())).thenReturn(Optional.of(representative));
    when(representativeRepository.save(Mockito.any())).thenReturn(representative);
    Mockito.when(userManagementControllerZull.getUserByEmail(Mockito.any()))
        .thenReturn(new UserRepresentationVO());
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
    representative.setReporters(users);
    representativeVO.setDataProviderId(null);
    DataProvider dataProvider = new DataProvider();
    dataProvider.setId(1L);
    representative.setDataProvider(dataProvider);
    when(representativeRepository.findById(Mockito.any())).thenReturn(Optional.of(representative));
    when(representativeRepository.save(Mockito.any())).thenReturn(representative);
    Mockito.when(userManagementControllerZull.getUserByEmail(Mockito.any()))
        .thenReturn(new UserRepresentationVO());
    assertEquals("error in the message", (Long) 1L,
        representativeServiceImpl.updateDataflowRepresentative(representativeVO));
  }

  /**
   * Update dataflow representative exception 3 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateDataflowRepresentativeException3Test() throws EEAException {
    representative.setReporters(users);
    representativeVO.setDataProviderId(null);
    representativeVO.setReceiptDownloaded(false);
    representativeVO.setReceiptOutdated(false);
    DataProvider dataProvider = new DataProvider();
    dataProvider.setId(1L);
    representative.setDataProvider(dataProvider);
    representative.setReceiptDownloaded(false);
    representative.setReceiptOutdated(false);
    representative.setReporters(new HashSet<User>());
    when(representativeRepository.findById(Mockito.any())).thenReturn(Optional.of(representative));
    when(representativeRepository.findByDataProviderIdAndDataflowId(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(arrayId));
    when(representativeRepository.save(Mockito.any())).thenReturn(representative);
    Mockito.when(representativeMapper.classToEntity(Mockito.any())).thenReturn(representative);
    Mockito.when(userManagementControllerZull.getUserByEmail(Mockito.any()))
        .thenReturn(new UserRepresentationVO());
    try {
      representativeServiceImpl.updateDataflowRepresentative(representativeVO);
    } catch (EEAException e) {
      assertEquals("error in the message", EEAErrorMessage.REPRESENTATIVE_DUPLICATED,
          e.getMessage());
    }
  }

  /**
   * Gets the all data provider types success test.
   *
   * @return the all data provider types success test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getAllDataProviderTypesSuccessTest() throws EEAException {
    List<DataProviderCode> dataProviderCodes = new ArrayList<>();
    dataProviderCodes.add(new DataProviderCode() {

      @Override
      public String getLabel() {
        return "Country";
      }

      @Override
      public Long getDataProviderGroupId() {
        return 1L;
      }
    });
    List<DataProviderCodeVO> dataProviderCodeVOs = new ArrayList<>();
    DataProviderCodeVO dataProviderCodeVO = new DataProviderCodeVO();
    dataProviderCodeVO.setDataProviderGroupId(1L);
    dataProviderCodeVO.setLabel("Country");
    dataProviderCodeVOs.add(dataProviderCodeVO);
    when(dataProviderRepository.findDistinctCode()).thenReturn(dataProviderCodes);
    assertEquals("error in the message", dataProviderCodeVOs,
        representativeServiceImpl.getAllDataProviderTypes());
  }

  /**
   * Gets the represetatives by id data flow exception test.
   *
   * @return the represetatives by id data flow exception test
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
   * Gets the represetatives by id data flow success test.
   *
   * @return the represetatives by id data flow success test
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
    when(dataProviderRepository.findAllByGroupId(1L)).thenReturn(new ArrayList<>());
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
    representative.setReporters(new HashSet<User>());
    RepresentativeVO representativeVO = new RepresentativeVO();
    representativeVO.setProviderAccounts(Arrays.asList("sample@email.net"));
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
    representativeVO.setProviderAccounts(Arrays.asList("sample@email.net"));
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
  @Test(expected = EEAException.class)
  public void createRepresentativeUserRequestotFoundExceptionTest() throws EEAException {
    RepresentativeVO representativeVO = new RepresentativeVO();
    representativeVO.setProviderAccounts(Arrays.asList("sample@email.net"));
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
    representative.setReporters(new HashSet<>());
    representatives.add(representative);
    Mockito.when(representativeRepository.findAllByDataflow_Id(1L)).thenReturn(representatives);
    byte[] expectedResult = "".getBytes();
    Assert.assertNotEquals(expectedResult, representativeServiceImpl.exportFile(1L));
  }

  @Test
  public void exportTemplateReportersFile() throws EEAException, IOException {
    List<DataProvider> dataProviderList = new ArrayList();
    DataProvider dataProvider = new DataProvider();
    dataProviderList.add(dataProvider);
    Mockito.when(dataProviderRepository.findAllByGroupId(1L)).thenReturn(dataProviderList);
    byte[] expectedResult = "".getBytes();
    Assert.assertNotEquals(expectedResult,
        representativeServiceImpl.exportTemplateReportersFile(1L));
  }


}
