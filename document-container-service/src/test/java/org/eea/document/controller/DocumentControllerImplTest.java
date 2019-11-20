package org.eea.document.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.io.IOException;
import org.eea.document.service.DocumentService;
import org.eea.document.type.FileResponse;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowDocumentController.DataFlowDocumentControllerZuul;
import org.eea.interfaces.vo.document.DocumentVO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;
import feign.FeignException;

/**
 * The Class DocumentControllerImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class DocumentControllerImplTest {

  /** The document controller. */
  @InjectMocks
  private DocumentControllerImpl documentController;

  /** The document service. */
  @Mock
  private DocumentService documentService;

  /** The file mock. */
  private MockMultipartFile fileMock;

  /** The file mock. */
  private MockMultipartFile emptyFileMock;

  /** The dataflow controller. */
  @Mock
  private DataFlowDocumentControllerZuul dataflowController;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    fileMock = new MockMultipartFile("file", "fileOriginal", "cvs", "content".getBytes());
    emptyFileMock = new MockMultipartFile("file", "fileOriginal", "cvs", (byte[]) null);
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Upload document test exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void uploadDocumentTestException() throws EEAException {

    documentController.uploadDocument(null, 1L, "ES", "desc");
  }

  /**
   * Upload document test exception 1.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void uploadDocumentTestException1() throws EEAException {

    documentController.uploadDocument(emptyFileMock, 1L, "ES", "desc");
  }

  /**
   * Upload document test exception 2.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void uploadDocumentTestException2() throws EEAException {

    documentController.uploadDocument(fileMock, null, "ES", "desc");
  }

  /**
   * Upload document test exception 3.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test(expected = ResponseStatusException.class)
  public void uploadDocumentTestException3() throws EEAException, IOException {
    doThrow(new EEAException()).when(documentService).uploadDocument(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any());
    documentController.uploadDocument(fileMock, 1L, "ES", "desc");
  }

  /**
   * Upload document test exception 4.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test(expected = ResponseStatusException.class)
  public void uploadDocumentTestException4() throws EEAException, IOException {
    doThrow(new EEAException(EEAErrorMessage.DOCUMENT_NOT_FOUND)).when(documentService)
        .uploadDocument(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    documentController.uploadDocument(fileMock, 1L, "ES", "desc");
  }

  /**
   * Upload document success test.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void uploadDocumentSuccessTest() throws EEAException, IOException {
    doNothing().when(documentService).uploadDocument(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any());
    documentController.uploadDocument(fileMock, 1L, "ES", "desc");
    Mockito.verify(documentService, times(1)).uploadDocument(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any());
  }

  /**
   * Gets the document exception null test.
   *
   * @return the document exception null test
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void getDocumentExceptionNullTest() throws EEAException {
    when(dataflowController.getDocumentInfoById(Mockito.any())).thenReturn(null);
    documentController.getDocument(1L);
  }

  /**
   * Gets the document exception test.
   *
   * @return the document exception test
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void getDocumentExceptionTest() throws EEAException {
    when(dataflowController.getDocumentInfoById(Mockito.any())).thenReturn(new DocumentVO());
    doThrow(new EEAException(EEAErrorMessage.DOCUMENT_NOT_FOUND)).when(documentService)
        .getDocument(Mockito.any(), Mockito.any(), Mockito.any());
    documentController.getDocument(1L);
  }

  /**
   * Gets the document exception 2 test.
   *
   * @return the document exception 2 test
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void getDocumentException2Test() throws EEAException {
    when(dataflowController.getDocumentInfoById(Mockito.any())).thenReturn(new DocumentVO());
    doThrow(new EEAException(EEAErrorMessage.DOCUMENT_UPLOAD_ERROR)).when(documentService)
        .getDocument(Mockito.any(), Mockito.any(), Mockito.any());
    documentController.getDocument(1L);
  }

  /**
   * Gets the document success test.
   *
   * @return the document success test
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void getDocumentSuccessTest() throws EEAException, IOException {
    FileResponse content = new FileResponse();
    content.setBytes(fileMock.getBytes());
    when(dataflowController.getDocumentInfoById(Mockito.any())).thenReturn(new DocumentVO());
    when(documentService.getDocument(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(content);
    documentController.getDocument(1L);
    Mockito.verify(documentService, times(1)).getDocument(Mockito.any(), Mockito.any(),
        Mockito.any());
  }

  /**
   * Delete document exception null test.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void deleteDocumentExceptionNullTest() throws Exception {
    when(dataflowController.getDocumentInfoById(Mockito.any())).thenReturn(null);
    documentController.deleteDocument(1L);
  }

  /**
   * Delete document exception test.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void deleteDocumentExceptionTest() throws Exception {
    when(dataflowController.getDocumentInfoById(Mockito.any())).thenReturn(new DocumentVO());
    doThrow(new EEAException(EEAErrorMessage.DOCUMENT_NOT_FOUND)).when(documentService)
        .deleteDocument(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    documentController.deleteDocument(1L);
  }

  /**
   * Delete document exception 2 test.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void deleteDocumentException2Test() throws Exception {
    when(dataflowController.getDocumentInfoById(Mockito.any())).thenReturn(new DocumentVO());
    doThrow(new EEAException(EEAErrorMessage.DOCUMENT_UPLOAD_ERROR)).when(documentService)
        .deleteDocument(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    documentController.deleteDocument(1L);
  }

  /**
   * Delete document exception 3 test.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void deleteDocumentException3Test() throws Exception {
    doThrow(FeignException.class).when(dataflowController).getDocumentInfoById(Mockito.any());
    documentController.deleteDocument(1L);
  }

  /**
   * Delete document success test.
   *
   * @throws Exception the exception
   */
  @Test
  public void deleteDocumentSuccessTest() throws Exception {
    FileResponse content = new FileResponse();
    content.setBytes(fileMock.getBytes());
    when(dataflowController.getDocumentInfoById(Mockito.any())).thenReturn(new DocumentVO());
    doNothing().when(documentService).deleteDocument(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any());
    documentController.deleteDocument(1L);
    Mockito.verify(documentService, times(1)).deleteDocument(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any());
  }


  @Test
  public void testUploadSnapshotSuccess() throws EEAException, IOException {
    doNothing().when(documentService).uploadSchemaSnapshot(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any());
    documentController.uploadSchemaSnapshotDocument(fileMock.getBytes(), 1L, "desc.json");
    Mockito.verify(documentService, times(1)).uploadSchemaSnapshot(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any());
  }


  @Test(expected = ResponseStatusException.class)
  public void testUploadSnapshotException() throws EEAException {

    documentController.uploadSchemaSnapshotDocument(null, 1L, "desc");

  }


  @Test(expected = ResponseStatusException.class)
  public void testUploadSnapshotException2() throws EEAException, IOException {

    documentController.uploadSchemaSnapshotDocument(fileMock.getBytes(), null, "desc");
  }



  @Test(expected = ResponseStatusException.class)
  public void testUploadSnapshotException3() throws EEAException, IOException {
    doThrow(new EEAException()).when(documentService).uploadSchemaSnapshot(Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any());
    documentController.uploadSchemaSnapshotDocument(fileMock.getBytes(), 1L, "desc");
  }


  @Test(expected = ResponseStatusException.class)
  public void testUploadSnapshotException4() throws EEAException, IOException {
    doThrow(new EEAException(EEAErrorMessage.DOCUMENT_NOT_FOUND)).when(documentService)
        .uploadSchemaSnapshot(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    documentController.uploadSchemaSnapshotDocument(fileMock.getBytes(), 1L, "desc");
  }


  @Test
  public void testGetSnapshotSuccess() throws EEAException, IOException {
    FileResponse content = new FileResponse();
    content.setBytes(fileMock.getBytes());

    when(documentService.getSnapshotDocument(Mockito.any(), Mockito.any())).thenReturn(content);
    documentController.getSnapshotDocument(1L, "test");
    Mockito.verify(documentService, times(1)).getSnapshotDocument(Mockito.any(), Mockito.any());
  }



  @Test(expected = ResponseStatusException.class)
  public void testGetSnapshotExceptionNull() throws EEAException {

    documentController.getSnapshotDocument(null, "test");
  }


  @Test(expected = ResponseStatusException.class)
  public void testGetSnapshotException() throws EEAException {

    doThrow(new EEAException(EEAErrorMessage.DOCUMENT_NOT_FOUND)).when(documentService)
        .getSnapshotDocument(Mockito.any(), Mockito.any());
    documentController.getSnapshotDocument(1L, "test");
  }

  @Test(expected = ResponseStatusException.class)
  public void testGetSnapshotException2() throws EEAException {

    doThrow(new EEAException(EEAErrorMessage.DOCUMENT_UPLOAD_ERROR)).when(documentService)
        .getSnapshotDocument(Mockito.any(), Mockito.any());
    documentController.getSnapshotDocument(1L, "test");
  }


  @Test
  public void testDeleteSnapshotSuccess() throws Exception {
    FileResponse content = new FileResponse();
    content.setBytes(fileMock.getBytes());
    doNothing().when(documentService).deleteSnapshotDocument(Mockito.any(), Mockito.any());
    documentController.deleteSnapshotSchemaDocument(1L, "test");
    Mockito.verify(documentService, times(1)).deleteSnapshotDocument(Mockito.any(), Mockito.any());
  }

  @Test(expected = ResponseStatusException.class)
  public void testDeleteSnapshotExceptionNull() throws Exception {

    documentController.deleteSnapshotSchemaDocument(null, "test");
  }


  @Test(expected = ResponseStatusException.class)
  public void tetsDeleteSnapshotException() throws Exception {

    doThrow(new EEAException(EEAErrorMessage.DOCUMENT_NOT_FOUND)).when(documentService)
        .deleteSnapshotDocument(Mockito.any(), Mockito.any());
    documentController.deleteSnapshotSchemaDocument(1L, "test");
  }


  @Test(expected = ResponseStatusException.class)
  public void testDeleteSnapshotException2() throws Exception {

    doThrow(new EEAException(EEAErrorMessage.DOCUMENT_UPLOAD_ERROR)).when(documentService)
        .deleteSnapshotDocument(Mockito.any(), Mockito.any());
    documentController.deleteSnapshotSchemaDocument(1L, "test");
  }

}
