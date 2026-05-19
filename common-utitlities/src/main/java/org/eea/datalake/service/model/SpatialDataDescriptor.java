package org.eea.datalake.service.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SpatialDataDescriptor {
    private Integer srid;
    private String type;
    private Double sizeMB;
    private String dimension;
}
