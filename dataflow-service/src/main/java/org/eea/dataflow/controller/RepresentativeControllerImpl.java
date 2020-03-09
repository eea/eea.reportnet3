package org.eea.dataflow.controller;

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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

/**
 * The Class RepresentativeControllerImpl.
 */
@RestController
@RequestMapping(value = "/representative")
public class RepresentativeControllerImpl implements RepresentativeController {

  /**
   * The representative service.
   */
  @Autowired
  private RepresentativeService representativeService;

  /**
   * The user management controller zull.
   */
  @Autowired
  private UserManagementControllerZull userManagementControllerZull;

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * Insert representative.
   *
   * @param dataflowId the dataflow id
   * @param representativeVO the representative VO
   *
   * @return the long
   */
  @Override
  @HystrixCommand
  @PostMapping(value = "/{dataflowId}")
  @PreAuthorize("hasRole('DATA_CUSTODIAN')")
  public ResponseEntity<?> insertRepresentative(@PathVariable("dataflowId") Long dataflowId,
      @RequestBody RepresentativeVO representativeVO) {
    List<UserRepresentationVO> users = userManagementControllerZull.getUsers();
    String message;
    HttpStatus status = HttpStatus.OK;
    UserRepresentationVO userRepresentationVO = users.stream()
        .filter(user -> representativeVO.getProviderAccount().equalsIgnoreCase(user.getEmail()))
        .findFirst().orElse(null);
    if (userRepresentationVO == null) {
      message = EEAErrorMessage.USER_REQUEST_NOTFOUND;
      status = HttpStatus.NOT_FOUND;
    } else {
      try {
        message = String
            .valueOf(representativeService.insertRepresentative(dataflowId, representativeVO));
      } catch (EEAException e) {
        if (EEAErrorMessage.REPRESENTATIVE_DUPLICATED.equals(e.getMessage())) {
          LOG_ERROR.error("Duplicated representative relationship", e.getCause());
          message = EEAErrorMessage.REPRESENTATIVE_DUPLICATED;
          status = HttpStatus.CONFLICT;
        } else {
          LOG_ERROR.error("Bad Request", e.getCause());
          message = EEAErrorMessage.REPRESENTATIVE_NOT_FOUND;
          status = HttpStatus.BAD_REQUEST;
        }
      }
    }
    return new ResponseEntity<>(message, status);
  }

  /**
   * Find all data provider by group id.
   *
   * @param groupId the group id
   *
   * @return the list
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/dataProvider/{groupId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('DATA_CUSTODIAN')")
  public List<DataProviderVO> findAllDataProviderByGroupId(@PathVariable("groupId") Long groupId) {
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
  @PreAuthorize("hasRole('DATA_CUSTODIAN')")
  public List<DataProviderCodeVO> findAllDataProviderTypes() {
    return representativeService.getAllDataProviderTypes();
  }



  /**
   * Find representatives by id data flow.
   *
   * @param dataflowId the dataflow id
   * @return the list
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/dataflow/{dataflowId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('DATA_CUSTODIAN')")
  public List<RepresentativeVO> findRepresentativesByIdDataFlow(
      @PathVariable("dataflowId") Long dataflowId) {
    if (dataflowId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.DATAFLOW_NOTFOUND);
    }
    List<RepresentativeVO> representativeVOs;
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
   * @return the response entity
   */
  @Override
  @HystrixCommand
  @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('DATA_CUSTODIAN') OR (hasRole('DATA_PROVIDER')")
  public ResponseEntity<?> updateRepresentative(@RequestBody RepresentativeVO representativeVO) {
    String message = null;
    HttpStatus status = HttpStatus.OK;
    if (representativeVO.getProviderAccount() != null) {
      List<UserRepresentationVO> users = userManagementControllerZull.getUsers();
      UserRepresentationVO userRepresentationVO = users.stream()
          .filter(user -> representativeVO.getProviderAccount().equalsIgnoreCase(user.getEmail()))
          .findFirst().orElse(null);
      if (userRepresentationVO == null) {
        message = EEAErrorMessage.USER_REQUEST_NOTFOUND;
        status = HttpStatus.NOT_FOUND;
      }
    }
    try {
      message = message == null
          ? String.valueOf(representativeService.updateDataflowRepresentative(representativeVO))
          : message;
    } catch (EEAException e) {
      if (EEAErrorMessage.REPRESENTATIVE_DUPLICATED.equals(e.getMessage())) {
        LOG_ERROR.error("Duplicated representative relationship", e.getCause());
        message = EEAErrorMessage.REPRESENTATIVE_DUPLICATED;
        status = HttpStatus.CONFLICT;
      } else {
        LOG_ERROR.error("Bad Request", e.getCause());
        message = EEAErrorMessage.REPRESENTATIVE_NOT_FOUND;
        status = HttpStatus.BAD_REQUEST;
      }
    }
    return new ResponseEntity<>(message, status);
  }

  /**
   * Delete representative.
   *
   * @param dataflowRepresentativeId the dataflow representative id
   */
  @Override
  @HystrixCommand
  @DeleteMapping(value = "/{dataflowRepresentativeId}")
  @PreAuthorize("hasRole('DATA_CUSTODIAN')")
  public void deleteRepresentative(
      @PathVariable("dataflowRepresentativeId") Long dataflowRepresentativeId) {
    try {
      representativeService.deleteDataflowRepresentative(dataflowRepresentativeId);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND,
          EEAErrorMessage.REPRESENTATIVE_NOT_FOUND, e);
    }
  }


  /**
   * Find data provider by id.
   *
   * @param dataProviderId the data provider id
   * @return the data provider VO
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/dataProvider/id/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('DATA_CUSTODIAN')")
  public DataProviderVO findDataProviderById(@PathVariable("id") Long dataProviderId) {
    if (null == dataProviderId) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND,
          EEAErrorMessage.REPRESENTATIVE_NOT_FOUND);
    }
    return representativeService.getDataProviderById(dataProviderId);
  }

  /**
   * Find data providers by ids.
   *
   * @param dataProviderIds the data provider ids
   * @return the list
   */
  @Override
  @GetMapping("/private/dataProvider")
  @PreAuthorize("hasRole('DATA_CUSTODIAN')")
  public List<DataProviderVO> findDataProvidersByIds(
      @RequestParam("id") List<Long> dataProviderIds) {
    return representativeService.findDataProvidersByIds(dataProviderIds);
  }
}
