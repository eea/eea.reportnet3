package org.eea.dataflow.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.Optional;
import org.eea.dataflow.mapper.DocumentMapper;
import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.dataflow.persistence.domain.Document;
import org.eea.dataflow.persistence.repository.DataflowRepository;
import org.eea.dataflow.persistence.repository.DocumentRepository;
import org.eea.dataflow.service.impl.DataflowDocumentServiceImpl;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.document.DocumentVO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The Class DataFlowServiceImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class DataFlowDocumentServiceImplTest {

  /** The dataflow service impl. */
  @InjectMocks
  private DataflowDocumentServiceImpl dataflowServiceImpl;

  /** The dataflow repository. */
  @Mock
  private DataflowRepository dataflowRepository;

  /** The document repository. */
  @Mock
  private DocumentRepository documentRepository;

  /** The document mapper. */
  @Mock
  private DocumentMapper documentMapper;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
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
    doNothing().when(documentRepository).deleteById(Mockito.any());
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
    dataflowServiceImpl.getDocumentInfoById(null);
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
    dataflowServiceImpl.getDocumentInfoById(1L);
  }

  /**
   * Gets the document by id success test.
   *
   * @return the document by id success test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getDocumentByIdSuccessTest() throws EEAException {
    DocumentVO documentVO = new DocumentVO();
    documentVO.setId(1L);
    when(documentRepository.findById(Mockito.any())).thenReturn(Optional.of(new Document()));
    when(documentMapper.entityToClass(Mockito.any())).thenReturn(documentVO);
    assertEquals("not equals", documentVO, dataflowServiceImpl.getDocumentInfoById(1L));
  }

}
