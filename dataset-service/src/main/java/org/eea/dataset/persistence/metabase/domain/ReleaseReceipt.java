package org.eea.dataset.persistence.metabase.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "RELEASE_RECEIPT")
public class ReleaseReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "release_receipt_id_seq")
    @SequenceGenerator(name = "release_receipt_id_seq", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "DATAFLOW_ID", nullable = false)
    private Long dataflowId;

    @Column(name = "NOTE")
    private String note;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Transient
    private String additionalMetadata;

    public ReleaseReceipt(Long dataflowId, String note, LocalDateTime updatedAt, String userEmail) {
        this.dataflowId = dataflowId;
        this.note = note;
        this.updatedAt = updatedAt;
    }
}
