package org.eea.rod.service.impl;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import org.eea.interfaces.vo.rod.IssueVO;
import org.eea.rod.mapper.IssueMapper;
import org.eea.rod.persistence.domain.Issue;
import org.eea.rod.persistence.repository.IssueFeignRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class IssueServiceImplTest {

  @InjectMocks
  private IssueServiceImpl IssueService;
  @Mock
  private org.eea.rod.persistence.repository.IssueFeignRepository IssueFeignRepository;
  @Mock
  private org.eea.rod.mapper.IssueMapper IssueMapper;

  @Before
  public void init() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void findAll() {
    List<IssueVO> dataVoList = new ArrayList<>();
    IssueVO dataVO = new IssueVO();
    dataVO.setIssueId(1);
    dataVoList.add(dataVO);

    List<Issue> dataList = new ArrayList<>();
    Issue data = new Issue();
    data.setIssueId(1);
    dataList.add(data);
    Mockito.when(IssueFeignRepository.findAll()).thenReturn(dataList);
    Mockito.when(IssueMapper.entityListToClass(Mockito.eq(dataList))).thenReturn(dataVoList);
    List<IssueVO> result = IssueService.findAll();
    Assert.assertNotNull(result);
    Assert.assertEquals(1, result.size());
    Assert.assertEquals(1, result.get(0).getIssueId().intValue());
  }
}