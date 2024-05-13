package org.eea.rod.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.eea.interfaces.vo.rod.LegalInstrumentVO;
import org.eea.interfaces.vo.rod.ObligationListVO;
import org.eea.interfaces.vo.rod.ObligationVO;
import org.eea.rod.mapper.ClientMapper;
import org.eea.rod.mapper.CountryMapper;
import org.eea.rod.mapper.IssueMapper;
import org.eea.rod.mapper.ObligationMapper;
import org.eea.rod.persistence.domain.Client;
import org.eea.rod.persistence.domain.Country;
import org.eea.rod.persistence.domain.Issue;
import org.eea.rod.persistence.domain.Obligation;
import org.eea.rod.persistence.repository.ClientFeignRepository;
import org.eea.rod.persistence.repository.CountryFeignRepository;
import org.eea.rod.persistence.repository.IssueFeignRepository;
import org.eea.rod.persistence.repository.ObligationFeignRepository;
import org.eea.rod.service.ObligationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * The type Obligation service.
 */
@Service
public class ObligationServiceImpl implements ObligationService {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(ObligationServiceImpl.class);

  /** The obligation feign repository. */
  @Autowired
  private ObligationFeignRepository obligationFeignRepository;

  /** The client feign repository. */
  @Autowired
  private ClientFeignRepository clientFeignRepository;

  /** The country feign repository. */
  @Autowired
  private CountryFeignRepository countryFeignRepository;

  /** The issue feign repository. */
  @Autowired
  private IssueFeignRepository issueFeignRepository;

  /** The obligation mapper. */
  @Autowired
  private ObligationMapper obligationMapper;

  /** The client mapper. */
  @Autowired
  private ClientMapper clientMapper;

  /** The country mapper. */
  @Autowired
  private CountryMapper countryMapper;

  /** The issue mapper. */
  @Autowired
  private IssueMapper issueMapper;

  /**
   * Find opened obligation.
   *
   * @param clientId the client id
   * @param spatialId the spatial id
   * @param issueId the issue id
   * @param deadlineDateFrom the deadline date from
   * @param deadlineDateTo the deadline date to
   * @return the obligation list VO
   */
  @Override
  public ObligationListVO findOpenedObligation(Integer clientId, Integer spatialId, Integer issueId,
      Date deadlineDateFrom, Date deadlineDateTo) {
    Long dateFrom = Optional.ofNullable(deadlineDateFrom).map(Date::getTime).orElse(null);
    Long dateTo = Optional.ofNullable(deadlineDateTo).map(Date::getTime).orElse(null);

    if (dateTo == null && dateFrom != null) {
      dateTo = dateFrom;
      deadlineDateTo = deadlineDateFrom;
    }

    List<Obligation> obligations =
        obligationFeignRepository.findOpenedObligations(clientId, issueId, spatialId, null, null);
    List<Obligation> filteredObligations = new ArrayList<>();

    if (dateTo != null && dateFrom != null) {
      LOG.info("Date range from {} to {}, (in milliseconds {} and {})", deadlineDateFrom,
          deadlineDateTo, dateFrom, dateTo);
      for (Obligation obligation : obligations) {
        if (obligation.getNextDeadline() != null
            && ((obligation.getNextDeadline().getTime() >= dateFrom
                && obligation.getNextDeadline().getTime() <= dateTo)
                || isSameDay(deadlineDateFrom, obligation.getNextDeadline()))) {
          filteredObligations.add(obligation);
          LOG.info("Obligation with id {} and nextDeadLine date {} added (in milliseconds {})",
              obligation.getObligationId(), obligation.getNextDeadline(),
              obligation.getNextDeadline().getTime());
        }
      }
    } else {
      filteredObligations = obligations;
    }

    List<ObligationVO> obligationVOS = obligationMapper.entityListToClass(filteredObligations);
    List<Client> clients = this.clientFeignRepository.findAll();
    List<Country> countries = new ArrayList<>();// this.countryFeignRepository.findAll(); this will
                                                // not be necessary at the moment
    List<Issue> issues = new ArrayList<>();// this.issueFeignRepository.findAll();this will not be
                                           // necessary at the moment

    for (int i = 0; i < filteredObligations.size(); i++) {
      fillObligationSubentityFields(obligationVOS.get(i), filteredObligations.get(i), clients,
          countries, issues);
    }
    ObligationListVO obligationListVO = new ObligationListVO();
    obligationListVO.setObligations(obligationVOS);
    obligationListVO.setFilteredRecords(Long.valueOf(filteredObligations.size()));
    obligationListVO.setTotalRecords(Long.valueOf(
        obligationFeignRepository.findOpenedObligations(null, null, null, null, null).size()));

    return obligationListVO;
  }

