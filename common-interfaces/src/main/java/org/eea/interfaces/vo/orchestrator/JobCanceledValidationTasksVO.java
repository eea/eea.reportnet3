package org.eea.interfaces.vo.orchestrator;

import lombok.*;

import java.io.Serializable;

/**
 * The Class JobCanceledValidationTasksVO.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class JobCanceledValidationTasksVO implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 8101234578901234567L;

    /** The task id. */
    private Long taskId;

    /** The rule code. */
    private String ruleCode;

    /** The rule id (SQL Rule). */
    private String ruleId;

    /** The rule level error. */
    private String ruleLevelError;
}
