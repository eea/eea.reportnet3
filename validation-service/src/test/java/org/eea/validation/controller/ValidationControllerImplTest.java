package org.eea.validation.controller;

import org.eea.interfaces.controller.validation.ValidationController;
import org.junit.Before;
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
  private ValidationController validationController;


  public void test() {


  }

}
