package org.eea.document.service.impl;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
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
import org.eea.document.service.DocumentService;
import org.eea.document.type.FileResponse;
import org.eea.document.type.NodeType;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * The type Document service.
 *
 * @author ruben.lozano
 *
 */
@Service("documentService")
public class DocumentServiceImpl implements DocumentService {

  /** The Constant CACHE_SIZE. */
  private static final int CACHE_SIZE = 16;

  /** The Constant PORT. */
  private static final int PORT = 27017;

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(DocumentServiceImpl.class);

  /** The Constant ADMIN. */
  private static final String ADMIN = "admin";

  /** The kafka sender utils. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /**
   * upload a document to the jackrabbit content repository.
   *
   * @param file the file
   * @param dataFlowId the data flow id
   * @param language the language
   * @param description the description
   * @throws EEAException the EEA exception
   */
  @Override
  @Async
  public void uploadDocument(final MultipartFile file, final Long dataFlowId, final String language,
      final String description) throws EEAException {
    Session session = null;
    DocumentNodeStore ns = null;
    try {
      LOG.info("Adding the file...");
      // Initialize the session
      ns = initializeNode();
      Repository repository = new Jcr(new Oak(ns)).createRepository();
      session = initializeSession(session, repository);

      // Add a file node with the document
      String modifiedFilename = addFileNode(session, "/" + dataFlowId, file.getInputStream(),
          insertStringBeforePoint(file.getOriginalFilename(), "-" + language), file.getSize(),
          file.getContentType());

      LOG.info("File added...");
      sendKafkaNotification(modifiedFilename.replace("-" + language, ""), dataFlowId, language,
          description, EventType.LOAD_DOCUMENT_COMPLETED_EVENT);
    } catch (RepositoryException | IOException | EEAException e) {
      throw new EEAException(EEAErrorMessage.DOCUMENT_UPLOAD_ERROR, e);
    } finally {
      cleanUp(session);
      if (ns != null) {
        ns.dispose();
      }
    }
  }

  /**
   * Gets the document.
   *
   * @param documentName the document name
   * @param dataFlowId the data flow id
   * @param language the language
   * @return the document
   * @throws EEAException the EEA exception
   */
  @Override
  public FileResponse getDocument(final String documentName, final Long dataFlowId,
      final String language) throws EEAException {
    Session session = null;
    FileResponse fileResponse = null;
    DocumentNodeStore ns = null;
    try (FileOutputStream fos = new FileOutputStream(documentName)) {
      // Initialize the session
      ns = initializeNode();
      Repository repository = new Jcr(new Oak(ns)).createRepository();
      session = initializeSession(session, repository);

      // downloading the file to the controller
      fileResponse = getFileContents(session, "/" + dataFlowId,
          insertStringBeforePoint(documentName, "-" + language));
      LOG.info("Fething the file...");
    } catch (IOException | RepositoryException e) {
      if (e.getClass().equals(PathNotFoundException.class)) {
        throw new EEAException(EEAErrorMessage.DOCUMENT_NOT_FOUND, e);
      }
      throw new EEAException(EEAErrorMessage.DOCUMENT_DOWNLOAD_ERROR, e);
    } finally {
      cleanUp(session);
      if (ns != null) {
        ns.dispose();
      }
    }
    return fileResponse;
  }

  /**
   * Delete document.
   *
   * @param documentName the document name
   * @param dataFlowId the data flow id
   * @param language the language
   * @throws EEAException the EEA exception
   */
  @Override
  @Modified
  public void deleteDocument(String documentName, Long dataFlowId, final String language)
      throws EEAException {
    Session session = null;
    DocumentNodeStore ns = null;
    try {
      // Initialize the session
      ns = initializeNode();
      Repository repository = new Jcr(new Oak(ns)).createRepository();
      session = initializeSession(session, repository);
      // Delete a file node with the document
      deleteFileNode(session, dataFlowId.toString(),
          insertStringBeforePoint(documentName, "-" + language));
      session.save();
      LOG.info("File deleted...");

      ns.getClock().waitUntil(ns.getClock().getTime() + 6000);
      ns.getVersionGarbageCollector().gc(0, TimeUnit.MILLISECONDS);
      MarkSweepGarbageCollector gc = new MarkSweepGarbageCollector(
          new DocumentBlobReferenceRetriever(ns), (GarbageCollectableBlobStore) ns.getBlobStore(),
          (ThreadPoolExecutor) Executors.newFixedThreadPool(1), "./target", 5, -1,
          ClusterRepositoryInfo.getId(ns));
      gc.collectGarbage(false);

      sendKafkaNotification(documentName, dataFlowId, language, null,
          EventType.DELETE_DOCUMENT_COMPLETED_EVENT);

    } catch (Exception e) {
      if (e.getClass().equals(PathNotFoundException.class)) {
        throw new EEAException(EEAErrorMessage.DOCUMENT_NOT_FOUND, e);
      }
      throw new EEAException(EEAErrorMessage.EXECUTION_ERROR, e);
    } finally {
      if (ns != null) {
        ns.dispose();
      }
      cleanUp(session);
    }
  }

  /**
   * Insert string before point.
   *
   * @param documentName the document name
   * @param language the language
   * @return the string
   */
  private String insertStringBeforePoint(final String documentName, final String language) {
    int location = 0;
    if (documentName.contains(").")) {
      location = documentName.lastIndexOf("(");
    } else {
      location = documentName.lastIndexOf('.');
    }
    return documentName.substring(0, location) + language + documentName.substring(location);
  }

