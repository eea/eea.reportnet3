package org.eea.dataflow.service.impl;

import java.util.ArrayList;
import java.util.List;
import javax.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.eea.dataflow.mapper.DataProviderMapper;
import org.eea.dataflow.mapper.RepresentativeMapper;
import org.eea.dataflow.persistence.domain.DataProvider;
import org.eea.dataflow.persistence.domain.DataProviderCode;
import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.dataflow.persistence.domain.Representative;
import org.eea.dataflow.persistence.repository.DataProviderRepository;
import org.eea.dataflow.persistence.repository.DataflowRepository;
import org.eea.dataflow.persistence.repository.RepresentativeRepository;
import org.eea.dataflow.service.RepresentativeService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.dataflow.DataProviderCodeVO;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** The Class RepresentativeServiceImpl. */
@Service("dataflowRepresentativeService")
public class RepresentativeServiceImpl implements RepresentativeService {

  /** The representative repository. */
  @Autowired
  private RepresentativeRepository representativeRepository;

  /** The data provider repository. */
  @Autowired
  private DataProviderRepository dataProviderRepository;

  /** The dataflow repository. */
  @Autowired
  private DataflowRepository dataflowRepository;

  /** The representative mapper. */
  @Autowired
  private RepresentativeMapper representativeMapper;

  /** The data provider mapper. */
  @Autowired
  private DataProviderMapper dataProviderMapper;

  /** The user management controller zull. */
  @Autowired
  private UserManagementControllerZull userManagementControllerZull;

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(RepresentativeServiceImpl.class);

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * Creates the representative.
   *
   * @param dataflowId the dataflow id
   * @param representativeVO the representative VO
   * @return the long
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public Long createRepresentative(Long dataflowId, RepresentativeVO representativeVO)
      throws EEAException {

    String email = representativeVO.getProviderAccount();
    Long dataProviderId = representativeVO.getDataProviderId();
    Dataflow dataflow = dataflowRepository.findById(dataflowId).orElse(null);

    if (dataflow == null) {
      throw new EEAException(EEAErrorMessage.DATAFLOW_NOTFOUND);
    }

    if (userManagementControllerZull.getUserByEmail(email) == null) {
      throw new EEAException(EEAErrorMessage.USER_REQUEST_NOTFOUND);
    }

    if (existsUserMail(dataProviderId, email, dataflowId)) {
      throw new EEAException(EEAErrorMessage.REPRESENTATIVE_DUPLICATED);
    }

    DataProvider dataProvider = new DataProvider();
    dataProvider.setId(dataProviderId);
    Representative representative = representativeMapper.classToEntity(representativeVO);
    representative.setDataflow(dataflow);
    representative.setDataProvider(dataProvider);
    representative.setReceiptDownloaded(false);
    representative.setReceiptOutdated(false);
    representative.setHasDatasets(false);

    LOG.info("Insert new representative relation to dataflow: {}", dataflowId);
    return representativeRepository.save(representative).getId();
  }

  /**
   * Delete dataflow representative.
   *
   * @param dataflowRepresentativeId the dataflow representative id
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void deleteDataflowRepresentative(Long dataflowRepresentativeId) throws EEAException {
    if (dataflowRepresentativeId == null) {
      throw new EEAException(EEAErrorMessage.DATAFLOW_NOTFOUND);
    }
    LOG.info("Deleting the representative relation");
    representativeRepository.deleteById(dataflowRepresentativeId);
  }

  /**
   * Update dataflow representative.
   *
   * @param representativeVO the representative VO
   * @return the long
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public Long updateDataflowRepresentative(RepresentativeVO representativeVO) throws EEAException {
    if (representativeVO == null) {
      throw new EEAException(EEAErrorMessage.DATAFLOW_NOTFOUND);
    }
    // load old relationship
    Representative representative =
        representativeRepository.findById(representativeVO.getId()).orElse(null);
    if (representative == null) {
      throw new EEAException(EEAErrorMessage.REPRESENTATIVE_NOT_FOUND);
    }
    if (existsUserMail(
        representativeVO.getDataProviderId() != null ? representativeVO.getDataProviderId()
            : representative.getDataProvider().getId(),
        representativeVO.getProviderAccount() != null ? representativeVO.getProviderAccount()
            : representative.getUserMail(),
        representative.getDataflow().getId())
        && !changesInReceiptStatus(representative, representativeVO)) {
      LOG_ERROR.error("Duplicated representative relationship");
      throw new EEAException(EEAErrorMessage.REPRESENTATIVE_DUPLICATED);
    } else {
      // update changes on first level
      if (representativeVO.getProviderAccount() != null) {
        representative.setUserMail(representativeVO.getProviderAccount());
      }
      if (representativeVO.getDataProviderId() != null) {
        DataProvider dataProvider = new DataProvider();
        dataProvider.setId(representativeVO.getDataProviderId());
        representative.setDataProvider(dataProvider);
      }
      if (representativeVO.getReceiptDownloaded() != null) {
        representative.setReceiptDownloaded(representativeVO.getReceiptDownloaded());
      }
      if (representativeVO.getReceiptOutdated() != null) {
        representative.setReceiptOutdated(representativeVO.getReceiptOutdated());
      }

      // save changes
      return representativeRepository.save(representative).getId();
    }
  }

  /**
   * Gets the all data provider types.
   *
   * @return the all data provider types
   */
  @Override
  public List<DataProviderCodeVO> getAllDataProviderTypes() {
    LOG.info("obtaining the distinct representative types");
    List<DataProviderCode> dataProviderCodes = dataProviderRepository.findDistinctCode();
    List<DataProviderCodeVO> dataProviderCodeVOs = new ArrayList<>();
    for (DataProviderCode dataProviderCode : dataProviderCodes) {
      DataProviderCodeVO item = new DataProviderCodeVO();
      item.setDataProviderGroupId(dataProviderCode.getDataProviderGroupId());
      item.setLabel(dataProviderCode.getLabel());
      dataProviderCodeVOs.add(item);
    }
    return dataProviderCodeVOs;
  }

