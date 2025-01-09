package org.eea.dataset.service;


import org.eea.interfaces.vo.dataset.ReleaseReceiptVO;

public interface ReleaseReceiptService {

    /**
     * Saves the ReleaseReceiptText.
     *
     * @param releaseReceiptVO the VO containing the user custom text to save
     */
    void saveReleaseReceiptText(ReleaseReceiptVO releaseReceiptVO);

    /**
     * Retrieves the ReleaseReceiptText for a specific ID.
     *
     * @param id the ID of the release receipt
     * @return the VO containing the user custom text
     */
    ReleaseReceiptVO getReleaseReceiptText(Long id);

}
