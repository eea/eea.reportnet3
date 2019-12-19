package org.eea.dataflow.service.impl;

import java.util.List;
import javax.transaction.Transactional;
import org.eea.dataflow.mapper.DataProviderMapper;
import org.eea.dataflow.mapper.RepresentativeMapper;
import org.eea.dataflow.persistence.domain.DataProvider;
import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.dataflow.persistence.domain.Representative;
import org.eea.dataflow.persistence.repository.DataProviderRepository;
import org.eea.dataflow.persistence.repository.RepresentativeRepository;
import org.eea.dataflow.service.RepresentativeService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataflow.DataProviderCodeVO;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The Class DataflowServiceImpl.
 */
@Service("dataflowRepresentativeService")
public class RepresentativeServiceImpl implements RepresentativeService {

  /** The dataflow representative repository. */
  @Autowired
  private RepresentativeRepository representativeRepository;

  /** The representative repository. */
  @Autowired
  private DataProviderRepository dataProviderRepository;

  /** The dataflow representative mapper. */
  @Autowired
  private RepresentativeMapper representativeMapper;

  /** The representative mapper. */
  @Autowired
  private DataProviderMapper dataProviderMapper;

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(RepresentativeServiceImpl.class);

  /**
   * Insert dataflow representative.
   *
   * @param dataflowId the dataflow id
   * @param dataflowRepresentativeVO the dataflow representative VO
   * @return the long
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public Long insertRepresentative(Long dataflowId, RepresentativeVO representativeVO)
      throws EEAException {
    if (representativeVO == null || dataflowId == null) {
      throw new EEAException(EEAErrorMessage.DATAFLOW_NOTFOUND);
    }
    Representative dataflowRepresentative = representativeMapper.classToEntity(representativeVO);
    Dataflow dataflow = new Dataflow();
    dataflow.setId(dataflowId);
    dataflowRepresentative.setDataflow(dataflow);
    DataProvider dataProvider = new DataProvider();
    dataProvider.setId(representativeVO.getDataProviderId());
    dataflowRepresentative.setDataProvider(dataProvider);
    LOG.info("Insert new representative relation to dataflow: {}", dataflowId);
    return representativeRepository.save(dataflowRepresentative).getId();
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
   * @param dataflowRepresentativeVO the dataflow representative VO
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
    Representative dataflowRepresentative =
        representativeRepository.findById(representativeVO.getId()).orElse(null);
    if (dataflowRepresentative == null) {
      throw new EEAException(EEAErrorMessage.REPRESENTATIVE_NOT_FOUND);
    }
    // update changes on first level
    if (representativeVO.getProviderAccount() != null) {
      dataflowRepresentative.setUserMail(representativeVO.getProviderAccount());
    }
    if (representativeVO.getDataProviderId() != null) {
      DataProvider dataProvider = new DataProvider();
      dataProvider.setId(representativeVO.getDataProviderId());
      dataflowRepresentative.setDataProvider(dataProvider);
    }
    // save changes
    LOG.info("updating the representative relation");
    return representativeRepository.save(dataflowRepresentative).getId();
  }

  /**
   * Gets the all representative types.
   *
   * @return the all representative types
   */
  @Override
  public List<DataProviderCodeVO> getAllDataProviderTypes() {
    LOG.info("obtaining the distinct representative types");
    return dataProviderRepository.findDistinctCode();
  }

  /**
   * Gets the dataflow represetatives by id data flow.
   *
   * @param dataflowId the dataflow id
   * @return the dataflow represetatives by id data flow
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
   * Gets the all representative by type.
   *
   * @param code the code
   * @return the all representative by type
   */
  @Override
  public List<DataProviderVO> getAllDataProviderByGroupId(Long groupId) {
    return dataProviderMapper.entityListToClass(dataProviderRepository.findAllByGroupId(groupId));
  }

}
