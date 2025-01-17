package org.eea.dataset.service;


import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.ReleaseReceiptVO;

public interface ReleaseReceiptService {

    /**
     * Saves the ReleaseReceipt.
     *
     * @param releaseReceiptVO the VO containing the user custom text to save
     * @return
     */
    ReleaseReceiptVO saveOrUpdateReleaseReceipt(ReleaseReceiptVO releaseReceiptVO) throws EEAException;

    /**
     * Retrieves the ReleaseReceipt a specific ID.
     *
     * @param id the ID of the release receipt
     * @return the VO containing the user custom text
     */
    ReleaseReceiptVO getReleaseReceipt(Long id) throws EEAException;

    /**
     * Retrieves the ReleaseReceipt for a specific Dataflow ID.
     *
     * @param dataflowId the ID of the Dataflow
     * @return the VO containing the user custom text
     */
    ReleaseReceiptVO getReleaseReceiptByDataflowId(Long dataflowId) throws EEAException;

    /**
     * Deletes the ReleaseReceipt for a specific Dataflow ID.
     *
     * @param dataflowId the ID of the Dataflow
     */
    void deleteReleaseReceiptByDataflowId(Long dataflowId) throws EEAException;

}
