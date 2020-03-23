package org.eea.document.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.oak.Oak;
import org.apache.jackrabbit.oak.jcr.Jcr;
import org.apache.jackrabbit.oak.plugins.blob.MarkSweepGarbageCollector;
import org.apache.jackrabbit.oak.plugins.document.DocumentBlobReferenceRetriever;
import org.apache.jackrabbit.oak.plugins.document.DocumentNodeStore;
import org.apache.jackrabbit.oak.plugins.document.mongo.MongoDocumentNodeStoreBuilder;
import org.apache.jackrabbit.oak.spi.blob.GarbageCollectableBlobStore;
import org.apache.jackrabbit.oak.spi.cluster.ClusterRepositoryInfo;
import org.eea.document.type.FileResponse;
import org.eea.document.type.NodeType;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * The Class OakRepositoryUtils.
 */
@Component
public class OakRepositoryUtils {


  /**
   * The Constant UPDATE_DELAY.
   */
  private static final int UPDATE_DELAY = 6000;

  /**
   * The Constant LEFT_PARENTHESIS.
   */
  private static final String LEFT_PARENTHESIS = "(";

  /**
   * The Constant CACHE_SIZE.
   */
  private static final int NODE_CACHE_SIZE = 16;


  /**
   * The name oak collection.
   */
  @Value("${nameOakCollection}")
  private String nameOakCollection;

  /**
   * The oak port.
   */
  @Value("${mongodb.primary.port}")
  private int oakPort;

  /**
   * The oak user.
   */
  @Value("${oakUser}")
  private String oakUser;

  /**
   * The target directory.
   */
  @Value("${targetDirectory}")
  private String targetDirectory;

  /**
   * The mongo hosts
   */
  @Value("${mongodb.hosts}")
  private String mongoHosts;
  /**
   * The Constant PATH_DELIMITER.
   */
  private static final String PATH_DELIMITER = "/";

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(OakRepositoryUtils.class);

  /**
   * Insert string before point.
   *
   * @param documentName the document name
   * @param language the language
   *
   * @return the string
   *
   * @throws EEAException the EEA exception
   */
  public String insertStringBeforePoint(final String documentName, final String language)
      throws EEAException {
    int location = 0;
    if (StringUtils.isBlank(documentName) || StringUtils.isBlank(language)) {
      throw new EEAException(EEAErrorMessage.FILE_NAME);
    }
    if (documentName.contains(").")) {
      location = documentName.lastIndexOf(LEFT_PARENTHESIS);
    } else {
      location = documentName.lastIndexOf('.');
    }
    return documentName.substring(0, location) + language + documentName.substring(location);
  }

  /**
   * Initialize session.
   *
   * @param repository the repository
   *
   * @return the session
   *
   * @throws RepositoryException the repository exception
   * @throws EEAException the EEA exception
   */
  public Session initializeSession(Repository repository) throws RepositoryException, EEAException {
    Session session = null;
    if (repository.getDescriptorKeys() != null) {
      session = repository.login(new SimpleCredentials(oakUser, oakUser.toCharArray()));
    }
    if (session == null) {
      throw new EEAException(EEAErrorMessage.REPOSITORY_NOT_FOUND);
    }
    return session;
  }

  /**
   * Initialize node.
   *
   * @return the document node store
   */
  public DocumentNodeStore initializeNodeStore() {
    // creates a node with name oak
    return new MongoDocumentNodeStoreBuilder()
        .setMongoDB(new StringBuilder("mongodb://").append(mongoHosts).toString(),
            nameOakCollection, NODE_CACHE_SIZE)
        .build();
  }

  /**
   * Initialize repository.
   *
   * @param ns the ns
   *
   * @return the repository
   */
  public Repository initializeRepository(DocumentNodeStore ns) {
    return new Jcr(new Oak(ns)).createRepository();
  }


  /**
   * Adds a file node if it's possible.
   *
   * @param session the session
   * @param absPath the abs path
   * @param is the file
   * @param filename the filename
   * @param contentType the content type
   *
   * @return the string
   *
   * @throws RepositoryException the repository exception
   * @throws EEAException the EEA exception
   */
  public String addFileNode(final Session session, final String absPath, final InputStream is,
      final String filename, final String contentType) throws RepositoryException, EEAException {

    Node node = createNodes(session, absPath);
    if (node == null) {
      throw new EEAException("Error creating nodes");
    }
    // Created a node with that of file Name
    if (node.hasNode(filename)) {
      Node oldnode = node.getNode(filename);
      oldnode.remove();
    }
    Node fileHolder = node.addNode(filename, "nt:file");
    Date now = new Date();

    if (fileHolder == null) {
      throw new EEAException("Error creating nodes");
    }
    // creation of file content node.
    Node content = fileHolder.addNode("jcr:content", "nt:resource");
    content.setProperty("jcr:mimeType", contentType);
    Binary binary = session.getValueFactory().createBinary(is);

    content.setProperty("jcr:data", binary);
    content.setProperty("jcr:lastModified", now.toInstant().toString());
    session.save();
    LOG.info("File Saved...");
    return filename;
  }

