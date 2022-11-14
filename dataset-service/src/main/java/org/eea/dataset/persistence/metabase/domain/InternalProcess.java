package org.eea.dataset.persistence.metabase.domain;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@Setter
@ToString
@Table(name = "internal_process", schema = "public")
public class InternalProcess implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 331689131017596907L;

    public InternalProcess() {
    }

    public InternalProcess(String type, Long dataflowId, Long dataProviderId, Long dataCollectionId, String transactionId, String aggregateId) {
        this.type = type;
        this.dataflowId = dataflowId;
        this.dataProviderId = dataProviderId;
        this.dataCollectionId = dataCollectionId;
        this.transactionId = transactionId;
        this.aggregateId = aggregateId;
    }

    /**
     * The id.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "internal_process_id_seq")
    @SequenceGenerator(name = "internal_process_id_seq", sequenceName = "internal_process_id_seq",
            allocationSize = 1)
    @Column(name = "id", columnDefinition = "serial")
    private Long id;

    @Column(name = "type")
    private String type;

    @Column(name = "dataflow_id")
    private Long dataflowId;

    @Column(name = "data_provider_id")
    private Long dataProviderId;

    @Column(name = "data_collection_id")
    private Long dataCollectionId;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "aggregate_id")
    private String aggregateId;

}























