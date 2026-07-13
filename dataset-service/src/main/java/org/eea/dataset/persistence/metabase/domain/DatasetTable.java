package org.eea.dataset.persistence.metabase.domain;

import lombok.*;
import javax.persistence.*;
import java.util.Date;

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

    @Column(name = "PREPARATION_CODE")
    private String preparationCode;

    @Column(name = "DATASET_SCHEMA_ID")
    private String datasetSchemaId;

    @Column(name = "TABLE_SCHEMA_ID")
    private String tableSchemaId;

    @Column(name = "IS_ICEBERG_TABLE_CREATED")
    private Boolean isIcebergTableCreated;

    @Column(name = "EDITING_USERNAME")
    private String editingUsername;

    @Column(name = "EDIT_LOCK_EXPIRES_AT")
    private Date editLockExpirationDate;

    @Transient
    private String tableName;

    public DatasetTable(Long datasetId, String preparationCode, String datasetSchemaId, String tableSchemaId, Boolean isIcebergTableCreated, String editingUsername, Date editLockExpirationDate) {
        this.datasetId = datasetId;
        this.datasetSchemaId = datasetSchemaId;
        this.tableSchemaId = tableSchemaId;
        this.isIcebergTableCreated = isIcebergTableCreated;
        this.editingUsername = editingUsername;
        this.editLockExpirationDate = editLockExpirationDate;
        this.preparationCode = preparationCode;
    }
}
