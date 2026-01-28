package org.eea.interfaces.vo.dataset;

import lombok.*;
import javax.validation.constraints.NotNull;

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
     * Parent dataset id
     */
    private Long parentDatasetId;

    /**
     * Parent dataflow id.
     */
    @NotNull
    private Long dataflowId;

    /**
     * Parent provider id.
     */
    @NotNull
    private Long providerId;

    /**
     * Preparation dataset display name.
     */
    @NotNull
    private String datasetName;

    /**
     * Preparation dataset code.
     */
    @NotNull
    private String code;

    /**
     * Indicates whether the preparation dataset has been created.
     */
    private Boolean isCreated;
}
