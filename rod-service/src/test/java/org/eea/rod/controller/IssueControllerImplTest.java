package org.eea.rod.controller;

import java.util.ArrayList;
import java.util.List;
import org.eea.interfaces.vo.rod.CountryVO;
import org.eea.interfaces.vo.rod.IssueVO;
import org.eea.rod.service.IssueService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class IssueControllerImplTest {

  @InjectMocks
  private IssueControllerImpl issueController;
  @Mock
  private IssueService issueService;

  @Before
  public void init() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void findAll() {
    List<IssueVO> dataList = new ArrayList<>();
    IssueVO data = new IssueVO();
    data.setIssueId(1);
    dataList.add(data);
    Mockito.when(issueService.findAll()).thenReturn(dataList);

    List<IssueVO> result = issueController.findAll();
    Assert.assertNotNull(result);
    Assert.assertEquals(1, result.size());
    Assert.assertEquals(1, result.get(0).getIssueId().intValue());
  }
}