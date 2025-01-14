package org.eea.dataset.service.impl;

import java.util.Optional;

import org.eea.dataset.persistence.metabase.domain.ReleaseReceipt;
import org.eea.dataset.persistence.metabase.repository.ReleaseReceiptRepository;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.ReleaseReceiptVO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class ReleaseReceiptServiceImplTest {

    @InjectMocks
    private ReleaseReceiptServiceImpl releaseReceiptService;

    @Mock
    private ReleaseReceiptRepository releaseReceiptRepository;

    @Before
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSaveOrUpdateReleaseReceipt_UpdateExisting() throws EEAException {
        Long dataflowId = 1L;
        ReleaseReceipt existingReceipt = new ReleaseReceipt();
        existingReceipt.setId(1L);
        existingReceipt.setDataflowId(dataflowId);
        existingReceipt.setNote("Old Note");

        ReleaseReceiptVO inputVO = new ReleaseReceiptVO();
        inputVO.setDataflowId(dataflowId);
        inputVO.setNote("Updated Note");

        Mockito.when(releaseReceiptRepository.findByDataflowId(dataflowId)).thenReturn(existingReceipt);

        ReleaseReceiptVO result = releaseReceiptService.saveOrUpdateReleaseReceipt(inputVO);

        Assert.assertNotNull(result);
        Assert.assertEquals(dataflowId, result.getDataflowId());
        Assert.assertEquals("Updated Note", result.getNote());
        Mockito.verify(releaseReceiptRepository, Mockito.times(1)).save(existingReceipt);
    }

    @Test
    public void testSaveOrUpdateReleaseReceipt_CreateNew() throws EEAException {
        Long dataflowId = 1L;
        ReleaseReceiptVO inputVO = new ReleaseReceiptVO();
        inputVO.setDataflowId(dataflowId);
        inputVO.setNote("New Note");

        Mockito.when(releaseReceiptRepository.findByDataflowId(dataflowId)).thenReturn(null);

        ReleaseReceiptVO result = releaseReceiptService.saveOrUpdateReleaseReceipt(inputVO);

        Assert.assertNotNull(result);
        Assert.assertEquals(dataflowId, result.getDataflowId());
        Assert.assertEquals("New Note", result.getNote());
        Mockito.verify(releaseReceiptRepository, Mockito.times(1)).save(Mockito.any(ReleaseReceipt.class));
    }

    @Test(expected = EEAException.class)
    public void testGetReleaseReceipt_InvalidId() throws EEAException {
        releaseReceiptService.getReleaseReceipt(null);
    }

    @Test
    public void testGetReleaseReceipt_ValidId() throws EEAException {
        Long id = 1L;
        ReleaseReceipt releaseReceipt = new ReleaseReceipt();
        releaseReceipt.setId(id);
        releaseReceipt.setDataflowId(1L);
        releaseReceipt.setNote("Test Note");

        Mockito.when(releaseReceiptRepository.findById(id)).thenReturn(Optional.of(releaseReceipt));

        ReleaseReceiptVO result = releaseReceiptService.getReleaseReceipt(id);

        Assert.assertNotNull(result);
        Assert.assertEquals(id, result.getId());
        Assert.assertEquals("Test Note", result.getNote());
    }

    @Test(expected = EEAException.class)
    public void testGetReleaseReceipt_NotFound() throws EEAException {
        Long id = 1L;

        Mockito.when(releaseReceiptRepository.findById(id)).thenReturn(Optional.empty());

        releaseReceiptService.getReleaseReceipt(id);
    }

    @Test
    public void testGetReleaseReceiptByDataflowId_Success() throws EEAException {
        Long dataflowId = 1L;
        ReleaseReceipt releaseReceipt = new ReleaseReceipt();
        releaseReceipt.setId(1L);
        releaseReceipt.setDataflowId(dataflowId);
        releaseReceipt.setNote("Test Note");

        Mockito.when(releaseReceiptRepository.findByDataflowId(dataflowId)).thenReturn(releaseReceipt);

        ReleaseReceiptVO result = releaseReceiptService.getReleaseReceiptByDataflowId(dataflowId);

        Assert.assertNotNull(result);
        Assert.assertEquals(dataflowId, result.getDataflowId());
        Assert.assertEquals("Test Note", result.getNote());
    }

    @Test(expected = EEAException.class)
    public void testGetReleaseReceiptByDataflowId_NotFound() throws EEAException {
        Long dataflowId = 1L;

        Mockito.when(releaseReceiptRepository.findByDataflowId(dataflowId)).thenReturn(null);

        releaseReceiptService.getReleaseReceiptByDataflowId(dataflowId);
    }
}
