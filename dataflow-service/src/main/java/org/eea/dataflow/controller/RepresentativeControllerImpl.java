package org.eea.dataflow.controller;

import java.util.ArrayList;
import java.util.List;
import org.eea.dataflow.service.RepresentativeService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.RepresentativeController;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.dataflow.DataProviderCodeVO;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.ums.UserRepresentationVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

/**
 * The Class RepresentativeControllerImpl.
 */
@RestController
@RequestMapping(value = "/representative")
public class RepresentativeControllerImpl implements RepresentativeController {

  /** The representative service. */
  @Autowired
  private RepresentativeService representativeService;

  /** The user management controller zull. */
  @Autowired
  private UserManagementControllerZull userManagementControllerZull;

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(RepresentativeControllerImpl.class);

  /**
   * Insert representative.
   *
   * @param dataflowId the dataflow id
   * @param representativeVO the representative VO
   * @return the long
   */
  @Override
  @HystrixCommand
  @PostMapping(value = "/{dataflowId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Long insertRepresentative(Long dataflowId, RepresentativeVO representativeVO) {
    List<UserRepresentationVO> users = userManagementControllerZull.getUsers();
    UserRepresentationVO userRepresentationVO =
        users.stream().filter(user -> representativeVO.getProviderAccount().equals(user.getEmail()))
            .findFirst().orElse(null);
    if (userRepresentationVO == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND,
          EEAErrorMessage.USER_REQUEST_NOTFOUND);
    }
    try {
      return representativeService.insertRepresentative(dataflowId, representativeVO);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.REPRESENTATIVE_NOT_FOUND, e);
    }
  }

  /**
   * Find all data provider by group id.
   *
   * @param groupId the group id
   * @return the list
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/dataProvider/{groupId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<DataProviderVO> findAllDataProviderByGroupId(Long groupId) {
    if (null == groupId) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.REPRESENTATIVE_TYPE_INCORRECT);
    }
    return representativeService.getAllDataProviderByGroupId(groupId);
  }

  /**
   * Find all data provider types.
   *
   * @return the list
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/dataProvider/types", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<DataProviderCodeVO> findAllDataProviderTypes() {
    return representativeService.getAllDataProviderTypes();
  }

  /**
   * Find represetatives by id data flow.
   *
   * @param dataflowId the dataflow id
   * @return the list
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/dataflow/{dataflowId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<RepresentativeVO> findRepresetativesByIdDataFlow(Long dataflowId) {
    if (dataflowId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.DATAFLOW_NOTFOUND);
    }
    List<RepresentativeVO> representativeVOs = new ArrayList<>();
    try {
      representativeVOs = representativeService.getRepresetativesByIdDataFlow(dataflowId);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND,
          EEAErrorMessage.REPRESENTATIVE_NOT_FOUND, e);
    }
    return representativeVOs;
  }

  /**
   * Update representative.
   *
   * @param representativeVO the representative VO
   */
  @Override
  @HystrixCommand
  @PutMapping(value = "/update")
  public void updateRepresentative(RepresentativeVO representativeVO) {
    if (representativeVO.getProviderAccount() != null) {
      List<UserRepresentationVO> users = userManagementControllerZull.getUsers();
      UserRepresentationVO userRepresentationVO = users.stream()
          .filter(user -> representativeVO.getProviderAccount().equals(user.getEmail())).findFirst()
          .orElse(null);
      if (userRepresentationVO == null) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND,
            EEAErrorMessage.USER_REQUEST_NOTFOUND);
      }
    }
    try {
      representativeService.updateDataflowRepresentative(representativeVO);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.REPRESENTATIVE_NOT_FOUND, e);
    }
  }

  /**
   * Delete representative.
   *
   * @param dataflowRepresentativeId the dataflow representative id
   */
  @Override
  @HystrixCommand
  @DeleteMapping(value = "/{dataflowRepresentativeId}")
  public void deleteRepresentative(Long dataflowRepresentativeId) {
    try {
      representativeService.deleteDataflowRepresentative(dataflowRepresentativeId);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND,
          EEAErrorMessage.REPRESENTATIVE_NOT_FOUND, e);
    }
  }
}
