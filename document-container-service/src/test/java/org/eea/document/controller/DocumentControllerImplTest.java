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
   */
  @Test(expected = ResponseStatusException.class)
  public void uploadDocumentTestException3() throws EEAException {
    doThrow(new EEAException()).when(documentService).uploadDocument(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any());
    documentController.uploadDocument(fileMock, 1L, "ES", "desc");
  }

  /**
   * Upload document test exception 4.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void uploadDocumentTestException4() throws EEAException {
    doThrow(new EEAException(EEAErrorMessage.DOCUMENT_NOT_FOUND)).when(documentService)
        .uploadDocument(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    documentController.uploadDocument(fileMock, 1L, "ES", "desc");
  }

  /**
   * Upload document success test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void uploadDocumentSuccessTest() throws EEAException {
    doNothing().when(documentService).uploadDocument(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any());
    documentController.uploadDocument(fileMock, 1L, "ES", "desc");
    Mockito.verify(documentService, times(1)).uploadDocument(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any());
  }

  /**
   * Gets the document exception test.
   *
   * @return the document exception test
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void getDocumentExceptionTest() throws EEAException {
    doThrow(new EEAException(EEAErrorMessage.DOCUMENT_NOT_FOUND)).when(documentService)
        .getDocument(Mockito.any(), Mockito.any(), Mockito.any());
    documentController.getDocument("name", 1L, "ES");
  }

  /**
   * Gets the document exception 2 test.
   *
   * @return the document exception 2 test
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void getDocumentException2Test() throws EEAException {
    doThrow(new EEAException(EEAErrorMessage.DOCUMENT_UPLOAD_ERROR)).when(documentService)
        .getDocument(Mockito.any(), Mockito.any(), Mockito.any());
    documentController.getDocument("name", 1L, "ES");
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
    when(documentService.getDocument(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(content);
    documentController.getDocument("name", 1L, "ES");
    Mockito.verify(documentService, times(1)).getDocument(Mockito.any(), Mockito.any(),
        Mockito.any());
  }

  /**
   * Delete document exception test.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void deleteDocumentExceptionTest() throws Exception {
    doThrow(new EEAException(EEAErrorMessage.DOCUMENT_NOT_FOUND)).when(documentService)
        .deleteDocument(Mockito.any(), Mockito.any(), Mockito.any());
    documentController.deleteDocument("name", 1L, "ES");
  }

  /**
   * Delete document exception 2 test.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void deleteDocumentException2Test() throws Exception {
    doThrow(new EEAException(EEAErrorMessage.DOCUMENT_UPLOAD_ERROR)).when(documentService)
        .deleteDocument(Mockito.any(), Mockito.any(), Mockito.any());
    documentController.deleteDocument("name", 1L, "ES");
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
    doNothing().when(documentService).deleteDocument(Mockito.any(), Mockito.any(), Mockito.any());
    documentController.deleteDocument("name", 1L, "ES");
    Mockito.verify(documentService, times(1)).deleteDocument(Mockito.any(), Mockito.any(),
        Mockito.any());
  }
}
