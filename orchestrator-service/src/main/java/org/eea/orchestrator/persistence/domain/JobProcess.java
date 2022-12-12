package org.eea.orchestrator.persistence.domain;

import lombok.*;

import javax.persistence.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "JOB_PROCESS")
public class JobProcess {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "job_process_id_seq")
    @SequenceGenerator(name = "job_process_id_seq", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(name = "job_id")
    private Long jobId;

    @Column(name = "process_id")
    private String processId;

    @Column(name = "dataset_id")
    private Long datasetId;

    @Column(name = "saga_transaction_id")
    private String sagaTransactionId;

    @Column(name = "aggregate_id")
    private String aggregateId;
}
