package org.eea.interfaces.vo.orchestrator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class JobsVO implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -1348263779137653665L;

    /** The process list. */
    private List<JobVO> jobsList;

    /** The total records. */
    private Long totalRecords;

    /** The filtered records. */
    private Long filteredRecords;
}
