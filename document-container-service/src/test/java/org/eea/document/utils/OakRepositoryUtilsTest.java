package org.eea.document.utils;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.io.IOException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import org.apache.sling.testing.mock.jcr.MockJcr;
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

/**
 * The Class OakRepositoryUtilsTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class OakRepositoryUtilsTest {
  /** The document service. */
  @InjectMocks
  private OakRepositoryUtils oakRepositoryUtils;

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
    MockitoAnnotations.openMocks(this);
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
    oakRepositoryUtils.addFileNode(null, "/", fileMock.getInputStream(),
        fileMock.getOriginalFilename(), fileMock.getContentType());
    Mockito.verify(oakRepositoryUtils, times(1)).addFileNode(Mockito.any(), Mockito.any(),
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
    oakRepositoryUtils.addFileNode(session, "1", fileMock.getInputStream(),
        fileMock.getOriginalFilename(), fileMock.getContentType());
    Mockito.verify(oakRepositoryUtils, times(1)).addFileNode(Mockito.any(), Mockito.any(),
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
    oakRepositoryUtils.addFileNode(session, "1/", fileMock.getInputStream(),
        fileMock.getOriginalFilename(), fileMock.getContentType());
    Mockito.verify(oakRepositoryUtils, times(1)).addFileNode(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any());
  }

  /**
   * Insert string before point test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void insertStringBeforePointTest() throws EEAException {
    assertEquals("not the expected result", "TestES.jpg",
        oakRepositoryUtils.insertStringBeforePoint("Test.jpg", "ES"));
  }

  /**
   * Insert string before point parenthesis test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void insertStringBeforePointParenthesisTest() throws EEAException {
    assertEquals("not the expected result", "TestES(1).jpg",
        oakRepositoryUtils.insertStringBeforePoint("Test(1).jpg", "ES"));
  }

  /**
   * Insert string before point exception 1 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void insertStringBeforePointException1Test() throws EEAException {
    oakRepositoryUtils.insertStringBeforePoint("", "ES");
  }

  /**
   * Insert string before point exception 2 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void insertStringBeforePointException2Test() throws EEAException {
    oakRepositoryUtils.insertStringBeforePoint("Test(1).jpg", "");
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
    oakRepositoryUtils.getFileContents(session, "", "filename");
    Mockito.verify(oakRepositoryUtils, times(1)).getFileContents(Mockito.any(), Mockito.any(),
        Mockito.any());
  }

  /**
   * Delete file node test.
   *
   * @throws RepositoryException the repository exception
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void deleteFileNodeTest() throws RepositoryException, EEAException {
    oakRepositoryUtils.deleteFileNode(session, "", "documentName");
    Mockito.verify(oakRepositoryUtils, times(1)).deleteFileNode(Mockito.any(), Mockito.any(),
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
    oakRepositoryUtils.deleteFileNode(null, "", "documentName");
    Mockito.verify(oakRepositoryUtils, times(1)).deleteFileNode(Mockito.any(), Mockito.any(),
        Mockito.any());
  }
}
