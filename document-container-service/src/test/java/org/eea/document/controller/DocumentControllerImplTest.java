package org.eea.document.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.util.ArrayList;
import org.eea.document.service.DocumentService;
import org.eea.document.type.FileResponse;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.communication.NotificationController.NotificationControllerZuul;
import org.eea.interfaces.controller.dataflow.DataFlowDocumentController.DataFlowDocumentControllerZuul;
import org.eea.interfaces.vo.document.DocumentVO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
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

  /** The notification controller zuul. */
  @Mock
  private NotificationControllerZuul notificationControllerZuul;

  /** The security context. */
  SecurityContext securityContext;

  /** The authentication. */
  Authentication authentication;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    authentication = Mockito.mock(Authentication.class);
    securityContext = Mockito.mock(SecurityContext.class);
    securityContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(securityContext);
    fileMock = new MockMultipartFile("file", "fileOriginal", "cvs", "content".getBytes());
    emptyFileMock = new MockMultipartFile("file", "fileOriginal", "cvs", (byte[]) null);
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Upload document test exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void uploadDocumentTestException() throws EEAException {
    Mockito.doNothing().when(notificationControllerZuul)
        .createUserNotificationPrivate(Mockito.anyString(), Mockito.any());

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    try {
      documentController.uploadDocument(null, 1L, "ES", "desc", true);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      assertEquals(EEAErrorMessage.FILE_FORMAT, e.getReason());
    }
  }

  /**
   * Upload document test exception 1.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void uploadDocumentTestException1() throws EEAException {
    Mockito.doNothing().when(notificationControllerZuul)
        .createUserNotificationPrivate(Mockito.anyString(), Mockito.any());

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    try {
      documentController.uploadDocument(emptyFileMock, 1L, "ES", "desc", true);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      assertEquals(EEAErrorMessage.FILE_FORMAT, e.getReason());
    }
  }

  /**
   * Upload document test exception 2.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void uploadDocumentTestException2() throws EEAException {
    Mockito.doNothing().when(notificationControllerZuul)
        .createUserNotificationPrivate(Mockito.anyString(), Mockito.any());

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    try {
      documentController.uploadDocument(fileMock, null, "ES", "desc", true);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      assertEquals(EEAErrorMessage.DATAFLOW_INCORRECT_ID, e.getReason());
    }
  }

  /**
   * Upload document test exception 3.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void uploadDocumentTestException3() throws EEAException, IOException {
    Mockito.doNothing().when(notificationControllerZuul)
        .createUserNotificationPrivate(Mockito.anyString(), Mockito.any());

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    doThrow(new EEAException()).when(documentService).uploadDocument(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any());
    try {
      documentController.uploadDocument(fileMock, 1L, "ES", "desc", true);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
    }
  }

  /**
   * Upload document test exception 4.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void uploadDocumentTestException4() throws EEAException, IOException {
    Mockito.doNothing().when(notificationControllerZuul)
        .createUserNotificationPrivate(Mockito.anyString(), Mockito.any());

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    doThrow(new EEAException(EEAErrorMessage.DOCUMENT_NOT_FOUND)).when(documentService)
        .uploadDocument(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    try {
      documentController.uploadDocument(fileMock, 1L, "ES", "desc", true);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
      assertEquals(EEAErrorMessage.DOCUMENT_NOT_FOUND, e.getReason());
    }
  }

  /**
   * Upload document success test.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void uploadDocumentSuccessTest() throws EEAException, IOException {
    Mockito.doNothing().when(notificationControllerZuul)
        .createUserNotificationPrivate(Mockito.anyString(), Mockito.any());

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    doNothing().when(documentService).uploadDocument(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any());
    documentController.uploadDocument(fileMock, 1L, "ES", "desc", true);
    Mockito.verify(documentService, times(1)).uploadDocument(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any());
  }

  /**
   * Upload document legacy success test.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void uploadDocumentLegacySuccessTest() throws EEAException, IOException {
    Mockito.doNothing().when(notificationControllerZuul)
        .createUserNotificationPrivate(Mockito.anyString(), Mockito.any());

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    doNothing().when(documentService).uploadDocument(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any());
    documentController.uploadDocumentLegacy(fileMock, 1L, "ES", "desc", true);
    Mockito.verify(documentService, times(1)).uploadDocument(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any());
  }

  /**
   * Gets the document exception null test.
   *
   * @return the document exception null test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getDocumentExceptionNullTest() throws EEAException {
    when(dataflowController.getDocumentInfoById(Mockito.any())).thenReturn(null);
    try {
      documentController.getDocument(1L, 1L);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
      assertEquals(EEAErrorMessage.DOCUMENT_NOT_FOUND, e.getReason());
    }
  }

  /**
   * Gets the document exception test.
   *
   * @return the document exception test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getDocumentExceptionTest() throws EEAException {
    when(dataflowController.getDocumentInfoById(Mockito.any())).thenReturn(new DocumentVO());
    doThrow(new EEAException(EEAErrorMessage.DOCUMENT_NOT_FOUND)).when(documentService)
        .getDocument(Mockito.any(), Mockito.any());
    try {
      documentController.getDocument(1L, 1L);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
      assertEquals(EEAErrorMessage.DOCUMENT_NOT_FOUND, e.getReason());
    }
  }

  /**
   * Gets the document exception 2 test.
   *
   * @return the document exception 2 test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getDocumentException2Test() throws EEAException {
    when(dataflowController.getDocumentInfoById(Mockito.any())).thenReturn(new DocumentVO());
    doThrow(new EEAException(EEAErrorMessage.DOCUMENT_UPLOAD_ERROR)).when(documentService)
        .getDocument(Mockito.any(), Mockito.any());
    try {
      documentController.getDocument(1L, 1L);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      assertEquals(EEAErrorMessage.RETRIEVING_DOCUMENT, e.getReason());
    }
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
    when(documentService.getDocument(Mockito.any(), Mockito.any())).thenReturn(content);
    documentController.getDocument(1L, 1L);
    Mockito.verify(documentService, times(1)).getDocument(Mockito.any(), Mockito.any());
  }


  /**
   * Gets the document legacy success test.
   *
   * @return the document legacy success test
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void getDocumentLegacySuccessTest() throws EEAException, IOException {
    FileResponse content = new FileResponse();
    content.setBytes(fileMock.getBytes());
    when(dataflowController.getDocumentInfoById(Mockito.any())).thenReturn(new DocumentVO());
    when(documentService.getDocument(Mockito.any(), Mockito.any())).thenReturn(content);
    documentController.getDocumentLegacy(1L, 1L);
    Mockito.verify(documentService, times(1)).getDocument(Mockito.any(), Mockito.any());
  }

  /**
   * Delete document exception null test.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void deleteDocumentExceptionNullTest() throws Exception {
    Mockito.doNothing().when(notificationControllerZuul)
        .createUserNotificationPrivate(Mockito.anyString(), Mockito.any());

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    when(dataflowController.getDocumentInfoById(Mockito.any())).thenReturn(null);
    documentController.deleteDocument(1L, 1L, null);
  }

  /**
   * Delete document exception test.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void deleteDocumentExceptionTest() throws Exception {
    Mockito.doNothing().when(notificationControllerZuul)
        .createUserNotificationPrivate(Mockito.anyString(), Mockito.any());

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    when(dataflowController.getDocumentInfoById(Mockito.any())).thenReturn(new DocumentVO());
    doThrow(new EEAException(EEAErrorMessage.DOCUMENT_NOT_FOUND)).when(documentService)
        .deleteDocument(Mockito.any(), Mockito.any(), Mockito.any());
    documentController.deleteDocument(1L, 1L, null);
  }

  /**
   * Delete document exception 2 test.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void deleteDocumentException2Test() throws Exception {
    Mockito.doNothing().when(notificationControllerZuul)
        .createUserNotificationPrivate(Mockito.anyString(), Mockito.any());

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    when(dataflowController.getDocumentInfoById(Mockito.any())).thenReturn(new DocumentVO());
    doThrow(new EEAException(EEAErrorMessage.DOCUMENT_UPLOAD_ERROR)).when(documentService)
        .deleteDocument(Mockito.any(), Mockito.any(), Mockito.any());
    documentController.deleteDocument(1L, 1L, null);
  }

  /**
   * Delete document exception 3 test.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void deleteDocumentException3Test() throws Exception {
    Mockito.doNothing().when(notificationControllerZuul)
        .createUserNotificationPrivate(Mockito.anyString(), Mockito.any());

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    doThrow(FeignException.class).when(dataflowController).getDocumentInfoById(Mockito.any());
    documentController.deleteDocument(1L, 1L, null);
  }

  /**
   * Delete document success test.
   *
   * @throws Exception the exception
   */
  @Test
  public void deleteDocumentSuccessTest() throws Exception {
    Mockito.doNothing().when(notificationControllerZuul)
        .createUserNotificationPrivate(Mockito.anyString(), Mockito.any());

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    FileResponse content = new FileResponse();
    content.setBytes(fileMock.getBytes());
    when(dataflowController.getDocumentInfoById(Mockito.any())).thenReturn(new DocumentVO());
    doNothing().when(documentService).deleteDocument(Mockito.any(), Mockito.any(), Mockito.any());
    documentController.deleteDocument(1L, 1L, null);
    Mockito.verify(documentService, times(1)).deleteDocument(Mockito.any(), Mockito.any(),
        Mockito.any());
  }

  /**
   * Delete document legacy success test.
   *
   * @throws Exception the exception
   */
  @Test
  public void deleteDocumentLegacySuccessTest() throws Exception {
    Mockito.doNothing().when(notificationControllerZuul)
        .createUserNotificationPrivate(Mockito.anyString(), Mockito.any());

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    FileResponse content = new FileResponse();
    content.setBytes(fileMock.getBytes());
    when(dataflowController.getDocumentInfoById(Mockito.any())).thenReturn(new DocumentVO());
    doNothing().when(documentService).deleteDocument(Mockito.any(), Mockito.any(), Mockito.any());
    documentController.deleteDocumentLegacy(1L, 1L, null);
    Mockito.verify(documentService, times(1)).deleteDocument(Mockito.any(), Mockito.any(),
        Mockito.any());
  }

  /**
   * Update document exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateDocumentExceptionTest() throws EEAException {
    Mockito.doNothing().when(notificationControllerZuul)
        .createUserNotificationPrivate(Mockito.anyString(), Mockito.any());

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    try {
      documentController.updateDocument(fileMock, null, "ES", "desc", 1L, true);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      assertEquals(EEAErrorMessage.DATAFLOW_INCORRECT_ID, e.getReason());
    }
  }

  /**
   * Update document exception 2 test.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void updateDocumentException2Test() throws EEAException, IOException {
    Mockito.doNothing().when(notificationControllerZuul)
        .createUserNotificationPrivate(Mockito.anyString(), Mockito.any());

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    when(dataflowController.getDocumentInfoById(Mockito.any())).thenReturn(new DocumentVO());
    doThrow(new EEAException()).when(documentService).uploadDocument(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any());
    try {
      documentController.updateDocument(fileMock, 1L, "ES", "desc", 1L, true);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
    }
  }

  /**
   * Update document exception 3 test.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void updateDocumentException3Test() throws EEAException, IOException {
    Mockito.doNothing().when(notificationControllerZuul)
        .createUserNotificationPrivate(Mockito.anyString(), Mockito.any());

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    when(dataflowController.getDocumentInfoById(Mockito.any())).thenReturn(new DocumentVO());
    doThrow(new EEAException(EEAErrorMessage.DOCUMENT_NOT_FOUND)).when(documentService)
        .uploadDocument(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    try {
      documentController.updateDocument(fileMock, 1L, null, null, 1L, true);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
      assertEquals(EEAErrorMessage.DOCUMENT_NOT_FOUND, e.getReason());
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
    Mockito.doNothing().when(notificationControllerZuul)
        .createUserNotificationPrivate(Mockito.anyString(), Mockito.any());

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    when(dataflowController.getDocumentInfoById(Mockito.any())).thenReturn(new DocumentVO());
    doNothing().when(documentService).uploadDocument(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any());
    documentController.updateDocument(fileMock, 1L, "ES", "desc", 1L, null);
    Mockito.verify(documentService, times(1)).uploadDocument(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any());
  }


  /**
   * Update document legacy success test.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void updateDocumentLegacySuccessTest() throws EEAException, IOException {
    Mockito.doNothing().when(notificationControllerZuul)
        .createUserNotificationPrivate(Mockito.anyString(), Mockito.any());

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    when(dataflowController.getDocumentInfoById(Mockito.any())).thenReturn(new DocumentVO());
    doNothing().when(documentService).uploadDocument(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any());
    documentController.updateDocumentLegacy(fileMock, 1L, "ES", "desc", 1L, null);
    Mockito.verify(documentService, times(1)).uploadDocument(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any());
  }

  /**
   * Update document success 2 test.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void updateDocumentSuccess2Test() throws EEAException, IOException {
    Mockito.doNothing().when(notificationControllerZuul)
        .createUserNotificationPrivate(Mockito.anyString(), Mockito.any());

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    when(dataflowController.getDocumentInfoById(Mockito.any())).thenReturn(new DocumentVO());
    doNothing().when(documentService).updateDocument(Mockito.any());
    documentController.updateDocument(null, 1L, "ES", "desc", 1L, true);
    Mockito.verify(documentService, times(1)).updateDocument(Mockito.any());
  }

  /**
   * Update document success 3 test.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void updateDocumentSuccess3Test() throws EEAException, IOException {
    Mockito.doNothing().when(notificationControllerZuul)
        .createUserNotificationPrivate(Mockito.anyString(), Mockito.any());

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    when(dataflowController.getDocumentInfoById(Mockito.any())).thenReturn(new DocumentVO());
    doNothing().when(documentService).updateDocument(Mockito.any());
    documentController.updateDocument(emptyFileMock, 1L, "ES", "desc", 1L, true);
    Mockito.verify(documentService, times(1)).updateDocument(Mockito.any());
  }

  /**
   * Test upload snapshot success.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void testUploadSnapshotSuccess() throws EEAException, IOException {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    doNothing().when(documentService).uploadSchemaSnapshot(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any());
    documentController.uploadSchemaSnapshotDocument(fileMock.getBytes(), 1L, "desc.json");
    Mockito.verify(documentService, times(1)).uploadSchemaSnapshot(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any());
  }

  /**
   * Test upload snapshot exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testUploadSnapshotException() throws EEAException {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    documentController.uploadSchemaSnapshotDocument(null, 1L, "desc");

  }


  /**
   * Test upload snapshot exception 2.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test(expected = ResponseStatusException.class)
  public void testUploadSnapshotException2() throws EEAException, IOException {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    documentController.uploadSchemaSnapshotDocument(fileMock.getBytes(), null, "desc");
  }



  /**
   * Test upload snapshot exception 3.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test(expected = ResponseStatusException.class)
  public void testUploadSnapshotException3() throws EEAException, IOException {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    doThrow(new EEAException()).when(documentService).uploadSchemaSnapshot(Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any());
    documentController.uploadSchemaSnapshotDocument(fileMock.getBytes(), 1L, "desc");
  }


  /**
   * Test upload snapshot exception 4.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test(expected = ResponseStatusException.class)
  public void testUploadSnapshotException4() throws EEAException, IOException {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    doThrow(new EEAException(EEAErrorMessage.DOCUMENT_NOT_FOUND)).when(documentService)
        .uploadSchemaSnapshot(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    documentController.uploadSchemaSnapshotDocument(fileMock.getBytes(), 1L, "desc");
  }


  /**
   * Test get snapshot success.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void testGetSnapshotSuccess() throws EEAException, IOException {
    FileResponse content = new FileResponse();
    content.setBytes(fileMock.getBytes());

    when(documentService.getSnapshotDocument(Mockito.any(), Mockito.any())).thenReturn(content);
    documentController.getSnapshotDocument(1L, "test");
    Mockito.verify(documentService, times(1)).getSnapshotDocument(Mockito.any(), Mockito.any());
  }



  /**
   * Test get snapshot exception null.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testGetSnapshotExceptionNull() throws EEAException {

    documentController.getSnapshotDocument(null, "test");
  }


  /**
   * Test get snapshot exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testGetSnapshotException() throws EEAException {

    doThrow(new EEAException(EEAErrorMessage.DOCUMENT_NOT_FOUND)).when(documentService)
        .getSnapshotDocument(Mockito.any(), Mockito.any());
    documentController.getSnapshotDocument(1L, "test");
  }

  /**
   * Test get snapshot exception 2.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testGetSnapshotException2() throws EEAException {

    doThrow(new EEAException(EEAErrorMessage.DOCUMENT_UPLOAD_ERROR)).when(documentService)
        .getSnapshotDocument(Mockito.any(), Mockito.any());
    documentController.getSnapshotDocument(1L, "test");
  }


  /**
   * Test delete snapshot success.
   *
   * @throws Exception the exception
   */
  @Test
  public void testDeleteSnapshotSuccess() throws Exception {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    FileResponse content = new FileResponse();
    content.setBytes(fileMock.getBytes());
    doNothing().when(documentService).deleteSnapshotDocument(Mockito.any(), Mockito.any());
    documentController.deleteSnapshotSchemaDocument(1L, "test");
    Mockito.verify(documentService, times(1)).deleteSnapshotDocument(Mockito.any(), Mockito.any());
  }

  /**
   * Test delete snapshot exception null.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testDeleteSnapshotExceptionNull() throws Exception {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    documentController.deleteSnapshotSchemaDocument(null, "test");
  }


  /**
   * Tets delete snapshot exception.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void tetsDeleteSnapshotException() throws Exception {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    doThrow(new EEAException(EEAErrorMessage.DOCUMENT_NOT_FOUND)).when(documentService)
        .deleteSnapshotDocument(Mockito.any(), Mockito.any());
    documentController.deleteSnapshotSchemaDocument(1L, "test");
  }


  /**
   * Test delete snapshot exception 2.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testDeleteSnapshotException2() throws Exception {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    doThrow(new EEAException(EEAErrorMessage.DOCUMENT_UPLOAD_ERROR)).when(documentService)
        .deleteSnapshotDocument(Mockito.any(), Mockito.any());
    documentController.deleteSnapshotSchemaDocument(1L, "test");
  }

  @Test
  public void testUploadCollaborationSuccess() throws EEAException, IOException {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    doNothing().when(documentService).uploadCollaborationDocument(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any());
    documentController.uploadCollaborationDocument(fileMock.getBytes(), 1L, "desc.json", "json",
        1L);
    Mockito.verify(documentService, times(1)).uploadCollaborationDocument(Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
  }

  @Test
  public void testGetCollaborationDocumentSuccess() throws EEAException, IOException {
    FileResponse content = new FileResponse();
    content.setBytes(fileMock.getBytes());

    when(documentService.getCollaborationDocument(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(content);
    documentController.getCollaborationDocument(1L, "test", 1L);
    Mockito.verify(documentService, times(1)).getCollaborationDocument(Mockito.any(), Mockito.any(),
        Mockito.any());
  }

  @Test
  public void testDeleteCollaborationDocumentSuccess() throws Exception {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    FileResponse content = new FileResponse();
    content.setBytes(fileMock.getBytes());
    doNothing().when(documentService).deleteCollaborationDocument(Mockito.any(), Mockito.any(),
        Mockito.any());
    documentController.deleteCollaborationDocument(1L, "test", 1L);
    Mockito.verify(documentService, times(1)).deleteCollaborationDocument(Mockito.any(),
        Mockito.any(), Mockito.any());
  }



  @Test(expected = ResponseStatusException.class)
  public void testUploadCollaborationDocumentException() throws EEAException {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    documentController.uploadCollaborationDocument(null, 1L, "desc.json", "json", 1L);
  }



  @Test(expected = ResponseStatusException.class)
  public void testUploadCollaborationException2() throws EEAException, IOException {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    documentController.uploadCollaborationDocument(fileMock.getBytes(), null, "desc.json", "json",
        1L);
  }



  @Test(expected = ResponseStatusException.class)
  public void testUploadCollaborationException3() throws EEAException, IOException {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    doThrow(new EEAException()).when(documentService).uploadCollaborationDocument(Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    documentController.uploadCollaborationDocument(fileMock.getBytes(), 1L, "desc", "json", 1L);
  }


  @Test(expected = ResponseStatusException.class)
  public void testGetCollaborationDocumentExceptionNull() throws EEAException {

    documentController.getCollaborationDocument(null, "test", 1L);
  }


  @Test(expected = ResponseStatusException.class)
  public void testGetCollaborationDocumentException() throws EEAException {

    doThrow(new EEAException(EEAErrorMessage.DOCUMENT_NOT_FOUND)).when(documentService)
        .getCollaborationDocument(Mockito.any(), Mockito.any(), Mockito.any());
    documentController.getCollaborationDocument(1L, "test", 1L);
  }


  @Test(expected = ResponseStatusException.class)
  public void testGetCollaborationDocumentException2() throws EEAException {

    doThrow(new EEAException(EEAErrorMessage.DOCUMENT_UPLOAD_ERROR)).when(documentService)
        .getCollaborationDocument(Mockito.any(), Mockito.any(), Mockito.any());
    documentController.getCollaborationDocument(1L, "test", 1L);
  }

  @Test(expected = ResponseStatusException.class)
  public void testDeleteCollaborationDocumentExceptionNull() throws Exception {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    documentController.deleteCollaborationDocument(null, "test", 1L);
  }


  @Test(expected = ResponseStatusException.class)
  public void tetsDeleteCollaborationDocumentException() throws Exception {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    doThrow(new EEAException(EEAErrorMessage.DOCUMENT_NOT_FOUND)).when(documentService)
        .deleteCollaborationDocument(Mockito.any(), Mockito.any(), Mockito.any());
    documentController.deleteCollaborationDocument(1L, "test", 1L);
  }


  @Test(expected = ResponseStatusException.class)
  public void testDeleteCollaborationDocumentException2() throws Exception {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    doThrow(new EEAException(EEAErrorMessage.DOCUMENT_UPLOAD_ERROR)).when(documentService)
        .deleteCollaborationDocument(Mockito.any(), Mockito.any(), Mockito.any());
    documentController.deleteCollaborationDocument(1L, "test", 1L);
  }


  @Test
  public void getAllDocumentsByDataflowTest() {
    Mockito.when(dataflowController.getAllDocumentsByDataflowId(Mockito.anyLong()))
        .thenReturn(new ArrayList<>());
    Assert.assertNotNull(documentController.getAllDocumentsByDataflow(1L));
  }

  @Test
  public void getPublicDocumentTest() throws EEAException, IOException {
    DocumentVO document = new DocumentVO();
    document.setId(1L);
    document.setIsPublic(true);
    FileResponse content = new FileResponse();
    content.setBytes(fileMock.getBytes());
    Mockito.when(dataflowController.getDocumentInfoById(Mockito.anyLong())).thenReturn(document);
    when(documentService.getDocument(Mockito.any(), Mockito.any())).thenReturn(content);
    Assert.assertNotNull(documentController.getPublicDocument(1L));
  }

  @Test(expected = ResponseStatusException.class)
  public void getPublicDocumentExceptionTest() {
    try {
      Resource resource = documentController.getPublicDocument(null);
    } catch (ResponseStatusException e) {
      assertEquals(e.getStatus(), HttpStatus.NOT_FOUND);
      throw e;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void getPublicDocumentBadRequestException() {
    DocumentVO document = new DocumentVO();
    document.setId(1L);
    document.setIsPublic(false);
    Mockito.when(dataflowController.getDocumentInfoById(Mockito.anyLong())).thenReturn(document);
    try {
      Resource resource = documentController.getPublicDocument(1L);
    } catch (ResponseStatusException e) {
      assertEquals(e.getStatus(), HttpStatus.BAD_REQUEST);
      throw e;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void getPublicDocumentDocumentInternalServerErrorExceptionTest()
      throws EEAException, IOException {
    DocumentVO document = new DocumentVO();
    document.setId(1L);
    document.setIsPublic(true);
    FileResponse content = new FileResponse();
    content.setBytes(fileMock.getBytes());
    Mockito.when(dataflowController.getDocumentInfoById(Mockito.anyLong())).thenReturn(document);
    when(documentService.getDocument(Mockito.any(), Mockito.any())).thenThrow(EEAException.class);
    try {
      Resource resource = documentController.getPublicDocument(1L);
    } catch (ResponseStatusException e) {
      assertEquals(e.getStatus(), HttpStatus.INTERNAL_SERVER_ERROR);
      throw e;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void getPublicDocumentDocumentNotFoundExceptionTest() throws EEAException, IOException {
    DocumentVO document = new DocumentVO();
    document.setId(1L);
    document.setIsPublic(true);
    FileResponse content = new FileResponse();
    content.setBytes(fileMock.getBytes());
    Mockito.when(dataflowController.getDocumentInfoById(Mockito.anyLong())).thenReturn(document);
    when(documentService.getDocument(Mockito.any(), Mockito.any()))
        .thenThrow(new EEAException(EEAErrorMessage.DOCUMENT_NOT_FOUND));
    try {
      Resource resource = documentController.getPublicDocument(1L);
    } catch (ResponseStatusException e) {
      assertEquals(e.getStatus(), HttpStatus.NOT_FOUND);
      throw e;
    }
  }

  @Test
  public void getAllDocumentsByDataflowLegacyTest() {
    Mockito.when(dataflowController.getAllDocumentsByDataflowId(Mockito.anyLong()))
        .thenReturn(new ArrayList<>());
    Assert.assertNotNull(documentController.getAllDocumentsByDataflowLegacy(1L));
  }


}
