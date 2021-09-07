package org.eea.dataflow.service.impl;

import java.util.ArrayList;
import java.util.List;
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
   * @param documentVO the document VO
   * @return the long
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public Long insertDocument(DocumentVO documentVO) throws EEAException {
    LOG.info("inserting document in metabase");
    if (documentVO == null) {
      throw new EEAException(EEAErrorMessage.DATAFLOW_NOTFOUND);
    }
    Dataflow dataflow = dataflowRepository.findById(documentVO.getDataflowId()).orElse(null);
    if (dataflow != null) {
      Document document = documentMapper.classToEntity(documentVO);
      document.setName(documentVO.getName());
      document.setDataflow(dataflow);
      document = documentRepository.save(document);
      LOG.info("document saved");
      return document.getId();
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


  /**
   * Update document.
   *
   * @param documentVO the document VO
   * @throws EEAException the EEA exception
   */
  @Override
  public void updateDocument(DocumentVO documentVO) throws EEAException {
    LOG.info("updating document in metabase");
    Document document = documentRepository.findById(documentVO.getId()).orElse(null);
    if (document == null) {
      throw new EEAException(EEAErrorMessage.DOCUMENT_NOT_FOUND);
    }
    Document documentNew = documentMapper.classToEntity(documentVO);
    documentNew.setDataflow(document.getDataflow());
    documentNew.setDate(document.getDate());
    documentRepository.save(documentNew);
  }

  /**
   * Gets the all documents by dataflow id.
   *
   * @param dataflowId the dataflow id
   * @return the all documents by dataflow id
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public List<DocumentVO> getAllDocumentsByDataflowId(Long dataflowId) throws EEAException {
    List<DocumentVO> documents = new ArrayList<>();
    if (null == dataflowId) {
      throw new EEAException(EEAErrorMessage.DATAFLOW_NOTFOUND);
    } else {
      Dataflow dataflow = dataflowRepository.findById(dataflowId).orElse(null);
      if (dataflow != null && dataflow.getDocuments() != null) {
        dataflow.getDocuments().stream().forEach(document -> {
          documents.add(documentMapper.entityToClass(document));
        });
      }
    }
    return documents;
  }

}
