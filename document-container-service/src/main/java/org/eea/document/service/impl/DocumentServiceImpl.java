package org.eea.document.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Date;
import javax.jcr.Binary;
import javax.jcr.GuestCredentials;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.version.VersionManager;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.oak.Oak;
import org.apache.jackrabbit.oak.jcr.Jcr;
import org.apache.jackrabbit.oak.plugins.document.DocumentNodeStore;
import org.apache.jackrabbit.oak.plugins.document.mongo.MongoDocumentNodeStoreBuilder;
import org.eea.document.service.DocumentService;
import org.eea.document.type.FileResponse;
import org.eea.document.type.NodeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The type Document service.
 * 
 * @author ruben.lozano
 *
 */
@Service("documentService")
public class DocumentServiceImpl implements DocumentService {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(DocumentServiceImpl.class);

  /** The Constant ADMIN. */
  private static final String ADMIN = "admin";

  /**
   * test the connection.
   *
   * @throws Exception the exception
   */
  @Override
  public void testLogging() throws Exception {
    // Obtain the default repository location
    Repository repository = JcrUtils.getRepository();
    // login with guest credentials
    Session session = repository.login(new GuestCredentials());
    try {
      // show a log message with the connection data
      String user = session.getUserID();
      String name = repository.getDescriptor(Repository.REP_NAME_DESC);
      String msg = String.format("Logged in as %s to a %s repository.", user, name);
      LOG.info(msg);
    } finally {
      session.logout();
    }

  }

  /**
   * upload a file to the jackrabbit content repository.
   *
   * @throws Exception the exception
   */
  @Override
  @Transactional
  public void uploadDocument() throws Exception {
    Session session = null;
    try {
      LOG.info("Adding the file...");
      // Initialize the session
      session = getSession();
      // Add a file node with the document (in this demo, hardcoded)
      addFileNode(session, "/test", new File("src/main/resources/file.txt"), ADMIN);

      LOG.info("Files added...");
    } catch (RepositoryException | IOException e) {
      LOG.error(e.getMessage());
    } finally {
      cleanUp(session);
    }
  }

  /**
   * Download the file to the fileSystem.
   *
   * @return the document
   * @throws Exception the exception
   */
  @Override
  @Transactional
  public void getDocument() throws Exception {
    Session session = null;
    try (FileOutputStream fos = new FileOutputStream("C:/OutFiles/" + "file.txt")) {
      session = getSession();
      // Initialize the session
      LOG.info("Fething the file...");
      FileResponse fileResponse = getFileContents(session, "/test", "file.txt");
      byte[] content = fileResponse.getBytes();
      // downloading the file to the filesystem
      if (content != null && content.length > 0) {
        fos.write(content);
        LOG.info("File fetch complete...");
      }
    } catch (RepositoryException | IOException e) {
      LOG.error(e.getMessage());
    } finally {
      cleanUp(session);
    }
  }

  /**
   * creates a repository in that location.
   *
   * @param host the host
   * @param port the port
   * @return the repo
   */
  public static Repository getRepo(String host, final int port) {
    String uri = "mongodb://" + host + ":" + port;
    LOG.info(uri);
    // creates a node with name oak_demo
    DocumentNodeStore ns =
        new MongoDocumentNodeStoreBuilder().setMongoDB(uri, "oak_demo", 16).build();
    return new Jcr(new Oak(ns)).createRepository();
  }

  /**
   * Gets the session.
   *
   * @return the session
   * @throws RepositoryException the repository exception
   */
  private Session getSession() throws RepositoryException {
    Repository repo = getRepo("localhost", 27017);
    if (repo.getDescriptorKeys() != null) {
      return repo.login(new SimpleCredentials(ADMIN, ADMIN.toCharArray()));
    } else {
      throw new NullPointerException("Repository not initialized");
    }
  }

  // Add a file node with the document (in this demo, hardcoded)

