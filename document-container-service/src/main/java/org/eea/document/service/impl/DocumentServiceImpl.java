package org.eea.document.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.Date;

import javax.jcr.Binary;
import javax.jcr.GuestCredentials;
import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.version.VersionManager;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.oak.Oak;
import org.apache.jackrabbit.oak.jcr.Jcr;
import org.apache.jackrabbit.oak.plugins.document.DocumentNodeStore;
import org.apache.jackrabbit.oak.plugins.document.mongo.MongoDocumentNodeStoreBuilder;
import org.eea.document.service.DocumentService;
import org.eea.document.type.NodeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The type Document service.
 */
@Service("documentService")
public class DocumentServiceImpl implements DocumentService {

	private static final Logger LOG = LoggerFactory.getLogger(DocumentServiceImpl.class);
	private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

	@Override
	public void testLogging() throws Exception {
		Repository repository = JcrUtils.getRepository();
		Session session = repository.login(new GuestCredentials());
		try {
			String user = session.getUserID();
			String name = repository.getDescriptor(Repository.REP_NAME_DESC);
			String msg = String.format("Logged in as %s to a %s repository.", user, name);
			LOG.info(msg);
		} finally {
			session.logout();
		}

	}

	@Override
	@Transactional
	public void uploadDocument() throws Exception {
		Session session = null;
		try {
			LOG.info("Adding the file...");
			session = getSession();
			addFileNode(session, "/testNode", new File("file.txt"), "admin");

			LOG.info("Files added...");
		} catch (RepositoryException | IOException e) {
			LOG.error(e.getMessage());
		}finally {
			cleanUp(session); // do this in finally
		}
	}

	public static Repository getRepo(String host, final int port) throws UnknownHostException {
		String uri = "mongodb://" + host + ":" + port;
		System.out.println(uri);
		System.setProperty("oak.documentMK.disableLeaseCheck", "true");
		DocumentNodeStore ns = new MongoDocumentNodeStoreBuilder().setMongoDB(uri, "oak_demo", 16).build();
		Repository repo = new Jcr(new Oak(ns)).createRepository();
		System.out
				.println("oak.documentMK.disableLeaseCheck=" + System.getProperty("oak.documentMK.disableLeaseCheck"));
		return repo;
	}

	private Session getSession() throws LoginException, RepositoryException, UnknownHostException {
		Repository repo = getRepo("localhost", 27017);
		if (repo != null)
			return repo.login(new SimpleCredentials("admin", "admin".toCharArray()));
		else
			throw new NullPointerException("Repository not initialized");
	}

	public static void addFileNode(Session session, String absPath, File file, String userName)
			throws RepositoryException, IOException {

		Node node = createNodes(session, absPath);
		if (node.hasNode(file.getName())) {
			System.out.println("File already added.");
			return;
		}

		Node fileHolder = node.addNode(file.getName()); // Created a node with that of file Name
		fileHolder.addMixin("mix:versionable");
		fileHolder.setProperty("jcr:createdBy", userName);
		fileHolder.setProperty("jcr:nodeType", NodeType.FILE.getValue());
		fileHolder.setProperty("size", file.length());

		Node file1 = fileHolder.addNode("theFile", "nt:file"); // create node of type file.

		Date now = new Date();
		now.toInstant().toString();

		Node content = file1.addNode("jcr:content", "nt:resource");
		String contentType = Files.probeContentType(file.toPath());
		content.setProperty("jcr:mimeType", contentType);
		FileInputStream is = new FileInputStream(file);
		Binary binary = session.getValueFactory().createBinary(is);

		content.setProperty("jcr:data", binary);
		content.setProperty("jcr:lastModified", now.toInstant().toString());
		session.save();
		VersionManager vm = session.getWorkspace().getVersionManager();
		vm.checkin(fileHolder.getPath());
		is.close();

		System.out.println("File Saved...");
	}

	public static Node createNodes(Session session, String absPath) throws RepositoryException {
		if (session.itemExists(absPath)) {
			System.out.println("Nodes already exist!!!");
			return session.getNode(absPath);
		}
		String[] nodeNames = (null != absPath) ? absPath.split("/") : null;
		Node node = createNodes(session, nodeNames);
		session.save();
		return node;
	}

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
	
	private void cleanUp(Session session) {
        if (session != null) {
            session.logout();
        }
}
}
