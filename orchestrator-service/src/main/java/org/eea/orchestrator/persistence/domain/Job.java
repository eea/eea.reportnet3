package org.eea.orchestrator.persistence.domain;

import lombok.*;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.interfaces.vo.orchestrator.enums.JobTypeEnum;
import org.eea.utils.HashMapConverter;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Map;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "JOBS")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "jobs_id_seq")
    @SequenceGenerator(name = "jobs_id_seq", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "JOB_TYPE")
    @Enumerated(EnumType.STRING)
    private JobTypeEnum jobType;

    @Column(name = "JOB_STATUS")
    @Enumerated(EnumType.STRING)
    private JobStatusEnum jobStatus;

    @Column(name = "DATE_ADDED")
    private Timestamp dateAdded;

    @Column(name = "DATE_STATUS_CHANGED")
    private Timestamp dateStatusChanged;

    @Convert(converter = HashMapConverter.class)
    private Map<String, Object> parameters;

    @Column(name = "CREATOR_USERNAME")
    private String creatorUsername;

    @Column(name = "RELEASE")
    private boolean release;

    @Column(name = "DATAFLOW_ID")
    private Long dataflowId;

    @Column(name = "PROVIDER_ID")
    private Long providerId;

    @Column(name = "DATASET_ID")
    private Long datasetId;
}
