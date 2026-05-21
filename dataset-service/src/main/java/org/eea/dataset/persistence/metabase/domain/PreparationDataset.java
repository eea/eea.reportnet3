package org.eea.dataset.persistence.metabase.domain;

import java.util.Objects;
import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The type PreparationDataset.
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "PREPARATION_DATASET")
public class PreparationDataset {

    /** The id. */
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "preparation_dataset_id_seq"
    )
    @SequenceGenerator(
            name = "preparation_dataset_id_seq",
            sequenceName = "preparation_dataset_id_seq",
            allocationSize = 1
    )
    @Column(name = "ID", columnDefinition = "serial")
    private Long id;

    /** Parent dataflow id. */
    @Column(name = "DATAFLOW_ID")
    private Long dataflowId;

    /** Provider id. */
    @Column(name = "DATA_PROVIDER_ID")
    private Long providerId;

    /** code for the preparation dataset. */
    @Column(name = "CODE")
    private String code;

    /** Preparation dataset name */
    @Column(name = "DATASET_NAME")
    private String datasetName;

    /** Indicates if the preparation dataset is created. */
    @Column(name = "IS_CREATED")
    private Boolean isCreated;

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final PreparationDataset that = (PreparationDataset) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
