package org.eea.dataset.service.impl;

import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.ReleaseReceiptVO;
import org.eea.dataset.service.ReleaseReceiptService;
import org.eea.dataset.persistence.metabase.domain.ReleaseReceipt;
import org.eea.dataset.persistence.metabase.repository.ReleaseReceiptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;


@Service
public class ReleaseReceiptServiceImpl implements ReleaseReceiptService {

    @Autowired
    private ReleaseReceiptRepository releaseReceiptRepository;

    @Override
    public ReleaseReceiptVO saveOrUpdateReleaseReceipt(ReleaseReceiptVO releaseReceiptVO) {
        Optional<ReleaseReceipt> existingReceipt = releaseReceiptRepository.findByDataflowId(releaseReceiptVO.getDataflowId());

        if (existingReceipt.isPresent()) {
            ReleaseReceipt receipt = existingReceipt.get();
            receipt.setNote(releaseReceiptVO.getNote());
            releaseReceiptRepository.save(receipt);
            return mapToVO(receipt);
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
                .orElseThrow(() -> new EEAException(EEAErrorMessage.RELEASE_RECEIPT_NOTFOUND));

        return mapToVO(releaseReceipt);
    }


    @Override
    public ReleaseReceiptVO getReleaseReceiptByDataflowId(Long dataflowId) throws EEAException {
        ReleaseReceipt releaseReceipt = releaseReceiptRepository.findByDataflowId(dataflowId).orElse(null);
        return releaseReceipt != null ? mapToVO(releaseReceipt) : null;
    }

    @Override
    public void deleteReleaseReceiptByDataflowId(Long dataflowId) throws EEAException {
        if (dataflowId == null || dataflowId <= 0) {
            throw new EEAException("Invalid dataflow ID: " + dataflowId);
        }

        releaseReceiptRepository.deleteByDataflowId(dataflowId);
    }


    private ReleaseReceiptVO mapToVO(ReleaseReceipt releaseReceipt) {
        ReleaseReceiptVO releaseReceiptVO = new ReleaseReceiptVO();
        releaseReceiptVO.setId(releaseReceipt.getId());
        releaseReceiptVO.setDataflowId(releaseReceipt.getDataflowId());
        releaseReceiptVO.setNote(releaseReceipt.getNote());
        return releaseReceiptVO;
    }

}

