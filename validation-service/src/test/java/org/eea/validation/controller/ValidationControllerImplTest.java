package org.eea.validation.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ValidationControllerImplTest {


  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @InjectMocks
  private ValidationControllerImpl validationController;

  @Test
  public void test() {

  }

}
