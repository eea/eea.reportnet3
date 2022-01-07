package org.eea.dataflow.service.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.eea.dataflow.mapper.DataProviderGroupMapper;
import org.eea.dataflow.mapper.DataProviderMapper;
import org.eea.dataflow.mapper.FMEUserMapper;
import org.eea.dataflow.mapper.LeadReporterMapper;
import org.eea.dataflow.mapper.RepresentativeMapper;
import org.eea.dataflow.persistence.domain.DataProvider;
import org.eea.dataflow.persistence.domain.DataProviderGroup;
import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.dataflow.persistence.domain.LeadReporter;
import org.eea.dataflow.persistence.domain.Representative;
import org.eea.dataflow.persistence.repository.DataProviderGroupRepository;
import org.eea.dataflow.persistence.repository.DataProviderRepository;
import org.eea.dataflow.persistence.repository.DataflowRepository;
import org.eea.dataflow.persistence.repository.FMEUserRepository;
import org.eea.dataflow.persistence.repository.LeadReporterRepository;
import org.eea.dataflow.persistence.repository.RepresentativeRepository;
import org.eea.dataflow.service.RepresentativeService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.dataset.ReferenceDatasetController.ReferenceDatasetControllerZuul;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.dataflow.DataProviderCodeVO;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataflow.FMEUserVO;
import org.eea.interfaces.vo.dataflow.LeadReporterVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.dataflow.enums.TypeDataProviderEnum;
import org.eea.interfaces.vo.dataflow.enums.TypeDataflowEnum;
import org.eea.interfaces.vo.dataset.ReferenceDatasetVO;
import org.eea.interfaces.vo.dataset.ReportingDatasetVO;
import org.eea.interfaces.vo.ums.ResourceAccessVO;
import org.eea.interfaces.vo.ums.ResourceAssignationVO;
import org.eea.interfaces.vo.ums.UserRepresentationVO;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.eea.interfaces.vo.ums.enums.ResourceTypeEnum;
import org.eea.interfaces.vo.ums.enums.SecurityRoleEnum;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.security.authorization.ObjectAccessRoleEnum;
import org.eea.security.jwt.expression.EeaSecurityExpressionRoot;
import org.eea.security.jwt.utils.EntityAccessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.opencsv.CSVWriter;
import io.jsonwebtoken.lang.Collections;

/** The Class RepresentativeServiceImpl. */
@Service("dataflowRepresentativeService")
public class RepresentativeServiceImpl implements RepresentativeService {

  /** The Constant REGEX: {@value}. */
  private static final String REGEX = "-";

  /** The Constant EMAIL_REGEX: {@value}. */
  private static final String EMAIL_REGEX =
      "^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$";

  /** The Constant ROLE_PROVIDER: {@value}. */
  private static final String ROLE_PROVIDER = "ROLE_PROVIDER-";

  /** The representative repository. */
  @Autowired
  private RepresentativeRepository representativeRepository;

  /** The data provider repository. */
  @Autowired
  private DataProviderRepository dataProviderRepository;

  /** The data provider repository. */
  @Autowired
  private DataProviderGroupRepository dataProviderGroupRepository;

  /** The dataflow repository. */
  @Autowired
  private DataflowRepository dataflowRepository;

  /** The representative mapper. */
  @Autowired
  private RepresentativeMapper representativeMapper;

  /** The data provider mapper. */
  @Autowired
  private DataProviderMapper dataProviderMapper;

  /** The lead reporter mapper. */
  @Autowired
  private LeadReporterMapper leadReporterMapper;

  /** The data provider group mapper. */
  @Autowired
  private DataProviderGroupMapper dataProviderGroupMapper;

  /** The user management controller zull. */
  @Autowired
  private UserManagementControllerZull userManagementControllerZull;

  /** The lead reporter repository. */
  @Autowired
  private LeadReporterRepository leadReporterRepository;

  /** The dataset metabase controller. */
  @Autowired
  private DataSetMetabaseControllerZuul datasetMetabaseController;

