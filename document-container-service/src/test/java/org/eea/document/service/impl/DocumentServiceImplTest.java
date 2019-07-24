package org.eea.document.service.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.io.IOException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import org.apache.sling.testing.mock.jcr.MockJcr;
import org.eea.exception.EEAException;
import org.eea.kafka.domain.EventType;
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

  /** The file mock. */
  private MockMultipartFile fileMock;

  /** The session. */
  @Mock
  Session session;

  /** The node. */
  @Mock
  Node node;

  /**
   * Inits the mocks.
   *
   * @throws RepositoryException the repository exception
   */
  @Before
  public void initMocks() throws RepositoryException {
    fileMock = new MockMultipartFile("file", "fileOriginal", "cvs", "content".getBytes());
    session = MockJcr.newSession();
    session.getRootNode().addNode("1", "nt:file");
    session.save();
    MockitoAnnotations.initMocks(this);
  }


  /**
   * Adds the file node exception test.
   *
   * @throws EEAException the EEA exception
   * @throws RepositoryException the repository exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test(expected = EEAException.class)
  public void addFileNodeExceptionTest() throws EEAException, RepositoryException, IOException {
    documentService.addFileNode(null, "/", fileMock.getInputStream(),
        fileMock.getOriginalFilename(), fileMock.getContentType());
    Mockito.verify(documentService, times(1)).addFileNode(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any());
  }

  /**
   * Adds the file node test.
   *
   * @throws EEAException the EEA exception
   * @throws RepositoryException the repository exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test(expected = EEAException.class)
  public void addFileNodeTest() throws EEAException, RepositoryException, IOException {
    when(session.itemExists(Mockito.any())).thenReturn(true);
    when(session.getNode(Mockito.any())).thenReturn(node);
    documentService.addFileNode(session, "1", fileMock.getInputStream(),
        fileMock.getOriginalFilename(), fileMock.getContentType());
    Mockito.verify(documentService, times(1)).addFileNode(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any());
  }

  /**
   * Adds the file node new test.
   *
   * @throws EEAException the EEA exception
   * @throws RepositoryException the repository exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test(expected = EEAException.class)
  public void addFileNodeNewTest() throws EEAException, RepositoryException, IOException {
    when(session.itemExists(Mockito.any())).thenReturn(false);
    documentService.addFileNode(session, "1/", fileMock.getInputStream(),
        fileMock.getOriginalFilename(), fileMock.getContentType());
    Mockito.verify(documentService, times(1)).addFileNode(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any());
  }

  /**
   * Insert string before point test.
   */
  @Test
  public void insertStringBeforePointTest() {
    assertEquals("TestES.jpg", documentService.insertStringBeforePoint("Test.jpg", "ES"));
  }

  /**
   * Insert string before point parenthesis test.
   */
  @Test
  public void insertStringBeforePointParenthesisTest() {
    assertEquals("TestES(1).jpg", documentService.insertStringBeforePoint("Test(1).jpg", "ES"));
  }

  /**
   * Gets the file contents test.
   *
   * @return the file contents test
   * @throws RepositoryException the repository exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void getFileContentsTest() throws RepositoryException, IOException, EEAException {
    documentService.getFileContents(session, "", "filename");
    Mockito.verify(documentService, times(1)).getFileContents(Mockito.any(), Mockito.any(),
        Mockito.any());
  }

  /**
   * Send kafka notification test.
   */
  @Test
  public void sendKafkaNotificationTest() {
    documentService.sendKafkaNotification("filename", 1L, "ES", "desc",
        EventType.LOAD_DOCUMENT_COMPLETED_EVENT);
    Mockito.verify(kafkaSenderUtils, times(1)).releaseKafkaEvent(Mockito.any(), Mockito.any());
  }

  /**
   * Delete file node test.
   *
   * @throws RepositoryException the repository exception
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void deleteFileNodeTest() throws RepositoryException, EEAException {
    documentService.deleteFileNode(session, "", "documentName");
    Mockito.verify(documentService, times(1)).deleteFileNode(Mockito.any(), Mockito.any(),
        Mockito.any());
  }

  /**
   * Delete file node null session test.
   *
   * @throws RepositoryException the repository exception
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void deleteFileNodeNullSessionTest() throws RepositoryException, EEAException {
    documentService.deleteFileNode(null, "", "documentName");
    Mockito.verify(documentService, times(1)).deleteFileNode(Mockito.any(), Mockito.any(),
        Mockito.any());
  }
}
