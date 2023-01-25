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
    @Column(name = "ID")
    private Long id;

    @Column(name = "JOB_ID")
    private Long jobId;

    @Column(name = "PROCESS_ID")
    private String processId;
}
