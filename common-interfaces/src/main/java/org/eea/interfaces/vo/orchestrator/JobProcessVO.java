package org.eea.interfaces.vo.orchestrator;

import lombok.*;

import java.io.Serializable;

/**
 * The Class JobProcessVO.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class JobProcessVO implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -1348263779137653665L;

    /** The id. */
    private Long id;

    /** The jobId. */
    private Long jobId;

    /** The process id. */
    private String processId;
}
