package org.eea.dataflow.service.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.eea.dataflow.mapper.DataProviderMapper;
import org.eea.dataflow.mapper.LeadReporterMapper;
import org.eea.dataflow.mapper.RepresentativeMapper;
import org.eea.dataflow.persistence.domain.DataProvider;
import org.eea.dataflow.persistence.domain.DataProviderCode;
import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.dataflow.persistence.domain.LeadReporter;
import org.eea.dataflow.persistence.domain.Representative;
import org.eea.dataflow.persistence.repository.DataProviderRepository;
import org.eea.dataflow.persistence.repository.DataflowRepository;
import org.eea.dataflow.persistence.repository.LeadReporterRepository;
import org.eea.dataflow.persistence.repository.RepresentativeRepository;
import org.eea.dataflow.service.RepresentativeService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.dataflow.DataProviderCodeVO;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataflow.LeadReporterVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.dataset.ReportingDatasetVO;
import org.eea.interfaces.vo.ums.ResourceAssignationVO;
import org.eea.interfaces.vo.ums.UserRepresentationVO;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.eea.security.authorization.ObjectAccessRoleEnum;
import org.eea.security.jwt.expression.EeaSecurityExpressionRoot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
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

  /** The lead reporter mapper. */
  @Autowired
  private LeadReporterMapper leadReporterMapper;

  /** The user management controller zull. */
  @Autowired
  private UserManagementControllerZull userManagementControllerZull;

  /** The lead reporter repository. */
  @Autowired
  private LeadReporterRepository leadReporterRepository;

  @Autowired
  private DatasetMetabaseController datasetMetabaseController;

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

    Long dataProviderId = representativeVO.getDataProviderId();
    Dataflow dataflow = dataflowRepository.findById(dataflowId).orElse(null);

    if (dataflow == null) {
      throw new EEAException(EEAErrorMessage.DATAFLOW_NOTFOUND);
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
      headers.add("Representing");
      headers.add("Email");
      csvWriter.writeNext(headers.stream().toArray(String[]::new), false);
      int nHeaders = 2;
      String[] fieldsToWrite = new String[nHeaders];

      // we find all representatives and add all representatives
      List<Representative> representativeList =
          representativeRepository.findAllByDataflow_Id(dataflowId);
      for (Representative representative : representativeList) {
        List<String> usersRepresentative = representative.getLeadReporters().stream()
            .map(LeadReporter::getEmail).collect(Collectors.toList());
        usersRepresentative.stream().forEach(users -> {
          fieldsToWrite[0] = representative.getDataProvider().getCode();
          fieldsToWrite[1] = users;
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
      headers.add("Representing");
      headers.add("Email");
      csvWriter.writeNext(headers.stream().toArray(String[]::new), false);
      int nHeaders = 2;
      String[] fieldsToWrite = new String[nHeaders];

      // we find all dataprovider for group id
      List<DataProvider> dataProviderList = dataProviderRepository.findAllByGroupId(groupId);
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
  @Transactional
  public byte[] importFile(Long dataflowId, Long groupId, MultipartFile file)
      throws EEAException, IOException {

    // we create the cvs to send when finish the import
    StringWriter writer = new StringWriter();
    try (CSVWriter csvWriter = new CSVWriter(writer, delimiter, CSVWriter.DEFAULT_QUOTE_CHARACTER,
        CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END)) {
      List<String> headers = new ArrayList<>();
      headers.add("Representing");
      headers.add("Email");
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
        String contryCode = dataLine[0].replaceAll("\"", "");
        String email = dataLine[1].replaceAll("\"", "");
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
              representative.setId(0L);
              LeadReporter leadReporter = new LeadReporter();
              leadReporter.setRepresentative(representative);
              leadReporter.setEmail(email);
              representative.setLeadReporters(Arrays.asList(leadReporter));
            } else {
              List<LeadReporter> leadReporters = representative.getLeadReporters();
              LeadReporter leadReporter = new LeadReporter();
              leadReporter.setRepresentative(representative);
              leadReporter.setEmail(email);
              leadReporters.add(leadReporter);
              representative.setLeadReporters(leadReporters);
            }
            representativeList.add(representative);
            fieldsToWrite[2] = "OK imported";
          } else {
            fieldsToWrite[2] = "KO imported already exist in reportnet";
          }

        }
        fieldsToWrite[0] = contryCode;
        fieldsToWrite[1] = email;
        csvWriter.writeNext(fieldsToWrite);
      }
      if (!Collections.isEmpty(representativeList)) {
        representativeRepository.saveAll(representativeList);
      }
    } catch (IOException e) {
      LOG_ERROR.error(EEAErrorMessage.CSV_FILE_ERROR, e);
    }
    // Once read we convert it to string
    String csv = writer.getBuffer().toString();
    return csv.getBytes();
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

    String email = leadReporterVO.getEmail();
    Representative representative =
        representativeRepository.findById(representativeId).orElse(null);

    if (representative == null) {
      throw new EEAException(EEAErrorMessage.REPRESENTATIVE_NOT_FOUND);
    }
    UserRepresentationVO user = userManagementControllerZull.getUserByEmail(email);
    if (user == null) {
      throw new EEAException(EEAErrorMessage.USER_REQUEST_NOTFOUND);
    }
    if (representative.getLeadReporters().stream()
        .anyMatch(reporter -> email.equals(reporter.getEmail()))) {
      throw new EEAException(EEAErrorMessage.USER_AND_COUNTRY_EXIST);
    }
    LeadReporter leadReporter = leadReporterMapper.classToEntity(leadReporterVO);
    leadReporter.setRepresentative(representative);
    LOG.info("Insert new lead reporter relation to representative: {}", representativeId);
    modifyLeadReporterPermissions(email, representative, false);
    return leadReporterRepository.save(leadReporter).getId();

  }

  @Override
  @Transactional
  public Long updateLeadReporter(LeadReporterVO leadReporterVO) throws EEAException {

    // load old reporter
    LeadReporter leadReporter = leadReporterRepository.findById(leadReporterVO.getId())
        .orElseThrow(() -> new EEAException(EEAErrorMessage.REPRESENTATIVE_NOT_FOUND));
    Representative representative;
    if (leadReporterVO.getEmail() != null) {
      UserRepresentationVO newUser =
          userManagementControllerZull.getUserByEmail(leadReporterVO.getEmail());
      if (newUser != null) {
        representative =
            representativeRepository.findById(leadReporterVO.getRepresentativeId()).orElse(null);
        if (null != representative && null != representative.getLeadReporters()
            && representative.getLeadReporters().stream()
                .filter(reporter -> leadReporterVO.getEmail().equals(reporter.getEmail()))
                .collect(Collectors.counting()) == 0) {
          leadReporter.setEmail(leadReporterVO.getEmail());
          modifyLeadReporterPermissions(leadReporterVO.getEmail(), representative, false);
        }
      }
    }
    representative = new Representative();
    if (leadReporterVO.getRepresentativeId() != null) {
      representative.setId(leadReporterVO.getRepresentativeId());
      leadReporter.setRepresentative(representative);
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
          EeaSecurityExpressionRoot eeaSecurityExpressionRoot = new EeaSecurityExpressionRoot(
              SecurityContextHolder.getContext().getAuthentication(), userManagementControllerZull);
          isAuthorized = eeaSecurityExpressionRoot.secondLevelAuthorize(representativeId,
              ObjectAccessRoleEnum.DATAFLOW_STEWARD, ObjectAccessRoleEnum.DATAFLOW_CUSTODIAN);
        }
      }
    }

    return isAuthorized;
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
    if (Boolean.TRUE.equals(representative.getHasDatasets())) {
      List<ResourceAssignationVO> assignments = new ArrayList<>();
      // get datasetId
      List<ReportingDatasetVO> datasets =
          datasetMetabaseController.findReportingDataSetIdByDataflowIdAndProviderId(
              representative.getDataflow().getId(), representative.getDataProvider().getId());
      // assign resource to lead reporter
      for (ReportingDatasetVO dataset : datasets) {
        assignments.add(
            createAssignments(dataset.getId(), email, ResourceGroupEnum.DATASET_LEAD_REPORTER));
      }
      // Assign Dataflow-%s-LEAD_REPORTER
      assignments.add(createAssignments(representative.getDataflow().getId(), email,
          ResourceGroupEnum.DATAFLOW_LEAD_REPORTER));
      if (!remove) {
        userManagementControllerZull.addContributorsToResources(assignments);
      } else {
        userManagementControllerZull.removeContributorsFromResources(assignments);
      }
    }
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
}
