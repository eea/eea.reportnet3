package org.eea.interfaces.vo.orchestrator;

import lombok.*;

/**
 * The Class JobProcessVO.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class JobProcessVO {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -1348263779137653665L;

    /** The id. */
    private Long id;

    /** The jobId. */
    private Long jobId;

    /** The process id. */
    private String processId;

    /** The dataset id */
    private Long datasetId;

    /** The saga transaction id */
    private String sagaTransactionId;

    /** The aggregate id */
    private String aggregateId;
}