  /** The reference dataset controller zuul. */
  @Autowired
  private ReferenceDatasetControllerZuul referenceDatasetControllerZuul;

  /** The entity access service. */
  @Autowired
  private EntityAccessService entityAccessService;

  /** The FME user repository. */
  @Autowired
  private FMEUserRepository fmeUserRepository;

  /** The FME user mapper. */
  @Autowired
  private FMEUserMapper fmeUserMapper;

  /** The kafka sender utils. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /**
   * The delimiter.
   */
  @Value("${exportDataDelimiter}")
  private char delimiter;


  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(RepresentativeServiceImpl.class);

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant EMAIL: {@value}. */
  private static final String EMAIL = "Email";

  /** The Constant REPRESENTING: {@value}. */
  private static final String REPRESENTING = "Representing";

  /** The Constant IMPORTED: {@value}. */
  private static final String IMPORTED = "Imported";

  /** The Constant OK_IMPORT: {@value}. */
  private static final String OK_IMPORT = "OK - imported";

  /** The Constant KO_INVALID_EMAIL: {@value}. */
  private static final String KO_INVALID_EMAIL = "KO - invalid email";

  /** The Constant KO_ALREADY_EXISTS: {@value}. */
  private static final String KO_ALREADY_EXISTS =
      "KO - imported user already exists in Reportnet representing for the same Representative.";

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

    Long dataProviderId = representativeVO.getDataProviderId();
    Dataflow dataflow = dataflowRepository.findById(dataflowId).orElse(null);

    if (dataflow == null) {
      throw new EEAException(EEAErrorMessage.DATAFLOW_NOTFOUND);
    }

