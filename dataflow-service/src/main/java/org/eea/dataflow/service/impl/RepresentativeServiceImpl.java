package org.eea.dataflow.service.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.eea.dataflow.mapper.DataProviderMapper;
import org.eea.dataflow.mapper.RepresentativeMapper;
import org.eea.dataflow.persistence.domain.DataProvider;
import org.eea.dataflow.persistence.domain.DataProviderCode;
import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.dataflow.persistence.domain.Representative;
import org.eea.dataflow.persistence.domain.User;
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
import org.eea.interfaces.vo.ums.UserRepresentationVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import com.opencsv.CSVWriter;
import io.jsonwebtoken.lang.Collections;

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

  /**
   * The delimiter.
   */
  @Value("${loadDataDelimiter}")
  private char delimiter;


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

    String email = representativeVO.getProviderAccounts().get(0);
    Long dataProviderId = representativeVO.getDataProviderId();
    Dataflow dataflow = dataflowRepository.findById(dataflowId).orElse(null);

    if (dataflow == null) {
      throw new EEAException(EEAErrorMessage.DATAFLOW_NOTFOUND);
    }
    UserRepresentationVO user = userManagementControllerZull.getUserByEmail(email);
    if (user == null) {
      throw new EEAException(EEAErrorMessage.USER_REQUEST_NOTFOUND);
    }
    if (null == representativeRepository.findOneByDataflowIdAndDataProviderIdUserMail(dataflowId,
        representativeVO.getDataProviderId(), email)) {
      throw new EEAException(EEAErrorMessage.USER_AND_COUNTRY_EXIST);
    }
    DataProvider dataProvider = new DataProvider();
    dataProvider.setId(dataProviderId);
    Representative representative = representativeMapper.classToEntity(representativeVO);
    representative.setDataflow(dataflow);
    representative.setDataProvider(dataProvider);
    representative.setReceiptDownloaded(false);
    representative.setReceiptOutdated(false);
    representative.setHasDatasets(false);
    representative.getReporters().stream().findFirst()
        .ifPresent(reporter -> reporter.setUserMail(email));

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
    // update changes on first level
    if (representativeVO.getProviderAccounts() != null) {
      Set<User> usersToInsert = new HashSet<>();
      for (String email : representativeVO.getProviderAccounts()) {
        Optional<User> user = representative.getReporters().stream()
            .filter(reporter -> reporter.getUserMail().equals(email)).findAny();
        if (user.isPresent()) {
          usersToInsert.add(user.get());
        } else {
          UserRepresentationVO newUser = userManagementControllerZull.getUserByEmail(email);
          usersToInsert.add(new User(newUser.getEmail(), null));
        }
      }
      representative.setReporters(usersToInsert);
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
  private boolean existsUserMail(Long dataProviderId, List<String> userMail, Long dataflowId)
      throws EEAException {
    if (dataProviderId == null || CollectionUtils.isEmpty(userMail)) {
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
   * Export file.
   *
   * @param dataflowId the dataflow id
   * @return the byte[]
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Override
  public byte[] exportFile(Long dataflowId) throws EEAException, IOException {
    // we create the csv
    StringWriter writer = new StringWriter();
    try (CSVWriter csvWriter = new CSVWriter(writer, delimiter, CSVWriter.DEFAULT_QUOTE_CHARACTER,
        CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END)) {
      List<String> headers = new ArrayList<>();
      headers.add("Email");
      headers.add("Representing");
      csvWriter.writeNext(headers.stream().toArray(String[]::new), false);
      int nHeaders = 2;
      String[] fieldsToWrite = new String[nHeaders];

      // we find all representatives and add all representatives
      List<Representative> representativeList =
          representativeRepository.findAllByDataflow_Id(dataflowId);
      for (Representative representative : representativeList) {
        List<String> usersRepresentative = representative.getReporters().stream()
            .map(User::getUserMail).collect(Collectors.toList());
        usersRepresentative.stream().forEach(users -> {
          fieldsToWrite[0] = users;
          fieldsToWrite[1] = representative.getDataProvider().getCode();
          csvWriter.writeNext(fieldsToWrite);
        });

      }
    } catch (IOException e) {
      LOG_ERROR.error(EEAErrorMessage.CSV_FILE_ERROR, e);
    }
    // Once read we convert it to string
    String csv = writer.getBuffer().toString();

    return csv.getBytes();
  }

  /**
   * Export template reporters file.
   *
   * @param groupId the group id
   * @return the byte[]
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Override
  public byte[] exportTemplateReportersFile(Long groupId) throws EEAException, IOException {
    // we create the csv
    StringWriter writer = new StringWriter();
    try (CSVWriter csvWriter = new CSVWriter(writer, delimiter, CSVWriter.DEFAULT_QUOTE_CHARACTER,
        CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END)) {
      List<String> headers = new ArrayList<>();
      headers.add("Email");
      headers.add("Representing");
      csvWriter.writeNext(headers.stream().toArray(String[]::new), false);
      int nHeaders = 2;
      String[] fieldsToWrite = new String[nHeaders];

      // we find all dataprovider for group id
      List<DataProvider> dataProviderList = dataProviderRepository.findAllByGroupId(groupId);
      for (DataProvider dataProvider : dataProviderList) {
        fieldsToWrite[1] = dataProvider.getCode();
        csvWriter.writeNext(fieldsToWrite);

      }
    } catch (IOException e) {
      LOG_ERROR.error(EEAErrorMessage.CSV_FILE_ERROR, e);
    }
    // Once read we convert it to string
    String csv = writer.getBuffer().toString();

    return csv.getBytes();
  }

  /**
   * Import file.
   *
   * @param dataflowId the dataflow id
   * @param groupId the group id
   * @param file the file
   * @return the byte[]
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Override
  // @Transactional
  public byte[] importFile(Long dataflowId, Long groupId, MultipartFile file)
      throws EEAException, IOException {

    // we create the cvs to send when finish the import
    StringWriter writer = new StringWriter();
    try (CSVWriter csvWriter = new CSVWriter(writer, delimiter, CSVWriter.DEFAULT_QUOTE_CHARACTER,
        CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END)) {
      List<String> headers = new ArrayList<>();
      headers.add("Email");
      headers.add("Representing");
      headers.add("Imported");
      csvWriter.writeNext(headers.stream().toArray(String[]::new), false);
      int nHeaders = 3;
      String[] fieldsToWrite = new String[nHeaders];


      List<DataProvider> dataProviderList = dataProviderRepository.findAllByGroupId(groupId);
      List<String> countryCodeList =
          dataProviderList.stream().map(DataProvider::getCode).collect(Collectors.toList());

      String content = new String(file.getBytes());
      List<String> everyLines = new ArrayList<>(Arrays.asList(content.split("\n")));
      everyLines.remove(0);

      Dataflow dataflow = dataflowRepository.findById(dataflowId).orElse(null);

      List<Representative> representativeList = new ArrayList<>();

      for (String representativeData : everyLines) {
        String[] dataLine = representativeData.split("[|]");
        String email = dataLine[0].replaceAll("\"", "");
        String contryCode = dataLine[1].replaceAll("\"", "");
        UserRepresentationVO user = userManagementControllerZull.getUserByEmail(email);
        if (!countryCodeList.contains(contryCode) && null == user) {
          fieldsToWrite[2] = "KO imported country and user doesn't exist in reportnet";
        } else if (!countryCodeList.contains(contryCode)) {
          fieldsToWrite[2] = "KO imported country doesn't exist";
        } else if (null == user) {
          fieldsToWrite[2] = "KO imported user doesn't exist in reportnet";
        } else {

          Long dataProviderId = dataProviderList.stream()
              .filter(dataProvider -> contryCode.equalsIgnoreCase(dataProvider.getCode()))
              .findFirst().get().getId();

          if (null == representativeRepository
              .findOneByDataflowIdAndDataProviderIdUserMail(dataflowId, dataProviderId, email)) {

            Representative representative = representativeRepository
                .findOneByDataflow_IdAndDataProvider_Id(dataflowId, dataProviderId);

            // if exist we dont create representative
            if (null == representative) {
              DataProvider dataProvider = new DataProvider();
              representative = new Representative();
              dataProvider.setId(dataProviderId);
              representative.setDataflow(dataflow);
              representative.setDataProvider(dataProvider);
              representative.setReceiptDownloaded(false);
              representative.setReceiptOutdated(false);
              representative.setHasDatasets(false);
            }
            if (!Collections.isEmpty(representative.getReporters())) {
              representative.getReporters().stream().findFirst()
                  .ifPresent(reporter -> reporter.setUserMail(email));
            } else {
              Set<User> reporters = new HashSet();
              User userNew = new User();
              userNew.setUserMail(user.getEmail());
              reporters.add(userNew);
              representative.setReporters(reporters);
              representative.getReporters().stream().findFirst()
                  .ifPresent(reporter -> reporter.setUserMail(user.getEmail()));
            }
            representativeList.add(representative);
            fieldsToWrite[2] = "OK imported";
          } else {
            fieldsToWrite[2] = "KO imported already exist in reportnet";
          }

        }
        fieldsToWrite[0] = email;
        fieldsToWrite[1] = contryCode;
        csvWriter.writeNext(fieldsToWrite);
      }
      if (!Collections.isEmpty(representativeList)) {
        representativeRepository.saveAll(representativeList);
      }
    } catch (

    IOException e) {
      LOG_ERROR.error(EEAErrorMessage.CSV_FILE_ERROR, e);
    }
    // Once read we convert it to string
    String csv = writer.getBuffer().toString();
    return csv.getBytes();
  }

}