  /**
   * Delete file node.
   *
   * @param session the session
   * @param relPath the rel path
   * @param documentName the document name
   *
   * @throws RepositoryException the repository exception
   * @throws EEAException the EEA exception
   */
  public void deleteFileNode(final Session session, final String relPath, final String documentName)
      throws RepositoryException, EEAException {
    if (session == null) {
      throw new EEAException("Session doesn't exist");
    }
    Node root = session.getRootNode();
    if (root == null) {
      throw new EEAException(EEAErrorMessage.FILE_NOT_FOUND);
    }
    if (root.hasNode(relPath)) {
      Node parentNode = root.getNode(relPath);
      if (parentNode == null) {
        throw new EEAException("Error getting nodes");
      }
      Node node = parentNode.getNode(documentName);
      if (node != null) {
        node.remove();
        session.save();
      }
    } else {
      LOG.info("Node does not exists!");
      throw new EEAException(EEAErrorMessage.FILE_NOT_FOUND);
    }

  }

  /**
   * Creates or retrieves nodes.
   *
   * @param session the session
   * @param absPath the abs path
   *
   * @return the node
   *
   * @throws RepositoryException the repository exception
   * @throws EEAException the EEA exception
   */
  public static Node createNodes(final Session session, final String absPath)
      throws RepositoryException, EEAException {
    // check if the value session is null

    if (session == null) {
      throw new EEAException("Session doesn't exist");
    }
    // check if the node is already created
    if (session.itemExists(absPath)) {
      LOG.info("Nodes already exist.");
      return session.getNode(absPath);
    }
    String[] nodeNames = (null != absPath) ? absPath.split(PATH_DELIMITER) : new String[1];
    Node node = createNodes(session, nodeNames);
    session.save();
    return node;
  }

  /**
   * Creates nodes from a list.
   *
   * @param session the session
   * @param nodes the nodes
   *
   * @return the node
   *
   * @throws RepositoryException the repository exception
   */
  private static Node createNodes(final Session session, final String[] nodes)
      throws RepositoryException {
    Node parentNode = session.getRootNode();
    for (String childNode : nodes) {
      if (StringUtils.isNotBlank(childNode) && null != parentNode) {
        addChild(parentNode, childNode);
        parentNode = parentNode.getNode(childNode);
        // set the node type
        parentNode.setProperty("jcr:nodeType", NodeType.FOLDER.getValue());
      }
    }
    return parentNode;

  }

  /**
   * Creates a new child node.
   *
   * @param parentNode the parent node
   * @param childNode the child node
   *
   * @return true, if successful
   *
   * @throws RepositoryException the repository exception
   */
  private static boolean addChild(final Node parentNode, final String childNode)
      throws RepositoryException {
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
   * @param ns the ns
   */
  public void cleanUp(final Session session, final DocumentNodeStore ns) {
    if (session != null) {
      session.logout();
    }
    if (ns != null) {
      ns.dispose();
    }
  }

  /**
   * Reads the file and generate a FileResponse, with the content and the type.
   *
   * @param session the session
   * @param basePath the base path
   * @param fileName the file name
   *
   * @return the file contents
   *
   * @throws RepositoryException the repository exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws EEAException the EEA exception
   */
  public FileResponse getFileContents(final Session session, final String basePath,
      final String fileName) throws RepositoryException, IOException, EEAException {
    // Obtains the information from the node,name, content and type.
    Node node = session.getNode(basePath);
    if (node == null) {
      throw new EEAException("Node not found");
    }
    Node fileHolder = node.getNode(fileName);
    Node fileContent = fileHolder.getNode("jcr:content");
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

  /**
   * Delete blobs from repository.
   *
   * @param ns the ns
   *
   * @throws Exception the exception
   */
  public void deleteBlobsFromRepository(DocumentNodeStore ns) throws Exception {
    ns.getClock().waitUntil(ns.getClock().getTime() + UPDATE_DELAY);
    ns.getVersionGarbageCollector().gc(0, TimeUnit.MILLISECONDS);
    MarkSweepGarbageCollector gc = new MarkSweepGarbageCollector(
        new DocumentBlobReferenceRetriever(ns), (GarbageCollectableBlobStore) ns.getBlobStore(),
        (ThreadPoolExecutor) Executors.newFixedThreadPool(1), targetDirectory, 5, -1,
        ClusterRepositoryInfo.getId(ns));
    gc.collectGarbage(false);
  }
}
