package org.eea.rod.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.eea.interfaces.vo.rod.ClientVO;
import org.eea.interfaces.vo.rod.CountryVO;
import org.eea.interfaces.vo.rod.IssueVO;
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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class ObligationServiceImplTest {

  @InjectMocks
  private ObligationServiceImpl obligationService;
  @Mock
  private ObligationFeignRepository obligationFeignRepository;

  @Mock
  private ClientFeignRepository clientFeignRepository;

  @Mock
  private CountryFeignRepository countryFeignRepository;

  @Mock
  private IssueFeignRepository issueFeignRepository;

  @Mock
  private ObligationMapper obligationMapper;

  @Mock
  private ClientMapper clientMapper;

  @Mock
  private CountryMapper countryMapper;

  @Mock
  private IssueMapper issueMapper;

  @Before
  public void init() {
    MockitoAnnotations.openMocks(this);

    //Set up issues mock
    List<Issue> issues = new ArrayList<>();
    Issue issue = new Issue();
    issue.setIssueId(1);
    issues.add(issue);
    Mockito.when(issueFeignRepository.findAll()).thenReturn(issues);
    List<IssueVO> issueVOs = new ArrayList<>();
    IssueVO issueVO = new IssueVO();
    issueVO.setIssueId(1);
    issueVOs.add(issueVO);
    Mockito.when(issueMapper.entityListToClass(Mockito.eq(issues))).thenReturn(issueVOs);

    //Set up countries mock
    List<Country> countries = new ArrayList<>();
    Country country = new Country();
    country.setSpatialId(1);
    countries.add(country);
    Mockito.when(countryFeignRepository.findAll()).thenReturn(countries);
    List<CountryVO> countryVOs = new ArrayList<>();
    CountryVO countryVO = new CountryVO();
    countryVO.setSpatialId(1);
    countryVOs.add(countryVO);
    Mockito.when(countryMapper.entityListToClass(Mockito.eq(countries))).thenReturn(countryVOs);

    //Set up clients mock
    List<Client> clients = new ArrayList<>();
    Client client = new Client();
    client.setClientId(1);
    clients.add(client);
    Mockito.when(clientFeignRepository.findAll()).thenReturn(clients);
    ClientVO clientVO = new ClientVO();
    clientVO.setClientId(1);
    Mockito.when(clientMapper.entityToClass(Mockito.eq(client))).thenReturn(clientVO);
  }

  @Test
  public void findOpenedObligation() {
    List<Obligation> obligations = new ArrayList<>();
    Obligation obligation = new Obligation();
    obligation.setObligationId(1);
    obligation.setClientId("1");
    obligation.setIssueId("1,");
    obligation.setSpatialId("1,");
    obligations.add(obligation);
    Mockito.when(obligationFeignRepository
        .findOpenedObligations(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(), Mockito.any(
            Long.class), Mockito.any(Long.class))).thenReturn(obligations);
    List<ObligationVO> obligationVOs = new ArrayList<>();
    ObligationVO obligationVO = new ObligationVO();
    obligationVO.setObligationId(1);

    List<IssueVO> issues = new ArrayList<>();
    IssueVO issueVO = new IssueVO();
    issueVO.setIssueId(1);
    issues.add(issueVO);

    List<CountryVO> countries = new ArrayList<>();
    CountryVO countryVO = new CountryVO();
    countryVO.setSpatialId(1);
    countries.add(countryVO);

    ClientVO clientVO = new ClientVO();
    clientVO.setClientId(1);

    obligationVO.setIssues(issues);
    obligationVO.setCountries(countries);
    obligationVO.setClient(clientVO);

    obligationVOs.add(obligationVO);
    Mockito.when(obligationMapper.entityListToClass(Mockito.eq(obligations)))
        .thenReturn(obligationVOs);
    List<ObligationVO> result = obligationService
        .findOpenedObligation(1, 1, 1, new Date(), new Date());
    Assert.assertNotNull(result);
    Assert.assertEquals(1, result.size());
    ObligationVO resultObligationVO = result.get(0);
    Assert.assertEquals(1, resultObligationVO.getObligationId().intValue());
    Assert.assertEquals(1, resultObligationVO.getClient().getClientId().intValue());
    Assert.assertEquals(1, resultObligationVO.getCountries().size());
    Assert.assertEquals(1, resultObligationVO.getCountries().get(0).getSpatialId().intValue());
    Assert.assertEquals(1, resultObligationVO.getIssues().get(0).getIssueId().intValue());
  }

  @Test
  public void findObligationById() {
    Obligation obligation = new Obligation();
    obligation.setObligationId(1);
    obligation.setClientId("1");
    obligation.setIssueId("1,");
    obligation.setSpatialId("1,");
    Mockito.when(obligationFeignRepository.findObligationById(Mockito.eq(1)))
        .thenReturn(obligation);

    ObligationVO obligationVO = new ObligationVO();
    obligationVO.setObligationId(1);

    List<IssueVO> issues = new ArrayList<>();
    IssueVO issueVO = new IssueVO();
    issueVO.setIssueId(1);
    issues.add(issueVO);

    List<CountryVO> countries = new ArrayList<>();
    CountryVO countryVO = new CountryVO();
    countryVO.setSpatialId(1);
    countries.add(countryVO);

    ClientVO clientVO = new ClientVO();
    clientVO.setClientId(1);

    obligationVO.setIssues(issues);
    obligationVO.setCountries(countries);
    obligationVO.setClient(clientVO);
    Mockito.when(obligationMapper.entityToClass(Mockito.eq(obligation)))
        .thenReturn(obligationVO);
    ObligationVO result = obligationService.findObligationById(1);

    Assert.assertEquals(1, result.getObligationId().intValue());
    Assert.assertEquals(1, result.getClient().getClientId().intValue());
    Assert.assertEquals(1, result.getCountries().size());
    Assert.assertEquals(1, result.getCountries().get(0).getSpatialId().intValue());
    Assert.assertEquals(1, result.getIssues().get(0).getIssueId().intValue());
  }
}