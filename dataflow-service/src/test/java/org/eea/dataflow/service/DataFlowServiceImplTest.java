package org.eea.dataflow.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.eea.dataflow.mapper.DataflowMapper;
import org.eea.dataflow.mapper.DataflowNoContentMapper;
import org.eea.dataflow.mapper.DocumentMapper;
import org.eea.dataflow.persistence.domain.Contributor;
import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.dataflow.persistence.domain.DataflowWithRequestType;
import org.eea.dataflow.persistence.domain.Document;
import org.eea.dataflow.persistence.repository.ContributorRepository;
import org.eea.dataflow.persistence.repository.DataflowRepository;
import org.eea.dataflow.persistence.repository.DocumentRepository;
import org.eea.dataflow.persistence.repository.UserRequestRepository;
import org.eea.dataflow.service.impl.DataflowServiceImpl;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.enums.TypeRequestEnum;
import org.eea.interfaces.vo.document.DocumentVO;
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

  /** The dataflow service impl. */
  @InjectMocks
  private DataflowServiceImpl dataflowServiceImpl;

  /** The dataflow repository. */
  @Mock
  private DataflowRepository dataflowRepository;


  /** The user request repository. */
  @Mock
  private UserRequestRepository userRequestRepository;

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

  /** The dataset controller. */
  @Mock
  private DataSetMetabaseControllerZuul datasetMetabaseController;

  @Mock
  private DocumentMapper documentMapper;

  /** The dataflows. */
  private List<Dataflow> dataflows;

  /** The pageable. */
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
   * @throws EEAException the EEA exception
   */
  @Test
  public void getById() throws EEAException {
    DataFlowVO dataFlowVO = new DataFlowVO();
    when(dataflowMapper.entityToClass(Mockito.any())).thenReturn(dataFlowVO);
    when(datasetMetabaseController.findDataSetIdByDataflowId(1L)).thenReturn(new ArrayList<>());
    dataflowServiceImpl.getById(1L);
    dataFlowVO.setDatasets(new ArrayList<>());
    assertEquals("fail", dataFlowVO, dataflowServiceImpl.getById(1L));
  }


  /**
   * Gets the by status.
   *
   * @return the by status
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
    when(dataflowRepository.findPendingAccepted(Mockito.any())).thenReturn(dataflows);
    DataFlowVO dfVO = new DataFlowVO();
    dfVO.setId(1L);
    List<DataFlowVO> dataflowsVO = new ArrayList<>();
    dataflowsVO.add(dfVO);
    when(dataflowNoContentMapper.entityListToClass(Mockito.any())).thenReturn(dataflowsVO);
    dataflowServiceImpl.getPendingAccepted(Mockito.any());
    assertEquals("fail", dataflowsVO, dataflowServiceImpl.getPendingAccepted(Mockito.any()));
  }

  /**
   * Gets the pending by user.
   *
   * @return the pending by user
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
   * @throws EEAException the EEA exception
   */
  @Test
  public void getCompletedEmpty() throws EEAException {
    when(dataflowRepository.findCompleted(Mockito.any(), Mockito.any()))
        .thenReturn(new ArrayList<>());
    dataflowServiceImpl.getCompleted(1L, pageable);
    assertEquals("fail", new ArrayList<>(), dataflowServiceImpl.getCompleted(1L, pageable));
  }

  /**
   * Gets the completed.
   *
   * @return the completed
   * @throws EEAException the EEA exception
   */
  @Test
  public void getCompleted() throws EEAException {
    when(dataflowRepository.findCompleted(Mockito.any(), Mockito.any())).thenReturn(dataflows);
    dataflowServiceImpl.getCompleted(1L, pageable);
    assertEquals("fail", new ArrayList<>(), dataflowServiceImpl.getCompleted(1L, pageable));
    dataflows.add(new Dataflow());
    dataflows.add(new Dataflow());
    when(dataflowRepository.findCompleted(Mockito.any(), Mockito.any())).thenReturn(dataflows);
    dataflowServiceImpl.getCompleted(1L, pageable);
    assertEquals("fail", new ArrayList<>(), dataflowServiceImpl.getCompleted(1L, pageable));
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
    dataflowServiceImpl.updateUserRequestStatus(1L, TypeRequestEnum.ACCEPTED);
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
    dataflowServiceImpl.addContributorToDataflow(1L, 1L);
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
    dataflowServiceImpl.removeContributorFromDataflow(1L, 1L);
    Mockito.verify(contributorRepository, times(1)).removeContributorFromDataset(Mockito.any(),
        Mockito.any());
  }

  /**
   * Insert document exception 1 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void insertDocumentException1Test() throws EEAException {
    dataflowServiceImpl.insertDocument(null, null, null, null);
  }

  /**
   * Insert document exception 2 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void insertDocumentException2Test() throws EEAException {
    dataflowServiceImpl.insertDocument(1L, null, null, null);
  }

  /**
   * Insert document exception 3 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void insertDocumentException3Test() throws EEAException {
    dataflowServiceImpl.insertDocument(1L, "filename", null, null);
  }

  /**
   * Insert document exception 4 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void insertDocumentException4Test() throws EEAException {
    dataflowServiceImpl.insertDocument(1L, "filename", "ES", null);
  }

  /**
   * Insert document exception 5 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void insertDocumentException5Test() throws EEAException {
    when(dataflowRepository.findById(Mockito.any())).thenReturn(Optional.empty());
    dataflowServiceImpl.insertDocument(1L, "filename", "ES", "desc");
  }

  /**
   * Insert document success test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void insertDocumentSuccessTest() throws EEAException {
    when(dataflowRepository.findById(Mockito.any())).thenReturn(Optional.of(new Dataflow()));
    when(documentRepository.save(Mockito.any())).thenReturn(new Document());
    dataflowServiceImpl.insertDocument(1L, "filename", "ES", "desc");
    Mockito.verify(documentRepository, times(1)).save(Mockito.any());
  }

  /**
   * Delete document exception 1 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void deleteDocumentException1Test() throws EEAException {
    dataflowServiceImpl.deleteDocument(null);
  }

  /**
   * Delete document success test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteDocumentSuccessTest() throws EEAException {
    doNothing().when(documentRepository).delete(Mockito.any());
    dataflowServiceImpl.deleteDocument(1L);
    Mockito.verify(documentRepository, times(1)).deleteById(Mockito.any());
  }

  /**
   * Gets the document by id exception 1 test.
   *
   * @return the document by id exception 1 test
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void getDocumentByIdException1Test() throws EEAException {
    dataflowServiceImpl.getDocumentById(null);
  }

  /**
   * Gets the document by id exception 2 test.
   *
   * @return the document by id exception 2 test
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void getDocumentByIdException2Test() throws EEAException {
    when(documentRepository.findById(Mockito.any())).thenReturn(Optional.empty());
    dataflowServiceImpl.getDocumentById(1L);
  }

  @Test
  public void getDocumentByIdSuccessTest() throws EEAException {
    DocumentVO documentVO = new DocumentVO();
    documentVO.setId(1L);
    when(documentRepository.findById(Mockito.any())).thenReturn(Optional.of(new Document()));
    when(documentMapper.entityToClass(Mockito.any())).thenReturn(documentVO);
    assertEquals("not equals", documentVO, dataflowServiceImpl.getDocumentById(1L));
  }

}
