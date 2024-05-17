package org.eea.interfaces.vo.dataset;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DatasetTableVO implements Serializable {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = -1348263779137653665L;

    private Long id;
    private Long datasetId;
    private String datasetSchemaId;
    private String tableSchemaId;
    private Boolean isIcebergTableCreated;
}
