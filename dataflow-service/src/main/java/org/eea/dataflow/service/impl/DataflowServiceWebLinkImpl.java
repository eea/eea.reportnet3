package org.eea.dataflow.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eea.dataflow.exception.EntityNotFoundException;
import org.eea.dataflow.exception.ResourceNoFoundException;
import org.eea.dataflow.exception.WrongDataExceptions;
import org.eea.dataflow.mapper.DataflowWebLinkMapper;
import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.dataflow.persistence.domain.Weblink;
import org.eea.dataflow.persistence.repository.DataflowRepository;
import org.eea.dataflow.persistence.repository.WebLinkRepository;
import org.eea.dataflow.service.DataflowWebLinkService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.ums.UserManagementController;
import org.eea.interfaces.vo.ums.ResourceAccessVO;
import org.eea.interfaces.vo.ums.enums.ResourceTypeEnum;
import org.eea.interfaces.vo.ums.enums.SecurityRoleEnum;
import org.eea.interfaces.vo.weblink.WeblinkVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;


/**
 * The Class DataflowServiceWebLinkImpl.
 */
@Service
public class DataflowServiceWebLinkImpl implements DataflowWebLinkService {


  /** The dataflow repository. */
  @Autowired
  private DataflowRepository dataflowRepository;

  /** The dataflow web link mapper. */
  @Autowired
  private DataflowWebLinkMapper dataflowWebLinkMapper;

  /** The web link repository. */
  @Autowired
  private WebLinkRepository webLinkRepository;

