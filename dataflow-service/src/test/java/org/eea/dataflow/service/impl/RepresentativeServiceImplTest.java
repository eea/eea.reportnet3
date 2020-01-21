package org.eea.dataflow.service.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.eea.dataflow.mapper.DataProviderMapper;
import org.eea.dataflow.mapper.RepresentativeMapper;
import org.eea.dataflow.persistence.domain.DataProvider;
import org.eea.dataflow.persistence.domain.DataProviderCode;
import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.dataflow.persistence.domain.Representative;
import org.eea.dataflow.persistence.repository.DataProviderRepository;
import org.eea.dataflow.persistence.repository.RepresentativeRepository;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataflow.DataProviderCodeVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * The Class RepresentativeServiceImplTest.
 */
public class RepresentativeServiceImplTest {

  /** The representative service impl. */
  @InjectMocks
  private RepresentativeServiceImpl representativeServiceImpl;

  /** The representative repository. */
  @Mock
  private RepresentativeRepository representativeRepository;

  /** The data provider repository. */
  @Mock
  private DataProviderRepository dataProviderRepository;

  /** The representative mapper. */
  @Mock
  private RepresentativeMapper representativeMapper;

  /** The data provider mapper. */
  @Mock
  private DataProviderMapper dataProviderMapper;

  /** The representative. */
  private Representative representative;

  /** The representative VO. */
  private RepresentativeVO representativeVO;

  /** The array id. */
  private List<Representative> arrayId;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    Dataflow dataflow = new Dataflow();
    dataflow.setId(1L);
    representative = new Representative();
    representative.setId(1L);
    representative.setDataflow(dataflow);
    representativeVO = new RepresentativeVO();
    representativeVO.setId(1L);
    representativeVO.setProviderAccount("email");
    arrayId = new ArrayList<>();
    arrayId.add(new Representative());
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Insert representative exception 1 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void insertRepresentativeException1Test() throws EEAException {
    try {
      representativeServiceImpl.insertRepresentative(null, representativeVO);
    } catch (EEAException e) {
      assertEquals("error in the message", EEAErrorMessage.DATAFLOW_NOTFOUND, e.getMessage());
    }
  }

  /**
   * Insert representative exception 2 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void insertRepresentativeException2Test() throws EEAException {
    try {
      representativeServiceImpl.insertRepresentative(1L, null);
    } catch (EEAException e) {
      assertEquals("error in the message", EEAErrorMessage.DATAFLOW_NOTFOUND, e.getMessage());
    }
  }

  /**
   * Insert representative exception 3 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void insertRepresentativeException3Test() throws EEAException {
    representativeVO.setDataProviderId(1L);
    when(representativeRepository.findBydataProviderIdAnduserMailAnddataflowId(Mockito.any(),
        Mockito.any(), Mockito.any())).thenReturn(Optional.of(arrayId));
    try {
      representativeServiceImpl.insertRepresentative(1L, representativeVO);
    } catch (EEAException e) {
      assertEquals("error in the message", EEAErrorMessage.REPRESENTATIVE_DUPLICATED,
          e.getMessage());
    }
  }

  /**
   * Insert representative exception 4 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void insertRepresentativeException4Test() throws EEAException {
    representativeVO.setDataProviderId(null);
    when(representativeRepository.findBydataProviderIdAnduserMailAnddataflowId(Mockito.any(),
        Mockito.any(), Mockito.any())).thenReturn(Optional.of(arrayId));
    try {
      representativeServiceImpl.insertRepresentative(1L, representativeVO);
    } catch (EEAException e) {
      assertEquals("error in the message", EEAErrorMessage.REPRESENTATIVE_NOT_FOUND,
          e.getMessage());
    }
  }

  /**
   * Insert representative exception 5 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void insertRepresentativeException5Test() throws EEAException {
    representativeVO.setDataProviderId(1L);
    representativeVO.setProviderAccount("");
    when(representativeRepository.findBydataProviderIdAnduserMailAnddataflowId(Mockito.any(),
        Mockito.any(), Mockito.any())).thenReturn(Optional.of(arrayId));
    try {
      representativeServiceImpl.insertRepresentative(1L, representativeVO);
    } catch (EEAException e) {
      assertEquals("error in the message", EEAErrorMessage.REPRESENTATIVE_NOT_FOUND,
          e.getMessage());
    }
  }

  /**
   * Insert representative success test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void insertRepresentativeSuccessTest() throws EEAException {
    representativeVO.setDataProviderId(1L);
    when(representativeRepository.findBydataProviderIdAnduserMailAnddataflowId(Mockito.any(),
        Mockito.any(), Mockito.any())).thenReturn(Optional.empty());
    when(representativeMapper.classToEntity(Mockito.any())).thenReturn(representative);
    when(representativeRepository.save(Mockito.any())).thenReturn(representative);
    assertEquals("error in the message", (Long) 1L,
        representativeServiceImpl.insertRepresentative(1L, representativeVO));
  }

  /**
   * Delete dataflow representative exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteDataflowRepresentativeExceptionTest() throws EEAException {
    try {
      representativeServiceImpl.deleteDataflowRepresentative(null);
    } catch (EEAException e) {
      assertEquals("error in the message", EEAErrorMessage.DATAFLOW_NOTFOUND, e.getMessage());
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
    representativeVO.setProviderAccount("user");
    representativeVO.setDataProviderId(1L);
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
    representative.setUserMail("mail");
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
   * Update dataflow representative exception 3 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateDataflowRepresentativeException3Test() throws EEAException {
    representative.setUserMail("mail");
    representativeVO.setDataProviderId(null);
    DataProvider dataProvider = new DataProvider();
    dataProvider.setId(1L);
    representative.setDataProvider(dataProvider);
    when(representativeRepository.findById(Mockito.any())).thenReturn(Optional.of(representative));
    when(representativeRepository.findBydataProviderIdAnduserMailAnddataflowId(Mockito.any(),
        Mockito.any(), Mockito.any())).thenReturn(Optional.of(arrayId));
    when(representativeRepository.save(Mockito.any())).thenReturn(representative);
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
}
