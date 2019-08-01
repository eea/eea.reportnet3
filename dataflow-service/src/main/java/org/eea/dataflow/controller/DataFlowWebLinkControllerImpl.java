package org.eea.dataflow.controller;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import org.eea.dataflow.service.DataflowWebLinkService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowWebLinkController;
import org.eea.interfaces.vo.weblink.WeblinkVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * The Class DataFlowWebLinkControllerImpl.
 */
@RestController
@RequestMapping(value = "/webLink")
public class DataFlowWebLinkControllerImpl implements DataFlowWebLinkController {


  /** The dataflow web link service. */
  @Autowired
  private DataflowWebLinkService dataflowWebLinkService;

  /**
   * Save link.
   *
   * @param idDataflow the id dataflow
   * @return the link
   */
  @Override
  @GetMapping(value = "{idLink}")
  public WeblinkVO getLink(@RequestParam("idLink") Long idLink) {
    try {
      return dataflowWebLinkService.getWebLink(idLink);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.USER_REQUEST_NOTFOUND);
    }
  }

  /**
   * Save link.
   *
   * @param idDataflow the id dataflow
   * @param url the url
   * @param description the description
   */
  @Override
  @PutMapping
  public void saveLink(Long idDataflow, String url, String description) {


    try {
      new URL(url).toURI();
    } catch (MalformedURLException exception) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.URL_FORMAT_INCORRECT);
    } catch (URISyntaxException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.URL_FORMAT_INCORRECT);
    }

    try {
      dataflowWebLinkService.saveWebLink(idDataflow, url, description);
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
  @DeleteMapping(value = "{idLink}")
  public void removeLink(@RequestParam(value = "idLink") Long idLink) {
    try {
      dataflowWebLinkService.removeWebLink(idLink);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.USER_REQUEST_NOTFOUND);
    }
  }

  /**
   * Update link.
   *
   * @param idLink the id link
   * @param url the url
   * @param description the description
   */
  @Override
  @PostMapping(value = "{idLink}")
  public void updateLink(@RequestParam(value = "idLink") Long idLink,
      @RequestParam(value = "url") String url,
      @RequestParam(value = "description") String description) {
    try {
      new URL(url).toURI();
    } catch (MalformedURLException exception) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.URL_FORMAT_INCORRECT);
    } catch (URISyntaxException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.URL_FORMAT_INCORRECT);
    }

    try {
      dataflowWebLinkService.updateWebLink(idLink, description, url);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.USER_REQUEST_NOTFOUND);
    }
  }

}
