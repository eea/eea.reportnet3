package org.eea.dataflow.service.impl;

import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ContributorServiceImplTest {

  @InjectMocks
  private ContributorServiceImpl contributorServiceImpl;


  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void test() {
    assertTrue(true);
  }
}