  /**
   * Initialize session.
   *
   * @param session the session
   * @param repository the repository
   * @return the session
   * @throws RepositoryException the repository exception
   */
  private Session initializeSession(Session session, Repository repository)
      throws RepositoryException {
    if (repository.getDescriptorKeys() != null) {
      session = repository.login(new SimpleCredentials(ADMIN, ADMIN.toCharArray()));
    }
    return session;
  }

  /**
   * Initialize node.
   *
   * @return the document node store
   */
  private DocumentNodeStore initializeNode() {
    final String uri = "mongodb://" + "localhost" + ":" + PORT;
    // creates a node with name oak
    return new MongoDocumentNodeStoreBuilder().setMongoDB(uri, "oak", CACHE_SIZE).build();
  }

  /**
   * Send kafka notification.
   *
   * @param filename the filename
   * @param dataFlowId the data flow id
   * @param language the language
   * @param description the description
   * @param eventType the event type
   */
  private void sendKafkaNotification(final String filename, final Long dataFlowId,
      final String language, final String description, final EventType eventType) {
    Map<String, Object> result = new HashMap<>();
    result.put("dataflow_id", dataFlowId);
    result.put("filename", filename);
    result.put("language", language);
    result.put("description", description);
    kafkaSenderUtils.releaseKafkaEvent(eventType, result);
  }

  /**
   * Adds a file node if it's possible.
   *
   * @param session the session
   * @param absPath the abs path
   * @param is the file
   * @param filename the filename
   * @param size the size
   * @param contentType the content type
   * @return the string
   * @throws RepositoryException the repository exception
   * @throws EEAException the EEA exception
   */
  public String addFileNode(final Session session, final String absPath, final InputStream is,
      final String filename, final Long size, final String contentType)
      throws RepositoryException, EEAException {

    Node node = createNodes(session, absPath);
    String newFilename = filename;
    if (node.hasNode(filename)) {
      LOG.info("File already added.");
      newFilename = increaseCounterFileName(node, filename);
    }
    // Created a node with that of file Name
    Node fileHolder = node.addNode(newFilename, "nt:file");
    // fileHolder.addMixin("mix:versionable");

    // create node of type file.

    Date now = new Date();

    // creation of file content node.
    Node content = fileHolder.addNode("jcr:content", "nt:resource");
    content.setProperty("jcr:mimeType", contentType);
    Binary binary = session.getValueFactory().createBinary(is);

    content.setProperty("jcr:data", binary);
    content.setProperty("jcr:lastModified", now.toInstant().toString());
    session.save();
    // VersionManager vm = session.getWorkspace().getVersionManager();
    // vm.checkin(fileHolder.getPath());
    LOG.info("File Saved...");
    return newFilename;
  }

  /**
   * Increase counter file name.
   *
   * @param node the node
   * @param filename the filename
   * @return the string
   * @throws RepositoryException the repository exception
   */
  private String increaseCounterFileName(final Node node, final String filename)
      throws RepositoryException {
    String result = filename;
    for (int i = 1; i < 1000; i++) {
      result = insertStringBeforePoint(filename, "(" + i + ")");
      if (!node.hasNode(result)) {
        break;
      }
    }
    return result;
  }

  /**
   * Delete file node.
   *
   * @param session the session
   * @param relPath the rel path
   * @param documentName the document name
   * @throws RepositoryException the repository exception
   * @throws EEAException the EEA exception
   */
  private void deleteFileNode(final Session session, final String relPath,
      final String documentName) throws RepositoryException, EEAException {
    if (session == null) {
      throw new EEAException("Session doesn't exist");
    }
    Node root = session.getRootNode();
    if (root.hasNode(relPath)) {
      Node parentNode = root.getNode(relPath);
      Node node = parentNode.getNode(documentName);
      if (node != null) {
        // VersionManager versionManager = session.getWorkspace().getVersionManager();
        // VersionHistory vHistory = versionManager.getVersionHistory(node.getPath());
        // for (VersionIterator pt = vHistory.getAllVersions(); pt.hasNext();) {
        // Version p = pt.nextVersion();
        // System.out.println(p.getPath());
        // System.out.println(p.getName());
        // System.out.println(p.getFrozenNode());
        // System.out.println(p.getReferences().toString());
        // if (!"jcr:rootVersion".equals(p.getName())) {
        // p.getFrozenNode().setProperty("theFile", (Node) null);
        // }
        // }
        node.remove();
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
   * @return the node
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
      LOG.info("Nodes already exist!");
      return session.getNode(absPath);
    }
    String[] nodeNames = (null != absPath) ? absPath.split("/") : new String[1];
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
  private static Node createNodes(final Session session, final String[] nodes)
      throws RepositoryException {
    Node parentNode = session.getRootNode();
    for (String childNode : nodes) {
      if (StringUtils.isNotBlank(childNode)) {
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
   * @return true, if successful
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
   */
  private void cleanUp(final Session session) {
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
  public static FileResponse getFileContents(final Session session, final String basePath,
      final String fileName) throws RepositoryException, IOException {
    // Obtains the information from the node,name, content and type.
    Node node = session.getNode(basePath);
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

}