  /**
   * Gets the represetatives by id data flow.
   *
   * @param dataflowId the dataflow id
   * @return the represetatives by id data flow
   * @throws EEAException the EEA exception
   */
  @Override
  public List<RepresentativeVO> getRepresetativesByIdDataFlow(Long dataflowId) throws EEAException {
    if (dataflowId == null) {
      throw new EEAException(EEAErrorMessage.DATAFLOW_NOTFOUND);
    }
    LOG.info("Obtaining the representatives for the dataflow : {}", dataflowId);
    return representativeMapper
        .entityListToClass(representativeRepository.findAllByDataflow_Id(dataflowId));
  }

  /**
   * Gets the all data provider by group id.
   *
   * @param groupId the group id
   * @return the all data provider by group id
   */
  @Override
  public List<DataProviderVO> getAllDataProviderByGroupId(Long groupId) {
    return dataProviderMapper.entityListToClass(dataProviderRepository.findAllByGroupId(groupId));
  }

  /**
   * Exists user mail.
   *
   * @param dataProviderId the data provider id
   * @param userMail the user mail
   * @param dataflowId the dataflow id
   * @return true, if successful
   * @throws EEAException the EEA exception
   */
  private boolean existsUserMail(Long dataProviderId, String userMail, Long dataflowId)
      throws EEAException {
    if (dataProviderId == null || StringUtils.isBlank(userMail)) {
      throw new EEAException(EEAErrorMessage.REPRESENTATIVE_NOT_FOUND);
    }
    return representativeRepository.findByDataProviderIdAndDataflowId(dataProviderId, dataflowId)
        .isPresent();
  }


  /**
   * Gets the data provider by id.
   *
   * @param dataProviderId the data provider id
   * @return the data provider by id
   */
  @Override
  public DataProviderVO getDataProviderById(Long dataProviderId) {
    DataProvider dataprovider =
        dataProviderRepository.findById(dataProviderId).orElse(new DataProvider());

    return dataProviderMapper.entityToClass(dataprovider);

  }

  /**
   * Find data providers by ids.
   *
   * @param dataProviderIds the data provider ids
   * @return the list
   */
  @Override
  public List<DataProviderVO> findDataProvidersByIds(List<Long> dataProviderIds) {
    List<DataProviderVO> list = new ArrayList<>();
    Iterable<DataProvider> dataProviders = dataProviderRepository.findAllById(dataProviderIds);
    dataProviders.forEach(dataProvider -> list.add(dataProviderMapper.entityToClass(dataProvider)));
    return list;
  }

  /**
   * Find data providers by ids.
   *
   * @param dataProviderIds the data provider ids
   * @return the list
   */
  @Override
  public List<DataProviderVO> findDataProvidersByCode(String code) {
    List<DataProviderVO> list = new ArrayList<>();
    List<DataProvider> dataProviders = dataProviderRepository.findByCode(code);
    dataProviders.forEach(dataProvider -> list.add(dataProviderMapper.entityToClass(dataProvider)));
    return list;
  }

  /**
   * Gets the represetatives by dataflow id and email.
   *
   * @param dataflowId the dataflow id
   * @param email the email
   * @return the represetatives by dataflow id and email
   */
  @Override
  public List<RepresentativeVO> getRepresetativesByDataflowIdAndEmail(Long dataflowId,
      String email) {
    return representativeMapper
        .entityListToClass(representativeRepository.findByDataflowIdAndEmail(dataflowId, email));
  }


  /**
   * Changes in receipt status.
   *
   * @param representative the representative
   * @param representativeVO the representative VO
   * @return true, if successful
   */
  private boolean changesInReceiptStatus(Representative representative,
      RepresentativeVO representativeVO) {

    Boolean changes = true;
    if (representative.getReceiptDownloaded().equals(representativeVO.getReceiptDownloaded())
        && representative.getReceiptOutdated().equals(representativeVO.getReceiptOutdated())) {
      changes = false;
    }
    return changes;
  }


}
