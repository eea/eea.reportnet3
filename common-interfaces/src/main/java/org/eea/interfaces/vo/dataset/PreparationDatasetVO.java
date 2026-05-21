package org.eea.interfaces.vo.dataset;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PreparationDatasetVO implements Serializable {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = -4127395810264479123L;

    private Long id;

    /**
     * Parent dataflow id.
     */
    private Long dataflowId;

    /**
     * Parent provider id.
     */
    private Long providerId;

    /**
     * Preparation dataset display name.
     */
    private String datasetName;

    /**
     * Preparation dataset code.
     */
    private String code;

    /**
     * Indicates whether the preparation dataset has been created.
     */
    private Boolean isCreated;
}
