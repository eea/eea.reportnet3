package org.eea.dataset.service.impl;

import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.ReleaseReceiptVO;
import org.eea.dataset.service.ReleaseReceiptService;
import org.eea.dataset.persistence.metabase.domain.ReleaseReceipt;
import org.eea.dataset.persistence.metabase.repository.ReleaseReceiptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class ReleaseReceiptServiceImpl implements ReleaseReceiptService {

    @Autowired
    private ReleaseReceiptRepository releaseReceiptRepository;

    @Override
    public ReleaseReceiptVO saveOrUpdateReleaseReceipt(ReleaseReceiptVO releaseReceiptVO) throws EEAException {
        ReleaseReceipt existingReceipt = releaseReceiptRepository.findByDataflowId(releaseReceiptVO.getDataflowId());
        if (existingReceipt != null) {
            existingReceipt.setNote(releaseReceiptVO.getNote());
            releaseReceiptRepository.save(existingReceipt);
            return mapToVO(existingReceipt);
        } else {
            ReleaseReceipt releaseReceipt = new ReleaseReceipt();
            releaseReceipt.setDataflowId(releaseReceiptVO.getDataflowId());
            releaseReceipt.setNote(releaseReceiptVO.getNote());
            releaseReceiptRepository.save(releaseReceipt);
            return mapToVO(releaseReceipt);
        }
    }

    @Override
    public ReleaseReceiptVO getReleaseReceipt(Long id) throws EEAException {
        if (id == null || id <= 0) {
            throw new EEAException("Invalid release receipt ID: " + id);
        }

        ReleaseReceipt releaseReceipt = releaseReceiptRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ReleaseReceipt not found for ID: " + id));
        if (releaseReceipt == null) {
            throw new EEAException(EEAErrorMessage.RELEASE_RECEIPT_NOTFOUND);
        }
        return mapToVO(releaseReceipt);
    }

    @Override
    public ReleaseReceiptVO getReleaseReceiptByDataflowId(Long dataflowId) throws EEAException {
        ReleaseReceipt releaseReceipt = releaseReceiptRepository.findByDataflowId(dataflowId);
        if (releaseReceipt == null) {
            throw new EEAException(EEAErrorMessage.RELEASE_RECEIPT_NOTFOUND);
        }
        return mapToVO(releaseReceipt);
    }

    private ReleaseReceiptVO mapToVO(ReleaseReceipt releaseReceipt) {
        ReleaseReceiptVO releaseReceiptVO = new ReleaseReceiptVO();
        releaseReceiptVO.setId(releaseReceipt.getId());
        releaseReceiptVO.setDataflowId(releaseReceipt.getDataflowId());
        releaseReceiptVO.setNote(releaseReceipt.getNote());
        return releaseReceiptVO;
    }

}

