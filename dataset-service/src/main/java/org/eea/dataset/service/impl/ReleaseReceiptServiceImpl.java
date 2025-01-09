package org.eea.dataset.service.impl;

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
    public void saveReleaseReceiptText(ReleaseReceiptVO releaseReceiptVO) {
        // Map VO to Entity
        ReleaseReceipt releaseReceipt = new ReleaseReceipt();
        releaseReceipt.setUserCustomText(releaseReceiptVO.getUserCustomText());
        releaseReceipt.setDataflowId(releaseReceiptVO.getDataflowId());
        // Save to DB
        releaseReceiptRepository.save(releaseReceipt);
    }

    @Override
    public ReleaseReceiptVO getReleaseReceiptText(Long id) {
        // Fetch Entity from DB
        ReleaseReceipt releaseReceipt = releaseReceiptRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ReleaseReceipt not found for ID: " + id));

        // Map Entity to VO
        ReleaseReceiptVO releaseReceiptVO = new ReleaseReceiptVO();
        releaseReceiptVO.setUserCustomText(releaseReceipt.getUserCustomText());

        return releaseReceiptVO;
    }
}

