package org.eea.interfaces.vo.dataset;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class InternalProcessVO {

    public InternalProcessVO() {

    }

    public InternalProcessVO(String type, Long dataflowId, Long dataProviderId, Long dataCollectionId, String transactionId, String aggregateId) {
        this.type = type;
        this.dataflowId = dataflowId;
        this.dataProviderId = dataProviderId;
        this.dataCollectionId = dataCollectionId;
        this.transactionId = transactionId;
        this.aggregateId = aggregateId;
    }

    private Long id;

    private String type;

    private Long dataflowId;

    private Long dataProviderId;

    private Long dataCollectionId;

    private String transactionId;

    private String aggregateId;
}























