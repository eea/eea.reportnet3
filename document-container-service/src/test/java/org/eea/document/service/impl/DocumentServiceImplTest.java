package org.eea.document.service.impl;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.io.IOException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import org.eea.document.type.FileResponse;
import org.eea.document.utils.OakRepositoryUtils;
import org.eea.exception.EEAException;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;

/**
 * The Class DocumentServiceImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class DocumentServiceImplTest {

  /** The document service. */
  @InjectMocks
  private DocumentServiceImpl documentService;

  /** The kafka sender utils. */
  @Mock
  private KafkaSenderUtils kafkaSenderUtils;

  /** The oak repository utils. */
  @Mock
  private OakRepositoryUtils oakRepositoryUtils;

  /** The file mock. */
  private MockMultipartFile fileMock;

  /**
   * Inits the mocks.
   *
   * @throws RepositoryException the repository exception
   */
  @Before
  public void initMocks() throws RepositoryException {
    fileMock = new MockMultipartFile("file", "fileOriginal", "cvs", "content".getBytes());
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Upload document exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void uploadDocumentExceptionTest() throws EEAException {
    doNothing().when(oakRepositoryUtils).cleanUp(Mockito.any(), Mockito.any());
    documentService.uploadDocument(null, 1L, "ES", "desc");
  }

  /**
   * Upload document exception 2 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void uploadDocumentException2Test() throws EEAException {
    fileMock = new MockMultipartFile("file", "fileOriginal", null, (byte[]) null);
    doNothing().when(oakRepositoryUtils).cleanUp(Mockito.any(), Mockito.any());
    documentService.uploadDocument(fileMock, 1L, "ES", "desc");
  }

  /**
   * Upload document exception 3 test.
   *
   * @throws EEAException the EEA exception
   * @throws RepositoryException the repository exception
   */
  @Test(expected = EEAException.class)
  public void uploadDocumentException3Test() throws EEAException, RepositoryException {
    when(oakRepositoryUtils.initializeNodeStore()).thenReturn(null);
    when(oakRepositoryUtils.initializeRepository(Mockito.any())).thenReturn(null);
    when(oakRepositoryUtils.initializeSession(Mockito.any())).thenReturn(null);
    when(oakRepositoryUtils.insertStringBeforePoint(Mockito.any(), Mockito.any()))
        .thenReturn("name");
    doThrow(new EEAException()).when(oakRepositoryUtils).addFileNode(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any());
    doNothing().when(oakRepositoryUtils).cleanUp(Mockito.any(), Mockito.any());
    documentService.uploadDocument(fileMock, 1L, "ES", "desc");
  }

  /**
   * Upload document exception 4 test.
   *
   * @throws EEAException the EEA exception
   * @throws RepositoryException the repository exception
   */
  @Test(expected = EEAException.class)
  public void uploadDocumentException4Test() throws EEAException, RepositoryException {
    when(oakRepositoryUtils.initializeNodeStore()).thenReturn(null);
    when(oakRepositoryUtils.initializeRepository(Mockito.any())).thenReturn(null);
    when(oakRepositoryUtils.initializeSession(Mockito.any())).thenReturn(null);
    when(oakRepositoryUtils.insertStringBeforePoint(Mockito.any(), Mockito.any()))
        .thenReturn("name");
    when(oakRepositoryUtils.addFileNode(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any())).thenReturn("");
    doNothing().when(oakRepositoryUtils).cleanUp(Mockito.any(), Mockito.any());
    documentService.uploadDocument(fileMock, 1L, "ES", "desc");
  }

  /**
   * Upload document success test.
   *
   * @throws EEAException the EEA exception
   * @throws RepositoryException the repository exception
   */
  @Test
  public void uploadDocumentSuccessTest() throws EEAException, RepositoryException {
    when(oakRepositoryUtils.initializeNodeStore()).thenReturn(null);
    when(oakRepositoryUtils.initializeRepository(Mockito.any())).thenReturn(null);
    when(oakRepositoryUtils.initializeSession(Mockito.any())).thenReturn(null);
    when(oakRepositoryUtils.insertStringBeforePoint(Mockito.any(), Mockito.any()))
        .thenReturn("name");
    when(oakRepositoryUtils.addFileNode(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any())).thenReturn("name-ES");
    doNothing().when(kafkaSenderUtils).releaseKafkaEvent(Mockito.any(), Mockito.any());
    doNothing().when(oakRepositoryUtils).cleanUp(Mockito.any(), Mockito.any());
    documentService.uploadDocument(fileMock, 1L, "ES", "desc");
    Mockito.verify(kafkaSenderUtils, times(1)).releaseKafkaEvent(Mockito.any(), Mockito.any());
  }

  /**
   * Gets the document exception test.
   *
   * @return the document exception test
   * @throws EEAException the EEA exception
   * @throws RepositoryException the repository exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test(expected = EEAException.class)
  public void getDocumentExceptionTest() throws EEAException, RepositoryException, IOException {
    when(oakRepositoryUtils.initializeNodeStore()).thenReturn(null);
    when(oakRepositoryUtils.initializeRepository(Mockito.any())).thenReturn(null);
    when(oakRepositoryUtils.initializeSession(Mockito.any())).thenReturn(null);
    when(oakRepositoryUtils.insertStringBeforePoint(Mockito.any(), Mockito.any()))
        .thenReturn("name");
    doThrow(new RepositoryException()).when(oakRepositoryUtils).getFileContents(Mockito.any(),
        Mockito.any(), Mockito.any());
    doNothing().when(oakRepositoryUtils).cleanUp(Mockito.any(), Mockito.any());
    documentService.getDocument("filename", 1L, "ES");
  }

  /**
   * Gets the document exception 2 test.
   *
   * @return the document exception 2 test
   * @throws EEAException the EEA exception
   * @throws RepositoryException the repository exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test(expected = EEAException.class)
  public void getDocumentException2Test() throws EEAException, RepositoryException, IOException {
    when(oakRepositoryUtils.initializeNodeStore()).thenReturn(null);
    when(oakRepositoryUtils.initializeRepository(Mockito.any())).thenReturn(null);
    when(oakRepositoryUtils.initializeSession(Mockito.any())).thenReturn(null);
    when(oakRepositoryUtils.insertStringBeforePoint(Mockito.any(), Mockito.any()))
        .thenReturn("name");
    doThrow(new PathNotFoundException()).when(oakRepositoryUtils).getFileContents(Mockito.any(),
        Mockito.any(), Mockito.any());
    doNothing().when(oakRepositoryUtils).cleanUp(Mockito.any(), Mockito.any());
    documentService.getDocument("filename", 1L, "ES");
  }

  /**
   * Gets the document success test.
   *
   * @return the document success test
   * @throws EEAException the EEA exception
   * @throws RepositoryException the repository exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void getDocumentSuccessTest() throws EEAException, RepositoryException, IOException {
    when(oakRepositoryUtils.initializeNodeStore()).thenReturn(null);
    when(oakRepositoryUtils.initializeRepository(Mockito.any())).thenReturn(null);
    when(oakRepositoryUtils.initializeSession(Mockito.any())).thenReturn(null);
    when(oakRepositoryUtils.insertStringBeforePoint(Mockito.any(), Mockito.any()))
        .thenReturn("name");
    when(oakRepositoryUtils.getFileContents(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(new FileResponse());
    doNothing().when(oakRepositoryUtils).cleanUp(Mockito.any(), Mockito.any());
    assertNotNull("null result", documentService.getDocument("filename", 1L, "ES"));
  }

  /**
   * Delete document exception test.
   *
   * @throws EEAException the EEA exception
   * @throws RepositoryException the repository exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test(expected = EEAException.class)
  public void deleteDocumentExceptionTest() throws EEAException, RepositoryException, IOException {
    when(oakRepositoryUtils.initializeNodeStore()).thenReturn(null);
    when(oakRepositoryUtils.initializeRepository(Mockito.any())).thenReturn(null);
    when(oakRepositoryUtils.initializeSession(Mockito.any())).thenReturn(null);
    when(oakRepositoryUtils.insertStringBeforePoint(Mockito.any(), Mockito.any()))
        .thenReturn("name");
    doThrow(new RepositoryException()).when(oakRepositoryUtils).deleteFileNode(Mockito.any(),
        Mockito.any(), Mockito.any());
    doNothing().when(oakRepositoryUtils).cleanUp(Mockito.any(), Mockito.any());
    documentService.deleteDocument("filename", 1L, "ES");
  }

  /**
   * Delete document exception 2 test.
   *
   * @throws EEAException the EEA exception
   * @throws RepositoryException the repository exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test(expected = EEAException.class)
  public void deleteDocumentException2Test() throws EEAException, RepositoryException, IOException {
    when(oakRepositoryUtils.initializeNodeStore()).thenReturn(null);
    when(oakRepositoryUtils.initializeRepository(Mockito.any())).thenReturn(null);
    when(oakRepositoryUtils.initializeSession(Mockito.any())).thenReturn(null);
    when(oakRepositoryUtils.insertStringBeforePoint(Mockito.any(), Mockito.any()))
        .thenReturn("name");
    doThrow(new PathNotFoundException()).when(oakRepositoryUtils).deleteFileNode(Mockito.any(),
        Mockito.any(), Mockito.any());
    doNothing().when(oakRepositoryUtils).cleanUp(Mockito.any(), Mockito.any());
    documentService.deleteDocument("filename", 1L, "ES");
  }

  /**
   * Delete document success test.
   *
   * @throws Exception the exception
   */
  @Test
  public void deleteDocumentSuccessTest() throws Exception {
    when(oakRepositoryUtils.initializeNodeStore()).thenReturn(null);
    when(oakRepositoryUtils.initializeRepository(Mockito.any())).thenReturn(null);
    when(oakRepositoryUtils.initializeSession(Mockito.any())).thenReturn(null);
    when(oakRepositoryUtils.insertStringBeforePoint(Mockito.any(), Mockito.any()))
        .thenReturn("name");
    doNothing().when(oakRepositoryUtils).deleteFileNode(Mockito.any(), Mockito.any(),
        Mockito.any());
    doNothing().when(oakRepositoryUtils).runGC(Mockito.any());
    doNothing().when(oakRepositoryUtils).cleanUp(Mockito.any(), Mockito.any());
    documentService.deleteDocument("filename", 1L, "ES");
    Mockito.verify(kafkaSenderUtils, times(1)).releaseKafkaEvent(Mockito.any(), Mockito.any());
  }

}
