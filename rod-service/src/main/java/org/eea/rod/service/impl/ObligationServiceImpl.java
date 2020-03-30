package org.eea.rod.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.eea.interfaces.vo.rod.LegalInstrumentVO;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * The type Obligation service.
 */
@Service
public class ObligationServiceImpl implements ObligationService {

  @Autowired
  private ObligationFeignRepository obligationFeignRepository;

  @Autowired
  private ClientFeignRepository clientFeignRepository;

  @Autowired
  private CountryFeignRepository countryFeignRepository;

  @Autowired
  private IssueFeignRepository issueFeignRepository;

  @Autowired
  private ObligationMapper obligationMapper;

  @Autowired
  private ClientMapper clientMapper;

  @Autowired
  private CountryMapper countryMapper;

  @Autowired
  private IssueMapper issueMapper;

  @Override
  public List<ObligationVO> findOpenedObligation(Integer clientId,
      Integer spatialId,
      Integer issueId,
      Date deadlineDateFrom,
      Date deadlineDateTo) {
    List<Obligation> obligations = obligationFeignRepository
        .findOpenedObligations(clientId, issueId, spatialId, deadlineDateFrom.getTime(),
            deadlineDateTo.getTime());
    List<ObligationVO> obligationVOS = obligationMapper.entityListToClass(obligations);
    List<Client> clients = this.clientFeignRepository.findAll();
    List<Country> countries = new ArrayList<>();//this.countryFeignRepository.findAll(); this will not be necessary at the moment
    List<Issue> issues = new ArrayList<>();//this.issueFeignRepository.findAll();this will not be necessary at the moment

    for (int i = 0; i < obligations.size(); i++) {
      fillObligationSubentityFields(obligationVOS.get(i), obligations.get(i), clients, countries,
          issues);
    }
    return obligationVOS;
  }

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

  private void fillObligationSubentityFields(ObligationVO obligationVO, Obligation obligation,
      final List<Client> clients, final List<Country> countries, final List<Issue> issues) {

    //Map legal instrument information
    LegalInstrumentVO legalInstrumentVO = new LegalInstrumentVO();
    legalInstrumentVO.setSourceAlias(obligation.getSourceAlias());
    legalInstrumentVO.setSourceId(obligation.getSourceId());
    legalInstrumentVO.setSourceTitle(obligation.getSourceTitle());
    obligationVO.setLegalInstrument(legalInstrumentVO);

    //Find clients from rod. Clients are unique since they are the ones registering the Obligation
    if (StringUtils.isNotBlank(obligation.getClientId()) && !CollectionUtils.isEmpty(clients)) {
      Client client = clients.stream()
          .filter(clientValue -> clientValue.getClientId().toString()
              .equals(obligation.getClientId())).findFirst()
          .get();
      obligationVO.setClient(clientMapper.entityToClass(client));
    }
    //Find countries from rod. These are the countries bounded to the obligation. Might be several.
    if (StringUtils.isNotBlank(obligation.getSpatialId()) && !CollectionUtils.isEmpty(countries)) {
      //Countries in the obligation comes in comma separated values. Need to convert to a list before being treated.
      final List<String> spatialIds = obligation.getSpatialId().endsWith(",") ? Arrays.asList(
          obligation.getSpatialId().substring(0, obligation.getSpatialId().length() - 1).split(","))
          : Arrays.asList(obligation.getSpatialId().split(","));
      //Find in rod the countries bounded to the obligation and setting them to the final ObligationVO
      List<Country> filteredCountries = countries.stream()
          .filter(countryValue -> spatialIds.contains(countryValue.getSpatialId().toString()))
          .collect(
              Collectors.toList());
      obligationVO.setCountries(countryMapper.entityListToClass(filteredCountries));

      //Find issues from rod. These are the issues bounded to the obligation. Might be several.
      if (StringUtils.isNotBlank(obligation.getSpatialId()) && !CollectionUtils.isEmpty(issues)) {
        //Issues in the obligation comes in comma separated values. Need to convert to a list before being treated.
        final List<String> issuesIds = obligation.getIssueId().endsWith(",") ? Arrays.asList(
            obligation.getIssueId().substring(0, obligation.getIssueId().length() - 1)
                .split(","))
            : Arrays.asList(obligation.getIssueId().split(","));
        //Find in rod the issues bounded to the obligation and setting them to the final ObligationVO
        List<Issue> filteredIssues = issues.stream()
            .filter(issueValue -> issuesIds.contains(issueValue.getIssueId().toString()))
            .collect(
                Collectors.toList());
        obligationVO.setIssues(issueMapper.entityListToClass(filteredIssues));
      }

    }

  }
}
