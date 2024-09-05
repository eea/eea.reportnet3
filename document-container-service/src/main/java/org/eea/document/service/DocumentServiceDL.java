package org.eea.document.service;

import org.eea.document.type.FileResponse;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.document.DocumentVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

public interface DocumentServiceDL {
    /**
     * Upload a document in s3.
     *
     * @param multipartFile the file
     * @param fileName the file name
     * @param documentVO the document VO
     * @param size the size
     */
    void uploadDocumentDL(MultipartFile multipartFile, final String fileName, DocumentVO documentVO, final Long size) throws EEAException, IOException;


    /**
     * Gets the document.
     *
     * @param document document
     * @return the document in bytes
     */
    FileResponse getDocumentDL(DocumentVO document) throws EEAException;

    /**
     * Delete document.
     *
     * @param documentVO the document
     * @param deleteMetabase the delete metabase
     * @throws EEAException the EEA exception
     */
    void deleteDocumentDL(DocumentVO documentVO, Boolean deleteMetabase) throws EEAException;


    /**
     * Upload collaboration document in s3.
     *
     * @param inputStream the input stream
     * @param filename the filename
     * @param dataflowId the dataflow id
     * @param providerId the provider id
     * @param messageId the message id
     * @throws EEAException the EEA exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    void uploadCollaborationDocumentDL(InputStream inputStream, String filename,
                                       Long dataflowId, Long providerId, Long messageId) throws IOException;


    /**
     * Delete collaboration document from s3.
     *
     * @param documentName the document name
     * @param dataflowId the dataflow id
     * @param providerId the provider id
     * @param messageId the message id
     */
    void deleteCollaborationDocumentDL(String documentName, Long dataflowId, Long providerId, Long messageId);


    /**
     * Gets the collaboration document from s3.
     *
     * @param documentName the document name
     * @param dataflowId the dataflow id
     * @param providerId the provider id
     * @param messageId the message id
     */
    FileResponse getCollaborationDocumentDL(final String documentName, final Long dataflowId, Long providerId, Long messageId);

}
