package org.eea.document.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import org.eea.document.service.DocumentService;
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

// TODO: Auto-generated Javadoc
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


}
