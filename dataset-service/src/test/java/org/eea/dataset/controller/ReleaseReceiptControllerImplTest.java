package org.eea.dataset.controller;

import org.eea.dataset.service.ReleaseReceiptService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.ReleaseReceiptVO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

public class ReleaseReceiptControllerImplTest {

    @InjectMocks
    private ReleaseReceiptControllerImpl releaseReceiptController;

    @Mock
    private ReleaseReceiptService releaseReceiptService;

    @Before
    public void init() {
        MockitoAnnotations.openMocks(this);
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void testCreateReleaseReceipt_Success() throws EEAException {
        ReleaseReceiptVO requestVO = new ReleaseReceiptVO();
        requestVO.setDataflowId(1L);
        requestVO.setNote("Test Note");

        ReleaseReceiptVO savedVO = new ReleaseReceiptVO();
        savedVO.setId(1L);
        savedVO.setDataflowId(1L);
        savedVO.setNote("Test Note");

        Mockito.when(releaseReceiptService.saveOrUpdateReleaseReceipt(Mockito.any())).thenReturn(savedVO);

        ResponseEntity<ReleaseReceiptVO> response = releaseReceiptController.createReleaseReceipt(requestVO);

        Assert.assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Assert.assertEquals(savedVO, response.getBody());
    }

    @Test(expected = ResponseStatusException.class)
    public void testCreateReleaseReceipt_MissingDataflowId() {
        ReleaseReceiptVO requestVO = new ReleaseReceiptVO();

        releaseReceiptController.createReleaseReceipt(requestVO);
    }

    @Test
    public void testUpdateReleaseReceipt_Success() throws EEAException {
        ReleaseReceiptVO requestVO = new ReleaseReceiptVO();
        requestVO.setDataflowId(1L);
        requestVO.setNote("Updated Note");

        ReleaseReceiptVO updatedVO = new ReleaseReceiptVO();
        updatedVO.setId(1L);
        updatedVO.setDataflowId(1L);
        updatedVO.setNote("Updated Note");

        Mockito.when(releaseReceiptService.saveOrUpdateReleaseReceipt(Mockito.any())).thenReturn(updatedVO);

        ResponseEntity<ReleaseReceiptVO> response = releaseReceiptController.updateReleaseReceipt(requestVO);

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(updatedVO, response.getBody());
    }

    @Test
    public void testGetReleaseReceiptById_Success() throws EEAException {
        ReleaseReceiptVO expectedVO = new ReleaseReceiptVO();
        expectedVO.setId(1L);
        expectedVO.setDataflowId(1L);
        expectedVO.setNote("Test Note");

        Mockito.when(releaseReceiptService.getReleaseReceipt(1L)).thenReturn(expectedVO);

        ResponseEntity<ReleaseReceiptVO> response = releaseReceiptController.getReleaseReceipt(1L);

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(expectedVO, response.getBody());
    }

    @Test(expected = ResponseStatusException.class)
    public void testGetReleaseReceiptById_NotFound() throws EEAException {
        Mockito.when(releaseReceiptService.getReleaseReceipt(1L)).thenThrow(new EEAException("Not found"));

        releaseReceiptController.getReleaseReceipt(1L);
    }

    @Test
    public void testGetReleaseReceiptByDataflowId_Success() throws EEAException {
        ReleaseReceiptVO expectedVO = new ReleaseReceiptVO();
        expectedVO.setDataflowId(1L);
        expectedVO.setNote("Test Note");

        Mockito.when(releaseReceiptService.getReleaseReceiptByDataflowId(1L)).thenReturn(expectedVO);

        ResponseEntity<ReleaseReceiptVO> response = releaseReceiptController.getReleaseReceiptByDataflowId(1L);

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(expectedVO, response.getBody());
    }

    @Test(expected = ResponseStatusException.class)
    public void testGetReleaseReceiptByDataflowId_NotFound() throws EEAException {
        Mockito.when(releaseReceiptService.getReleaseReceiptByDataflowId(1L)).thenThrow(new EEAException("Not found"));

        releaseReceiptController.getReleaseReceiptByDataflowId(1L);
    }
}
