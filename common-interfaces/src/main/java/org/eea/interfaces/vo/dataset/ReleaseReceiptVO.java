package org.eea.interfaces.vo.dataset;
import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ReleaseReceiptVO implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The ID of the release receipt. */
    private Long id;

    /** The custom text for the release receipt. */
    private String note;

    /** The ID of the dataflow associated with the release receipt. */
    private Long dataflowId;

    /**
     * Instantiates a new ReleaseReceiptTextVO.
     */
    public ReleaseReceiptVO() {
    }

    /**
     * Instantiates a new ReleaseReceiptTextVO with parameters.
     *
     * @param id the ID of the release receipt
     * @param note the custom text for the release receipt
     * @param dataflowId the ID of the associated dataflow
     */
    public ReleaseReceiptVO(Long id, String note, Long dataflowId) {
        this.id = id;
        this.note = note;
        this.dataflowId = dataflowId;
    }

}
