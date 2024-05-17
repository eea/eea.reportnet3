package org.eea.dataset.persistence.metabase.domain;

import lombok.*;
import javax.persistence.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "DATASET_TABLE")
public class DatasetTable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dataset_table_id_seq")
    @SequenceGenerator(name = "dataset_table_id_seq", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "DATASET_ID")
    private Long datasetId;

    @Column(name = "DATASET_SCHEMA_ID")
    private String datasetSchemaId;

    @Column(name = "TABLE_SCHEMA_ID")
    private String tableSchemaId;

    @Column(name = "IS_ICEBERG_TABLE_CREATED")
    private Boolean isIcebergTableCreated;
}
