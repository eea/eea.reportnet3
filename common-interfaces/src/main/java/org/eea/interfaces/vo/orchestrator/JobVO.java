package org.eea.interfaces.vo.orchestrator;

import lombok.*;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.interfaces.vo.orchestrator.enums.JobTypeEnum;

import java.sql.Timestamp;
import java.util.Map;

/**
 * The Class JobVO.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class JobVO {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -1348263779137653665L;

    /** The id. */
    private Long id;

    /** The type of the job. */
    private JobTypeEnum jobType;

    /** The status of the job. */
    private JobStatusEnum jobStatus;

    /** The date the job was added. */
    private Timestamp dateAdded;

    /** The date that the status changed. */
    private Timestamp dateStatusChanged;

    /** The parameters. */
    private Map<String, Object> parameters;

    /** The creator username. */
    private String creatorUsername;

    /** The release */
    private boolean release;

    /** The dataflow id */
    private Long dataflowId;

    /** The data provider id */
    private Long providerId;

    /** The dataset id */
    private Long datasetId;

    private String fmeJobId;
}
