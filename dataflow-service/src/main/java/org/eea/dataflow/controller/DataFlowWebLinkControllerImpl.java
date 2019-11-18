package org.eea.dataflow.controller;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eea.dataflow.mapper.DataflowWebLinkMapper;
import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.dataflow.persistence.domain.Weblink;
import org.eea.dataflow.persistence.repository.DataflowRepository;
import org.eea.dataflow.service.DataflowWebLinkService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowWebLinkController;
import org.eea.interfaces.controller.ums.UserManagementController;
import org.eea.interfaces.vo.ums.ResourceAccessVO;
import org.eea.interfaces.vo.ums.enums.ResourceTypeEnum;
import org.eea.interfaces.vo.ums.enums.SecurityRoleEnum;
import org.eea.interfaces.vo.weblink.WeblinkVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;


/**
 * The Class DataFlowWebLinkControllerImpl.
 */
@RestController
@RequestMapping(value = "/weblink")
public class DataFlowWebLinkControllerImpl implements DataFlowWebLinkController {


  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");


  private static final String REGEX_URL =
      "^(sftp:\\/\\/www\\.|sftp:\\/\\/|ftp:\\/\\/www\\.|ftp:\\/\\/|http:\\/\\/www\\.|https:\\/\\/www\\.|http:\\/\\/|https:\\/\\/)?[a-z0-9]+([\\-\\.]{1}[a-z0-9]+)*\\.[a-z]{2,63}(:[0-9]{1,5})?(\\/.*)?$";
  /**
   * The dataflow web link service.
   */
  @Autowired
  private DataflowWebLinkService dataflowWebLinkService;

  @Autowired
  private DataflowWebLinkMapper dataflowWebLinkMapper;

  @Autowired
  private UserManagementController userManagementControllerZull;

  @Autowired
  private DataflowRepository dataflowRepository;

  /**
   * Gets the link.
   *
   * @param idLink the id link
   *
   * @return the link
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "{idLink}")
  public WeblinkVO getLink(@RequestParam("idLink") Long idLink) {

    Dataflow dataFlow = dataflowRepository.findDataflowByWeblinks_Id(idLink);
    Long dataFlowId = dataFlow.getId();

    List<ResourceAccessVO> resources =
        userManagementControllerZull.getResourcesByUser(ResourceTypeEnum.DATAFLOW);

    try {
      // get idDataflow
      resources.stream().filter(resourceAccessVO -> {
        return resourceAccessVO.getId() == dataFlowId
            && SecurityRoleEnum.DATA_CUSTODIAN.equals(resourceAccessVO.getRole());
      }).findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));

      return dataflowWebLinkService.getWebLink(idLink);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.USER_REQUEST_NOTFOUND);
    }
  }

  /**
   * Save link.
   *
   * @param dataflowId the dataflow id
   * @param weblinkVO the weblink VO
   */
  @Override
  @HystrixCommand
  @PostMapping
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_CUSTODIAN')")
  public void saveLink(Long dataflowId, WeblinkVO weblinkVO) {


    Weblink weblink = dataflowWebLinkMapper.classToEntity(weblinkVO);

    Pattern patN = Pattern.compile(REGEX_URL);

    Matcher matN = patN.matcher(weblink.getUrl());

    if (!matN.find()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.URL_FORMAT_INCORRECT);
    }

    try {
      dataflowWebLinkService.saveWebLink(dataflowId, weblink.getUrl(), weblink.getDescription());
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.USER_REQUEST_NOTFOUND);
    }

  }

  /**
   * Removes the link.
   *
   * @param idLink the id link
   */
  @Override
  @HystrixCommand
  @DeleteMapping(value = "{idLink}")
  public void removeLink(@RequestParam(value = "idLink") Long idLink) {

    Dataflow dataFlow = dataflowRepository.findDataflowByWeblinks_Id(idLink);
    Long dataFlowId = dataFlow.getId();

    List<ResourceAccessVO> resources =
        userManagementControllerZull.getResourcesByUser(ResourceTypeEnum.DATAFLOW);

    try {
      // get idDataflow
      resources.stream().filter(resourceAccessVO -> {
        return resourceAccessVO.getId() == dataFlowId
            && SecurityRoleEnum.DATA_CUSTODIAN.equals(resourceAccessVO.getRole());
      }).findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));

      dataflowWebLinkService.removeWebLink(idLink);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.USER_REQUEST_NOTFOUND, e);
    }
  }

  /**
   * Update link.
   *
   * @param weblinkVO the weblink VO
   */
  @Override
  @HystrixCommand
  @PutMapping
  public void updateLink(WeblinkVO weblinkVO) {

    Weblink weblink = dataflowWebLinkMapper.classToEntity(weblinkVO);

    Dataflow dataFlow = dataflowRepository.findDataflowByWeblinks_Id(weblink.getId());
    Long dataFlowId = dataFlow.getId();

    List<ResourceAccessVO> resources =
        userManagementControllerZull.getResourcesByUser(ResourceTypeEnum.DATAFLOW);


    Pattern patN = Pattern.compile(REGEX_URL);

    Matcher matN = patN.matcher(weblink.getUrl());

    if (!matN.find()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.URL_FORMAT_INCORRECT);
    }

    try {
      // get idDataflow
      resources.stream().filter(resourceAccessVO -> {
        return resourceAccessVO.getId() == dataFlowId
            && SecurityRoleEnum.DATA_CUSTODIAN.equals(resourceAccessVO.getRole());
      }).findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));

      dataflowWebLinkService.updateWebLink(weblink.getId(), weblink.getDescription(),
          weblink.getUrl());
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.USER_REQUEST_NOTFOUND);
    }
  }

}
