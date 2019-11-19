package org.eea.dataflow.controller;

import org.eea.dataflow.mapper.DataflowWebLinkMapper;
import org.eea.dataflow.persistence.repository.DataflowRepository;
import org.eea.dataflow.service.DataflowWebLinkService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowWebLinkController;
import org.eea.interfaces.controller.ums.UserManagementController;
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
   * @throws EEAException
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "{idLink}")
  public WeblinkVO getLink(@RequestParam("idLink") Long idLink) {

    WeblinkVO weblink = new WeblinkVO();

    try {
      weblink = dataflowWebLinkService.getWebLink(idLink);
    } catch (EEAException e) {
      e.printStackTrace();
    }

    return weblink;

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
  public void saveLink(@RequestParam(value = "dataflowId") Long dataflowId, WeblinkVO weblinkVO) {

    try {
      dataflowWebLinkService.saveWebLink(dataflowId, weblinkVO);
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

    try {
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
    try {
      dataflowWebLinkService.updateWebLink(weblinkVO);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.USER_REQUEST_NOTFOUND);
    }
  }

}
