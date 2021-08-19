package org.eea.document.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.IOException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import org.eea.document.type.FileResponse;
import org.eea.document.utils.OakRepositoryUtils;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowDocumentController.DataFlowDocumentControllerZuul;
import org.eea.interfaces.vo.document.DocumentVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.thread.ThreadPropertiesManager;
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

  /** The dataflow controller. */
  @Mock
  private DataFlowDocumentControllerZuul dataflowController;

  /** The file mock. */
  private MockMultipartFile fileMock;

  /** The document VO. */
  private DocumentVO documentVO;


  /**
   * Inits the mocks.
   *
   * @throws RepositoryException the repository exception
   */
  @Before
  public void initMocks() throws RepositoryException {
    ThreadPropertiesManager.setVariable("user", "user");
    fileMock = new MockMultipartFile("file", "fileOriginal.cvs", "cvs", "content".getBytes());
    documentVO = new DocumentVO();
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Upload document exception test.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void uploadDocumentExceptionTest() throws EEAException, IOException {
    doNothing().when(oakRepositoryUtils).cleanUp(Mockito.any(), Mockito.any());
    try {
      documentService.uploadDocument(fileMock.getInputStream(), null, null, null, 1L);
    } catch (EEAException e) {
      assertEquals(EEAErrorMessage.DOCUMENT_UPLOAD_ERROR, e.getMessage());
    }
  }

  /**
   * Upload document exception 2 test.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void uploadDocumentException2Test() throws EEAException, IOException {
    fileMock = new MockMultipartFile("file", "fileOriginal", null, (byte[]) null);
    doNothing().when(oakRepositoryUtils).cleanUp(Mockito.any(), Mockito.any());
    try {
      documentService.uploadDocument(fileMock.getInputStream(), fileMock.getContentType(),
          fileMock.getOriginalFilename(), null, 1L);
    } catch (EEAException e) {
      assertEquals(EEAErrorMessage.DOCUMENT_UPLOAD_ERROR, e.getMessage());
    }
  }

  /**
   * Upload document exception 3 test.
   *
   * @throws EEAException the EEA exception
   * @throws RepositoryException the repository exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void uploadDocumentException3Test() throws EEAException, RepositoryException, IOException {
    when(dataflowController.insertDocument(Mockito.any())).thenReturn(null);
    doNothing().when(oakRepositoryUtils).cleanUp(Mockito.any(), Mockito.any());
    try {
      documentService.uploadDocument(fileMock.getInputStream(), fileMock.getContentType(),
          fileMock.getOriginalFilename(), documentVO, 1L);
    } catch (EEAException e) {
      assertEquals(EEAErrorMessage.DOCUMENT_UPLOAD_ERROR, e.getMessage());
    }
  }

  /**
   * Upload document success test.
   *
   * @throws EEAException the EEA exception
   * @throws RepositoryException the repository exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void uploadDocumentSuccessTest() throws EEAException, RepositoryException, IOException {
    when(dataflowController.insertDocument(Mockito.any())).thenReturn(1L);
    when(oakRepositoryUtils.initializeNodeStore()).thenReturn(null);
    when(oakRepositoryUtils.initializeRepository(Mockito.any())).thenReturn(null);
    when(oakRepositoryUtils.initializeSession(Mockito.any())).thenReturn(null);
    when(oakRepositoryUtils.addFileNode(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any())).thenReturn("name-ES");
    doNothing().when(oakRepositoryUtils).cleanUp(Mockito.any(), Mockito.any());
    documentService.uploadDocument(fileMock.getInputStream(), fileMock.getContentType(),
        fileMock.getOriginalFilename(), documentVO, 10000L);
    Mockito.verify(oakRepositoryUtils, times(1)).addFileNode(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any());
  }

  /**
   * Update document exception 1 test.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void updateDocumentException1Test() throws EEAException, IOException {
    try {
      documentService.updateDocument(null);
    } catch (EEAException e) {
      assertEquals(EEAErrorMessage.DOCUMENT_UPLOAD_ERROR, e.getMessage());
    }
  }

  /**
   * Update document success test.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void updateDocumentSuccessTest() throws EEAException, IOException {
    doNothing().when(dataflowController).updateDocument(Mockito.any());
    documentService.updateDocument(documentVO);
    verify(dataflowController, times(1)).updateDocument(documentVO);
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
    doThrow(new RepositoryException()).when(oakRepositoryUtils).getFileContents(Mockito.any(),
        Mockito.any(), Mockito.any());
    doNothing().when(oakRepositoryUtils).cleanUp(Mockito.any(), Mockito.any());
    documentService.getDocument(1L, 1L);
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
    doThrow(new PathNotFoundException()).when(oakRepositoryUtils).getFileContents(Mockito.any(),
        Mockito.any(), Mockito.any());
    doNothing().when(oakRepositoryUtils).cleanUp(Mockito.any(), Mockito.any());
    documentService.getDocument(1L, 1L);
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
    when(oakRepositoryUtils.getFileContents(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(new FileResponse());
    doNothing().when(oakRepositoryUtils).cleanUp(Mockito.any(), Mockito.any());
    assertNotNull("null result", documentService.getDocument(1L, 1L));
  }

  /**
   * Delete document exception test.
   *
   * @throws EEAException the EEA exception
   * @throws RepositoryException the repository exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void deleteDocumentExceptionTest() throws RepositoryException, EEAException {
    when(oakRepositoryUtils.initializeNodeStore()).thenReturn(null);
    when(oakRepositoryUtils.initializeRepository(Mockito.any())).thenReturn(null);
    when(oakRepositoryUtils.initializeSession(Mockito.any())).thenReturn(null);
    doThrow(new RepositoryException()).when(oakRepositoryUtils).deleteFileNode(Mockito.any(),
        Mockito.any(), Mockito.any());
    doNothing().when(oakRepositoryUtils).cleanUp(Mockito.any(), Mockito.any());
    documentService.deleteDocument(1L, 1L, Boolean.FALSE);
    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
  }

  /**
   * Delete document success test.
   *
   * @throws Exception the exception
   */
  @Test
  public void deleteDocumentSuccessTest() throws Exception {
    doNothing().when(dataflowController).deleteDocument(Mockito.any());
    when(oakRepositoryUtils.initializeNodeStore()).thenReturn(null);
    when(oakRepositoryUtils.initializeRepository(Mockito.any())).thenReturn(null);
    when(oakRepositoryUtils.initializeSession(Mockito.any())).thenReturn(null);
    doNothing().when(oakRepositoryUtils).deleteFileNode(Mockito.any(), Mockito.any(),
        Mockito.any());
    doNothing().when(oakRepositoryUtils).deleteBlobsFromRepository(Mockito.any());
    doNothing().when(oakRepositoryUtils).cleanUp(Mockito.any(), Mockito.any());
    documentService.deleteDocument(1L, 1L, Boolean.TRUE);
    Mockito.verify(oakRepositoryUtils, times(1)).cleanUp(Mockito.any(), Mockito.any());
  }


  /**
   * Test upload snapshot success.
   *
   * @throws EEAException the EEA exception
   * @throws RepositoryException the repository exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void testUploadSnapshotSuccess() throws EEAException, RepositoryException, IOException {
    when(oakRepositoryUtils.initializeNodeStore()).thenReturn(null);
    when(oakRepositoryUtils.initializeRepository(Mockito.any())).thenReturn(null);
    when(oakRepositoryUtils.initializeSession(Mockito.any())).thenReturn(null);
    when(oakRepositoryUtils.addFileNode(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any())).thenReturn("name-ES");
    doNothing().when(oakRepositoryUtils).cleanUp(Mockito.any(), Mockito.any());
    documentService.uploadSchemaSnapshot(fileMock.getInputStream(), fileMock.getContentType(),
        fileMock.getOriginalFilename(), 1L);
    Mockito.verify(oakRepositoryUtils, times(1)).cleanUp(Mockito.any(), Mockito.any());
  }


  /**
   * Test upload snapshot exception.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test(expected = EEAException.class)
  public void testUploadSnapshotException() throws EEAException, IOException {
    doNothing().when(oakRepositoryUtils).cleanUp(Mockito.any(), Mockito.any());
    documentService.uploadSchemaSnapshot(fileMock.getInputStream(), null, null, 1L);
  }


  /**
   * Test upload snapshot exception 2 test.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test(expected = EEAException.class)
  public void testUploadSnapshotException2Test() throws EEAException, IOException {
    fileMock = new MockMultipartFile("file", "fileOriginal", null, (byte[]) null);
    doNothing().when(oakRepositoryUtils).cleanUp(Mockito.any(), Mockito.any());
    documentService.uploadSchemaSnapshot(fileMock.getInputStream(), fileMock.getContentType(),
        "test", 1L);
  }


  /**
   * Test upload snapshot exception 3.
   *
   * @throws EEAException the EEA exception
   * @throws RepositoryException the repository exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test(expected = EEAException.class)
  public void testUploadSnapshotException3() throws EEAException, RepositoryException, IOException {

    doNothing().when(oakRepositoryUtils).cleanUp(Mockito.any(), Mockito.any());
    documentService.uploadSchemaSnapshot(fileMock.getInputStream(), fileMock.getContentType(),
        "test", 1L);

  }

  /**
   * Test upload snapshot exception 4 test.
   *
   * @throws EEAException the EEA exception
   * @throws RepositoryException the repository exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test(expected = EEAException.class)
  public void testUploadSnapshotException4Test()
      throws EEAException, RepositoryException, IOException {

    doNothing().when(oakRepositoryUtils).cleanUp(Mockito.any(), Mockito.any());
    documentService.uploadSchemaSnapshot(fileMock.getInputStream(), fileMock.getContentType(),
        "test", 1L);
  }

  /**
   * Test upload snapshot exception 5 test.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws RepositoryException the repository exception
   */
  @Test(expected = EEAException.class)
  public void testUploadSnapshotException5Test()
      throws EEAException, IOException, RepositoryException {
    when(oakRepositoryUtils.initializeNodeStore()).thenReturn(null);
    when(oakRepositoryUtils.initializeRepository(Mockito.any())).thenReturn(null);
    when(oakRepositoryUtils.initializeSession(Mockito.any())).thenReturn(null);
    when(oakRepositoryUtils.addFileNode(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any())).thenReturn("");
    doNothing().when(oakRepositoryUtils).cleanUp(Mockito.any(), Mockito.any());
    documentService.uploadSchemaSnapshot(fileMock.getInputStream(), fileMock.getContentType(),
        fileMock.getOriginalFilename(), 1L);
  }


  /**
   * Test get snapshot success.
   *
   * @throws EEAException the EEA exception
   * @throws RepositoryException the repository exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void testGetSnapshotSuccess() throws EEAException, RepositoryException, IOException {
    when(oakRepositoryUtils.initializeNodeStore()).thenReturn(null);
    when(oakRepositoryUtils.initializeRepository(Mockito.any())).thenReturn(null);
    when(oakRepositoryUtils.initializeSession(Mockito.any())).thenReturn(null);
    when(oakRepositoryUtils.getFileContents(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(new FileResponse());
    doNothing().when(oakRepositoryUtils).cleanUp(Mockito.any(), Mockito.any());
    assertNotNull("null result", documentService.getSnapshotDocument("test", 1L));
  }


  /**
   * Test get snapshot exception.
   *
   * @throws EEAException the EEA exception
   * @throws RepositoryException the repository exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test(expected = EEAException.class)
  public void testGetSnapshotException() throws EEAException, RepositoryException, IOException {
    when(oakRepositoryUtils.initializeNodeStore()).thenReturn(null);
    when(oakRepositoryUtils.initializeRepository(Mockito.any())).thenReturn(null);
    when(oakRepositoryUtils.initializeSession(Mockito.any())).thenReturn(null);
    doThrow(new RepositoryException()).when(oakRepositoryUtils).getFileContents(Mockito.any(),
        Mockito.any(), Mockito.any());
    doNothing().when(oakRepositoryUtils).cleanUp(Mockito.any(), Mockito.any());
    documentService.getSnapshotDocument("test", 1L);
  }

  /**
   * Test get snapshot exception 2 test.
   *
   * @throws EEAException the EEA exception
   * @throws RepositoryException the repository exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test(expected = EEAException.class)
  public void testGetSnapshotException2Test()
      throws EEAException, RepositoryException, IOException {
    when(oakRepositoryUtils.initializeNodeStore()).thenReturn(null);
    when(oakRepositoryUtils.initializeRepository(Mockito.any())).thenReturn(null);
    when(oakRepositoryUtils.initializeSession(Mockito.any())).thenReturn(null);
    doThrow(new PathNotFoundException()).when(oakRepositoryUtils).getFileContents(Mockito.any(),
        Mockito.any(), Mockito.any());
    doNothing().when(oakRepositoryUtils).cleanUp(Mockito.any(), Mockito.any());
    documentService.getSnapshotDocument("test", 1L);
  }


  /**
   * Test delete snapshot success test.
   *
   * @throws Exception the exception
   */
  @Test
  public void testDeleteSnapshotSuccessTest() throws Exception {
    when(oakRepositoryUtils.initializeNodeStore()).thenReturn(null);
    when(oakRepositoryUtils.initializeRepository(Mockito.any())).thenReturn(null);
    when(oakRepositoryUtils.initializeSession(Mockito.any())).thenReturn(null);

    doNothing().when(oakRepositoryUtils).deleteFileNode(Mockito.any(), Mockito.any(),
        Mockito.any());
    doNothing().when(oakRepositoryUtils).deleteBlobsFromRepository(Mockito.any());
    doNothing().when(oakRepositoryUtils).cleanUp(Mockito.any(), Mockito.any());
    documentService.deleteSnapshotDocument("filename", 1L);
    Mockito.verify(oakRepositoryUtils, times(1)).cleanUp(Mockito.any(), Mockito.any());

  }

  /**
   * Test delete snapshot exception test.
   *
   * @throws EEAException the EEA exception
   * @throws RepositoryException the repository exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test(expected = EEAException.class)
  public void testDeleteSnapshotExceptionTest()
      throws EEAException, RepositoryException, IOException {
    when(oakRepositoryUtils.initializeNodeStore()).thenReturn(null);
    when(oakRepositoryUtils.initializeRepository(Mockito.any())).thenReturn(null);
    when(oakRepositoryUtils.initializeSession(Mockito.any())).thenReturn(null);
    doThrow(new RepositoryException()).when(oakRepositoryUtils).deleteFileNode(Mockito.any(),
        Mockito.any(), Mockito.any());
    doNothing().when(oakRepositoryUtils).cleanUp(Mockito.any(), Mockito.any());
    documentService.deleteSnapshotDocument("filename", 1L);
  }


  /**
   * Test delete snapshot exception 2.
   *
   * @throws EEAException the EEA exception
   * @throws RepositoryException the repository exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test(expected = EEAException.class)
  public void testDeleteSnapshotException2() throws EEAException, RepositoryException, IOException {
    when(oakRepositoryUtils.initializeNodeStore()).thenReturn(null);
    when(oakRepositoryUtils.initializeRepository(Mockito.any())).thenReturn(null);
    when(oakRepositoryUtils.initializeSession(Mockito.any())).thenReturn(null);
    doThrow(new PathNotFoundException()).when(oakRepositoryUtils).deleteFileNode(Mockito.any(),
        Mockito.any(), Mockito.any());
    doNothing().when(oakRepositoryUtils).cleanUp(Mockito.any(), Mockito.any());
    documentService.deleteSnapshotDocument("filename", 1L);
  }

}
