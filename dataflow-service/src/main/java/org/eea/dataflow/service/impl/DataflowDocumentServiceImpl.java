package org.eea.dataflow.service.impl;

import javax.transaction.Transactional;
import org.eea.dataflow.mapper.DocumentMapper;
import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.dataflow.persistence.domain.Document;
import org.eea.dataflow.persistence.repository.DataflowRepository;
import org.eea.dataflow.persistence.repository.DocumentRepository;
import org.eea.dataflow.service.DataflowDocumentService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.document.DocumentVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The Class DataflowServiceImpl.
 */
@Service("dataflowDocumentService")
public class DataflowDocumentServiceImpl implements DataflowDocumentService {

  /** The dataflow repository. */
  @Autowired
  private DataflowRepository dataflowRepository;

  /** The document repository. */
  @Autowired
  private DocumentRepository documentRepository;

  /** The document mapper. */
  @Autowired
  private DocumentMapper documentMapper;

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DataflowDocumentServiceImpl.class);

  /**
   * Insert document.
   *
   * @param dataflowId the dataflow id
   * @param filename the filename
   * @param language the language
   * @param description the description
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void insertDocument(Long dataflowId, String filename, String language, String description)
      throws EEAException {
    if (dataflowId == null || filename == null || language == null || description == null) {
      throw new EEAException(EEAErrorMessage.DATAFLOW_NOTFOUND);
    }
    Dataflow dataflow = dataflowRepository.findById(dataflowId).orElse(null);
    if (dataflow != null) {
      Document document = new Document();
      document.setDescription(description);
      document.setLanguage(language);
      document.setName(filename);
      document.setDataflow(dataflow);
      documentRepository.save(document);
      LOG.info("document saved");
    } else {
      throw new EEAException(EEAErrorMessage.DATAFLOW_NOTFOUND);
    }
  }


  /**
   * Delete document.
   *
   * @param documentId the document id
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void deleteDocument(Long documentId) throws EEAException {
    if (documentId == null) {
      throw new EEAException(EEAErrorMessage.DATAFLOW_NOTFOUND);
    }
    documentRepository.deleteById(documentId);
    LOG.info("document deleted");
  }

  /**
   * Gets the document by id.
   *
   * @param documentId the document id
   * @return the document by id
   * @throws EEAException the EEA exception
   */
  @Override
  public DocumentVO getDocumentInfoById(Long documentId) throws EEAException {
    if (documentId == null) {
      throw new EEAException(EEAErrorMessage.DATAFLOW_NOTFOUND);
    }
    Document document = documentRepository.findById(documentId).orElse(null);
    if (document == null) {
      throw new EEAException(EEAErrorMessage.DOCUMENT_NOT_FOUND);
    }
    return documentMapper.entityToClass(document);
  }

}