    Representative repre =
        representativeRepository.findOneByDataflow_IdAndDataProvider_Id(dataflowId, dataProviderId);
    if (repre != null) {
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
    representative.setId(0L);
    representative.setLeadReporters(new ArrayList<>());

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
   */
  @Override
  @Transactional
  public Long updateDataflowRepresentative(RepresentativeVO representativeVO) {

    // load old relationship
    Representative representative =
        representativeRepository.findById(representativeVO.getId()).orElse(new Representative());

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
   * @param providerType the provider type
   * @return the all data provider types
   */
  @Override
  public List<DataProviderCodeVO> getDataProviderGroupByType(TypeDataProviderEnum providerType) {
    LOG.info("obtaining the distinct representative types");
    List<DataProviderGroup> dataProviderGroups =
        dataProviderGroupRepository.findDistinctCode(providerType);

    return dataProviderGroupMapper.entityListToClass(dataProviderGroups);
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
    return dataProviderMapper
        .entityListToClass(dataProviderRepository.findAllByDataProviderGroup_id(groupId));
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
   * @param code the code
   * @return the list
   */
  @Override
  public List<DataProviderVO> findDataProvidersByCode(String code) {
    List<DataProvider> dataProviders = dataProviderRepository.findByCode(code);
    return dataProviderMapper.entityListToClass(dataProviders);
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
   * Find representatives by dataflow and dataprovider list.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderIdList the data provider id list
   * @return the list
   */
  @Override
  public List<RepresentativeVO> findRepresentativesByDataflowIdAndDataproviderList(Long dataflowId,
      List<Long> dataProviderIdList) {
    return representativeMapper.entityListToClass(representativeRepository
        .findByDataflowIdAndDataProviderIdIn(dataflowId, dataProviderIdList));
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
      headers.add(REPRESENTING);
      headers.add(EMAIL);
      csvWriter.writeNext(headers.stream().toArray(String[]::new), false);
      int nHeaders = 2;
      String[] fieldsToWrite = new String[nHeaders];

      // we find all representatives and add all representatives
      List<Representative> representativeList =
          representativeRepository.findAllByDataflow_Id(dataflowId);
      for (Representative representative : representativeList) {
        if (CollectionUtils.isEmpty(representative.getLeadReporters())) {
          fieldsToWrite[0] = representative.getDataProvider().getCode();
          fieldsToWrite[1] = "";
          csvWriter.writeNext(fieldsToWrite);
        } else {
          List<String> usersRepresentative = representative.getLeadReporters().stream()
              .map(LeadReporter::getEmail).collect(Collectors.toList());
          usersRepresentative.stream().forEach(users -> {
            fieldsToWrite[0] = representative.getDataProvider().getCode();
            fieldsToWrite[1] = users;
            csvWriter.writeNext(fieldsToWrite);
          });
        }

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
      headers.add(REPRESENTING);
      headers.add(EMAIL);
      csvWriter.writeNext(headers.stream().toArray(String[]::new), false);
      int nHeaders = 2;
      String[] fieldsToWrite = new String[nHeaders];

      // we find all dataprovider for group id
      List<DataProvider> dataProviderList =
          dataProviderRepository.findAllByDataProviderGroup_id(groupId);
      for (DataProvider dataProvider : dataProviderList) {
        fieldsToWrite[0] = dataProvider.getCode();
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
   * Import lead reporters file.
   *
   * @param dataflowId the dataflow id
   * @param groupId the group id
   * @param file the file
   * @return the byte[]
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Override
  @Transactional
  public byte[] importLeadReportersFile(Long dataflowId, Long groupId, MultipartFile file)
      throws EEAException, IOException {

    // We create the CSV used to inform about the import results
    StringWriter writer = new StringWriter();
    try (CSVWriter csvWriter = new CSVWriter(writer, delimiter, CSVWriter.DEFAULT_QUOTE_CHARACTER,
        CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END)) {

      List<String> headers = new ArrayList<>(Arrays.asList(REPRESENTING, EMAIL, IMPORTED));
      csvWriter.writeNext(headers.stream().toArray(String[]::new), false);
      int nHeaders = headers.size();

      List<DataProvider> dataProviderList =
          dataProviderRepository.findAllByDataProviderGroup_id(groupId);

      String content = new String(file.getBytes());
      List<String> everyLines = new ArrayList<>(Arrays.asList(content.split("\n")));
      // Removes the headers to be able to parse the Lead Reporters info
      everyLines.remove(0);

      List<Representative> representativeList = fillImportLeadReporterResults(everyLines,
          dataProviderList, nHeaders, csvWriter, dataflowId);

      // Add the imported lead reporters whose email and country code was alright to the repository
      if (!Collections.isEmpty(representativeList)) {
        representativeRepository.saveAll(representativeList);
      }
    } catch (IOException e) {
      LOG_ERROR.error(EEAErrorMessage.CSV_FILE_ERROR, e);
    } catch (IndexOutOfBoundsException e) {
      LOG_ERROR.error(EEAErrorMessage.DATA_FILE_ERROR, e);
      throw new EEAException(EEAErrorMessage.DATA_FILE_ERROR);
    } catch (EEAException e) {
      LOG_ERROR.error(EEAErrorMessage.DATAFLOW_NOTFOUND, e);
      throw new EEAException(EEAErrorMessage.DATAFLOW_NOTFOUND);
    }

    // Converts the read buffer to string and write it into the CSV file
    String csv = writer.getBuffer().toString();

    return csv.getBytes();
  }

  /**
   * Fill import lead reporter results.
   *
   * @param everyLines the every lines
   * @param dataProviderList the data provider list
   * @param headerSize the header size
   * @param csvWriter the csv writer
   * @param dataflowId the dataflow id
   * @return the list
   * @throws EEAException the EEA exception
   */
  private List<Representative> fillImportLeadReporterResults(List<String> everyLines,
      List<DataProvider> dataProviderList, int headerSize, CSVWriter csvWriter, Long dataflowId)
      throws EEAException {

    List<String> countryCodeList =
        dataProviderList.stream().map(DataProvider::getCode).collect(Collectors.toList());
    List<Representative> representativeList = new ArrayList<>();

    String[] fieldsToWrite = new String[headerSize];

    for (String representativeData : everyLines) {
      String[] dataLine = representativeData.split("[" + delimiter + "]");
      String countryCode = dataLine[0].replace("\"", "");
      String email = "";
      UserRepresentationVO user = null;
      if (dataLine.length == 2 && null != dataLine[1]) {
        email = dataLine[1].replace("\"", "").replace("\r", "");
        if (StringUtils.isNotBlank(email)) {
          user = userManagementControllerZull.getUserByEmail(email.toLowerCase());
        }
      }

      fieldsToWrite[0] = countryCode;
      fieldsToWrite[1] = email.toLowerCase();
      fieldsToWrite[2] = addLeadReporter(countryCodeList, countryCode, dataProviderList, dataflowId,
          email, representativeList, user);
      csvWriter.writeNext(fieldsToWrite);
    }
    return representativeList;
  }

  /**
   * Creates the lead reporter.
   *
   * @param representativeId the representative id
   * @param leadReporterVO the lead reporter VO
   * @return the long
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public Long createLeadReporter(Long representativeId, LeadReporterVO leadReporterVO)
      throws EEAException {

    String email = leadReporterVO.getEmail().toLowerCase();
    leadReporterVO.setEmail(email);
    Representative representative =
        representativeRepository.findById(representativeId).orElse(null);

    if (representative == null) {
      throw new EEAException(EEAErrorMessage.REPRESENTATIVE_NOT_FOUND);
    }
    if (representative.getLeadReporters().stream()
        .anyMatch(reporter -> email.equals(reporter.getEmail()))) {
      throw new EEAException(EEAErrorMessage.USER_AND_COUNTRY_EXIST);
    }
    LeadReporter leadReporter = leadReporterMapper.classToEntity(leadReporterVO);
    leadReporter.setRepresentative(representative);
    LOG.info("Insert new lead reporter relation to representative: {}", representativeId);

    UserRepresentationVO user = userManagementControllerZull.getUserByEmail(email);
    if (user == null) {
      leadReporter.setInvalid(true);
    } else {
      leadReporter.setInvalid(false);
      modifyLeadReporterPermissions(email, representative, false);
    }
    return leadReporterRepository.save(leadReporter).getId();

  }

  /**
   * Update lead reporter.
   *
   * @param leadReporterVO the lead reporter VO
   * @return the long
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public Long updateLeadReporter(LeadReporterVO leadReporterVO) throws EEAException {

    // load old reporter
    LeadReporter leadReporter = leadReporterRepository.findById(leadReporterVO.getId())
        .orElseThrow(() -> new EEAException(EEAErrorMessage.REPRESENTATIVE_NOT_FOUND));
    if (leadReporterVO.getRepresentativeId() != null) {
      Representative representative =
          representativeRepository.findById(leadReporterVO.getRepresentativeId()).orElse(null);
      if (representative == null) {
        throw new EEAException(EEAErrorMessage.REPRESENTATIVE_NOT_FOUND);
      }
      if (!leadReporterVO.getRepresentativeId().equals(leadReporter.getRepresentative().getId())) {
        throw new EEAException(EEAErrorMessage.REPRESENTATIVE_NOT_FOUND);
      }
      if (leadReporterVO.getEmail() != null) {
        leadReporterVO.setEmail(leadReporterVO.getEmail().toLowerCase());
        UserRepresentationVO newUser =
            userManagementControllerZull.getUserByEmail(leadReporterVO.getEmail().toLowerCase());
        leadReporter.setInvalid(newUser == null ? true : false);
        if (null != representative.getLeadReporters() && representative.getLeadReporters().stream()
            .filter(reporter -> !Boolean.TRUE.equals(leadReporter.getInvalid())
                && leadReporterVO.getEmail().equalsIgnoreCase(reporter.getEmail()))
            .collect(Collectors.counting()) == 1) {
          modifyLeadReporterPermissions(leadReporter.getEmail().toLowerCase(), representative,
              false);
          leadReporter.setEmail(leadReporterVO.getEmail());
        }
        leadReporter.setRepresentative(representative);
      }

    }

    // save changes
    return leadReporterRepository.save(leadReporter).getId();
  }

  /**
   * Delete lead reporter.
   *
   * @param leadReporterId the lead reporter id
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void deleteLeadReporter(Long leadReporterId) throws EEAException {
    Optional<LeadReporter> leadReporter = leadReporterRepository.findById(leadReporterId);
    if (leadReporter.isPresent()) {
      modifyLeadReporterPermissions(leadReporter.get().getEmail(),
          leadReporter.get().getRepresentative(), true);
    }
    LOG.info("Deleting the lead reporter relation");
    leadReporterRepository.deleteById(leadReporterId);
  }

  /**
   * Validate lead reporters.
   *
   * @param dataflowId the dataflow id
   * @param sendNotification the send notification
   * @throws EEAException the EEA exception
   */
  @Transactional
  @Async
  @Override
  public void validateLeadReporters(Long dataflowId, boolean sendNotification) throws EEAException {
    List<RepresentativeVO> representativeList = getRepresetativesByIdDataFlow(dataflowId);

    try {
      for (RepresentativeVO representative : representativeList) {
        List<LeadReporterVO> leadReporterList = representative.getLeadReporters().stream().filter(
            leadReporterVO -> leadReporterVO.getInvalid() != null && leadReporterVO.getInvalid())
            .collect(Collectors.toList());

        for (LeadReporterVO leadReporter : leadReporterList) {
          updateLeadReporter(leadReporter);
        }
      }

      if (sendNotification) {
        NotificationVO notificationVO = NotificationVO.builder()
            .user(SecurityContextHolder.getContext().getAuthentication().getName())
            .dataflowId(dataflowId).build();

        kafkaSenderUtils.releaseNotificableKafkaEvent(
            EventType.VALIDATE_LEAD_REPORTERS_COMPLETED_EVENT, null, notificationVO);
      }

    } catch (Exception e) {
      LOG.error("An error was produced while validating lead reporters for dataflow {}",
          dataflowId);
      if (sendNotification) {
        NotificationVO notificationVO = NotificationVO.builder()
            .user(SecurityContextHolder.getContext().getAuthentication().getName())
            .dataflowId(dataflowId).build();

        kafkaSenderUtils.releaseNotificableKafkaEvent(
            EventType.VALIDATE_LEAD_REPORTERS_FAILED_EVENT, null, notificationVO);
      }
    }

  }


  /**
   * Update representative visibility restrictions.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   * @param restrictFromPublic the restrict from public
   */
  @Override
  public void updateRepresentativeVisibilityRestrictions(Long dataflowId, Long dataProviderId,
      boolean restrictFromPublic) {
    representativeRepository.updateRepresentativeVisibilityRestrictions(dataflowId, dataProviderId,
        restrictFromPublic);
  }

  /**
   * Authorize by representative id.
   *
   * @param representativeId the representative id
   * @return true, if successful
   */
  @Override
  public boolean authorizeByRepresentativeId(Long representativeId) {

    boolean isAuthorized = false;

    if (null != representativeId) {
      Representative representative =
          representativeRepository.findById(representativeId).orElse(null);
      if (null != representative) {
        Dataflow dataflow = representative.getDataflow();
        if (null != dataflow) {
          EeaSecurityExpressionRoot eeaSecurityExpressionRoot =
              new EeaSecurityExpressionRoot(SecurityContextHolder.getContext().getAuthentication(),
                  userManagementControllerZull, entityAccessService);
          isAuthorized = eeaSecurityExpressionRoot.secondLevelAuthorize(dataflow.getId(),
              ObjectAccessRoleEnum.DATAFLOW_STEWARD, ObjectAccessRoleEnum.DATAFLOW_CUSTODIAN,
              ObjectAccessRoleEnum.DATAFLOW_CUSTODIAN_SUPPORT);
        }
      }
    }

    return isAuthorized;
  }


  /**
   * Gets the provider ids.
   *
   * @return the provider ids
   * @throws EEAException the EEA exception
   */
  @Override
  public List<Long> getProviderIds() throws EEAException {
    List<DataProviderVO> dataProviders = null;
    String countryCode = getCountryCodeNC();
    if (null != countryCode) {
      dataProviders = findDataProvidersByCode(countryCode);
    } else {
      throw new EEAException(EEAErrorMessage.UNAUTHORIZED);
    }
    return dataProviders.stream().map(DataProviderVO::getId).collect(Collectors.toList());
  }


  /**
   * Find fme users.
   *
   * @return the list
   */
  @Override
  public List<FMEUserVO> findFmeUsers() {
    return fmeUserMapper.entityListToClass(fmeUserRepository.findAll());
  }

  /**
   * Check restrict from public.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   * @return true, if successful
   * @throws EEAException the EEA exception
   */
  @Override
  public boolean checkRestrictFromPublic(Long dataflowId, Long dataProviderId) throws EEAException {
    boolean restrict = true;
    List<RepresentativeVO> representatives = getRepresetativesByIdDataFlow(dataflowId);
    if (null == representatives) {
      throw new EEAException(EEAErrorMessage.REPRESENTATIVE_NOT_FOUND);
    }
    for (RepresentativeVO representative : representatives) {
      if (representative.getDataProviderId().equals(dataProviderId)) {
        restrict = representative.isRestrictFromPublic();
      }
    }
    return restrict;
  }

  /**
   * Check if data have been release.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   * @return true, if successful
   * @throws EEAException the EEA exception
   */
  @Override
  public boolean checkDataHaveBeenRelease(Long dataflowId, Long dataProviderId)
      throws EEAException {
    boolean isReleased = true;
    List<ReportingDatasetVO> reportings =
        datasetMetabaseController.findReportingDataSetIdByDataflowId(dataflowId);
    if (null == reportings) {
      throw new EEAException(EEAErrorMessage.DATASET_NOTFOUND);
    }
    for (ReportingDatasetVO reporting : reportings) {
      if (reporting.getDataProviderId().equals(dataProviderId)
          && (Boolean.FALSE.equals(reporting.getIsReleased())
              || reporting.getIsReleased() == null)) {
        isReleased = false;
      }
    }
    return isReleased;
  }

  /**
   * Modify lead reporter permissions.
   *
   * @param email the email
   * @param representative the representative
   * @param remove the remove
   */
  private void modifyLeadReporterPermissions(String email, Representative representative,
      boolean remove) {
    if (userManagementControllerZull.getUserByEmail(email.toLowerCase()) != null) {
      if (Boolean.TRUE.equals(representative.getHasDatasets())) {
        List<ResourceAssignationVO> assignments = new ArrayList<>();
        // get datasetId
        List<ReportingDatasetVO> datasets =
            datasetMetabaseController.findReportingDataSetIdByDataflowIdAndProviderId(
                representative.getDataflow().getId(), representative.getDataProvider().getId());
        // assign resource to lead reporter
        List<Long> datasetsIds = new ArrayList<>();
        for (ReportingDatasetVO dataset : datasets) {
          assignments.add(
              createAssignments(dataset.getId(), email, ResourceGroupEnum.DATASET_LEAD_REPORTER));
          datasetsIds.add(dataset.getId());
        }
        // assign reference to lead reporter
        List<ReferenceDatasetVO> references = referenceDatasetControllerZuul
            .findReferenceDatasetByDataflowId(representative.getDataflow().getId());
        for (ReferenceDatasetVO referenceDatasetVO : references) {
          assignments.add(createAssignments(referenceDatasetVO.getId(), email,
              ResourceGroupEnum.REFERENCEDATASET_CUSTODIAN));
        }

        // Assign Dataflow-%s-LEAD_REPORTER
        if (!remove || !hasOtherReportingsByDataflow(email, representative, datasetsIds)) {
          assignments.add(createAssignments(representative.getDataflow().getId(), email,
              ResourceGroupEnum.DATAFLOW_LEAD_REPORTER));
        }

        if (!remove) {
          userManagementControllerZull.addContributorsToResources(assignments);
        } else {
          userManagementControllerZull.removeContributorsFromResources(assignments);
        }
      }
    } else {
      LOG.info(
          "Permissions were not assigned or deleted because the email pertains to a temporary Lead Reporter. Email: {}",
          email);
    }
  }

  /**
   * Checks for other reportings by dataflow.
   *
   * @param email the email
   * @param representative the representative
   * @param datasetIds the dataset ids
   * @return true, if successful
   */
  private boolean hasOtherReportingsByDataflow(String email, Representative representative,
      List<Long> datasetIds) {
    boolean result = false;
    List<ResourceAccessVO> resources = userManagementControllerZull.getResourcesByUserEmail(email);
    List<ReportingDatasetVO> reportings = datasetMetabaseController
        .findReportingDataSetIdByDataflowId(representative.getDataflow().getId());
    if (!CollectionUtils.isEmpty(resources) && !CollectionUtils.isEmpty(reportings)) {
      for (ReportingDatasetVO reportingDatasetVO : reportings) {
        if (!datasetIds.contains(reportingDatasetVO.getId())) {
          for (ResourceAccessVO resourceAccessVO : resources) {
            if (ResourceTypeEnum.DATASET.equals(resourceAccessVO.getResource())
                && SecurityRoleEnum.LEAD_REPORTER.equals(resourceAccessVO.getRole())
                && reportingDatasetVO.getId().equals(resourceAccessVO.getId())) {
              result = true;
            }
          }
        }
      }
    }
    return result;
  }


  /**
   * Creates the assignments.
   *
   * @param id the id
   * @param email the email
   * @param group the group
   * @return the resource assignation VO
   */
  private ResourceAssignationVO createAssignments(Long id, String email, ResourceGroupEnum group) {

    ResourceAssignationVO resource = new ResourceAssignationVO();
    resource.setResourceId(id);
    resource.setEmail(email);
    resource.setResourceGroup(group);

    return resource;
  }

  /**
   * Gets the country code NC.
   *
   * @return the country code NC
   */
  private String getCountryCodeNC() {
    Collection<String> authorities = SecurityContextHolder.getContext().getAuthentication()
        .getAuthorities().stream().map(authority -> ((GrantedAuthority) authority).getAuthority())
        .collect(Collectors.toList());
    String countryCode = null;
    for (String auth : authorities) {
      if (null != auth && auth.contains(ROLE_PROVIDER)) {
        String[] roleSplit = auth.split(REGEX);
        countryCode = roleSplit[1];
        break;
      }
    }
    return countryCode;
  }


  /**
   * Adds the lead reporter.
   *
   * @param countryCodeList the country code list
   * @param countryCode the country code
   * @param dataProviderList the data provider list
   * @param dataflowId the dataflow id
   * @param email the email
   * @param representativeList the representative list
   * @param user the user
   * @return the string
   * @throws EEAException the EEA exception
   */
  private String addLeadReporter(List<String> countryCodeList, String countryCode,
      List<DataProvider> dataProviderList, Long dataflowId, String email,
      List<Representative> representativeList, UserRepresentationVO user) throws EEAException {

    String importResult = "";

    Dataflow dataflow = dataflowRepository.findById(dataflowId).orElse(null);

    if (dataflow == null) {
      throw new EEAException("Couldn't find a dataflow with the provided Id");
    }

    String dataProviderType =
        TypeDataflowEnum.BUSINESS.equals(dataflow.getType()) ? "company" : "country";

    if (!countryCodeList.contains(countryCode)) {
      importResult = "Error during import, " + dataProviderType + " code doesn't correspond to a "
          + dataProviderType + " in ReportNet";
    } else {
      importResult = prepareImportResult(countryCode, dataProviderList, email, representativeList,
          user, importResult, dataflow);

    }
    return importResult;
  }

  /**
   * Prepare import result.
   *
   * @param countryCode the country code
   * @param dataProviderList the data provider list
   * @param email the email
   * @param representativeList the representative list
   * @param user the user
   * @param importResult the import result
   * @param dataflow the dataflow
   * @return the string
   */
  private String prepareImportResult(String countryCode, List<DataProvider> dataProviderList,
      String email, List<Representative> representativeList, UserRepresentationVO user,
      String importResult, Dataflow dataflow) {
    DataProvider dataProvider = dataProviderList.stream()
        .filter(dataProv -> countryCode.equalsIgnoreCase(dataProv.getCode())).findFirst()
        .orElse(null);

    if (null != dataProvider
        && (null == representativeRepository.findOneByDataflowIdAndDataProviderIdUserMail(
            dataflow.getId(), dataProvider.getId(), email.toLowerCase()))) {

      Representative representative = representativeList.stream()
          .filter(rep -> dataProvider.getId().equals(rep.getDataProvider().getId())).findFirst()
          .orElse(representativeRepository.findOneByDataflow_IdAndDataProvider_Id(dataflow.getId(),
              dataProvider.getId()));

      Pattern p = Pattern.compile(EMAIL_REGEX);
      Matcher m = p.matcher(email);

      if (m.matches()) {
        // If the representative exists we don't create it again
        if (null == representative) {
          importResult = createRepresentativeAndLeadReporter(email, representativeList, user,
              dataflow, dataProvider);
        } else {
          importResult = createLeadReporterWhenRepresentativeExists(email, representativeList, user,
              representative);
        }
      } else {
        importResult = KO_INVALID_EMAIL;
      }
    }
    return importResult;
  }

  /**
   * Creates the lead reporter when representative exists.
   *
   * @param email the email
   * @param representativeList the representative list
   * @param user the user
   * @param representative the representative
   * @return the string
   */
  private String createLeadReporterWhenRepresentativeExists(String email,
      List<Representative> representativeList, UserRepresentationVO user,
      Representative representative) {
    String importResult = "";
    List<LeadReporter> leadReporters = representative.getLeadReporters();
    if (StringUtils.isNotBlank(email)) {
      final String innerEmail = email.toLowerCase();
      if (leadReporters.stream().noneMatch(rep -> innerEmail.equals(rep.getEmail()))) {
        LeadReporter leadReporter = new LeadReporter();
        leadReporter.setRepresentative(representative);
        leadReporter.setEmail(innerEmail);
        if (null == user) {
          leadReporter.setInvalid(true);
        }
        leadReporters.add(leadReporter);
        representative.setLeadReporters(leadReporters);
        importResult = OK_IMPORT;
      } else {
        importResult = KO_ALREADY_EXISTS;
      }
    }
    if (!representativeList.contains(representative)) {
      representativeList.add(representative);
    }

    return importResult;
  }

  /**
   * Creates the representative and lead reporter.
   *
   * @param email the email
   * @param representativeList the representative list
   * @param user the user
   * @param dataflow the dataflow
   * @param dataProvider the data provider
   * @return the string
   */
  private String createRepresentativeAndLeadReporter(String email,
      List<Representative> representativeList, UserRepresentationVO user, Dataflow dataflow,
      DataProvider dataProvider) {
    String importResult;
    Representative representative;
    DataProvider dataProviderNew = new DataProvider();
    representative = new Representative();
    dataProviderNew.setId(dataProvider.getId());
    representative.setDataflow(dataflow);
    representative.setDataProvider(dataProviderNew);
    representative.setReceiptDownloaded(false);
    representative.setReceiptOutdated(false);
    representative.setHasDatasets(false);
    representative.setId(0L);
    if (StringUtils.isNotBlank(email)) {
      LeadReporter leadReporter = new LeadReporter();
      leadReporter.setRepresentative(representative);
      leadReporter.setEmail(email.toLowerCase());
      if (null == user) {
        leadReporter.setInvalid(true);
      }
      representative.setLeadReporters(new ArrayList<>(Arrays.asList(leadReporter)));
    } else {
      representative.setLeadReporters(new ArrayList<>());
    }
    representativeList.add(representative);
    importResult = OK_IMPORT;
    return importResult;
  }

}