  /**
   * Find obligation by id.
   *
   * @param obligationId the obligation id
   * @return the obligation VO
   */
  @Override
  public ObligationVO findObligationById(Integer obligationId) {
    List<Client> clients = this.clientFeignRepository.findAll();
    List<Country> countries = this.countryFeignRepository.findAll();
    List<Issue> issues = this.issueFeignRepository.findAll();
    Obligation obligation = obligationFeignRepository.findObligationById(obligationId);
    ObligationVO obligationVO = obligationMapper.entityToClass(obligation);

    fillObligationSubentityFields(obligationVO, obligation, clients, countries, issues);
    return obligationVO;
  }

  /**
   * Fill obligation subentity fields.
   *
   * @param obligationVO the obligation VO
   * @param obligation the obligation
   * @param clients the clients
   * @param countries the countries
   * @param issues the issues
   */
  private void fillObligationSubentityFields(ObligationVO obligationVO, Obligation obligation,
      final List<Client> clients, final List<Country> countries, final List<Issue> issues) {

    // Map legal instrument information
    LegalInstrumentVO legalInstrumentVO = new LegalInstrumentVO();
    legalInstrumentVO.setSourceAlias(obligation.getSourceAlias());
    legalInstrumentVO.setSourceId(obligation.getSourceId());
    legalInstrumentVO.setSourceTitle(obligation.getSourceTitle());
    obligationVO.setLegalInstrument(legalInstrumentVO);

    // Find clients from rod. Clients are unique since they are the ones registering the Obligation
    if (StringUtils.isNotBlank(obligation.getClientId()) && !CollectionUtils.isEmpty(clients)) {
      Client client = clients.stream()
          .filter(
              clientValue -> clientValue.getClientId().toString().equals(obligation.getClientId()))
          .findFirst().orElse(new Client());
      obligationVO.setClient(clientMapper.entityToClass(client));
    }
    // Find countries from rod. These are the countries bounded to the obligation. Might be several.
    if (StringUtils.isNotBlank(obligation.getSpatialId()) && !CollectionUtils.isEmpty(countries)) {
      // Countries in the obligation comes in comma separated values. Need to convert to a list
      // before being treated.
      final List<String> spatialIds = obligation.getSpatialId().endsWith(",")
          ? Arrays.asList(obligation.getSpatialId()
              .substring(0, obligation.getSpatialId().length() - 1).split(","))
          : Arrays.asList(obligation.getSpatialId().split(","));
      // Find in rod the countries bounded to the obligation and setting them to the final
      // ObligationVO
      List<Country> filteredCountries = countries.stream()
          .filter(countryValue -> spatialIds.contains(countryValue.getSpatialId().toString()))
          .collect(Collectors.toList());
      obligationVO.setCountries(countryMapper.entityListToClass(filteredCountries));

      // Find issues from rod. These are the issues bounded to the obligation. Might be several.
      if (StringUtils.isNotBlank(obligation.getSpatialId()) && !CollectionUtils.isEmpty(issues)) {
        // Issues in the obligation comes in comma separated values. Need to convert to a list
        // before being treated.
        final List<String> issuesIds = obligation.getIssueId().endsWith(",")
            ? Arrays.asList(obligation.getIssueId()
                .substring(0, obligation.getIssueId().length() - 1).split(","))
            : Arrays.asList(obligation.getIssueId().split(","));
        // Find in rod the issues bounded to the obligation and setting them to the final
        // ObligationVO
        List<Issue> filteredIssues = issues.stream()
            .filter(issueValue -> issuesIds.contains(issueValue.getIssueId().toString()))
            .collect(Collectors.toList());
        obligationVO.setIssues(issueMapper.entityListToClass(filteredIssues));
      }

    }

  }

  /**
   * Checks if is same day.
   *
   * @param dateRequest the date request
   * @param dateResponse the date response
   * @return true, if is same day
   */
  private boolean isSameDay(Date dateRequest, Date dateResponse) {
    boolean result = false;
    if (dateRequest != null && dateResponse != null) {
      Calendar calendarRequest = Calendar.getInstance();
      calendarRequest.setTime(dateRequest);
      Calendar calendarResponse = Calendar.getInstance();
      calendarResponse.setTime(dateResponse);
      result = calendarRequest.get(Calendar.YEAR) == calendarResponse.get(Calendar.YEAR)
          && calendarRequest.get(Calendar.MONTH) == calendarResponse.get(Calendar.MONTH)
          && calendarRequest.get(Calendar.DAY_OF_MONTH) == calendarResponse
              .get(Calendar.DAY_OF_MONTH);
    }
    return result;
  }
}
