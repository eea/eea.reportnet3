package org.eea.dataflow.service.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Optional;
import org.eea.dataflow.mapper.DocumentMapper;
import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.dataflow.persistence.domain.Document;
import org.eea.dataflow.persistence.repository.DataflowRepository;
import org.eea.dataflow.persistence.repository.DocumentRepository;
import org.eea.dataflow.service.impl.DataflowDocumentServiceImpl;
import org.eea.exception.EEAErrorMessage;
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

  DocumentVO documentVO;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    documentVO = new DocumentVO();
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Insert document exception 1 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void insertDocumentException1Test() throws EEAException {
    try {
      dataflowServiceImpl.insertDocument(null);
    } catch (EEAException e) {
      assertEquals("error in the message", EEAErrorMessage.DATAFLOW_NOTFOUND, e.getMessage());
    }
  }


  /**
   * Insert document exception 5 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void insertDocumentException3Test() throws EEAException {
    when(dataflowRepository.findById(Mockito.any())).thenReturn(Optional.empty());
    try {
      dataflowServiceImpl.insertDocument(documentVO);
    } catch (EEAException e) {
      assertEquals("error in the message", EEAErrorMessage.DATAFLOW_NOTFOUND, e.getMessage());
    }
  }

  /**
   * Insert document success test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void insertDocumentSuccessTest() throws EEAException {
    Document document = new Document();
    document.setId(1L);
    when(dataflowRepository.findById(Mockito.any())).thenReturn(Optional.of(new Dataflow()));
    when(documentMapper.classToEntity(Mockito.any())).thenReturn(document);
    when(documentRepository.save(Mockito.any())).thenReturn(document);
    assertEquals((Long) 1L, dataflowServiceImpl.insertDocument(documentVO));
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

  @Test
  public void updateDocumentExceptionTest() throws EEAException {
    when(documentRepository.findById(Mockito.any())).thenReturn(Optional.empty());
    try {
      dataflowServiceImpl.updateDocument(documentVO);
    } catch (EEAException e) {
      assertEquals("bad message", EEAErrorMessage.DOCUMENT_NOT_FOUND, e.getMessage());
    }
  }

  @Test
  public void updateDocumentSuccessTest() throws EEAException {
    when(documentRepository.findById(Mockito.any())).thenReturn(Optional.of(new Document()));
    when(documentMapper.classToEntity(Mockito.any())).thenReturn(new Document());
    when(documentRepository.save(Mockito.any())).thenReturn(null);
    dataflowServiceImpl.updateDocument(documentVO);
    verify(documentRepository, times(1)).save(Mockito.any());
  }
}