  /**
   * Adds a file node if it's possible.
   *
   * @param session the session
   * @param absPath the abs path
   * @param file the file
   * @param userName the user name
   * @throws RepositoryException the repository exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void addFileNode(Session session, String absPath, File file, String userName)
      throws RepositoryException, IOException {

    Node node = createNodes(session, absPath);
    if (node.hasNode(file.getName())) {
      LOG.info("File already added.");
      return;
    }
    try (FileInputStream is = new FileInputStream(file)) {
      // Created a node with that of file Name
      Node fileHolder = node.addNode(file.getName());
      fileHolder.addMixin("mix:versionable");
      fileHolder.setProperty("jcr:createdBy", userName);
      fileHolder.setProperty("jcr:nodeType", NodeType.FILE.getValue());
      fileHolder.setProperty("size", file.length());

      // create node of type file.
      Node file1 = fileHolder.addNode("theFile", "nt:file");

      Date now = new Date();

      // creation of file content node.
      Node content = file1.addNode("jcr:content", "nt:resource");
      String contentType = Files.probeContentType(file.toPath());
      content.setProperty("jcr:mimeType", contentType);
      Binary binary = session.getValueFactory().createBinary(is);

      content.setProperty("jcr:data", binary);
      content.setProperty("jcr:lastModified", now.toInstant().toString());
      session.save();
      VersionManager vm = session.getWorkspace().getVersionManager();
      vm.checkin(fileHolder.getPath());
    }
    LOG.info("File Saved...");
  }


  /**
   * Creates or retrieves nodes.
   *
   * @param session the session
   * @param absPath the abs path
   * @return the node
   * @throws RepositoryException the repository exception
   */
  public static Node createNodes(Session session, String absPath) throws RepositoryException {
    // check if the node is already created
    if (session.itemExists(absPath)) {
      LOG.info("Nodes already exist!");
      return session.getNode(absPath);
    }
    String[] nodeNames = (null != absPath) ? absPath.split("/") : null;
    Node node = createNodes(session, nodeNames);
    session.save();
    return node;
  }

  /**
   * Creates nodes from a list.
   *
   * @param session the session
   * @param nodes the nodes
   * @return the node
   * @throws RepositoryException the repository exception
   */
  private static Node createNodes(Session session, String[] nodes) throws RepositoryException {
    Node parentNode = session.getRootNode();
    for (String childNode : nodes) {
      if (StringUtils.isNotBlank(childNode)) {
        addChild(parentNode, childNode);
        parentNode = parentNode.getNode(childNode);
        parentNode.setProperty("jcr:nodeType", NodeType.FOLDER.getValue()); // set the node type
      }
    }
    return parentNode;

  }

  /**
   * Creates a new child node.
   *
   * @param parentNode the parent node
   * @param childNode the child node
   * @return true, if successful
   * @throws RepositoryException the repository exception
   */
  private static boolean addChild(Node parentNode, String childNode) throws RepositoryException {
    boolean nodeAdded = false;
    if (!parentNode.isNode()) {
      throw new RepositoryException("The parentNode does not exist..");
    }
    if (!parentNode.hasNode(childNode)) {
      parentNode.addNode(childNode);
      nodeAdded = true;
    }
    return nodeAdded;
  }

  /**
   * Clean up.
   *
   * @param session the session
   */
  private void cleanUp(Session session) {
    if (session != null) {
      session.logout();
    }
  }

  /**
   * Reads the file and generate a FileResponse, with the content and the type.
   *
   * @param session the session
   * @param basePath the base path
   * @param fileName the file name
   * @return the file contents
   * @throws RepositoryException the repository exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static FileResponse getFileContents(Session session, String basePath, String fileName)
      throws RepositoryException, IOException {
    // Obtains the information from the node,name, content and type.
    Node node = session.getNode(basePath);
    Node fileHolder = node.getNode(fileName);
    Node fileContent = fileHolder.getNode("theFile").getNode("jcr:content");
    Binary bin = fileContent.getProperty("jcr:data").getBinary();
    InputStream stream = bin.getStream();
    byte[] bytes = IOUtils.toByteArray(stream);
    bin.dispose();
    stream.close();

    // creates the FileResponse to return it
    FileResponse fileResponse = new FileResponse();
    fileResponse.setBytes(bytes);
    fileResponse.setContentType(fileContent.getProperty("jcr:mimeType").getString());
    return fileResponse;

  }
}
