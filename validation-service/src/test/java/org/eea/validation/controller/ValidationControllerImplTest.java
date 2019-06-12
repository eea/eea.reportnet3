package org.eea.validation.controller;

import org.eea.interfaces.controller.validation.ValidationController;
import org.eea.validation.service.impl.ValidationServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ValidationControllerImplTest {

  @InjectMocks
  ValidationController dataSetControllerImpl;

  @Mock
  ValidationServiceImpl datasetService;

  @Test
  public void test() {}

}
