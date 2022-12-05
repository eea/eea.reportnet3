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
@Table(name = "JOB_HISTORY")
public class JobHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "job_history_id_seq")
    @SequenceGenerator(name = "job_history_id_seq", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "JOB_ID")
    private Long jobId;

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

}
