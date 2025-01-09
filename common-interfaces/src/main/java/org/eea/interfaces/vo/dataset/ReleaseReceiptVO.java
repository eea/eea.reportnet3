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
    private String userCustomText;

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
     * @param userCustomText the custom text for the release receipt
     * @param dataflowId the ID of the associated dataflow
     */
    public ReleaseReceiptVO(Long id, String userCustomText, Long dataflowId) {
        this.id = id;
        this.userCustomText = userCustomText;
        this.dataflowId = dataflowId;
    }

}