  /** The user management controller zull. */
  @Autowired
  private UserManagementController userManagementControllerZull;

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DataflowServiceImpl.class);

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant REGEX_URL. */
  private static final String REGEX_URL =
      "^(sftp:\\/\\/www\\.|sftp:\\/\\/|ftp:\\/\\/www\\.|ftp:\\/\\/|http:\\/\\/www\\.|https:\\/\\/www\\.|http:\\/\\/|https:\\/\\/)?[a-z0-9]+([\\-\\.]{1}[a-z0-9]+)*\\.[a-z]{2,63}(:[0-9]{1,5})?(\\/.*)?$";

  /**
   * Gets the web link.
   *
   * @param idLink the id link
   * @return the web link
   * @throws EEAException the EEA exception
   */
  @Override
  public WeblinkVO getWebLink(Long idLink) throws EEAException {

    Dataflow dataFlow = dataflowRepository.findDataflowByWeblinks_Id(idLink);
    if (null != dataFlow) {
      new EntityNotFoundException(EEAErrorMessage.DATAFLOW_NOTFOUND);
    }
    Long dataFlowId = dataFlow.getId();

    List<ResourceAccessVO> resources = userManagementControllerZull
        .getResourcesByUser(ResourceTypeEnum.DATAFLOW, SecurityRoleEnum.DATA_CUSTODIAN);

    // get idDataflow
    resources.stream().filter(resourceAccessVO -> {
      return resourceAccessVO.getId().equals(dataFlowId);
    }).findFirst().orElseThrow(() -> new ResourceNoFoundException(EEAErrorMessage.FORBIDDEN));

    Optional<Weblink> idLinkData = webLinkRepository.findById(idLink);
    LOG.info("get the links with id : {}", idLink);
    WeblinkVO weblinkVO = null;
    if (idLinkData.isPresent()) {
      weblinkVO = dataflowWebLinkMapper.entityToClass(idLinkData.get());
    } else {
      throw new EntityNotFoundException(EEAErrorMessage.ID_LINK_INCORRECT);
    }
    return weblinkVO;
  }

  /**
   * Save web link.
   *
   * @param idDataflow the id dataflow
   * @param weblinkVO the weblink VO
   * @throws EEAException the EEA exception
   */
  @Override
  public void saveWebLink(Long idDataflow, WeblinkVO weblinkVO) throws EEAException {

    Weblink weblink = dataflowWebLinkMapper.classToEntity(weblinkVO);

    Pattern patN = Pattern.compile(REGEX_URL);

    Matcher matN = patN.matcher(weblink.getUrl());

    if (!matN.find()) {
      throw new WrongDataExceptions(EEAErrorMessage.URL_FORMAT_INCORRECT);
    }

    Optional<Dataflow> dataflow = dataflowRepository.findById(idDataflow);
    if (!dataflow.isPresent()) {
      throw new EntityNotFoundException(EEAErrorMessage.DATAFLOW_NOTFOUND);
    }
    weblink.setDataflow(dataflow.get());
    webLinkRepository.save(weblink);
    LOG.info("Save the link: {}, with description: {} , in {}", weblink.getUrl(),
        weblink.getDescription(), dataflow.get().getName());
  }


  /**
   * Removes the web link.
   *
   * @param webLinkId the web link id
   * @throws EEAException the EEA exception
   */
  @Override
  public void removeWebLink(Long webLinkId) throws EEAException {

    Dataflow dataFlow = dataflowRepository.findDataflowByWeblinks_Id(webLinkId);
    if (null != dataFlow) {
      new EntityNotFoundException(EEAErrorMessage.DATAFLOW_NOTFOUND);
    }
    Long dataFlowId = dataFlow.getId();

    List<ResourceAccessVO> resources = userManagementControllerZull
        .getResourcesByUser(ResourceTypeEnum.DATAFLOW, SecurityRoleEnum.DATA_CUSTODIAN);

    // get idDataflow
    resources.stream().filter(resourceAccessVO -> {
      return resourceAccessVO.getId().equals(dataFlowId);
    }).findFirst().orElseThrow(() -> new ResourceNoFoundException(EEAErrorMessage.FORBIDDEN));

    try {
      webLinkRepository.deleteById(webLinkId);
    } catch (EmptyResultDataAccessException e) {
      LOG_ERROR.error("link with id: {}", webLinkId);
      throw new EntityNotFoundException(EEAErrorMessage.ID_LINK_INCORRECT, e);
    }
    LOG.info("delete the link with id : {}", webLinkId);
  }

  /**
   * Update web link.
   *
   * @param weblinkVO the weblink VO
   * @throws EEAException the EEA exception
   */
  @Override
  public void updateWebLink(WeblinkVO weblinkVO) throws EEAException {

    Weblink weblink = dataflowWebLinkMapper.classToEntity(weblinkVO);

    Dataflow dataFlow = dataflowRepository.findDataflowByWeblinks_Id(weblink.getId());
    if (null != dataFlow) {
      new EntityNotFoundException(EEAErrorMessage.DATAFLOW_NOTFOUND);
    }
    Long dataFlowId = dataFlow.getId();

    List<ResourceAccessVO> resources = userManagementControllerZull
        .getResourcesByUser(ResourceTypeEnum.DATAFLOW, SecurityRoleEnum.DATA_CUSTODIAN);

    Pattern urlPattern = Pattern.compile(REGEX_URL);

    Matcher urlMatcher = urlPattern.matcher(weblink.getUrl());

    if (!urlMatcher.find()) {
      throw new WrongDataExceptions(EEAErrorMessage.URL_FORMAT_INCORRECT);
    }
    // get idDataflow
    resources.stream().filter(resourceAccessVO -> {
      return resourceAccessVO.getId().equals(dataFlowId);
    }).findFirst().orElseThrow(() -> new ResourceNoFoundException(EEAErrorMessage.FORBIDDEN));

    Optional<Weblink> webLinkFound = webLinkRepository.findById(weblink.getId());
    if (!webLinkFound.isPresent()) {
      throw new EntityNotFoundException(EEAErrorMessage.ID_LINK_NOT_FOUND);
    }
    webLinkFound.get().setDescription(weblink.getDescription());
    webLinkFound.get().setUrl(weblink.getUrl());
    webLinkRepository.save(webLinkFound.get());
    LOG.info("Save the link with id : {}", weblink.getId());

  }

}

