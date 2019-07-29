package org.eea.dataflow.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import org.eea.dataflow.mapper.DataflowWebLinkMapper;
import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.dataflow.persistence.domain.Weblink;
import org.eea.dataflow.persistence.repository.DataflowRepository;
import org.eea.dataflow.persistence.repository.WebLinkRepository;
import org.eea.dataflow.service.DataflowWebLinkService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.weblink.WeblinkVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The Class DataflowServiceImpl.
 */
@Service("WebLinkService")
public class DataflowServiceWebLinkImpl implements DataflowWebLinkService {


  /** The dataflow repository. */
  @Autowired
  private DataflowRepository dataflowRepository;

  @Autowired
  private DataflowWebLinkMapper dataflowWebLinkMapper;

  /** The web link repository. */
  @Autowired
  private WebLinkRepository webLinkRepository;


  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DataflowServiceImpl.class);


  @Override
  public List<WeblinkVO> getWebLink(Long idDataflow) {
    List<Weblink> listWebLinks = webLinkRepository.findAllByDataflow_Id(idDataflow);
    LOG.info("get the links with dataflowId : {}", idDataflow);

    List<WeblinkVO> weblinkVOList = new ArrayList<>();

    if (null != listWebLinks && !listWebLinks.isEmpty()) {
      for (Weblink weblink : listWebLinks) {
        weblinkVOList.add(dataflowWebLinkMapper.entityToClass(weblink));
      }
    }
    return weblinkVOList;
  }

  /**
   * Save web link.
   *
   * @param idDataflow the id dataflow
   * @param url the url
   * @param description the description
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void saveWebLink(Long idDataflow, String url, String description) throws EEAException {

    Weblink webLinkObject = new Weblink();
    webLinkObject.setDescription(description);
    webLinkObject.setUrl(url);
    Optional<Dataflow> dataflow = dataflowRepository.findById(idDataflow);
    if (!dataflow.isPresent()) {
      throw new EEAException(EEAErrorMessage.DATAFLOW_INCORRECT_ID);
    }
    webLinkObject.setDataflow(dataflow.get());
    webLinkRepository.save(webLinkObject);
    LOG.info("Save the link: {}, with description: {} , in {}", url, description,
        dataflow.get().getName());
  }

  /**
   * Removes the web link.
   *
   * @param webLink the web link
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void removeWebLink(Long webLink) throws EEAException {

    Optional<Weblink> webLinkData = webLinkRepository.findById(webLink);
    if (!webLinkData.isPresent()) {
      throw new EEAException(EEAErrorMessage.ID_LINK_INCORRECT);
    }
    webLinkRepository.delete(webLinkData.get());
    LOG.info("delete the link with id : {}", webLink);
  }

  /**
   * Update web link.
   *
   * @param webLinkId the web link id
   * @param description the description
   * @param url the url
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void updateWebLink(Long webLinkId, String description, String url) throws EEAException {

    Optional<Weblink> webLink = webLinkRepository.findById(webLinkId);
    if (!webLink.isPresent()) {
      throw new EEAException(EEAErrorMessage.ID_LINK_INCORRECT);
    }
    webLink.get().setDescription(description);
    webLink.get().setUrl(url);
    webLinkRepository.save(webLink.get());
    LOG.info("Save the link with id : {}", webLinkId);

  }

}

