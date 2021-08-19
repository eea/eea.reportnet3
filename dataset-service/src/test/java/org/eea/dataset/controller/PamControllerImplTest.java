package org.eea.dataset.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import java.util.ArrayList;
import org.eea.dataset.service.PaMService;
import org.eea.exception.EEAException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

public class PamControllerImplTest {


  @InjectMocks
  private PamControllerImpl pamController;

  @Mock
  private PaMService paMService;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testGetListSinglePaM() {
    assertEquals(new ArrayList<>(), pamController.getListSinglePaM(1L, "1"));
  }

  @Test(expected = ResponseStatusException.class)
  public void testThrowGetListSinglePaM() throws EEAException {
    doThrow(new EEAException()).when(paMService).getListSinglePaM(Mockito.any(), Mockito.any());
    try {
      pamController.getListSinglePaM(1L, "1");
    } catch (ResponseStatusException e) {
      throw e;
    }
  }

}
